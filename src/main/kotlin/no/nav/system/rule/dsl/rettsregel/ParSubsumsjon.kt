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
 * TODO Vurder om en subsumsjon (og Predicate) burde ha en evaluate variabel slik sonm Rule. Hensikten er å oppdage bruk av Sumsumsjoner som ikke har blitt evaluert enda (feilsituasjon)
 */
class ParSubsumsjon(
    override val komparator: ParKomparator,
    private val faktum1: Faktum<*>,
    private val faktum2: Faktum<*>,
    override val utfallFunksjon: () -> Boolean,
) : AbstractSubsumsjon(komparator = komparator, utfallFunksjon = utfallFunksjon) {

    init {
        this.children.add(faktum1)
        this.children.add(faktum2)
        this.evaluate()
    }

    override fun type(): RuleComponentType = PAR_SUBSUMSJON

    override fun toString(): String {
        val komparatorText = if (fired) komparator.text else komparator.negated()
        val f1text = if (faktum1.anonymous) "'${faktum1.navn}'" else "'${faktum1.navn}' (${faktum1.verdi})"
        val f2text = if (faktum2.anonymous) "'${faktum2.navn}'" else "'${faktum2.navn}' (${faktum2.verdi})"
        return "${type()}: ${fired.svarord()} $f1text$komparatorText${f2text}"
    }
}

class MengdeSubsumsjon(
    override val komparator: MengdeKomparator,
    private val faktum: Faktum<*>,
    private val abstractRuleComponentList: List<AbstractRuleComponent>,
    override val utfallFunksjon: () -> Boolean,
) : AbstractSubsumsjon(komparator = komparator, utfallFunksjon = utfallFunksjon) {

    init {
        this.children.addAll(abstractRuleComponentList)
        this.evaluate()
    }

    override fun type(): RuleComponentType = MENGDE_SUBSUMSJON

    override fun toString(): String {
//        return "${type()}: ${fired.svarord()} ${faktum.navn} (${faktum.verdi})${komparator.text}:"
        val komparatorText = if (fired) komparator.text else komparator.negated()
        val f1text = if (faktum.anonymous) "'${faktum.navn}'" else "'${faktum.navn}' (${faktum.verdi})"
        val f2text = abstractRuleComponentList.toString()
        return "${type()}: ${fired.svarord()} $f1text$komparatorText${f2text}"
    }
}

abstract class AbstractSubsumsjon(
    open val komparator: Komparator,
    open val utfallFunksjon: () -> Boolean,
) : Predicate(function = utfallFunksjon) {

    /**
     * Evaluates the predicate function.
     *
     * @return returns false. Sumsumtions never terminates callers evaluation chain.
     */
    override fun evaluate(): Boolean {
        parent?.children?.add(this)
        fired = utfallFunksjon.invoke()
        return false
    }
}