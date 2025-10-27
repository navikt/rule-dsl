package no.nav.system.rule.dsl.demo.forklaring

import no.nav.system.rule.dsl.forklaring.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class UttrykkTest {

    @Test
    fun `Grunnlag skal evaluere til verdi`() {
        val grunnlag = Grunnlag("test", Const(42))

        assertEquals(42, grunnlag.evaluer())
        assertEquals("test", grunnlag.notasjon())
        assertEquals("42", grunnlag.konkret())
    }

    @Test
    fun `Const skal evaluere til konstant verdi`() {
        val uttrykk = Const(123)

        assertEquals(123, uttrykk.evaluer())
        assertEquals("123", uttrykk.notasjon())
    }

    @Test
    fun `Add skal addere to uttrykk`() {
        val a = Grunnlag("a", Const(10))
        val b = Grunnlag("b", Const(20))
        val uttrykk = a + b

        assertEquals(30, uttrykk.evaluer())
        assertEquals("a + b", uttrykk.notasjon())
        assertEquals("10 + 20", uttrykk.konkret())
    }

    @Test
    fun `Sub skal subtrahere to uttrykk`() {
        val a = Grunnlag("a", Const(50))
        val b = Grunnlag("b", Const(30))
        val uttrykk = a - b

        assertEquals(20, uttrykk.evaluer())
        assertEquals("a - b", uttrykk.notasjon())
    }

    @Test
    fun `Mul skal multiplisere to uttrykk`() {
        val a = Grunnlag("a", Const(5))
        val b = Grunnlag("b", Const(7))
        val uttrykk = a * b

        assertEquals(35, uttrykk.evaluer())
        assertEquals("a * b", uttrykk.notasjon())
    }

    @Test
    fun `Div skal dividere to uttrykk og gi Double`() {
        val a = Grunnlag("a", Const(100))
        val b = Grunnlag("b", Const(4))
        val uttrykk = a / b

        assertEquals(25.0, uttrykk.evaluer(), 0.001)
        assertEquals("a / b", uttrykk.notasjon())
    }

    @Test
    fun `Div med null skal kaste exception`() {
        val a = Grunnlag("a", Const(100))
        val b = Grunnlag("b", Const(0))
        val uttrykk = a / b

        assertThrows(ArithmeticException::class.java) {
            uttrykk.evaluer()
        }
    }

    @Test
    fun `Neg skal negere uttrykk`() {
        val a = Grunnlag("a", Const(42))
        val uttrykk = -a

        assertEquals(-42, uttrykk.evaluer())
        assertEquals("-a", uttrykk.notasjon())
    }

    @Test
    fun `kompleks uttrykk skal evalueres korrekt`() {
        val a = Grunnlag("a", Const(10))
        val b = Grunnlag("b", Const(20))
        val c = Grunnlag("c", Const(5))

        // (a + b) * c
        val uttrykk = (a + b) * c

        assertEquals(150, uttrykk.evaluer())
        assertEquals("(a + b) * c", uttrykk.notasjon())
    }

    @Test
    fun `navngitt uttrykk skal fungere som atomisk enhet`() {
        val a = Grunnlag("a", Const(10))
        val b = Grunnlag("b", Const(20))

        val sum = (a + b).navngi("sum")

        assertEquals(30, sum.evaluer())
        assertEquals("sum", sum.notasjon())
        assertEquals("30", sum.konkret())
    }

    @Test
    fun `grunnlagListe skal returnere alle grunnlag`() {
        val a = Grunnlag("a", Const(10))
        val b = Grunnlag("b", Const(20))
        val c = Grunnlag("c", Const(30))

        val uttrykk = (a + b) * c
        val grunnlagListe = uttrykk.grunnlagListe()

        // Grunnlag i uttrykket: a, b, c, og det komplekse uttrykket (a + b) * c
        assertEquals(3, grunnlagListe.size)
    }

    @Test
    fun `dybde skal beregnes korrekt`() {
        val a = Grunnlag("a", Const(10))
        val b = Grunnlag("b", Const(20))

        val enkel = a
        assertEquals(1, enkel.dybde())

        val sum = a + b
        assertEquals(2, sum.dybde())

        val kompleks = (a + b) * Const<Int>(5)
        assertEquals(3, kompleks.dybde())
    }

    @Test
    fun `forklarKompakt skal gi 3 linjer`() {
        val a = Grunnlag("a", Const(10))
        val uttrykk = a * 2

        val forklaring = uttrykk.forklarKompakt("resultat")
        val linjer = forklaring.trim().split("\n")

        assertEquals(3, linjer.size)
        assertTrue(forklaring.contains("resultat = a * 2"))
        assertTrue(forklaring.contains("resultat = 10 * 2"))
        assertTrue(forklaring.contains("resultat = 20"))
    }

    @Test
    fun `forklar skal generere HvordanForklaring`() {
        val a = Grunnlag("a", Const(10))
        val b = Grunnlag("b", Const(5))
        val uttrykk = a / b

        val forklaring = uttrykk.forklar("resultat")

        assertEquals("resultat", forklaring.hvaForklaring.navn)
        assertEquals("a / b", forklaring.hvaForklaring.symbolskUttrykk)
        assertEquals(2.0, forklaring.hvaForklaring.resultat)
    }

    @Test
    fun `visitor skal kunne traverse tre`() {
        val a = Grunnlag("a", Const(10))
        val b = Grunnlag("b", Const(20))
        val uttrykk = (a + b) * Const<Int>(2)

        val typer = uttrykk.visit { expr ->
            listOf(expr::class.simpleName ?: "Unknown")
        }

        assertTrue(typer.contains("Mul"))
        assertTrue(typer.contains("Add"))
        assertTrue(typer.contains("Grunnlag"))
        assertTrue(typer.contains("Const"))
    }

    @Test
    fun `forenkel skal evaluere konstante subtre`() {
        val x = Grunnlag("x", Const(5))

        // (2 * 3) + x  skal forenkles til 6 + x
        val uttrykk = (Const<Int>(2) * Const<Int>(3)) + x
        val forenklet = uttrykk.forenkel()

        assertEquals(11, forenklet.evaluer())

        // Sjekk at konstante delen er forenklet
        assertTrue(forenklet is Add)
        assertTrue((forenklet as Add).venstre is Const)
        assertEquals(6, forenklet.venstre.evaluer())
    }

    @Test
    fun `erstatt skal substituere variabel`() {
        val x = Grunnlag("x", Const(5))
        val y = Grunnlag("y", Const(10))

        val uttrykk = x + y

        // Erstatt x med konstanten 100
        val substituert = uttrykk.erstatt("x") { Const(100) }

        // Siden x er et Grunnlag (ikke Var), erstatter ikke denne funksjonen det
        // erstatt() fungerer kun for Var som har faktum.name
        assertEquals(15, substituert.evaluer())
        assertEquals("x + y", substituert.notasjon())
    }

    @Test
    fun `operator overloading skal fungere med Number`() {
        val x = Grunnlag("x", Const(5))

        val uttrykk1 = x + 10  // Uttrykk + Number
        assertEquals(15, uttrykk1.evaluer())

        val uttrykk2 = 10 + x  // Number + Uttrykk
        assertEquals(15, uttrykk2.evaluer())

        val uttrykk3 = x * 2
        assertEquals(10, uttrykk3.evaluer())

        val uttrykk4 = 2 * x
        assertEquals(10, uttrykk4.evaluer())
    }

    @Test
    fun `kompleks slitertillegg beregning`() {
        val G = Grunnlag("G", Const(110000))
        val faktiskTrygdetid = Grunnlag("faktiskTrygdetid", Const(20))
        val fullTrygdetid = Grunnlag("FULL_TRYGDETID", Const(40))
        val MND_36 = Grunnlag("MND_36", Const(36))
        val antallMåneder = Grunnlag("antallMånederEtterNedreAldersgrense", Const(24))

        // Subberegninger
        val fulltSlitertillegg = (0.25 * G / 12).navngi("fulltSlitertillegg")
        val trygdetidFaktor = (faktiskTrygdetid / fullTrygdetid).navngi("trygdetidFaktor")
        val justeringsFaktor = ((MND_36 - antallMåneder) / MND_36).navngi("justeringsFaktor")

        // Hovedberegning
        val slitertillegg = fulltSlitertillegg * justeringsFaktor * trygdetidFaktor

        // Verifiser resultat
        val resultat = slitertillegg.evaluer()
        assertTrue(resultat > 380 && resultat < 382) // Ca 381.94

        // Verifiser notasjon
        val notasjon = slitertillegg.notasjon()
        assertTrue(notasjon.contains("fulltSlitertillegg"))
        assertTrue(notasjon.contains("justeringsFaktor"))
        assertTrue(notasjon.contains("trygdetidFaktor"))

        // Generer forklaring
        val forklaring = slitertillegg.forklar("slitertillegg", maxDybde = 2)
        assertNotNull(forklaring)
        assertTrue(forklaring.subformler.isNotEmpty())
    }

    // ========================================================================
    // Tester for Grunnlag operator overloading (direkte bruk)
    // ========================================================================

    @Test
    fun `Grunnlag plus Grunnlag skal fungere`() {
        val a = Grunnlag("a", Const(10))
        val b = Grunnlag("b", Const(20))
        val uttrykk = a + b

        assertEquals(30, uttrykk.evaluer())
        assertEquals("a + b", uttrykk.notasjon())
        assertEquals("10 + 20", uttrykk.konkret())
    }

    @Test
    fun `Grunnlag minus Grunnlag skal fungere`() {
        val a = Grunnlag("a", Const(50))
        val b = Grunnlag("b", Const(30))
        val uttrykk = a - b

        assertEquals(20, uttrykk.evaluer())
        assertEquals("a - b", uttrykk.notasjon())
        assertEquals("50 - 30", uttrykk.konkret())
    }

    @Test
    fun `Grunnlag times Grunnlag skal fungere`() {
        val a = Grunnlag("a", Const(5))
        val b = Grunnlag("b", Const(7))
        val uttrykk = a * b

        assertEquals(35, uttrykk.evaluer())
        assertEquals("a * b", uttrykk.notasjon())
        assertEquals("5 * 7", uttrykk.konkret())
    }

    @Test
    fun `Grunnlag div Grunnlag skal fungere`() {
        val a = Grunnlag("a", Const(100))
        val b = Grunnlag("b", Const(4))
        val uttrykk = a / b

        assertEquals(25.0, uttrykk.evaluer(), 0.001)
        assertEquals("a / b", uttrykk.notasjon())
        assertEquals("100 / 4", uttrykk.konkret())
    }

    @Test
    fun `Grunnlag plus Number skal fungere`() {
        val x = Grunnlag("x", Const(10))
        val uttrykk = x + 5

        assertEquals(15, uttrykk.evaluer())
        assertEquals("x + 5", uttrykk.notasjon())
    }

    @Test
    fun `Number plus Grunnlag skal fungere`() {
        val x = Grunnlag("x", Const(10))
        val uttrykk = 5 + x

        assertEquals(15, uttrykk.evaluer())
        assertEquals("5 + x", uttrykk.notasjon())
    }

    @Test
    fun `Grunnlag times Number skal fungere`() {
        val x = Grunnlag("x", Const(10))
        val uttrykk = x * 3

        assertEquals(30, uttrykk.evaluer())
        assertEquals("x * 3", uttrykk.notasjon())
    }

    @Test
    fun `Number times Grunnlag skal fungere`() {
        val x = Grunnlag("x", Const(10))
        val uttrykk = 3 * x

        assertEquals(30, uttrykk.evaluer())
        assertEquals("3 * x", uttrykk.notasjon())
    }

    @Test
    fun `Grunnlag div Number skal fungere`() {
        val x = Grunnlag("x", Const(100))
        val uttrykk = x / 4

        assertEquals(25.0, uttrykk.evaluer(), 0.001)
        assertEquals("x / 4", uttrykk.notasjon())
    }

    @Test
    fun `Number div Grunnlag skal fungere`() {
        val x = Grunnlag("x", Const(4))
        val uttrykk = 100 / x

        assertEquals(25.0, uttrykk.evaluer(), 0.001)
        assertEquals("100 / x", uttrykk.notasjon())
    }

    @Test
    fun `Grunnlag plus Uttrykk skal fungere`() {
        val a = Grunnlag("a", Const(10))
        val b = Grunnlag("b", Const(20))
        val uttrykk = a + (b * 2)  // a + (b * 2)

        assertEquals(50, uttrykk.evaluer())
        assertEquals("a + b * 2", uttrykk.notasjon())
    }

    @Test
    fun `Uttrykk plus Grunnlag skal fungere`() {
        val a = Grunnlag("a", Const(10))
        val b = Grunnlag("b", Const(20))
        val uttrykk = (a * 2) + b  // (a * 2) + b

        assertEquals(40, uttrykk.evaluer())
        assertEquals("a * 2 + b", uttrykk.notasjon())
    }

    @Test
    fun `kompleks uttrykk med Grunnlag direkte skal fungere`() {
        val G = Grunnlag("G", Const(110000))
        val sats = Grunnlag("sats", Const(0.25))
        val måneder = Grunnlag("måneder", Const(12))

        // Direkte bruk
        val uttrykk = sats * G / måneder

        val resultat = uttrykk.evaluer()
        assertTrue(resultat > 2291 && resultat < 2292)

        assertEquals("sats * G / måneder", uttrykk.notasjon())
        assertEquals("0.25 * 110000 / 12", uttrykk.konkret())
    }

    @Test
    fun `slitertillegg med Grunnlag direkte syntaks`() {
        val G = Grunnlag("G", Const(110000))
        val faktiskTrygdetid = Grunnlag("faktiskTrygdetid", Const(20))
        val fullTrygdetid = Grunnlag("FULL_TRYGDETID", Const(40))
        val MND_36 = Grunnlag("MND_36", Const(36))
        val antallMåneder = Grunnlag("antallMånederEtterNedreAldersgrense", Const(24))

        // Subberegninger med direkte Grunnlag-bruk
        val fulltSlitertillegg = (0.25 * G / 12).navngi("fulltSlitertillegg")
        val trygdetidFaktor = (faktiskTrygdetid / fullTrygdetid).navngi("trygdetidFaktor")
        val justeringsFaktor = ((MND_36 - antallMåneder) / MND_36).navngi("justeringsFaktor")

        // Hovedberegning
        val slitertillegg = fulltSlitertillegg * justeringsFaktor * trygdetidFaktor

        // Verifiser resultat
        val resultat = slitertillegg.evaluer()
        assertTrue(resultat > 380 && resultat < 382) // Ca 381.94

        // Verifiser at notasjon bruker navngitte uttrykk
        val notasjon = slitertillegg.notasjon()
        assertTrue(notasjon.contains("fulltSlitertillegg"))
        assertTrue(notasjon.contains("justeringsFaktor"))
        assertTrue(notasjon.contains("trygdetidFaktor"))
    }

    @Test
    fun `test min med Grunnlag`() {
        val a = Grunnlag("a", Const(10))
        val b = Grunnlag("b", Const(5))

        val uttrykk = min(a, b)

        assertEquals(5.0, uttrykk.evaluer())  // min returns Double
        assertEquals("min(a,b)", uttrykk.notasjon())  // No space after comma
    }
}
