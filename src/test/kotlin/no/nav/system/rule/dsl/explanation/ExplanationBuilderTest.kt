package no.nav.system.rule.dsl.explanation

import no.nav.system.rule.dsl.AbstractRuleset
import no.nav.system.rule.dsl.DslDomainPredicate
import no.nav.system.rule.dsl.perspectives.Perspective
import no.nav.system.rule.dsl.rettsregel.Faktum
import no.nav.system.rule.dsl.rettsregel.operators.erStørreEllerLik
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ExplanationBuilderTest {

    @OptIn(DslDomainPredicate::class)
    class TestRuleset(alder: Int) : AbstractRuleset<Boolean>() {

        val alder = Faktum("alder", alder)
        val aldersgrense = Faktum("aldersgrense", 67)

        override fun create() {

            regel("VILKÅR-ALDER") {
                HVIS { alder erStørreEllerLik aldersgrense }
                SÅ { sporing("Aldersvilkår", true) }
                ELLERS { sporing("Aldersvilkår", false) }
            }
        }
    }

    @Test
    fun `ExplanationBuilder - Faktum-centric (bottom-up)`() {
        val ruleset = TestRuleset(70)
        ruleset.test()

        // Get a Faktum from the tree
        val faktumNodes = ruleset.collectFaktum()
        val alderFaktum = faktumNodes.first { it.faktum.navn == "alder" }.faktum

        // Use ExplanationBuilder to traverse UP from Faktum
        val explanation = alderFaktum.explain()
            .perspective(Perspective.FUNCTIONAL)
            .direction(Direction.UP)
            .toText()

        println("=== Faktum-centric explanation (UP) ===")
        println(explanation)

        assertTrue(explanation.contains("predikat"))
        assertTrue(explanation.contains("regel: TestRuleset.VILKÅR-ALDER"))
    }

    @Test
    fun `ExplanationBuilder - Service-centric (top-down)`() {
        val ruleset = TestRuleset(70)
        ruleset.test()

        // Use ExplanationBuilder to traverse DOWN from service
        val trace = ruleset.explain()
            .perspective(Perspective.FUNCTIONAL)
            .direction(Direction.DOWN)
            .toText()

        println("=== Service-centric trace (DOWN) ===")
        println(trace)

        assertTrue(trace.contains("regel"))
        assertTrue(trace.contains("faktum"))
    }

    @Test
    fun `ExplanationBuilder - Custom transform to list`() {
        val ruleset = TestRuleset(70)
        ruleset.test()

        // Custom transform: return just the names
        val names = ruleset.explain()
            .perspective(Perspective.FUNCTIONAL)
            .direction(Direction.DOWN)
            .transform { nodes ->
                nodes.map { it.name() }
            }

        println("=== Custom transform (names only) ===")
        println(names)

        assertTrue(names.any { it.contains("VILKÅR-ALDER") })
        assertTrue(names.contains("alder"))
        assertTrue(names.contains("aldersgrense"))
    }

    @Test
    fun `ExplanationBuilder - Custom perspective`() {
        val ruleset = TestRuleset(70)
        ruleset.test()

        // Define custom perspective: only Faktum nodes
        val OnlyFaktum = Perspective { arc ->
            arc.type() == no.nav.system.rule.dsl.enums.RuleComponentType.FAKTUM
        }

        val faktumOnly = ruleset.explain()
            .perspective(OnlyFaktum)
            .direction(Direction.DOWN)
            .toText()

        println("=== Custom perspective (only Faktum) ===")
        println(faktumOnly)

        assertTrue(faktumOnly.contains("alder"))
        assertTrue(faktumOnly.contains("aldersgrense"))
        assertFalse(faktumOnly.contains("regel"))
    }

    @Test
    fun `Convenience API - forklar() provides formatted explanation`() {
        val ruleset = TestRuleset(70)
        ruleset.test()

        val faktumNodes = ruleset.collectFaktum()
        val alderFaktum = faktumNodes.first { it.faktum.navn == "alder" }.faktum

        // Convenience API: forklar() for formatted text
        val explanation = alderFaktum.forklar()

        println("=== forklar() convenience method ===")
        println(explanation)

        assertTrue(explanation.contains("HVA"))
        assertTrue(explanation.contains("HVORFOR"))
        assertTrue(explanation.contains("regel"))
    }
}
