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
  * The application of a [function] on [Fact].
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
        parent?.children?.add(this)
        function.invoke().also { terminateEvaluation = false }
    }
}

/**
 * Compares [fact1] with [fact2]
 */
class PairSubsumtion(
    override val comparator: PairComparator,
    private val fact1: Fact<*>,
    private val fact2: Fact<*>,
    override val function: () -> Boolean,
) : AbstractSubsumtion(comparator = comparator, function = function) {

    init {
        this.children.add(fact1)
        this.children.add(fact2)
    }

    override fun type(): RuleComponentType = PAR_SUBSUMSJON

    override fun toString(): String {
        val komparatorText = if (fired) comparator.text else comparator.negated()
        return "${fired.svarord()} $fact1$komparatorText$fact2"
    }
}

/**
 * Compares [fact] relationship with items [abstractRuleComponentList]
 */
class ListSubsumtion(
    override val comparator: ListComparator,
    private val fact: Fact<*>,
    private val abstractRuleComponentList: List<AbstractRuleComponent>,
    override val function: () -> Boolean,
) : AbstractSubsumtion(comparator = comparator, function = function) {

    init {
        this.children.addAll(abstractRuleComponentList)
    }

    override fun type(): RuleComponentType = LISTE_SUBSUMSJON

    override fun toString(): String {
        val komparatorText = if (fired) comparator.text else comparator.negated()
        return "${fired.svarord()} $fact$komparatorText${abstractRuleComponentList}"
    }
}