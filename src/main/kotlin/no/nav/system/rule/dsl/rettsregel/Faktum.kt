package no.nav.system.rule.dsl.rettsregel

import no.nav.system.rule.dsl.Predicate
import no.nav.system.rule.dsl.enums.Comparator
import no.nav.system.rule.dsl.enums.ListComparator
import no.nav.system.rule.dsl.enums.PairComparator
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
    fun grunnlagListe(): List<Uttrykk<Any>>


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
        val v = venstre.notasjon().medParentesVedBehov(venstre)
        val h = høyre.notasjon().medParentesVedBehov(høyre)
        return "$v + $h"
    }

    override fun konkret(): String {
        val v = venstre.konkret().medParentesVedBehov(venstre)
        val h = høyre.konkret().medParentesVedBehov(høyre)
        return "$v + $h"
    }

    override fun grunnlagListe(): List<Uttrykk<Any>> =
        venstre.grunnlagListe() + høyre.grunnlagListe()

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
        val v = venstre.notasjon().medParentesVedBehov(venstre)
        val h = høyre.notasjon().medParentesVedBehov(høyre, true)
        return "$v - $h"
    }

    override fun konkret(): String {
        val v = venstre.konkret().medParentesVedBehov(venstre)
        val h = høyre.konkret().medParentesVedBehov(høyre, true)
        return "$v - $h"
    }

    override fun grunnlagListe(): List<Uttrykk<Any>> =
        venstre.grunnlagListe() + høyre.grunnlagListe()

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
        val v = venstre.notasjon().medParentesVedBehov(venstre)
        val h = høyre.notasjon().medParentesVedBehov(høyre)
        return "$v * $h"
    }

    override fun konkret(): String {
        val v = venstre.konkret().medParentesVedBehov(venstre)
        val h = høyre.konkret().medParentesVedBehov(høyre)
        return "$v * $h"
    }

    override fun grunnlagListe(): List<Uttrykk<Any>> =
        venstre.grunnlagListe() + høyre.grunnlagListe()

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
        val v = venstre.notasjon().medParentesVedBehov(venstre)
        val h = høyre.notasjon().medParentesVedBehov(høyre, true)
        return "$v / $h"
    }

    override fun konkret(): String {
        val v = venstre.konkret().medParentesVedBehov(venstre)
        val h = høyre.konkret().medParentesVedBehov(høyre, true)
        return "$v / $h"
    }

    override fun grunnlagListe(): List<Uttrykk<Any>> =
        venstre.grunnlagListe() + høyre.grunnlagListe()

}


/**
 * Hjelpefunksjon for å legge til parenteser ved behov.
 */
private fun String.medParentesVedBehov(uttrykk: Uttrykk<*>, høyreSide: Boolean = false): String {
    val trengerParentes = when (uttrykk) {
        is Add, is Sub -> true
        else -> false
    }

    return if (trengerParentes) "($this)" else this
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

    override fun grunnlagListe(): List<Faktum<out Any>> = listOf(this)

    fun forklaring(): String = "${hva()} = ${konkret()}"
    fun forklarUsing(forklaringProdusent: (Uttrykk<Any>) -> String): String = forklaringProdusent(this)

    fun hva(): String = navn

    fun hvordan(): String = buildHvordan(0).trimEnd()

    fun hvorfor(): String = hvorfor ?: "hvorfor ikke tilgjengelig"
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

    override fun grunnlagListe(): List<Faktum<out Any>> = emptyList()

    override fun toString(): String = verdi.toString()
}

/**
 * The application of a [function] that returns the boolean.
 */
abstract class DomainPredicate(
    open val comparator: Comparator,
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

    fun komparatorText(): String = if (fired) comparator.text else comparator.negated()

}

/**
 * Compares [venstre] with [høyre]
 */
class PairDomainPredicate(
    override val comparator: PairComparator,
    private val venstre: Uttrykk<*>,
    private val høyre: Uttrykk<*>,
    override val function: () -> Boolean,
) : DomainPredicate(comparator = comparator, function = function) {

    override fun type(): RuleComponentType = DOMENE_PREDIKAT_PAR

    override fun toString(): String = "${fired.svarord()} '${venstre.notasjon()}' (${venstre.konkret()})${komparatorText()}'${høyre.notasjon()}' (${høyre.konkret()})"

    override fun evaluer(): Boolean = fired

    override fun notasjon(): String = "${fired.svarord()} ${venstre.notasjon()}${komparatorText()}${høyre.notasjon()}"

    override fun konkret(): String = "${venstre.konkret()}${komparatorText()}${høyre.konkret()}"

    override fun grunnlagListe(): List<Uttrykk<Any>> = listOf(venstre, høyre)
}

/**
 * Compares [uttrykk] relationship with items [uttrykkList]
 */
class ListDomainPredicate(
    override val comparator: ListComparator,
    private val uttrykk: Uttrykk<*>,
    val uttrykkList: List<Uttrykk<*>>,
    override val function: () -> Boolean
) : DomainPredicate(comparator = comparator, function = function) {

    override fun type(): RuleComponentType = DOMENE_PREDIKAT_LISTE

    override fun toString(): String = "${fired.svarord()} $uttrykk${komparatorText()}$uttrykkList"

    override fun evaluer(): Boolean = fired

    override fun notasjon(): String = "${uttrykk.notasjon()}${komparatorText()}${uttrykkList.map { it.notasjon() }}"

    override fun konkret(): String = "${uttrykk.konkret()}${komparatorText()}${uttrykkList.map { it.konkret() }}"

    override fun grunnlagListe(): List<Uttrykk<Any>> = listOf(uttrykk) + uttrykkList

}

/**
 * Helper function to recursively build hvordan explanation with proper indentation.
 */
private fun Faktum<*>.buildHvordan(depth: Int): String {
    val indent = "  ".repeat(depth)
    val sb = StringBuilder()

    // Show this faktum's name and value
    sb.append("$indent$navn = ${evaluer()}\n")

    // If this is a computed value (not a simple constant), show its grunnlag
    if (uttrykk !is Const) {
        // Recursively show each grunnlag faktum
        for (grunnlag in uttrykk.grunnlagListe()) {
            if (grunnlag is Faktum<*>) {
                sb.append(grunnlag.buildHvordan(depth + 1))
            }
        }
    }

    return sb.toString()
}