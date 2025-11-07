package no.nav.system.rule.dsl.rettsregel

import no.nav.system.rule.dsl.Predicate
import no.nav.system.rule.dsl.enums.Operator
import no.nav.system.rule.dsl.enums.ListOperator
import no.nav.system.rule.dsl.enums.MathOperator
import no.nav.system.rule.dsl.enums.NegatableOperator
import no.nav.system.rule.dsl.enums.PairOperator
import no.nav.system.rule.dsl.enums.RuleComponentType
import no.nav.system.rule.dsl.enums.RuleComponentType.DOMENE_PREDIKAT_LISTE
import no.nav.system.rule.dsl.enums.RuleComponentType.DOMENE_PREDIKAT_PAR
import no.nav.system.rule.dsl.rettsregel.helper.svarord
import java.io.Serializable

/**
 * Numeric expression tree.
 *
 * IMPORTANT: This is now INTERNAL to Faktum<Number>.
 * Users do not work with Uttrykk directly - they use Faktum operators.
 *
 * Supported operations:
 * - Add, Sub, Mul, Div: arithmetic operations
 * - Min: minimum function
 */
interface Uttrykk<out T : Any> : Serializable {
    fun evaluer(): T
    fun notasjon(): String
    fun konkret(): String

    /**
     * Hvilke navngitte faktum bidrar til dette uttrykket.
     */
    fun faktumSet(): Set<Faktum<*>>
    fun forklar(level: Int = 0): String
}

internal data class Add<T : Number>(
    val venstre: Uttrykk<Number>,
    val høyre: Uttrykk<Number>
) : Uttrykk<T> {
    @Suppress("UNCHECKED_CAST")
    override fun evaluer(): T {
        // Cache evalueringene for å unngå dobbel-evaluering
        val vVerdi = venstre.evaluer()
        val hVerdi = høyre.evaluer()
        val v = vVerdi.toDouble()
        val h = hVerdi.toDouble()
        val resultat = v + h

        // Returner riktig type basert på input
        return if (vVerdi is Int && hVerdi is Int) {
            resultat.toInt() as T
        } else {
            resultat as T
        }
    }

    override fun notasjon(): String {
        val (v, h) = medParenteserVedBehov(
            venstre.notasjon(), venstre,
            MathOperator.ADD,
            høyre.notasjon(), høyre
        )
        return "$v + $h"
    }

    override fun konkret(): String {
        val (v, h) = medParenteserVedBehov(
            venstre.konkret(), venstre,
            MathOperator.ADD,
            høyre.konkret(), høyre
        )
        return "$v + $h"
    }

    override fun faktumSet(): Set<Faktum<*>> =
        venstre.faktumSet() + høyre.faktumSet()

    override fun forklar(level: Int): String = buildString {
        indent(level).append("HVORDAN\n")
        indent(level + 1).append("${notasjon()}\n")
        indent(level + 1).append("${konkret()}\n")

        faktumSet().forEach { faktum ->
            // Show HVORDAN for each Faktum's expression (skip HVA/HVORFOR)
            indent(level + 1).append(faktum.uttrykk.forklar(level + 2))
        }
    }

}

/**
 * Subtraksjon.
 */
internal data class Sub<T : Number>(
    val venstre: Uttrykk<Number>,
    val høyre: Uttrykk<Number>
) : Uttrykk<T> {
    @Suppress("UNCHECKED_CAST")
    override fun evaluer(): T {
        // Cache evalueringene for å unngå dobbel-evaluering
        val vVerdi = venstre.evaluer()
        val hVerdi = høyre.evaluer()
        val v = vVerdi.toDouble()
        val h = hVerdi.toDouble()
        val resultat = v - h

        return if (vVerdi is Int && hVerdi is Int) {
            resultat.toInt() as T
        } else {
            resultat as T
        }
    }

    override fun notasjon(): String {
        val (v, h) = medParenteserVedBehov(
            venstre.notasjon(), venstre,
            MathOperator.SUB,
            høyre.notasjon(), høyre
        )
        return "$v - $h"
    }

    override fun konkret(): String {
        val (v, h) = medParenteserVedBehov(
            venstre.konkret(), venstre,
            MathOperator.SUB,
            høyre.konkret(), høyre
        )
        return "$v - $h"
    }

    override fun faktumSet(): Set<Faktum<*>> =
        venstre.faktumSet() + høyre.faktumSet()

    override fun forklar(level: Int): String = buildString {
        indent(level).append("HVORDAN\n")
        indent(level + 1).append("${notasjon()}\n")
        indent(level + 1).append("${konkret()}\n")

        faktumSet().forEach { faktum ->
            // Show HVORDAN for each Faktum's expression (skip HVA/HVORFOR)
            indent(level + 1).append(faktum.uttrykk.forklar(level + 2))
        }
    }
}

