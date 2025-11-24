package no.nav.pensjon.sliterordning.regelsett

import no.nav.pensjon.sliterordning.grunnlag.NormertPensjonsalder
import no.nav.pensjon.sliterordning.grunnlag.Person
import no.nav.pensjon.sliterordning.grunnlag.Trygdetid
import no.nav.system.rule.dsl.explanation.Direction
import no.nav.system.rule.dsl.explanation.explain
import no.nav.system.rule.dsl.explanation.forklar
import no.nav.system.rule.dsl.explanation.toIndentedText
import no.nav.system.rule.dsl.perspectives.Perspective
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.YearMonth

class BeregnSlitertilleggRSForklartFaktumMedDomenePredikatSekvensielleReglerVersjonTest {

    private fun person(fodselsdato: YearMonth, trygdetidMnd: Int): Person =
        Person(fodselsdato, Trygdetid(trygdetidMnd), NormertPensjonsalder.default())

    @Test
    fun `SLITERTILLEGG-BEREGNING-UAVKORTET - uttak lik nedrePensjonsDato og full trygdetid`() {
        val slitertillegg = BeregnSlitertilleggRSFaktumMedDomenePredikatSekvensielleReglerVersjon(
            innUttakstidspunkt = YearMonth.of(2024, 1),
            innPerson = person(YearMonth.of(1961, 12), 40),
            innGrunnbeløp = 110000
        ).test()

        // ASSERT FAKTUM
        assertEquals("slitertillegg", slitertillegg.navn)
        assertEquals(2291.67, slitertillegg.verdi, 0.01)

        // ASSERT FORKLARING
        val forklaringIterator = slitertillegg.forklar().split("\n").map { it.trim() }.filter { it.isNotBlank() }.iterator()

        // ASSERT HVA
        assertEquals("HVA", forklaringIterator.next())
        assertEquals("slitertillegg = 2291.6666666666665", forklaringIterator.next())

        // ASSERT HVORFOR
        assertEquals("HVORFOR", forklaringIterator.next())
        assertEquals(
            "regel: JA BeregnSlitertilleggRSFaktumMedDomenePredikatSekvensielleReglerVersjon.SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT-OG-AVKORTING-TRYGDETID",
            forklaringIterator.next()
        )

        // ASSERT HVORDAN slitertillegg
        assertEquals("HVORDAN", forklaringIterator.next())
        assertEquals("fulltSlitertillegg * justeringsFaktor * trygdetidFaktor", forklaringIterator.next())
        assertEquals("2291.6666666666665 * 1.0 * 1.0", forklaringIterator.next())

        // ASSERT HVA (rekursjon) fulltSlitertillegg
        assertEquals("HVA", forklaringIterator.next())
        assertEquals("fulltSlitertillegg = 2291.6666666666665", forklaringIterator.next())

        // ASSERT HVORDAN fulltSlitertillegg
        assertEquals("HVORDAN", forklaringIterator.next())
        assertEquals("0.25 * G / 12", forklaringIterator.next())
        assertEquals("0.25 * 110000 / 12", forklaringIterator.next())

        // ASSERT HVA (rekursjon) G
        assertEquals("HVA", forklaringIterator.next())
        assertEquals("G = 110000", forklaringIterator.next())

        // ASSERT HVA (rekursjon) justeringsFaktor
        assertEquals("HVA", forklaringIterator.next())
        assertEquals("justeringsFaktor = 1.0", forklaringIterator.next())

        // ASSERT HVORFOR justeringsFaktor
        assertEquals("HVORFOR", forklaringIterator.next())
        assertEquals(
            "regel: JA BeregnSlitertilleggRSFaktumMedDomenePredikatSekvensielleReglerVersjon.SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT-TIDLIG",
            forklaringIterator.next()
        )
        assertEquals("JA 'antallMånederEtterNedrePensjonsDato' er mindre enn '36'", forklaringIterator.next())
        assertEquals("JA '0' er mindre enn '36'", forklaringIterator.next())

        // ASSERT HVORDAN justeringsFaktor
        assertEquals("HVORDAN", forklaringIterator.next())
        assertEquals("(36 - antallMånederEtterNedrePensjonsDato) / 36", forklaringIterator.next())
        assertEquals("(36 - 0) / 36", forklaringIterator.next())

        // ASSERT HVA antallMånederEtterNedrePensjonsDato
        assertEquals("HVA", forklaringIterator.next())
        assertEquals("antallMånederEtterNedrePensjonsDato = 0", forklaringIterator.next())

        // ASSERT HVA trygdetidFaktor
        assertEquals("HVA", forklaringIterator.next())
        assertEquals("trygdetidFaktor = 1.0", forklaringIterator.next())

        // ASSERT HVORDAN trygdetidFaktor
        assertEquals("HVORDAN", forklaringIterator.next())
        assertEquals("faktiskTrygdetid / fullTrygdetid", forklaringIterator.next())
        assertEquals("40 / 40", forklaringIterator.next())
    }

