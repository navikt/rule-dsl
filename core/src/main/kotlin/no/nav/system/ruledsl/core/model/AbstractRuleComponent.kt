package no.nav.system.ruledsl.core.model

import no.nav.system.ruledsl.core.enums.RuleComponentType
import no.nav.system.ruledsl.core.error.ResourceAccessException
import no.nav.system.ruledsl.core.reference.Reference
import no.nav.system.ruledsl.core.resource.AbstractResource
import no.nav.system.ruledsl.core.resource.Root
import no.nav.system.ruledsl.core.resource.tracker.NoOpTracker
import no.nav.system.ruledsl.core.resource.tracker.TrackerResource
import no.nav.system.ruledsl.core.rettsregel.Const
import no.nav.system.ruledsl.core.rettsregel.Faktum
import no.nav.system.ruledsl.core.rettsregel.Uttrykk
import java.io.Serializable
import kotlin.reflect.KClass

/**
 * Common functionality across all components of the DSL.
 *
 * All rulecomponents are organized in a tree using [children].
 * FaktumNode (wrapping Faktum) is also an AbstractRuleComponent, so the tree
 * naturally includes both orchestration (Rules, Rulesets) and data (Faktum).
 *
 * A [resourceMap] keeps track of all instantiated resources for convenient access during rule processing.
 * Resources are all classes that needs to be instantiated once per ruleService call, typically resources
 * are Rates (norsk "Sats"), Console-capture or anything else non-static.
 */
abstract class AbstractRuleComponent : Serializable {
    private val _children: MutableList<AbstractRuleComponent> = mutableListOf()

    /**
     * Read-only view of child components.
     * Use addChild() to add new children - this maintains parent pointers and propagates resourceMap.
     */
    val children: List<AbstractRuleComponent> get() = _children

    internal var resourceMap: MutableMap<KClass<*>, AbstractResource> = mutableMapOf()

    /**
     * Parent component in the ARC tree.
     * Enables upward traversal for computing Faktum.hvorfor() and other analyses.
     */
    var parent: AbstractRuleComponent? = null

    /**
     * References to external documentation, legal sources, or other resources.
     */
    val references: MutableList<Reference> = mutableListOf()

    init {
        if (!resourceMap.containsKey(Root::class)) {
            /**
             * Adds arc-reference as function to avoid leaking 'this' in constructor.
             */
            putResource(Root::class, Root(arc = { this }))
        }
        // Guarantee tracker is always present (defaults to NoOpTracker)
        if (!resourceMap.containsKey(TrackerResource::class)) {
            putResource(TrackerResource::class, NoOpTracker())
        }
    }

    /**
     * Adds a child component and maintains bidirectional parent-child links.
     * Also propagates resourceMap to the child.
     *
     * This is the ONLY way to add children - ensures tree invariants are maintained.
     */
    fun addChild(child: AbstractRuleComponent) {
        child.parent = this
        child.resourceMap = this.resourceMap
        _children.add(child)
    }

    abstract fun name(): String
    abstract fun type(): RuleComponentType
    abstract fun fired(): Boolean

    /**
     * Adds a resource to the ARC-platform.
     * Only a single instance of TrackerResource is maintained in the resourceMap.
     * // TODO Add conflict check.
     */
    fun <T : AbstractResource> putResource(key: KClass<T>, service: T) {
        if (key.isInstance(TrackerResource::class))
            resourceMap[TrackerResource::class] = service
        else
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
     * Get the TrackerResource for this component.
     * Always returns a tracker (defaults to NoOpTracker if none registered).
     */
    fun tracker(): TrackerResource<*> {
        return getResource(TrackerResource::class)
    }

    /**
     * Produserer Faktum med hvorfor-sporing og angitt Uttrykk.
     *
     * Adds the Faktum to the component tree via FaktumNode wrapper.
     * The hvorfor path is computed dynamically by traversing up the tree from the FaktumNode.
     */
    fun <T : Any> sporing(navn: String, uttrykk: Uttrykk<T>): Faktum<T> {
        return Faktum(
            navn = navn,
            uttrykk = uttrykk
        ).also {
            // Add to tree via wrapper
            addChild(FaktumNode(it))
            // Notify tracker
            tracker().onFaktumCreated(it, this)
        }
    }

    /**
     * Produserer ForklartFaktum med sporing og angitt verdi.
     *
     * Adds the Faktum to the component tree via FaktumNode wrapper.
     * The hvorfor path is computed dynamically by traversing up the tree from the FaktumNode.
     */
    fun <T : Any> sporing(navn: String, verdi: T): Faktum<T> {
        return Faktum(
            navn = navn,
            uttrykk = Const(verdi)
        ).also {
            // Add to tree via wrapper
            addChild(FaktumNode(it))
            // Notify tracker
            tracker().onFaktumCreated(it, this)
        }
    }

}