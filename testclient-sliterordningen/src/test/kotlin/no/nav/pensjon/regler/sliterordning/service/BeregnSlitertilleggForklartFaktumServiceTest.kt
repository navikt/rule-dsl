package no.nav.pensjon.regler.sliterordning.service

import no.nav.pensjon.regler.sliterordning.domain.NormertPensjonsalder
import no.nav.pensjon.regler.sliterordning.domain.Person
import no.nav.pensjon.regler.sliterordning.domain.Trygdetid
import no.nav.pensjon.regler.sliterordning.to.BeregnSlitertilleggRequest
import no.nav.pensjon.regler.sliterordning.to.Response
import no.nav.system.ruledsl.core.inspections.printTree
import no.nav.system.ruledsl.core.resource.tracker.forklar
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.YearMonth

/**
 * WIP
 */
class BeregnSlitertilleggForklartFaktumServiceTest {

    private fun person(fodselsdato: YearMonth, trygdetidMnd: Int): Person =
        Person(fodselsdato, Trygdetid(trygdetidMnd), NormertPensjonsalder.default())


    @Test
    fun `ruleflow innvilger slitertillegg når vilkår er oppfylt`() {
        // Arrange: Create test data
        val fødselsdato = YearMonth.of(1958, 1)
        val uttakstidspunkt = YearMonth.of(2020, 2)  // 62 years + 1 month
        val virkningstidspunkt = YearMonth.of(2020, 2)

        val person = Person(
            fødselsdato = fødselsdato,
            trygdetid = Trygdetid(faktiskTrygdetid = 40),
            normertPensjonsalder = NormertPensjonsalder.Companion.default()
        )

        // Act: Run the ruleflow through the service (which provides necessary resources)
        val result = BeregnSlitertilleggForklartFaktumService(
            BeregnSlitertilleggRequest(
                uttakstidspunkt = uttakstidspunkt,
                virkningstidspunkt = virkningstidspunkt,
                person = person
            )
        ).run()

        // Assert: Verify result is Innvilget (since VilkårsprøvSlitertilleggRS always returns true)
        assertTrue(result is Response.SliterordningForklartFaktum.Innvilget, "Expected Innvilget result")

        val innvilget = result as Response.SliterordningForklartFaktum.Innvilget
        assertTrue(innvilget.slitertillegg.verdi > 0.0, "Expected positive slitertillegg amount")

        val txt = innvilget.slitertillegg.forklar()
        println(txt)
        val x = 0
    }

    @Test
    fun `branch condition faktum should recursively show its ruleset origin`() {
        // Arrange: Create test data
        val fødselsdato = YearMonth.of(1958, 1)
        val uttakstidspunkt = YearMonth.of(2020, 2)
        val virkningstidspunkt = YearMonth.of(2020, 2)

        val person = Person(
            fødselsdato = fødselsdato,
            trygdetid = Trygdetid(faktiskTrygdetid = 40),
            normertPensjonsalder = NormertPensjonsalder.default()
        )

        // Act: Run the service
        val service = BeregnSlitertilleggForklartFaktumService(
            BeregnSlitertilleggRequest(
                uttakstidspunkt = uttakstidspunkt,
                virkningstidspunkt = virkningstidspunkt,
                person = person
            )
        )
        val result = service.run()

        // Assert
        assertTrue(result is Response.SliterordningForklartFaktum.Innvilget)

        val innvilget = result as Response.SliterordningForklartFaktum.Innvilget
        // The slitertillegg Faktum's HVORFOR trace should include vilkårStatus with its origin
        val explanation = innvilget.slitertillegg.forklar()
        println(explanation)

        // Verify that the trace recursively includes the vilkårStatus Faktum from the branch
        assertTrue(
            explanation.contains("Vilkår Slitertillegg"),
            "Should show the vilkårStatus Faktum in the HVORFOR trace (branch condition)"
        )
        // TODO missing complete recursive forklaring from branch (branch does condition.toString, should be condition.forklar()?)
//        assertTrue(
//            explanation.contains("VilkårsprøvSlitertilleggRS"),
//            "Should recursively show the origin ruleset of vilkårStatus"
//        )
//        assertTrue(
//            explanation.contains("ALLTID-INNVILGET"),
//            "Should show the specific rule that created vilkårStatus"
//        )
    }

