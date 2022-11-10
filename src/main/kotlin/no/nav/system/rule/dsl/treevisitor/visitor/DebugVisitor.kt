package no.nav.system.rule.dsl.treevisitor.visitor

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


/**
 * Lists the ancestors of [AbstractRuleComponent]
 */
class DebugUpVisitor : TreeVisitor {
    private val upOrder = mutableListOf<String>()

    override fun visit(arc: AbstractRuleComponent) {
        upOrder.add(0, arc.toString())
        arc.parent?.accept(this)
    }

    fun result(): String = upOrder
        .mapIndexed { index, s -> " ".repeat(index * 2) + s }
        .joinToString("\n")
}


fun AbstractRuleComponent.debug(): String {
    val debugVisitor = DebugVisitor()
    this.accept(debugVisitor)
    return debugVisitor.result()
}

fun AbstractRuleComponent.debugUp(): String {
    val debugVisitor = DebugUpVisitor()
    this.accept(debugVisitor)
    return debugVisitor.result()
}