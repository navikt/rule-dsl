package no.nav.system.rule.dsl.demo.forklaring

import no.nav.system.rule.dsl.demo.domain.koder.UnntakEnum
import no.nav.system.rule.dsl.demo.domain.koder.UnntakEnum.*
import no.nav.system.rule.dsl.forklaring.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Test for erBlant og erIkkeBlant operatorer i Uttrykk.
 */
class UttrykkErBlantTest {

    @Test
    fun `erBlant med Const verdi og Grunnlag liste`() {
        val aktuelleUnntakstyper = Grunnlag(
            "aktuelleUnntakstyper",
            Const(listOf(FLYKT_ALDER, FLYKT_BARNEP, FLYKT_GJENLEV, FLYKT_UFOREP))
        )

        val unntakType = FLYKT_ALDER
        val uttrykk = Const(unntakType) erBlant aktuelleUnntakstyper

        // Test evaluering
        assertTrue(uttrykk.evaluer())

        // Test notasjon
        assertEquals("FLYKT_ALDER ER BLANT aktuelleUnntakstyper", uttrykk.notasjon())

        // Test konkret
        assertEquals("FLYKT_ALDER ER BLANT [FLYKT_ALDER, FLYKT_BARNEP, FLYKT_GJENLEV, FLYKT_UFOREP]", uttrykk.konkret())
    }

    @Test
    fun `erBlant med verdi som ikke finnes i listen`() {
        val aktuelleUnntakstyper = Grunnlag(
            "aktuelleUnntakstyper",
            Const(listOf(FLYKT_ALDER, FLYKT_BARNEP))
        )

        val unntakType = FLYKT_GJENLEV
        val uttrykk = Const(unntakType) erBlant aktuelleUnntakstyper

        // Test evaluering
        assertFalse(uttrykk.evaluer())
    }

    @Test
    fun `erIkkeBlant med Const verdi og Grunnlag liste`() {
        val aktuelleUnntakstyper = Grunnlag(
            "aktuelleUnntakstyper",
            Const(listOf(FLYKT_ALDER, FLYKT_BARNEP))
        )

        val unntakType = FLYKT_GJENLEV
        val uttrykk = Const(unntakType) erIkkeBlant aktuelleUnntakstyper

        // Test evaluering
        assertTrue(uttrykk.evaluer())

        // Test notasjon
        assertEquals("FLYKT_GJENLEV ER IKKE BLANT aktuelleUnntakstyper", uttrykk.notasjon())
    }

    @Test
    fun `erBlant med Grunnlag verdi og Const liste`() {
        val unntakType = Grunnlag("unntakType", Const(FLYKT_ALDER))
        val uttrykk = unntakType erBlant listOf(FLYKT_ALDER, FLYKT_BARNEP, FLYKT_GJENLEV, FLYKT_UFOREP)

        // Test evaluering
        assertTrue(uttrykk.evaluer())

        // Test notasjon
        assertEquals("unntakType ER BLANT [FLYKT_ALDER, FLYKT_BARNEP, FLYKT_GJENLEV, FLYKT_UFOREP]", uttrykk.notasjon())

        // Test konkret
        assertEquals("FLYKT_ALDER ER BLANT [FLYKT_ALDER, FLYKT_BARNEP, FLYKT_GJENLEV, FLYKT_UFOREP]", uttrykk.konkret())
    }

    @Test
    fun `erBlant med heltall`() {
        val tall = Grunnlag("tall", Const(5))
        val uttrykk = tall erBlant listOf(1, 2, 3, 4, 5)

        // Test evaluering
        assertTrue(uttrykk.evaluer())

        // Test notasjon
        assertEquals("tall ER BLANT [1, 2, 3, 4, 5]", uttrykk.notasjon())

        // Test konkret
        assertEquals("5 ER BLANT [1, 2, 3, 4, 5]", uttrykk.konkret())
    }

    @Test
    fun `erIkkeBlant med heltall`() {
        val tall = Grunnlag("tall", Const(10))
        val uttrykk = tall erIkkeBlant listOf(1, 2, 3, 4, 5)

        // Test evaluering
        assertTrue(uttrykk.evaluer())

        // Test notasjon
        assertEquals("tall ER IKKE BLANT [1, 2, 3, 4, 5]", uttrykk.notasjon())

        // Test konkret
        assertEquals("10 ER IKKE BLANT [1, 2, 3, 4, 5]", uttrykk.konkret())
    }

    @Test
    fun `erBlant med strenger`() {
        val tekst = Grunnlag("tekst", Const("hei"))
        val uttrykk = tekst erBlant listOf("hei", "ha", "hallo")

        // Test evaluering
        assertTrue(uttrykk.evaluer())

        // Test notasjon
        assertEquals("tekst ER BLANT [hei, ha, hallo]", uttrykk.notasjon())
    }

    @Test
    fun `erBlant i betinget uttrykk`() {
        val unntakType = Grunnlag("unntakType", Const(FLYKT_ALDER))
        val aktuelleUnntakstyper = Grunnlag(
            "aktuelleUnntakstyper",
            Const(listOf(FLYKT_ALDER, FLYKT_BARNEP))
        )

        val resultat = (unntakType erBlant aktuelleUnntakstyper)
            .så { Const("GODKJENT") }
            .ellers { Const("AVVIST") }

        // Test evaluering
        assertEquals("GODKJENT", resultat.evaluer())

        // Test notasjon
        val notasjon = resultat.notasjon()
        assertTrue(notasjon.contains("ER BLANT"))
        assertTrue(notasjon.contains("GODKJENT"))
        assertTrue(notasjon.contains("AVVIST"))
    }

    @Test
    fun `erBlant i logisk OG uttrykk`() {
        val unntakType = Grunnlag("unntakType", Const(FLYKT_ALDER))
        val aktuelleUnntakstyper = Grunnlag(
            "aktuelleUnntakstyper",
            Const(listOf(FLYKT_ALDER, FLYKT_BARNEP))
        )
        val harUnntak = Grunnlag("harUnntak", Const(true))

        val uttrykk = harUnntak og (unntakType erBlant aktuelleUnntakstyper)

        // Test evaluering
        assertTrue(uttrykk.evaluer())
    }

    @Test
    fun `forklar med erBlant`() {
        val unntakType = Grunnlag("unntakType", Const(FLYKT_ALDER))
        val aktuelleUnntakstyper = Grunnlag(
            "aktuelleUnntakstyper",
            Const(listOf(FLYKT_ALDER, FLYKT_BARNEP, FLYKT_GJENLEV, FLYKT_UFOREP))
        )

        val uttrykk = unntakType erBlant aktuelleUnntakstyper

        val forklaring = uttrykk.forklar("erFlyktning")

        // Verifiser at forklaringen inneholder nødvendig informasjon
        assertNotNull(forklaring)
        assertTrue(forklaring.hvaForklaring.symbolskUttrykk.contains("ER BLANT"))
        assertEquals(true, forklaring.hvaForklaring.resultat)
    }
}
