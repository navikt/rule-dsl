package no.nav.system.rule.dsl.visitor

import no.nav.system.rule.dsl.AbstractRuleComponent
import no.nav.system.rule.dsl.enums.RuleComponentType
import no.nav.system.rule.dsl.rettsregel.PairSubsumtion

/**
 * Searches the ruleComponent tree for target [AbstractRuleComponent]s matching [target].
 * Shows the path from where the visitor was accepted to where [target]s where found.
 *
 * @param qualifier A ruleComponent that resolves [qualifier] expression as true is eligible for search.
 * @param target A ruleComponent that matches [target] expression is added to [result]
 */
class ArcTraceVisitor(
    private val qualifier: (AbstractRuleComponent) -> Boolean = { true },
    private val target: (AbstractRuleComponent) -> Boolean = { true },
) : ArcVisitor(target) {
    private lateinit var rootTraceNode: TraceNode

    /**
     * Shows the path from where the visitor was accepted to where target(s) where found.
     */
    fun trace(): String = rootTraceNode.toString()

    override fun visit(arc: AbstractRuleComponent) {
        rootTraceNode = TraceNode(parent = null, arc = arc).apply {
            traceVisit(this)
        }
    }

    private fun traceVisit(parent: TraceNode) {
        if (parent.arc is PairSubsumtion) return
        parent.arc.children.filter(qualifier).forEach { child ->
            TraceNode(
                parent = parent,
                arc = child
            ).apply {
                parent.children.add(this)
                if (target.invoke(arc)) {
                    result.add(arc)
                    this.verify()
                }
                traceVisit(this)
            }
        }
    }

    private class TraceNode(
        var parent: TraceNode? = null,
        val children: MutableList<TraceNode> = mutableListOf(),
        val arc: AbstractRuleComponent,
        var partOfResult: Boolean = false,
    ) {
        fun verify() {
            partOfResult = true
            parent?.verify()
        }

        override fun toString(): String = toTreeString(0).trim()

        private fun toTreeString(level: Int): String {
            return StringBuilder().apply {
                append(" ".repeat(level * 2)).append(arc.toString()).append("\n")
                children.filter { it.partOfResult }.forEach {
                    append(it.toTreeString(level + 1))
                }
            }.toString()
        }
    }
}

fun AbstractRuleComponent.trace(target: (AbstractRuleComponent) -> Boolean = { true }): String {
    return ArcTraceVisitor(target = target).run {
        this@trace.accept(this)
        this.trace()
    }
}

fun AbstractRuleComponent.trace(targetType: RuleComponentType): String {
    return ArcTraceVisitor { arc -> arc.type() == targetType }.run {
        this@trace.accept(this)
        this.trace()
    }
}