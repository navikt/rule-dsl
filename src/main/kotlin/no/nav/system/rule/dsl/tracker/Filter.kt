package no.nav.system.rule.dsl.tracker

import no.nav.system.rule.dsl.AbstractRuleComponent
import no.nav.system.rule.dsl.enums.RuleComponentType

/**
 * Determines which ARC components to include during explanation building.
 */
fun interface Filter {
    fun includes(component: AbstractRuleComponent): Boolean
}

object Filters {
    /**
     * Include only functional/decision-making components.
     * Excludes container nodes (services, flows, decisions, rulesets).
     */
    val FUNCTIONAL = Filter { component ->
        when (component.type()) {
            RuleComponentType.REGEL,
            RuleComponentType.GREN,
            RuleComponentType.PREDIKAT,
            RuleComponentType.FAKTUM -> true
            else -> false
        }
    }
}
