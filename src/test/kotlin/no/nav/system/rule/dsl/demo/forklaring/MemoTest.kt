package no.nav.system.rule.dsl.demo.forklaring

import no.nav.system.rule.dsl.forklaring.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Tester for Memo-funksjonalitet (memoisering/caching av uttrykk).
 *
 * Siden Uttrykk er et sealed interface kan vi ikke lage custom test-klasser utenfor pakken.
 * Disse testene verifiserer at Memo fungerer korrekt med eksisterende uttrykkstyper.
 */
class MemoTest {

    @Test
    fun `Memo evaluerer korrekt med aritmetiske uttrykk`() {
        val a = Const(10)
        val b = Const(20)
        val c = Const(5)

        // Kompleks uttrykk: (a + b) * c = 150
        val kompleks = Mul<Int>(Add(a, b), c)
        val memo = Memo(kompleks)

        // Verifiser korrekt evaluering
        assertEquals(150, memo.evaluer())

        // Verifiser at gjentatte kall gir samme resultat
        assertEquals(150, memo.evaluer())
        assertEquals(150, memo.evaluer())
    }

    @Test
    fun `Memo cacher notasjon()`() {
        val a = Const(10)
        val b = Const(20)
        val sum = Add<Int>(a, b)
        val memo = Memo(sum)

        val n1 = memo.notasjon()
        val n2 = memo.notasjon()

        assertEquals("10 + 20", n1)
        assertEquals(n1, n2)
    }

    @Test
    fun `Memo cacher konkret()`() {
        val a = Const(10)
        val b = Const(20)
        val sum = Add<Int>(a, b)
        val memo = Memo(sum)

        val k1 = memo.konkret()
        val k2 = memo.konkret()

        assertEquals("10 + 20", k1)
        assertEquals(k1, k2)
    }

    @Test
    fun `Memo cacher grunnlagListe()`() {
        val a = Grunnlag("a", Const(10))
        val b = Grunnlag("b", Const(20))
        val sum = Add<Int>(a, b)
        val memo = Memo(sum)

        val grunnlag1 = memo.grunnlagListe()
        val grunnlag2 = memo.grunnlagListe()

        assertEquals(2, grunnlag1.size)
        assertEquals(2, grunnlag2.size)
        assertSame(grunnlag1, grunnlag2, "GrunnlagListe skal være cachet")
    }

    @Test
    fun `memoise() extension function wrapperer uttrykk`() {
        val a = Const(42)
        val memo = a.memoise()

        assertTrue(memo is Memo)
        assertEquals(42, memo.evaluer())
    }

    @Test
    fun `memoise() på allerede memoisert uttrykk returnerer samme Memo`() {
        val a = Const(42)
        val memo1 = a.memoise()
        val memo2 = memo1.memoise()

        assertSame(memo1, memo2, "Skal ikke double-wrappe Memo")
    }

    @Test
    fun `navngi() med memoise=true (default) legger til memoisering for komplekse uttrykk`() {
        val a = Const(10)
        val b = Const(20)
        val sum = Add<Int>(a, b)

        val grunnlag = sum.navngi("test", memoise = true)

        // Verifiser at grunnlag inneholder Memo
        assertTrue(grunnlag.utpakk() is Memo, "Skal være memoisert via navngi()")
        assertEquals(30, grunnlag.evaluer())
    }

    @Test
    fun `navngi() med memoise=false legger IKKE til memoisering`() {
        val a = Const(10)
        val b = Const(20)
        val sum = Add<Int>(a, b)

        val grunnlag = sum.navngi("test", memoise = false)

        // Verifiser at grunnlag IKKE inneholder Memo
        assertFalse(grunnlag.utpakk() is Memo, "Skal IKKE være memoisert")
        assertTrue(grunnlag.utpakk() is Add, "Skal være Add direkte")
        assertEquals(30, grunnlag.evaluer())
    }

    @Test
    fun `navngi() på Const legger IKKE til unødvendig memoisering`() {
        val konstant = Const(42)
        val grunnlag = konstant.navngi("test")

        // Sjekk at Grunnlag ikke inneholder Memo for konstanter
        assertFalse(grunnlag.utpakk() is Memo, "Const trenger ikke memoisering")
        assertTrue(grunnlag.utpakk() is Const)
        assertEquals(42, grunnlag.evaluer())
    }

    @Test
    fun `Memo i tabell fungerer med Boolean-uttrykk`() {
        val a = Const(true)
        val b = Const(false)

        val aOgB = Og(a, b)
        val memoisert = aOgB.memoise()

        // Bruk samme memoiserte uttrykk i flere regler
        val tabell = tabell<String> {
            regel {
                når { ikke(memoisert) }
                resultat { Const("REGEL 1") }
            }
            regel {
                når { memoisert }
                resultat { Const("REGEL 2") }
            }
        }

        assertEquals("REGEL 1", tabell.evaluer())
    }

    @Test
    fun `Memo utpakk() returnerer underliggende uttrykk`() {
        val a = Const(42)
        val memo = Memo(a)

        assertSame(a, memo.utpakk())
    }

    @Test
    fun `Memo toString() viser wrapping`() {
        val a = Const(42)
        val memo = Memo(a)

        val str = memo.toString()
        assertTrue(str.contains("Memo"), "toString skal vise Memo")
        assertTrue(str.contains("42"), "toString skal vise innhold")
    }

    @Test
    fun `Memo fungerer med komplekse Boolean-uttrykk`() {
        val a = Const(true)
        val b = Const(false)

        val aMemo = a.memoise()
        val bMemo = b.memoise()

        // Bruk memoiserte uttrykk i flere steder
        val uttrykk1 = Og(aMemo, bMemo)
        val uttrykk2 = Eller(aMemo, bMemo)

        assertEquals(false, uttrykk1.evaluer())
        assertEquals(true, uttrykk2.evaluer())
    }

    @Test
    fun `Memo med Grunnlag fungerer korrekt`() {
        val a = Grunnlag("a", Const(10))
        val b = Grunnlag("b", Const(20))
        val sum = Add<Int>(a, b)

        val memo = Memo(sum)

        assertEquals(30, memo.evaluer())
        assertEquals("a + b", memo.notasjon())
        assertEquals("10 + 20", memo.konkret())
    }

    @Test
    fun `Memo i nøstede uttrykk`() {
        val a = Const(5)
        val b = Const(3)
        val c = Const(2)

        val aPlusB = Add<Int>(a, b).memoise()
        val resultat = Mul<Int>(aPlusB, c)

        // (5 + 3) * 2 = 16
        assertEquals(16, resultat.evaluer())

        // Gjenta evaluering
        assertEquals(16, resultat.evaluer())
    }

    @Test
    fun `Memo med divisjon`() {
        val a = Const(100)
        val b = Const(4)

        val divisjon = Div(a, b).memoise()

        assertEquals(25.0, divisjon.evaluer())
        assertEquals(25.0, divisjon.evaluer())
    }
}
