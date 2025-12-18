package no.nav.system.ruledsl.core.trace

import no.nav.system.ruledsl.core.expression.Faktum
import no.nav.system.ruledsl.core.helper.checkmark

/**
 * Node in an explanation tree.
 * Used as intermediate representation that can be transformed to any output format.
 */
data class ExplanationNode(
    val kind: ExplanationKind,
    val text: String,
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
            node.children.forEach { walk(it, "$indent  ") }
        }
        walk(root)
    }
}

/**
 * Build an explanation tree for a Faktum, starting from the result and walking backwards.
 *
 * @param filter Whether to include all rules or only fired rules (FUNCTIONAL)
 * @return ExplanationNode tree that can be transformed to any format
 */
fun Faktum<*>.buildExplanation(filter: TraceFilter = TraceFilter.FUNCTIONAL): ExplanationNode {
    val root = ExplanationNode(ExplanationKind.HVA, "HVA")
    
    // Add the result value
    root.children.add(ExplanationNode(ExplanationKind.FORMEL, "$name = $value"))
    
    // Find the source node (the rule that produced this Faktum)
    val source = sourceNode as? TraceNode
    if (source != null) {
        // HVORFOR - walk up the tree to collect fired rules
        val hvorfor = ExplanationNode(ExplanationKind.HVORFOR, "HVORFOR")
        val path = source.pathFromRoot()
        
        // Collect rules in the path that fired (skip root which is the Trace itself)
        path.drop(1).filter { it.fired || filter == TraceFilter.ALL }.forEach { node ->
            val ruleNode = ExplanationNode(
                ExplanationKind.REGEL,
                "${node.fired.checkmark()} ${node.name}"
            )
            
            // Add predicates for this rule
            node.predicates.forEach { predicate ->
                val status = predicate.value.checkmark()
                if (predicate.value || filter == TraceFilter.ALL) {
                    ruleNode.children.add(
                        ExplanationNode(ExplanationKind.PREDIKAT, "$status $predicate")
                    )
                }
            }
            
            if (ruleNode.children.isNotEmpty() || filter == TraceFilter.ALL) {
                hvorfor.children.add(ruleNode)
            } else {
                // Still add the rule name even if no predicates to show
                hvorfor.children.add(ruleNode)
            }
        }
        
        if (hvorfor.children.isNotEmpty()) {
            root.children.add(hvorfor)
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
            
            // Recursively explain contributing Faktum
            val contributingFaktum = expression.faktumSet()
            contributingFaktum.forEach { contributing ->
                if (contributing != this && contributing.sourceNode != null) {
                    val subExplanation = contributing.buildExplanation(filter)
                    // Flatten: add HVORDAN children directly
                    subExplanation.children.filterIsInstance<ExplanationNode>()
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
