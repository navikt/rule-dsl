package no.nav.system.ruledsl.core.trace

import no.nav.system.ruledsl.core.expression.Faktum
import no.nav.system.ruledsl.core.expression.boolean.erMindreEnn
import no.nav.system.ruledsl.core.expression.math.times
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RulesetTest {

    data class TestUser(val age: Int, val trygdetid: Int, val hasOption: Boolean = false)

    @Test
    fun `regel with SÅ executes side effect`() {
        val trace = Trace("test")
        var sideEffectExecuted = false

        with(trace) {
            traced<Unit> {
                regel("side effect rule") {
                    HVIS { true }
                    SÅ {
                        sideEffectExecuted = true
                    }
                }
            }
        }

        assertTrue(sideEffectExecuted)
    }

    @Test
    fun `regel with RETURNER returns Faktum`() {
        val trace = Trace("test")

        val result = with(trace) {
            traced<Faktum<Int>> {
                regel("return rule") {
                    HVIS { true }
                    RETURNER {
                        Faktum("result", 42)
                    }
                }
            }
        }

        assertEquals(42, result.value)
        assertEquals("result", result.name)
    }

    @Test
    fun `first matching rule wins`() {
        val trace = Trace("test")

        val result = with(trace) {
            traced<Faktum<Int>> {
                regel("first rule") {
                    HVIS { true }
                    RETURNER {
                        Faktum("first", 1)
                    }
                }

                regel("explosive rule") {
                    HVIS { null!! }
                    RETURNER {
                        Faktum("second", 2)
                    }
                }
            }
        }

        assertEquals(1, result.value)
        assertEquals("first", result.name)
    }

    @Test
    fun `rule with false predicate does not fire`() {
        val trace = Trace("test")

        val result = with(trace) {
            traced<Faktum<Int>> {
                regel("false rule") {
                    HVIS { false }
                    RETURNER {
                        Faktum("should not match", 1)
                    }
                }

                regel("true rule") {
                    HVIS { true }
                    RETURNER {
                        Faktum("should match", 2)
                    }
                }
            }
        }

        assertEquals(2, result.value)
    }

    @Test
    fun `throws when no rule matches`() {
        val trace = Trace("test")

        assertThrows<IllegalStateException> {
            with(trace) {
                traced<Faktum<Int>> {
                    regel("never matches") {
                        HVIS { false }
                        RETURNER {
                            Faktum("result", 1)
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `technical predicate short-circuits on false`() {
        val trace = Trace("test")
        var secondPredicateEvaluated = false

        with(trace) {
            traced<Unit> {
                regel("short circuit rule") {
                    HVIS { false }
                    SÅ {
                        secondPredicateEvaluated = true
                    }
                }
            }
        }

        assertFalse(secondPredicateEvaluated)
    }

    @Test
    fun `domain predicate with erMindreEnn`() {
        val trace = Trace("test")
        val user = TestUser(age = 25, trygdetid = 14)

        val result = with(trace) {
            traced<Faktum<Int>> {
                regel("age check") {
                    HVIS { user.age erMindreEnn 30 }
                    RETURNER {
                        Faktum("young", user.age)
                    }
                }
            }
        }

        assertEquals(25, result.value)
    }

    @Test
    fun `formula is traced in RETURNER`() {
        val trace = Trace("test")
        val sats = Faktum("sats", 1000)
        val faktor = Faktum("faktor", 2)

        with(trace) {
            traced<Faktum<Int>> {
                regel("calculation") {
                    HVIS { true }
                    RETURNER {
                        Faktum("result", sats * faktor)
                    }
                }
            }
        }

        val formulas = trace.root.children.first().formulas
        assertEquals(1, formulas.size)
        assertEquals("result", formulas.first().name)
    }

    @Test
    fun `SPOR explicitly traces Faktum in SÅ block`() {
        val trace = Trace("test")
        var tracedFaktum: Faktum<Int>? = null

        with(trace) {
            traced<Unit> {
                regel("spor test") {
                    HVIS { true }
                    SÅ {
                        tracedFaktum = SPOR(Faktum("traced", 123))
                    }
                }
            }
        }

        assertEquals(123, tracedFaktum?.value)
        val formulas = trace.root.children.first().formulas
        assertEquals(1, formulas.size)
        assertEquals("traced", formulas.first().name)
    }

    @Test
    fun `nested traced calls preserve hierarchy`() {
        val trace = Trace("test")

        context(trace: Trace)
        fun innerCalculation(): Faktum<Int> = traced<Faktum<Int>> {
            regel("inner rule") {
                HVIS { true }
                RETURNER {
                    Faktum("inner result", 100)
                }
            }
        }

        with(trace) {
            traced<Faktum<Int>> {
                regel("outer rule") {
                    HVIS { true }
                    RETURNER {
                        innerCalculation()
                    }
                }
            }
        }

        val outerRule = trace.root.children.first()
        assertEquals("outer rule", outerRule.name)

        val innerRule = outerRule.children.first()
        assertEquals("inner rule", innerRule.name)
    }

    @Test
    fun `throws when both SÅ and RETURNER used in same rule`() {
        val trace = Trace("test")

        assertThrows<IllegalStateException> {
            with(trace) {
                traced<Faktum<Int>> {
                    regel("invalid rule") {
                        HVIS { true }
                        SÅ { }
                        RETURNER {
                            Faktum("result", 1)
                        }
                    }
                }
            }
        }
    }

    // Resource test support
    class TestRateResource(val rate: Int)

    @Test
    fun `resources can be registered and accessed in SÅ block`() {
        val trace = Trace("test")
        trace.putResource(TestRateResource::class, TestRateResource(1000))

        var accessedRate = 0

        with(trace) {
            traced<Unit> {
                regel("resource access") {
                    HVIS { true }
                    SÅ {
                        accessedRate = getResource(TestRateResource::class).rate
                    }
                }
            }
        }

        assertEquals(1000, accessedRate)
    }

    @Test
    fun `resources can be accessed in RETURNER block`() {
        val trace = Trace("test")
        trace.putResource(TestRateResource::class, TestRateResource(500))

        val result = with(trace) {
            traced<Faktum<Int>> {
                regel("resource calculation") {
                    HVIS { true }
                    RETURNER {
                        val rate = getResource(TestRateResource::class).rate
                        Faktum("calculated", rate * 2)
                    }
                }
            }
        }

        assertEquals(1000, result.value)
    }

    @Test
    fun `extension functions on ResourceAccessor work in SÅ block`() {
        val trace = Trace("test")
        trace.putResource(TestRateResource::class, TestRateResource(750))

        // Extension function on ResourceAccessor
        fun Rule<*>.testRate(): Int = getResource(TestRateResource::class).rate

        var accessedRate = 0

        with(trace) {
            traced<Unit> {
                regel("extension access") {
                    HVIS { true }
                    SÅ {
                        accessedRate = testRate()
                    }
                }
            }
        }

        assertEquals(750, accessedRate)
    }

    @Test
    fun `regel with pattern creates rule per element`() {
        val trace = Trace("test")
        val items = listOf("A", "B", "C")
        val processedItems = mutableListOf<String>()

        with(trace) {
            traced<Unit> {
                regel("process item", items) { item ->
                    HVIS { true }
                    SÅ {
                        processedItems.add(item)
                    }
                }
            }
        }

        assertEquals(listOf("A", "B", "C"), processedItems)
        // Trace should show process item.1, process item.2, process item.3
        val debugOutput = trace.debugTree()
        assertTrue(debugOutput.contains("process item.1"))
        assertTrue(debugOutput.contains("process item.2"))
        assertTrue(debugOutput.contains("process item.3"))
    }

    @Test
    fun `regel with pattern and RETURNER stops at first match`() {
        val trace = Trace("test")
        val numbers = listOf(5, 10, 15, 20)

        val result = with(trace) {
            traced<Faktum<Int>> {
                regel("find first over 10", numbers) { num ->
                    HVIS { num > 10 }
                    RETURNER {
                        Faktum("found", num)
                    }
                }
            }
        }

        // Should find 15 (first > 10)
        assertEquals(15, result.value)
    }

    @Test
    fun `regel with pattern accumulates values`() {
        val trace = Trace("test")
        data class Period(val months: Int)
        val periods = listOf(Period(12), Period(24), Period(6))
        var totalMonths = 0

        with(trace) {
            traced<Unit> {
                regel("sum periods", periods) { period ->
                    HVIS { true }
                    SÅ {
                        totalMonths += period.months
                    }
                }
            }
        }

        assertEquals(42, totalMonths)
    }
}
