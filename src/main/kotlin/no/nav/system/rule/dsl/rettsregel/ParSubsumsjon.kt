package no.nav.system.rule.dsl.rettsregel

import no.nav.system.rule.dsl.Predicate
import no.nav.system.rule.dsl.enums.Komparator
import svarord

/**
 * TODO Vurder om en subsumsjon (og Predicate) burde ha en evaluate variabel slik sonm Rule. Hensikten er å oppdage bruk av Sumsumsjoner som ikke har blitt evaluert enda (feilsituasjon)
 * Kanskje bør vi ha TreSubsumsjon (komparator anvendes på children) eller ParSubsumsjon (komparator anvendes på Pair)
 */
class ParSubsumsjon(
    override val komparator: Komparator,
    val faktum1: Faktum<*>,
    val faktum2: Faktum<*>,
    override val utfallFunksjon: () -> Boolean
) : AbstractSubsumsjon(komparator = komparator, utfallFunksjon = utfallFunksjon) {

    init {
        this.children.add(faktum1)
        this.children.add(faktum2)
    }

    override fun type(): String = "ParSubsumsjon"

    override fun toString(): String {
        val komparatorText = if (fired) komparator.text else komparator.negated()
        return "${fired.svarord()} ${faktum1}$komparatorText${faktum2}"
    }
}

class MengdeSubsumsjon(
    override val komparator: Komparator,
    val faktum: List<Faktum<*>>,
    override val utfallFunksjon: () -> Boolean
) : AbstractSubsumsjon(komparator = komparator, utfallFunksjon = utfallFunksjon) {

    init {
        this.children.addAll(faktum)
    }

    override fun type(): String {
        return "MengdeSubsumsjon"
    }

    override fun toString(): String {
        return "MengdeSubsumsjon(komparator=$komparator, faktum=$faktum, utfallFunksjon=$utfallFunksjon)"
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
        parent!!.children.add(this)
        fired = utfallFunksjon.invoke()
        return false
    }
}