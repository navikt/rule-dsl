package no.nav.pensjon.regler.sliterordning.format

import no.nav.system.ruledsl.core.forklaring.FaktumTransformer
import no.nav.system.ruledsl.core.forklaring.Filter
import no.nav.system.ruledsl.core.model.arc.TrackablePredicate
import no.nav.system.ruledsl.core.model.Faktum
import no.nav.system.ruledsl.core.model.arc.AbstractRuleComponent
import no.nav.system.ruledsl.core.model.arc.AbstractRuleflow
import no.nav.system.ruledsl.core.model.arc.Rule

/**
 * Example custom transformer that produces structured sections.
 *
 * Demonstrates how clients can create their own transformers for custom output formats.
 * The sections can then be rendered in various ways (HTML, Markdown, JSON, etc.).
 */
object SectionTransformer : FaktumTransformer<List<Section>> {

    override fun transform(faktum: Faktum<*>, filter: Filter): List<Section> {
        return buildSections(faktum, filter, depth = 0)
    }

    private fun buildSections(faktum: Faktum<*>, filter: Filter, depth: Int): List<Section> {
        val sections = mutableListOf<Section>()
        val showHva = !faktum.isConstant || depth == 0

        // HVA section
        if (showHva) {
            sections.add(
                Section(
                    depth = depth,
                    type = SectionType.HVA,
                    content = "${faktum.navn} = ${faktum.verdi}"
                )
            )
        }

        // HVORDAN section
        if (!faktum.isConstant) {
            sections.add(
                Section(
                    depth = depth,
                    type = SectionType.HVORDAN,
                    content = "notasjon: ${faktum.uttrykk.notasjon()}"
                )
            )
            sections.add(
                Section(
                    depth = depth,
                    type = SectionType.HVORDAN,
                    content = "konkret: ${faktum.uttrykk.konkret()}"
                )
            )
        }

        // HVORFOR section - walk up the tree
        if (!faktum.isConstant || depth == 0) {
            val hvorforSections = collectDecisionPath(faktum, filter, depth)
            sections.addAll(hvorforSections)
        }

        // Recursively add contributing faktum
        val contributingFaktum = faktum.uttrykk.faktumSet()
        contributingFaktum.forEach { contributing ->
            sections.addAll(buildSections(contributing, filter, depth + 1))
        }

        return sections
    }

    private fun collectDecisionPath(faktum: Faktum<*>, filter: Filter, baseDepth: Int): List<Section> {
        val sections = mutableListOf<Section>()

        // Find the FaktumNode wrapper in the tree
        val faktumNode = faktum.wrapperNode ?: return emptyList()

        // Walk up to find the Rule parent
        var node: AbstractRuleComponent? = faktumNode.parent
        var foundRule: Rule<*>? = null

        while (node != null && foundRule == null) {
            if (node is Rule<*>) {
                foundRule = node
            }
            node = node.parent
        }

        if (foundRule == null) return emptyList()

        val rule = foundRule

        // Check if this rule passes the filter
        if (!filter.includes(rule)) {
            return emptyList()
        }

        // Add the rule
        sections.add(
            Section(
                depth = baseDepth,
                type = SectionType.RULE,
                content = rule.toString()
            )
        )

        // Add predicates
        rule.children.filterIsInstance<TrackablePredicate>().forEach { predicate ->
            sections.add(
                Section(
                    depth = baseDepth + 1,
                    type = SectionType.PREDICATE,
                    content = predicate.toString()
                )
            )
        }

        // Add references
        rule.references.forEach { ref ->
            sections.add(
                Section(
                    depth = baseDepth + 1,
                    type = SectionType.REFERENCE,
                    content = "${ref.id}: ${ref.url}"
                )
            )
        }

        // Walk up to find Branch ancestors
        var ancestor = rule.parent
        while (ancestor != null) {
            if (ancestor is AbstractRuleflow.Decision.Branch) {
                val condition = ancestor.condition
                val faktumInCondition = condition.faktumSet()

                faktumInCondition.forEach { conditionFaktum ->
                    sections.add(
                        Section(
                            depth = baseDepth + 1,
                            type = SectionType.BRANCH_CONDITION,
                            content = "${conditionFaktum.navn} = ${conditionFaktum.verdi}"
                        )
                    )

                    // Recursively explain the branch condition
                    sections.addAll(buildSections(conditionFaktum, filter, baseDepth + 2))
                }
            }
            ancestor = ancestor.parent
        }

        return sections
    }
}
