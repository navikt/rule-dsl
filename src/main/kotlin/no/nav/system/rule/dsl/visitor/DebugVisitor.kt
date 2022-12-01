package no.nav.system.rule.dsl.visitor

import no.nav.system.rule.dsl.AbstractRuleComponent
import no.nav.system.rule.dsl.rettsregel.PairSubsumtion
import no.nav.system.rule.dsl.rettsregel.helper.isLeafPairSubsumtion

/**
 * Lists the decendants of [AbstractRuleComponent]
 */
class DebugVisitor(
    private val includeLeafFaktum: Boolean = false,
) : TreeVisitor {
    private val debugString = StringBuilder()
    private var level = 0

    override fun visit(arc: AbstractRuleComponent) {
        debugString.append(" ".repeat(level * 2))
        debugString.append(arc.toString()).append("\n")

        if (!includeLeafFaktum && arc.isLeafPairSubsumtion()) return

        level++
        arc.children.forEach { it.accept(this) }
        level--
    }

    fun result(): String = debugString.toString().trim()
}

fun AbstractRuleComponent.debug(includeLeafFaktum: Boolean = false): String {
    return DebugVisitor(includeLeafFaktum).run {
        this@debug.accept(this)
        this.result()
    }
}
