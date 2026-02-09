package no.nav.system.ruledsl.core.trace

import no.nav.system.ruledsl.core.expression.Expression
import no.nav.system.ruledsl.core.expression.Faktum
import no.nav.system.ruledsl.core.reference.Reference
import no.nav.system.ruledsl.core.resource.ResourceAccessor
import kotlin.reflect.KClass
import kotlin.reflect.cast

/**
 * Execution context for rule evaluation.
 *
 * Combines resource management with optional tracing (WHY/HOW).
 * Tracing is itself a resource - stored in the internal resources map.
 * 
 * Extension functions on RuleContext provide domain-specific helpers
 * accessible within Rule and Ruleset blocks.
 *
 * @param resources Pre-populated map of resources. Must contain exactly one Tracer (defaults to NoOpTracer).
 */
class RuleContext(
    private val resources: MutableMap<KClass<*>, Any> = mutableMapOf(),
) : ResourceAccessor {

    init {
        resources.putIfAbsent(Tracer::class, NoOpTracer())
    }

    override fun <T : Any> getResource(key: KClass<T>): T {
        val resource = resources[key]
            ?: throw IllegalStateException("No resource found for $key")
        return key.cast(resource)
    }

    override fun <T : Any> putResource(key: KClass<T>, resource: T) {
        resources[key] = resource
        if (resource is Tracer) {
            val tracerCount = resources.values.count { it is Tracer }
            require(tracerCount == 1) {
                "RuleContext must contain at most one Tracer implementation, found $tracerCount"
            }
        }
    }

    /**
     * Creates a Faktum and automatically records it to the trace.
     *
     * This is the ONLY way to create a Faktum - the constructor is internal.
     * Every Faktum created this way is automatically traced.
     *
     * @param name The name of the faktum (appears in explanations)
     * @param expression The expression to wrap
     * @param references Optional references to legal sources or documentation
     * @return The created and recorded Faktum
     */
    fun <T : Any> faktum(
        name: String,
        expression: Expression<T>,
        references: List<Reference> = emptyList()
    ): Faktum<T> {
        val faktum = Faktum.create(name, expression, references)
        tracer().recordExpression(faktum)
        return faktum
    }

}

/**
 * Extension function to get the tracer from resources.
 * This demonstrates the same pattern users should follow for their own resources.
 */
fun ResourceAccessor.tracer(): Tracer = getResource(Tracer::class)

/**
 * Extension function to get the trace root.
 */
fun ResourceAccessor.root(): RuleTrace = tracer().root()

/**
 * Extension function to get the debug tree output.
 */
fun ResourceAccessor.debugTree(): String = tracer().debugTree()