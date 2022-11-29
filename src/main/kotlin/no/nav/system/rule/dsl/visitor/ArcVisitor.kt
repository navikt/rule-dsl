package no.nav.system.rule.dsl.visitor

import no.nav.system.rule.dsl.AbstractRuleComponent

/**
 * Searches the ruleComponent tree for target [AbstractRuleComponent]s matching [target].
 */
open class ArcVisitor(
    private val qualifier: (AbstractRuleComponent) -> Boolean = { true },
    private val target: (AbstractRuleComponent) -> Boolean = { true },
) : TreeVisitor {
    val result = mutableListOf<AbstractRuleComponent>()

    override fun visit(arc: AbstractRuleComponent) {
        if (target.invoke(arc)) result.add(arc)
        arc.children
            .filter(qualifier)
            .forEach { it.accept(this) }
    }
}


fun AbstractRuleComponent.find(target: (AbstractRuleComponent) -> Boolean): List<AbstractRuleComponent> {
    return ArcVisitor(target = target).run {
        this@find.accept(this)
        this.result
    }
}