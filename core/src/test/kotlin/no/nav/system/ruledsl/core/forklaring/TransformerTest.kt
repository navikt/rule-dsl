package no.nav.system.ruledsl.core.forklaring

import no.nav.system.ruledsl.core.model.Faktum
import no.nav.system.ruledsl.core.model.arc.AbstractRuleset
import no.nav.system.ruledsl.core.operators.erMindreEnn
import no.nav.system.ruledsl.core.operators.erStørreEnn
import no.nav.system.ruledsl.core.operators.plus
import no.nav.system.ruledsl.core.operators.times
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Tests for transformer system and explanation generation.
 *
 * Tests IndentedTextFormatter and Faktum.forklar() extension function.
 * Uses simple Int and String values.
 */
class TransformerTest {

    /**
     * Test basic explanation of a constant faktum
     */
    @Test
    fun `forklar explains constant faktum`() {
        class SimpleRS : AbstractRuleset<Faktum<Int>>() {
            override fun create() {
                regel("createValue") {
                    HVIS { true }
                    SÅ {
                        val f = sporing("count", 42)
                        RETURNER(f)
                    }
                }
            }
        }

        val faktum = SimpleRS().test()
        val explanation = faktum.forklar()

        assertTrue(explanation.contains("HVA:"))
        assertTrue(explanation.contains("count = 42"))
        assertTrue(explanation.contains("HVORFOR:"))
        assertTrue(explanation.contains("createValue"))
    }

    /**
     * Test explanation of calculated faktum
     */
    @Test
    fun `forklar explains calculated faktum with HVORDAN`() {
        class CalculationRS : AbstractRuleset<Faktum<Int>>() {
            override fun create() {
                regel("calculate") {
                    HVIS { true }
                    SÅ {
                        val result = sporing("sum", Faktum("a", 10) + Faktum("b", 20))
                        RETURNER(result)
                    }
                }
            }
        }

        val faktum = CalculationRS().test()
        val explanation = faktum.forklar()

        assertTrue(explanation.contains("HVA:"))
        assertTrue(explanation.contains("sum = 30"))
        assertTrue(explanation.contains("HVORDAN:"))
        assertTrue(explanation.contains("notasjon:"))
        assertTrue(explanation.contains("konkret:"))
        assertTrue(explanation.contains("HVORFOR:"))
    }

    /**
     * Test that inline faktum without sporing cannot be explained
     */
    @Test
    fun `forklar throws exception for non-tracked faktum`() {
        val inlineFaktum = Faktum("inline", 123)

        assertThrows<IllegalStateException> {
            inlineFaktum.forklar()
        }
    }

    /**
     * Test explanation includes predicates
     */
    @Test
    fun `forklar includes predicates from rule`() {
        class PredicateRS : AbstractRuleset<Faktum<String>>() {
            override fun create() {
                regel("checkValue") {
                    HVIS { Faktum("value", 50) erStørreEnn Faktum("min", 10) }
                    SÅ {
                        val result = sporing("status", "valid")
                        RETURNER(result)
                    }
                }
            }
        }

        val faktum = PredicateRS().test()
        val explanation = faktum.forklar()

        assertTrue(explanation.contains("checkValue"))
        // Should contain predicate information
        assertTrue(explanation.contains("JA") || explanation.contains("value"))
    }

    /**
     * Test explanation with multiple rules
     */
    @Test
    fun `forklar works with chained rules`() {
        class ChainedRS : AbstractRuleset<Faktum<Int>>() {
            var intermediate: Faktum<Int>? = null

            override fun create() {
                regel("first") {
                    HVIS { true }
                    SÅ {
                        intermediate = sporing("step1", 10)
                    }
                }

                regel("second") {
                    HVIS { "first".harTruffet() }
                    SÅ {
                        val final = sporing("step2", intermediate!!.verdi * 2)
                        RETURNER(final)
                    }
                }
            }
        }

        val faktum = ChainedRS().test()
        val explanation = faktum.forklar()

        assertTrue(explanation.contains("step2"))
        assertTrue(explanation.contains("second"))
    }

    /**
     * Test Filter.FUNCTIONAL
     */
    @Test
    fun `FUNCTIONAL filter includes relevant components`() {
        class FilteredRS : AbstractRuleset<Faktum<String>>() {
            override fun create() {
                regel("process") {
                    HVIS { true }
                    SÅ {
                        val result = sporing("output", "done")
                        RETURNER(result)
                    }
                }
            }
        }

        val faktum = FilteredRS().test()
        val explanation = faktum.forklar(filter = Filters.FUNCTIONAL)

        // Should include functional components
        assertTrue(explanation.contains("output = done"))
        assertTrue(explanation.contains("process"))
    }

    /**
     * Test custom filter
     */
    @Test
    fun `custom filter can control output`() {
        class CustomFilterRS : AbstractRuleset<Faktum<Int>>() {
            override fun create() {
                regel("calculate") {
                    HVIS { true }
                    SÅ {
                        val result = sporing("value", 99)
                        RETURNER(result)
                    }
                }
            }
        }

        val faktum = CustomFilterRS().test()

        // Filter that excludes everything
        val noFilter = Filter { false }
        val noExplanation = faktum.forklarMed(IndentedTextFormatter, noFilter)

        // Should still have HVA section but no HVORFOR
        assertTrue(noExplanation.contains("HVA:"))
        assertTrue(noExplanation.contains("value = 99"))
    }

