package no.nav.system.ruledsl.core.expression

import no.nav.system.ruledsl.core.reference.Reference
import no.nav.system.ruledsl.core.trace.RuleTrace

/**
 * Traced result - always recorded to RuleTrace.
 *
 * Faktum can ONLY be created via the `faktum()` function in RuleContext.
 * This ensures every Faktum is automatically recorded to the trace.
 *
 * Use [Verdi] for input values and constants (not traced).
 * Use `faktum()` for computed results that need tracing.
 *
 * Faktum wraps any [Expression] and gives it a name, making it user-visible and trackable.
 * It acts as the boundary between anonymous calculations and named business results
 * that appear in rule explanations.
 *
 * Example:
 * ```
 * // Input values - use Verdi (not traced)
 * val G = Verdi("G", 118620)
 * val sats = Verdi("sats", 0.66)
 *
 * // In rule context - traced results via faktum()
 * regel("beregn") {
 *     SÅ {
 *         val grunnpensjon = faktum("grunnpensjon", G * sats)
 *         // Automatically recorded to trace
 *         // notation: "G * sats"
 *         // concrete: "118620 * 0.66"
 *
 *         domain.pensjon = grunnpensjon.value
 *     }
 * }
 * ```
 */
class Faktum<T : Any> private constructor(
    val name: String,
    val expression: Expression<T>,
    val references: List<Reference> = emptyList()
) : Expression<T> {

    /**
     * Reference to the RuleTrace that produced this Faktum.
     * Set internally by the Tracer when recorded.
     * Used for explanation traversal (walking back from result to rules).
     */
    internal var sourceNode: RuleTrace? = null

    /**
     * True if this Faktum holds a simple value (not a calculation).
     * Simple values show just the name; calculations can show formula details.
     */
    val isConstant: Boolean = expression is Verdi<*>

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
     * Returns the Faktum directly used in the underlying expression.
     * Used by the tracer to discover sub-formulas for hierarchical recording.
     */
    override fun faktumSet(): Set<Faktum<*>> = expression.faktumSet()

    override fun toString(): String = "'$name' ($value)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Faktum<*>) return false
        return name == other.name && expression == other.expression
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + expression.hashCode()
        return result
    }

    companion object {
        /**
         * Internal factory for RuleContext.faktum() to use.
         * This is the ONLY way to create a Faktum.
         */
        internal fun <T : Any> create(
            name: String,
            expression: Expression<T>,
            references: List<Reference> = emptyList()
        ): Faktum<T> = Faktum(name, expression, references)
    }
}

/**
 * Generates a debug tree showing the formula hierarchy.
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

        // Subformulas are direct non-constant Faktum used in this expression
        val subformulas = expression.faktumSet().filter { !it.isConstant }
        if (subformulas.isNotEmpty()) {
            sb.appendLine("$indent  subformulas:")
            subformulas.forEach { sub ->
                sub.debugTreeInternal(sb, "$indent    ", visited)
            }
        }
    }
}
