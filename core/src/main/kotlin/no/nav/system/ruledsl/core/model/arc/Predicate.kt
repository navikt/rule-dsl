package no.nav.system.ruledsl.core.model.arc

import no.nav.system.ruledsl.core.enums.RuleComponentType
import no.nav.system.ruledsl.core.helper.svarord
import no.nav.system.ruledsl.core.model.Uttrykk

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
    override fun type(): RuleComponentType = RuleComponentType.PREDIKAT
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
     * Override toString() to show the predicate's expression with type prefix.
     * Shows "predikat: <uttrykk>"
     */
    override fun toString(): String = "${type()}: $uttrykk"
}

/**
 * Tracks a branch condition (betingelse) in the ARC tree.
 * Similar to TrackablePredicate, but for branch decision points instead of rule predicates.
 */
class TrackableCondition(
    val uttrykk: Uttrykk<Boolean>
) : AbstractRuleComponent() {
    fun notasjon(): String = uttrykk.notasjon()
    fun konkret(): String = uttrykk.konkret()

    override fun name(): String = "betingelse"
    override fun type(): RuleComponentType = RuleComponentType.BETINGELSE
    override fun fired(): Boolean = uttrykk.verdi
    override fun toString(): String = "${type()}: $uttrykk"
}

@RequiresOptIn(message = "Domain predicate entries are currently experimental.", level = RequiresOptIn.Level.WARNING)
annotation class DslDomainPredicate