package no.nav.pensjon.regler.sliterordning.format

import no.nav.pensjon.regler.sliterordning.domain.NormertPensjonsalder
import no.nav.pensjon.regler.sliterordning.domain.Person
import no.nav.pensjon.regler.sliterordning.domain.Trygdetid
import no.nav.pensjon.regler.sliterordning.service.BeregnSlitertilleggForklartFaktumService
import no.nav.pensjon.regler.sliterordning.to.BeregnSlitertilleggRequest
import no.nav.pensjon.regler.sliterordning.to.Response
import no.nav.system.ruledsl.core.forklaring.IndentedTextFormatter
import no.nav.system.ruledsl.core.resource.tracker.Filters
import no.nav.system.ruledsl.core.resource.tracker.forklar
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.YearMonth

/**
 * Demonstrates the new transformer architecture:
 * - Built-in IndentedTextFormatter (core module)
 * - Custom SectionTransformer (client module)
 * - Separation of transformation from rendering
 */
class TransformerDemoTest {

    @Test
    fun `demonstrate built-in IndentedTextFormatter`() {
        val result = createTestResult()
        val slitertillegg = (result as Response.SliterordningForklartFaktum.Innvilget).slitertillegg

        println("═".repeat(80))
        println("BUILT-IN TRANSFORMER: IndentedTextFormatter")
        println("═".repeat(80))
        println()

        // Method 1: Using the convenient forklar() extension
        val text1 = slitertillegg.forklar()
        println("Method 1: faktum.forklar()")
        println(text1)
        println()

        // Method 2: Using the transformer directly
        val text2 = IndentedTextFormatter.transform(slitertillegg, Filters.FUNCTIONAL)
        println("Method 2: IndentedTextFormatter.transform()")
        println(text2)
        println()

        // Both should produce the same result
        assertTrue(text1 == text2)
    }

    @Test
    fun `demonstrate custom SectionTransformer with multiple renderers`() {
        val result = createTestResult()
        val slitertillegg = (result as Response.SliterordningForklartFaktum.Innvilget).slitertillegg

        println("═".repeat(80))
        println("CUSTOM TRANSFORMER: SectionTransformer")
        println("═".repeat(80))
        println()

        // Transform once to structured sections
        val sections = SectionTransformer.transform(slitertillegg, Filters.FUNCTIONAL)

        println("Transformed to ${sections.size} sections")
        println()

        // Render in multiple formats from the same structured data
        println("─".repeat(80))
        println("RENDER AS TEXT:")
        println("─".repeat(80))
        val text = sections.renderAsText()
        println(text)
        println()

        println("─".repeat(80))
        println("RENDER AS MARKDOWN:")
        println("─".repeat(80))
        val markdown = sections.renderAsMarkdown()
        println(markdown)
        println()

        println("─".repeat(80))
        println("RENDER AS HTML:")
        println("─".repeat(80))
        val html = sections.renderAsHtml()
        println(html)
        println()

        println("─".repeat(80))
        println("RENDER AS JSON:")
        println("─".repeat(80))
        val json = sections.renderAsJson()
        println(json)
        println()

        // Verify we got some sections
        assertTrue(sections.isNotEmpty())
        assertTrue(sections.any { it.type == SectionType.HVA })
        assertTrue(sections.any { it.type == SectionType.HVORDAN })
        assertTrue(sections.any { it.type == SectionType.RULE })
    }

    @Test
    fun `demonstrate section structure inspection`() {
        val result = createTestResult()
        val slitertillegg = (result as Response.SliterordningForklartFaktum.Innvilget).slitertillegg

        val sections = SectionTransformer.transform(slitertillegg, Filters.FUNCTIONAL)

        println("═".repeat(80))
        println("SECTION STRUCTURE INSPECTION")
        println("═".repeat(80))
        println()

        // Group by type
        val byType = sections.groupBy { it.type }
        println("Sections by type:")
        byType.forEach { (type, sectionList) ->
            println("  $type: ${sectionList.size} sections")
        }
        println()

        // Show depth distribution
        val byDepth = sections.groupBy { it.depth }
        println("Sections by depth:")
        byDepth.forEach { (depth, sectionList) ->
            println("  Depth $depth: ${sectionList.size} sections")
        }
        println()

        // Show first few sections with details
        println("First 10 sections:")
        sections.take(10).forEach { section ->
            println("  [${section.type}] depth=${section.depth} content='${section.content}'")
        }
    }

    private fun createTestResult(): Response.SliterordningForklartFaktum {
        val fødselsdato = YearMonth.of(1958, 1)
        val uttakstidspunkt = YearMonth.of(2020, 2)
        val virkningstidspunkt = YearMonth.of(2020, 2)

        val person = Person(
            fødselsdato = fødselsdato,
            trygdetid = Trygdetid(faktiskTrygdetid = 40),
            normertPensjonsalder = NormertPensjonsalder.default()
        )

        return BeregnSlitertilleggForklartFaktumService(
            BeregnSlitertilleggRequest(
                uttakstidspunkt = uttakstidspunkt,
                virkningstidspunkt = virkningstidspunkt,
                person = person
            )
        ).run()
    }
}
