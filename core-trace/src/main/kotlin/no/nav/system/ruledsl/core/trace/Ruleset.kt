package no.nav.system.ruledsl.core.trace

import no.nav.system.ruledsl.core.expression.Faktum
import no.nav.system.ruledsl.core.resource.ResourceAccessor
import kotlin.reflect.KClass

/**
 * Ruleset - collects results from rules, implements first-match-wins semantics.
 *
 * Type parameter T represents the result type. Can be any type.
 * If T is a Faktum, it will be automatically recorded to the trace.
 *
 * Rules are evaluated in sequence. The first rule that fires and produces a result
 * (via RETURNER) stops further evaluation. Side-effect rules (via SÅ) do not stop evaluation.
 *
 * Implements ResourceAccessor to allow extension functions for resource access.
 */
class Ruleset<T : Any>(private val trace: Trace) : ResourceAccessor {
    var result: T? = null
        private set
    var hasResult = false
        private set

    // ResourceAccessor delegation to Trace
    override fun <R : Any> getResource(key: KClass<R>): R = trace.getResource(key)
    override fun <R : Any> putResource(key: KClass<R>, resource: R) = trace.putResource(key, resource)

    /**
     * Define a rule that supports both side-effects (SÅ) and value-producing (RETURNER).
     * Use SÅ for side-effects, RETURNER for returning a value of type T.
     * Cannot use both in the same rule.
     *
     * @param name The rule name (used in trace output)
     * @param builder DSL builder for the rule content (HVIS, OG, SÅ, RETURNER)
     */
    fun regel(name: String, builder: Rule<T>.() -> Unit) {
        if (hasResult) return

        val rule = Rule<T>(trace)
        rule.builder()

        val ruleFired = rule.evaluate()
        val execution = trace.recordRule(name, ruleFired, rule.expressions())
        trace.pushContext(execution)

        if (ruleFired) {
            when {
                rule.hasReturner() && rule.hasAction() -> 
                    throw IllegalStateException("regel '$name' cannot have both SÅ and RETURNER")
                
                rule.hasReturner() -> {
                    result = rule.executeReturner()
                    hasResult = true
                }
                
                rule.hasAction() -> {
                    rule.executeAction()
                }
            }
        }

        trace.popContext()
    }

    /**
     * Define a rule that applies to each element in a list (pattern).
     * Each element creates a separate rule with indexed naming (e.g., "RuleName.1", "RuleName.2").
     * 
     * @param name The base rule name
     * @param pattern List of elements to apply the rule to
     * @param builder DSL builder that receives the current element
     */
    fun <P> regel(name: String, pattern: List<P>, builder: Rule<T>.(P) -> Unit) {
        pattern.forEachIndexed { index, element ->
            regel("$name.${index + 1}") {
                builder(element)
            }
        }
    }
}

/**
 * Entry point for traced rule evaluation.
 * Evaluates rules in sequence, returns first match.
 *
 * Type parameter T is the result type. Can be any type.
 * If T is a Faktum, it will be automatically recorded to the trace.
 *
 * For Unit type: returns Unit without throwing if no rule matched.
 *
 * @param block DSL block containing regel definitions
 * @return The result from the first matching rule with RETURNER
 * @throws IllegalStateException if no rule matched (except for Unit)
 */
context(trace: Trace)
inline fun <reified T : Any> traced(block: Ruleset<T>.() -> Unit): T {
    val scope = Ruleset<T>(trace)
    scope.block()

    return when {
        scope.hasResult -> scope.result!!
        T::class == Unit::class -> Unit as T
        else -> throw IllegalStateException("No rule matched")
    }
}
