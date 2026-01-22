package no.nav.system.ruledsl.core.trace

import no.nav.system.ruledsl.core.expression.Expression
import no.nav.system.ruledsl.core.expression.Faktum
import no.nav.system.ruledsl.core.helper.checkmark
import no.nav.system.ruledsl.core.reference.Reference
import no.nav.system.ruledsl.core.resource.ResourceAccessor
import no.nav.system.ruledsl.core.resource.ResourceMap
import kotlin.reflect.KClass

/**
 * Tree-based execution trace - captures rule evaluations with composition hierarchy.
 * Preserves function nesting structure for complete AST.
 */
class RuleTrace(
    val name: String,
    val fired: Boolean,
    val predicates: List<Expression<Boolean>> = emptyList(),
    val formulas: MutableList<Faktum<*>> = mutableListOf(),
    val references: List<Reference> = emptyList(),
    var parent: RuleTrace? = null,
    val children: MutableList<RuleTrace> = mutableListOf()
) {
    /**
     * Walk up the tree to collect the path from root to this node.
     * Returns list from root (first) to this node (last).
     */
    fun pathFromRoot(): List<RuleTrace> {
        val path = mutableListOf<RuleTrace>()
        var current: RuleTrace? = this
        while (current != null) {
            path.add(0, current)
            current = current.parent
        }
        return path
    }

    /**
     * Find the rule that produced a given Faktum by searching this node's formulas.
     */
    fun findProducingRule(faktum: Faktum<*>): RuleTrace? {
        if (formulas.contains(faktum)) return this
        for (child in children) {
            val found = child.findProducingRule(faktum)
            if (found != null) return found
        }
        return null
    }
}

/**
 * Execution context for rule evaluation.
 *
 * Combines tracing (WHY/HOW) with resource management (plugin capabilities).
 * Resources registered here are accessible via ResourceAccessor interface
 * on Rule and Regelsett.
 */
class RuleContext(name: String) : ResourceAccessor {
    val root = RuleTrace(name, fired = true)
    private val stack = mutableListOf(root)
    private val resources = ResourceMap()

    /**
     * Current context in the execution tree.
     */
    private val currentContext: RuleTrace
        get() = stack.last()

    // ResourceAccessor delegation
    override fun <T : Any> getResource(key: KClass<T>): T = resources.getResource(key)
    override fun <T : Any> putResource(key: KClass<T>, resource: T) = resources.putResource(key, resource)

    /**
     * Creates the RuleTrace and attach it to the current context.
     * Returns the created RuleTrace for stack tracking.
     */
    fun recordRule(name: String, fired: Boolean, predicates: List<Expression<Boolean>>, references: List<Reference> = emptyList()): RuleTrace {
        val trace = RuleTrace(
            name = name,
            fired = fired,
            predicates = predicates,
            formulas = mutableListOf(),
            references = references,
            parent = currentContext,
            children = mutableListOf()
        )
        currentContext.children.add(trace)
        return trace
    }

    /**
     * Record a Faktum (formula) to the current context.
     * Called by SPOR and RETURNER to trace calculations.
     * Skips if already traced (prevents duplicates in nested calls).
     * Sets the sourceNode on the Faktum for inverse explanation traversal.
     */
    fun recordFaktum(faktum: Faktum<*>) {
        if (!faktum.traced) {
            faktum.traced = true
            faktum.sourceNode = currentContext
            currentContext.formulas.add(faktum)
        }
    }

    /**
     * Push an execution context onto the stack.
     * Called before executing a rule's action block.
     */
    fun pushContext(execution: RuleTrace) {
        stack.add(execution)
    }

    /**
     * Pop the current execution context from the stack.
     * Called after executing a rule's action block.
     */
    fun popContext() {
        require(stack.size > 1) { "Cannot pop root context from stack" }
        stack.removeLast()
    }

    /**
     * Detailed explanation: show all rules with their conditions and formulas (recursive tree).
     */
    fun debugTree(): String = buildString {
        appendLine("TRACE: ${root.name}")
        fun walk(nodes: List<RuleTrace>, indent: String = "  ") {
            nodes.forEach { node ->
                val ruleStatus = node.fired.checkmark()
                appendLine("${indent}regel: $ruleStatus ${node.name}")

                node.predicates.forEach { predicate ->
                    val predicateStatus = predicate.value.checkmark()
                    appendLine("$indent  $predicateStatus $predicate")
                }
                node.formulas.forEach { faktum ->
                    appendLine("$indent  → ${faktum.name} = ${faktum.value}")
                    if (!faktum.isConstant) {
                        appendLine("$indent    ${faktum.expression.notation()}")
                    }
                }
                node.references.forEach { ref ->
                    appendLine("$indent  📖 ${ref.id}: ${ref.url}")
                }

                if (node.children.isNotEmpty()) {
                    walk(node.children, "$indent  ")
                }
            }
        }
        walk(root.children)
    }
}