    @Test
    fun `SLITERTILLEGG-AVKORTING-TRYGDETID - uttak lik nedrePensjonsDato og redusert trygdetid`() {
        val slitertillegg = BeregnSlitertilleggRSFaktumMedDomenePredikatSekvensielleReglerVersjon(
            innUttakstidspunkt = YearMonth.of(2024, 1),
            innPerson = person(YearMonth.of(1961, 12), 30),
            innGrunnbeløp = 110000
        ).test()

        // ASSERT FAKTUM
        assertEquals("slitertillegg", slitertillegg.navn)
        assertEquals(1718.75, slitertillegg.verdi, 0.01)

        // ASSERT FORKLARING
        val forklaringIterator = slitertillegg.forklar().split("\n").map { it.trim() }.filter { it.isNotBlank() }.iterator()

        // ASSERT HVA
        assertEquals("HVA", forklaringIterator.next())
        assertEquals("slitertillegg = 1718.75", forklaringIterator.next())

        // ASSERT HVORFOR
        assertEquals("HVORFOR", forklaringIterator.next())
        assertEquals(
            "regel: JA BeregnSlitertilleggRSFaktumMedDomenePredikatSekvensielleReglerVersjon.SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT-OG-AVKORTING-TRYGDETID",
            forklaringIterator.next()
        )

        // ASSERT HVORDAN slitertillegg
        assertEquals("HVORDAN", forklaringIterator.next())
        assertEquals("fulltSlitertillegg * justeringsFaktor * trygdetidFaktor", forklaringIterator.next())
        assertEquals("2291.6666666666665 * 1.0 * 0.75", forklaringIterator.next())

        // ASSERT HVA (rekursjon) fulltSlitertillegg
        assertEquals("HVA", forklaringIterator.next())
        assertEquals("fulltSlitertillegg = 2291.6666666666665", forklaringIterator.next())

        // ASSERT HVORDAN fulltSlitertillegg
        assertEquals("HVORDAN", forklaringIterator.next())
        assertEquals("0.25 * G / 12", forklaringIterator.next())
        assertEquals("0.25 * 110000 / 12", forklaringIterator.next())

        // ASSERT HVA G
        assertEquals("HVA", forklaringIterator.next())
        assertEquals("G = 110000", forklaringIterator.next())

        // ASSERT HVA G
        assertEquals("HVA", forklaringIterator.next())
        assertEquals("justeringsFaktor = 1.0", forklaringIterator.next())

        // ASSERT HVORFOR justeringsFaktor
        assertEquals("HVORFOR", forklaringIterator.next())
        assertEquals(
            "regel: JA BeregnSlitertilleggRSFaktumMedDomenePredikatSekvensielleReglerVersjon.SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT-TIDLIG",
            forklaringIterator.next()
        )
        assertEquals("JA 'antallMånederEtterNedrePensjonsDato' er mindre enn '36'", forklaringIterator.next())
        assertEquals("JA '0' er mindre enn '36'", forklaringIterator.next())

        // ASSERT HVORDAN justeringsFaktor
        assertEquals("HVORDAN", forklaringIterator.next())
        assertEquals("(36 - antallMånederEtterNedrePensjonsDato) / 36", forklaringIterator.next())
        assertEquals("(36 - 0) / 36", forklaringIterator.next())

        // ASSERT HVA antallMånederEtterNedrePensjonsDato
        assertEquals("HVA", forklaringIterator.next())
        assertEquals("antallMånederEtterNedrePensjonsDato = 0", forklaringIterator.next())

        // ASSERT HVA trygdetidFaktor
        assertEquals("HVA", forklaringIterator.next())
        assertEquals("trygdetidFaktor = 0.75", forklaringIterator.next())

        // ASSERT HVORDAN trygdetidFaktor
        assertEquals("HVORDAN", forklaringIterator.next())
        assertEquals("faktiskTrygdetid / fullTrygdetid", forklaringIterator.next())
        assertEquals("30 / 40", forklaringIterator.next())
    }

