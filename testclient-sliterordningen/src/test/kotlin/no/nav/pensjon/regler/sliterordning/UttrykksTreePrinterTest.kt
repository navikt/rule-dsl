package no.nav.pensjon.regler.sliterordning

import no.nav.system.ruledsl.core.inspections.printTree
import no.nav.system.ruledsl.core.rettsregel.Faktum
import no.nav.system.ruledsl.core.operators.erLik
import no.nav.system.ruledsl.core.operators.plus
import no.nav.system.ruledsl.core.operators.times
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class UttrykksTreePrinterTest {

    @Test
    fun `skal vise enkel Faktum med konstant verdi`() {
        val alder = Faktum("Alder", 67)

        val output = alder.printTree()

        println(output)
        Assertions.assertTrue(output.contains("Alder = 67"))
    }

    @Test
    fun `skal vise MathOperation med multiplikasjon`() {
        val sats = Faktum("Sats", 1000)
        val faktor = Faktum("Faktor", 2.5)
        val resultat = Faktum("Resultat", sats * faktor)

        val output = resultat.printTree()

        println(output)
        println("---")

        Assertions.assertTrue(output.contains("Resultat"))
        Assertions.assertTrue(output.contains("MUL"))
        Assertions.assertTrue(output.contains("Sats"))
        Assertions.assertTrue(output.contains("Faktor"))
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

        Assertions.assertTrue(output.contains("Resultat"))
        Assertions.assertTrue(output.contains("MUL"))
        Assertions.assertTrue(output.contains("ADD"))
        Assertions.assertTrue(output.contains("A = 10"))
        Assertions.assertTrue(output.contains("B = 20"))
        Assertions.assertTrue(output.contains("C = 5"))
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
        Assertions.assertTrue(output.contains("[1]") || output.contains("[2]") || output.contains("[3]"))
        Assertions.assertTrue(output.contains("forekomster") || output.contains("forekomst"))
        Assertions.assertTrue(output.contains("Trygdetid"))
    }

    @Test
    fun `skal vise ComparisonOperation`() {
        val alder = Faktum("Alder", 67)
        val grense = Faktum("Aldersgrense", 62)
        val sammenligning = alder erLik grense

        val output = sammenligning.printTree()

        println(output)
        println("---")

        Assertions.assertTrue(output.contains("LIK") || output.contains("NEI"))
        Assertions.assertTrue(output.contains("Alder"))
        Assertions.assertTrue(output.contains("Aldersgrense"))
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

        Assertions.assertTrue(output.contains("slitertillegg"))
        Assertions.assertTrue(output.contains("MUL"))
        Assertions.assertTrue(output.contains("fulltSlitertillegg"))
        Assertions.assertTrue(output.contains("trygdetidFaktor"))
        Assertions.assertTrue(output.contains("justeringsFaktor"))
    }

    @Test
    fun `skal bruke extension function printTree()`() {
        val verdi = Faktum("TestVerdi", 42)

        val output = verdi.printTree()

        Assertions.assertNotNull(output)
        Assertions.assertTrue(output.contains("TestVerdi"))
        Assertions.assertTrue(output.contains("42"))
    }
}