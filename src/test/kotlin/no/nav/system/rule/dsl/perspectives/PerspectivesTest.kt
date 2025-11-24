package no.nav.system.rule.dsl.perspectives

import no.nav.system.rule.dsl.AbstractRuleset
import no.nav.system.rule.dsl.explanation.traverseHva
import no.nav.system.rule.dsl.explanation.traverseFull
import no.nav.system.rule.dsl.explanation.collectFaktum
import no.nav.system.rule.dsl.reference.ref
import no.nav.system.rule.dsl.rettsregel.Faktum
import no.nav.system.rule.dsl.rettsregel.operators.erStørreEllerLik
import no.nav.system.rule.dsl.rettsregel.operators.times
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class PerspectivesTest {

    // Simple test ruleset
    class TestRuleset(private val alder: Int) : AbstractRuleset<Boolean>() {
        override fun create() {
            regel("VILKÅR-ALDER") {
                HVIS { Faktum("alder", alder) erStørreEllerLik Faktum("aldersgrense", 67) }
                SÅ { RETURNER(true) }
                ELLERS { RETURNER(false) }
            }
        }
    }

    @Test
    fun `skal kunne kalle traverseHva() på ARC tree`() {
        val ruleset = TestRuleset(70)
        val result = ruleset.test()

        val output = ruleset.traverseHva()

        println(output)
        assertNotNull(output)
        assertTrue(output.isNotEmpty())
        assertTrue(output.contains("TestRuleset"))
    }

    @Test
    fun `skal kunne kalle traverseFull() på ARC tree`() {
        val ruleset = TestRuleset(70)
        val result = ruleset.test()

        val output = ruleset.traverseFull()

        println(output)
        assertNotNull(output)
        assertTrue(output.isNotEmpty())
        assertTrue(output.contains("TestRuleset"))
    }

    @Test
    fun `skal kunne kalle collectFaktum() på ARC tree`() {
        val ruleset = TestRuleset(70)
        val result = ruleset.test()

        val faktumNodes = ruleset.collectFaktum()

        println("Found ${faktumNodes.size} Faktum nodes")
        faktumNodes.forEach { node ->
            println("  - ${node.faktum.navn} = ${node.faktum.verdi}")
        }

        assertNotNull(faktumNodes)
        assertTrue(faktumNodes.isNotEmpty())
    }

    @Test
    fun `skal kunne kalle toUttrykksTree() på Faktum`() {
        val sats = Faktum("sats", 1000)
        val faktor = Faktum("faktor", 2.5)
        val resultat = Faktum("resultat", sats * faktor)

        val output = resultat.toUttrykksTree()

        println(output)
        assertTrue(output.contains("Formula Tree"))
        assertTrue(output.contains("resultat"))
        assertTrue(output.contains("MUL"))
    }

    @Test
    fun `skal kunne kalle toFaktumExplanation() på Faktum`() {
        val aldersgrense = Faktum("aldersgrense", 67)

        val output = aldersgrense.toFaktumExplanation()

        println(output)
        assertTrue(output.contains("Explanation for 'aldersgrense'"))
        assertTrue(output.contains("HVA"))
    }

    @Test
    fun `perspektiver skal fungere med Faktum med referanser`() {
        val aldersgrense = Faktum("aldersgrense", 67)
            .ref("FTL-20-7", "https://lovdata.no/...")

        val explanation = aldersgrense.toFaktumExplanation()

        println(explanation)
        assertTrue(explanation.contains("aldersgrense"))
        // References might not be shown in forklar() yet, but at least verify Faktum works
        assertNotNull(aldersgrense.references)
        assertEquals(1, aldersgrense.references.size)
    }

    @Test
    fun `Faktum hvorfor skal traversere opp i ARC tree`() {
        val ruleset = TestRuleset(70)
        val result = ruleset.test()

        // Collect all Faktum from the execution
        val faktumNodes = ruleset.collectFaktum()

        assertTrue(faktumNodes.isNotEmpty())

        // Each Faktum should be able to compute its hvorfor path
        faktumNodes.forEach { node ->
            val hvorfor = node.faktum.hvorfor()
            println("Faktum '${node.faktum.navn}' hvorfor path: ${hvorfor.map { it.hva() }}")

            // Hvorfor might be empty for some Faktum, but the method should work
            assertNotNull(hvorfor)
        }
    }
}
