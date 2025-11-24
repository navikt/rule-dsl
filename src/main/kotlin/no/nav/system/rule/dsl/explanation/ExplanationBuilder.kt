package no.nav.system.rule.dsl.explanation

import no.nav.system.rule.dsl.AbstractRuleComponent
import no.nav.system.rule.dsl.FaktumNode
import no.nav.system.rule.dsl.TrackablePredicate
import no.nav.system.rule.dsl.enums.RuleComponentType
import no.nav.system.rule.dsl.perspectives.Perspective
import no.nav.system.rule.dsl.rettsregel.Const
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
 *     .transform(::toIndentedText)
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
     * @param fn Function that converts List<Pair<AbstractRuleComponent, Int>> to desired type T
     *          Each pair contains (node, depth) where depth is the indentation level
     * @return Result of applying transformation function
     */
    fun <T> transform(fn: (List<Pair<AbstractRuleComponent, Int>>) -> T): T {
        val nodes = when (direction) {
            Direction.UP -> collectUp().mapIndexed { index, node -> node to index }
            Direction.DOWN -> collectDown()
        }
        return fn(nodes)
    }


    /**
     * Traverse UP from start node towards root, collecting filtered nodes.
     */
    private fun collectUp(): List<AbstractRuleComponent> {
        val result = mutableListOf<AbstractRuleComponent>()
        val visited = mutableSetOf<AbstractRuleComponent>()
        var current: AbstractRuleComponent? = startNode.parent

        while (current != null) {
            // Cycle detection: if we've seen this node before, we have a cycle
            if (visited.contains(current)) {
                throw IllegalStateException(
                    "Cycle detected in ARC tree at node: ${current.name()} (${current.type()}). " +
                            "Parent chain forms a loop. This indicates a bug in tree construction."
                )
            }
            visited.add(current)

            if (perspective.includes(current)) {
                // For rules in FUNCTIONAL perspective, also include predicates
                if (current.type() == RuleComponentType.REGEL && perspective == Perspective.FUNCTIONAL) {
                    // When prepending to result, add in reverse order to maintain natural order
                    val predicates = current.children.filterIsInstance<TrackablePredicate>()
                    result.add(0, current)  // Add rule first
                    // Add predicates in reverse (so they end up in correct order when prepended)
                    predicates.reversed().forEach { result.add(0, it) }
                } else {
                    result.add(0, current)
                }
            }
            current = current.parent
        }
        return result
    }

    /**
     * Traverse DOWN from start node through children, collecting filtered nodes with depth.
     */
    private fun collectDown(): List<Pair<AbstractRuleComponent, Int>> {
        val result = mutableListOf<Pair<AbstractRuleComponent, Int>>()
        val visited = mutableSetOf<AbstractRuleComponent>()
        collectDownRecursive(startNode, 0, result, visited)
        return result
    }

    private fun collectDownRecursive(
        node: AbstractRuleComponent,
        depth: Int,
        result: MutableList<Pair<AbstractRuleComponent, Int>>,
        visited: MutableSet<AbstractRuleComponent>
    ) {
        // Cycle detection: if we've seen this node before, we have a cycle
        if (visited.contains(node)) {
            throw IllegalStateException(
                "Cycle detected in ARC tree at node: ${node.name()} (${node.type()}). " +
                        "Child chain forms a loop. This indicates a bug in tree construction."
            )
        }
        visited.add(node)

        if (perspective.includes(node)) {
            result.add(node to depth)
        }

        // Calculate next depth: only increment if current node is included
        val nextDepth = if (perspective.includes(node)) depth + 1 else depth
        node.children.forEach { child ->
            collectDownRecursive(child, nextDepth, result, visited)
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
 * Formatter: Generate indented text output showing toString() for each node.
 *
 * Use with transform():
 * ```
 * val text = service.explain().transform(::toIndentedText)
 * ```
 *
 * @param nodes List of (node, depth) pairs where depth indicates indentation level
 * @return Formatted string with indented tree structure
 */
fun toIndentedText(nodes: List<Pair<AbstractRuleComponent, Int>>): String = buildString {
    nodes.forEach { (node, depth) ->
        val indent = "  ".repeat(depth)
        appendLine("$indent$node")
    }
}

/**
 * Extension function to start building an explanation from any ARC component.
 *
 * Example:
 * ```
 * val trace = service.explain().transform(::toIndentedText)
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
 * val explanation = faktum.explain().direction(Direction.UP).transform(::toIndentedText)
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
        .transform { nodes -> nodes.map { it.first }.filterIsInstance<FaktumNode<*>>() }
}

/**
 * Convenience: Traverses tree from root downward with given perspective.
 * Returns formatted text showing tree structure.
 *
 * This is syntactic sugar for: explain().perspective(p).direction(DOWN).transform(::toIndentedText)
 */
fun AbstractRuleComponent.traverseHva(perspective: Perspective = Perspective.FULL): String {
    // Custom formatter that preserves level numbering
    return explain()
        .perspective(perspective)
        .direction(Direction.DOWN)
        .transform { _ ->
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

        // Show formula if not constant
        if (uttrykk !is Const<*>) {
            appendLine()
            appendLine("HVORDAN (calculation):")
            appendLine("  Formula: ${uttrykk.notasjon()}")
            appendLine("  Result: ${uttrykk.konkret()}")
        }

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
                trace.forEach { (arc, _) ->
                    appendLine("  - $arc")
                }
            }
        }


    }
}
