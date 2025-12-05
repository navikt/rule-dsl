package no.nav.system.rule.dsl.tracker

import no.nav.system.ruledsl.core.model.AbstractRuleset
import no.nav.system.ruledsl.core.model.DslDomainPredicate
import no.nav.system.ruledsl.core.resource.tracker.forklar
import no.nav.system.ruledsl.core.model.Faktum
import no.nav.system.ruledsl.core.operators.erStørreEllerLik
import no.nav.system.ruledsl.core.operators.plus
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TrackerExtensionsTest {

    @OptIn(DslDomainPredicate::class)
    class TestRuleset(private val alderInput: Int) : AbstractRuleset<Unit>() {
        lateinit var aldersvilkår: Faktum<Boolean>

        override fun create() {
            val alder = sporing<Int>("alder", alderInput)
            val aldersgrense = sporing<Int>("aldersgrense", 67)

            regel("VILKÅR-ALDER") {
                HVIS { alder erStørreEllerLik aldersgrense }
                SÅ { aldersvilkår = sporing("Aldersvilkår", true) }
                ELLERS { aldersvilkår = sporing("Aldersvilkår", false) }
            }
        }
    }

    @Test
    fun `forklar produces expected output`() {
        val ruleset = TestRuleset(70)
        ruleset.test()

        val explanation = ruleset.aldersvilkår.forklar()

        println("=== forklar() output ===")
        println(explanation)

        assertTrue(explanation.contains("HVA:"))
        assertTrue(explanation.contains("Aldersvilkår = true"))
        assertTrue(explanation.contains("HVORFOR"))
    }

    @Test
    fun `forklar output format check`() {
        val ruleset = TestRuleset(70)
        ruleset.test()

        val explanation = ruleset.aldersvilkår.forklar()

        println("=== Full forklar() output ===")
        println(explanation)
        println("=== End output ===")

        // Split and check first line
        val lines = explanation.split("\n").map { it.trim() }.filter { it.isNotBlank() }
        println("First line: '${lines.firstOrNull()}'")

        // Print all lines for debugging
        println("All lines (${lines.size} total):")
        lines.forEachIndexed { index, line -> println("  [$index]: '$line'") }

        assertTrue(lines.isNotEmpty())
        assertEquals("HVA:", lines[0], "First section should be HVA")

        // Check that the output has expected sections
        assertTrue(lines.contains("HVORFOR:"), "Should have HVORFOR section")
    }

    @Test
    fun `forklar with contributing faktum shows nested sections`() {
        @OptIn(DslDomainPredicate::class)
        class TestRulesetWithFormula : AbstractTrackedRuleset<Unit>() {
            lateinit var sum: Faktum<Int>

            override fun create() {
                val a = sporing("a", 10)
                val b = sporing("b", 20)
                sum = sporing("sum", a + b)

                regel("REGEL") {
                    HVIS { sum erStørreEllerLik 15 }
                    SÅ { sporing("result", true) }
                }
            }
        }

        val ruleset = TestRulesetWithFormula()
        ruleset.test()

        val explanation = ruleset.sum.forklar()

        println("=== Explanation with contributing faktum ===")
        println(explanation)
        println("=== End ===")

        val lines = explanation.split("\n").map { it.trim() }.filter { it.isNotBlank() }
        lines.forEachIndexed { i, line -> println("[$i]: $line") }

        // Should see HVA and HVORDAN for sum itself
        assertTrue(lines.contains("HVA:"))
        assertTrue(lines.contains("HVORDAN:"))
        assertTrue(lines.any { it.contains("sum = 30") })
        assertTrue(lines.any { it.contains("notasjon: a + b") })
    }

    @Test
    fun `forklar works gracefully with no tracker registered`() {
        @OptIn(DslDomainPredicate::class)
        class TestRulesetNoTracker : AbstractRuleset<Unit>() {
            lateinit var aldersvilkår: Faktum<Boolean>

            override fun create() {
                val alder = sporing<Int>("alder", 70)
                val aldersgrense = sporing<Int>("aldersgrense", 67)

                regel("VILKÅR-ALDER") {
                    HVIS { alder erStørreEllerLik aldersgrense }
                    SÅ { aldersvilkår = sporing("Aldersvilkår", true) }
                }
            }
        }

        val ruleset = TestRulesetNoTracker()
        ruleset.test()  // No tracker registered

        // forklar() returns informative message
        val explanation = ruleset.aldersvilkår.forklar()
        assertTrue(explanation.contains("No tracker implementation found"), "Should inform user no tracker registered")
    }

    @Test
    fun `SectionTracker builds structured model`() {
        @OptIn(DslDomainPredicate::class)
        class TestRulesetWithSection : AbstractRuleset<Unit>() {
            lateinit var aldersvilkår: Faktum<Boolean>

            override fun test(): Unit {
                // Use SectionTracker instead of IndentedTextTracker
                putResource(TrackerResource::class, SectionTracker())
                return internalRun()
            }

            override fun create() {
                val alder = sporing<Int>("alder", 70)
                val aldersgrense = sporing<Int>("aldersgrense", 67)

                regel("VILKÅR-ALDER") {
                    HVIS { alder erStørreEllerLik aldersgrense }
                    SÅ { aldersvilkår = sporing("Aldersvilkår", true) }
                }
            }
        }

        val ruleset = TestRulesetWithSection()
        ruleset.test()

        val tracker = ruleset.tracker() as SectionTracker

        // Build the model
        val model = tracker.buildExplanationModel(ruleset.aldersvilkår)

        // Verify structure
        assertTrue(model.sections.isNotEmpty())
        val hvaSection = model.sections.find { it.type == ForklaringTypeEnum.HVA }
        assertNotNull(hvaSection)
        assertTrue(hvaSection!!.lines.any { it.contains("Aldersvilkår") })
    }
}
