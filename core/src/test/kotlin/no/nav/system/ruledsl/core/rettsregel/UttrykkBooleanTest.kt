package no.nav.system.ruledsl.core.model

import no.nav.system.ruledsl.core.operators.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Tester for Boolean-uttrykk, sammenligninger og Hvis-logikk.
 */
class UttrykkBooleanTest {

    // ========================================================================
    // Sammenligninger
    // ========================================================================

    @Test
    fun `erLik skal sammenligne verdier`() {
        val a = Faktum("a", Const(10))
        val b = Faktum("b", Const(10))
        val uttrykk = a erLik b

        assertTrue(uttrykk.verdi)
        assertEquals("JA 'a' er lik 'b'", uttrykk.notasjon())
        assertEquals("JA '10' er lik '10'", uttrykk.konkret())
    }

    @Test
    fun `erUlik skal detektere ulike verdier`() {
        val a = Faktum("a", Const(10))
        val b = Faktum("b", Const(20))
        val uttrykk = a erUlik b

        assertTrue(uttrykk.verdi)
        assertEquals("JA 'a' er ulik 'b'", uttrykk.notasjon())
    }

    @Test
    fun `erStørreEnn skal sammenligne tall`() {
        val a = Faktum("a", Const(20))
        val b = Faktum("b", Const(10))
        val uttrykk = a erStørreEnn b

        assertTrue(uttrykk.verdi)
        assertEquals("JA 'a' er større enn 'b'", uttrykk.notasjon())
    }

    @Test
    fun `erMindreEnn skal sammenligne tall`() {
        val a = Faktum("a", Const(5))
        val b = Faktum("b", Const(10))
        val uttrykk = a erMindreEnn b

        assertTrue(uttrykk.verdi)
        assertEquals("JA 'a' er mindre enn 'b'", uttrykk.notasjon())
    }

    @Test
    fun `erStørreEllerLik skal håndtere lik verdi`() {
        val a = Faktum("a", Const(10))
        val b = Faktum("b", Const(10))
        val uttrykk = a erStørreEllerLik b

        assertTrue(uttrykk.verdi)
        assertEquals("JA 'a' er større eller lik 'b'", uttrykk.notasjon())
    }

    @Test
    fun `erMindreEllerLik skal håndtere lik verdi`() {
        val a = Faktum("a", Const(10))
        val b = Faktum("b", Const(10))
        val uttrykk = a erMindreEllerLik b

        assertTrue(uttrykk.verdi)
        assertEquals("JA 'a' er mindre eller lik 'b'", uttrykk.notasjon())
    }

    @Test
    fun `sammenligning med konstant`() {
        val inntektsavvik = Faktum("inntektsavvik", Const(100))
        val uttrykk = inntektsavvik erMindreEnn 1000

        assertTrue(uttrykk.verdi)
        assertEquals("JA 'inntektsavvik' er mindre enn '1000'", uttrykk.notasjon())
    }
}
