package no.nav.system.ruledsl.core.trace

import no.nav.system.ruledsl.core.expression.Expression
import no.nav.system.ruledsl.core.expression.Faktum
import no.nav.system.ruledsl.core.helper.checkmark

/**
 * Tree-based execution trace - captures rule evaluations with composition hierarchy.
 * Preserves function nesting structure for complete AST.
 */
class TraceNode(
    val name: String,
    val fired: Boolean,
    val predicates: List<Expression<Boolean>> = emptyList(),
    val children: MutableList<TraceNode> = mutableListOf(),
    val formulas: MutableList<Faktum<*>> = mutableListOf()
)

class Trace(name: String) {
    val root = TraceNode(name, fired = true)
    private val stack = mutableListOf(root)

    /**
     * Current context in the execution tree.
     */
    private val currentContext: TraceNode
        get() = stack.last()

    /**
     * Record a rule execution and attach it to the current context.
     * Returns the created TraceNode for stack tracking.
     */
    fun recordRule(name: String, fired: Boolean, predicates: List<Expression<Boolean>>): TraceNode {
        val execution = TraceNode(name, fired, predicates, mutableListOf())
        currentContext.children.add(execution)
        return execution
    }

    /**
     * Record a Faktum (formula) to the current context.
     * Called by SPOR and RETURNER to trace calculations.
     */
    fun recordFaktum(faktum: Faktum<*>) {
        currentContext.formulas.add(faktum)
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
                    val predicateStatus = predicate.value.checkmark()
                    appendLine("$indent  $predicateStatus $predicate")
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
