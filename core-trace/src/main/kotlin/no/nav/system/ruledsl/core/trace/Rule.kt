package no.nav.system.ruledsl.core.trace

import no.nav.system.ruledsl.core.expression.Expression
import no.nav.system.ruledsl.core.expression.Faktum
import no.nav.system.ruledsl.core.expression.boolean.GuardExpression
import kotlin.experimental.ExperimentalTypeInference

/**
 * Rule supporting both side-effects (SÅ) and value-producing (RETURNER).
 *
 * DSL keywords:
 * - HVIS / OG: Define predicates (technical or domain)
 * - SÅ: Define side-effect action
 * - RETURNER: Define value-producing action (returns Faktum)
 * - SPOR: Explicitly trace a Faktum within SÅ block
 *
 * Cannot use both SÅ and RETURNER in the same rule.
 */
@OptIn(ExperimentalTypeInference::class)
class Rule<T>(private val trace: Trace) {
    private val predicates = mutableListOf<Expression<Boolean>>()
    private var action: (context(Trace) () -> Unit)? = null
    private var resultBlock: (context(Trace) () -> Faktum<out Any>)? = null
    private var resultFaktum: Faktum<out Any>? = null

    /**
     * HVIS - technical predicate (null checks, validations).
     * Terminates evaluation on false to prevent NPE in subsequent predicates.
     */
    fun HVIS(booleanFunction: () -> Boolean) {
        predicates.add(GuardExpression(booleanFunction))
    }

    /**
     * OG - technical predicate (additional condition).
     * Alias for HVIS.
     */
    fun OG(predicate: () -> Boolean) = HVIS(predicate)

    /**
     * HVIS - domain predicate (functional business logic).
     * Does not short-circuit, allowing all domain predicates to be evaluated for tracing.
     */
    @JvmName("HVIS_domain")
    @OverloadResolutionByLambdaReturnType
    fun HVIS(predicateFunction: () -> Expression<Boolean>) {
        predicates.add(predicateFunction())
    }

    /**
     * OG - domain predicate (additional functional condition).
     * Alias for HVIS with domain predicate.
     */
    @JvmName("OG_domain")
    @OverloadResolutionByLambdaReturnType
    fun OG(predicateFunction: () -> Expression<Boolean>) = HVIS(predicateFunction)

    /**
     * SÅ - side-effect action (no return value).
     * Use SPOR inside to trace Faktum calculations.
     */
    fun SÅ(block: context(Trace) () -> Unit) {
        action = block
    }

    /**
     * SPOR - explicitly records a Faktum to the trace within a SÅ block.
     * Returns the Faktum for further use.
     *
     * @param faktum The Faktum to trace
     * @return The same Faktum (for chaining)
     */
    fun <R : Any> SPOR(faktum: Faktum<R>): Faktum<R> {
        trace.recordFaktum(faktum)
        return faktum
    }

    /**
     * RETURNER - value-producing action. Returns Faktum<R>.
     * The returned Faktum is automatically traced.
     * Stores the block for deferred execution (after trace context is pushed).
     *
     * @param block Lambda that produces the Faktum result
     */
    fun <R : Any> RETURNER(block: context(Trace) () -> Faktum<R>) {
        resultBlock = block
    }

    /**
     * Evaluates all predicates and returns true if all pass.
     *
     * Short-circuit behavior:
     * - Technical predicates (null-checks, validations): terminate immediately on false
     *   to prevent NPE in subsequent predicates that depend on the checked value.
     * - Functional predicates (domain logic): continue evaluating even if false,
     *   so all functional predicate results are available for explanation/tracing.
     */
    fun evaluate(): Boolean {
        var result = true
        for (predicate in predicates) {
            result = result && predicate.value

            if (!result && predicate is GuardExpression) {
                return false
            }
        }

        return result
    }

    fun hasAction(): Boolean = action != null

    fun executeAction() {
        action?.let { 
            with(trace) {
                it()
            }
        }
    }

    fun hasReturner(): Boolean = resultBlock != null

    /**
     * Executes the RETURNER block. Must be called after trace context is pushed.
     * Automatically records the returned Faktum to the trace.
     */
    fun executeReturner(): Faktum<out Any>? {
        resultBlock?.let { block ->
            with(trace) {
                resultFaktum = block()
                resultFaktum?.let { trace.recordFaktum(it) }
            }
        }
        return resultFaktum
    }

    /**
     * Returns domain predicates (non-guard expressions) for tracing.
     */
    fun expressions(): List<Expression<Boolean>> = predicates.filterNot { it is GuardExpression }
}
