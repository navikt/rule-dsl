package no.nav.system.rule.dsl.inspections

import no.nav.system.rule.dsl.AbstractRuleComponent
import no.nav.system.rule.dsl.enums.RuleComponentType
import no.nav.system.rule.dsl.rettsregel.helper.isLeafFaktum

/**
 * Searches the ruleComponent tree for target [AbstractRuleComponent]s matching [target].
 * Shows the path from the receiver to where [target]s where found.
 *
 * @param qualifier A ruleComponent that resolves [qualifier] expression as true is eligible for search.
 * @param target A ruleComponent that matches [target] expression is added to [result]
 */
fun AbstractRuleComponent.trace(targetType: RuleComponentType): String {
    return trace(target = { arc -> arc.type() == targetType })
}


fun AbstractRuleComponent.trace(
    includeLeafFaktum: Boolean = false,
    qualifier: (AbstractRuleComponent) -> Boolean = { true },
    target: (AbstractRuleComponent) -> Boolean
): String {
    val rootTraceNode = TraceNode(parent = null, arc = this).apply {
        inspect(this, includeLeafFaktum, qualifier, target)
    }

    return rootTraceNode.toString()
}

private fun inspect(
    parent: TraceNode,
    includeLeafFaktum: Boolean = false,
    qualifier: (AbstractRuleComponent) -> Boolean,
    target: (AbstractRuleComponent) -> Boolean
) {
    if (parent.arc.isLeafFaktum()) return
    parent.arc.children
        .filter(qualifier)
        .filterNot { !includeLeafFaktum && it.isLeafFaktum() }.forEach { child ->
        TraceNode(
            parent = parent,
            arc = child
        ).apply {
            parent.children.add(this)
            if (target.invoke(child)) {
                this.verify()
            }
            inspect(this, includeLeafFaktum, qualifier, target)
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