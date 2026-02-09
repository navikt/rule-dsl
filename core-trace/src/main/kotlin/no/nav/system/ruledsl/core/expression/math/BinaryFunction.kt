package no.nav.system.ruledsl.core.expression.math

import no.nav.system.ruledsl.core.expression.Expression
import no.nav.system.ruledsl.core.expression.Faktum

/**
 * Binary funksjon for operasjoner på to elementer (min, max).
 */
data class BinaryFunction<T : Number>(
    val left: Expression<Number>,
    val right: Expression<Number>,
    val name: String,
    val evaluator: () -> T
) : Expression<T> {

    override val value: T by lazy { evaluator() }

    override fun notation(): String = "$name(${left.notation()}, ${right.notation()})"

    override fun concrete(): String = "$name(${left.concrete()}, ${right.concrete()})"

    override fun faktumSet(): Set<Faktum<*>> {
        val result = mutableSetOf<Faktum<*>>()
        if (left is Faktum<*>) result.add(left) else result.addAll(left.faktumSet())
        if (right is Faktum<*>) result.add(right) else result.addAll(right.faktumSet())
        return result
    }
}
