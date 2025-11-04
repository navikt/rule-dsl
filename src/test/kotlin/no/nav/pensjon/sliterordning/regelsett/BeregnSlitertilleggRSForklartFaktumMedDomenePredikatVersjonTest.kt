package no.nav.pensjon.sliterordning.regelsett

import no.nav.pensjon.sliterordning.grunnlag.NormertPensjonsalder
import no.nav.pensjon.sliterordning.grunnlag.Person
import no.nav.pensjon.sliterordning.grunnlag.Trygdetid
import no.nav.system.rule.dsl.inspections.hvorfor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.YearMonth

class BeregnSlitertilleggRSForklartFaktumMedDomenePredikatVersjonTest {

    private fun person(fodselsdato: YearMonth, trygdetidMnd: Int): Person =
        Person(fodselsdato, Trygdetid(trygdetidMnd), NormertPensjonsalder.default())

    @Test
    fun `SLITERTILLEGG-BEREGNING-UAVKORTET - uttak lik nedrePensjonsDato og full trygdetid`() {
//        val slitertillegg = BeregnSlitertilleggRSForklartFaktumMedDomenePredikatVersjon(
//            innUttakstidspunkt = YearMonth.of(2024, 1),
//            innPerson = person(YearMonth.of(1961, 12), 40),
//            innGrunnbeløp = 110000
//        ).test()
                val rs = BeregnSlitertilleggRSForklartFaktumMedDomenePredikatVersjon(
            innUttakstidspunkt = YearMonth.of(2024, 1),
            innPerson = person(YearMonth.of(1961, 12), 40),
            innGrunnbeløp = 110000
        )

        val slitertillegg = rs.test()


        // ASSERT HVA
        assertEquals("slitertillegg", slitertillegg.navn)
        assertEquals(2291.67, slitertillegg.evaluer(), 0.01)

        // ASSERT HVORFOR
        val hvorforParts = slitertillegg.hvorfor().split("\n").map { it.trim() }
        assertEquals("regelsett: BeregnSlitertilleggRSForklartFaktumMedDomenePredikatVersjon", hvorforParts[0])
        assertEquals("regel: JA BeregnSlitertilleggRSForklartFaktumMedDomenePredikatVersjon.SLITERTILLEGG-BEREGNING-UAVKORTET", hvorforParts[1])
        assertEquals("JA 'nedrePensjonsDato' (2024-01) er lik 'uttakstidspunkt' (2024-01)", hvorforParts[2])
        assertEquals("JA 'faktiskTrygdetid' (40) er lik 'fullTrygdetid' (40)", hvorforParts[3])

        // ASSERT HVORDAN
        assertEquals("0.25 * G / 12", slitertillegg.hvordan())
        assertEquals("0.25 * 110000 / 12", slitertillegg.hvordan())
    }

    @Test
    fun `SLITERTILLEGG-AVKORTING-TRYGDETID - uttak lik nedrePensjonsDato og redusert trygdetid`() {
        val slitertillegg = BeregnSlitertilleggRSForklartFaktumMedDomenePredikatVersjon(
            innUttakstidspunkt = YearMonth.of(2024, 1),
            innPerson = person(YearMonth.of(1961, 12), 30),
            innGrunnbeløp = 110000
        ).test()

        // ASSERT HVA
        assertEquals("slitertillegg", slitertillegg.navn)
        assertEquals(1718.75, slitertillegg.evaluer(), 0.01)

        // ASSERT HVORFOR
        val hvorforParts = slitertillegg.hvorfor().split("\n").map { it.trim() }
        assertEquals("regelsett: BeregnSlitertilleggRSForklartFaktumMedDomenePredikatVersjon", hvorforParts[0])
        assertEquals("regel: JA BeregnSlitertilleggRSForklartFaktumMedDomenePredikatVersjon.SLITERTILLEGG-AVKORTING-TRYGDETID", hvorforParts[1])
        assertEquals("JA 'nedrePensjonsDato' (2024-01) er lik 'uttakstidspunkt' (2024-01)", hvorforParts[2])
        assertEquals("JA 'faktiskTrygdetid' (30) er mindre enn 'fullTrygdetid' (40)", hvorforParts[3])

        // ASSERT HVORDAN
        assertEquals("fulltSlitertillegg * trygdetidFaktor", slitertillegg.hvordan())
        assertEquals("2291.6666666666665 * 0.75", slitertillegg.hvordan())

//        slitertillegg.hvordan().subFormelList.first().let { fulltSlitertillegg ->
//            assertEquals("fulltSlitertillegg", fulltSlitertillegg.navn)
//            assertEquals(2291.66, fulltSlitertillegg.evaluer().toDouble(), 0.01)
//            assertEquals("0.25 * G / 12", fulltSlitertillegg.notasjon)
//            assertEquals("0.25 * 110000 / 12", fulltSlitertillegg.innhold)
//        }
//
//        slitertillegg.hvordan().subFormelList.last().let { trygdetidFaktor ->
//            assertEquals("trygdetidFaktor", trygdetidFaktor.navn)
//            assertEquals(0.75, trygdetidFaktor.evaluer().toDouble(), 0.01)
//            assertEquals("faktiskTrygdetid / 40", trygdetidFaktor.notasjon)
//            assertEquals("30 / 40", trygdetidFaktor.innhold)
//        }
    }

