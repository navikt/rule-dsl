package no.nav.system.rule.dsl.pattern

import no.nav.system.rule.dsl.Rule

/**
 * Patterns enable rules to evaluate for each element in the pattern.
 */
interface Pattern<P> {

    /**
     * Map of [Rule] and their corresponding patternElement ([P]) that was used in the evaluation.
     */
    val ruleResultMap: HashMap<Rule<*>, P>

    /**
     * Internal use only.
     * Retrieve the list of patternElements[P]
     */
    fun get(): List<P>

    /**
     * Internal use only.
     * Registers a [rule] and its corresponding [patternElement] in the [ruleResultMap]
     *
     * @param rule the rule to register
     * @param patternElement the element the [rule] is evaluating
     */
    fun registerRule(rule: Rule<*>, patternElement: P) {
        rule.pattern = this
        ruleResultMap[rule] = patternElement
    }
}