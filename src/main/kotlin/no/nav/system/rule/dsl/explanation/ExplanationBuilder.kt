package no.nav.system.rule.dsl.explanation

import no.nav.system.rule.dsl.AbstractRuleComponent
import no.nav.system.rule.dsl.FaktumNode
import no.nav.system.rule.dsl.TrackablePredicate
import no.nav.system.rule.dsl.enums.RuleComponentType
import no.nav.system.rule.dsl.perspectives.Perspective
import no.nav.system.rule.dsl.rettsregel.Faktum
import no.nav.system.rule.dsl.rettsregel.Uttrykk

/**
 * Fluent builder for creating explanations from the ARC tree.
 *
 * Provides a composable API for:
 * - **Perspective**: Which nodes to include (FULL, FUNCTIONAL, custom)
 * - **Direction**: Traverse UP (faktum→root) or DOWN (root→leaves)
 * - **Transform**: Convert nodes to output format (String, JSON, HTML, custom)
 *
 * Example - Faktum-centric (bottom-up):
 * ```
 * val explanation = faktum.explain()
 *     .perspective(Perspective.FUNCTIONAL)
 *     .direction(Direction.UP)
 *     .toText()
 * ```
 *
 * Example - Service-centric (top-down):
 * ```
 * val trace = service.explain()
 *     .perspective(Perspective.FULL)
 *     .direction(Direction.DOWN)
 *     .transform { nodes -> toJSON(nodes) }
 * ```
 */
class ExplanationBuilder internal constructor(
    private val startNode: AbstractRuleComponent
) {
    private var perspective: Perspective = Perspective.FUNCTIONAL
    private var direction: Direction = Direction.DOWN

    /**
     * Set which nodes to include in the explanation.
     *
     * @param p Perspective filter (FULL, FUNCTIONAL, or custom)
     */
    fun perspective(p: Perspective) = apply { perspective = p }

    /**
     * Set traversal direction.
     *
     * @param d Direction.UP (faktum→root) or Direction.DOWN (root→leaves)
     */
    fun direction(d: Direction) = apply { direction = d }

    /**
     * Transform the filtered nodes to custom output format.
     *
     * @param fn Function that converts List<AbstractRuleComponent> to desired type T
     * @return Result of applying transformation function
     */
    fun <T> transform(fn: (List<AbstractRuleComponent>) -> T): T {
        val nodes = when (direction) {
            Direction.UP -> collectUp()
            Direction.DOWN -> collectDown()
        }
        return fn(nodes)
    }

    /**
     * Built-in transform: Generate indented text output showing toString() for each node.
     *
     * @return Formatted string with tree structure
     */
    fun toText(): String = transform { nodes ->
        buildString {
            nodes.forEachIndexed { index, node ->
                val indent = "  ".repeat(index)
                appendLine("$indent$node")
            }
        }
    }

    /**
     * Traverse UP from start node towards root, collecting filtered nodes.
     */
    private fun collectUp(): List<AbstractRuleComponent> {
        val result = mutableListOf<AbstractRuleComponent>()
        var current: AbstractRuleComponent? = startNode.parent

        while (current != null) {
            if (perspective.includes(current)) {
                // For rules in FUNCTIONAL perspective, also include predicates
                if (current.type() == RuleComponentType.REGEL && perspective == Perspective.FUNCTIONAL) {
                    result.add(0, current)  // Add rule first
                    // Then add predicates
                    current.children
                        .filterIsInstance<TrackablePredicate>()
                        .forEach { result.add(0, it) }
                } else {
                    result.add(0, current)
                }
            }
            current = current.parent
        }
        return result
    }

    /**
     * Traverse DOWN from start node through children, collecting filtered nodes.
     */
    private fun collectDown(): List<AbstractRuleComponent> {
        val result = mutableListOf<AbstractRuleComponent>()
        collectDownRecursive(startNode, result)
        return result
    }

    private fun collectDownRecursive(node: AbstractRuleComponent, result: MutableList<AbstractRuleComponent>) {
        if (perspective.includes(node)) {
            result.add(node)
        }
        node.children.forEach { child ->
            collectDownRecursive(child, result)
        }
    }
}

