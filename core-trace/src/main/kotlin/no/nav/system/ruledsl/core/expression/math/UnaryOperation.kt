package no.nav.system.ruledsl.core.expression.math

import no.nav.system.ruledsl.core.expression.Faktum
import no.nav.system.ruledsl.core.expression.Expression

/**
 * Unary funksjon for operasjoner på ett enkelt element (avrund, min, max, etc)
 */
data class UnaryOperation<T : Number>(
    val expression: Expression<Number>,
    val name: String,
    val evaluator: () -> T
) : Expression<T> {

    override val value: T by lazy { evaluator() }

    override fun notation(): String = "$name(${expression.notation()})"

    override fun concrete(): String = "$name(${expression.concrete()})"

    override fun faktumSet(): Set<Faktum<*>> = expression.faktumSet()
}
