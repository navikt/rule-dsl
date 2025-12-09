package no.nav.system.ruledsl.core.model.arc

import no.nav.system.ruledsl.core.model.Faktum
import no.nav.system.ruledsl.core.operators.erLik
import no.nav.system.ruledsl.core.operators.erMindreEnn
import no.nav.system.ruledsl.core.operators.erStørreEnn
import no.nav.system.ruledsl.core.pattern.createPattern
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Tests for AbstractRuleset core functionality.
 *
 * Uses simple Int and String values to avoid domain-specific concepts.
 */
class AbstractRulesetTest {

    /**
     * Test that rules execute in sequence order (100, 200, 300, etc.)
     */
    class RuleOrderingRS : AbstractRuleset<String>() {
        private val events = mutableListOf<String>()

        override fun create() {
            regel("third") {
                HVIS { true }
                SÅ {
                    events.add("third")
                }
            }

            regel("first") {
                HVIS { true }
                SÅ {
                    events.add("first")
                }
            }

            regel("second") {
                HVIS { true }
                SÅ {
                    events.add("second")
                }
            }

            regel("return") {
                HVIS { true }
                SÅ {
                    RETURNER(events.joinToString(","))
                }
            }
        }
    }

    @Test
    fun `rules execute in declaration order using sequence numbers`() {
        val result = RuleOrderingRS().test()
        // Rules should execute in order: first, second, third, return
        assertEquals("third,first,second", result)
    }

    /**
     * Test that RETURNER stops ruleset evaluation
     */
    class EarlyReturnRS(private val valueToReturn: Int) : AbstractRuleset<Int>() {
        var executionCount = 0

        override fun create() {
            regel("rule1") {
                HVIS { true }
                SÅ {
                    executionCount++
                    RETURNER(valueToReturn)
                }
            }

            regel("rule2") {
                HVIS { true }
                SÅ {
                    executionCount++
                }
            }

            regel("rule3") {
                HVIS { true }
                SÅ {
                    executionCount++
                }
            }
        }
    }

    @Test
    fun `RETURNER stops further rule evaluation`() {
        val rs = EarlyReturnRS(42)
        val result = rs.test()

        assertEquals(42, result)
        assertEquals(1, rs.executionCount, "Only first rule should execute")
    }

    /**
     * Test rule chaining with harTruffet()
     */
    class RuleChainingRS(private val value: Int) : AbstractRuleset<Int>() {
        private var counter = 0

        override fun create() {
            regel("checkPositive") {
                HVIS { value > 0 }
                SÅ {
                    counter += 10
                }
            }

            regel("chainedRule") {
                HVIS { "checkPositive".harTruffet() }
                SÅ {
                    counter += 5
                }
            }

            regel("return") {
                HVIS { true }
                SÅ {
                    RETURNER(counter)
                }
            }
        }
    }

    @Test
    fun `harTruffet returns true when referenced rule fired`() {
        val result = RuleChainingRS(10).test()
        assertEquals(15, result, "Both rules should execute: 10 + 5")
    }

    @Test
    fun `harTruffet returns false when referenced rule did not fire`() {
        val result = RuleChainingRS(-10).test()
        assertEquals(0, result, "Chained rule should not execute")
    }

    /**
     * Test harIkkeTruffet()
     */
    class NegativeRuleChainingRS(private val value: Int) : AbstractRuleset<Int>() {
        private var counter = 0

        override fun create() {
            regel("checkNegative") {
                HVIS { value < 0 }
                SÅ {
                    counter += 10
                }
            }

            regel("whenNotNegative") {
                HVIS { "checkNegative".harIkkeTruffet() }
                SÅ {
                    counter += 20
                }
            }

            regel("return") {
                HVIS { true }
                SÅ {
                    RETURNER(counter)
                }
            }
        }
    }

    @Test
    fun `harIkkeTruffet returns true when referenced rule did not fire`() {
        val result = NegativeRuleChainingRS(5).test()
        assertEquals(20, result, "Only whenNotNegative should execute")
    }

    @Test
    fun `harIkkeTruffet returns false when referenced rule fired`() {
        val result = NegativeRuleChainingRS(-5).test()
        assertEquals(10, result, "Only checkNegative should execute")
    }

    /**
     * Test pattern-based rules
     */
    class PatternRuleRS(private val numbers: List<Int>) : AbstractRuleset<Int>() {
        private var sum = 0

        override fun create() {
            val pattern = numbers.createPattern()

            regel("processNumber", pattern) { num ->
                HVIS { num > 0 }
                SÅ {
                    sum += num
                }
            }

            regel("return") {
                HVIS { true }
                SÅ {
                    RETURNER(sum)
                }
            }
        }
    }

    @Test
    fun `pattern rules execute for each element in pattern`() {
        val result = PatternRuleRS(listOf(5, -3, 10, -1, 7)).test()
        assertEquals(22, result, "Sum of positive numbers: 5 + 10 + 7")
    }

    @Test
    fun `empty pattern creates single rule that does nothing`() {
        val result = PatternRuleRS(emptyList()).test()
        assertEquals(0, result)
    }

