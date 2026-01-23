package no.nav.system.ruledsl.core.resource

import kotlin.reflect.KClass

/**
 * Interface for accessing resources during rule evaluation.
 *
 * Resources are plugin components that provide external capabilities
 * (database connections, rate lookups, configuration, etc.) to rules.
 *
 * Users can create extension functions on RuleContext to provide
 * domain-specific helpers that are directly callable within SÅ/RETURNER blocks:
 *
 * ```kotlin
 * fun RuleContext.grunnbeløp(dato: YearMonth): Int =
 *     getResource(GrunnbeløpSatsResource::class).lookup(dato)
 *
 * // Usage in rule:
 * regel("calculate") {
 *     HVIS { ... }
 *     SÅ {
 *         val g = grunnbeløp(virkningstidspunkt)
 *     }
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
