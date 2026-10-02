package no.nav.system.rdsl2

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotSame
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.time.LocalDate

class UttrykkTest {

    private val a = Faktum("a", 6)
    private val b = Faktum("b", 2)

    @Test
    fun `notasjon og konkret setter parenteser etter presedens`() {
        val u = (10 - a) / b * 3
        assertEquals("(10 - a) / b * 3", u.notasjon())
        assertEquals("(10 - 6) / 2 * 3", u.konkret())
        assertEquals(6.0, u.verdi)

        assertEquals("a / (b / 4)", (a / (b / 4)).notasjon())
    }

    @Test
    fun `faktum er en grense i notasjonen og i grunnlaget`() {
        val sum = regel("R") { SÅ { } }.let { r -> Faktum("sum", a * b, r) }
        val dobbel = a * sum

        assertEquals("a * sum", dobbel.notasjon())
        assertEquals("6 * 12.0", dobbel.konkret())
        assertEquals(listOf(a, sum), dobbel.grunnlag())
        assertEquals(listOf(a, b), sum.uttrykk.grunnlag())
    }

    @Test
    fun `faktum inne i SÅ faar regelen som eier blokken`() {
        lateinit var resultat: Faktum<Double>
        val r = regel("R") {
            HVIS { a erStørreEllerLik 5 }
            OG { b erMindreEnn 3 }
            SÅ { resultat = faktum("resultat", a * b) }
        }

        assertTrue(r.verdi)
        assertSame(r, resultat.regel)
        assertEquals("a ≥ 5 og b < 3", r.uttrykk.notasjon())
        assertEquals("6 ≥ 5 og 2 < 3", r.uttrykk.konkret())
        assertNull(r.regel)
    }

    @Test
    fun `alle betingelser evalueres og SÅ kjøres ikke naar regelen ikke treffer`() {
        var kjørt = false
        val r = regel("R") {
            HVIS { a erMindreEnn 5 }
            OG { b erMindreEnn 3 }
            SÅ { kjørt = true }
        }

        assertFalse(r.verdi)
        assertFalse(kjørt)
        assertEquals("6 < 5 og 2 < 3", r.uttrykk.konkret())
    }

    @Test
    fun `regel kan brukes som betingelse i en annen regel`() {
        val nok = regel("NOK") { HVIS { a erStørreEllerLik 5 } }
        val innvilget = regel("INNVILGET") { HVIS { nok } }

        assertTrue(innvilget.verdi)
        assertEquals(listOf(nok), innvilget.uttrykk.grunnlag())
    }

    @Test
    fun `regel i SÅ-blokk er betinget av regelen som eier blokken`() {
        lateinit var indre: Faktum<Boolean>
        val ytre = regel("YTRE") {
            SÅ { indre = regel("INDRE") { HVIS { a erStørreEllerLik 1 } } }
        }
        assertSame(ytre, indre.regel)
    }

    @Test
    fun `likhet, stoerre enn og negasjon`() {
        val r = regel("R") {
            HVIS { a erStørreEnn b }
            OG { Faktum("tekst", "x") erLik "x" }
            OG { !(a erLik b) }
        }
        assertTrue(r.verdi)
        assertEquals("a > b og tekst = x og ikke(a = b)", r.uttrykk.notasjon())
        assertEquals("6 > 2 og x = x og ikke(6 = 2)", r.uttrykk.konkret())
    }

    @Test
    fun `sammenligning mot ren verdi`() {
        val r = regel("R") { HVIS { Faktum("dato", LocalDate.of(2020, 3, 1)) erStørreEnn LocalDate.of(2020, 2, 1) } }
        assertTrue(r.verdi)
        assertEquals("dato > 2020-02-01", r.uttrykk.notasjon())
    }

    @Test
    fun `guard spores ikke og stopper evalueringen`() {
        val mangler: Faktum<Int>? = null
        val r = regel("R") {
            HVIS { mangler != null }
            OG { mangler!! erStørreEnn 1 }
        }
        assertFalse(r.verdi)

        val s = regel("S") {
            HVIS { true }
            OG { a erStørreEnn b }
        }
        assertTrue(s.verdi)
        assertEquals("a > b", s.uttrykk.notasjon())
    }

    @Test
    fun `like utregninger er ulike faktum`() {
        val f = List(2) { regel("R") { }.let { r -> Faktum("x", a * b, r) } }
        assertNotSame(f[0], f[1])
        assertEquals(2, f.distinct().size)
    }

    @Test
    fun `faktum kan serialiseres med forklaringen intakt`() {
        lateinit var resultat: Faktum<Double>
        regel("R") {
            HVIS { a erStørreEllerLik 5 }
            SÅ { resultat = faktum("resultat", avrund(a / b)) }
        }

        val kopi = ObjectInputStream(ByteArrayInputStream(
            ByteArrayOutputStream().also { ObjectOutputStream(it).writeObject(resultat) }.toByteArray()
        )).readObject() as Faktum<*>

        assertEquals(resultat.forklar(), kopi.forklar())
    }

    private fun avrund(x: Uttrykk<Number>) = funksjon("avrund", x) { Math.round(x.verdi.toDouble()).toDouble() }
}
