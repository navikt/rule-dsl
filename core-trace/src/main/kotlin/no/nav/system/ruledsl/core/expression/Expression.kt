package no.nav.system.ruledsl.core.expression

import java.io.Serializable

/**
 * Numeric expression tree.
 *
 * IMPORTANT: This is now INTERNAL to Faktum<Number>.
 * Users do not work with Uttrykk directly - they use Faktum operators.
 *
 */
interface Expression<out T : Any> : Serializable {
    val value: T
    fun notation(): String
    fun concrete(): String

    /**
     * Returns the Faktum directly used in this expression.
     * Stops at Faktum boundaries - does not recurse into their expressions.
     */
    fun faktumSet(): Set<Faktum<*>>
}