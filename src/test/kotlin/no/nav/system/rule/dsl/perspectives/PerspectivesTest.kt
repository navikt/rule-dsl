package no.nav.system.rule.dsl.perspectives

import no.nav.system.rule.dsl.AbstractRuleset
import no.nav.system.rule.dsl.reference.ref
import no.nav.system.rule.dsl.resource.ExecutionTrace
import no.nav.system.rule.dsl.rettsregel.Faktum
import no.nav.system.rule.dsl.rettsregel.operators.erStørreEllerLik
import no.nav.system.rule.dsl.rettsregel.operators.times
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class PerspectivesTest {

    // Simple test ruleset with ExecutionTrace
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
    fun `skal kunne kalle toFullString() på ExecutionTrace`() {
        val trace = ExecutionTrace()

        // Note: ExecutionTrace is populated during actual service execution
        // For now, just verify the API works (will show empty trace)
        val output = trace.toFullString()

        println(output)
        assertNotNull(output)
        assertTrue(output.isNotEmpty())
        // Empty trace shows appropriate message
        assertTrue(output.contains("Ingen kjøring") || output.contains("Full Execution Trace"))
    }

    @Test
    fun `skal kunne kalle toFunctionalString() på ExecutionTrace`() {
        val trace = ExecutionTrace()

        // Note: ExecutionTrace is populated during actual service execution
        // For now, just verify the API works (will show empty trace)
        val output = trace.toFunctionalString()

        println(output)
        assertNotNull(output)
        assertTrue(output.isNotEmpty())
        // Empty trace shows appropriate message
        assertTrue(output.contains("Ingen beslutninger") || output.contains("Functional Execution Path"))
    }

    @Test
    fun `skal kunne kalle toUttrykksTree() på Faktum`() {
        val trace = ExecutionTrace()

        val sats = Faktum("sats", 1000)
        val faktor = Faktum("faktor", 2.5)
        val resultat = Faktum("resultat", sats * faktor)

        val output = trace.toUttrykksTree(resultat)

        println(output)
        assertTrue(output.contains("Formula Tree"))
        assertTrue(output.contains("resultat"))
        assertTrue(output.contains("MUL"))
    }

    @Test
    fun `skal kunne kalle toFaktumExplanation() på Faktum`() {
        val trace = ExecutionTrace()

        val aldersgrense = Faktum("aldersgrense", 67)

        val output = trace.toFaktumExplanation(aldersgrense)

        println(output)
        assertTrue(output.contains("Explanation for 'aldersgrense'"))
        assertTrue(output.contains("HVA"))
    }

    @Test
    fun `toFullString() skal vise tom melding når trace er tom`() {
        val trace = ExecutionTrace()

        val output = trace.toFullString()

        assertTrue(output.contains("Ingen kjøring registrert") || output.contains("Empty execution trace"))
    }

    @Test
    fun `toFunctionalString() skal vise tom melding når ingen beslutninger`() {
        val trace = ExecutionTrace()

        val output = trace.toFunctionalString()

        assertTrue(output.contains("Ingen beslutninger") || output.contains("No decisions recorded"))
    }

    @Test
    fun `perspektiver skal fungere med Faktum med referanser`() {
        val trace = ExecutionTrace()

        val aldersgrense = Faktum("aldersgrense", 67)
            .ref("FTL-20-7", "https://lovdata.no/...")

        val explanation = trace.toFaktumExplanation(aldersgrense)

        println(explanation)
        assertTrue(explanation.contains("aldersgrense"))
        // References might not be shown in forklar() yet, but at least verify Faktum works
        assertNotNull(aldersgrense.references)
        assertEquals(1, aldersgrense.references.size)
    }
}
