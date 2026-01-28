package no.nav.system.ruledsl.core.resource

import kotlin.reflect.KClass

/**
 * Interface for accessing resources during rule evaluation.
 *
 * Resources are plugin components that provide external capabilities
 * (database connections, rate lookups, tracing, logging etc.) to rules.
 *
 * This interface exists to provide a common type for extension functions that need
 * to work in both [Rule] and [Ruleset] contexts. Both classes implement this interface,
 * so user-defined extension functions on [ResourceAccessor] are directly callable
 * within HVIS, SÅ, RETURNER blocks and at the Ruleset level.
 *
 * Example - defining an extension function:
 * ```kotlin
 * fun ResourceAccessor.grunnbeløp(dato: YearMonth): Int =
 *     getResource(GrunnbeløpSatsResource::class).lookup(dato)
 * ```
 *
 * Example - using in a rule (works because Rule implements ResourceAccessor):
 * ```kotlin
 * regel("calculate") {
 *     HVIS { ... }
 *     SÅ {
 *         val g = grunnbeløp(virkningstidspunkt)  // Extension function available here
 *     }
 * }
 * ```
 *
 * Example - using in a ruleset (works because Ruleset implements ResourceAccessor):
 * ```kotlin
 * regelsett {
 *     val g = grunnbeløp(virkningstidspunkt)  // Extension function available here too
 *     regel("...") { ... }
 * }
 * ```
 */
interface ResourceAccessor {
    /**
     * Retrieve a resource by its class type.
     *
     * @param key The class of the resource to retrieve
     * @return The resource instance
     * @throws IllegalStateException if resource not found
     */
    fun <T : Any> getResource(key: KClass<T>): T

    /**
     * Register a resource for use during rule evaluation.
     *
     * @param key The class of the resource (used as lookup key)
     * @param resource The resource instance
     */
    fun <T : Any> putResource(key: KClass<T>, resource: T)
}
