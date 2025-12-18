package no.nav.system.ruledsl.core.expression.boolean

import no.nav.system.ruledsl.core.expression.Expression
import no.nav.system.ruledsl.core.expression.Faktum
import no.nav.system.ruledsl.core.helper.jaNei

// The expression that captures the comparison structure (for tracing)
class Comparison(
    val left: Expression<*>,
    val right: Expression<*>,
    val operator: PairOperator,
    private val evaluator: () -> Boolean
) : Expression<Boolean> {
    override val value: Boolean by lazy { evaluator() }
    override fun notation(): String = "${value.jaNei()} '${left.notation()}'${operatorText()}'${right.notation()}'"
    override fun concrete(): String = "${value.jaNei()} '${left.concrete()}'${operatorText()}'${right.concrete()}'"
    override fun faktumSet(): Set<Faktum<*>> = left.faktumSet() + right.faktumSet()
    override fun toString(): String = buildString {
        append(value.jaNei()).append(" ")
        if (left.notation() == left.concrete())
            append(left.notation())
        else
            append(left.notation()).append(" (").append(left.concrete()).append(")")
        append(operatorText())
        if (right.notation() == right.concrete())
            append(right.notation())
        else
            append(right.notation()).append(" (").append(right.concrete()).append(")")
    }

    private fun operatorText(): String = if (value) operator.text else operator.negated()
}