/**
 * Multiplikasjon.
 */
internal data class Mul<T : Number>(
    val venstre: Uttrykk<Number>,
    val høyre: Uttrykk<Number>
) : Uttrykk<T> {
    @Suppress("UNCHECKED_CAST")
    override fun evaluer(): T {
        // Cache evalueringene for å unngå dobbel-evaluering
        val vVerdi = venstre.evaluer()
        val hVerdi = høyre.evaluer()
        val v = vVerdi.toDouble()
        val h = hVerdi.toDouble()
        val resultat = v * h

        return if (vVerdi is Int && hVerdi is Int) {
            resultat.toInt() as T
        } else {
            resultat as T
        }
    }

    override fun notasjon(): String {
        val (v, h) = medParenteserVedBehov(
            venstre.notasjon(), venstre,
            MathOperator.MUL,
            høyre.notasjon(), høyre
        )
        return "$v * $h"
    }

    override fun konkret(): String {
        val (v, h) = medParenteserVedBehov(
            venstre.konkret(), venstre,
            MathOperator.MUL,
            høyre.konkret(), høyre
        )
        return "$v * $h"
    }

    override fun faktumSet(): Set<Faktum<*>> =
        venstre.faktumSet() + høyre.faktumSet()

    override fun forklar(level: Int): String = buildString {
        indent(level).append("HVORDAN\n")
        indent(level + 1).append("${notasjon()}\n")
        indent(level + 1).append("${konkret()}\n")

        faktumSet().forEach { faktum ->
            // Show HVORDAN for each Faktum's expression (skip HVA/HVORFOR)
            indent(level + 1).append(faktum.uttrykk.forklar(level + 2))
        }
    }
}

/**
 * Divisjon (gir alltid Double).
 */
internal data class Div(
    val venstre: Uttrykk<Number>,
    val høyre: Uttrykk<Number>
) : Uttrykk<Double> {
    override fun evaluer(): Double {
        val v = venstre.evaluer().toDouble()
        val h = høyre.evaluer().toDouble()

        if (h == 0.0) {
            throw ArithmeticException("Divisjon med null: $v / $h")
        }

        return v / h
    }

    override fun notasjon(): String {
        val (v, h) = medParenteserVedBehov(
            venstre.notasjon(), venstre,
            MathOperator.DIV,
            høyre.notasjon(), høyre
        )
        return "$v / $h"
    }

    override fun konkret(): String {
        val (v, h) = medParenteserVedBehov(
            venstre.konkret(), venstre,
            MathOperator.DIV,
            høyre.konkret(), høyre
        )
        return "$v / $h"
    }

    override fun faktumSet(): Set<Faktum<*>> =
        venstre.faktumSet() + høyre.faktumSet()

    override fun forklar(level: Int): String = buildString {
        indent(level).append("HVORDAN\n")
        indent(level + 1).append("${notasjon()}\n")
        indent(level + 1).append("${konkret()}\n")

        faktumSet().forEach { faktum ->
            // Show HVORDAN for each Faktum's expression (skip HVA/HVORFOR)
            indent(level + 1).append(faktum.uttrykk.forklar(level + 2))
        }
    }
}

private fun StringBuilder.indent(level: Int): StringBuilder = append(" ".repeat(level * 2))

private enum class Operator {
    ADD, SUB, MUL, DIV
}

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
    operator: Operator,
    høyre: String,
    høyreUttrykk: Uttrykk<*>
): Pair<String, String> {
    val venstreTrengerParentes = when (operator) {
        MathOperator.MUL, MathOperator.DIV -> venstreUttrykk is Add || venstreUttrykk is Sub
        else -> false
    }

    val høyreTrengerParentes = when (operator) {
        MathOperator.MUL, MathOperator.DIV, MathOperator.SUB -> høyreUttrykk is Add || høyreUttrykk is Sub
        else -> false
    }

    val v = if (venstreTrengerParentes) "($venstre)" else venstre
    val h = if (høyreTrengerParentes) "($høyre)" else høyre

    return v to h
}


/**
 * Named expression - treated as atomic unit.
 * This is can be created by users.
 */
