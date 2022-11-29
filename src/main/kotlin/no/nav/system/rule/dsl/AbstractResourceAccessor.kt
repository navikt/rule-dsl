package no.nav.system.rule.dsl

import no.nav.system.rule.dsl.error.ResourceAccessException
import no.nav.system.rule.dsl.resource.Root
import kotlin.reflect.KClass

abstract class AbstractResourceAccessor : AbstractRuleComponent() {
    internal var resourceMap: MutableMap<KClass<*>, AbstractResource> = mutableMapOf()

    init {
        if (!resourceMap.containsKey(Root::class)) {
            /**
             * Adds arc-reference as function to avoid leaking 'this' in constructor.
             */
            putResource(Root::class, Root(arc = { this }))
        }
    }

    fun <T : AbstractResource> putResource(key: KClass<T>, service: T) {
        resourceMap[key] = service
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : AbstractResource> getResource(key: KClass<T>): T {
        if (resourceMap.isEmpty()) throw ResourceAccessException("ResourceMap is empty for class '${this.javaClass.name}'")
        if (!resourceMap.containsKey(key)) throw ResourceAccessException("No resource found for $key.")

        return resourceMap[key] as T
    }
}