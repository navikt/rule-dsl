package no.nav.system.ruledsl.core.forklaring

import no.nav.system.ruledsl.core.model.AbstractRuleComponent
import no.nav.system.ruledsl.core.model.AbstractRuleflow
import no.nav.system.ruledsl.core.model.Rule
import no.nav.system.ruledsl.core.model.TrackablePredicate
import no.nav.system.ruledsl.core.resource.tracker.Filter
import no.nav.system.ruledsl.core.rettsregel.Faktum
import kotlin.collections.isNotEmpty

/**
 * Built-in transformer that produces indented text explanations.
 *
 * The ARC tree is the complete execution trace - all information needed for explanation
 * is already stored in the tree structure:
 * - Rules and their predicates are tree nodes
 * - Predicates store their evaluation results via fired()
 * - Faktum store their values
 * - Parent pointers enable upward traversal
 * - Branch conditions and fired status are in the tree
 */
object IndentedTextFormatter : FaktumTransformer<String> {

    /**
     * Transform a Faktum into indented text explanation.
     */
    override fun transform(faktum: Faktum<*>, filter: Filter): String {
        return buildExplanation(faktum, filter, depth = 0)
    }

    private fun buildExplanation(faktum: Faktum<*>, filter: Filter, depth: Int): String {
        val showHva = !faktum.isConstant || depth == 0

        return buildString {
            val indent = "  ".repeat(depth)

            // HVA section (conditionally)
            if (showHva) {
                if (isNotEmpty() && !endsWith("\n\n")) {
                    appendLine()
                }
                appendLine("${indent}HVA:")
                appendLine("$indent  ${faktum.navn} = ${faktum.verdi}")
            }

            // HVORDAN section (if not constant)
            if (!faktum.isConstant) {
                if (isNotEmpty() && !endsWith("\n\n")) {
                    appendLine()
                }
                appendLine("${indent}HVORDAN:")
                appendLine("$indent  notasjon: ${faktum.uttrykk.notasjon()}")
                appendLine("$indent  konkret: ${faktum.uttrykk.konkret()}")
            }

            // HVORFOR section - walk up the tree to find the Rule that created this Faktum
            if (!faktum.isConstant || depth == 0) {
                val hvorforLines = collectDecisionPath(faktum, filter)
                if (hvorforLines.isNotEmpty()) {
                    if (isNotEmpty() && !endsWith("\n\n")) {
                        appendLine()
                    }
                    appendLine("${indent}HVORFOR:")
                    hvorforLines.forEach { line ->
                        appendLine("$indent  $line")
                    }
                }
            }

            // Recursively add contributing faktum
            val contributingFaktum = faktum.uttrykk.faktumSet()
            contributingFaktum.forEach { contributingFaktum ->
                val contributingExplanation = buildExplanation(contributingFaktum, filter, depth + 1)
                if (contributingExplanation.isNotBlank()) {
                    append(contributingExplanation)
                }
            }
        }
    }

    /**
     * Walk up the ARC tree from the Faktum to find Rules and Branches that created it.
     */
    private fun collectDecisionPath(faktum: Faktum<*>, filter: Filter): List<String> {
        val lines = mutableListOf<String>()

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
        lines.add("$rule")

        // Add predicates from the rule's children
        rule.children.filterIsInstance<TrackablePredicate>().forEach { predicate ->
            lines.add("  $predicate")
        }

        // Add references for this rule (if any)
        if (rule.references.isNotEmpty()) {
            lines.add("  REFERENCES:")
            rule.references.forEach { ref ->
                lines.add("    ${ref.id}: ${ref.url}")
            }
        }

        // Walk up the ARC tree to find Branch ancestors
        var ancestor = rule.parent
        while (ancestor != null) {
            if (ancestor is AbstractRuleflow.Decision.Branch) {
                val condition = ancestor.condition

                // Extract all Faktum objects from the condition expression
                val faktumInCondition = condition.faktumSet()

                // Recursively explain each Faktum in the branch condition
                faktumInCondition.forEach { conditionFaktum ->
                    lines.add("  gren betingelse: ${conditionFaktum.navn} = ${conditionFaktum.verdi}")
                    val conditionExplanation = buildExplanation(conditionFaktum, filter, depth = 1)
                    if (conditionExplanation.isNotBlank()) {
                        // Indent each line of the recursive explanation
                        conditionExplanation.lines().forEach { line ->
                            if (line.isNotBlank()) {
                                lines.add("  $line")
                            }
                        }
                    }
                }
            }
            ancestor = ancestor.parent
        }

        return lines
    }
}
