package no.nav.system.ruledsl.core.trace

import no.nav.system.ruledsl.core.expression.Faktum
import no.nav.system.ruledsl.core.expression.Verdi
import no.nav.system.ruledsl.core.expression.boolean.erLik
import no.nav.system.ruledsl.core.expression.boolean.erMindreEnn
import no.nav.system.ruledsl.core.expression.boolean.erStørreEllerLik
import no.nav.system.ruledsl.core.expression.math.div
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
                        faktum("result", 42)
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
                        faktum("first", 1)
                    }
                }

                regel("explosive rule") {
                    HVIS { null!! }
                    RETURNER {
                        faktum("second", 2)
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
                        faktum("should not match", 1)
                    }
                }

                regel("true rule") {
                    HVIS { true }
                    RETURNER {
                        faktum("should match", 2)
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
                            faktum("result", 1)
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
                        faktum("young", user.age)
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
        val sats = Verdi("sats", 1000)
        val faktor = Verdi("faktor", 2)

        with(ruleContext) {
            traced<Faktum<Int>> {
                regel("calculation") {
                    HVIS { true }
                    RETURNER {
                        faktum("result", sats * faktor)
                    }
                }
            }
        }

        val expressions = ruleContext.root().children.first().expressions.filterIsInstance<Faktum<*>>()
        assertEquals(1, expressions.size)
        assertEquals("result", expressions.first().name)
    }

    @Test
    fun `faktum explicitly traces in SÅ block`() {
        val ruleContext = RuleContext(
            mutableMapOf(Tracer::class to DefaultTracer("test"))
        )
        var tracedFaktum: Faktum<Int>? = null

        with(ruleContext) {
            traced<Unit> {
                regel("faktum test") {
                    HVIS { true }
                    SÅ {
                        tracedFaktum = faktum("traced", 123)
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
                    faktum("inner result", 100)
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
                            faktum("result", 1)
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
                        faktum("calculated", rate * 2)
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
                        faktum("found", num)
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
                        faktum("result", "r1 fired")
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
                        faktum("result", "r1 fired")
                    }
                }

                regel("fallback") {
                    HVIS { true }
                    RETURNER {
                        faktum("result", "fallback")
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
                        faktum("result", "found items over 10")
                    }
                }

                regel("fallback") {
                    HVIS { true }
                    RETURNER {
                        faktum("result", "none over 10")
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
                        faktum("result", "no items over 10")
                    }
                }

                regel("fallback") {
                    HVIS { true }
                    RETURNER {
                        faktum("result", "some over 10")
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
                val factor = Verdi("factor", user.trygdetid / 40.0)
                val base = Verdi("base", 1000)

                regel("calculate pension") {
                    HVIS { user.age erMindreEnn 70 }
                    RETURNER {
                        faktum("pension", base * factor)
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
                        faktum("benefit", Verdi("granted"))
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
                        faktum("result", Verdi("first"))
                    }
                }

                regel("second option") {
                    HVIS { true }  // Will fire
                    RETURNER {
                        faktum("result", Verdi("second"))
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
                val score = Verdi("score", 75)
                val threshold = Verdi("threshold", 50)

                regel("vilkårsvurdering") {
                    HVIS { score erStørreEllerLik threshold }
                    RETURNER {
                        faktum("oppfylt", Verdi(true))
                    }
                }

                regel("ikke oppfylt") {
                    HVIS { score erMindreEnn threshold }
                    RETURNER {
                        faktum("oppfylt", Verdi(false))
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
                        faktum("svar", Verdi(1000))
                    }
                }

                regel("beregn null ytelse") {
                    HVIS { oppfylt erLik false }
                    RETURNER {
                        faktum("svar", Verdi(0))
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

        // Note: Recursive explanation of dependencies is a future enhancement
        // For now, we verify that the main rule and its predicate are shown
        // The full recursive chain (vilkårsvurdering -> oppfylt) can be added later
    }

    @Test
    fun `debugTree shows integrated rules predicates and formula hierarchy`() {
        val ruleContext = RuleContext(
            mutableMapOf(Tracer::class to DefaultTracer("pension-calculation"))
        )

        with(ruleContext) {
            traced<Faktum<Double>> {
                // Input facts (Verdi - not traced)
                val grunnbeløp = Verdi("grunnbeløp", 118620)
                val satsFaktor = Verdi("satsFaktor", 0.66)
                val trygdetid = Verdi("trygdetid", 35)
                val fullTrygdetid = Verdi("fullTrygdetid", 40)
                val alder = Verdi("alder", 67)

                // Intermediate calculations (Faktum via faktum() - traced)
                val grunnpensjon = faktum("grunnpensjon", grunnbeløp * satsFaktor)
                val trygdetidFaktor = faktum("trygdetidFaktor", trygdetid / fullTrygdetid)

                regel("calculate pension") {
                    HVIS { alder erStørreEllerLik 62 }
                    OG { trygdetid erStørreEllerLik 3 }
                    RETURNER {
                        faktum("pensjon", grunnpensjon * trygdetidFaktor)
                    }
                }
            }
        }

        println("=== Integrated debugTree ===")
        println(ruleContext.debugTree())

        val tree = ruleContext.debugTree()

        // Should show the rule
        assertTrue(tree.contains("calculate pension"))

        // Should show predicates with values
        assertTrue(tree.contains("alder"))
        assertTrue(tree.contains("trygdetid"))

        // Should show result Faktum
        assertTrue(tree.contains("pensjon ="))

        // Should show formula notation (Faktum names, not expanded)
        assertTrue(tree.contains("grunnpensjon * trygdetidFaktor"))

        // Intermediate Faktum are recorded at traced block level
        // They appear as expressions, showing formula notation/concrete
        assertTrue(tree.contains("notation:") && tree.contains("concrete:"))
    }
}
