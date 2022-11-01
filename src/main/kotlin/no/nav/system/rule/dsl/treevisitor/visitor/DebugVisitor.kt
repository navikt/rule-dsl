package no.nav.system.rule.dsl.treevisitor.visitor

import no.nav.system.rule.dsl.AbstractRuleComponent
import no.nav.system.rule.dsl.rettsregel.PairSubsumtion

/**
 * Lists the complete tree of [AbstractRuleComponent]
 */
class DebugVisitor(
    private val includeFaktum: Boolean = false,
) : TreeVisitor {
    val debugString = StringBuilder()
    private var level = 0

    override fun visit(ruleComponent: AbstractRuleComponent) {
        debugString.append(" ".repeat(level * 2))
        debugString.append(ruleComponent.toString()).append("\n")

        if (!includeFaktum && ruleComponent is PairSubsumtion) return

        level++
        ruleComponent.children.forEach { it.accept(this) }
        level--
    }
}

fun AbstractRuleComponent.debug(): String {
    val fdv = DebugVisitor()
    this.accept(fdv)
    return fdv.debugString.toString().trim()
}