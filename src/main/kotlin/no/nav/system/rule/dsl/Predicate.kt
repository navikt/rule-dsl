package no.nav.system.rule.dsl

import no.nav.system.rule.dsl.enums.RuleComponentType
import no.nav.system.rule.dsl.enums.RuleComponentType.PREDIKAT
import no.nav.system.rule.dsl.rettsregel.Uttrykk
import no.nav.system.rule.dsl.rettsregel.helper.svarord

/**
 * A single boolean statement used in evaluation of a rule.
 */
open class Predicate(
    /**
     * Is true if the predicate should terminate further evaluation of predicates.
     *
     * Typicaly used by null-check predicates.
     */
    internal val terminateEvaluation: Boolean = true,
    open val function: () -> Boolean
) : AbstractRuleComponent() {

    /**
     * Evaluates the predicate function.
     *
     * @return returns true if further evaluation of remaining predicates in the rule should be prevented.
     */
    internal open val fired: Boolean by lazy {
        function.invoke()
    }

    override fun name(): String = ""
    override fun type(): RuleComponentType = PREDIKAT
    override fun fired(): Boolean = fired
    override fun toString(): String = "${type()}: ${fired.svarord()}"
}

/**
 * Adapter between Uttrykk and Predicate to allow the tracking of the predicate to rely on Uttrykk.
 */
class TrackablePredicate(
    val uttrykk: Uttrykk<Boolean>
) : Predicate(
    terminateEvaluation = false,
    function = { uttrykk.verdi }
) {
    fun notasjon(): String = uttrykk.notasjon()
    fun konkret(): String = uttrykk.konkret()

    /**
     * Override toString() to show the predicate's expression.
     * Shows the uttrykk's representation rather than just "PREDIKAT: "
     */
    override fun toString(): String = uttrykk.toString()
}