    /**
     * Test HVIS and OG predicates
     */
    class MultiplePredicatesRS(private val value: Int) : AbstractRuleset<String>() {
        override fun create() {
            regel("checkBounds") {
                HVIS { value >= 0 }
                OG { value <= 100 }
                SÅ {
                    RETURNER("valid")
                }
                ELLERS {
                    RETURNER("invalid")
                }
            }
        }
    }

    @Test
    fun `both HVIS and OG must be true for rule to fire`() {
        assertEquals("valid", MultiplePredicatesRS(50).test())
        assertEquals("invalid", MultiplePredicatesRS(-10).test())
        assertEquals("invalid", MultiplePredicatesRS(150).test())
    }

    /**
     * Test domain predicates with Faktum
     */
    class DomainPredicateRS(private val age: Int) : AbstractRuleset<String>() {
        override fun create() {
            regel("checkAdult") {
                HVIS { Faktum("age", age) erStørreEnn Faktum("minAge", 18) }
                SÅ {
                    RETURNER("adult")
                }
                ELLERS {
                    RETURNER("minor")
                }
            }
        }
    }

    @Test
    fun `domain predicates work with Faktum`() {
        assertEquals("adult", DomainPredicateRS(25).test())
        assertEquals("minor", DomainPredicateRS(15).test())
    }

    /**
     * Test that ruleset without return value throws ClassCastException when used
     */
    class NoReturnRS : AbstractRuleset<Int>() {
        override fun create() {
            regel("someRule") {
                HVIS { true }
                SÅ {
                    // No RETURNER
                }
            }
        }
    }

    @Test
    fun `ruleset with non-Unit type but no return value throws when value is used`() {
        assertThrows<ClassCastException> {
            NoReturnRS().test() + 1
        }
    }

    /**
     * Test Unit ruleset (no return value expected)
     */
    class UnitRulesetRS : AbstractRuleset<Unit>() {
        var executed = false

        override fun create() {
            regel("doSomething") {
                HVIS { true }
                SÅ {
                    executed = true
                }
            }
        }
    }

    @Test
    fun `Unit ruleset executes without requiring RETURNER`() {
        val rs = UnitRulesetRS()
        rs.test()
        assertEquals(true, rs.executed)
    }

    /**
     * Test pattern-specific harTruffet
     */
    class PatternElementChainingRS(private val values: List<String>) : AbstractRuleset<Int>() {
        private var count = 0

        override fun create() {
            val pattern = values.createPattern()

            regel("processString", pattern) { str ->
                HVIS { str.length > 3 }
                SÅ {
                    // Rule fires for this element
                }
            }

            regel("countLong", pattern) { str ->
                HVIS { "processString".harTruffet(str) }
                SÅ {
                    count++
                }
            }

            regel("return") {
                HVIS { true }
                SÅ {
                    RETURNER(count)
                }
            }
        }
    }

    @Test
    fun `pattern element specific harTruffet checks individual element firing`() {
        val result = PatternElementChainingRS(listOf("hi", "hello", "a", "world")).test()
        assertEquals(2, result, "Two strings have length > 3: 'hello' and 'world'")
    }

    /**
     * Test multiple technical predicates (multiple OG)
     */
    class MultipleTechnicalPredicatesRS(private val value: Int) : AbstractRuleset<String>() {
        override fun create() {
            regel("multiCheck") {
                HVIS { value > 0 }
                OG { value % 2 == 0 }
                OG { value < 100 }
                SÅ {
                    RETURNER("pass")
                }
                ELLERS {
                    RETURNER("fail")
                }
            }
        }
    }

    @Test
    fun `multiple technical predicates all must be true`() {
        assertEquals("pass", MultipleTechnicalPredicatesRS(50).test())
        assertEquals("fail", MultipleTechnicalPredicatesRS(51).test()) // Odd
        assertEquals("fail", MultipleTechnicalPredicatesRS(-2).test()) // Negative
        assertEquals("fail", MultipleTechnicalPredicatesRS(150).test()) // Too large
    }

    /**
     * Test mixed technical and domain predicates
     */
    class MixedPredicatesRS(private val value: Int, private val name: String) : AbstractRuleset<String>() {
        override fun create() {
            regel("mixedCheck") {
                HVIS { name.isNotEmpty() }
                HVIS { Faktum("value", value) erMindreEnn Faktum("max", 100) }
                SÅ {
                    RETURNER("$name:$value")
                }
                ELLERS {
                    RETURNER("invalid")
                }
            }
        }
    }

    @Test
    fun `mixed technical and domain predicates work together`() {
        assertEquals("test:50", MixedPredicatesRS(50, "test").test())
        assertEquals("invalid", MixedPredicatesRS(150, "test").test())
        assertEquals("invalid", MixedPredicatesRS(50, "").test())
    }

    /**
     * Test kommentar() method
     */
    class RuleWithCommentRS : AbstractRuleset<Int>() {
        override fun create() {
            regel("documented") {
                HVIS { true }
                SÅ {
                    RETURNER(42)
                }
                kommentar("This rule always returns 42")
            }
        }
    }

    @Test
    fun `rules can have comments without affecting execution`() {
        assertEquals(42, RuleWithCommentRS().test())
    }
}