    @Test
    fun `SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT - uttak etter nedrePensjonsDato og full trygdetid`() {
        val slitertillegg = BeregnSlitertilleggRSFaktumMedDomenePredikatSekvensielleReglerVersjon(
            innUttakstidspunkt = YearMonth.of(2025, 9), // 20 måneder etter nedre pensjonsalder,
            innPerson = person(YearMonth.of(1961, 12), 40), // nedre pensjonsdato 2024-01,
            innGrunnbeløp = 110000
        ).test()

        // ASSERT slitertillegg
        assertEquals("slitertillegg", slitertillegg.navn)
        assertEquals(1018.52, slitertillegg.verdi, 0.01)

        // FORKLARING
        val forklaringIterator = slitertillegg.forklar().split("\n").map { it.trim() }.filter { it.isNotBlank() }.iterator()

        // ASSERT HVA
        assertEquals("HVA", forklaringIterator.next())
        assertEquals("slitertillegg = 1018.5185185185184", forklaringIterator.next())

        // ASSERT HVORFOR
        assertEquals("HVORFOR", forklaringIterator.next())
        assertEquals(
            "regel: JA BeregnSlitertilleggRSFaktumMedDomenePredikatSekvensielleReglerVersjon.SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT-OG-AVKORTING-TRYGDETID",
            forklaringIterator.next()
        )

        // ASSERT HVORDAN slitertillegg
        assertEquals("HVORDAN", forklaringIterator.next())
        assertEquals("fulltSlitertillegg * justeringsFaktor * trygdetidFaktor", forklaringIterator.next())
        assertEquals("2291.6666666666665 * 0.4444444444444444 * 1.0", forklaringIterator.next())

        // ASSERT HVA (rekursjon) fulltSlitertillegg
        assertEquals("HVA", forklaringIterator.next())
        assertEquals("fulltSlitertillegg = 2291.6666666666665", forklaringIterator.next())

        // ASSERT HVORDAN fulltSlitertillegg
        assertEquals("HVORDAN", forklaringIterator.next())
        assertEquals("0.25 * G / 12", forklaringIterator.next())
        assertEquals("0.25 * 110000 / 12", forklaringIterator.next())

        // ASSERT HVA (rekursjon) G
        assertEquals("HVA", forklaringIterator.next())
        assertEquals("G = 110000", forklaringIterator.next())

        // ASSERT HVORFOR justeringsFaktor
        assertEquals("HVA", forklaringIterator.next())
        assertEquals("justeringsFaktor = 0.4444444444444444", forklaringIterator.next())

        // ASSERT HVORFOR justeringsFaktor
        assertEquals("HVORFOR", forklaringIterator.next())
        assertEquals(
            "regel: JA BeregnSlitertilleggRSFaktumMedDomenePredikatSekvensielleReglerVersjon.SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT-TIDLIG",
            forklaringIterator.next()
        )
        assertEquals("JA 'antallMånederEtterNedrePensjonsDato' er mindre enn '36'", forklaringIterator.next())
        assertEquals("JA '20' er mindre enn '36'", forklaringIterator.next())

        // ASSERT HVORDAN justeringsFaktor
        assertEquals("HVORDAN", forklaringIterator.next())
        assertEquals("(36 - antallMånederEtterNedrePensjonsDato) / 36", forklaringIterator.next())
        assertEquals("(36 - 20) / 36", forklaringIterator.next())

        // ASSERT HVA (rekursjon) antallMånederEtterNedrePensjonsDato
        assertEquals("HVA", forklaringIterator.next())
        assertEquals("antallMånederEtterNedrePensjonsDato = 20", forklaringIterator.next())

        // ASSERT HVA (rekursjon) trygdetidFaktor
        assertEquals("HVA", forklaringIterator.next())
        assertEquals("trygdetidFaktor = 1.0", forklaringIterator.next())

        // ASSERT HVORDAN trygdetidFaktor
        assertEquals("HVORDAN", forklaringIterator.next())
        assertEquals("faktiskTrygdetid / fullTrygdetid", forklaringIterator.next())
        assertEquals("40 / 40", forklaringIterator.next())
    }

