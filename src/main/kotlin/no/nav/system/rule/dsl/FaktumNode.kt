package no.nav.system.rule.dsl

import no.nav.system.rule.dsl.enums.RuleComponentType
import no.nav.system.rule.dsl.rettsregel.Faktum

/**
 * Minimal adapter that allows Faktum (pure data) to participate in the ARC tree.
 *
 * This lightweight wrapper exists because:
 * - Not all Faktum should be in the ARC tree (inline expressions should stay lightweight)
 * - Only Faktum created via sporing() need ARC tree participation
 *
 * FaktumNode provides ONLY the ARC interface - all presentation logic
 * (explanation, perspective filtering, etc.) lives in extension functions.
 *
 * Created automatically by sporing() - users don't instantiate this directly.
 *
 * Example:
 * ```
 * val faktum = sporing("alder", 67)  // Creates FaktumNode internally
 * ```
 */
class FaktumNode<T : Any>(val faktum: Faktum<T>) : AbstractRuleComponent() {

    init {
        // Backlink allows extension functions to access ARC tree from Faktum
        faktum.wrapperNode = this
    }

    override fun name(): String = faktum.navn

    override fun fired(): Boolean = true

    override fun type(): RuleComponentType = RuleComponentType.FAKTUM

    // hva() inherited from AbstractRuleComponent: "${type()}: ${name()}"

    override fun toString(): String = "faktum: ${faktum.navn} = ${faktum.verdi}"
}
