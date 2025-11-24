package no.nav.system.rule.dsl.rettsregel

import no.nav.system.rule.dsl.enums.ListOperator
import no.nav.system.rule.dsl.enums.MathOperator
import no.nav.system.rule.dsl.enums.PairOperator
import no.nav.system.rule.dsl.reference.Reference
import no.nav.system.rule.dsl.rettsregel.helper.svarord
import java.io.Serializable

/**
 * Numeric expression tree.
 *
 * IMPORTANT: This is now INTERNAL to Faktum<Number>.
 * Users do not work with Uttrykk directly - they use Faktum operators.
 *
 */
interface Uttrykk<out T : Any> : Serializable {
    val verdi: T
    fun notasjon(): String
    fun konkret(): String

    /**
     * Hvilke navngitte faktum bidrar til dette uttrykket.
     */
    fun faktumSet(): Set<Faktum<*>>
    fun forklar(level: Int = 0): String
}

/**
 * Math operation for arithmetic calculations (addition, subtraction, multiplication, division).
 * This replaces the previous Add, Sub, Mul, Div classes with a single implementation.
 */
internal data class MathOperation<T : Number>(
    val venstre: Uttrykk<Number>,
    val høyre: Uttrykk<Number>,
    val operator: MathOperator,
    val evaluator: () -> T
) : Uttrykk<T> {
    override val verdi: T by lazy { evaluator() }

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

    override fun forklar(level: Int): String = buildString {
        indent(level).append("HVORDAN\n")
        indent(level + 1).append("${notasjon()}\n")
        indent(level + 1).append("${konkret()}\n")

        faktumSet().forEach { faktum ->
            // Show full explanation for each contributing Faktum at the same level
            // (only indent for named Faktum, not for intermediate AST nodes)
            append(faktum.forklar(level))
        }
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
        operator: MathOperator,
        høyre: String,
        høyreUttrykk: Uttrykk<*>
    ): Pair<String, String> {
        val venstreTrengerParentes = when (operator) {
            MathOperator.MUL, MathOperator.DIV ->
                venstreUttrykk is MathOperation<*> &&
                        (venstreUttrykk.operator == MathOperator.ADD || venstreUttrykk.operator == MathOperator.SUB)

            else -> false
        }

        val høyreTrengerParentes = when (operator) {
            MathOperator.MUL, MathOperator.DIV, MathOperator.SUB ->
                høyreUttrykk is MathOperation<*> &&
                        (høyreUttrykk.operator == MathOperator.ADD || høyreUttrykk.operator == MathOperator.SUB)

            else -> false
        }

        val v = if (venstreTrengerParentes) "($venstre)" else venstre
        val h = if (høyreTrengerParentes) "($høyre)" else høyre

        return v to h
    }

}

/**
 * Comparison operation for pair comparisons (erLik, erMindreEnn, erFør, etc.).
 * Pure boolean expression without Predicate machinery.
 */
internal data class ComparisonOperation(
    val venstre: Uttrykk<*>,
    val høyre: Uttrykk<*>,
    val operator: PairOperator,
    val evaluator: () -> Boolean
) : Uttrykk<Boolean> {

    override val verdi: Boolean by lazy { evaluator() }

    override fun notasjon(): String = "${verdi.svarord()} '${venstre.notasjon()}'${operatorText()}'${høyre.notasjon()}'"

    override fun konkret(): String = "${verdi.svarord()} '${venstre.konkret()}'${operatorText()}'${høyre.konkret()}'"

    override fun toString(): String = "${verdi.svarord()} ${venstre}${operatorText()}${høyre}"

    private fun operatorText(): String = if (verdi) operator.text else operator.negated()

    override fun faktumSet(): Set<Faktum<*>> = venstre.faktumSet() + høyre.faktumSet()

    override fun forklar(level: Int): String = buildString {
        indent(level).append("${notasjon()}\n")
        indent(level).append("${konkret()}\n")
    }
}


/**
 * List operation for list membership checks (erBlant, erIkkeBlant).
 * Pure boolean expression without Predicate machinery.
 */
