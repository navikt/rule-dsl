package no.nav.system.ruledsl.core.trace

import no.nav.system.ruledsl.core.expression.Faktum
import no.nav.system.ruledsl.core.helper.checkmark
import no.nav.system.ruledsl.core.reference.Reference

/**
 * Node in an explanation tree.
 * Used as intermediate representation that can be transformed to any output format.
 */
data class ExplanationNode(
    val kind: ExplanationKind,
    val text: String,
    val references: List<Reference> = emptyList(),
    val children: MutableList<ExplanationNode> = mutableListOf()
)

enum class ExplanationKind {
    /** WHAT - the result value */
    HVA,
    /** WHY - which rules fired to produce this result */
    HVORFOR,
    /** HOW - the calculation/formula */
    HVORDAN,
    /** A rule that fired */
    REGEL,
    /** A predicate that was evaluated */
    PREDIKAT,
    /** A formula/expression */
    FORMEL
}

/**
 * Transformer interface for converting explanation nodes to custom output formats.
 */
fun interface ExplanationTransformer<R> {
    fun transform(root: ExplanationNode): R
}

/**
 * Default text transformer - produces indented text output.
 */
object DefaultTextTransformer : ExplanationTransformer<String> {
    override fun transform(root: ExplanationNode): String = buildString {
        fun walk(node: ExplanationNode, indent: String = "") {
            appendLine("$indent${node.text}")
            if (node.references.isNotEmpty()) {
                node.references.forEach { ref ->
                    appendLine("$indent  REF: ${ref.id} : ${ref.url}")
                }
            }
            node.children.forEach { walk(it, "$indent  ") }
        }
        walk(root)
    }
}

/**
 * Build an explanation tree for a Faktum, starting from the result and walking backwards.
 *
 * Recursively follows two dependency paths:
 * 1. Predicate dependencies: Faktums used in rule predicates (e.g., `bool erLik true` depends on `bool`)
 * 2. Formula dependencies: Faktums used in calculations (e.g., `result = a * b` depends on `a` and `b`)
 *
 * @param filter Whether to include all rules or only fired rules (FUNCTIONAL)
 * @return ExplanationNode tree that can be transformed to any format
 */
fun Faktum<*>.buildExplanation(filter: TraceFilter = TraceFilter.FUNCTIONAL): ExplanationNode {
    return buildExplanationInternal(filter, mutableSetOf())
}

/**
 * Internal implementation with visited tracking to prevent infinite loops.
 */
private fun Faktum<*>.buildExplanationInternal(
    filter: TraceFilter,
    visited: MutableSet<Faktum<*>>
): ExplanationNode {
    // Prevent infinite loops on circular dependencies
    if (this in visited) {
        return ExplanationNode(ExplanationKind.HVA, "$name = $value (see above)")
    }
    visited.add(this)
    
    val root = ExplanationNode(ExplanationKind.HVA, "HVA")
    
    // Add the result value
    root.children.add(ExplanationNode(ExplanationKind.FORMEL, "$name = $value"))
    
    // Find the source node (the rule that produced this Faktum)
    val source = sourceNode
    if (source != null) {
        // HVORFOR - walk up the tree to collect fired rules
        val hvorfor = ExplanationNode(ExplanationKind.HVORFOR, "HVORFOR")
        val path = source.pathFromRoot()
        
        // Collect Faktums used in predicates for recursive explanation
        val predicateDependencies = mutableSetOf<Faktum<*>>()
        
        // Collect rules in the path that fired (skip root which is the Trace itself)
        path.drop(1).filter { it.fired || filter == TraceFilter.ALL }.forEach { node ->
            val ruleNode = ExplanationNode(
                ExplanationKind.REGEL,
                "${node.fired.checkmark()} ${node.name}",
                references = node.references
            )
            
            // Add expressions for this rule (both predicates and formulas)
            node.expressions.forEach { expr ->
                when (val exprValue = expr.value) {
                    is Boolean -> {
                        // Boolean expression (predicate)
                        val status = exprValue.checkmark()
                        if (exprValue || filter == TraceFilter.ALL) {
                            ruleNode.children.add(
                                ExplanationNode(ExplanationKind.PREDIKAT, "$status $expr")
                            )
                            // Collect Faktums used in this expression for recursive explanation
                            expr.faktumSet()
                                .filter { it != this && it.sourceNode != null && it !in visited }
                                .forEach { predicateDependencies.add(it) }
                        }
                    }
                    else -> {
                        // Non-boolean expression (formula) - handled in HVORDAN section
                    }
                }
            }
            
            hvorfor.children.add(ruleNode)
        }
        
        if (hvorfor.children.isNotEmpty()) {
            root.children.add(hvorfor)
        }
        
        // Recursively explain Faktums that this rule depended on (from predicates)
        predicateDependencies.forEach { dependency ->
            val dependencyExplanation = dependency.buildExplanationInternal(filter, visited)
            // Add as a child section showing the dependency chain
            val dependencyNode = ExplanationNode(
                ExplanationKind.HVORFOR,
                "AVHENGER AV: ${dependency.name}"
            )
            dependencyNode.children.addAll(dependencyExplanation.children)
            root.children.add(dependencyNode)
        }
        
        // HVORDAN - show the calculation if not a constant
        if (!isConstant) {
            val hvordan = ExplanationNode(ExplanationKind.HVORDAN, "HVORDAN")
            
            // Show the formula notation
            hvordan.children.add(
                ExplanationNode(ExplanationKind.FORMEL, "$name = ${expression.notation()}")
            )
            
            // Show the concrete values
            hvordan.children.add(
                ExplanationNode(ExplanationKind.FORMEL, "$name = ${expression.concrete()}")
            )
            
            // Recursively explain contributing Faktum from the calculation
            val contributingFaktum = expression.faktumSet()
            contributingFaktum.forEach { contributing ->
                if (contributing != this && contributing.sourceNode != null && contributing !in visited) {
                    val subExplanation = contributing.buildExplanationInternal(filter, visited)
                    // Flatten: add HVORDAN children directly
                    subExplanation.children
                        .filter { it.kind == ExplanationKind.HVORDAN }
                        .forEach { hvordanChild ->
                            hvordan.children.addAll(hvordanChild.children)
                        }
                }
            }
            
            root.children.add(hvordan)
        }
    }
    
    return root
}

/**
 * Filter for trace traversal.
 */
enum class TraceFilter {
    /** Include all rules (fired and not fired) */
    ALL,
    /** Include only rules that fired (functional explanation) */
    FUNCTIONAL
}


/**
 * Generate an explanation for this Faktum using a transformer.
 *
 * @param filter Whether to include all rules or only fired rules
 * @param transformer Function to convert the explanation tree to desired format
 * @return Transformed explanation
 */
fun <R> Faktum<*>.forklarUsing(
    filter: TraceFilter = TraceFilter.FUNCTIONAL,
    transformer: ExplanationTransformer<R>
): R {
    val explanationTree = buildExplanation(filter)
    return transformer.transform(explanationTree)
}

/**
 * Generate a default text explanation for this Faktum.
 *
 * @param filter Whether to include all rules or only fired rules
 * @return Text explanation
 */
fun Faktum<*>.forklar(filter: TraceFilter = TraceFilter.FUNCTIONAL): String {
    return forklarUsing(filter, DefaultTextTransformer)
}
