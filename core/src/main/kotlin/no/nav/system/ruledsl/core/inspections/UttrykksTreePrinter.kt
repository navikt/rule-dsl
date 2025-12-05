package no.nav.system.ruledsl.core.inspections

import no.nav.system.ruledsl.core.model.ComparisonOperation
import no.nav.system.ruledsl.core.model.Const
import no.nav.system.ruledsl.core.model.Faktum
import no.nav.system.ruledsl.core.model.ListOperation
import no.nav.system.ruledsl.core.model.MathOperation
import no.nav.system.ruledsl.core.model.Uttrykk

/**
 * Visualizes an Uttrykk (expression tree) as a hierarchical tree structure
 * with deduplication tracking.
 *
 * Features:
 * - Box-drawing characters for tree visualization
 * - Deduplication: First occurrence shows full subtree, subsequent show reference
 * - Display both symbolic names and concrete values
 *
 * Example output:
 * ```
 * Grunnlag(slitertillegg) = 2291.67
 * │  └─ Mul
 * │  │  ├─ Mul
 * │  │  │  ├─ Grunnlag(fulltSlitertillegg) = 25000.0
 * │  │  │  └─ [1] trygdetidFaktor = 0.9
 * │  │  └─ [2] justeringsFaktor = 1.02
 * [1] trygdetidFaktor (2 forekomster)
 * [2] justeringsFaktor (1 forekomst)
 * ```
 */
class UttrykksTreePrinter {

    // Track Faktum instances we've seen for deduplication
    private val seenFaktum = mutableMapOf<Faktum<*>, Int>()
    private val faktumOccurrences = mutableMapOf<Int, Int>() // reference number -> count
    private var nextReferenceNumber = 1

    /**
     * Print the expression tree starting from the given Uttrykk.
     */
    fun print(uttrykk: Uttrykk<*>): String {
        val result = buildString {
            appendTree(uttrykk, "", true, isRoot = true)

            // Add deduplication summary at the end
            if (faktumOccurrences.isNotEmpty()) {
                append("\n")
                val sortedRefs = faktumOccurrences.entries.sortedBy { it.key }
                sortedRefs.forEach { (refNum, count) ->
                    val faktum = seenFaktum.entries.find { it.value == refNum }?.key
                    if (faktum != null && count > 1) {
                        val forekomster = if (count == 1) "forekomst" else "forekomster"
                        append("[$refNum] ${faktum.navn} ($count $forekomster)\n")
                    }
                }
            }
        }
        return result.trimEnd()
    }

    private fun StringBuilder.appendTree(
        uttrykk: Uttrykk<*>,
        prefix: String,
        isLast: Boolean,
        isRoot: Boolean = false
    ) {
        // Determine the connector for this node
        val connector = when {
            isRoot -> ""
            isLast -> "└─ "
            else -> "├─ "
        }

        // Check if this is a Faktum we've seen before
        if (uttrykk is Faktum<*>) {
            val existingRef = seenFaktum[uttrykk]
            if (existingRef != null) {
                // We've seen this Faktum before - show reference
                append("$prefix$connector[$existingRef] ${uttrykk.navn} = ${uttrykk.konkret()}\n")
                faktumOccurrences[existingRef] = (faktumOccurrences[existingRef] ?: 1) + 1
                return
            } else {
                // First time seeing this Faktum - assign reference number
                val refNum = nextReferenceNumber++
                seenFaktum[uttrykk] = refNum
                faktumOccurrences[refNum] = 1
            }
        }

        // Display the node
        val nodeLabel = when (uttrykk) {
            is Faktum<*> -> "${uttrykk.navn} = ${uttrykk.konkret()}"
            is MathOperation<*> -> uttrykk.operator.name
            is ComparisonOperation -> "${uttrykk.operator.name} = ${uttrykk.konkret()}"
            is ListOperation -> "${uttrykk.operator.name} = ${uttrykk.konkret()}"
            is Const<*> -> uttrykk.konkret()
            else -> uttrykk.notasjon()
        }

        append("$prefix$connector$nodeLabel\n")

        // Determine children for recursive traversal
        val children = when (uttrykk) {
            is MathOperation<*> -> listOf(uttrykk.venstre, uttrykk.høyre)
            is ComparisonOperation -> listOf(uttrykk.venstre, uttrykk.høyre)
            is ListOperation -> listOf(uttrykk.uttrykk, uttrykk.mengdeUttrykk)
            is Faktum<*> -> if (uttrykk.uttrykk !is Const<*>) listOf(uttrykk.uttrykk) else emptyList()
            else -> emptyList()
        }

        // Recursively print children
        val childPrefix = when {
            isRoot -> "│  "
            isLast -> "$prefix    "
            else -> "$prefix│   "
        }

        children.forEachIndexed { index, child ->
            val isLastChild = index == children.size - 1
            appendTree(child, childPrefix, isLastChild)
        }
    }
}

/**
 * Extension function for convenient tree printing.
 */
fun Uttrykk<*>.printTree(): String {
    return UttrykksTreePrinter().print(this)
}
