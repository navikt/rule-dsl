package no.nav.system.ruledsl.core.expression.math

import no.nav.system.ruledsl.core.expression.Faktum
import no.nav.system.ruledsl.core.expression.Expression

/**
 * Matematisk operasjon for beregning (pluss, minus, gange, dele).
 */
internal data class BinaryOperation<T : Number>(
    val left: Expression<Number>,
    val right: Expression<Number>,
    val operator: MathOperator,
    val evaluator: () -> T
) : Expression<T> {
    override val value: T by lazy {
        // Prevent silent NaN/Infinity from division by zero
        if (operator == MathOperator.DIV && right.value.toDouble() == 0.0)
            throw ArithmeticException("Divisjon med null")

        evaluator()
    }

    override fun notation(): String {
        val (v, h) = checkAndApplyParenthesis(
            left.notation(), left,
            operator,
            right.notation(), right
        )
        return "$v${operator.text}$h"
    }

    override fun concrete(): String {
        val (v, h) = checkAndApplyParenthesis(
            left.concrete(), left,
            operator,
            right.concrete(), right
        )
        return "$v${operator.text}$h"
    }

    override fun faktumSet(): Set<Faktum<*>> =
        left.faktumSet() + right.faktumSet()

    /**
     * Legger til parenteser rundt uttrykk ved behov basert på operator precedence.
     *
     * Regler basert på standard aritmetisk presedens:
     * - Multiplikasjon og divisjon: venstre og høyre side får parenteser hvis de er addisjon/subtraksjon
     * - Subtraksjon: høyre side får parenteser hvis den er addisjon/subtraksjon (for å bevare korrekt evaluering)
     * - Addisjon: ingen automatiske parenteser (assosiativ og lav presedens)
     *
     * For unlocked Faktum: unwraps to the inner expression for precedence checks,
     * since unlocked Faktums are transparent and their notation expands inline.
     */
    private fun checkAndApplyParenthesis(
        left: String,
        leftExpression: Expression<*>,
        operator: MathOperator,
        right: String,
        rightExpression: Expression<*>
    ): Pair<String, String> {
        // Unwrap unlocked Faktum to get the effective expression for precedence checking
        val effectiveLeft = unwrapUnlockedFaktum(leftExpression)
        val effectiveRight = unwrapUnlockedFaktum(rightExpression)

        val leftParenthesisNeeded = when (operator) {
            MathOperator.MUL, MathOperator.DIV ->
                effectiveLeft is BinaryOperation<*> &&
                        (effectiveLeft.operator == MathOperator.ADD || effectiveLeft.operator == MathOperator.SUB)

            else -> false
        }

        val rightParenthesisNeeded = when (operator) {
            MathOperator.MUL, MathOperator.DIV, MathOperator.SUB ->
                effectiveRight is BinaryOperation<*> &&
                        (effectiveRight.operator == MathOperator.ADD || effectiveRight.operator == MathOperator.SUB)

            else -> false
        }

        val v = if (leftParenthesisNeeded) "($left)" else left
        val h = if (rightParenthesisNeeded) "($right)" else right

        return v to h
    }

    /**
     * Recursively unwraps unlocked Faktum to get the underlying expression.
     * Locked Faktum (and all other expression types) are returned as-is.
     */
    private fun unwrapUnlockedFaktum(expr: Expression<*>): Expression<*> {
        return when {
            expr is Faktum<*> && !expr.locked -> unwrapUnlockedFaktum(expr.expression)
            else -> expr
        }
    }

}
