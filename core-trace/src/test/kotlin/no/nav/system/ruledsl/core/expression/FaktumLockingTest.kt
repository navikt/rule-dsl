package no.nav.system.ruledsl.core.expression

import no.nav.system.ruledsl.core.expression.math.*
import no.nav.system.ruledsl.core.trace.DefaultTracer
import no.nav.system.ruledsl.core.trace.RuleContext
import no.nav.system.ruledsl.core.trace.Tracer
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Tests for Faktum behavior.
 *
 * Rule: Faktum always acts as a boundary - notation() returns name.
 * To see the formula, access expression.notation() directly.
 *
 * Note: Faktum can ONLY be created via faktum() in RuleContext.
 * Use Verdi for input values and constants (not traced).
 */
class FaktumLockingTest {

    private fun createContext() = RuleContext(
        mutableMapOf(Tracer::class to DefaultTracer("test"))
    )

    @Test
    fun `faktum always returns name in notation`() {
        val ctx = createContext()
        with(ctx) {
            val a = Verdi("a", 10)
            val b = Verdi("b", 20)
            val sum = faktum("sum", a + b)

            // Faktum returns its name
            assertEquals("sum", sum.notation())

            // To see the formula, access expression.notation()
            assertEquals("a + b", sum.expression.notation())
        }
    }

    @Test
    fun `faktum always returns value in concrete`() {
        val ctx = createContext()
        with(ctx) {
            val a = Verdi("a", 10)
            val b = Verdi("b", 20)
            val sum = faktum("sum", a + b)

            assertEquals("30", sum.concrete())

            // To see the concrete calculation, access expression.concrete()
            assertEquals("10 + 20", sum.expression.concrete())
        }
    }

    @Test
    fun `faktum used in expression shows name`() {
        val ctx = createContext()
        with(ctx) {
            val a = Verdi("a", 10)
            val b = Verdi("b", 20)
            val c = Verdi("c", 5)

            val sum = faktum("sum", a + b)
            val result = sum * c  // BinaryOperation using Faktum

            // sum shows as "sum", not expanded
            assertEquals("sum * c", result.notation())
            assertEquals("30 * 5", result.concrete())
            assertEquals(150, result.value)
        }
    }

    @Test
    fun `isConstant distinguishes constants from calculations`() {
        val ctx = createContext()
        with(ctx) {
            val a = Verdi("a", 10)
            val b = Verdi("b", 20)

            val konstant = faktum("konstant", Verdi(100))  // wraps Verdi
            val sum = faktum("sum", a + b)  // wraps BinaryOperation

            assertTrue(konstant.isConstant)
            assertFalse(sum.isConstant)
        }
    }

    @Test
    fun `faktumSet for verdi inputs returns empty set`() {
        val ctx = createContext()
        with(ctx) {
            // Faktum with Verdi inputs has no Faktum dependencies
            val a = Verdi("a", 10)
            val b = Verdi("b", 20)
            val sum = faktum("sum", a + b)

            assertEquals(emptySet<Faktum<*>>(), sum.faktumSet())
        }
    }

    @Test
    fun `faktumSet for faktum inputs returns those faktum`() {
        val ctx = createContext()
        with(ctx) {
            val a = faktum("a", Verdi(10))
            val b = faktum("b", Verdi(20))
            val sum = faktum("sum", a + b)

            // faktumSet returns immediate Faktum used in the expression
            assertEquals(setOf(a, b), sum.faktumSet())
        }
    }

    @Test
    fun `nested faktum hierarchy`() {
        val ctx = createContext()
        with(ctx) {
            val grunnbeløp = Verdi("grunnbeløp", 118620)
            val satsFaktor = Verdi("satsFaktor", 0.66)

            val grunnpensjon = faktum("grunnpensjon", grunnbeløp * satsFaktor)
            val tillegg = Verdi("tillegg", 15000)

            val total = faktum("total", grunnpensjon + tillegg)

            // total's expression uses named Faktum and Verdi
            assertEquals("grunnpensjon + tillegg", total.expression.notation())

            // grunnpensjon's expression uses Verdi
            assertEquals("grunnbeløp * satsFaktor", grunnpensjon.expression.notation())

            // faktumSet returns immediate Faktum used in the expression
            // (only grunnpensjon, not tillegg since tillegg is Verdi)
            assertEquals(setOf(grunnpensjon), total.faktumSet())
        }
    }

    // ===== debugTree() tests =====

    @Test
    fun `debugTree shows formula hierarchy`() {
        val ctx = createContext()
        with(ctx) {
            val G = Verdi("G", 56123)
            val sats = Verdi("sats", 0.42)
            val grunnpensjon = faktum("grunnpensjon", G * sats)
            val tillegg = Verdi("tillegg", 15000)
            val total = faktum("total", grunnpensjon + tillegg)

            val tree = total.debugTree()
            println(tree)

            // Verify structure - subformulas only show non-constant Faktum
            assertTrue(tree.contains("total ="))
            assertTrue(tree.contains("notation: grunnpensjon + tillegg"))
            assertTrue(tree.contains("grunnpensjon ="))
            // Verdi (G, sats, tillegg) are NOT shown as subformulas
            assertFalse(tree.contains("G = 56123"))
            assertFalse(tree.contains("sats = 0.42"))
        }
    }

    @Test
    fun `debugTree with complex nested formulas`() {
        val ctx = createContext()
        with(ctx) {
            val G = Verdi("G", 56123)
            val OPT = Verdi("OPT", 3.1)
            val PP = Verdi("PP", 0.5)
            val SPT = Verdi("SPT", 4.3)
            val PX = Verdi("PX", 0.8)

            val fortyTwo = faktum("fortyTwo", 0.42 * G * OPT * PP / 40)
            val fortyFive = faktum("fortyFive", 0.45 * G * SPT * PX / 40)
            val sum = faktum("sum", fortyTwo + fortyFive)

            println("=== debugTree output ===")
            println(sum.debugTree())

            val tree = sum.debugTree()
            assertTrue(tree.contains("sum ="))
            assertTrue(tree.contains("notation: fortyTwo + fortyFive"))
            assertTrue(tree.contains("fortyTwo ="))
            assertTrue(tree.contains("fortyFive ="))
            // Verdi are NOT shown as subformulas
            assertFalse(tree.contains("G = 56123"))
        }
    }
}
