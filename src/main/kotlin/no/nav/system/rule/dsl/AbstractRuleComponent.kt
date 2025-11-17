package no.nav.system.rule.dsl

import no.nav.system.rule.dsl.enums.RuleComponentType
import no.nav.system.rule.dsl.error.ResourceAccessException
import no.nav.system.rule.dsl.resource.ExecutionTrace
import no.nav.system.rule.dsl.resource.Root
import no.nav.system.rule.dsl.rettsregel.Const
import no.nav.system.rule.dsl.rettsregel.Faktum
import no.nav.system.rule.dsl.rettsregel.Uttrykk
import java.io.Serializable
import kotlin.reflect.KClass

/**
 * Common functionality across all components of the DSL.
 *
 * All rulecomponents are organized in a tree of rulecomponents using [children].
 *
 * A [resourceMap] keeps track of all instantiated resources for convenient access during rule processing.
 * Resources are all classes that needs to be instantiated once per ruleService call, typically resources
 * are Rates (norsk "Sats"), Console-capture or anything else non-static.
 */
abstract class AbstractRuleComponent : Serializable {
    val children: MutableList<AbstractRuleComponent> = mutableListOf()
    internal var resourceMap: MutableMap<KClass<*>, AbstractResource> = mutableMapOf()

    init {
        if (!resourceMap.containsKey(Root::class)) {
            /**
             * Adds arc-reference as function to avoid leaking 'this' in constructor.
             */
            putResource(Root::class, Root(arc = { this }))
        }
    }

    abstract fun name(): String
    abstract fun type(): RuleComponentType
    abstract fun fired(): Boolean

    /**
     * Converts this component to an Uttrykk for trace representation.
     * Each component decides how to represent itself in execution traces.
     */
    abstract fun toUttrykk(): Uttrykk<*>

    fun <T : AbstractResource> putResource(key: KClass<T>, service: T) {
        resourceMap[key] = service
    }

    fun <T : AbstractResource> getResource(key: KClass<T>): T {
        if (resourceMap.isEmpty()) throw ResourceAccessException("ResourceMap is empty for class '${this.javaClass.name}'")

        val resource = resourceMap[key]
            ?: throw ResourceAccessException("No resource found for $key.")

        if (!key.isInstance(resource)) {
            throw ResourceAccessException(
                "Type mismatch for resource key $key. Expected: ${key.qualifiedName}, Found: ${resource.javaClass.name}"
            )
        }

        @Suppress("UNCHECKED_CAST")
        return resource as T
    }

    fun <T : AbstractResource> getResourceOrNull(key: KClass<T>): T? {
        val resource = resourceMap[key]

        if (resource != null && !key.isInstance(resource)) {
            throw ResourceAccessException(
                "Type mismatch for resource key $key. Expected: ${key.qualifiedName}, Found: ${resource.javaClass.name}"
            )
        }

        @Suppress("UNCHECKED_CAST")
        return resource as T?
    }

    /**
     * Produserer Faktum med hvorfor-sporing og angitt Uttrykk.
     *
     * Captures the current execution path from the ExecutionTrace resource (if enabled).
     */
    fun <T : Any> sporing(navn: String, uttrykk: Uttrykk<T>): Faktum<T> {
        return Faktum(
            navn = navn,
            uttrykk = uttrykk,
            hvorfor = getResourceOrNull(ExecutionTrace::class)?.pathForHvorfor()
        )
    }

    /**
     * Produserer ForklartFaktum med sporing og angitt verdi.
     *
     * Captures the current execution path from the ExecutionTrace resource (if enabled).
     */
    fun <T : Any> sporing(navn: String, verdi: T): Faktum<T> {
        return Faktum(
            navn = navn,
            uttrykk = Const(verdi),
            hvorfor = getResourceOrNull(ExecutionTrace::class)?.pathForHvorfor()
        )
    }

}