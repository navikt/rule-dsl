package no.nav.system.rule.dsl

import kotlin.reflect.KClass

interface ResourceHolder {

    // TODO resourceMap og tilhørende funksjoner bør flyttes ut i eget interface som implementeres kun der det er nødvendig (dvs ikke i Sumsumsjon / Predicate)
    var resourceMap: MutableMap<KClass<*>, AbstractResource>
        get() = mutableMapOf()
        set(value) = throw IllegalAccessError("fy")

    fun <T : AbstractResource> putResource(key: KClass<T>, service: T) {
        resourceMap[key] = service
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : AbstractResource> getResource(key: KClass<T>): T {
        return resourceMap[key] as T
    }
}