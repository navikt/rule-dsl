package no.nav.system.rule.dsl

import no.nav.system.rule.dsl.treevisitor.visitor.TreeVisitor
import org.jetbrains.annotations.NotNull
import kotlin.reflect.KClass

/**
 * Common functionality across all components of the DSL.
 *
 * All rulecomponents are organized in a tree of rulecomponents using [children] and [parent]. Navigate the
 * rulecomponenttree by providing a [TreeVisitor] in the [accept] method.
 *
 * A [resourceMap] keeps track of all instantiated resources for convinient access during rule processing.
 * Resources are all classes that needs to be instantiated once per ruleService call, typically resources
 * are Rates (norsk "Sats"), Console-capture or anything else non-static.
 */
abstract class AbstractRuleComponent {
    val children: MutableList<AbstractRuleComponent> = mutableListOf()
    var parent: AbstractRuleComponent? = null
        set(@NotNull parent) {
            field = parent
            this.resourceMap = parent!!.resourceMap
        }

    // TODO resourceMap og tilhørende funksjoner bør flyttes ut i eget interface som implementeres kun der det er nødvendig (dvs ikke i Sumsumsjon / Predicate)
    private var resourceMap: MutableMap<KClass<*>, AbstractResource> = mutableMapOf()

    fun <T : AbstractResource> putResource(key: KClass<T>, service: T) {
        resourceMap[key] = service
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : AbstractResource> getResource(key: KClass<T>): T {
        return resourceMap[key] as T
    }

    abstract fun name(): String
    // TODO En RuleComponentType enum er kanskje bedre enn String her.
    abstract fun type(): String
    abstract fun fired(): Boolean

    /**
     * Accepts implementations of [TreeVisitor] for operations on the ruleComponent-tree.
     */
    fun accept(treeVisitor: TreeVisitor) {
        treeVisitor.visit(this)
    }
}