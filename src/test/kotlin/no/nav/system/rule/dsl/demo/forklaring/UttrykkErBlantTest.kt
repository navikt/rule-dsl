package no.nav.system.rule.dsl.demo.forklaring

import no.nav.system.rule.dsl.demo.domain.koder.UnntakEnum.*
import no.nav.system.rule.dsl.rettsregel.Const
import no.nav.system.rule.dsl.rettsregel.Faktum
import no.nav.system.rule.dsl.rettsregel.operators.erBlant
import no.nav.system.rule.dsl.rettsregel.operators.erIkkeBlant
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Test for erBlant og erIkkeBlant operatorer i Uttrykk.
 */
class UttrykkErBlantTest {

    @Test
    fun `erBlant med Const verdi og Faktum liste`() {
        val aktuelleUnntakstyper = Faktum("aktuelleUnntakstyper",
            listOf(FLYKT_ALDER, FLYKT_BARNEP, FLYKT_GJENLEV, FLYKT_UFOREP)
        )

        val unntakType = FLYKT_ALDER
        val uttrykk = Const(unntakType) erBlant aktuelleUnntakstyper

        // Test evaluering
        assertTrue(uttrykk.verdi)

        // Test notasjon
        assertEquals("JA 'FLYKT_ALDER' er blandt 'aktuelleUnntakstyper'", uttrykk.notasjon())

        // Test konkret
        assertEquals("JA 'FLYKT_ALDER' er blandt '[FLYKT_ALDER, FLYKT_BARNEP, FLYKT_GJENLEV, FLYKT_UFOREP]'", uttrykk.konkret())
    }

    @Test
    fun `erBlant med verdi som ikke finnes i listen`() {
        val aktuelleUnntakstyper = Faktum(
            "aktuelleUnntakstyper",
            Const(listOf(FLYKT_ALDER, FLYKT_BARNEP))
        )

        val unntakType = FLYKT_GJENLEV
        val uttrykk = Const(unntakType) erBlant aktuelleUnntakstyper

        // Test evaluering
        assertFalse(uttrykk.verdi)
    }

    @Test
    fun `erIkkeBlant med Const verdi og Faktum liste`() {
        val aktuelleUnntakstyper = Faktum(
            "aktuelleUnntakstyper",
            Const(listOf(FLYKT_ALDER, FLYKT_BARNEP))
        )

        val unntakType = FLYKT_GJENLEV
        val uttrykk = Const(unntakType) erIkkeBlant aktuelleUnntakstyper

        // Test evaluering
        assertTrue(uttrykk.verdi)

        // Test notasjon
        assertEquals("JA 'FLYKT_GJENLEV' er ikke blandt 'aktuelleUnntakstyper'", uttrykk.notasjon())
    }

    @Test
    fun `erBlant med Faktum verdi og Const liste`() {
        val unntakType = Faktum("unntakType", Const(FLYKT_ALDER))
        val uttrykk = unntakType erBlant listOf(FLYKT_ALDER, FLYKT_BARNEP, FLYKT_GJENLEV, FLYKT_UFOREP)

        // Test evaluering
        assertTrue(uttrykk.verdi)

        // Test notasjon
        assertEquals("JA 'unntakType' er blandt '[FLYKT_ALDER, FLYKT_BARNEP, FLYKT_GJENLEV, FLYKT_UFOREP]'", uttrykk.notasjon())

        // Test konkret
        assertEquals("JA 'FLYKT_ALDER' er blandt '[FLYKT_ALDER, FLYKT_BARNEP, FLYKT_GJENLEV, FLYKT_UFOREP]'", uttrykk.konkret())
    }

    @Test
    fun `erBlant med heltall`() {
        val tall = Faktum("tall", Const(5))
        val uttrykk = tall erBlant listOf(1, 2, 3, 4, 5)

        // Test evaluering
        assertTrue(uttrykk.verdi)

        // Test notasjon
        assertEquals("JA 'tall' er blandt '[1, 2, 3, 4, 5]'", uttrykk.notasjon())

        // Test konkret
        assertEquals("JA '5' er blandt '[1, 2, 3, 4, 5]'", uttrykk.konkret())
    }

    @Test
    fun `erIkkeBlant med heltall`() {
        val tall = Faktum("tall", Const(10))
        val uttrykk = tall erIkkeBlant listOf(1, 2, 3, 4, 5)

        // Test evaluering
        assertTrue(uttrykk.verdi)

        // Test notasjon
        assertEquals("JA 'tall' er ikke blandt '[1, 2, 3, 4, 5]'", uttrykk.notasjon())

        // Test konkret
        assertEquals("JA '10' er ikke blandt '[1, 2, 3, 4, 5]'", uttrykk.konkret())
    }

    @Test
    fun `erBlant med strenger`() {
        val tekst = Faktum("tekst", Const("hei"))
        val uttrykk = tekst erBlant listOf("hei", "ha", "hallo")

        // Test evaluering
        assertTrue(uttrykk.verdi)

        // Test notasjon
        assertEquals("JA 'tekst' er blandt '[hei, ha, hallo]'", uttrykk.notasjon())
    }

}
