package no.nav.system.ruledsl.core.trace

import no.nav.system.ruledsl.core.expression.Expression
import no.nav.system.ruledsl.core.expression.Faktum
import no.nav.system.ruledsl.core.expression.Verdi
import no.nav.system.ruledsl.core.reference.Reference
import no.nav.system.ruledsl.core.resource.ResourceAccessor
import kotlin.experimental.ExperimentalTypeInference
import kotlin.reflect.KClass

/**
 * Sealed interface for rule predicates.
 * Enables exhaustive when-matching and clear type hierarchy.
 *
 * Two variants:
 * - Guard: Technical predicates (null-checks) that short-circuit on false
 * - Domain: Functional predicates (business logic) that are traced
 */
private sealed interface Predicate : Expression<Boolean> {
    
    /**
     * Guard predicate for technical evaluations (null-checks, validations).
     * An evaluation to false short-circuits the evaluation chain and prevents
     * further evaluation of any remaining predicates.
     */
    class Guard(private val evaluator: () -> Boolean) : Predicate {
        override val value: Boolean by lazy { evaluator() }
        override fun notation() = value.toString()
        override fun concrete() = value.toString()
        override fun toString(): String = value.toString()
        override fun faktumSet() = emptySet<Faktum<*>>()
    }
    
    /**
     * Domain predicate for functional business logic.
     * Defers evaluation of the producer lambda until value is accessed,
     * allowing guards to short-circuit before nullable expressions are evaluated.
     */
    class Domain(private val producer: () -> Expression<Boolean>) : Predicate {
        private val inner: Expression<Boolean> by lazy { producer() }
        override val value: Boolean get() = inner.value
        override fun notation(): String = inner.notation()
        override fun concrete(): String = inner.concrete()
        override fun toString(): String = inner.toString()
        override fun faktumSet(): Set<Faktum<*>> = inner.faktumSet()
    }
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
 * - RETURNER: Define value-producing action
 * - faktum(): Create and trace a Faktum (the only way to create Faktum)
 *
 * Cannot use both SÅ and RETURNER in the same rule.
 *
 * Type parameter T represents the result type. Can be any type.
 */
@OptIn(ExperimentalTypeInference::class)
class Rule<T : Any>(private val ruleContext: RuleContext) : ResourceAccessor {
    private val predicates = mutableListOf<Expression<Boolean>>()
    private val references = mutableListOf<Reference>()
    private var action: (Rule<T>.() -> Unit)? = null
    
    /**
     * Block that produces the rule's result of type T.
     * Stored for deferred execution after trace context is pushed.
     * If result is a Faktum, it will be automatically recorded to the trace.
     */
    private var resultBlock: (Rule<T>.() -> T)? = null
    private var resultValue: T? = null

    // ResourceAccessor delegation to Trace
    override fun <R : Any> getResource(key: KClass<R>): R = ruleContext.getResource(key)
    override fun <R : Any> putResource(key: KClass<R>, resource: R) = ruleContext.putResource(key, resource)

    /**
     * REF - attach a reference to legal source, documentation, or other resource.
     * Can be called multiple times to add multiple references.
     */
    fun REF(id: String, url: String) {
        references.add(Reference(id, url))
    }

    /**
     * Returns all references attached to this rule.
     */
    fun references(): List<Reference> = references.toList()

    /**
     * HVIS - technical predicate (null checks, validations).
     * Terminates evaluation on false to prevent NPE in subsequent predicates.
     */
    fun HVIS(booleanFunction: () -> Boolean) {
        predicates.add(Predicate.Guard(booleanFunction))
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
        predicates.add(Predicate.Domain(predicateFunction))
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
     * Use faktum() inside to create and trace calculations.
     * Extension functions on ResourceAccessor are available in this block.
     */
    fun SÅ(block: Rule<T>.() -> Unit) {
        action = block
    }

    fun <R : Any> faktum(
        name: String,
        value: R,
        references: List<Reference> = emptyList()
    ) = faktum(name, Verdi(value), references)

    /**
     * Creates a Faktum and automatically records it to the trace.
     * Delegates to RuleContext.faktum().
     *
     * This is the ONLY way to create a Faktum - the constructor is internal.
     * Every Faktum created this way is automatically traced.
     *
     * @param name The name of the faktum (appears in explanations)
     * @param expression The expression to wrap
     * @param references Optional references to legal sources or documentation
     * @return The created and recorded Faktum
     */
    fun <R : Any> faktum(
        name: String,
        expression: Expression<R>,
        references: List<Reference> = emptyList()
    ): Faktum<R> = ruleContext.faktum(name, expression, references)

    /**
     * RETURNER - value-producing action. Returns T.
     * If T is a Faktum, it is automatically recorded to the trace.
     * Stores the block for deferred execution (after trace context is pushed).
     * Extension functions on ResourceAccessor are available in this block.
     *
     * @param block Lambda that produces the result of type T
     */
    fun RETURNER(block: Rule<T>.() -> T) {
        resultBlock = block
    }

    /**
     * Evaluates all predicates and returns true if all pass.
     * Records each evaluated domain predicate directly via the callback.
     *
     * Short-circuit behavior:
     * - Technical predicates (null-checks, validations): terminate immediately on false
     *   to prevent NPE in subsequent predicates that depend on the checked value.
     * - Functional predicates (domain logic): continue evaluating even if false,
     *   so all functional predicate results are available for explanation/tracing.
     *
     * @param onDomainPredicate Callback invoked for each domain predicate as it's evaluated
     * @return true if all predicates pass
     */
    fun evaluate(onDomainPredicate: (Expression<Boolean>) -> Unit): Boolean {
        var result = true
        for (predicate in predicates) {
            result = result && predicate.value

            when (predicate) {
                is Predicate.Domain -> onDomainPredicate(predicate)
                is Predicate.Guard -> if (!result) return false
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
     *
     * Note: Faktum returned from this block should be created via faktum(),
     * which automatically records to trace. No special handling needed here.
     *
     * @return The result of type T
     */
    fun executeReturner(): T? {
        resultBlock?.let { block ->
            resultValue = block()
        }
        return resultValue
    }

}
