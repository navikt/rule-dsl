package no.nav.system.ruledsl.core.model

import no.nav.system.ruledsl.core.operators.erBlant
import no.nav.system.ruledsl.core.operators.erIkkeBlant
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Test for erBlant og erIkkeBlant operatorer i Uttrykk.
 */
class UttrykkErBlantTest {

    enum class UnntakEnum {
        FEM, SEKS, SYV;
    }


    @Test
    fun `erBlant med Const verdi og Faktum liste`() {
        val aktuelleTyper = Faktum(
            "aktuelleTyper",
            listOf(UnntakEnum.FEM, UnntakEnum.SEKS)
        )

        val type = UnntakEnum.FEM
        val uttrykk = Const(type) erBlant aktuelleTyper

        // Test evaluering
        assertTrue(uttrykk.verdi)

        // Test notasjon
        assertEquals("JA 'FEM' er blandt 'aktuelleTyper'", uttrykk.notasjon())

        // Test konkret
        assertEquals("JA 'FEM' er blandt '[FEM, SEKS]'", uttrykk.konkret())
    }

    @Test
    fun `erBlant med verdi som ikke finnes i listen`() {
        val aktuelleTyper = Faktum(
            "aktuelleTyper",
            Const(listOf(UnntakEnum.FEM, UnntakEnum.SEKS))
        )

        val unntakType = UnntakEnum.SYV
        val uttrykk = Const(unntakType) erBlant aktuelleTyper

        // Test evaluering
        assertFalse(uttrykk.verdi)
    }

    @Test
    fun `erIkkeBlant med Const verdi og Faktum liste`() {
        val aktuelleTyper = Faktum(
            "aktuelleTyper",
            Const(listOf(UnntakEnum.FEM, UnntakEnum.SEKS))
        )

        val type = UnntakEnum.SYV
        val uttrykk = Const(type) erIkkeBlant aktuelleTyper

        // Test evaluering
        assertTrue(uttrykk.verdi)

        // Test notasjon
        assertEquals("JA 'SYV' er ikke blandt 'aktuelleTyper'", uttrykk.notasjon())
    }

    @Test
    fun `erBlant med Faktum verdi og Const liste`() {
        val type = Faktum("type", Const(UnntakEnum.FEM))
        val uttrykk = type erBlant listOf(UnntakEnum.FEM, UnntakEnum.SEKS, UnntakEnum.SYV)

        // Test evaluering
        assertTrue(uttrykk.verdi)

        // Test notasjon
        assertEquals("JA 'type' er blandt '[FEM, SEKS, SYV]'", uttrykk.notasjon())

        // Test konkret
        assertEquals("JA 'FEM' er blandt '[FEM, SEKS, SYV]'", uttrykk.konkret())
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
