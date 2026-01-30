package no.nav.system.ruledsl.core.trace

import no.nav.system.ruledsl.core.expression.Faktum
import no.nav.system.ruledsl.core.expression.boolean.erLik
import no.nav.system.ruledsl.core.expression.boolean.erMindreEnn
import no.nav.system.ruledsl.core.expression.boolean.erStørreEllerLik
import no.nav.system.ruledsl.core.expression.math.div
import no.nav.system.ruledsl.core.expression.math.plus
import no.nav.system.ruledsl.core.expression.math.times

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RulesetTest {

    data class TestUser(val age: Int, val trygdetid: Int, val hasOption: Boolean = false)


    @Test
    fun `regel with SÅ executes side effect`() {
        val ruleContext = RuleContext(
            mutableMapOf(Tracer::class to DefaultTracer("test"))
        )
        var sideEffectExecuted = false

        with(ruleContext) {
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
        val ruleContext = RuleContext(
            mutableMapOf(Tracer::class to DefaultTracer("test"))
        )

        val result = with(ruleContext) {
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
        val ruleContext = RuleContext(
            mutableMapOf(Tracer::class to DefaultTracer("test"))
        )

        val result = with(ruleContext) {
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
        val ruleContext = RuleContext(
            mutableMapOf(Tracer::class to DefaultTracer("test"))
        )

        val result = with(ruleContext) {
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
        val ruleContext = RuleContext(
            mutableMapOf(Tracer::class to DefaultTracer("test"))
        )

        assertThrows<IllegalStateException> {
            with(ruleContext) {
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
        val ruleContext = RuleContext(
            mutableMapOf(Tracer::class to DefaultTracer("test"))
        )
        var secondPredicateEvaluated = false

        with(ruleContext) {
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
        val ruleContext = RuleContext(
            mutableMapOf(Tracer::class to DefaultTracer("test"))
        )
        val user = TestUser(age = 25, trygdetid = 14)

        val result = with(ruleContext) {
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
        val ruleContext = RuleContext(
            mutableMapOf(Tracer::class to DefaultTracer("test"))
        )
        val sats = Faktum("sats", 1000)
        val faktor = Faktum("faktor", 2)

        with(ruleContext) {
            traced<Faktum<Int>> {
                regel("calculation") {
                    HVIS { true }
                    RETURNER {
                        Faktum("result", sats * faktor)
                    }
                }
            }
        }

        val expressions = ruleContext.root().children.first().expressions.filterIsInstance<Faktum<*>>()
        assertEquals(1, expressions.size)
        assertEquals("result", expressions.first().name)
    }

    @Test
    fun `SPOR explicitly traces Faktum in SÅ block`() {
        val ruleContext = RuleContext(
            mutableMapOf(Tracer::class to DefaultTracer("test"))
        )
        var tracedFaktum: Faktum<Int>? = null

        with(ruleContext) {
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
        val expressions = ruleContext.root().children.first().expressions.filterIsInstance<Faktum<*>>()
        assertEquals(1, expressions.size)
        assertEquals("traced", expressions.first().name)
    }

    @Test
    fun `nested traced calls preserve hierarchy`() {
        val ruleContext = RuleContext(
            mutableMapOf(Tracer::class to DefaultTracer("test"))
        )

        context(ruleContext: RuleContext)
        fun innerCalculation(): Faktum<Int> = traced<Faktum<Int>> {
            regel("inner rule") {
                HVIS { true }
                RETURNER {
                    Faktum("inner result", 100)
                }
            }
        }

        with(ruleContext) {
            traced<Faktum<Int>> {
                regel("outer rule") {
                    HVIS { true }
                    RETURNER {
                        innerCalculation()
                    }
                }
            }
        }

        val outerRule = ruleContext.root().children.first()
        assertEquals("outer rule", outerRule.name)

        val innerRule = outerRule.children.first()
        assertEquals("inner rule", innerRule.name)
    }

    @Test
    fun `throws when both SÅ and RETURNER used in same rule`() {
        val ruleContext = RuleContext(
            mutableMapOf(Tracer::class to DefaultTracer("test"))
        )

        assertThrows<IllegalStateException> {
            with(ruleContext) {
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
        val ruleContext = RuleContext(
            mutableMapOf(Tracer::class to DefaultTracer("test"))
        )
        ruleContext.putResource(TestRateResource::class, TestRateResource(1000))

        var accessedRate = 0

        with(ruleContext) {
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
        val ruleContext = RuleContext(
            mutableMapOf(Tracer::class to DefaultTracer("test"))
        )
        ruleContext.putResource(TestRateResource::class, TestRateResource(500))

        val result = with(ruleContext) {
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
        val ruleContext = RuleContext(
            mutableMapOf(Tracer::class to DefaultTracer("test"))
        )
        ruleContext.putResource(TestRateResource::class, TestRateResource(750))

        // Extension function on ResourceAccessor
        fun Rule<*>.testRate(): Int = getResource(TestRateResource::class).rate

        var accessedRate = 0

        with(ruleContext) {
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
        val ruleContext = RuleContext(
            mutableMapOf(Tracer::class to DefaultTracer("test"))
        )
        val items = listOf("A", "B", "C")
        val processedItems = mutableListOf<String>()

        with(ruleContext) {
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
        val debugOutput = ruleContext.debugTree()
        assertTrue(debugOutput.contains("process item.1"))
        assertTrue(debugOutput.contains("process item.2"))
        assertTrue(debugOutput.contains("process item.3"))
    }

    @Test
    fun `regel with pattern and RETURNER stops at first match`() {
        val ruleContext = RuleContext(
            mutableMapOf(Tracer::class to DefaultTracer("test"))
        )
        val numbers = listOf(5, 10, 15, 20)

        val result = with(ruleContext) {
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
        val ruleContext = RuleContext(
            mutableMapOf(Tracer::class to DefaultTracer("test"))
        )

        data class Period(val months: Int)

        val periods = listOf(Period(12), Period(24), Period(6))
        var totalMonths = 0

        with(ruleContext) {
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

    @Test
    fun `RuleExpression can be used for introspection in subsequent rules`() {
        val ruleContext = RuleContext(
            mutableMapOf(Tracer::class to DefaultTracer("test"))
        )

        val result = with(ruleContext) {
            traced<Faktum<String>> {
                val r1 = regel("check condition") {
                    HVIS { true }
                    SÅ { }
                }

                regel("uses r1") {
                    HVIS { r1 }  // RuleExpression as Expression<Boolean>
                    RETURNER {
                        Faktum("result", "r1 fired")
                    }
                }
            }
        }

        assertEquals("r1 fired", result.value)
        // Check trace contains the introspection info
        val debugOutput = ruleContext.debugTree()
        assertTrue(debugOutput.contains("regel 'check condition'"))
    }

    @Test
    fun `RuleExpression shows not fired when rule condition fails`() {
        val ruleContext = RuleContext(
            mutableMapOf(Tracer::class to DefaultTracer("test"))
        )

        val result = with(ruleContext) {
            traced<Faktum<String>> {
                val r1 = regel("check condition") {
                    HVIS { false }  // Will not fire
                    SÅ { }
                }

                regel("uses r1") {
                    HVIS { r1 }  // r1 didn't fire, so this is false
                    RETURNER {
                        Faktum("result", "r1 fired")
                    }
                }

                regel("fallback") {
                    HVIS { true }
                    RETURNER {
                        Faktum("result", "fallback")
                    }
                }
            }
        }

        assertEquals("fallback", result.value)
    }

    @Test
    fun `pattern rules can be introspected with minstEnHarTruffet`() {
        val ruleContext = RuleContext(
            mutableMapOf(Tracer::class to DefaultTracer("test"))
        )
        val items = listOf(1, 2, 3, 10, 20)

        val result = with(ruleContext) {
            traced<Faktum<String>> {
                val overTen = regel("over ten", items) { item ->
                    HVIS { item > 10 }
                    SÅ { }
                }

                regel("decision") {
                    HVIS { overTen.minstEnHarTruffet() }
                    RETURNER {
                        Faktum("result", "found items over 10")
                    }
                }

                regel("fallback") {
                    HVIS { true }
                    RETURNER {
                        Faktum("result", "none over 10")
                    }
                }
            }
        }

        assertEquals("found items over 10", result.value)
    }

    @Test
    fun `pattern rules can be introspected with ingenHarTruffet`() {
        val ruleContext = RuleContext(
            mutableMapOf(Tracer::class to DefaultTracer("test"))
        )
        val items = listOf(1, 2, 3, 5)  // None over 10

        val result = with(ruleContext) {
            traced<Faktum<String>> {
                val overTen = regel("over ten", items) { item ->
                    HVIS { item > 10 }
                    SÅ { }
                }

                regel("none found") {
                    HVIS { overTen.ingenHarTruffet() }
                    RETURNER {
                        Faktum("result", "no items over 10")
                    }
                }

                regel("fallback") {
                    HVIS { true }
                    RETURNER {
                        Faktum("result", "some over 10")
                    }
                }
            }
        }

        assertEquals("no items over 10", result.value)
    }

    @Test
    fun `forklar produces inverse explanation from result Faktum`() {
        val ruleContext = RuleContext(
            mutableMapOf(Tracer::class to DefaultTracer("test"))
        )
        val user = TestUser(age = 25, trygdetid = 30)

        val result = with(ruleContext) {
            traced<Faktum<Double>> {
                val factor = Faktum("factor", user.trygdetid / 40.0)
                val base = Faktum("base", 1000)

                regel("calculate pension") {
                    HVIS { user.age erMindreEnn 70 }
                    RETURNER {
                        Faktum("pension", base * factor)
                    }
                }
            }
        }

        val explanation = result.forklar()
        println("=== Inverse Explanation ===")
        println(explanation)

        // Should contain HVA section with result
        assertTrue(explanation.contains("HVA"), "Should have HVA section")
        assertTrue(explanation.contains("pension"), "Should show result name")

        // Should contain HVORFOR section with fired rules
        assertTrue(explanation.contains("HVORFOR"), "Should have HVORFOR section")
        assertTrue(explanation.contains("calculate pension"), "Should show rule that produced result")

        // Should contain HVORDAN section with formula
        assertTrue(explanation.contains("HVORDAN"), "Should have HVORDAN section")
        assertTrue(explanation.contains("base * factor"), "Should show formula notation")
    }

    @Test
    fun `forklar with nested rules shows full decision path`() {
        val ruleContext = RuleContext(
            mutableMapOf(Tracer::class to DefaultTracer("test"))
        )

        val result = with(ruleContext) {
            traced<Faktum<String>> {
                val eligibility = regel("check eligibility") {
                    HVIS { true }
                    SÅ { }
                }

                regel("grant benefit") {
                    HVIS { eligibility }
                    RETURNER {
                        Faktum("benefit", "granted")
                    }
                }
            }
        }

        val explanation = result.forklar()
        println("=== Nested Rules Explanation ===")
        println(explanation)

        // Should show the dependency chain
        assertTrue(explanation.contains("check eligibility"), "Should show prerequisite rule")
        assertTrue(explanation.contains("grant benefit"), "Should show producing rule")
    }

    @Test
    fun `forklar with TraceFilter ALL shows non-fired rules too`() {
        val ruleContext = RuleContext(
            mutableMapOf(Tracer::class to DefaultTracer("test"))
        )

        val result = with(ruleContext) {
            traced<Faktum<String>> {
                regel("first option") {
                    HVIS { false }  // Won't fire
                    RETURNER {
                        Faktum("result", "first")
                    }
                }

                regel("second option") {
                    HVIS { true }  // Will fire
                    RETURNER {
                        Faktum("result", "second")
                    }
                }
            }
        }

        val functionalExplanation = result.forklar(TraceFilter.FUNCTIONAL)
        val allExplanation = result.forklar(TraceFilter.ALL)

        println("=== FUNCTIONAL filter ===")
        println(functionalExplanation)
        println("=== ALL filter ===")
        println(allExplanation)

        // FUNCTIONAL should not show the non-fired rule in HVORFOR
        // ALL should include more detail
        assertTrue(
            allExplanation.length >= functionalExplanation.length,
            "ALL filter should produce at least as much output"
        )
    }

    @Test
    fun `forklar recursively explains Faktum dependencies in predicates`() {
        // Simulates:
        // - First compute "oppfylt" based on score >= threshold
        // - Then use oppfylt in a predicate to decide which rule to fire
        // - When explaining answer, we should see WHY oppfylt was true

        val ruleContext = RuleContext(
            mutableMapOf(Tracer::class to DefaultTracer("test"))
        )

        // First, compute oppfylt in a separate traced context
        val oppfylt: Faktum<Boolean> = with(ruleContext) {
            traced<Faktum<Boolean>> {
                val score = Faktum("score", 75)
                val threshold = Faktum("threshold", 50)

                regel("vilkårsvurdering") {
                    HVIS { score erStørreEllerLik threshold }
                    RETURNER {
                        Faktum("oppfylt", true)
                    }
                }

                regel("ikke oppfylt") {
                    HVIS { score erMindreEnn threshold }
                    RETURNER {
                        Faktum("oppfylt", false)
                    }
                }
            }
        }

        // Now use oppfylt in predicates for the calculation
        val answer: Faktum<Int> = with(ruleContext) {
            traced<Faktum<Int>> {
                // Use oppfylt in a domain predicate
                regel("beregn positiv ytelse") {
                    HVIS { oppfylt erLik true }
                    RETURNER {
                        Faktum("svar", 1000)
                    }
                }

                regel("beregn null ytelse") {
                    HVIS { oppfylt erLik false }
                    RETURNER {
                        Faktum("svar", 0)
                    }
                }
            }
        }

        val explanation = answer.forklar()
        println("=== Recursive Faktum Dependency Explanation ===")
        println(explanation)

        // Should show HVA (result)
        assertTrue(explanation.contains("svar = 1000"), "Should show result value")

        // Should show HVORFOR with the fired rule
        assertTrue(explanation.contains("beregn positiv ytelse"), "Should show producing rule")

        // Should show AVHENGER AV section with the bool dependency
        assertTrue(
            explanation.contains("AVHENGER AV") || explanation.contains("oppfylt"),
            "Should show dependency on 'oppfylt' Faktum that determined rule firing"
        )

        // Should recursively explain how bool was computed
        assertTrue(
            explanation.contains("vilkårsvurdering"),
            "Should show the rule that produced the bool dependency"
        )
    }

    @Test
    fun `debugTree shows integrated rules predicates and formula hierarchy`() {
        val ruleContext = RuleContext(
            mutableMapOf(Tracer::class to DefaultTracer("pension-calculation"))
        )

        val result = with(ruleContext) {
            traced<Faktum<Double>> {
                // Input facts
                val grunnbeløp = Faktum("grunnbeløp", 118620)
                val satsFaktor = Faktum("satsFaktor", 0.66)
                val trygdetid = Faktum("trygdetid", 35)
                val fullTrygdetid = Faktum("fullTrygdetid", 40)
                val alder = Faktum("alder", 67)

                // Intermediate calculations
                val grunnpensjon = Faktum("grunnpensjon", grunnbeløp * satsFaktor)
                val trygdetidFaktor = Faktum("trygdetidFaktor", trygdetid / fullTrygdetid)

                regel("calculate pension") {
                    HVIS { alder erStørreEllerLik 62 }
                    OG { trygdetid erStørreEllerLik 3 }
                    RETURNER {
                        Faktum("pensjon", grunnpensjon * trygdetidFaktor)
                    }
                }
            }
        }

        println("=== Integrated debugTree ===")
        println(ruleContext.debugTree())

        val tree = ruleContext.debugTree()

        // Should show the rule
        assertTrue(tree.contains("calculate pension"))

        // Should show predicates
        assertTrue(tree.contains("alder"))
        assertTrue(tree.contains("trygdetid"))

        // Should show formula hierarchy
        assertTrue(tree.contains("pensjon ="))
        assertTrue(tree.contains("notation: grunnpensjon * trygdetidFaktor"))
        assertTrue(tree.contains("grunnpensjon ="))
        assertTrue(tree.contains("notation: grunnbeløp * satsFaktor"))
    }
}
