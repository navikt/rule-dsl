package no.nav.system.rule.dsl

import no.nav.system.rule.dsl.enums.RuleComponentType
import no.nav.system.rule.dsl.explanation.Hva
import no.nav.system.rule.dsl.explanation.Hvordan
import no.nav.system.rule.dsl.explanation.Hvorfor
import no.nav.system.rule.dsl.rettsregel.Faktum

/**
 * Lightweight wrapper that allows Faktum to be added to the ARC tree.
 *
 * This adapter enables Faktum (which is an Uttrykk, not an ARC) to participate
 * in the component tree hierarchy as a proper AbstractRuleComponent.
 *
 * Delegates explanation methods (Hvorfor, Hvordan) to the wrapped Faktum.
 *
 * Note: FaktumNode is created automatically by sporing() - users typically don't create these directly.
 *
 * Example:
 * ```
 * val faktum = sporing("alder", 67)  // Automatically creates and adds FaktumNode
 * ```
 */
class FaktumNode<T : Any>(val faktum: Faktum<T>) : AbstractRuleComponent(), Hvorfor, Hvordan {

    init {
        // Set backlink so Faktum can compute hvorfor by traversing up the tree
        faktum.wrapperNode = this
    }

    override fun name(): String = faktum.navn

    override fun fired(): Boolean = true

    override fun type(): RuleComponentType = RuleComponentType.FAKTUM

    // Delegate Hva to parent (uses name())
    // hva() is inherited from AbstractRuleComponent and returns "${type()}: ${name()}"

    /**
     * Computes the decision path by traversing up the tree from this FaktumNode.
     *
     * @param perspective Controls which nodes are included:
     *   - FULL: All ancestor nodes
     *   - FUNCTIONAL: Only decision nodes (rules, branches, predicates)
     */
    fun decisionPath(perspective: no.nav.system.rule.dsl.perspectives.Perspective = no.nav.system.rule.dsl.perspectives.Perspective.FUNCTIONAL): List<Hva> {
        val path = mutableListOf<Hva>()
        var current: AbstractRuleComponent? = this.parent

        while (current != null) {
            // Check if this node should be included based on perspective
            if (perspective.includes(current)) {
                // For rules in FUNCTIONAL perspective, also include predicates
                if (current.type() == RuleComponentType.REGEL && perspective == no.nav.system.rule.dsl.perspectives.Perspective.FUNCTIONAL) {
                    path.add(0, current)  // Add rule
                    // Add its predicates
                    current.children
                        .filterIsInstance<TrackablePredicate>()
                        .forEach { path.add(0, it) }
                } else {
                    path.add(0, current)
                }
            }
            current = current.parent
        }
        return path
    }

    /**
     * Returns the decision path using FUNCTIONAL perspective by default.
     * This shows only decision-relevant nodes (rules, branches, predicates).
     */
    override fun hvorfor() = decisionPath(no.nav.system.rule.dsl.perspectives.Perspective.FUNCTIONAL)

    override fun hvordan() = faktum.hvordan()

    override fun toString(): String = "faktum: ${faktum.navn} = ${faktum.verdi}"
}
