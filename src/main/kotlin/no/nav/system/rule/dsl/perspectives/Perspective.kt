package no.nav.system.rule.dsl.perspectives

import no.nav.system.rule.dsl.AbstractRuleComponent
import no.nav.system.rule.dsl.enums.RuleComponentType

/**
 * Defines different perspectives for viewing the ARC tree execution trace.
 *
 * Perspectives control which nodes are included when traversing the tree,
 * allowing different levels of detail for different use cases.
 */
enum class Perspective {
    /**
     * FULL PERSPECTIVE: Complete audit trail showing every component.
     * - Service-centric use case: Complete compliance audit
     * - Shows: All nodes (services, flows, decisions, branches, rulesets, rules, predicates, faktum)
     * - Use when: Need to see every step of execution
     */
    FULL,

    /**
     * FUNCTIONAL PERSPECTIVE: Only decision-making components.
     * - Service-centric use case: Understanding business logic flow
     * - Shows: Rules, branches, predicates, and faktum (the actual decisions and data)
     * - Hides: Container nodes (services, flows, decisions, rulesets)
     * - Use when: Focus on "what decisions were made" rather than structure
     */
    FUNCTIONAL;

    /**
     * Determines if a given ARC node should be included in this perspective.
     */
    fun includes(arc: AbstractRuleComponent): Boolean {
        return when (this) {
            FULL -> true  // Include everything
            FUNCTIONAL -> when (arc.type()) {
                // Decision-making components
                RuleComponentType.REGEL -> true
                RuleComponentType.GREN -> true
                RuleComponentType.PREDIKAT -> true
                RuleComponentType.FAKTUM -> true

                // Container components (excluded in functional view)
                RuleComponentType.REGELTJENESTE -> false
                RuleComponentType.REGELFLYT -> false
                RuleComponentType.FORGRENING -> false
                RuleComponentType.REGELSETT -> false
            }
        }
    }
}
