package no.nav.system.ruledsl.core.trace

import no.nav.system.ruledsl.core.expression.Expression
import no.nav.system.ruledsl.core.expression.Faktum
import no.nav.system.ruledsl.core.resource.ResourceAccessor
import kotlin.reflect.KClass

/**
 * Result of a rule evaluation, usable as an Expression<Boolean> for introspection.
 * 
 * Can be used directly in HVIS/OG predicates of subsequent rules:
 * ```
 * val r1 = regel("First") { ... }
 * regel("Second") {
 *     HVIS { r1 }  // Traces as "regel 'First' har truffet" or "har ikke truffet"
 * }
 * ```
 */
class RuleResult internal constructor(
    private val name: String,
    private val fired: Boolean
) : Expression<Boolean> {
    override val value: Boolean get() = fired
    override fun notation() = "regel '$name'"
    override fun concrete() = if (fired) "har truffet" else "har ikke truffet"
    override fun faktumSet(): Set<Faktum<*>> = emptySet()
    override fun toString() = "${notation()} ${concrete()}"
    
    fun harTruffet() = fired
    fun ikkeHarTruffet() = !fired
}

/**
 * Extension for checking if at least one rule in a group fired.
 * Usable as Expression<Boolean> in HVIS/OG predicates.
 */
fun List<RuleResult>.minstEnHarTruffet(): Expression<Boolean> = object : Expression<Boolean> {
    override val value: Boolean get() = this@minstEnHarTruffet.any { it.harTruffet() }
    override fun notation() = "minst én av ${size} regler"
    override fun concrete() = if (value) "har truffet" else "ingen har truffet"
    override fun faktumSet(): Set<Faktum<*>> = emptySet()
    override fun toString() = "${notation()} ${concrete()}"
}

/**
 * Extension for checking if no rules in a group fired.
 * Usable as Expression<Boolean> in HVIS/OG predicates.
 */
fun List<RuleResult>.ingenHarTruffet(): Expression<Boolean> = object : Expression<Boolean> {
    override val value: Boolean get() = this@ingenHarTruffet.none { it.harTruffet() }
    override fun notation() = "ingen av ${size} regler"
    override fun concrete() = if (value) "har truffet" else "minst én har truffet"
    override fun faktumSet(): Set<Faktum<*>> = emptySet()
    override fun toString() = "${notation()} ${concrete()}"
}

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
     * Returns a RuleResult that can be used for introspection in subsequent rules.
     *
     * @param name The rule name (used in trace output)
     * @param builder DSL builder for the rule content (HVIS, OG, SÅ, RETURNER)
     * @return RuleResult indicating whether the rule fired (usable as Expression<Boolean>)
     */
    fun regel(name: String, builder: Rule<T>.() -> Unit): RuleResult {
        if (hasResult) return RuleResult(name, false)

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
        return RuleResult(name, ruleFired)
    }

    /**
     * Define a rule that applies to each element in a list (pattern).
     * Each element creates a separate rule with indexed naming (e.g., "RuleName.1", "RuleName.2").
     *
     * Returns a list of RuleResults, one per element.
     * 
     * @param name The base rule name
     * @param pattern List of elements to apply the rule to
     * @param builder DSL builder that receives the current element
     * @return List of RuleResults for introspection (e.g., minstEnHarTruffet())
     */
    fun <P> regel(name: String, pattern: List<P>, builder: Rule<T>.(P) -> Unit): List<RuleResult> {
        return pattern.mapIndexed { index, element ->
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
