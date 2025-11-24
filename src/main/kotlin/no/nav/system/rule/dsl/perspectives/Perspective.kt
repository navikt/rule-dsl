package no.nav.system.rule.dsl.perspectives

import no.nav.system.rule.dsl.AbstractRuleComponent
import no.nav.system.rule.dsl.enums.RuleComponentType

/**
 * API for filtering which nodes are included when traversing the ARC tree.
 *
 * Functional interface allows users to create custom perspectives by implementing
 * the includes() predicate function.
 *
 * Example - User-defined custom perspective:
 * ```
 * val OnlyFiredRules = Perspective { arc ->
 *     arc.type() == RuleComponentType.REGEL && arc.fired()
 * }
 * ```
 */
fun interface Perspective {
    /**
     * Determines if a given ARC node should be included in this perspective.
     *
     * @param arc The AbstractRuleComponent to evaluate
     * @return true if the node should be included in traversal output
     */
    fun includes(arc: AbstractRuleComponent): Boolean

    companion object {
        /**
         * FULL PERSPECTIVE: Complete audit trail showing every component.
         * - Service-centric use case: Complete compliance audit
         * - Shows: All nodes (services, flows, decisions, branches, rulesets, rules, predicates, faktum)
         * - Use when: Need to see every step of execution
         */
        val FULL = Perspective { true }

        /**
         * FUNCTIONAL PERSPECTIVE: Only decision-making components.
         * - Service-centric use case: Understanding business logic flow
         * - Shows: Rules, branches, predicates, and faktum (the actual decisions and data)
         * - Hides: Container nodes (services, flows, decisions, rulesets)
         * - Use when: Focus on "what decisions were made" rather than structure
         */
        val FUNCTIONAL = Perspective { arc ->
            when (arc.type()) {
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
