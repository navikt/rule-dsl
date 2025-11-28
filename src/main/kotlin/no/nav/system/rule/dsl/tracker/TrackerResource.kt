package no.nav.system.rule.dsl.tracker

import no.nav.system.rule.dsl.AbstractResource
import no.nav.system.rule.dsl.AbstractRuleComponent
import no.nav.system.rule.dsl.FaktumNode
import no.nav.system.rule.dsl.Rule
import no.nav.system.rule.dsl.TrackablePredicate
import no.nav.system.rule.dsl.rettsregel.Faktum

/**
 * Abstract base class for tracking rule execution and generating explanations.
 *
 * Generic type parameter R represents the native return type of this tracker:
 * - String for text-based trackers (IndentedTextTracker)
 * - ExplanationModel for structured data trackers (SectionTracker)
 * - JsonObject, XML, or any custom format
 *
 * Uses Accumulator/Collector pattern:
 * - ACCUMULATION PHASE: Hook methods accumulate data during execution
 * - TRANSFORMATION PHASE: Transform methods convert accumulated data → R
 *
 * TrackerResource uses the ResourceMap pattern - register an instance in your service:
 * ```
 * override fun run(): Response {
 *     putResource(TrackerResource::class, IndentedTextTracker())
 *     return super.run()
 * }
 * ```
 */
abstract class TrackerResource<R> : AbstractResource() {

    // === ACCUMULATION PHASE ===
    // Hook methods called during execution to accumulate tracking data

    /**
     * Called when a Faktum is created via sporing().
     * Accumulate HVA (what was computed) and HVORDAN (how it was calculated).
     */
    open fun onFaktumCreated(faktum: Faktum<*>, parent: AbstractRuleComponent) {}

    /**
     * Called when a Rule begins evaluation.
     */
    open fun onRuleEvaluationStart(rule: Rule<*>) {}

    /**
     * Called when a Rule completes evaluation.
     * Accumulate HVORFOR (why this decision was made).
     */
    open fun onRuleEvaluationEnd(rule: Rule<*>, fired: Boolean) {}

    /**
     * Called when a TrackablePredicate (domain predicate) is evaluated.
     * Accumulate specific conditions that led to decisions.
     */
    open fun onPredicateEvaluated(predicate: TrackablePredicate, rule: Rule<*>, result: Boolean) {}

    /**
     * Called when entering a ruleset.
     */
    open fun onRulesetEnter(component: AbstractRuleComponent) {}

    /**
     * Called when exiting a ruleset.
     */
    open fun onRulesetExit(component: AbstractRuleComponent) {}

    // === TRANSFORMATION PHASE ===
    // Transform accumulated data into explanations

    /**
     * Generate explanation for a Faktum in native format R.
     *
     * @param faktum The Faktum to explain
     * @param filter Which components to include (ALL or FUNCTIONAL)
     * @return Explanation in native format R (String, ExplanationModel, JsonObject, etc.)
     */
    abstract fun explainFaktum(faktum: Faktum<*>, filter: Filter): R

    /**
     * Generate explanation for a Faktum as human-readable String.
     * Required for forklar() API which always returns String.
     *
     * @param faktum The Faktum to explain
     * @param filter Which components to include (ALL or FUNCTIONAL)
     * @return Human-readable String explanation
     */
    abstract fun explainFaktumAsString(faktum: Faktum<*>, filter: Filter): String
}
