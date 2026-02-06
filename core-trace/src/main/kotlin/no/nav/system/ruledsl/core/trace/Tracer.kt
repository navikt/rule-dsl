package no.nav.system.ruledsl.core.trace

import no.nav.system.ruledsl.core.expression.Expression
import no.nav.system.ruledsl.core.expression.Faktum
import no.nav.system.ruledsl.core.helper.checkmark
import no.nav.system.ruledsl.core.reference.Reference

/**
 * Tree node for execution trace - captures evaluated expressions.
 *
 * Both decisions (Expression<Boolean>) and calculations (Expression<Number>)
 * are unified as Expression<*> in the trace graph.
 */
class RuleTrace(
    val name: String,
    var fired: Boolean = false,
    val expressions: MutableList<Expression<*>> = mutableListOf(),
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
     * Find the rule that produced a given Faktum by searching expressions.
     */
    fun findProducingRule(faktum: Faktum<*>): RuleTrace? {
        if (expressions.contains(faktum)) return this
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
 * Implementations capture execution details for explanation.
 * Stored as a resource in RuleContext, making tracing optional and configurable.
 */
interface Tracer {
    /**
     * Creates a RuleTrace with given name and attaches it to the current context.
     */
    fun createRuleTrace(name: String, references: List<Reference> = emptyList()): RuleTrace

    /**
     * Record an expression to the current context.
     * Works for both decisions (Boolean) and calculations (Number, etc).
     */
    fun recordExpression(expression: Expression<*>)

    /**
     * Push an execution context onto the stack.
     */
    fun pushContext(trace: RuleTrace)

    /**
     * Pop the current execution context from the stack.
     */
    fun popContext()

    /**
     * Get the root trace node.
     */
    fun root(): RuleTrace

    /**
     * Debug output showing the trace tree.
     */
    fun debugTree(): String
}

/**
 * Default implementation of Tracer.
 */
class DefaultTracer(name: String) : Tracer {
    private val root = RuleTrace(name, fired = true)
    private val stack = mutableListOf(root)
    private val recorded = mutableSetOf<Expression<*>>()

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

    override fun recordExpression(expression: Expression<*>) {
        if (expression !in recorded) {
            recorded.add(expression)
            currentContext.expressions.add(expression)

            // Set sourceNode for backward traversal in explanations
            if (expression is Faktum<*>) {
                expression.sourceNode = currentContext
            }
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

                node.expressions.forEach { expr ->
                    appendLine("$indent  ${formatExpression(expr)}")
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

    /**
     * Format an expression for debug output.
     * Handles both Boolean (decisions) and other types (calculations) uniformly.
     */
    private fun formatExpression(expr: Expression<*>): String {
        return when (expr) {
            is Faktum<*> -> formatFaktum(expr, "", mutableSetOf())
            else -> {
                // For Boolean expressions (comparisons, etc.)
                val status = (expr.value as? Boolean)?.checkmark() ?: ""
                "$status $expr"
            }
        }
    }

    /**
     * Format a Faktum with its formula hierarchy.
     */
    private fun formatFaktum(
        faktum: Faktum<*>,
        indent: String,
        visited: MutableSet<Faktum<*>>
    ): String = buildString {
        // Prevent infinite recursion on circular references
        if (faktum in visited) {
            append("$indent${faktum.name} = ${faktum.value} (see above)")
            return@buildString
        }
        visited.add(faktum)

        appendLine("${faktum.name} = ${faktum.value}")
        appendLine("$indent  notation: ${faktum.expression.notation()}")
        appendLine("$indent  concrete: ${faktum.expression.concrete()}")

        // Show sub-Faktum (non-trivial ones)
        val subFaktum = faktum.faktumSet().filter { it.faktumSet().isNotEmpty() }
        if (subFaktum.isNotEmpty()) {
            appendLine("$indent  subformulas:")
            subFaktum.forEach { sub ->
                append("$indent    ${formatFaktum(sub, "$indent    ", visited)}")
            }
        }
    }.trimEnd()
}

/**
 * No-op tracer for when tracing is not needed.
 */
class NoOpTracer : Tracer {
    private val emptyRoot = RuleTrace("no-trace", fired = true)

    override fun createRuleTrace(name: String, references: List<Reference>) = RuleTrace(name)
    override fun recordExpression(expression: Expression<*>) {}
    override fun pushContext(trace: RuleTrace) {}
    override fun popContext() {}
    override fun root(): RuleTrace = emptyRoot
    override fun debugTree(): String = "Tracing disabled"
}
