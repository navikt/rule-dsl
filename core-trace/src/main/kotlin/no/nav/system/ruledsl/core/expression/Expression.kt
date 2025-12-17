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
     * Hvilke navngitte faktum bidrar til dette uttrykket.
     */
    fun faktumSet(): Set<Faktum<*>>
}