package no.nav.pensjon.sliterordning.regelsett

import no.nav.pensjon.sliterordning.fagdata.FagKonstanter.FULL_TRYGDETID
import no.nav.pensjon.sliterordning.grunnlag.NormertPensjonsalder
import no.nav.pensjon.sliterordning.grunnlag.Person
import no.nav.pensjon.sliterordning.grunnlag.Trygdetid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.YearMonth

/**
 * Test for BeregnSlitertilleggRSUttrykk - demonstrerer integrasjon av Uttrykk i Rule DSL
 *
 * Denne testen viser hvordan faktum() kombinerer:
 * - HVORFOR: Regelflyt-sporing via Trace.kt
 * - HVORDAN: AST-basert beregningsforklaring via Uttrykk.forklarDetaljert()
 */
class BeregnSlitertilleggRSUttrykkTest {

    private fun person(fodselsdato: YearMonth, trygdetidMnd: Int): Person =
        Person(fodselsdato, Trygdetid(trygdetidMnd), NormertPensjonsalder.default())

    @Test
    fun `beregning ved uttak på nedre aldersgrense - full faktor og 50pct trygdetid`() {
        val virkningstidspunkt = YearMonth.of(2002, 2)
        val person = person(YearMonth.of(1940, 1), 20)
        val rs = BeregnSlitertilleggRSUttrykk(
            uttakstidspunkt = virkningstidspunkt,
            virkningstidspunkt = virkningstidspunkt,
            person = person
        )
        val forklartFaktum = rs.test()

        // Verifiser at beregningen er korrekt
        val fullt = 0.25 * 110000 / 12
        val forventet = fullt * 1.0 * (20.0 / FULL_TRYGDETID)
        assertEquals(forventet, forklartFaktum.value, 1e-9)

        // Verifiser at navnet er satt
        assertEquals("slitertillegg", forklartFaktum.name)
    }

    @Test
    fun `demonstrer kombinert HVORFOR og HVORDAN forklaring`() {
        val virkningstidspunkt = YearMonth.of(2002, 2)
        val person = person(YearMonth.of(1940, 1), 20)
        val rs = BeregnSlitertilleggRSUttrykk(
            uttakstidspunkt = virkningstidspunkt,
            virkningstidspunkt = virkningstidspunkt,
            person = person
        )
        val forklartFaktum = rs.test()

        println("\n" + "=".repeat(80))
        println("DEMONSTRASJON AV FAKTUM() INTEGRASJON")
        println("=".repeat(80))

        println("\nHVORFOR (Regelflyt-sporing via Trace.kt):")
        println("-".repeat(80))
        println(forklartFaktum.hvorfor)

        println("\nHVORDAN (AST-basert beregningsforklaring via Uttrykk):")
        println("-".repeat(80))
        println(forklartFaktum.hvordan.toString())

        println("\n" + "=".repeat(80))

        // Verifiser at begge forklaringer er tilstede
        assert(forklartFaktum.hvorfor.isNotEmpty()) { "HVORFOR-forklaring skal være tilstede" }
        assert(forklartFaktum.hvordan.toString().isNotEmpty()) { "HVORDAN-forklaring skal være tilstede" }
    }

    @Test
    fun `beregning 35 mnd etter nedre aldersgrense - faktor 1_36`() {
        val virkningstidspunkt = YearMonth.of(2005, 1) // 35 måneder etter
        val person = person(YearMonth.of(1940, 1), 40)
        val rs = BeregnSlitertilleggRSUttrykk(
            uttakstidspunkt = virkningstidspunkt,
            virkningstidspunkt = virkningstidspunkt,
            person = person
        )
        val forklartFaktum = rs.test()

        val fullt = 0.25 * 110000 / 12
        val faktorMnd = (36.0 - 35.0) / 36.0
        assertEquals(fullt * faktorMnd, forklartFaktum.value, 1e-9)
    }

    @Test
    fun `beregning 36 mnd etter nedre aldersgrense - faktor 0 og beregnet 0`() {
        val virkningstidspunkt = YearMonth.of(2005, 2) // 36 måneder etter
        val person = person(YearMonth.of(1940, 1), 40)
        val rs = BeregnSlitertilleggRSUttrykk(
            uttakstidspunkt = virkningstidspunkt,
            virkningstidspunkt = virkningstidspunkt,
            person = person
        )
        val forklartFaktum = rs.test()

        assertEquals(0.0, forklartFaktum.value, 1e-9)
    }

    @Test
    fun `trygdetid 0 gir beregnet 0 selv om tidsfaktor 1`() {
        val virkningstidspunkt = YearMonth.of(2002, 2)
        val person = person(YearMonth.of(1940, 1), 0)
        val rs = BeregnSlitertilleggRSUttrykk(
            uttakstidspunkt = virkningstidspunkt,
            virkningstidspunkt = virkningstidspunkt,
            person = person
        )
        val forklartFaktum = rs.test()

        assertEquals(0.0, forklartFaktum.value, 1e-9)
    }
}
