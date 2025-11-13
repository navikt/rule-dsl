package no.nav.system.rule.dsl.forklaring

import kotlin.math.min

// ========================================================================
// Aritmetiske operatorer
// ========================================================================

/**
 * Addisjon.
 */
internal data class Add<T : Number>(
    val venstre: Uttrykk<Number>,
    val høyre: Uttrykk<Number>
) : BinærOperator<Number, Number, T>() {

    override fun venstre() = venstre
    override fun høyre() = høyre

    override fun operatorNavn() = "Add"
    override fun operatorSymbol() = "+"

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
}

/**
 * Subtraksjon.
 */
internal data class Sub<T : Number>(
    val venstre: Uttrykk<Number>,
    val høyre: Uttrykk<Number>
) : BinærOperator<Number, Number, T>() {

    override fun venstre() = venstre
    override fun høyre() = høyre

    override fun operatorNavn() = "Sub"
    override fun operatorSymbol() = "-"
    override fun høyreSideParentes() = true  // Sub krever parenteser på høyre side

    @Suppress("UNCHECKED_CAST")
    override fun evaluer(): T {
        // Cache evalueringene for å unngå dobbel-evaluering
        val vVerdi = venstre.evaluer()
        val hVerdi = høyre.evaluer()
        val resultat = vVerdi.toDouble() - hVerdi.toDouble()

        return if (vVerdi is Int && hVerdi is Int) {
            resultat.toInt() as T
        } else {
            resultat as T
        }
    }
}

/**
 * Multiplikasjon.
 */
internal data class Mul<T : Number>(
    val venstre: Uttrykk<Number>,
    val høyre: Uttrykk<Number>
) : BinærOperator<Number, Number, T>() {

    override fun venstre() = venstre
    override fun høyre() = høyre

    override fun operatorNavn() = "Mul"
    override fun operatorSymbol() = "*"

    @Suppress("UNCHECKED_CAST")
    override fun evaluer(): T {
        // Cache evalueringene for å unngå dobbel-evaluering
        val vVerdi = venstre.evaluer()
        val hVerdi = høyre.evaluer()
        val resultat = vVerdi.toDouble() * hVerdi.toDouble()

        return if (vVerdi is Int && hVerdi is Int) {
            resultat.toInt() as T
        } else {
            resultat as T
        }
    }
}

/**
 * Divisjon (gir alltid Double).
 */
internal data class Div(
    val venstre: Uttrykk<Number>,
    val høyre: Uttrykk<Number>
) : BinærOperator<Number, Number, Double>() {

    override fun venstre() = venstre
    override fun høyre() = høyre

    override fun operatorNavn() = "Div"
    override fun operatorSymbol() = "/"
    override fun høyreSideParentes() = true  // Div krever parenteser på høyre side

    override fun evaluer(): Double {
        val v = venstre.evaluer().toDouble()
        val h = høyre.evaluer().toDouble()

        if (h == 0.0) {
            throw ArithmeticException("Divisjon med null: $v / $h")
        }

        return v / h
    }
}

/**
 * Heltallsdivisjon (integer division).
 * Bruker truncate-avrunding (.toInt()).
 *
 * Syntaks: `teller div nevner`
 * Notasjon: `teller // nevner`
 *
 * Eksempler:
 * - 10 div 3 = 3
 * - -10 div 3 = -3 (truncate mot null)
 */
internal data class IntDiv(
    val venstre: Uttrykk<Number>,
    val høyre: Uttrykk<Number>
) : BinærOperator<Number, Number, Int>() {

    override fun venstre() = venstre
    override fun høyre() = høyre

    override fun operatorNavn() = "IntDiv"
    override fun operatorSymbol() = "//"
    override fun høyreSideParentes() = true  // IntDiv krever parenteser på høyre side

    override fun evaluer(): Int {
        val v = venstre.evaluer().toDouble()
        val h = høyre.evaluer().toDouble()

        if (h == 0.0) {
            throw ArithmeticException("Heltallsdivisjon med null: $v // $h")
        }

        return (v / h).toInt()  // Truncate mot null
    }
}

/**
 * Minimum av to tall.
 */
internal data class Min(
    val venstre: Uttrykk<Number>,
    val høyre: Uttrykk<Number>
) : BinærOperator<Number, Number, Double>() {

    override fun venstre() = venstre
    override fun høyre() = høyre

    override fun operatorNavn() = "Min"
    override fun operatorSymbol() = "min"  // Brukes ikke i standard notasjon
    override fun høyreSideParentes() = true  // Min bruker funksjonssyntaks

    override fun evaluer(): Double =
        min(venstre.evaluer().toDouble(), høyre.evaluer().toDouble())

    // Min har spesiell funksjonssyntaks i stedet for infix
    override fun notasjon(): String {
        val v = venstre.notasjon().medParentesVedBehov(venstre)
        val h = høyre.notasjon().medParentesVedBehov(høyre, true)
        return "min($v,$h)"
    }

    override fun konkret(): String {
        val v = venstre.konkret().medParentesVedBehov(venstre)
        val h = høyre.konkret().medParentesVedBehov(høyre, true)
        return "min($v,$h)"
    }
}

/**
 * Negasjon (unær minus).
 */
internal data class Neg<T : Number>(
    val uttrykk: Uttrykk<Number>
) : UnærOperator<Number, T>() {

    override fun uttrykk() = uttrykk

    override fun operatorNavn() = "Neg"
    override fun operatorSymbol() = "-"

    // Override notasjon/konkret to remove space between - and operand
    override fun notasjon(): String = "-${uttrykk.notasjon().medParentesVedBehov(uttrykk)}"

    override fun konkret(): String = "-${uttrykk.konkret().medParentesVedBehov(uttrykk)}"

    @Suppress("UNCHECKED_CAST")
    override fun evaluer(): T {
        // Cache evalueringen for å unngå dobbel-evaluering
        val verdi = uttrykk.evaluer()
        val resultat = -verdi.toDouble()

        return if (verdi is Int) {
            resultat.toInt() as T
        } else {
            resultat as T
        }
    }
}
