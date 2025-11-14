package no.nav.system.rule.dsl.inspections

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
     * Get the current execution path as a list of Uttrykk.
     *
     * Converts each component on the stack via toTraceUttrykk(),
     * filtering for components that represent decisions (rules, branches).
     */
    fun currentPathAsUttrykk(): List<Uttrykk<*>> {
        return stack.flatMap { arc ->
            when (arc.type()) {
                RuleComponentType.REGEL -> {
                    buildList {
                        // Add rule name first
                        add(arc.toTraceUttrykk())

                        // Then add predicates from rule's children
                        arc.children
                            .filterIsInstance<TrackablePredicate>()
                            .forEach { predicate ->
                                add(predicate.toTraceUttrykk())
                            }
                    }
                }

                RuleComponentType.GREN -> {
                    // Add branch condition
                    listOf(arc.toTraceUttrykk())
                }

                // Other types not included in trace
                // Uncomment these to include them:
                // RuleComponentType.REGELSETT -> listOf(arc.toTraceUttrykk())
                // RuleComponentType.FORGRENING -> listOf(arc.toTraceUttrykk())

                else -> emptyList()
            }
        }
    }
}
