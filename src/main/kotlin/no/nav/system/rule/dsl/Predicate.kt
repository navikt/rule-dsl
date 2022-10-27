package no.nav.system.rule.dsl

import no.nav.system.rule.dsl.enums.RuleComponentType
import no.nav.system.rule.dsl.enums.RuleComponentType.PREDIKAT
import no.nav.system.rule.dsl.rettsregel.helper.svarord

/**
 * A single boolean statement used in evaluation of a rule.
 */
open class Predicate(
    private val function: () -> Boolean,
) : AbstractRuleComponent() {

    /**
     * Is true if the predicate should terminate further evaluation of predicates.
     *
     * Typicaly used by null-check predicates.
     */
    internal var terminateEvaluation: Boolean = false

    /**
     * Evaluates the predicate function.
     *
     * @return returns true if further evaluation of remaining predicates in the rule should be prevented.
     */
    internal open val fired: Boolean by lazy {
        function.invoke().also { terminateEvaluation = !it }
    }

    override fun name(): String = ""
    override fun type(): RuleComponentType = PREDIKAT
    override fun fired(): Boolean = fired
    override fun toString(): String = "${type()}: ${fired.svarord()}"
}