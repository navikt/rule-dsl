package no.nav.system.ruledsl.core.expression

import no.nav.system.ruledsl.core.reference.Reference

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
     * Useful for transformers to determine if HVORDAN section should be shown.
     */
    val isConstant: Boolean = expression is Const<*>

    override val value: T by lazy { expression.value }

    override fun notation(): String = name

    override fun concrete(): String = value.toString()

    /**
     * Returns this Faktum as the atomic unit.
     *
     * IMPORTANT: Returns `setOf(this)` rather than `uttrykk.faktumSet()` because
     * Faktum defines the boundary for named facts. This stops recursion at the name,
     * enabling layered explanations (result facts vs. input facts).
     *
     * To access contributing facts inside this Faktum, use `uttrykk.faktumSet()`.
     */
    override fun faktumSet(): Set<Faktum<*>> = setOf(this)

    override fun toString(): String = "'$name' ($value)"
}
