package no.nav.system.ruledsl.core.forklaring

import no.nav.system.ruledsl.core.enums.RuleComponentType
import no.nav.system.ruledsl.core.model.arc.AbstractRuleComponent

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
