package no.nav.system.rule.dsl

import no.nav.system.rule.dsl.enums.RuleComponentType
import no.nav.system.rule.dsl.enums.RuleComponentType.PREDIKAT

/**
 * A single boolean statement used in evaluation of a rule.
 */
open class Predicate(
    private val function: () -> Boolean,
) : AbstractRuleComponent() {

    protected var fired: Boolean = false

    /**
     * Evaluates the predicate function.
     *
     * @return returns true if further evaluation of remaining predicates in the rule should be prevented.
     */
    internal open fun evaluate(): Boolean {
        fired = function.invoke()
        return !fired
    }

    override fun name(): String = ""
    override fun type(): RuleComponentType = PREDIKAT
    override fun fired(): Boolean = fired
    override fun toString(): String = "Predicate(fired=$fired)"
}