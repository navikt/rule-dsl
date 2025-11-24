package no.nav.system.rule.dsl.explanation

import no.nav.system.rule.dsl.AbstractRuleComponent
import no.nav.system.rule.dsl.FaktumNode
import no.nav.system.rule.dsl.perspectives.Perspective

/**
 * Traverses and prints the execution tree using Hva/Hvorfor/Hvordan interfaces.
 *
 * This demonstrates how the unified tree structure (ARCs + FaktumNode) can be
 * traversed polymorphically to generate different types of explanations.
 *
 * Supports two perspectives:
 * - FULL: Complete audit trail (all nodes)
 * - FUNCTIONAL: Only decision nodes (rules, branches, predicates, faktum)
 */

/**
 * Traverses the tree starting from root and prints HVA for each node.
 * Returns formatted string with indentation showing tree structure.
 *
 * @param perspective Controls which nodes are included (default: FULL for complete audit trail)
 *
 * Example output (FULL perspective):
 * ```
 * 0: Ruleservice: BeregnSlitertilleggService
 * 1:   Ruleflow: BehandleSliterordningFlyt
 * 2:     Ruleset: VilkårsprøvSlitertilleggRS
 * 3:       Rule: ALLTID-INNVILGET
 * 4:         Faktum: Vilkår Slitertillegg = true
 * ```
 *
 * Example output (FUNCTIONAL perspective):
 * ```
 * 0: Rule: ALLTID-INNVILGET
 * 1:   Faktum: Vilkår Slitertillegg = true
 * ```
 */
fun AbstractRuleComponent.traverseHva(perspective: Perspective = Perspective.FULL): String {
    return buildString {
        traverseHvaRecursive(this@traverseHva, 0, this, perspective)
    }
}

private fun traverseHvaRecursive(
    arc: AbstractRuleComponent,
    level: Int,
    builder: StringBuilder,
    perspective: Perspective
) {
    // Check if this node should be included in the current perspective
    if (perspective.includes(arc)) {
        val indent = "  ".repeat(level)
        builder.appendLine("$indent$level: ${arc.hva()}")
    }

    // Traverse all children (including FaktumNode)
    // Note: We always traverse children, but only print if perspective.includes() returns true
    arc.children.forEach { child ->
        traverseHvaRecursive(child, level + 1, builder, perspective)
    }
}

/**
 * Traverses the tree and prints full explanation (HVA + HVORFOR + HVORDAN) for nodes that support it.
 *
 * @param perspective Controls which nodes are included (default: FULL for complete audit trail)
 */
fun AbstractRuleComponent.traverseFull(perspective: Perspective = Perspective.FULL): String {
    return buildString {
        traverseFullRecursive(this@traverseFull, 0, this, perspective)
    }
}

private fun traverseFullRecursive(
    arc: AbstractRuleComponent,
    level: Int,
    builder: StringBuilder,
    perspective: Perspective
) {
    // Check if this node should be included in the current perspective
    if (perspective.includes(arc)) {
        val indent = "  ".repeat(level)

        // Always show HVA (all ARCs have it)
        builder.appendLine("$indent${arc.hva()}")

        // Show HVORFOR if available (e.g., FaktumNode)
        if (arc is Hvorfor) {
            val hvorforList = arc.hvorfor()
            if (hvorforList.isNotEmpty()) {
                builder.appendLine("${indent}  HVORFOR:")
                hvorforList.forEach {
                    builder.appendLine("${indent}    - ${it.hva()}")
                }
            }
        }

        // Show HVORDAN if available (e.g., FaktumNode)
        if (arc is Hvordan) {
            val hvordan = arc.hvordan()
            if (hvordan.isNotEmpty()) {
                builder.appendLine("${indent}  HVORDAN:")
                hvordan.lines().forEach { line ->
                    if (line.isNotBlank()) {
                        builder.appendLine("${indent}    $line")
                    }
                }
            }
        }
    }

    // Traverse all children (including FaktumNode)
    // Note: We always traverse children, but only print if perspective.includes() returns true
    arc.children.forEach { child ->
        traverseFullRecursive(child, level + 1, builder, perspective)
    }
}

/**
 * Collects all Faktum nodes from the tree.
 * Useful for finding all data produced during execution.
 */
fun AbstractRuleComponent.collectFaktum(): List<FaktumNode<*>> {
    val result = mutableListOf<FaktumNode<*>>()
    collectFaktumRecursive(this, result)
    return result
}

private fun collectFaktumRecursive(arc: AbstractRuleComponent, result: MutableList<FaktumNode<*>>) {
    arc.children.forEach { child ->
        if (child is FaktumNode<*>) {
            result.add(child)
        }
        // Recurse for all children (including FaktumNode which is also an ARC)
        collectFaktumRecursive(child, result)
    }
}
