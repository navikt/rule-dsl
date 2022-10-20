package no.nav.system.rule.dsl.rettsregel

import no.nav.system.rule.dsl.AbstractRuleComponent
import no.nav.system.rule.dsl.Predicate
import no.nav.system.rule.dsl.enums.Komparator
import no.nav.system.rule.dsl.enums.MengdeKomparator
import no.nav.system.rule.dsl.enums.ParKomparator
import no.nav.system.rule.dsl.enums.RuleComponentType
import no.nav.system.rule.dsl.enums.RuleComponentType.MENGDE_SUBSUMSJON
import no.nav.system.rule.dsl.enums.RuleComponentType.PAR_SUBSUMSJON
import svarord

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
    }

    override fun type(): RuleComponentType = PAR_SUBSUMSJON

    override fun toString(): String {
        val komparatorText = if (fired) komparator.text else komparator.negated()
        return "${fired.svarord()} ${faktum1}$komparatorText${faktum2}"
    }
}

class MengdeSubsumsjon(
    override val komparator: MengdeKomparator,
    private val faktum: Faktum<String>,
    private val faktumList: List<AbstractRuleComponent>,
    override val utfallFunksjon: () -> Boolean,
) : AbstractSubsumsjon(komparator = komparator, utfallFunksjon = utfallFunksjon) {

    init {
        this.children.addAll(faktumList)
    }

    override fun type(): RuleComponentType {
        return MENGDE_SUBSUMSJON
    }

    override fun toString(): String {
        return "${fired.svarord()} ${faktum.navn} (${faktum.verdi})${komparator.text}:"
    }
}

abstract class AbstractSubsumsjon(
    open val komparator: Komparator,
    open val utfallFunksjon: () -> Boolean
) : Predicate(function = utfallFunksjon) {

    /**
     * Evaluates the predicate function.
     *
     * @return returns true if further evaluation of remaining predicates in the rule should be prevented.
     */
    override fun evaluate(): Boolean {
        parent?.children?.add(this)
        fired = utfallFunksjon.invoke()
        return false
    }
}