data class Faktum<T : Any>(
    val navn: String,
    val uttrykk: Uttrykk<T>,
    val rvsId: String? = null,
    private val hvorfor: String? = null
) : Uttrykk<T> {

    constructor(
        navn: String,
        verdi: T,
        rvsId: String? = null
    ) : this(
        navn = navn,
        uttrykk = Const(verdi),
        rvsId = rvsId
    )

    override fun evaluer(): T = uttrykk.evaluer()

    override fun notasjon(): String = navn

    override fun konkret(): String = evaluer().toString()

    override fun faktumSet(): Set<Faktum<*>> = setOf(this)

    override fun toString(): String = "'$navn' (${evaluer()})"

    override fun forklar(level: Int): String = buildString {
        indent(level).append("HVA\n")
        indent(level + 1).append("$navn = ${evaluer()}\n")
        append("\n")
        indent(level).append("HVORFOR\n")
        indent(level + 1).append("$hvorfor\n")
        append("\n")
        indent(level + 1).append("${uttrykk.forklar(level + 2)}\n")
    }
}

/**
 * Unnamed constant - treated as atomic unit.
 * This is can NOT be created by users.
 */
internal data class Const<T : Any>(
    val verdi: T
) : Uttrykk<T> {

    override fun evaluer(): T = verdi

    override fun notasjon(): String = verdi.toString()

    override fun konkret(): String = verdi.toString()

    override fun faktumSet(): Set<Faktum<out Any>> = emptySet()

    override fun toString(): String = "'$verdi'"
    override fun forklar(level: Int): String = ""
}

/**
 * The application of a [function] that returns the boolean.
 */
abstract class DomainPredicate(
    open val operator: NegatableOperator,
    override val function: () -> Boolean,
) : Predicate(function = function), Uttrykk<Boolean> {

    /**
     * Evaluates the predicate function.
     * DomainPredicate never terminates callers evaluation chain ([terminateEvaluation] )
     *
     * @return boolean result of function.
     */
    override val fired: Boolean by lazy {
        function.invoke().also { terminateEvaluation = false }
    }

    fun komparatorText(): String = if (fired) operator.text else operator.negated()

}

/**
 * Compares [venstre] with [høyre]
 */
class PairDomainPredicate(
    override val operator: PairOperator,
    private val venstre: Uttrykk<*>,
    private val høyre: Uttrykk<*>,
    override val function: () -> Boolean,
) : DomainPredicate(operator = operator, function = function) {

    override fun type(): RuleComponentType = DOMENE_PREDIKAT_PAR
    override fun evaluer(): Boolean = fired
    override fun toString(): String = "${fired.svarord()} ${venstre}${komparatorText()}${høyre}"

    /**
     * someStartDate erFør someEndDate
     * 2022-01-01 erFør 2023-01-01
     *
     * someStartDate (2022-01-01) erFør someEndDate (2023-01-01)
     *
     * someStartDate erFør someEndDate -> 2022-01-01 erFør 2023-01-01
     */
    override fun forklar(level: Int): String = buildString {
        indent(level).append("${notasjon()}\n")
        indent(level).append("${konkret()}\n")
    }

    override fun notasjon(): String = "${fired.svarord()} '${venstre.notasjon()}'${komparatorText()}'${høyre.notasjon()}'"

    override fun konkret(): String = "${fired.svarord()} '${venstre.konkret()}'${komparatorText()}'${høyre.konkret()}'"

    override fun faktumSet(): Set<Faktum<*>> = venstre.faktumSet() + høyre.faktumSet()
}

/**
 * Compares [uttrykk] relationship with items [uttrykkList]
 */
class ListDomainPredicate(
    override val operator: ListOperator,
    private val uttrykk: Uttrykk<*>,
    val mengdeUttrykk: Uttrykk<List<*>>,
    override val function: () -> Boolean
) : DomainPredicate(operator = operator, function = function) {

    override fun type(): RuleComponentType = DOMENE_PREDIKAT_LISTE

    override fun toString(): String = konkret()

    override fun evaluer(): Boolean = fired

    override fun notasjon(): String = "${fired.svarord()} '${uttrykk.notasjon()}'${komparatorText()}'${mengdeUttrykk.notasjon()}'"

    override fun konkret(): String = "${fired.svarord()} '${uttrykk.konkret()}'${komparatorText()}'${mengdeUttrykk.evaluer().map { it.toString() }}'"

    override fun faktumSet(): Set<Faktum<*>> = uttrykk.faktumSet() + mengdeUttrykk.faktumSet()

    override fun forklar(level: Int): String = buildString {
        val uttrykkItems = mengdeUttrykk.evaluer().map { "'${it.toString()}'" }
        indent(level).append("${notasjon()}\n")
        indent(level + 1).append("${konkret()}\n")
        uttrykkItems.forEach {
            indent(level + 1).append(it.toString())
        }
    }
}