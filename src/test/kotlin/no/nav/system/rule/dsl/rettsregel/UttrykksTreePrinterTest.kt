package no.nav.system.rule.dsl.rettsregel

import no.nav.system.rule.dsl.inspections.UttrykksTreePrinter
import no.nav.system.rule.dsl.inspections.printTree
import no.nav.system.rule.dsl.rettsregel.operators.erLik
import no.nav.system.rule.dsl.rettsregel.operators.times
import no.nav.system.rule.dsl.rettsregel.operators.plus
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class UttrykksTreePrinterTest {

    @Test
    fun `skal vise enkel Faktum med konstant verdi`() {
        val alder = Faktum("Alder", 67)

        val output = alder.printTree()

        println(output)
        assertTrue(output.contains("Alder = 67"))
    }

    @Test
    fun `skal vise MathOperation med multiplikasjon`() {
        val sats = Faktum("Sats", 1000)
        val faktor = Faktum("Faktor", 2.5)
        val resultat = Faktum("Resultat", sats * faktor)

        val output = resultat.printTree()

        println(output)
        println("---")

        assertTrue(output.contains("Resultat"))
        assertTrue(output.contains("MUL"))
        assertTrue(output.contains("Sats"))
        assertTrue(output.contains("Faktor"))
    }

    @Test
    fun `skal vise nested MathOperations`() {
        val a = Faktum("A", 10)
        val b = Faktum("B", 20)
        val c = Faktum("C", 5)

        // (A + B) * C
        val sum = a + b
        val resultat = Faktum("Resultat", sum * c)

        val output = resultat.printTree()

        println(output)
        println("---")

        assertTrue(output.contains("Resultat"))
        assertTrue(output.contains("MUL"))
        assertTrue(output.contains("ADD"))
        assertTrue(output.contains("A = 10"))
        assertTrue(output.contains("B = 20"))
        assertTrue(output.contains("C = 5"))
    }

    @Test
    fun `skal vise deduplication når samme Faktum brukes flere ganger`() {
        val trygdetid = Faktum("Trygdetid", 40)
        val sats1 = Faktum("Sats1", 1000)
        val sats2 = Faktum("Sats2", 2000)

        // Use trygdetid twice
        val beregning1 = sats1 * trygdetid
        val beregning2 = sats2 * trygdetid
        val total = Faktum("Total", beregning1 + beregning2)

        val output = total.printTree()

        println(output)
        println("---")

        // Should show trygdetid reference
        assertTrue(output.contains("[1]") || output.contains("[2]") || output.contains("[3]"))
        assertTrue(output.contains("forekomster") || output.contains("forekomst"))
        assertTrue(output.contains("Trygdetid"))
    }

    @Test
    fun `skal vise ComparisonOperation`() {
        val alder = Faktum("Alder", 67)
        val grense = Faktum("Aldersgrense", 62)
        val sammenligning = alder erLik grense

        val output = sammenligning.printTree()

        println(output)
        println("---")

        assertTrue(output.contains("LIK") || output.contains("NEI"))
        assertTrue(output.contains("Alder"))
        assertTrue(output.contains("Aldersgrense"))
    }

    @Test
    fun `skal vise kompleks beregning med deduplication`() {
        // Simulate pension calculation: fulltSlitertillegg * trygdetidFaktor * justeringsFaktor
        val fulltSlitertillegg = Faktum("fulltSlitertillegg", 25000.0)
        val trygdetidFaktor = Faktum("trygdetidFaktor", 0.9)
        val justeringsFaktor = Faktum("justeringsFaktor", 1.02)

        val trinn1 = fulltSlitertillegg * trygdetidFaktor
        val trinn2 = trinn1 * justeringsFaktor
        val slitertillegg = Faktum("slitertillegg", trinn2)

        val output = slitertillegg.printTree()

        println(output)
        println("---")

        assertTrue(output.contains("slitertillegg"))
        assertTrue(output.contains("MUL"))
        assertTrue(output.contains("fulltSlitertillegg"))
        assertTrue(output.contains("trygdetidFaktor"))
        assertTrue(output.contains("justeringsFaktor"))
    }

    @Test
    fun `skal bruke extension function printTree()`() {
        val verdi = Faktum("TestVerdi", 42)

        val output = verdi.printTree()

        assertNotNull(output)
        assertTrue(output.contains("TestVerdi"))
        assertTrue(output.contains("42"))
    }
}