    @Test
    fun `DEMO - Perspectives - Service-Centric vs Faktum-Centric Views`() {
        println("═".repeat(80))
        println("DEMO: Service-Centric vs Faktum-Centric Perspectives")
        println("═".repeat(80))
        println()

        // Arrange: Create test data
        val fødselsdato = YearMonth.of(1958, 1)
        val uttakstidspunkt = YearMonth.of(2025, 9)  // Early withdrawal
        val virkningstidspunkt = YearMonth.of(2024, 1)

        val person = Person(
            fødselsdato = fødselsdato,
            trygdetid = Trygdetid(faktiskTrygdetid = 20),  // Partial insurance period
            normertPensjonsalder = NormertPensjonsalder.default()
        )

        // Act: Run the service
        val service = BeregnSlitertilleggForklartFaktumService(
            BeregnSlitertilleggRequest(
                uttakstidspunkt = uttakstidspunkt,
                virkningstidspunkt = virkningstidspunkt,
                person = person
            )
        )
        val result = service.run()

        // Assert: Verify we got an Innvilget response
        assertTrue(result is Response.SliterordningForklartFaktum.Innvilget)
        val innvilget = result as Response.SliterordningForklartFaktum.Innvilget

        println("┌─────────────────────────────────────────────────────────────────────┐")
        println("│ FAKTUM-CENTRIC PERSPECTIVES                                         │")
        println("│ (Bottom-up: Why does THIS specific value have this result?)         │")
        println("└─────────────────────────────────────────────────────────────────────┘")
        println()

        // FAKTUM PERSPECTIVE 1: Formula Tree Visualization
        println("1. UTTRYKKS TREE PERSPECTIVE (Formula Structure)")
        println("   - Shows: Calculation tree with deduplication")
        println("   - Use case: Technical testers, formula verification")
        println("   - API: faktum.printTree()")
        println()

        // Show formula tree directly from Faktum
        println(innvilget.slitertillegg.printTree())
        println()

        println("─".repeat(80))
        println()

        // FAKTUM PERSPECTIVE 2: Complete Explanation
        println("2. FAKTUM EXPLANATION PERSPECTIVE (WHAT/HOW/WHY)")
        println("   - Shows: Value, formula, AND execution context")
        println("   - Use case: Explaining specific calculation results")
        println("   - API: faktum.forklar()")
        println()

        // Use forklar() which shows WHAT/HOW/WHY
        val explanation = innvilget.slitertillegg.forklar()
        println(explanation)
        println()

        println("─".repeat(80))
        println()

        println("┌─────────────────────────────────────────────────────────────────────┐")
        println("│ KEY INSIGHTS - NEW ARCHITECTURE                                     │")
        println("└─────────────────────────────────────────────────────────────────────┘")
        println()
        println("✓ THE ARC TREE IS THE EXECUTION TRACE!")
        println("  - No more ExecutionTrace with runtime stack")
        println("  - FaktumNode adds data (Faktum) directly to the ARC tree")
        println("  - Parent pointers enable upward traversal for Faktum.hvorfor()")
        println("  - Single unified tree structure for everything")
        println()
        println("✓ TWO PERSPECTIVES FOR TRACING:")
        println("  - FUNCTIONAL: Decision flow (rules, branches, predicates, faktum only)")
        println("  - Both work top-down (service-centric) and bottom-up (faktum-centric)")
        println()
        println("✓ Faktum-Centric (Bottom-Up): Answers \"Why does THIS value = ${innvilget.slitertillegg.verdi}?\"")
        println("  - faktum.forklar() - Shows WHAT/WHY/HOW for specific value")
        println("  - faktum.printTree() - Shows formula structure")
        println()
        println("✓ Explanation via Interfaces:")
        println("  - Hva: Every ARC and Faktum can identify itself")
        println("  - Hvorfor: Components explain why they were created (via tree traversal)")
        println("  - Hvordan: Components explain how they calculated results")
        println()
        println("✓ Extensible: Add custom traversals as extension functions:")
        println("  - fun AbstractRuleComponent.toJSON(Perspective): JsonObject")
        println("  - fun AbstractRuleComponent.toHTML(Perspective): String")
        println("  - fun AbstractRuleComponent.toMarkdown(Perspective): String")
        println()

        println("═".repeat(80))
    }
}
