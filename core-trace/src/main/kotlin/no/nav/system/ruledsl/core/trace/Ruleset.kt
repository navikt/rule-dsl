package no.nav.system.ruledsl.core.trace

/**
 * Regelsett - collects results from rules, implements first-match-wins semantics.
 *
 * Rules are evaluated in sequence. The first rule that fires and produces a result
 * (via RETURNER) stops further evaluation. Side-effect rules (via SÅ) do not stop evaluation.
 */
class Ruleset<T>(private val trace: Trace) {
    var result: T? = null
        private set
    var hasResult = false
        private set

    /**
     * Define a rule that supports both side-effects (SÅ) and value-producing (RETURNER).
     * Use SÅ for side-effects, RETURNER for returning Faktum<T>.
     * Cannot use both in the same rule.
     *
     * @param name The rule name (used in trace output)
     * @param builder DSL builder for the rule content (HVIS, OG, SÅ, RETURNER)
     */
    fun regel(name: String, builder: context(Trace) Rule<T>.() -> Unit) {
        if (hasResult) return

        val rule = Rule<T>(trace)
        with(trace) {
            rule.builder()
        }

        val ruleFired = rule.evaluate()
        val execution = trace.recordRule(name, ruleFired, rule.expressions())
        trace.pushContext(execution)

        if (ruleFired) {
            when {
                rule.hasReturner() && rule.hasAction() -> 
                    throw IllegalStateException("regel '$name' cannot have both SÅ and RETURNER")
                
                rule.hasReturner() -> {
                    val expression = rule.executeReturner()!!
                    @Suppress("UNCHECKED_CAST")
                    result = expression as T
                    hasResult = true
                }
                
                rule.hasAction() -> {
                    rule.executeAction()
                }
            }
        }

        trace.popContext()
    }
}

/**
 * Entry point for traced rule evaluation.
 * Evaluates rules in sequence, returns first match.
 *
 * For non-Unit types: throws IllegalStateException if no rule matched.
 * For Unit: returns without throwing.
 *
 * @param block DSL block containing regel definitions
 * @return The result from the first matching rule with RETURNER
 */
context(trace: Trace)
inline fun <reified T> traced(block: context(Trace) Ruleset<T>.() -> Unit): T {
    val scope = Ruleset<T>(trace)
    with(trace) {
        scope.block()
    }

    return when {
        scope.hasResult -> scope.result!!
        T::class == Unit::class -> Unit as T
        else -> throw IllegalStateException("No rule matched")
    }
}
