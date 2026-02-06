package no.nav.system.ruledsl.core.trace

import no.nav.system.ruledsl.core.expression.Faktum
import no.nav.system.ruledsl.core.expression.Verdi
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ReferenceTest {

    @Test
    fun `references are attached to rule trace and shown in explanation`() {
        val ruleContext = RuleContext(
            mutableMapOf(Tracer::class to DefaultTracer("test"))
        )

        val result = with(ruleContext) {
            traced<Faktum<Int>> {
                regel("pension calculation") {
                    HVIS { true }
                    RETURNER { faktum("pension", Verdi(1000)) }
                    REF("FTL-20-18", "https://lovdata.no/ftl-20-18")
                    REF("RS-456", "https://rundskriv.nav.no/456")
                }
            }
        }

        // References are recorded in trace
        val ruleTrace = ruleContext.root().children.first()
        assertEquals(2, ruleTrace.references.size)
        assertEquals("FTL-20-18", ruleTrace.references[0].id)
        assertEquals("RS-456", ruleTrace.references[1].id)

        // References are included in explanation
        val explanation = result.forklar()
        assertTrue(explanation.contains("FTL-20-18"))
        assertTrue(explanation.contains("lovdata.no"))
        assertTrue(explanation.contains("RS-456"))
        assertTrue(explanation.contains("rundskriv.nav.no"))
    }
}
