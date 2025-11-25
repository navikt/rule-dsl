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
    class TestRuleset(private val alderInput: Int) : AbstractRuleset<Boolean>() {

        override fun create() {
            val alder = sporing<Int>("alder", alderInput)
            val aldersgrense = sporing<Int>("aldersgrense", 67)

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

        // Get a Faktum from the tree - use Aldersvilkår which is created inside the rule
        val faktumNodes = ruleset.collectFaktum()
        val aldersvilkårFaktum = faktumNodes.first { it.faktum.navn == "Aldersvilkår" }.faktum

        // Use ExplanationBuilder to traverse UP from Faktum
        val explanation = aldersvilkårFaktum.explain()
            .perspective(Perspective.FUNCTIONAL)
            .direction(Direction.UP)
            .transform(::toIndentedText)

        println("=== Faktum-centric explanation (UP) ===")
        println(explanation)

        // Now uses toString() instead of hva(), so format is different
        assertTrue(explanation.contains("'alder'"))
        assertTrue(explanation.contains("regel: JA TestRuleset.VILKÅR-ALDER"))
    }

    @Test
    fun `ExplanationBuilder - Service-centric (top-down)`() {
        val ruleset = TestRuleset(70)
        ruleset.test()

        // Use ExplanationBuilder to traverse DOWN from service
        val trace = ruleset.explain()
            .perspective(Perspective.FUNCTIONAL)
            .direction(Direction.DOWN)
            .transform(::toIndentedText)

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
                nodes.map { (node, _) -> node.name() }
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
            .transform(::toIndentedText)

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
        val aldersvilkårFaktum = faktumNodes.first { it.faktum.navn == "Aldersvilkår" }.faktum

        // Convenience API: forklar() for formatted text
        // Need to explicitly use the extension function by providing the perspective parameter
        val explanation = aldersvilkårFaktum.forklar(Perspective.FUNCTIONAL)

        println("=== forklar() convenience method ===")
        println(explanation)

        // Check that the explanation contains key sections
        assertTrue(explanation.contains("HVA"))
        assertTrue(explanation.contains("Aldersvilkår = true"))
        assertTrue(explanation.contains("HVORFOR"))
        assertTrue(explanation.contains("regel: JA TestRuleset.VILKÅR-ALDER"))
        assertTrue(explanation.contains("predikat: JA 'alder' (70) er større eller lik 'aldersgrense' (67)"))
    }
}
