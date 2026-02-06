package no.nav.system.ruledsl.core.expression

/**
 * Named or unnamed value - building block for expressions.
 *
 * Verdi can be created anywhere and used in formulas. It is NOT traced independently.
 * It becomes part of the trace only when used in a Faktum expression.
 *
 * Two construction modes:
 * - Named: `Verdi("G", 118620)` → notation: "G"
 * - Unnamed: `Verdi(118620)` → notation: "118620" (value as name)
 *
 * Unnamed Verdi replaces the old Const class and is created by math operators
 * when literals are used in expressions.
 *
 * Example:
 * ```
 * val G = Verdi("G", 118620)           // Named building block
 * val sats = Verdi("sats", 0.66)       // Named building block
 *
 * // In rule context:
 * val grunnpensjon = faktum("grunnpensjon", G * sats)
 * // notation: "G * sats"
 * // concrete: "118620 * 0.66"
 *
 * // Literals become unnamed Verdi via operators:
 * val justert = faktum("justert", grunnpensjon * 0.9)
 * // notation: "grunnpensjon * 0.9"
 * ```
 */
data class Verdi<T : Any>(
    val name: String,
    override val value: T
) : Expression<T> {

    /**
     * Unnamed constructor: name defaults to value.toString().
     * Used by operators when literals appear in expressions.
     */
    constructor(value: T) : this(value.toString(), value)

    override fun notation(): String = name

    override fun concrete(): String = value.toString()

    override fun faktumSet(): Set<Faktum<*>> = emptySet()

    override fun toString(): String = "'$name' ($value)"
}
