package no.nav.system.ruledsl.core.expression

import no.nav.system.ruledsl.core.expression.math.*
import no.nav.system.ruledsl.core.trace.DefaultTracer
import no.nav.system.ruledsl.core.trace.RuleContext
import no.nav.system.ruledsl.core.trace.Tracer
import no.nav.system.ruledsl.core.trace.traced
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Tests for Faktum behavior.
 *
 * Rule: Faktum always acts as a boundary - notation() returns name.
 * To see the formula, access expression.notation() directly.
 *
 * Note: Faktum can ONLY be created via faktum() inside a regel block.
 * Use Verdi for input values and constants (not traced).
 */
class FaktumLockingTest {

    private fun createContext() = RuleContext(
        mutableMapOf(Tracer::class to DefaultTracer("test"))
    )

    @Test
    fun `faktum always returns name in notation`() {
        val ruleContext = createContext()
        with(ruleContext) {
            val result: Faktum<Int> = traced {
                val a = Verdi("a", 10)
                val b = Verdi("b", 20)

                regel("test") {
                    HVIS { true }
                    RETURNER { faktum("sum", a + b) }
                }
            }

            assertEquals("sum", result.notation())
            assertEquals("a + b", result.expression.notation())
        }
    }

    @Test
    fun `faktum always returns value in concrete`() {
        val ruleContext = createContext()
        with(ruleContext) {
            val result: Faktum<Int> = traced {
                val a = Verdi("a", 10)
                val b = Verdi("b", 20)

                regel("test") {
                    HVIS { true }
                    RETURNER { faktum("sum", a + b) }
                }
            }

            assertEquals("30", result.concrete())
            assertEquals("10 + 20", result.expression.concrete())
        }
    }

    @Test
    fun `faktum used in expression shows name`() {
        val ruleContext = createContext()
        with(ruleContext) {
            val result: Faktum<Int> = traced {
                val a = Verdi("a", 10)
                val b = Verdi("b", 20)
                val c = Verdi("c", 5)

                var sum: Faktum<Int>? = null
                regel("make sum") {
                    HVIS { true }
                    SÅ { sum = faktum("sum", a + b) }
                }

                regel("use sum") {
                    HVIS { true }
                    RETURNER { faktum("result", sum!! * c) }
                }
            }

            assertEquals("sum * c", result.expression.notation())
            assertEquals("30 * 5", result.expression.concrete())
            assertEquals(150, result.value)
        }
    }

    @Test
    fun `isConstant distinguishes constants from calculations`() {
        val ruleContext = createContext()
        with(ruleContext) {
            traced<Unit> {
                val a = Verdi("a", 10)
                val b = Verdi("b", 20)

                regel("test") {
                    HVIS { true }
                    SÅ {
                        val konstant = faktum("konstant", 100)
                        val sum = faktum("sum", a + b)

                        assertTrue(konstant.isConstant)
                        assertFalse(sum.isConstant)
                    }
                }
            }
        }
    }

    @Test
    fun `faktumSet for verdi inputs returns empty set`() {
        val ruleContext = createContext()
        with(ruleContext) {
            traced<Unit> {
                val a = Verdi("a", 10)
                val b = Verdi("b", 20)

                regel("test") {
                    HVIS { true }
                    SÅ {
                        val sum = faktum("sum", a + b)
                        assertEquals(emptySet<Faktum<*>>(), sum.faktumSet())
                    }
                }
            }
        }
    }

    @Test
    fun `faktumSet for faktum inputs returns those faktum`() {
        val ruleContext = createContext()
        with(ruleContext) {
            traced<Unit> {
                var a: Faktum<Int>? = null
                var b: Faktum<Int>? = null

                regel("setup") {
                    HVIS { true }
                    SÅ {
                        a = faktum("a", 10)
                        b = faktum("b", 20)
                    }
                }

                regel("test") {
                    HVIS { true }
                    SÅ {
                        val sum = faktum("sum", a!! + b!!)
                        assertEquals(setOf(a, b), sum.faktumSet())
                    }
                }
            }
        }
    }

    @Test
    fun `nested faktum hierarchy`() {
        val ruleContext = createContext()
        with(ruleContext) {
            val total: Faktum<Double> = traced {
                val grunnbeløp = Verdi("grunnbeløp", 118620)
                val satsFaktor = Verdi("satsFaktor", 0.66)
                val tillegg = Verdi("tillegg", 15000)

                var grunnpensjon: Faktum<Double>? = null
                regel("beregn grunnpensjon") {
                    HVIS { true }
                    SÅ { grunnpensjon = faktum("grunnpensjon", grunnbeløp * satsFaktor) }
                }

                regel("beregn total") {
                    HVIS { true }
                    RETURNER { faktum("total", grunnpensjon!! + tillegg) }
                }
            }

            assertEquals("grunnpensjon + tillegg", total.expression.notation())
            assertEquals("grunnbeløp * satsFaktor", total.faktumSet().first().expression.notation())
            assertEquals(setOf("grunnpensjon"), total.faktumSet().map { it.name }.toSet())
        }
    }

    // ===== debugTree() tests =====

    @Test
    fun `debugTree shows formula hierarchy`() {
        val ruleContext = createContext()
        with(ruleContext) {
            val total: Faktum<Double> = traced {
                val G = Verdi("G", 56123)
                val sats = Verdi("sats", 0.42)
                val tillegg = Verdi("tillegg", 15000)

                var grunnpensjon: Faktum<Double>? = null
                regel("beregn grunnpensjon") {
                    HVIS { true }
                    SÅ { grunnpensjon = faktum("grunnpensjon", G * sats) }
                }

                regel("beregn total") {
                    HVIS { true }
                    RETURNER { faktum("total", grunnpensjon!! + tillegg) }
                }
            }

            val tree = total.debugTree()
            println(tree)

            assertTrue(tree.contains("total ="))
            assertTrue(tree.contains("notation: grunnpensjon + tillegg"))
            assertTrue(tree.contains("grunnpensjon ="))
            assertFalse(tree.contains("G = 56123"))
            assertFalse(tree.contains("sats = 0.42"))
        }
    }

    @Test
    fun `debugTree with complex nested formulas`() {
        val ruleContext = createContext()
        with(ruleContext) {
            val sum: Faktum<Double> = traced {
                val G = Verdi("G", 56123)
                val OPT = Verdi("OPT", 3.1)
                val PP = Verdi("PP", 0.5)
                val SPT = Verdi("SPT", 4.3)
                val PX = Verdi("PX", 0.8)

                var fortyTwo: Faktum<Double>? = null
                var fortyFive: Faktum<Double>? = null

                regel("beregn fortyTwo") {
                    HVIS { true }
                    SÅ { fortyTwo = faktum("fortyTwo", 0.42 * G * OPT * PP / 40) }
                }

                regel("beregn fortyFive") {
                    HVIS { true }
                    SÅ { fortyFive = faktum("fortyFive", 0.45 * G * SPT * PX / 40) }
                }

                regel("beregn sum") {
                    HVIS { true }
                    RETURNER { faktum("sum", fortyTwo!! + fortyFive!!) }
                }
            }

            println("=== debugTree output ===")
            println(sum.debugTree())

            val tree = sum.debugTree()
            assertTrue(tree.contains("sum ="))
            assertTrue(tree.contains("notation: fortyTwo + fortyFive"))
            assertTrue(tree.contains("fortyTwo ="))
            assertTrue(tree.contains("fortyFive ="))
            assertFalse(tree.contains("G = 56123"))
        }
    }
}
