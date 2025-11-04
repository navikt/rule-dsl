package no.nav.pensjon.sliterordning.regelsett

import no.nav.pensjon.sliterordning.grunnlag.NormertPensjonsalder
import no.nav.pensjon.sliterordning.grunnlag.Person
import no.nav.pensjon.sliterordning.grunnlag.Trygdetid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.YearMonth

/**
 * Test for BeregnSlitertilleggRSFormel - demonstrerer regelflyt-forklaring uten AST-detaljer.
 *
 * Denne testen viser tradisjonell tilnærming:
 * - HVORFOR: Regelflyt-sporing via medForklaring()
 * - Ingen HVORDAN: Ingen automatisk beregningsforklaring
 *
 * Sammenlign med BeregnSlitertilleggRSUttrykkTest for å se forskjellen.
 */
class BeregnSlitertilleggRSFormelTest {

    private fun person(fodselsdato: YearMonth, trygdetidMnd: Int): Person =
        Person(fodselsdato, Trygdetid(trygdetidMnd), NormertPensjonsalder.default())

    @Test
    fun `beregning ved uttak på nedre aldersgrense - full faktor og 50pct trygdetid`() {
        val virkningstidspunkt = YearMonth.of(2002, 2)
        val person = person(YearMonth.of(1940, 1), 20)
        val rs = BeregnSlitertilleggRSFormel(
            uttakstidspunkt = virkningstidspunkt,
            virkningstidspunkt = virkningstidspunkt,
            person = person
        )
        val resultat = rs.test()

        // Verifiser at beregningen er korrekt
        val fullt = 0.25 * 110000 / 12
        val forventet = fullt * 1.0 * (20.0 / 40)
        assertEquals(forventet, resultat, 1e-9)
    }

    @Test
    fun `demonstrer regelflyt-forklaring uten beregningsdetaljer`() {
        val virkningstidspunkt = YearMonth.of(2002, 2)
        val person = person(YearMonth.of(1940, 1), 20)
        val rs = BeregnSlitertilleggRSFormel(
            uttakstidspunkt = virkningstidspunkt,
            virkningstidspunkt = virkningstidspunkt,
            person = person
        )
        rs.test()

        // Hent forklaring via medForklaring() fra AbstractRuleset
        val forklartResultat = rs.medForklaring()

        println("\n" + "=".repeat(80))
        println("DEMONSTRASJON: Formel-approach (kun HVORFOR)")
        println("=".repeat(80))
        println(forklartResultat.forklaring())
        println("=".repeat(80))

        // Verifiser at forklaring finnes
        assert(forklartResultat.hvorfor.isNotEmpty()) { "HVORFOR-forklaring skal være tilstede" }

        // Legg merke til: Vi har IKKE automatisk HVORDAN-forklaring her
        // For å få AST-basert beregningsforklaring, bruk Uttrykk-varianten
    }

    @Test
    fun `sammenlign med Uttrykk-variant`() {
        val virkningstidspunkt = YearMonth.of(2002, 2)
        val person = person(YearMonth.of(1940, 1), 20)

        // Formel-variant: Kun regelflyt-forklaring
        val rsFormel = BeregnSlitertilleggRSFormel(
            uttakstidspunkt = virkningstidspunkt,
            virkningstidspunkt = virkningstidspunkt,
            person = person
        )
        rsFormel.test()
        val formelForklaring = rsFormel.medForklaring()

        // Uttrykk-variant: Regelflyt + AST-forklaring
        val rsUttrykk = BeregnSlitertilleggRSUttrykk(
            uttakstidspunkt = virkningstidspunkt,
            virkningstidspunkt = virkningstidspunkt,
            person = person
        )
        val uttrykkForklaring = rsUttrykk.test()

        println("\n" + "=".repeat(80))
        println("SAMMENLIGNING: Formel vs Uttrykk")
        println("=".repeat(80))

        println("\n--- FORMEL-VARIANT: Kun HVORFOR ---")
        println(formelForklaring.forklaring())

        println("\n--- UTTRYKK-VARIANT: HVORFOR + HVORDAN ---")
        println("HVA")
        println("    ${uttrykkForklaring.name} = ${uttrykkForklaring.value}")
        println()
        println("HVORFOR (Regelflyt-sporing):")
        println(uttrykkForklaring.hvorfor.prependIndent("    "))
        println()
        println("HVORDAN (AST-basert beregningsforklaring):")
        println(uttrykkForklaring.hvordan.toString().prependIndent("    "))

        println("\n" + "=".repeat(80))

        // Begge gir samme resultat
        assertEquals(formelForklaring.value, uttrykkForklaring.value, 1e-9)
    }

    @Test
    fun `beregning 36 mnd etter nedre aldersgrense - faktor 0 og beregnet 0`() {
        val virkningstidspunkt = YearMonth.of(2005, 2) // 36 måneder etter
        val person = person(YearMonth.of(1940, 1), 40)
        val rs = BeregnSlitertilleggRSFormel(
            uttakstidspunkt = virkningstidspunkt,
            virkningstidspunkt = virkningstidspunkt,
            person = person
        )
        val resultat = rs.test()

        assertEquals(0.0, resultat, 1e-9)
    }
}
