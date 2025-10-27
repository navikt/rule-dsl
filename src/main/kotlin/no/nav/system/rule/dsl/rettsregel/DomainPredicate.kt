package no.nav.system.rule.dsl.rettsregel

import no.nav.system.rule.dsl.Predicate
import no.nav.system.rule.dsl.enums.Comparator
import no.nav.system.rule.dsl.enums.ListComparator
import no.nav.system.rule.dsl.enums.PairComparator
import no.nav.system.rule.dsl.enums.RuleComponentType
import no.nav.system.rule.dsl.enums.RuleComponentType.DOMENE_PREDIKAT_LISTE
import no.nav.system.rule.dsl.enums.RuleComponentType.DOMENE_PREDIKAT_PAR
import no.nav.system.rule.dsl.rettsregel.helper.svarord

/**
 * The application of a [function] that returns the boolean.
 */
abstract class DomainPredicate(
    open val comparator: Comparator,
    override val function: () -> Boolean,
) : Predicate(function = function) {

    /**
     * Evaluates the predicate function.
     * DomainPredicate never terminates callers evaluation chain ([terminateEvaluation] )
     *
     * @return boolean result of function.
     */
    override val fired: Boolean by lazy {
        function.invoke().also { terminateEvaluation = false }
    }
}

/**
 * Compares [verdi1] with [verdi2]
 */
class PairDomainPredicate(
    override val comparator: PairComparator,
    private val verdi1: Verdi<*>,
    private val verdi2: Verdi<*>,
    override val function: () -> Boolean,
) : DomainPredicate(comparator = comparator, function = function) {

    init {
//        if (verdi1.name != verdi1.value) this.children.add(verdi1)
//        if (verdi2.name != verdi2.value) this.children.add(verdi2)
    }

    override fun type(): RuleComponentType = DOMENE_PREDIKAT_PAR

    override fun toString(): String {
        val komparatorText = if (fired) comparator.text else comparator.negated()
        return "${fired.svarord()} ${verdi1.hva()}$komparatorText${verdi2.hva()}"
    }
}

/**
 * Compares [verdi] relationship with items [verdiList]
 */
class ListDomainPredicate(
    override val comparator: ListComparator,
    private val verdi: Verdi<*>,
    val verdiList: List<Verdi<*>>,
    override val function: () -> Boolean
) : DomainPredicate(comparator = comparator, function = function) {

    init {
//        this.children.addAll(abstractRuleComponentList)
    }

    override fun type(): RuleComponentType = DOMENE_PREDIKAT_LISTE

    override fun toString(): String {
        val komparatorText = if (fired) comparator.text else comparator.negated()
        return "${fired.svarord()} $verdi$komparatorText$verdiList"
    }
}