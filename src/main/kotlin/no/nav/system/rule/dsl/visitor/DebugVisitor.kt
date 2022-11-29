package no.nav.system.rule.dsl.visitor

import no.nav.system.rule.dsl.AbstractRuleComponent
import no.nav.system.rule.dsl.rettsregel.PairSubsumtion

/**
 * Lists the decendants of [AbstractRuleComponent]
 */
class DebugVisitor(
    private val includeFaktum: Boolean = false,
) : TreeVisitor {
    private val debugString = StringBuilder()
    private var level = 0

    override fun visit(arc: AbstractRuleComponent) {
        debugString.append(" ".repeat(level * 2))
        debugString.append(arc.toString()).append("\n")

        if (!includeFaktum && arc is PairSubsumtion) return

        level++
        arc.children.forEach { it.accept(this) }
        level--
    }

    fun result(): String = debugString.toString().trim()
}

fun AbstractRuleComponent.debug(): String {
    return DebugVisitor().run {
        this@debug.accept(this)
        this.result()
    }
}
