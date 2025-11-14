package no.nav.system.rule.dsl.inspections

import no.nav.system.rule.dsl.AbstractRuleComponent
import no.nav.system.rule.dsl.AbstractRuleflow
import no.nav.system.rule.dsl.Rule
import no.nav.system.rule.dsl.TrackablePredicate
import no.nav.system.rule.dsl.enums.RuleComponentType
import no.nav.system.rule.dsl.rettsregel.Const
import no.nav.system.rule.dsl.rettsregel.Uttrykk
import no.nav.system.rule.dsl.rettsregel.helper.svarord
import org.jetbrains.kotlin.builtins.StandardNames.FqNames.target

/**
 * Searches the ruleComponent tree for target [AbstractRuleComponent]s matching [target].
 * Shows the path from the receiver to where [target]s where found.
 *
 * @param qualifier A ruleComponent that resolves [qualifier] expression as true is eligible for search.
 * @param target A ruleComponent that matches [target] expression is added to [result]
 */
fun AbstractRuleComponent.traceType(targetType: RuleComponentType): String {
    return trace(target = { arc -> arc.type() == targetType })
}

/**
 * Builds a structured execution trace as a list of Uttrykk from root to target component.
 *
 * The list contains:
 * - Const<String> for component names (regelsett, regel)
 * - Uttrykk<Boolean> for predicates (extracted from TrackablePredicate)
 *
 * This structured format allows custom rendering and filtering.
 */
fun AbstractRuleComponent.hvorforAsUttrykk(
    target: AbstractRuleComponent
): List<Uttrykk<*>> {
    val rootTraceNode = TraceNode(parent = null, arc = this).apply {
        inspect(this, qualifier = { true }, target = { arc -> arc === target })
    }

    return buildHvorforUttrykk(rootTraceNode)
}

/**
 * Bygger uttrykkliste for hvorfor forklaring.
 * Kun noder som representerer en besluttninger (grener, regler og predikat) er med.
 */
private fun buildHvorforUttrykk(node: TraceNode): List<Uttrykk<*>> {
    val result = mutableListOf<Uttrykk<*>>()

    fun collect(currentNode: TraceNode) {
        if (!currentNode.partOfResult) return

        when (currentNode.arc.type()) {

            RuleComponentType.REGEL -> {
                result.add(Const("regel: ${currentNode.arc.fired().svarord()} ${currentNode.arc.name()}"))

                // Extract predicates from Rule's children
                currentNode.arc.children
                    .filterIsInstance<TrackablePredicate>()
                    .forEach { predicate ->
                        result.add(predicate.uttrykk)  // Already Uttrykk<Boolean>
                    }
            }

            RuleComponentType.GREN -> {
                (currentNode.arc as AbstractRuleflow.Decision.Branch).let { branch ->
                    result.add(branch.condition)
                }
            }

            else -> {
            }
        }

        // Recursively collect from children
        currentNode.children
            .filter { it.partOfResult }
            .forEach { collect(it) }
    }

    collect(node)
    return result
}

fun AbstractRuleComponent.hvorfor(
    qualifier: (AbstractRuleComponent) -> Boolean = { true },
    target: AbstractRuleComponent
): String {
    val rootTraceNode = TraceNode(parent = null, arc = this).apply {
        inspect(this, qualifier, target = { arc -> arc === target })
    }

    return rootTraceNode.renderUsing(hvorforForklaringRenderer())
}

fun AbstractRuleComponent.trace(
    qualifier: (AbstractRuleComponent) -> Boolean = { true },
    target: (AbstractRuleComponent) -> Boolean
): String {
    val rootTraceNode = TraceNode(parent = null, arc = this).apply {
        inspect(this, qualifier, target)
    }

    return rootTraceNode.toString().trim()
}

private fun inspect(
    parent: TraceNode,
    qualifier: (AbstractRuleComponent) -> Boolean,
    target: (AbstractRuleComponent) -> Boolean
) {
    parent.arc.children
        .filter(qualifier)
        .forEach { child ->
            // Detect cycles: check if child is already in ancestor chain
            var ancestor: TraceNode? = parent
            while (ancestor != null) {
                if (ancestor.arc === child) {
                    // Cycle detected, skip this child
                    return@forEach
                }
                ancestor = ancestor.parent
            }

            TraceNode(
                parent = parent,
                arc = child
            ).apply {
                parent.children.add(this)
                if (target.invoke(child)) {
                    this.verify()
                }
                inspect(this, qualifier, target)
            }
        }
}

class TraceNode(
    var parent: TraceNode? = null,
    val children: MutableList<TraceNode> = mutableListOf(),
    val arc: AbstractRuleComponent,
    var partOfResult: Boolean = false,
) {
    fun verify() {
        partOfResult = true
        parent?.verify()
    }

    override fun toString(): String = renderUsing(defaultTreeRenderer())

    fun renderUsing(renderer: (TraceNode, Int) -> String): String {
        return renderer.invoke(this, 0)
    }

}

private fun defaultTreeRenderer(): (TraceNode, Int) -> String {
    // Rekursiv funksjon for å bygge trestrukturen
    fun render(node: TraceNode, level: Int): String {
        return buildString {
            append(" ".repeat(level * 2)).append(node.arc.toString()).append("\n")
            node.children.filter { it.partOfResult }.forEach { child ->
                append(render(child, level + 1))  // Recursive call
            }
        }
    }
    return ::render
}

/**
 * Egen funksjon som omsetter TraceNodene i treet til tekst. Spesialbehandling for å gi egnet visning for Hvorfor-forklaringen.
 */
private fun hvorforForklaringRenderer(): (TraceNode, Int) -> String {
    // Rekursiv funksjon for å bygge trestrukturen
    fun render(node: TraceNode, level: Int): String {
        return buildString {
            when (node.arc.type()) {
                /**
                 * Egen rendering for REGEL slik at vi kan liste predikatene under regelen.
                 * (Trace i en regel går IKKE via predikatene)
                 */
                RuleComponentType.REGEL -> {
                    append(" ".repeat(level * 2)).append(node.arc.toString()).append("\n")
                    node.arc.children
                        .filterIsInstance<TrackablePredicate>()
                        .forEach { domenePredikat ->
                            append(" ".repeat((level + 1) * 2)).append(domenePredikat.forklar(level + 2)).append("\n")
                        }
                }

                /**
                 * Egen rendering for FORGRENING slik at vi kan liste ut valgt gren på samme linje som forgreningen.
                 */
                RuleComponentType.FORGRENING -> {
                    append(" ".repeat(level * 2)).append(node.arc.toString())

                    val gren = node.children.firstOrNull { it.partOfResult && it.arc.type() == RuleComponentType.GREN }

                    if (gren != null) {
                        append(" ").append(gren.arc.fired().svarord()).append("\n")
                        gren.children.filter { it.partOfResult }.forEach { child ->
                            append(render(child, level + 1))  // Recursive call
                        }
                    } else {
                        append("\n")
                        node.children.filter { it.partOfResult }.forEach { child ->
                            append(render(child, level + 1))  // Recursive call
                        }
                    }
                }

                /**
                 * Vanlig visning av andre noder.
                 */
                else -> {
                    append(" ".repeat(level * 2)).append(node.arc.toString()).append("\n")
                    node.children.filter { it.partOfResult }.forEach { child ->
                        append(render(child, level + 1))  // Recursive call
                    }
                }
            }
        }
    }
    return ::render
}

