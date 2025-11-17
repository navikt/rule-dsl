package no.nav.system.rule.dsl.resource

import no.nav.system.rule.dsl.AbstractResource
import no.nav.system.rule.dsl.AbstractRuleComponent
import no.nav.system.rule.dsl.TrackablePredicate
import no.nav.system.rule.dsl.enums.RuleComponentType
import no.nav.system.rule.dsl.rettsregel.Uttrykk

/**
 * Tracks the execution path through the component tree as it executes.
 *
 * Each component pushes itself onto the stack when it starts executing,
 * and pops itself when done. This creates a live trace of the execution path.
 *
 * Thread-safe: Each root component has its own Trace instance in resourceMap,
 * so concurrent executions don't interfere.
 */
class ExecutionTrace : AbstractResource() {
    private val stack = mutableListOf<AbstractRuleComponent>()

    /**
     * Push a component onto the execution stack.
     */
    fun push(arc: AbstractRuleComponent) {
        stack.add(arc)
    }

    /**
     * Pop the most recently pushed component from the stack.
     */
    fun pop() {
        if (stack.isNotEmpty()) {
            stack.removeLast()
        }
    }

    /**
     * Get the complete execution path with all components.
     *
     * Returns unfiltered trace - simply converts each component on the stack
     * via toTraceUttrykk(). Useful for debugging and full execution traces.
     */
    fun fullPath(): List<Uttrykk<*>> {
        return stack.map { it.toUttrykk() }
    }

    /**
     * Get the execution path filtered for hvorfor explanations.
     *
     * Filters for components that represent decisions (rules, branches) and
     * includes predicates from rules. This provides a focused view of the
     * decision path without container components like rulesets and ruleflows.
     */
    fun pathForHvorfor(): List<Uttrykk<*>> {
        return stack.flatMap { arc ->
            when (arc.type()) {
                RuleComponentType.REGEL -> {
                    buildList {
                        // Add rule name first
                        add(arc.toUttrykk())

                        // Then add predicates from rule's children
                        arc.children
                            .filterIsInstance<TrackablePredicate>()
                            .forEach { predicate ->
                                add(predicate.toUttrykk())
                            }
                    }
                }

                RuleComponentType.GREN -> {
                    // Add branch condition
                    listOf(arc.toUttrykk())
                }

                else -> emptyList()
            }
        }
    }
}