package no.nav.system.rule.dsl.demo.ruleset

import no.nav.system.rule.dsl.AbstractRuleset
import no.nav.system.rule.dsl.demo.domain.PensjonsgivendeInntekt
import java.time.LocalDate

class VilkårsprøvingSlitertilleggRS(
    val inntektListe: List<PensjonsgivendeInntekt>,
    val veietGrunnbeløpListe: List<Int>,
    val uttaksdato: LocalDate,
    ) : AbstractRuleset<Boolean>() {

    override fun create() {
        TODO("Not yet implemented")
    }
}