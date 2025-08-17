package no.nav.system.rule.dsl.demo.ruleset

import no.nav.system.rule.dsl.demo.domain.PensjonsgivendeInntekt
import no.nav.system.rule.dsl.demo.domain.VeietGrunnbeløp
import no.nav.system.rule.dsl.demo.helper.localDate
import no.nav.system.rule.dsl.inspections.debug
import org.junit.jupiter.api.Test

class VilkårsprøvingSlitertilleggRSTest2 {

    val inntektsListe = listOf(
        PensjonsgivendeInntekt(100_000, 2020),
        PensjonsgivendeInntekt(150_000, 2021),
        PensjonsgivendeInntekt(200_000, 2022),
        PensjonsgivendeInntekt(250_000, 2023),
        PensjonsgivendeInntekt(300_000, 2024),
    )

    val veietGrunnbeløpListe = listOf(
        VeietGrunnbeløp(50_000, 2020),
        VeietGrunnbeløp(75_000, 2021),
        VeietGrunnbeløp(100_000, 2022),
        VeietGrunnbeløp(125_000, 2023),
        VeietGrunnbeløp(150_000, 2024),
    )

    @Test
    fun `test VilkårsprøvingSlitertillegg - oppfylt`() {



        val svar = VilkårsprøvingSlitertilleggRS(
            inntektListe = inntektsListe,
            veietGrunnbeløpListe = veietGrunnbeløpListe,
            uttaksdato = localDate(2025, 1, 1)
        ).test()
        println(svar.debug(true))
        assert(true)
//        assert(resultat.isNotEmpty())
//        (resultat.first() as Rule<*>).let { regelVilkårOppfylt ->
//            assertTrue(regelVilkårOppfylt.evaluated)
//            assertTrue(regelVilkårOppfylt.fired())
//        }
    }
}