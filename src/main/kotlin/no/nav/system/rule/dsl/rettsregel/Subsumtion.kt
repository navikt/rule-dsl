package no.nav.system.rule.dsl.rettsregel

import no.nav.system.rule.dsl.AbstractRuleComponent
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
 * Compares [faktum1] with [faktum2]
 */
class PairSubsumtion(
    override val comparator: PairComparator,
    private val faktum1: Faktum<*>,
    private val faktum2: Faktum<*>,
    override val function: () -> Boolean,
) : AbstractSubsumtion(comparator = comparator, function = function) {

    init {
        if (!faktum1.anonymous) this.children.add(faktum1)
        if (!faktum2.anonymous) this.children.add(faktum2)
    }

    override fun type(): RuleComponentType = PAR_SUBSUMSJON

    override fun toString(): String {
        val komparatorText = if (fired) comparator.text else comparator.negated()
        return "${fired.svarord()} $faktum1$komparatorText$faktum2"
    }
}

/**
 * Compares [faktum] relationship with items [abstractRuleComponentList]
 */
class ListSubsumtion(
    override val comparator: ListComparator,
    private val faktum: Faktum<*>,
    private val abstractRuleComponentList: List<AbstractRuleComponent>,
    override val function: () -> Boolean,
) : AbstractSubsumtion(comparator = comparator, function = function) {

    init {
        this.children.addAll(abstractRuleComponentList)
    }

    override fun type(): RuleComponentType = LISTE_SUBSUMSJON

    override fun toString(): String {
        val komparatorText = if (fired) comparator.text else comparator.negated()
        return "${fired.svarord()} $faktum$komparatorText${abstractRuleComponentList}"
    }
}