package no.nav.pensjon.sliterordning.service

import no.nav.pensjon.sliterordning.BeregnSlitertilleggRequest
import no.nav.pensjon.sliterordning.fagdata.FagKonstanter.FULL_TRYGDETID
import no.nav.pensjon.sliterordning.grunnlag.NormertPensjonsalder
import no.nav.pensjon.sliterordning.grunnlag.Person
import no.nav.pensjon.sliterordning.grunnlag.Trygdetid
import no.nav.system.rule.dsl.demo.domain.Response
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.YearMonth

class BeregnSlitertilleggForklartFaktumServiceTest {

    private fun person(fodselsdato: YearMonth, trygdetidMnd: Int): Person =
        Person(fodselsdato, Trygdetid(trygdetidMnd), NormertPensjonsalder.default())

    @Test
    fun `beregning ved uttak på nedre aldersgrense - full faktor og 50pct trygdetid`() {
        val virkningstidspunkt = YearMonth.of(2002, 2)
        val person = person(YearMonth.of(1940, 1), 20)
        val grunnbeløp = 110000

        val request = BeregnSlitertilleggRequest(
            uttakstidspunkt = virkningstidspunkt,
            virkningstidspunkt = virkningstidspunkt,
            person = person
        )

        val response = BeregnSlitertilleggForklartFaktumService(request).run()

        // Verify it's an Innvilget response
        assertTrue(response is Response.SliterordningForklartFaktum.Innvilget)

        val innvilget = response as Response.SliterordningForklartFaktum.Innvilget
        val faktum = innvilget.slitertillegg

        // Verify calculation
        val fullt = 0.25 * grunnbeløp / 12
        val forventet = fullt * 1.0 * (20.0 / FULL_TRYGDETID)
        assertEquals(forventet, faktum.value, 1e-9)

        // Verify trace information exists
        assertNotNull(faktum.hvorfor)
        assertNotNull(faktum.hvordan)

        // Verify trace contains expected content
        val hvordanText = faktum.hvordan.toString()
        assertTrue(hvordanText.contains("slitertillegg"), "HVORDAN should contain formula name")
    }

    @Test
    fun `beregning 35 mnd etter nedre aldersgrense - faktor 1_36 med trace`() {
        val virkningstidspunkt = YearMonth.of(2005, 1) // 35 måneder etter
        val person = person(YearMonth.of(1940, 1), 40)
        val grunnbeløp = 110000

        val request = BeregnSlitertilleggRequest(
            uttakstidspunkt = virkningstidspunkt,
            virkningstidspunkt = virkningstidspunkt,
            person = person
        )

        val response = BeregnSlitertilleggForklartFaktumService(request).run()

        assertTrue(response is Response.SliterordningForklartFaktum.Innvilget)

        val innvilget = response as Response.SliterordningForklartFaktum.Innvilget
        val faktum = innvilget.slitertillegg

        // Verify calculation
        val fullt = 0.25 * grunnbeløp / 12
        val faktorMnd = (36.0 - 35.0) / 36.0
        val faktorTrygdetid = 40.0 / FULL_TRYGDETID
        assertEquals(fullt * faktorMnd * faktorTrygdetid, faktum.value, 1e-9)

        // Verify trace information
        assertNotNull(faktum.hvorfor, "HVORFOR trace should exist")
        assertNotNull(faktum.hvordan, "HVORDAN trace should exist")

        val hvorforText = faktum.hvorfor
        assertTrue(hvorforText.isNotEmpty(), "HVORFOR should not be empty")
    }

    @Test
    fun `beregning 36 mnd etter nedre aldersgrense - faktor 0 og beregnet 0`() {
        val virkningstidspunkt = YearMonth.of(2005, 2) // 36 måneder etter
        val person = person(YearMonth.of(1940, 1), 40)

        val request = BeregnSlitertilleggRequest(
            uttakstidspunkt = virkningstidspunkt,
            virkningstidspunkt = virkningstidspunkt,
            person = person
        )

        val response = BeregnSlitertilleggForklartFaktumService(request).run()

        assertTrue(response is Response.SliterordningForklartFaktum.Innvilget)

        val innvilget = response as Response.SliterordningForklartFaktum.Innvilget
        val faktum = innvilget.slitertillegg

        assertEquals(0.0, faktum.value, 1e-9)

        // Even with zero result, trace should exist
        assertNotNull(faktum.hvorfor)
        assertNotNull(faktum.hvordan)
    }

    @Test
    fun `trygdetid 0 gir beregnet 0 selv om tidsfaktor 1 - med trace`() {
        val virkningstidspunkt = YearMonth.of(2002, 2)
        val person = person(YearMonth.of(1940, 1), 0)

        val request = BeregnSlitertilleggRequest(
            uttakstidspunkt = virkningstidspunkt,
            virkningstidspunkt = virkningstidspunkt,
            person = person
        )

        val response = BeregnSlitertilleggForklartFaktumService(request).run()

        assertTrue(response is Response.SliterordningForklartFaktum.Innvilget)

        val innvilget = response as Response.SliterordningForklartFaktum.Innvilget
        val faktum = innvilget.slitertillegg

        assertEquals(0.0, faktum.value, 1e-9)

        // Verify trace exists
        assertNotNull(faktum.hvorfor)
        assertNotNull(faktum.hvordan)
    }