internal data class ListOperation(
    val uttrykk: Uttrykk<*>,
    val mengdeUttrykk: Uttrykk<List<*>>,
    val operator: ListOperator,
    private val evaluator: () -> Boolean
) : Uttrykk<Boolean> {

    override val verdi: Boolean by lazy { evaluator() }

    override fun notasjon(): String = "${verdi.svarord()} '${uttrykk.notasjon()}'${operatorText()}'${mengdeUttrykk.notasjon()}'"

    override fun konkret(): String = "${verdi.svarord()} '${uttrykk.konkret()}'${operatorText()}'${mengdeUttrykk.verdi.map { it.toString() }}'"

    override fun toString(): String = "${verdi.svarord()} ${uttrykk}${operatorText()}${mengdeUttrykk}"

    private fun operatorText(): String = if (verdi) operator.text else operator.negated()
    override fun faktumSet(): Set<Faktum<*>> = uttrykk.faktumSet() + mengdeUttrykk.faktumSet()

    override fun forklar(level: Int): String = buildString {
        val uttrykkItems = mengdeUttrykk.verdi.map { "'${it.toString()}'" }
        indent(level).append("${notasjon()}\n")
        indent(level + 1).append("${konkret()}\n")
        uttrykkItems.forEach {
            indent(level + 1).append(it.toString())
        }
    }
}

private fun StringBuilder.indent(level: Int): StringBuilder = append(" ".repeat(level * 2))
private fun StringBuilder.appendIf(level: Int, statement: () -> Boolean): StringBuilder = if (statement()) append(" ".repeat(level * 2)) else this


/**
 * Named expression - treated as atomic unit for fact tracking.
 *
 * Faktum wraps any [Uttrykk] and gives it a name, making it user-visible and trackable.
 * It acts as the boundary between anonymous calculations (Const, MathOperation) and
 * named business facts that appear in rule explanations.
 *
 * Pure data class - presentation is handled separately via extension functions.
 * This separation allows users to write custom explanation/presentation logic.
 *
 * Example:
 * ```
 * val alder = Faktum("Alder", 67)              // Named fact
 * val sats = Faktum("Sats", 1000)              // Named fact
 * val produkt = alder * sats                   // Anonymous MathOperation
 * val tillegg = Faktum("Tillegg", produkt)     // Wrap as named result
 *
 * produkt.faktumSet()  // → { alder, sats }    Input facts
 * tillegg.faktumSet()  // → { tillegg }         Result fact (stops at boundary)
 * tillegg.uttrykk.faktumSet()  // → { alder, sats }  Contributing facts inside
 * ```
 *
 * This is can be created by users.
 */
data class Faktum<T : Any>(
    val navn: String,
    val uttrykk: Uttrykk<T>,
    val references: List<Reference> = emptyList()
) : Uttrykk<T> {

    constructor(
        navn: String,
        verdi: T,
        references: List<Reference> = emptyList()
    ) : this(
        navn = navn,
        uttrykk = Const(verdi),
        references = references
    )

    /**
     * Backlink to the FaktumNode wrapper when this Faktum is added to the ARC tree.
     * Used by extension functions to compute hvorfor by traversing up the tree.
     */
    @Transient
    internal var wrapperNode: no.nav.system.rule.dsl.FaktumNode<T>? = null

    override val verdi: T by lazy { uttrykk.verdi }

    override fun notasjon(): String = navn

    override fun konkret(): String = verdi.toString()

    /**
     * Returns this Faktum as the atomic unit.
     *
     * IMPORTANT: Returns `setOf(this)` rather than `uttrykk.faktumSet()` because
     * Faktum defines the boundary for named facts. This stops recursion at the name,
     * enabling layered explanations (result facts vs. input facts).
     *
     * To access contributing facts inside this Faktum, use `uttrykk.faktumSet()`.
     */
    override fun faktumSet(): Set<Faktum<*>> = setOf(this)

    override fun toString(): String = "'$navn' ($verdi)"

    /**
     * Internal implementation for Uttrykk interface.
     * Users should use extension functions in ExplanationBuilder.kt for presentation.
     */
    override fun forklar(level: Int): String = buildString {
        indent(level).append("$navn = $verdi\n")
        // Show formula/nested explanation if not a simple constant
        if (uttrykk !is Const<*>) {
            append(uttrykk.forklar(level))
        }
    }
}

/**
 * Unnamed constant - treated as atomic unit.
 * This is can NOT be created by users.
 */
internal data class Const<T : Any>(
    override val verdi: T
) : Uttrykk<T> {

    override fun notasjon(): String = verdi.toString()

    override fun konkret(): String = verdi.toString()

    override fun faktumSet(): Set<Faktum<out Any>> = emptySet()

    override fun toString(): String = "'$verdi'"
    override fun forklar(level: Int): String = ""
}