    @Test
    fun `SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT-OG-AVKORTING-TRYGDETID - uttak før virkning og redusert trygdetid`() {
        val slitertilleggRS = BeregnSlitertilleggRSFaktumMedDomenePredikatSekvensielleReglerVersjon(
            innUttakstidspunkt = YearMonth.of(2025, 9), // 20 måneder etter nedre pensjonsalder,
            innPerson = person(YearMonth.of(1961, 12), 20),// nedre pensjonsdato 2024-01,
            innGrunnbeløp = 110000
        )
        val slitertillegg = slitertilleggRS.test()

        // ASSERT FAKTUM
        assertEquals("slitertillegg", slitertillegg.navn)
        assertEquals(509.26, slitertillegg.verdi, 0.01)

        // ASSERT FORKLARING
        println(slitertilleggRS.explain().direction(d= Direction.DOWN).perspective(p=Perspective.FULL).transform(::toIndentedText))
        val forklaringIterator = slitertillegg.forklar().split("\n").map { it.trim() }.filter { it.isNotBlank() }.iterator()

        // ASSERT HVA
        assertEquals("HVA", forklaringIterator.next())
        assertEquals("slitertillegg = 509.2592592592592", forklaringIterator.next())

        // ASSERT HVORFOR
        assertEquals("HVORFOR", forklaringIterator.next())
        assertEquals(
            "regel: JA BeregnSlitertilleggRSFaktumMedDomenePredikatSekvensielleReglerVersjon.SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT-OG-AVKORTING-TRYGDETID",
            forklaringIterator.next()
        )

        // ASSERT HVORDAN slitertillegg
        assertEquals("HVORDAN", forklaringIterator.next())
        assertEquals("fulltSlitertillegg * justeringsFaktor * trygdetidFaktor", forklaringIterator.next())
        assertEquals("2291.6666666666665 * 0.4444444444444444 * 0.5", forklaringIterator.next())

        // ASSERT HVA (rekursjon) fulltSlitertillegg
        assertEquals("HVA", forklaringIterator.next())
        assertEquals("fulltSlitertillegg = 2291.6666666666665", forklaringIterator.next())

        // ASSERT HVORDAN fulltSlitertillegg
        assertEquals("HVORDAN", forklaringIterator.next())
        assertEquals("0.25 * G / 12", forklaringIterator.next())
        assertEquals("0.25 * 110000 / 12", forklaringIterator.next())

        // ASSERT HVA (rekursjon) G
        assertEquals("HVA", forklaringIterator.next())
        assertEquals("G = 110000", forklaringIterator.next())

        // ASSERT HVA (rekursjon) justeringsFaktor
        assertEquals("HVA", forklaringIterator.next())
        assertEquals("justeringsFaktor = 0.4444444444444444", forklaringIterator.next())

        // ASSERT HVORFOR justeringsFaktor
        assertEquals("HVORFOR", forklaringIterator.next())
        assertEquals(
            "regel: JA BeregnSlitertilleggRSFaktumMedDomenePredikatSekvensielleReglerVersjon.SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT-TIDLIG",
            forklaringIterator.next()
        )
        assertEquals("JA 'antallMånederEtterNedrePensjonsDato' er mindre enn '36'", forklaringIterator.next())
        assertEquals("JA '20' er mindre enn '36'", forklaringIterator.next())

        // ASSERT HVORDAN justeringsFaktor
        assertEquals("HVORDAN", forklaringIterator.next())
        assertEquals("(36 - antallMånederEtterNedrePensjonsDato) / 36", forklaringIterator.next())
        assertEquals("(36 - 20) / 36", forklaringIterator.next())

        // ASSERT HVA (rekursjon) antallMånederEtterNedrePensjonsDato
        assertEquals("HVA", forklaringIterator.next())
        assertEquals("antallMånederEtterNedrePensjonsDato = 20", forklaringIterator.next())

        // ASSERT HVA (rekursjon) trygdetidFaktor
        assertEquals("HVA", forklaringIterator.next())
        assertEquals("trygdetidFaktor = 0.5", forklaringIterator.next())

        // ASSERT HVORDAN trygdetidFaktor
        assertEquals("HVORDAN", forklaringIterator.next())
        assertEquals("faktiskTrygdetid / fullTrygdetid", forklaringIterator.next())
        assertEquals("20 / 40", forklaringIterator.next())
    }
}
