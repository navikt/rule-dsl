package no.nav.system.rule.dsl.perspectives

import no.nav.system.rule.dsl.inspections.printTree
import no.nav.system.rule.dsl.resource.ExecutionTrace
import no.nav.system.rule.dsl.rettsregel.Faktum

/**
 * Perspective functions for ExecutionTrace.
 *
 * These functions provide different views of the same execution data:
 * - FullPerspective: Complete trace (everything)
 * - FunctionalPerspective: Decision nodes only
 * - UttrykksTreePerspective: Formula visualization
 * - FaktumPerspective: Bottom-up explanation from a Faktum
 *
 * Part of the siloed architecture: Execution → Tracing → Perspectives
 */

/**
 * FullPerspective: Complete execution trace as formatted string.
 * Shows all components that executed, including containers (rulesets, ruleflows).
 *
 * Use case: Complete audit trail, debugging, compliance
 */
fun ExecutionTrace.toFullString(): String {
    val path = fullPath()

    if (path.isEmpty()) {
        return "Ingen kjøring registrert (Empty execution trace)"
    }

    return buildString {
        appendLine("=== Full Execution Trace ===")
        appendLine()

        path.forEachIndexed { index, uttrykk ->
            val indent = "  ".repeat(index.coerceAtMost(10)) // Cap indentation at reasonable level
            appendLine("$indent${index + 1}. ${uttrykk.notasjon()} = ${uttrykk.konkret()}")
        }

        appendLine()
        appendLine("=== End of Trace (${path.size} components) ===")
    }
}

/**
 * FunctionalPerspective: Filtered execution trace showing only decision nodes.
 * Shows rules, branches, and predicates - hides technical containers.
 *
 * Use case: Business analysts, functional documentation
 */
fun ExecutionTrace.toFunctionalString(): String {
    val path = pathForHvorfor() // Already filtered for decision nodes

    if (path.isEmpty()) {
        return "Ingen beslutninger registrert (No decisions recorded)"
    }

    return buildString {
        appendLine("=== Functional Execution Path ===")
        appendLine()

        path.forEach { uttrykk ->
            appendLine("  • ${uttrykk.notasjon()} = ${uttrykk.konkret()}")
        }

        appendLine()
        appendLine("=== End of Path (${path.size} decisions) ===")
    }
}

/**
 * UttrykksTreePerspective: Visualizes a Faktum's formula structure.
 * Uses UttrykksTreePrinter to show calculation tree with deduplication.
 *
 * Use case: Technical testers, formula verification
 */
fun ExecutionTrace.toUttrykksTree(faktum: Faktum<*>): String {
    return buildString {
        appendLine("=== Formula Tree for '${faktum.navn}' ===")
        appendLine()
        appendLine(faktum.printTree())
        appendLine()
        appendLine("=== End of Formula Tree ===")
    }
}

/**
 * FaktumPerspective: Bottom-up explanation starting from a Faktum.
 * Combines:
 * - WHAT: The faktum value
 * - HOW: Formula structure (if not a constant)
 * - WHY: Execution context from hvorfor
 *
 * Use case: Explaining specific calculation results
 */
fun ExecutionTrace.toFaktumExplanation(faktum: Faktum<*>): String {
    return buildString {
        appendLine("=== Explanation for '${faktum.navn}' ===")
        appendLine()

        // Use the existing forklar() method which already provides WHAT/WHY/HOW
        appendLine(faktum.forklar())

        appendLine()
        appendLine("=== End of Explanation ===")
    }
}
