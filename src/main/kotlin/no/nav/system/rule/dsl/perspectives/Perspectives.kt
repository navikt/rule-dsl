package no.nav.system.rule.dsl.perspectives

import no.nav.system.rule.dsl.inspections.printTree
import no.nav.system.rule.dsl.rettsregel.Faktum

/**
 * Perspective functions for Faktum.
 *
 * NOTE: ExecutionTrace has been removed. All perspectives are now tree-based:
 * - Use arc.traverseHva() for complete structure
 * - Use arc.traverseFull() for detailed explanations
 * - Use arc.collectFaktum() for all Faktum
 * - Use faktum.forklar() for bottom-up explanation
 * - Use faktum.printTree() for formula visualization
 */

/**
 * UttrykksTreePerspective: Visualizes a Faktum's formula structure.
 * Uses UttrykksTreePrinter to show calculation tree with deduplication.
 *
 * Use case: Technical testers, formula verification
 */
fun Faktum<*>.toUttrykksTree(): String {
    return buildString {
        appendLine("=== Formula Tree for '${this@toUttrykksTree.navn}' ===")
        appendLine()
        appendLine(this@toUttrykksTree.printTree())
        appendLine()
        appendLine("=== End of Formula Tree ===")
    }
}

/**
 * FaktumPerspective: Bottom-up explanation starting from a Faktum.
 * Combines:
 * - WHAT: The faktum value
 * - HOW: Formula structure (if not a constant)
 * - WHY: Execution context from hvorfor (computed by tree traversal)
 *
 * Use case: Explaining specific calculation results
 */
fun Faktum<*>.toFaktumExplanation(): String {
    return buildString {
        appendLine("=== Explanation for '${this@toFaktumExplanation.navn}' ===")
        appendLine()

        // Use the existing forklar() method which already provides WHAT/WHY/HOW
        appendLine(this@toFaktumExplanation.forklar())

        appendLine()
        appendLine("=== End of Explanation ===")
    }
}