/**
 * Traversal direction for explanation building.
 */
enum class Direction {
    /**
     * UP: Traverse from node towards root (faktum-centric, "why was this created?")
     */
    UP,

    /**
     * DOWN: Traverse from root towards leaves (service-centric, "what happened during execution?")
     */
    DOWN
}

/**
 * Extension function to start building an explanation from any ARC component.
 *
 * Example:
 * ```
 * val trace = service.explain().perspective(Perspective.FULL).toText()
 * ```
 */
fun AbstractRuleComponent.explain() = ExplanationBuilder(this)

/**
 * Extension function to start building an explanation from a Faktum.
 *
 * Throws IllegalStateException if Faktum is not in the ARC tree (not created via sporing()).
 *
 * Example:
 * ```
 * val explanation = faktum.explain().direction(Direction.UP).toText()
 * ```
 */
fun <T : Any> Faktum<T>.explain(): ExplanationBuilder {
    val node = wrapperNode ?: throw IllegalStateException(
        "Faktum '$navn' is not in the ARC tree. Only Faktum created via sporing() can be explained."
    )
    return ExplanationBuilder(node).direction(Direction.UP)
}

/**
 * Collects all Faktum nodes from the tree.
 * Useful for finding all data produced during execution.
 *
 * Example:
 * ```
 * val allFaktum = service.collectFaktum()
 * allFaktum.forEach { node -> println("${node.faktum.navn} = ${node.faktum.verdi}") }
 * ```
 */
fun AbstractRuleComponent.collectFaktum(): List<FaktumNode<*>> {
    return explain()
        .perspective(Perspective { it is FaktumNode<*> })
        .direction(Direction.DOWN)
        .transform { nodes -> nodes.filterIsInstance<FaktumNode<*>>() }
}

/**
 * Convenience: Traverses tree from root downward with given perspective.
 * Returns formatted text showing tree structure.
 *
 * This is syntactic sugar for: explain().perspective(p).direction(DOWN).toText()
 */
fun AbstractRuleComponent.traverseHva(perspective: Perspective = Perspective.FULL): String {
    // Custom formatter that preserves level numbering
    return explain()
        .perspective(perspective)
        .direction(Direction.DOWN)
        .transform { nodes ->
            buildString {
                traverseWithLevel(this@traverseHva, 0, this, perspective)
            }
        }
}

private fun traverseWithLevel(
    arc: AbstractRuleComponent,
    level: Int,
    builder: StringBuilder,
    perspective: Perspective
) {
    var nextLevel = level
    if (perspective.includes(arc)) {
        val indent = "  ".repeat(level)
        builder.appendLine("$indent$level: $arc")
        nextLevel++
    }
    arc.children.forEach { child ->
        traverseWithLevel(child, nextLevel, builder, perspective)
    }
}

/**
 * Convenience: Complete explanation of a Faktum showing HVA/HVORFOR/HVORDAN.
 *
 * Example:
 * ```
 * val explanation = faktum.forklar()
 * ```
 */
fun <T : Any> Faktum<T>.forklar(perspective: Perspective = Perspective.FUNCTIONAL): String {
    return buildString {
        appendLine("=== Forklaring for '$navn' ===")
        appendLine()
        appendLine("HVA:")
        appendLine("  $navn = $verdi")

        // Get decision path using ExplanationBuilder
        val node = wrapperNode
        if (node != null) {
            val trace = node.explain()
                .perspective(perspective)
                .direction(Direction.UP)
                .transform { it }

            if (trace.isNotEmpty()) {
                appendLine()
                appendLine("HVORFOR (decision path):")
                trace.forEach { arc ->
                    appendLine("  - $arc")

                    // If it's also an Uttrykk, show details
                    if (arc is Uttrykk<*>) {
                        val details = arc.forklar(1)
                        if (details.isNotBlank()) {
                            append(details)
                        }
                    }
                }
            }
        }

        // Show formula if not constant
        if (uttrykk !is no.nav.system.rule.dsl.rettsregel.Const<*>) {
            appendLine()
            appendLine("HVORDAN (calculation):")
            append(uttrykk.forklar(0))
        }
    }
}
