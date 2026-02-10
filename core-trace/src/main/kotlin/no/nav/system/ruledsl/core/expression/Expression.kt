package no.nav.system.ruledsl.core.expression

/**
 * Base interface for all values in the rule DSL.
 *
 * Implemented by:
 * - [Verdi] — input values and constants, created freely
 * - [Faktum] — traced results, created via `faktum()` inside rules
 * - Internal types from operators (BinaryOperation, Comparison, etc.)
 */
interface Expression<out T : Any> {
    val value: T
    fun notation(): String
    fun concrete(): String

    /**
     * Returns the Faktum directly used in this expression.
     * Stops at Faktum boundaries - does not recurse into their expressions.
     */
    fun faktumSet(): Set<Faktum<*>>
}