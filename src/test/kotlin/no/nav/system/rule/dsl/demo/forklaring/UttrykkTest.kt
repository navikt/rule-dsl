package no.nav.system.rule.dsl.demo.forklaring

import no.nav.system.rule.dsl.forklaring.*
import no.nav.system.rule.dsl.rettsregel.Faktum
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class UttrykkTest {

    @Test
    fun `Var skal evaluere til faktum verdi`() {
        val faktum = Faktum("test", 42)
        val uttrykk = Var(faktum)

        assertEquals(42, uttrykk.evaluer())
        assertEquals("test", uttrykk.notasjon())
        assertEquals("42", uttrykk.konkret())
    }

    @Test
    fun `Const skal evaluere til konstant verdi`() {
        val uttrykk = Const(123)

        assertEquals(123, uttrykk.evaluer())
        assertEquals("123", uttrykk.notasjon())
    }

    @Test
    fun `Add skal addere to uttrykk`() {
        val a = Faktum("a", 10)
        val b = Faktum("b", 20)
        val uttrykk = Var(a) + Var(b)

        assertEquals(30, uttrykk.evaluer())
        assertEquals("a + b", uttrykk.notasjon())
        assertEquals("10 + 20", uttrykk.konkret())
    }

    @Test
    fun `Sub skal subtrahere to uttrykk`() {
        val a = Faktum("a", 50)
        val b = Faktum("b", 30)
        val uttrykk = Var(a) - Var(b)

        assertEquals(20, uttrykk.evaluer())
        assertEquals("a - b", uttrykk.notasjon())
    }

    @Test
    fun `Mul skal multiplisere to uttrykk`() {
        val a = Faktum("a", 5)
        val b = Faktum("b", 7)
        val uttrykk = Var(a) * Var(b)

        assertEquals(35, uttrykk.evaluer())
        assertEquals("a * b", uttrykk.notasjon())
    }

    @Test
    fun `Div skal dividere to uttrykk og gi Double`() {
        val a = Faktum("a", 100)
        val b = Faktum("b", 4)
        val uttrykk = Var(a) / Var(b)

        assertEquals(25.0, uttrykk.evaluer(), 0.001)
        assertEquals("a / b", uttrykk.notasjon())
    }

    @Test
    fun `Div med null skal kaste exception`() {
        val a = Faktum("a", 100)
        val b = Faktum("b", 0)
        val uttrykk = Var(a) / Var(b)

        assertThrows(ArithmeticException::class.java) {
            uttrykk.evaluer()
        }
    }

    @Test
    fun `Neg skal negere uttrykk`() {
        val a = Faktum("a", 42)
        val uttrykk = -Var(a)

        assertEquals(-42, uttrykk.evaluer())
        assertEquals("-a", uttrykk.notasjon())
    }

    @Test
    fun `kompleks uttrykk skal evalueres korrekt`() {
        val a = Faktum("a", 10)
        val b = Faktum("b", 20)
        val c = Faktum("c", 5)

        // (a + b) * c
        val uttrykk = (Var(a) + Var(b)) * Var(c)

        assertEquals(150, uttrykk.evaluer())
        assertEquals("(a + b) * c", uttrykk.notasjon())
    }

    @Test
    fun `navngitt uttrykk skal fungere som atomisk enhet`() {
        val a = Faktum("a", 10)
        val b = Faktum("b", 20)

        val sum = (Var(a) + Var(b)).navngi("sum")

        assertEquals(30, sum.evaluer())
        assertEquals("sum", sum.notasjon())
        assertEquals("30", sum.konkret())
    }

    @Test
    fun `faktumListe skal returnere alle faktum`() {
        val a = Faktum("a", 10)
        val b = Faktum("b", 20)
        val c = Faktum("c", 30)

        val uttrykk = (Var(a) + Var(b)) * Var(c)
        val faktumListe = uttrykk.faktumListe()

        assertEquals(3, faktumListe.size)
        assertTrue(faktumListe.any { it.name == "a" })
        assertTrue(faktumListe.any { it.name == "b" })
        assertTrue(faktumListe.any { it.name == "c" })
    }

    @Test
    fun `dybde skal beregnes korrekt`() {
        val a = Faktum("a", 10)
        val b = Faktum("b", 20)

        val enkel = Var(a)
        assertEquals(1, enkel.dybde())

        val sum = Var(a) + Var(b)
        assertEquals(2, sum.dybde())

        val kompleks = (Var(a) + Var(b)) * Const<Int>(5)
        assertEquals(3, kompleks.dybde())
    }

    @Test
    fun `forklarKompakt skal gi 3 linjer`() {
        val a = Faktum("a", 10)
        val uttrykk = Var(a) * 2

        val forklaring = uttrykk.forklarKompakt("resultat")
        val linjer = forklaring.trim().split("\n")

        assertEquals(3, linjer.size)
        assertTrue(forklaring.contains("resultat = a * 2"))
        assertTrue(forklaring.contains("resultat = 10 * 2"))
        assertTrue(forklaring.contains("resultat = 20"))
    }

    @Test
    fun `forklar skal generere HvordanForklaring`() {
        val a = Faktum("a", 10)
        val b = Faktum("b", 5)
        val uttrykk = Var(a) / Var(b)

        val forklaring = uttrykk.forklar("resultat")

        assertEquals("resultat", forklaring.hvaForklaring.navn)
        assertEquals("a / b", forklaring.hvaForklaring.symbolskUttrykk)
        assertEquals(2.0, forklaring.hvaForklaring.resultat)
    }

    @Test
    fun `visitor skal kunne traverse tre`() {
        val a = Faktum("a", 10)
        val b = Faktum("b", 20)
        val uttrykk = (Var(a) + Var(b)) * Const<Int>(2)

        val typer = uttrykk.visit { expr ->
            listOf(expr::class.simpleName ?: "Unknown")
        }

        assertTrue(typer.contains("Mul"))
        assertTrue(typer.contains("Add"))
        assertTrue(typer.contains("Var"))
        assertTrue(typer.contains("Const"))
    }

    @Test
    fun `forenkel skal evaluere konstante subtre`() {
        val x = Faktum("x", 5)

        // (2 * 3) + x  skal forenkles til 6 + x
        val uttrykk = (Const<Int>(2) * Const<Int>(3)) + Var(x)
        val forenklet = uttrykk.forenkel()

        assertEquals(11, forenklet.evaluer())

        // Sjekk at konstante delen er forenklet
        assertTrue(forenklet is Add)
        assertTrue((forenklet as Add).venstre is Const)
        assertEquals(6, forenklet.venstre.evaluer())
    }

    @Test
    fun `erstatt skal substituere variabel`() {
        val x = Faktum("x", 5)
        val y = Faktum("y", 10)

        val uttrykk = Var(x) + Var(y)

        // Erstatt x med konstanten 100
        val substituert = uttrykk.erstatt("x") { Const(100) }

        assertEquals(110, substituert.evaluer())
        assertEquals("100 + y", substituert.notasjon())
    }

    @Test
    fun `operator overloading skal fungere med Number`() {
        val x = Faktum("x", 5)

        val uttrykk1 = Var(x) + 10  // Uttrykk + Number
        assertEquals(15, uttrykk1.evaluer())

        val uttrykk2 = 10 + Var(x)  // Number + Uttrykk
        assertEquals(15, uttrykk2.evaluer())

        val uttrykk3 = Var(x) * 2
        assertEquals(10, uttrykk3.evaluer())

        val uttrykk4 = 2 * Var(x)
        assertEquals(10, uttrykk4.evaluer())
    }

    @Test
    fun `kompleks slitertillegg beregning`() {
        val G = Faktum("G", 110000)
        val faktiskTrygdetid = Faktum("faktiskTrygdetid", 20)
        val fullTrygdetid = Faktum("FULL_TRYGDETID", 40)
        val MND_36 = Faktum("MND_36", 36)
        val antallMåneder = Faktum("antallMånederEtterNedreAldersgrense", 24)

        // Subberegninger
        val fulltSlitertillegg = (0.25 * Var(G) / 12).navngi("fulltSlitertillegg")
        val trygdetidFaktor = (Var(faktiskTrygdetid) / Var(fullTrygdetid)).navngi("trygdetidFaktor")
        val justeringsFaktor = ((Var(MND_36) - Var(antallMåneder)) / Var(MND_36)).navngi("justeringsFaktor")

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
    // Tester for Faktum operator overloading (direkte bruk uten Var())
    // ========================================================================

    @Test
    fun `Faktum plus Faktum skal fungere`() {
        val a = Faktum("a", 10)
        val b = Faktum("b", 20)
        val uttrykk = a + b

        assertEquals(30, uttrykk.evaluer())
        assertEquals("a + b", uttrykk.notasjon())
        assertEquals("10 + 20", uttrykk.konkret())
    }

    @Test
    fun `Faktum minus Faktum skal fungere`() {
        val a = Faktum("a", 50)
        val b = Faktum("b", 30)
        val uttrykk = a - b

        assertEquals(20, uttrykk.evaluer())
        assertEquals("a - b", uttrykk.notasjon())
        assertEquals("50 - 30", uttrykk.konkret())
    }

    @Test
    fun `Faktum times Faktum skal fungere`() {
        val a = Faktum("a", 5)
        val b = Faktum("b", 7)
        val uttrykk = a * b

        assertEquals(35, uttrykk.evaluer())
        assertEquals("a * b", uttrykk.notasjon())
        assertEquals("5 * 7", uttrykk.konkret())
    }

    @Test
    fun `Faktum div Faktum skal fungere`() {
        val a = Faktum("a", 100)
        val b = Faktum("b", 4)
        val uttrykk = a / b

        assertEquals(25.0, uttrykk.evaluer(), 0.001)
        assertEquals("a / b", uttrykk.notasjon())
        assertEquals("100 / 4", uttrykk.konkret())
    }

    @Test
    fun `Faktum plus Number skal fungere`() {
        val x = Faktum("x", 10)
        val uttrykk = x + 5

        assertEquals(15, uttrykk.evaluer())
        assertEquals("x + 5", uttrykk.notasjon())
    }

    @Test
    fun `Number plus Faktum skal fungere`() {
        val x = Faktum("x", 10)
        val uttrykk = 5 + x

        assertEquals(15, uttrykk.evaluer())
        assertEquals("5 + x", uttrykk.notasjon())
    }

    @Test
    fun `Faktum times Number skal fungere`() {
        val x = Faktum("x", 10)
        val uttrykk = x * 3

        assertEquals(30, uttrykk.evaluer())
        assertEquals("x * 3", uttrykk.notasjon())
    }

    @Test
    fun `Number times Faktum skal fungere`() {
        val x = Faktum("x", 10)
        val uttrykk = 3 * x

        assertEquals(30, uttrykk.evaluer())
        assertEquals("3 * x", uttrykk.notasjon())
    }

    @Test
    fun `Faktum div Number skal fungere`() {
        val x = Faktum("x", 100)
        val uttrykk = x / 4

        assertEquals(25.0, uttrykk.evaluer(), 0.001)
        assertEquals("x / 4", uttrykk.notasjon())
    }

    @Test
    fun `Number div Faktum skal fungere`() {
        val x = Faktum("x", 4)
        val uttrykk = 100 / x

        assertEquals(25.0, uttrykk.evaluer(), 0.001)
        assertEquals("100 / x", uttrykk.notasjon())
    }

    @Test
    fun `Faktum plus Uttrykk skal fungere`() {
        val a = Faktum("a", 10)
        val b = Faktum("b", 20)
        val uttrykk = a + (b * 2)  // a + (b * 2)

        assertEquals(50, uttrykk.evaluer())
        assertEquals("a + b * 2", uttrykk.notasjon())
    }

    @Test
    fun `Uttrykk plus Faktum skal fungere`() {
        val a = Faktum("a", 10)
        val b = Faktum("b", 20)
        val uttrykk = (a * 2) + b  // (a * 2) + b

        assertEquals(40, uttrykk.evaluer())
        assertEquals("a * 2 + b", uttrykk.notasjon())
    }

    @Test
    fun `kompleks uttrykk med Faktum direkte skal fungere`() {
        val G = Faktum("G", 110000)
        val sats = Faktum("sats", 0.25)
        val måneder = Faktum("måneder", 12)

        // Direkte bruk uten Var()
        val uttrykk = sats * G / måneder

        val resultat = uttrykk.evaluer()
        assertTrue(resultat > 2291 && resultat < 2292)

        assertEquals("sats * G / måneder", uttrykk.notasjon())
        assertEquals("0.25 * 110000 / 12", uttrykk.konkret())
    }

    @Test
    fun `slitertillegg med Faktum direkte syntaks`() {
        val G = Faktum("G", 110000)
        val faktiskTrygdetid = Faktum("faktiskTrygdetid", 20)
        val fullTrygdetid = Faktum("FULL_TRYGDETID", 40)
        val MND_36 = Faktum("MND_36", 36)
        val antallMåneder = Faktum("antallMånederEtterNedreAldersgrense", 24)

        // Subberegninger med direkte Faktum-bruk
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
    fun `blandet bruk av Faktum og Var skal fungere`() {
        val a = Faktum("a", 10)
        val b = Faktum("b", 20)

        // Blandet syntaks (ikke anbefalt i praksis, men skal fungere)
        val uttrykk = a + Var(b)

        assertEquals(30, uttrykk.evaluer())
        assertEquals("a + b", uttrykk.notasjon())
    }
}