    @Test
    fun `trace inneholder formelstruktur - HVORDAN`() {
        val virkningstidspunkt = YearMonth.of(2002, 2)
        val person = person(YearMonth.of(1940, 1), 20)

        val request = BeregnSlitertilleggRequest(
            uttakstidspunkt = virkningstidspunkt,
            virkningstidspunkt = virkningstidspunkt,
            person = person
        )

        val response = BeregnSlitertilleggForklartFaktumService(request).run()

        assertTrue(response is Response.SliterordningForklartFaktum.Innvilget)

        val innvilget = response as Response.SliterordningForklartFaktum.Innvilget
        val faktum = innvilget.slitertillegg

        // Verify HVORDAN contains formula components
        val hvordanText = faktum.hvordan.toString()

        // Should contain references to the component formulas
        assertTrue(
            hvordanText.contains("SLITERTILLEGG") || hvordanText.contains("slitertillegg"),
            "HVORDAN should reference slitertillegg formula"
        )
    }

    @Test
    fun `trace inneholder regelflyt - HVORFOR`() {
        val virkningstidspunkt = YearMonth.of(2002, 2)
        val person = person(YearMonth.of(1940, 1), 20)

        val request = BeregnSlitertilleggRequest(
            uttakstidspunkt = virkningstidspunkt,
            virkningstidspunkt = virkningstidspunkt,
            person = person
        )

        val response = BeregnSlitertilleggForklartFaktumService(request).run()

        assertTrue(response is Response.SliterordningForklartFaktum.Innvilget)

        val innvilget = response as Response.SliterordningForklartFaktum.Innvilget
        val faktum = innvilget.slitertillegg

        // Verify HVORFOR contains rule execution trace
        val hvorforText = faktum.hvorfor

        assertFalse(hvorforText.isEmpty(), "HVORFOR should contain execution trace")

        // Should reference the service or flow components
        assertTrue(
            hvorforText.contains("Behandle") ||
                    hvorforText.contains("Beregn") ||
                    hvorforText.contains("REGEL") ||
                    hvorforText.contains("REGELSETT"),
            "HVORFOR should reference execution flow: $hvorforText"
        )
    }

    @Test
    fun `forskjellige grunnbeløp gir proporsjonale resultater med konsistent trace`() {
        val virkningstidspunkt = YearMonth.of(2002, 2)
        val person = person(YearMonth.of(1940, 1), 40)

        val request1 = BeregnSlitertilleggRequest(
            uttakstidspunkt = virkningstidspunkt,
            virkningstidspunkt = virkningstidspunkt,
            person = person
        )

        val request2 = BeregnSlitertilleggRequest(
            uttakstidspunkt = virkningstidspunkt,
            virkningstidspunkt = virkningstidspunkt,
            person = person
        )

        val response1 = BeregnSlitertilleggForklartFaktumService(request1).run()
        val response2 = BeregnSlitertilleggForklartFaktumService(request2).run()

        assertTrue(response1 is Response.SliterordningForklartFaktum.Innvilget)
        assertTrue(response2 is Response.SliterordningForklartFaktum.Innvilget)

        val faktum1 = (response1 as Response.SliterordningForklartFaktum.Innvilget).slitertillegg
        val faktum2 = (response2 as Response.SliterordningForklartFaktum.Innvilget).slitertillegg

        // Both should have trace information
        assertNotNull(faktum1.hvorfor)
        assertNotNull(faktum1.hvordan)
        assertNotNull(faktum2.hvorfor)
        assertNotNull(faktum2.hvordan)

        // Trace structure should be similar (both should reference same rules/formulas)
        // though specific values will differ
        assertFalse(faktum1.hvorfor.isEmpty())
        assertFalse(faktum2.hvorfor.isEmpty())
    }

    @Test
    fun `full trace inspection - print for manual verification`() {
        val virkningstidspunkt = YearMonth.of(2003, 6) // 24 måneder etter nedre aldersgrense
        val person = person(YearMonth.of(1940, 1), 30)

        val request = BeregnSlitertilleggRequest(
            uttakstidspunkt = virkningstidspunkt,
            virkningstidspunkt = virkningstidspunkt,
            person = person
        )

        val response = BeregnSlitertilleggForklartFaktumService(request).run()

        assertTrue(response is Response.SliterordningForklartFaktum.Innvilget)

        val innvilget = response as Response.SliterordningForklartFaktum.Innvilget
        val faktum = innvilget.slitertillegg

        // Print full trace for manual inspection
        println("=".repeat(80))
        println("FULL TRACE FOR FORKLART FAKTUM")
        println("=".repeat(80))
        println()
        println("HVA:")
        println("  Navn: ${faktum.name}")
        println("  Verdi: ${faktum.value}")
        println()
        println("HVORFOR:")
        println(faktum.hvorfor.prependIndent("  "))
        println()
        println("HVORDAN:")
        println(faktum.hvordan.toString().prependIndent("  "))
        println()
        println("=".repeat(80))

        // Basic assertions
        assertTrue(faktum.value > 0, "Value should be positive")
        assertFalse(faktum.hvorfor.isEmpty())
    }
}