    @Test
    fun `SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT - uttak etter nedrePensjonsDato og full trygdetid`() {
        val slitertillegg = BeregnSlitertilleggRSForklartFaktumMedDomenePredikatVersjon(
            innUttakstidspunkt = YearMonth.of(2025, 9), // 20 måneder etter nedre pensjonsalder,
            innPerson = person(YearMonth.of(1961, 12), 40), // nedre pensjonsdato 2024-01,
            innGrunnbeløp = 110000
        ).test()

        // ASSERT HVA
        assertEquals("slitertillegg", slitertillegg.navn)
        assertEquals(1018.51, slitertillegg.evaluer(), 0.01)

        // ASSERT HVORFOR
        val hvorforParts = slitertillegg.hvorfor().split("\n").map { it.trim() }
        assertEquals("regelsett: BeregnSlitertilleggRSForklartFaktumMedDomenePredikatVersjon", hvorforParts[0])
        assertEquals("regel: JA BeregnSlitertilleggRSForklartFaktumMedDomenePredikatVersjon.SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT", hvorforParts[1])
        assertEquals("JA 'nedrePensjonsDato' (2024-01) er før 'uttakstidspunkt' (2025-09)", hvorforParts[2])
        assertEquals("JA 'faktiskTrygdetid' (40) er lik 'fullTrygdetid' (40)", hvorforParts[3])

        // ASSERT HVORDAN
        assertEquals("fulltSlitertillegg * justeringsFaktor", slitertillegg.hvordan())
        assertEquals("2291.6666666666665 * 0.4444444444444444", slitertillegg.hvordan())

//        slitertillegg.hvordan().subFormelList.first().let { fulltSlitertillegg ->
//            assertEquals("fulltSlitertillegg", fulltSlitertillegg.navn)
//            assertEquals(2291.66, fulltSlitertillegg.evaluer().toDouble(), 0.01)
//            assertEquals("0.25 * G / 12", fulltSlitertillegg.notasjon)
//            assertEquals("0.25 * 110000 / 12", fulltSlitertillegg.innhold)
//        }
//
//        slitertillegg.hvordan().subFormelList.last().let { justeringsFaktor ->
//            assertEquals("justeringsFaktor", justeringsFaktor.navn)
//            assertEquals(0.44, justeringsFaktor.evaluer().toDouble(), 0.01)
//            assertEquals("(36 - antallMånederEtterNedrePensjonsDato) / 36", justeringsFaktor.notasjon)
//            assertEquals("(36 - 20) / 36", justeringsFaktor.innhold)
//        }

    }

    @Test
    fun `SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT-OG-AVKORTING-TRYGDETID - uttak før virkning og redusert trygdetid`() {

        val slitertillegg = BeregnSlitertilleggRSForklartFaktumMedDomenePredikatVersjon(
            innUttakstidspunkt = YearMonth.of(2025, 9), // 20 måneder etter nedre pensjonsalder,
            innPerson = person(YearMonth.of(1961, 12), 20),// nedre pensjonsdato 2024-01,
            innGrunnbeløp = 110000
        ).test()

        // ASSERT HVA
        assertEquals("slitertillegg", slitertillegg.navn)
        assertEquals(509.25, slitertillegg.evaluer(), 0.01)

        // ASSERT HVORFOR
        val hvorforParts = slitertillegg.hvorfor().split("\n").map { it.trim() }
        assertEquals("regelsett: BeregnSlitertilleggRSForklartFaktumMedDomenePredikatVersjon", hvorforParts[0])
        assertEquals(
            "regel: JA BeregnSlitertilleggRSForklartFaktumMedDomenePredikatVersjon.SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT-OG-AVKORTING-TRYGDETID",
            hvorforParts[1]
        )
        assertEquals("JA 'nedrePensjonsDato' (2024-01) er før 'uttakstidspunkt' (2025-09)", hvorforParts[2])
        assertEquals("JA 'faktiskTrygdetid' (20) er mindre enn 'fullTrygdetid' (40)", hvorforParts[3])

        // ASSERT HVORDAN
        assertEquals("fulltSlitertillegg * justeringsFaktor * trygdetidFaktor", slitertillegg.hvordan())
        assertEquals("2291.6666666666665 * 0.4444444444444444 * 0.5", slitertillegg.hvordan())

//        slitertillegg.hvordan().subFormelList.elementAt(0).let { fulltSlitertillegg ->
//            assertEquals("fulltSlitertillegg", fulltSlitertillegg.navn)
//            assertEquals(2291.66, fulltSlitertillegg.evaluer().toDouble(), 0.01)
//            assertEquals("0.25 * G / 12", fulltSlitertillegg.notasjon)
//            assertEquals("0.25 * 110000 / 12", fulltSlitertillegg.innhold)
//        }
//
//        slitertillegg.hvordan().subFormelList.elementAt(1).let { justeringsFaktor ->
//            assertEquals("justeringsFaktor", justeringsFaktor.navn)
//            assertEquals(0.44, justeringsFaktor.evaluer().toDouble(), 0.01)
//            assertEquals("(36 - antallMånederEtterNedrePensjonsDato) / 36", justeringsFaktor.notasjon)
//            assertEquals("(36 - 20) / 36", justeringsFaktor.innhold)
//        }
//        slitertillegg.hvordan().subFormelList.elementAt(2).let { trygdetidFaktor ->
//            assertEquals("trygdetidFaktor", trygdetidFaktor.navn)
//            assertEquals(0.5, trygdetidFaktor.evaluer().toDouble(), 0.01)
//            assertEquals("faktiskTrygdetid / 40", trygdetidFaktor.notasjon)
//            assertEquals("20 / 40", trygdetidFaktor.innhold)
//        }
    }
}
