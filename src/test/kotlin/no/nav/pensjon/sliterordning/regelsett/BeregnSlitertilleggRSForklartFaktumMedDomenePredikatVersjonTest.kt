package no.nav.pensjon.sliterordning.regelsett

import no.nav.pensjon.sliterordning.grunnlag.NormertPensjonsalder
import no.nav.pensjon.sliterordning.grunnlag.Person
import no.nav.pensjon.sliterordning.grunnlag.Trygdetid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.YearMonth

class BeregnSlitertilleggRSForklartFaktumMedDomenePredikatVersjonTest {

    private fun person(fodselsdato: YearMonth, trygdetidMnd: Int): Person =
        Person(fodselsdato, Trygdetid(trygdetidMnd), NormertPensjonsalder.default())

    @Test
    fun `SLITERTILLEGG-BEREGNING-UAVKORTET - uttak lik nedrePensjonsDato og full trygdetid`() {
        val slitertillegg = BeregnSlitertilleggRSForklartFaktumMedDomenePredikatVersjon(
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
        assertEquals("regel: JA BeregnSlitertilleggRSForklartFaktumMedDomenePredikatVersjon.SLITERTILLEGG-BEREGNING-UAVKORTET", forklaringIterator.next())
        assertEquals("JA 'nedrePensjonsDato' er lik 'uttakstidspunkt'", forklaringIterator.next())
        assertEquals("JA '2024-01' er lik '2024-01'", forklaringIterator.next())
        assertEquals("JA 'faktiskTrygdetid' er lik 'fullTrygdetid'", forklaringIterator.next())
        assertEquals("JA '40' er lik '40'", forklaringIterator.next())
    }

    @Test
    fun `SLITERTILLEGG-AVKORTING-TRYGDETID - uttak lik nedrePensjonsDato og redusert trygdetid`() {
        val slitertillegg = BeregnSlitertilleggRSForklartFaktumMedDomenePredikatVersjon(
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
        assertEquals("regel: JA BeregnSlitertilleggRSForklartFaktumMedDomenePredikatVersjon.SLITERTILLEGG-AVKORTING-TRYGDETID", forklaringIterator.next())
        assertEquals("JA 'nedrePensjonsDato' er lik 'uttakstidspunkt'", forklaringIterator.next())
        assertEquals("JA '2024-01' er lik '2024-01'", forklaringIterator.next())
        assertEquals("JA 'faktiskTrygdetid' er mindre enn 'fullTrygdetid'", forklaringIterator.next())
        assertEquals("JA '30' er mindre enn '40'", forklaringIterator.next())

        // ASSERT HVORDAN
        assertEquals("HVORDAN", forklaringIterator.next())
        assertEquals("fulltSlitertillegg * trygdetidFaktor", forklaringIterator.next())
        assertEquals("2291.6666666666665 * 0.75", forklaringIterator.next())
    }

    @Test
    fun `SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT - uttak etter nedrePensjonsDato og full trygdetid`() {
        val slitertillegg = BeregnSlitertilleggRSForklartFaktumMedDomenePredikatVersjon(
            innUttakstidspunkt = YearMonth.of(2025, 9), // 20 måneder etter nedre pensjonsalder,
            innPerson = person(YearMonth.of(1961, 12), 40), // nedre pensjonsdato 2024-01,
            innGrunnbeløp = 110000
        ).test()

        // ASSERT FAKTUM
        assertEquals("slitertillegg", slitertillegg.navn)
        assertEquals(1018.51, slitertillegg.verdi, 0.01)

        // ASSERT FORKLARING
        val forklaringIterator = slitertillegg.forklar().split("\n").map { it.trim() }.filter { it.isNotBlank() }.iterator()

        // ASSERT HVA
        assertEquals("HVA", forklaringIterator.next())
        assertEquals("slitertillegg = 1018.5185185185184", forklaringIterator.next())

        // ASSERT HVORFOR
        assertEquals("HVORFOR", forklaringIterator.next())
        assertEquals("regel: JA BeregnSlitertilleggRSForklartFaktumMedDomenePredikatVersjon.SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT", forklaringIterator.next())
        assertEquals("JA 'nedrePensjonsDato' er før 'uttakstidspunkt'", forklaringIterator.next())
        assertEquals("JA '2024-01' er før '2025-09'", forklaringIterator.next())
        assertEquals("JA 'faktiskTrygdetid' er lik 'fullTrygdetid'", forklaringIterator.next())
        assertEquals("JA '40' er lik '40'", forklaringIterator.next())

        // ASSERT HVORDAN
        assertEquals("HVORDAN", forklaringIterator.next())
        assertEquals("fulltSlitertillegg * justeringsFaktor", forklaringIterator.next())
        assertEquals("2291.6666666666665 * 0.4444444444444444", forklaringIterator.next())
    }

    @Test
    fun `SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT-OG-AVKORTING-TRYGDETID - uttak før virkning og redusert trygdetid`() {
        val slitertillegg = BeregnSlitertilleggRSForklartFaktumMedDomenePredikatVersjon(
            innUttakstidspunkt = YearMonth.of(2025, 9), // 20 måneder etter nedre pensjonsalder,
            innPerson = person(YearMonth.of(1961, 12), 20),// nedre pensjonsdato 2024-01,
            innGrunnbeløp = 110000
        ).test()

        // ASSERT FAKTUM
        assertEquals("slitertillegg", slitertillegg.navn)
        assertEquals(509.25, slitertillegg.verdi, 0.01)

        // ASSERT FORKLARING
        val forklaringIterator = slitertillegg.forklar().split("\n").map { it.trim() }.filter { it.isNotBlank() }.iterator()
        println(slitertillegg.forklar())
        // ASSERT HVA
        assertEquals("HVA", forklaringIterator.next())
        assertEquals("slitertillegg = 509.2592592592592", forklaringIterator.next())

        // ASSERT HVORFOR
        assertEquals("HVORFOR", forklaringIterator.next())
        assertEquals(
            "regel: JA BeregnSlitertilleggRSForklartFaktumMedDomenePredikatVersjon.SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT-OG-AVKORTING-TRYGDETID",
            forklaringIterator.next()
        )
        assertEquals("JA 'nedrePensjonsDato' er før 'uttakstidspunkt'", forklaringIterator.next())
        assertEquals("JA '2024-01' er før '2025-09'", forklaringIterator.next())
        assertEquals("JA 'faktiskTrygdetid' er mindre enn 'fullTrygdetid'", forklaringIterator.next())
        assertEquals("JA '20' er mindre enn '40'", forklaringIterator.next())

        // ASSERT HVORDAN
        assertEquals("HVORDAN", forklaringIterator.next())
        assertEquals("fulltSlitertillegg * justeringsFaktor * trygdetidFaktor", forklaringIterator.next())
        assertEquals("2291.6666666666665 * 0.4444444444444444 * 0.5", forklaringIterator.next())
    }
}