    /**
     * Test explanation nesting with contributing faktum
     */
    @Test
    fun `explanation includes contributing faktum recursively`() {
        class NestedRS : AbstractRuleset<Faktum<Int>>() {
            override fun create() {
                regel("nested") {
                    HVIS { true }
                    SÅ {
                        val a = Faktum("base", 5)
                        val b = Faktum("multiplier", 3)
                        val result = sporing("product", a * b)
                        RETURNER(result)
                    }
                }
            }
        }

        val faktum = NestedRS().test()
        val explanation = faktum.forklar()

        // Main faktum
        assertTrue(explanation.contains("product"))

        // Contributing faktum should appear
        assertTrue(explanation.contains("base") || explanation.contains("5"))
        assertTrue(explanation.contains("multiplier") || explanation.contains("3"))
    }

    /**
     * Test explanation format structure
     */
    @Test
    fun `explanation has correct section structure`() {
        class StructureRS : AbstractRuleset<Faktum<Int>>() {
            override fun create() {
                regel("structured") {
                    HVIS { Faktum("x", 10) erMindreEnn Faktum("y", 20) }
                    SÅ {
                        val result = sporing("output", Faktum("a", 1) + Faktum("b", 2))
                        RETURNER(result)
                    }
                }
            }
        }

        val faktum = StructureRS().test()
        val explanation = faktum.forklar()

        // Check section order
        val hvaIndex = explanation.indexOf("HVA:")
        val hvordanIndex = explanation.indexOf("HVORDAN:")
        val hvorforIndex = explanation.indexOf("HVORFOR:")

        assertTrue(hvaIndex >= 0, "Should have HVA section")
        assertTrue(hvordanIndex >= 0, "Should have HVORDAN section")
        assertTrue(hvorforIndex >= 0, "Should have HVORFOR section")

        // HVA should come first
        assertTrue(hvaIndex < hvorforIndex)
    }

    /**
     * Test that constant faktum (depth 0) shows HVA
     */
    @Test
    fun `constant faktum at depth 0 shows HVA section`() {
        class ConstantRS : AbstractRuleset<Faktum<Int>>() {
            override fun create() {
                regel("constant") {
                    HVIS { true }
                    SÅ {
                        val result = sporing("fixedValue", 777)
                        RETURNER(result)
                    }
                }
            }
        }

        val faktum = ConstantRS().test()
        val explanation = faktum.forklar()

        assertTrue(explanation.contains("HVA:"))
        assertTrue(explanation.contains("fixedValue = 777"))
    }

    /**
     * Test explanation with ELLERS block
     */
    @Test
    fun `forklar works when rule uses ELLERS`() {
        class EllersRS(private val value: Int) : AbstractRuleset<Faktum<String>>() {
            override fun create() {
                regel("checkValue") {
                    HVIS { Faktum("input", value) erStørreEnn Faktum("threshold", 10) }
                    SÅ {
                        RETURNER(sporing("result", "high"))
                    }
                    ELLERS {
                        RETURNER(sporing("result", "low"))
                    }
                }
            }
        }

        val highFaktum = EllersRS(20).test()
        val highExplanation = highFaktum.forklar()
        assertTrue(highExplanation.contains("high"))

        val lowFaktum = EllersRS(5).test()
        val lowExplanation = lowFaktum.forklar()
        assertTrue(lowExplanation.contains("low"))
    }

    /**
     * Test explanation doesn't fail with complex expressions
     */
    @Test
    fun `forklar handles complex uttrykk expressions`() {
        class ComplexRS : AbstractRuleset<Faktum<Int>>() {
            override fun create() {
                regel("complex") {
                    HVIS { true }
                    SÅ {
                        val a = Faktum("x", 2)
                        val b = Faktum("y", 3)
                        val c = Faktum("z", 4)
                        val result = sporing("formula", (a + b) * c)
                        RETURNER(result)
                    }
                }
            }
        }

        val faktum = ComplexRS().test()
        val explanation = faktum.forklar()

        assertTrue(explanation.contains("formula"))
        assertTrue(explanation.contains("20")) // (2 + 3) * 4 = 20
    }

    /**
     * Test explanation with domain predicates
     */
    @Test
    fun `forklar includes domain predicate information`() {
        class DomainPredicateRS : AbstractRuleset<Faktum<String>>() {
            override fun create() {
                regel("domainCheck") {
                    HVIS { Faktum("age", 25) erStørreEnn Faktum("minAge", 18) }
                    SÅ {
                        val status = sporing("status", "adult")
                        RETURNER(status)
                    }
                }
            }
        }

        val faktum = DomainPredicateRS().test()
        val explanation = faktum.forklar()

        assertTrue(explanation.contains("status"))
        assertTrue(explanation.contains("adult"))
        assertTrue(explanation.contains("domainCheck"))
    }

    /**
     * Test that explanation indentation works
     */
    @Test
    fun `explanation uses indentation for nested faktum`() {
        class IndentedRS : AbstractRuleset<Faktum<Int>>() {
            override fun create() {
                regel("indented") {
                    HVIS { true }
                    SÅ {
                        val nested = Faktum("inner", 5)
                        val outer = sporing("outer", nested + Faktum("add", 10))
                        RETURNER(outer)
                    }
                }
            }
        }

        val faktum = IndentedRS().test()
        val explanation = faktum.forklar()

        // Check for indentation (spaces at line starts)
        val lines = explanation.lines()
        val hasIndentation = lines.any { it.startsWith("  ") }
        assertTrue(hasIndentation, "Explanation should use indentation")
    }
}
