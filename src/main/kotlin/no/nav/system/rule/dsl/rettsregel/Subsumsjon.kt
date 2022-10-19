package no.nav.system.rule.dsl.rettsregel

import no.nav.system.rule.dsl.Predicate
import no.nav.system.rule.dsl.enums.Komparator
import svarord

/**
 * TODO Vurder om en subsumsjon (og Predicate) burde ha en evaluate variabel slik sonm Rule. Hensikten er å oppdage bruk av Sumsumsjoner som ikke har blitt evaluert enda (feilsituasjon)
 * Kanskje bør vi ha TreSubsumsjon (komparator anvendes på children) eller ParSubsumsjon (komparator anvendes på Pair)
 */
class Subsumsjon(
    val komparator: Komparator,
    val pair: Pair<Faktum<*>, Faktum<*>>? = null,
    val utfallFunksjon: () -> Boolean,
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

    override fun type(): String = "Subsumsjon"

    override fun toString(): String {
        val komparatorText = if (fired) komparator.text else komparator.negated()
        return "${fired.svarord()}: ${pair?.first ?: ""}$komparatorText${pair?.second ?: ""}"
    }
}
