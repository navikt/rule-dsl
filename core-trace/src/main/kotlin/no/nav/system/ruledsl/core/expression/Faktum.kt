package no.nav.system.ruledsl.core.expression

import no.nav.system.ruledsl.core.reference.Reference
import no.nav.system.ruledsl.core.trace.RuleTrace

/**
 * Named expression - treated as atomic unit for fact tracking.
 *
 * Faktum wraps any [Expression] and gives it a name, making it user-visible and trackable.
 * It acts as the boundary between anonymous calculations (Const, BinaryOperation) and
 * named business facts that appear in rule explanations.
 *
 * Pure data class - presentation is handled separately via extension functions.
 * This separation allows users to write custom explanation/presentation logic.
 *
 * Example:
 * ```
 * val alder = Faktum("Alder", 67)              // Named fact
 * val sats = Faktum("Sats", 1000)              // Named fact
 * val produkt = alder * sats                   // Anonymous BinaryOperation
 * val tillegg = Faktum("Tillegg", produkt)     // Wrap as named result
 *
 * produkt.faktumSet()  // → { alder, sats }    Input facts
 * tillegg.faktumSet()  // → { tillegg }         Result fact (stops at boundary)
 * tillegg.uttrykk.faktumSet()  // → { alder, sats }  Contributing facts inside
 * ```
 *
 * This is can be created by users.
 */
data class Faktum<T : Any>(
    val name: String,
    val expression: Expression<T>,
    val references: List<Reference> = emptyList()
) : Expression<T> {

    /**
     * Reference to the RuleTrace that produced this Faktum.
     * Set by Tracer.recordExpression() when SPOR or RETURNER is called.
     * Used for inverse explanation traversal (starting from result, walking back).
     */
    internal var sourceNode: RuleTrace? = null

    constructor(
        name: String,
        value: T,
        references: List<Reference> = emptyList()
    ) : this(
        name = name,
        expression = Const(value),
        references = references
    )

    /**
     * True if this Faktum holds a constant value (not a calculated expression).
     * Constants act as atomic units (show name), calculations expand.
     */
    val isConstant: Boolean = expression is Const<*>

    override val value: T by lazy { expression.value }

    /**
     * Always returns name - Faktum acts as a boundary in expressions.
     * To see the formula, access expression.notation() directly.
     */
    override fun notation(): String = name

    /**
     * Always returns value - Faktum acts as a boundary in expressions.
     * To see the expanded calculation, access expression.concrete() directly.
     */
    override fun concrete(): String = value.toString()

    /**
     * Constants return self as the atomic unit (boundary for fact tracking).
     * Calculations delegate to expression (transparent, dependencies flow through).
     */
    override fun faktumSet(): Set<Faktum<*>> =
        if (isConstant) setOf(this) else expression.faktumSet()

    override fun toString(): String = "'$name' ($value)"
}


/**
 * Generates a debug tree showing the formula hierarchy.
 *
 * For locked Faktums, shows the formula notation/concrete and recurses into subformulas.
 * For unlocked Faktums, shows as transparent (included in parent's notation).
 *
 * Example output:
 * ```
 * sum = 1000.0
 *   notation: fortyTwo + fortyFive
 *   concrete: 400.0 + 600.0
 *   subformulas:
 *     fortyTwo = 400.0
 *       notation: 0.42 * G * OPT * PP / 40
 *       concrete: 0.42 * 56123 * 3.1 * 0.5 / 40
 * ```
 */
fun Faktum<*>.debugTree(): String = buildString {
    debugTreeInternal(this, "", mutableSetOf())
}

internal fun Faktum<*>.debugTreeInternal(
    sb: StringBuilder,
    indent: String,
    visited: MutableSet<Faktum<*>>
) {
    // Prevent infinite recursion on circular references
    if (this in visited) {
        sb.appendLine("$indent$name = $value (see above)")
        return
    }
    visited.add(this)

    sb.appendLine("$indent$name = $value")

    if (!isConstant) {
        sb.appendLine("$indent  notation: ${expression.notation()}")
        sb.appendLine("$indent  concrete: ${expression.concrete()}")

        // Get subformulas (direct locked dependencies)
        val subformulas = expression.faktumSet()
        if (subformulas.isNotEmpty()) {
            sb.appendLine("$indent  subformulas:")
            subformulas.forEach { sub ->
                sub.debugTreeInternal(sb, "$indent    ", visited)
            }
        }
    }
}
