package no.nav.system.rule.dsl.rettsregel

import no.nav.system.rule.dsl.AbstractRuleComponent
import no.nav.system.rule.dsl.Predicate
import no.nav.system.rule.dsl.enums.Komparator
import no.nav.system.rule.dsl.enums.MengdeKomparator
import no.nav.system.rule.dsl.enums.ParKomparator
import no.nav.system.rule.dsl.enums.RuleComponentType
import no.nav.system.rule.dsl.enums.RuleComponentType.MENGDE_SUBSUMSJON
import no.nav.system.rule.dsl.enums.RuleComponentType.PAR_SUBSUMSJON
import no.nav.system.rule.dsl.rettsregel.helper.svarord

/**
 * Subsumsjon
 * The application of a [function] on [Faktum].
 */
abstract class AbstractSubsumsjon(
    open val komparator: Komparator,
    open val funksjon: () -> Boolean,
) : Predicate(function = funksjon) {

    /**
     * Evaluates the predicate function.
     * Sumsumtions never terminates callers evaluation chain ([terminateEvaluation] )
     *
     * @return boolean result of function.
     */
    override val fired: Boolean by lazy {
        parent?.children?.add(this)
        funksjon.invoke().also { terminateEvaluation = false }
    }
}

/**
 * Compares [faktum1] with [faktum2]
 */
class ParSubsumsjon(
    override val komparator: ParKomparator,
    private val faktum1: Faktum<*>,
    private val faktum2: Faktum<*>,
    override val funksjon: () -> Boolean,
) : AbstractSubsumsjon(komparator = komparator, funksjon = funksjon) {

    init {
        this.children.add(faktum1)
        this.children.add(faktum2)
    }

    override fun type(): RuleComponentType = PAR_SUBSUMSJON

    override fun toString(): String {
        val komparatorText = if (fired) komparator.text else komparator.negated()
        return "${fired.svarord()} $faktum1$komparatorText$faktum2"
    }
}

/**
 * Compares [faktum] relationship with items [abstractRuleComponentList]
 */
class MengdeSubsumsjon(
    override val komparator: MengdeKomparator,
    private val faktum: Faktum<*>,
    private val abstractRuleComponentList: List<AbstractRuleComponent>,
    override val funksjon: () -> Boolean,
) : AbstractSubsumsjon(komparator = komparator, funksjon = funksjon) {

    init {
        this.children.addAll(abstractRuleComponentList)
    }

    override fun type(): RuleComponentType = MENGDE_SUBSUMSJON

    override fun toString(): String {
        val komparatorText = if (fired) komparator.text else komparator.negated()
        return "${fired.svarord()} $faktum$komparatorText${abstractRuleComponentList}"
    }
}