package no.nav.system.rule.dsl.rettsregel

import no.nav.system.rule.dsl.Predicate
import no.nav.system.rule.dsl.enums.Comparator
import no.nav.system.rule.dsl.enums.ListComparator
import no.nav.system.rule.dsl.enums.PairComparator
import no.nav.system.rule.dsl.enums.RuleComponentType
import no.nav.system.rule.dsl.enums.RuleComponentType.LISTE_SUBSUMSJON
import no.nav.system.rule.dsl.enums.RuleComponentType.PAR_SUBSUMSJON
import no.nav.system.rule.dsl.rettsregel.helper.svarord

/**
 * The application of a [function] on [Faktum].
 */
abstract class AbstractSubsumtion(
    open val comparator: Comparator,
    override val function: () -> Boolean,
) : Predicate(function = function) {

    /**
     * Evaluates the predicate function.
     * Sumsumtions never terminates callers evaluation chain ([terminateEvaluation] )
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
class PairSubsumtion(
    override val comparator: PairComparator,
    private val verdi1: Verdi<*>,
    private val verdi2: Verdi<*>,
    override val function: () -> Boolean,
) : AbstractSubsumtion(comparator = comparator, function = function) {

    init {
//        if (verdi1.name != verdi1.value) this.children.add(verdi1)
//        if (verdi2.name != verdi2.value) this.children.add(verdi2)
    }

    override fun type(): RuleComponentType = PAR_SUBSUMSJON

    override fun toString(): String {
        val komparatorText = if (fired) comparator.text else comparator.negated()
        return "${fired.svarord()} $verdi1$komparatorText$verdi2"
    }
}

/**
 * Compares [faktum] relationship with items [verdiList]
 */
class ListSubsumtion(
    override val comparator: ListComparator,
    private val faktum: Verdi<*>,
    private val verdiList: List<Verdi<*>>,
    override val function: () -> Boolean,
) : AbstractSubsumtion(comparator = comparator, function = function) {

    init {
//        this.children.addAll(abstractRuleComponentList)
    }

    override fun type(): RuleComponentType = LISTE_SUBSUMSJON

    override fun toString(): String {
        val komparatorText = if (fired) comparator.text else comparator.negated()
        return "${fired.svarord()} $faktum$komparatorText$verdiList"
    }
}