package no.nav.system.ruledsl.core.trace

import no.nav.system.ruledsl.core.expression.Verdi
import no.nav.system.ruledsl.core.expression.debugTree
import no.nav.system.ruledsl.core.expression.math.div
import no.nav.system.ruledsl.core.expression.math.plus
import no.nav.system.ruledsl.core.expression.math.times
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test

/**
 * Tests demonstrating formula tracing with Verdi inputs.
 */
class ComplexFormulaTest {

    private fun createContext() = RuleContext(
        mutableMapOf(Tracer::class to DefaultTracer("test"))
    )

    @Test
    fun `complex formula`() {
        val ctx = createContext()
        with(ctx) {
            // Input facts (Verdi - named constants, not traced)
            val grunnbeløp = Verdi("grunnbeløp", 118620)
            val satsFaktor = Verdi("satsFaktor", 0.66)
            val tillegg = Verdi("tillegg", 15000)
            val barnetilleggFaktor = Verdi("barnetilleggFaktor", 2.0)
            val trygdetid = Verdi("trygdetid", 35)
            val fullTrygdetid = Verdi("fullTrygdetid", 40)

            // Intermediate calculations - each named and traced via faktum()
            val grunnpensjon = faktum("grunnpensjon", grunnbeløp * satsFaktor * trygdetid / fullTrygdetid)
            val barnetillegg = faktum("barnetillegg", tillegg * barnetilleggFaktor)
            val pensjon = faktum("pensjon", grunnpensjon + barnetillegg)

            // 1. Final result value
            assertEquals(98503.05, pensjon.value, 0.01)

            println(pensjon.debugTree())

            val debugLines = pensjon.debugTree().split("\n").iterator()

            assertEquals("pensjon = 98503.05", debugLines.next())
            assertEquals("  notation: grunnpensjon + barnetillegg", debugLines.next())
            assertEquals("  concrete: 68503.05 + 30000.0", debugLines.next())
            assertEquals("  subformulas:", debugLines.next())

            assertEquals("    grunnpensjon = 68503.05", debugLines.next())
            assertEquals("      notation: grunnbeløp * satsFaktor * trygdetid / fullTrygdetid", debugLines.next())
            assertEquals("      concrete: 118620 * 0.66 * 35 / 40", debugLines.next())

            assertEquals("    barnetillegg = 30000.0", debugLines.next())
            assertEquals("      notation: tillegg * barnetilleggFaktor", debugLines.next())
            assertEquals("      concrete: 15000 * 2.0", debugLines.next())

            // Last line is empty (from appendLine)
            assertEquals("", debugLines.next())
            assertFalse(debugLines.hasNext())
        }
    }
}
