package no.nav.system.ruledsl.core.trace

import no.nav.system.ruledsl.core.expression.Expression
import no.nav.system.ruledsl.core.expression.Faktum
import no.nav.system.ruledsl.core.helper.checkmark
import no.nav.system.ruledsl.core.resource.ResourceAccessor
import no.nav.system.ruledsl.core.resource.ResourceMap
import kotlin.reflect.KClass

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
 * Tree-based execution trace - captures rule evaluations with composition hierarchy.
 * Preserves function nesting structure for complete AST.
 */
class TraceNode(
    val name: String,
    val fired: Boolean,
    val predicates: List<Expression<Boolean>> = emptyList(),
    val children: MutableList<TraceNode> = mutableListOf(),
    val formulas: MutableList<Faktum<*>> = mutableListOf(),
    var parent: TraceNode? = null
) {
    /**
     * Walk up the tree to collect the path from root to this node.
     * Returns list from root (first) to this node (last).
     */
    fun pathFromRoot(): List<TraceNode> {
        val path = mutableListOf<TraceNode>()
        var current: TraceNode? = this
        while (current != null) {
            path.add(0, current)
            current = current.parent
        }
        return path
    }
    
    /**
     * Find the rule that produced a given Faktum by searching this node's formulas.
     */
    fun findProducingRule(faktum: Faktum<*>): TraceNode? {
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
class Trace(name: String) : ResourceAccessor {
    val root = TraceNode(name, fired = true)
    private val stack = mutableListOf(root)
    private val resources = ResourceMap()

    /**
     * Current context in the execution tree.
     */
    private val currentContext: TraceNode
        get() = stack.last()

    // ResourceAccessor delegation
    override fun <T : Any> getResource(key: KClass<T>): T = resources.getResource(key)
    override fun <T : Any> putResource(key: KClass<T>, resource: T) = resources.putResource(key, resource)

    /**
     * Record a rule execution and attach it to the current context.
     * Returns the created TraceNode for stack tracking.
     */
    fun recordRule(name: String, fired: Boolean, predicates: List<Expression<Boolean>>): TraceNode {
        val execution = TraceNode(name, fired, predicates, mutableListOf(), mutableListOf(), parent = currentContext)
        currentContext.children.add(execution)
        return execution
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
    fun pushContext(execution: TraceNode) {
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
        fun walk(nodes: List<TraceNode>, indent: String = "  ") {
            nodes.forEach { node ->
                val ruleStatus = node.fired.checkmark()
                appendLine("${indent}regel: $ruleStatus ${node.name}")
                node.predicates.forEach { predicate ->
                    try {
                        val predicateStatus = predicate.value.checkmark()
                        appendLine("$indent  $predicateStatus $predicate")
                    } catch (e: Exception) {
                        // Predicate was not evaluated (guard short-circuited before it)
                        appendLine("$indent  - (not evaluated)")
                    }
                }
                node.formulas.forEach { faktum ->
                    appendLine("$indent  → ${faktum.name} = ${faktum.value}")
                    if (!faktum.isConstant) {
                        appendLine("$indent    ${faktum.expression.notation()}")
                    }
                }
                if (node.children.isNotEmpty()) {
                    walk(node.children, "$indent  ")
                }
            }
        }
        walk(root.children)
    }
}
