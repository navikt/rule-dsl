package no.nav.system.ruledsl.core.expression.math

import no.nav.system.ruledsl.core.expression.Faktum
import no.nav.system.ruledsl.core.expression.Expression

/**
 * Matematisk operasjon for beregning (pluss, minus, gange, dele).
 */
internal data class BinaryOperation<T : Number>(
    val venstre: Expression<Number>,
    val høyre: Expression<Number>,
    val operator: MathOperator,
    val evaluator: () -> T
) : Expression<T> {
    override val value: T by lazy {
        // Prevent silent NaN/Infinity from division by zero
        if (operator == MathOperator.DIV && høyre.value.toDouble() == 0.0)
            throw ArithmeticException("Divisjon med null")

        evaluator()
    }

    override fun notation(): String {
        val (v, h) = checkAndApplyParenthesis(
            venstre.notation(), venstre,
            operator,
            høyre.notation(), høyre
        )
        return "$v${operator.text}$h"
    }

    override fun concrete(): String {
        val (v, h) = checkAndApplyParenthesis(
            venstre.concrete(), venstre,
            operator,
            høyre.concrete(), høyre
        )
        return "$v${operator.text}$h"
    }

    override fun faktumSet(): Set<Faktum<*>> =
        venstre.faktumSet() + høyre.faktumSet()

    /**
     * Legger til parenteser rundt uttrykk ved behov basert på operator precedence.
     *
     * Regler basert på standard aritmetisk presedens:
     * - Multiplikasjon og divisjon: venstre og høyre side får parenteser hvis de er addisjon/subtraksjon
     * - Subtraksjon: høyre side får parenteser hvis den er addisjon/subtraksjon (for å bevare korrekt evaluering)
     * - Addisjon: ingen automatiske parenteser (assosiativ og lav presedens)
     */
    private fun checkAndApplyParenthesis(
        left: String,
        leftExpression: Expression<*>,
        operator: MathOperator,
        right: String,
        rightExpression: Expression<*>
    ): Pair<String, String> {
        val leftParenthesisNeeded = when (operator) {
            MathOperator.MUL, MathOperator.DIV ->
                leftExpression is BinaryOperation<*> &&
                        (leftExpression.operator == MathOperator.ADD || leftExpression.operator == MathOperator.SUB)

            else -> false
        }

        val rightParenthesisNeeded = when (operator) {
            MathOperator.MUL, MathOperator.DIV, MathOperator.SUB ->
                rightExpression is BinaryOperation<*> &&
                        (rightExpression.operator == MathOperator.ADD || rightExpression.operator == MathOperator.SUB)

            else -> false
        }

        val v = if (leftParenthesisNeeded) "($left)" else left
        val h = if (rightParenthesisNeeded) "($right)" else right

        return v to h
    }

}
