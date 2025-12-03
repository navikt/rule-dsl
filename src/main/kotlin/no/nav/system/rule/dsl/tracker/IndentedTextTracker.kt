package no.nav.system.rule.dsl.tracker

import no.nav.system.rule.dsl.AbstractRuleComponent
import no.nav.system.rule.dsl.FaktumNode
import no.nav.system.rule.dsl.Rule
import no.nav.system.rule.dsl.TrackablePredicate
import no.nav.system.rule.dsl.rettsregel.Const
import no.nav.system.rule.dsl.rettsregel.Faktum

/**
 * Simple tracker that builds formatted string output.
 * Good for logging, debugging, and console output.
 *
 * Native format is String (R = String), so explainFaktum and explainFaktumAsString return the same result.
 */
class IndentedTextTracker : TrackerResource<String>() {

    // Track execution context: which Rule created which Faktum
    private val faktumToRuleContext = mutableMapOf<Faktum<*>, MutableList<RuleContext>>()

    // Track predicate results by rule
    private val ruleToPredicates = mutableMapOf<Rule<*>, MutableList<PredicateEvaluation>>()

    // Track current rule being evaluated (for associating faktum with rules)
    private var currentRule: Rule<*>? = null

    private data class RuleContext(val rule: Rule<*>, val parent: AbstractRuleComponent)
    private data class PredicateEvaluation(val predicate: TrackablePredicate, val result: Boolean)

    override fun onFaktumCreated(faktum: Faktum<*>, parent: AbstractRuleComponent) {
        // If created during rule evaluation, track the association
        currentRule?.let { rule ->
            faktumToRuleContext
                .getOrPut(faktum) { mutableListOf() }
                .add(RuleContext(rule, parent))
        }
    }

    override fun onRuleEvaluationStart(rule: Rule<*>) {
        currentRule = rule
    }

    override fun onRuleEvaluationEnd(rule: Rule<*>, fired: Boolean) {
        currentRule = null
    }

    override fun onPredicateEvaluated(predicate: TrackablePredicate, rule: Rule<*>, result: Boolean) {
        ruleToPredicates
            .getOrPut(rule) { mutableListOf() }
            .add(PredicateEvaluation(predicate, result))
    }

    override fun explainFaktum(faktum: Faktum<*>, filter: Filter): String {
        return buildExplanation(faktum, filter, depth = 0)
    }

    override fun explainFaktumAsString(faktum: Faktum<*>, filter: Filter): String {
        return explainFaktum(faktum, filter)  // Same - already String!
    }

    private fun buildExplanation(faktum: Faktum<*>, filter: Filter, depth: Int): String {
        val isConst = faktum.uttrykk is Const<*>
        val showHva = !isConst || depth == 0

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
            if (!isConst) {
                if (isNotEmpty() && !endsWith("\n\n")) {
                    appendLine()
                }
                appendLine("${indent}HVORDAN:")
                appendLine("$indent  notasjon: ${faktum.uttrykk.notasjon()}")
                appendLine("$indent  konkret: ${faktum.uttrykk.konkret()}")
            }

            // HVORFOR section with decision path
            if (!isConst || depth == 0) {
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

    private fun collectDecisionPath(faktum: Faktum<*>, filter: Filter): List<String> {
        val lines = mutableListOf<String>()

        // Get rules that created this faktum
        val ruleContexts = faktumToRuleContext[faktum] ?: return emptyList()

        ruleContexts.forEach { context ->
            val rule = context.rule

            // Check if this rule passes the filter
            if (!filter.includes(rule)) {
                return@forEach
            }

            // Add the rule (toString() already includes "regel:" prefix)
            lines.add("$rule")

            // Add predicates for this rule (indented, toString() already includes "predikat:" prefix)
            val predicates = ruleToPredicates[rule] ?: emptyList()
            predicates.forEach { evaluation ->
                val predicate = evaluation.predicate
                lines.add("  $predicate")
            }

            // Add references for this rule (if any)
            if (rule.references.isNotEmpty()) {
                lines.add("  REFERENCES:")
                rule.references.forEach { ref ->
                    lines.add("    ${ref.id}: ${ref.url}")
                }
            }
        }

        return lines
    }
}
