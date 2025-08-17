package no.nav.system.rule.dsl.demo.helper

import no.nav.system.rule.dsl.demo.domain.PensjonsgivendeInntekt
import no.nav.system.rule.dsl.demo.domain.VeietGrunnbeløp
import no.nav.system.rule.dsl.rettsregel.Faktum
import java.time.LocalDate

fun vilkårsprøve(
    inntektListe: List<PensjonsgivendeInntekt>,
    veietGrunnbeløpListe: List<VeietGrunnbeløp>, // antar veiet grunnbeløp siste 3 år
    uttaksdato: LocalDate,
): Faktum<Boolean> {
    return Faktum("test", true)
}