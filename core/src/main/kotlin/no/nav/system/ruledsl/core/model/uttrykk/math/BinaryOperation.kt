package no.nav.system.ruledsl.core.model.uttrykk.math

import no.nav.system.ruledsl.core.model.Faktum
import no.nav.system.ruledsl.core.model.Uttrykk
import no.nav.system.ruledsl.core.operators.MathOperator

/**
 * Matematisk operasjon for beregning (pluss, minus, gange, dele).
 */
internal data class BinaryOperation<T : Number>(
    val venstre: Uttrykk<Number>,
    val høyre: Uttrykk<Number>,
    val operator: MathOperator,
    val evaluator: () -> T
) : Uttrykk<T> {
    override val verdi: T by lazy {
        // Prevent silent NaN/Infinity from division by zero
        if (operator == MathOperator.DIV && høyre.verdi.toDouble() == 0.0)
            throw ArithmeticException("Divisjon med null")

        evaluator()
    }

    override fun notasjon(): String {
        val (v, h) = medParenteserVedBehov(
            venstre.notasjon(), venstre,
            operator,
            høyre.notasjon(), høyre
        )
        return "$v${operator.text}$h"
    }

    override fun konkret(): String {
        val (v, h) = medParenteserVedBehov(
            venstre.konkret(), venstre,
            operator,
            høyre.konkret(), høyre
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
    private fun medParenteserVedBehov(
        venstre: String,
        venstreUttrykk: Uttrykk<*>,
        operator: MathOperator,
        høyre: String,
        høyreUttrykk: Uttrykk<*>
    ): Pair<String, String> {
        val venstreTrengerParentes = when (operator) {
            MathOperator.MUL, MathOperator.DIV ->
                venstreUttrykk is BinaryOperation<*> &&
                        (venstreUttrykk.operator == MathOperator.ADD || venstreUttrykk.operator == MathOperator.SUB)

            else -> false
        }

        val høyreTrengerParentes = when (operator) {
            MathOperator.MUL, MathOperator.DIV, MathOperator.SUB ->
                høyreUttrykk is BinaryOperation<*> &&
                        (høyreUttrykk.operator == MathOperator.ADD || høyreUttrykk.operator == MathOperator.SUB)

            else -> false
        }

        val v = if (venstreTrengerParentes) "($venstre)" else venstre
        val h = if (høyreTrengerParentes) "($høyre)" else høyre

        return v to h
    }

}
