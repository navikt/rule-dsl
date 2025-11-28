package no.nav.system.rule.dsl.tracker

import no.nav.system.rule.dsl.AbstractRuleComponent
import no.nav.system.rule.dsl.FaktumNode
import no.nav.system.rule.dsl.Rule
import no.nav.system.rule.dsl.TrackablePredicate
import no.nav.system.rule.dsl.rettsregel.Const
import no.nav.system.rule.dsl.rettsregel.Faktum

/**
 * Domain-independent explanation model.
 * Structured data for GUI consumers.
 */
data class ExplanationModel(
    val sections: List<Section>
)

/**
 * A section in the explanation (HVA, HVORDAN, or HVORFOR).
 * Can have child sections for nested contributing faktum.
 */
data class Section(
    val type: ForklaringTypeEnum,
    val lines: List<String>,
    val children: List<Section> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * Types of explanation sections.
 */
enum class ForklaringTypeEnum {
    HVA,      // What: the result/output
    HVORDAN,  // How: the calculation/formula
    HVORFOR   // Why: the decision path (rules/predicates)
}

/**
 * Tracker that builds structured Section data model.
 * Good for GUI consumers, JSON serialization, or custom rendering.
 *
 * Native format is ExplanationModel (R = ExplanationModel).
 * The explainFaktumAsString method converts the model to human-readable text.
 */
class SectionTracker : TrackerResource<ExplanationModel>() {

    // Track execution context: which Rule created which Faktum
    private val faktumToRuleContext = mutableMapOf<Faktum<*>, MutableList<RuleContext>>()

    // Track predicate results by rule
    private val ruleToPredicates = mutableMapOf<Rule<*>, MutableList<PredicateEvaluation>>()

    // Track current rule being evaluated
    private var currentRule: Rule<*>? = null

    private data class RuleContext(val rule: Rule<*>, val parent: AbstractRuleComponent)
    private data class PredicateEvaluation(val predicate: TrackablePredicate, val result: Boolean)

    override fun onFaktumCreated(faktum: Faktum<*>, parent: AbstractRuleComponent) {
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

    override fun explainFaktum(faktum: Faktum<*>, filter: Filter): ExplanationModel {
        return buildExplanationModel(faktum, filter)
    }

    override fun explainFaktumAsString(faktum: Faktum<*>, filter: Filter): String {
        return explainFaktum(faktum, filter).toHvaHvordanHvorfor()
    }

    /**
     * Build structured ExplanationModel for a Faktum.
     * Public for GUI consumers who want the model directly.
     */
    fun buildExplanationModel(faktum: Faktum<*>, filter: Filter = Filters.FUNCTIONAL): ExplanationModel {
        val sections = buildSectionsForFaktum(faktum, filter, depth = 0)
        return ExplanationModel(sections)
    }

    private fun buildSectionsForFaktum(faktum: Faktum<*>, filter: Filter, depth: Int): List<Section> {
        val isConst = faktum.uttrykk is Const<*>
        val showHva = !isConst || depth == 0

        val sections = mutableListOf<Section>()

        // HVA section (conditionally)
        if (showHva) {
            sections.add(
                Section(
                    type = ForklaringTypeEnum.HVA,
                    lines = listOf("${faktum.navn} = ${faktum.verdi}"),
                    metadata = mapOf(
                        "name" to faktum.navn,
                        "value" to faktum.verdi.toString()
                    )
                )
            )
        }

        // HVORDAN section (if not constant)
        if (!isConst) {
            sections.add(
                Section(
                    type = ForklaringTypeEnum.HVORDAN,
                    lines = listOf(
                        "notasjon: ${faktum.uttrykk.notasjon()}",
                        "konkret: ${faktum.uttrykk.konkret()}"
                    ),
                    metadata = mapOf(
                        "notasjon" to faktum.uttrykk.notasjon(),
                        "konkret" to faktum.uttrykk.konkret()
                    )
                )
            )
        }

        // HVORFOR section with decision path and contributing faktum as children
        if (!isConst || depth == 0) {
            val hvorforLines = collectDecisionPath(faktum, filter)
            val contributingChildren = faktum.uttrykk.faktumSet().flatMap { contributingFaktum ->
                buildSectionsForFaktum(contributingFaktum, filter, depth + 1)
            }

            if (hvorforLines.isNotEmpty() || contributingChildren.isNotEmpty()) {
                sections.add(
                    Section(
                        type = ForklaringTypeEnum.HVORFOR,
                        lines = hvorforLines,
                        children = contributingChildren
                    )
                )
            }
        }

        return sections
    }

    private fun collectDecisionPath(faktum: Faktum<*>, filter: Filter): List<String> {
        val lines = mutableListOf<String>()

        val ruleContexts = faktumToRuleContext[faktum] ?: return emptyList()

        ruleContexts.forEach { context ->
            val rule = context.rule

            if (!filter.includes(rule)) {
                return@forEach
            }

            lines.add("$rule")

            val predicates = ruleToPredicates[rule] ?: emptyList()
            predicates.forEach { evaluation ->
                lines.add("  ${evaluation.predicate}")
            }
        }

        return lines
    }
}

/**
 * Render ExplanationModel to HVA/HVORDAN/HVORFOR format.
 */
fun ExplanationModel.toHvaHvordanHvorfor(): String = buildString {
    fun renderSection(section: Section, indent: Int = 0) {
        val prefix = "  ".repeat(indent)

        // Section header
        if (isNotEmpty() && !endsWith("\n\n")) {
            appendLine()
        }
        appendLine("$prefix${section.type}:")

        // Section content lines
        section.lines.forEach { line ->
            appendLine("$prefix  $line")
        }

        // Child sections
        section.children.forEach { child ->
            renderSection(child, indent + 1)
        }
    }

    sections.forEach { section ->
        renderSection(section)
    }
}
