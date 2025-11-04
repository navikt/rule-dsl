package no.nav.system.rule.dsl

import no.nav.system.rule.dsl.enums.RuleComponentType
import no.nav.system.rule.dsl.error.ResourceAccessException
import no.nav.system.rule.dsl.forklaring.Grunnlag
import no.nav.system.rule.dsl.forklaring.UttrykkFormelAdapter
import no.nav.system.rule.dsl.formel.Formel
import no.nav.system.rule.dsl.inspections.hvorfor
import no.nav.system.rule.dsl.resource.Root
import no.nav.system.rule.dsl.resource.root
import no.nav.system.rule.dsl.rettsregel.forklartfaktum.ForklartFaktum
import java.io.Serializable
import kotlin.reflect.KClass

/**
 * Common functionality across all components of the DSL.
 *
 * All rulecomponents are organized in a tree of rulecomponents using [children]. Navigate the
 * rulecomponenttree by providing a [TreeVisitor] in the [accept] method.
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

    /**
     * Produserer ForklartFaktum med sporing og angitt Formel.
     */
    fun <T : Number> faktum(formel: Formel<T>): ForklartFaktum<T> {
        return ForklartFaktum(
            formel.name,
            formel.value,
            /**
             * Med utgangspunkt i root(), spor opp hvorfor denne (this@AbstractRuleComponent) has eksekvert.
             * Resultatet formes av "hvorfor-renderer".
             */
            this.root().hvorfor(target = this@AbstractRuleComponent),
            hvordan = formel
        )
    }

    /**
     * Produserer ForklartFaktum med sporing og angitt Uttrykk/Grunnlag.
     *
     * Dette er den foretrukne metoden for nye beregninger da den bruker Uttrykk-systemets
     * AST-baserte forklaringsgenerering. Den kombinerer:
     * - HVORFOR: Regelflyt-sporing via Trace.kt (hvilken vei gjennom regeltreet)
     * - HVORDAN: Beregningsforklaring via Uttrykk.forklarDetaljert() (hvordan verdien ble beregnet)
     *
     * Eksempel:
     * ```kotlin
     * regel("BeregnSlitertillegg") {
     *     HVIS { true }
     *     SÅ {
     *         val fulltSlitertillegg = (0.25 * G() / 12).navngi("fulltSlitertillegg")
     *         val justeringsFaktor = beregn...
     *         val slitertillegg = (fulltSlitertillegg * justeringsFaktor)
     *             .navngi("slitertillegg")
     *             .id("SLITERTILLEGG-BEREGNET")
     *
     *         RETURNER(faktum(slitertillegg))
     *     }
     * }
     * ```
     *
     * Output vil inneholde både regelsporing (HVORFOR) og AST-forklaring (HVORDAN):
     * ```
     * HVORFOR:
     *   BeregnSlitertilleggService
     *     BehandleSliterordningFlyt
     *       BeregnSlitertilleggRS
     *         BeregnSlitertillegg
     *
     * HVORDAN:
     *     SLITERTILLEGG-BEREGNET
     *     slitertillegg = fulltSlitertillegg * justeringsFaktor
     *     slitertillegg = 2292.0 * 0.33
     *     slitertillegg = 756.36
     *
     *     fulltSlitertillegg = 0.25 * G / 12
     *     fulltSlitertillegg = 0.25 * 110000 / 12
     *     fulltSlitertillegg = 2292.0
     *     ...
     * ```
     *
     * @param grunnlag Uttrykk-basert Grunnlag som inneholder AST-struktur for beregningen
     * @return ForklartFaktum med kombinert HVORFOR + HVORDAN forklaring
     * @see UttrykkFormelAdapter for detaljer om integrasjonen
     */
    fun <T : Number> faktum(grunnlag: Grunnlag<T>): ForklartFaktum<T> {
        return faktum(UttrykkFormelAdapter(grunnlag))
    }

}