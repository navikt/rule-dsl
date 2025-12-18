package no.nav.system.ruledsl.core.trace

import no.nav.system.ruledsl.core.expression.Expression
import no.nav.system.ruledsl.core.expression.Faktum
import no.nav.system.ruledsl.core.resource.ResourceAccessor
import kotlin.experimental.ExperimentalTypeInference
import kotlin.reflect.KClass

/**
 * Lazy wrapper for domain predicates.
 * Defers evaluation of the producer lambda until value is accessed,
 * allowing guards to short-circuit before nullable expressions are evaluated.
 *
 * Internal to Rule - not exposed to users.
 */
private class DomainPredicate(
    private val producer: () -> Expression<Boolean>
) : Expression<Boolean> {
    private val inner: Expression<Boolean> by lazy { producer() }
    override val value: Boolean get() = inner.value
    override fun notation(): String = inner.notation()
    override fun concrete(): String = inner.concrete()
    override fun toString(): String = inner.toString()
    override fun faktumSet(): Set<Faktum<*>> = inner.faktumSet()
}
/**
 * Lazy wrapper for guard predicates.
 * Typically for technical evaluations (null-checks).
 * An evaluation to false short-circuits the evaluationchain and prevents
 * further evaluation of any remaining predicates.
 *
 * Internal to Rule - not exposed to users.
 */
private class GuardPredicate(
    private val evaluator: () -> Boolean
) : Expression<Boolean> {
    override val value: Boolean by lazy { evaluator() }
    override fun notation() = value.toString()
    override fun concrete() = value.toString()
    override fun toString(): String = value.toString()
    override fun faktumSet() = emptySet<Faktum<*>>()
}

/**
 * Rule supporting both side-effects (SÅ) and value-producing (RETURNER).
 *
 * Implements ResourceAccessor to allow extension functions for resource access
 * to be called directly within SÅ/RETURNER blocks.
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
class Rule<T>(private val trace: Trace) : ResourceAccessor {
    private val predicates = mutableListOf<Expression<Boolean>>()
    private var action: (Rule<T>.() -> Unit)? = null
    
    /**
     * Block that produces the rule's result Faktum.
     * Stored for deferred execution after trace context is pushed.
     * 
     * Note: Uses Faktum<out Any> because the Rule's type parameter T represents
     * the full result type (which is typically Faktum<X>), not the inner value type.
     * This allows flexibility in what RETURNER can return while still working
     * with the traced/Ruleset infrastructure.
     */
    private var resultBlock: (Rule<T>.() -> Faktum<out Any>)? = null
    private var resultFaktum: Faktum<out Any>? = null

    // ResourceAccessor delegation to Trace
    override fun <T : Any> getResource(key: KClass<T>): T = trace.getResource(key)
    override fun <T : Any> putResource(key: KClass<T>, resource: T) = trace.putResource(key, resource)

    /**
     * HVIS - technical predicate (null checks, validations).
     * Terminates evaluation on false to prevent NPE in subsequent predicates.
     */
    fun HVIS(booleanFunction: () -> Boolean) {
        predicates.add(GuardPredicate(booleanFunction))
    }

    /**
     * OG - technical predicate (additional condition).
     * Alias for HVIS.
     */
    fun OG(predicate: () -> Boolean) = HVIS(predicate)

    /**
     * HVIS - domain predicate (functional business logic).
     * Wrapped in LazyDomainPredicate to defer evaluation until value is accessed,
     * allowing guards to short-circuit before nullable expressions are evaluated.
     */
    @JvmName("HVIS_domain")
    @OverloadResolutionByLambdaReturnType
    fun HVIS(predicateFunction: () -> Expression<Boolean>) {
        predicates.add(DomainPredicate(predicateFunction))
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
     * Extension functions on ResourceAccessor are available in this block.
     */
    fun SÅ(block: Rule<T>.() -> Unit) {
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
     * Extension functions on ResourceAccessor are available in this block.
     *
     * @param block Lambda that produces the Faktum result
     */
    fun <R : Any> RETURNER(block: Rule<T>.() -> Faktum<R>) {
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

            if (!result && predicate is GuardPredicate) {
                return false
            }
        }

        return result
    }

    fun hasAction(): Boolean = action != null

    fun executeAction() {
        action?.invoke(this)
    }

    fun hasReturner(): Boolean = resultBlock != null

    /**
     * Executes the RETURNER block. Must be called after trace context is pushed.
     * Automatically records the returned Faktum to the trace.
     * 
     * @return The Faktum containing the rule's result
     */
    fun executeReturner(): Faktum<out Any>? {
        resultBlock?.let { block ->
            resultFaktum = block()
            resultFaktum?.let { trace.recordFaktum(it) }
        }
        return resultFaktum
    }

    /**
     * Returns domain predicates (non-guard expressions) for tracing.
     */
    fun expressions(): List<Expression<Boolean>> = predicates.filterNot { it is GuardPredicate }
}
