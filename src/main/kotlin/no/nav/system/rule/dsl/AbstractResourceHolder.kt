package no.nav.system.rule.dsl

import kotlin.reflect.KClass

abstract class AbstractResourceHolder : AbstractRuleComponent() {

    internal var resourceMap: MutableMap<KClass<*>, AbstractResource> = mutableMapOf()

    fun <T : AbstractResource> putResource(key: KClass<T>, service: T) {
        resourceMap[key] = service
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : AbstractResource> getResource(key: KClass<T>): T {
        return resourceMap[key] as T
    }
}