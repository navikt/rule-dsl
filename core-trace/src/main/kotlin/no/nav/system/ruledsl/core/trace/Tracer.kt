package no.nav.system.ruledsl.core.trace

import no.nav.system.ruledsl.core.expression.Expression
import no.nav.system.ruledsl.core.expression.Faktum
import no.nav.system.ruledsl.core.helper.checkmark
import no.nav.system.ruledsl.core.reference.Reference

/**
 * Tree node for execution trace - captures rule evaluations with composition hierarchy.
 * Preserves function nesting structure for complete AST.
 */
class RuleTrace(
    val name: String,
    var fired: Boolean = false,
    val predicates: MutableList<Expression<Boolean>> = mutableListOf(),
    val formulas: MutableList<Faktum<*>> = mutableListOf(),
    val references: MutableList<Reference> = mutableListOf(),
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
 * Interface for tracing rule evaluation.
 * 
 * Implementations can capture execution details (WHY/HOW) for explanation.
 * Stored as a resource in RuleContext, making tracing optional and configurable.
 */
interface Tracer {
    /**
     * Creates a RuleTrace with given name and attaches it to the current context.
     * Returns the created RuleTrace for stack tracking.
     */
    fun createRuleTrace(name: String, references: List<Reference> = emptyList()): RuleTrace

    /**
     * Record a Faktum (formula) to the current context.
     * Called by SPOR and RETURNER to trace calculations.
     */
    fun recordFaktum(faktum: Faktum<*>)

    /**
     * Push an execution context onto the stack.
     * Called before executing a rule's action block.
     */
    fun pushContext(trace: RuleTrace)

    /**
     * Pop the current execution context from the stack.
     * Called after executing a rule's action block.
     */
    fun popContext()

    /**
     * Get the root trace node.
     */
    fun root(): RuleTrace

    /**
     * Detailed explanation: show all rules with their conditions and formulas (recursive tree).
     */
    fun debugTree(): String
}

/**
 * Default implementation of Tracer.
 * Maintains a tree structure of RuleTrace nodes representing the execution flow.
 *
 * @param name Root trace name (typically the service name)
 */
class DefaultTracer(name: String) : Tracer {
    private val root = RuleTrace(name, fired = true)
    private val stack = mutableListOf(root)

    private val currentContext: RuleTrace
        get() = stack.last()

    override fun createRuleTrace(name: String, references: List<Reference>): RuleTrace {
        val trace = RuleTrace(
            name = name,
            references = references.toMutableList(),
            parent = currentContext,
        )
        currentContext.children.add(trace)
        return trace
    }

    override fun recordFaktum(faktum: Faktum<*>) {
        if (!faktum.traced) {
            faktum.traced = true
            faktum.sourceNode = currentContext
            currentContext.formulas.add(faktum)
        }
    }

    override fun pushContext(trace: RuleTrace) {
        stack.add(trace)
    }

    override fun popContext() {
        require(stack.size > 1) { "Cannot pop root context from stack" }
        stack.removeLast()
    }

    override fun root(): RuleTrace = root

    override fun debugTree(): String = buildString {
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

/**
 * No-op tracer for when tracing is not needed.
 * All operations are ignored, minimal overhead.
 */
class NoOpTracer : Tracer {
    private val emptyRoot = RuleTrace("no-trace", fired = true)
    private var currentTrace: RuleTrace = emptyRoot

    override fun createRuleTrace(name: String, references: List<Reference>): RuleTrace {
        // Return a lightweight trace that doesn't build the tree
        currentTrace = RuleTrace(name)
        return currentTrace
    }

    override fun recordFaktum(faktum: Faktum<*>) {
        // No-op
    }

    override fun pushContext(trace: RuleTrace) {
        // No-op
    }

    override fun popContext() {
        // No-op
    }

    override fun root(): RuleTrace = emptyRoot

    override fun debugTree(): String = "Tracing disabled"
}
