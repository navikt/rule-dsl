package no.nav.system.rule.dsl.inspections

import no.nav.system.rule.dsl.AbstractRuleComponent

/**
 * Searches the ruleComponent tree for target [AbstractRuleComponent]s matching [target].
 */
fun AbstractRuleComponent.find(
    qualifier: (AbstractRuleComponent) -> Boolean = { true },
    target: (AbstractRuleComponent) -> Boolean = { true }
): MutableList<AbstractRuleComponent> {
    val result: MutableList<AbstractRuleComponent> = mutableListOf()
    inspect(this, result, qualifier, target)
    return result
}


private fun inspect(
    arc: AbstractRuleComponent,
    result: MutableList<AbstractRuleComponent>,
    qualifier: (AbstractRuleComponent) -> Boolean,
    target: (AbstractRuleComponent) -> Boolean
) {
    if (target.invoke(arc)) result.add(arc)
    arc.children
        .filter(qualifier)
        .forEach { inspect(it, result, qualifier, target) }
}