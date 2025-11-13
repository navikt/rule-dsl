package no.nav.system.rule.dsl.forklaring

import no.nav.system.rule.dsl.rettsregel.Faktum
import java.io.Serializable
import kotlin.math.min

/**
 * Rekursiv uttrykksstruktur for matematiske beregninger.
 *
 * Dette er en alternativ representasjon av Formel som er mer eksplisitt rekursiv
 * og lettere å traverse for regelsporing.
 *
 * Inspirert av klassiske Expression Trees / Abstract Syntax Trees.
 *
 * ## Eksempel - Enkel syntaks (med Faktum operator overloading)
 * ```kotlin
 * val G = Faktum("G", 110000)
 * val sats = Faktum("sats", 0.25)
 * val måneder = Faktum("måneder", 12)
 *
 * // Direkte bruk av Faktum uten Var()
 * val uttrykk = sats * G / måneder
 *
 * // Evaluering
 * val resultat = uttrykk.evaluer()  // 2291.67
 *
 * // Forklaring
 * val forklaring = uttrykk.forklar("beregning")
 * // Output: beregning = sats * G / måneder
 * //         beregning = 0.25 * 110000 / 12
 * //         beregning = 2291.67
 * ```
 *
 * ## Eksempel - Eksplisitt syntaks (med Var og Const)
 * ```kotlin
 * val uttrykk = Div(
 *     Mul(Var(sats), Var(G)),
 *     Var(måneder)
 * )
 * ```
 */
sealed interface Uttrykk<out T : Any> : Serializable {
    /**
     * Evaluerer uttrykket til en verdi.
     */
    fun evaluer(): T

    /**
     * Genererer symbolsk notasjon (med variabelnavn).
     */
    fun notasjon(): String

    /**
     * Genererer konkret notasjon (med verdier).
     */
    fun konkret(): String

    /**
     * Returnerer liste av alle grunnlag brukt i uttrykket.
     */
    fun grunnlagListe(): List<Grunnlag<out Any>>

    /**
     * Returnerer dybde av uttrykkstre.
     */
    fun dybde(): Int

    /**
     * Genererer en strukturell hash av uttrykkstre for deduplikasjon.
     *
     * Hash-verdien er basert på:
     * - Type av uttrykk (Add, Mul, Grunnlag, etc.)
     * - Verdier i Const-noder
     * - Navn i Grunnlag-noder
     * - Strukturell hash av child-noder
     *
     * To uttrykk med samme struktur og verdier vil ha samme hash,
     * uavhengig av objektreferanse.
     */
    fun strukturellHash(): String
}

/**
 * Konstant verdi.
 */
data class Const<T : Any>(
    val verdi: T,
    var funksjon: String? = null
) : Uttrykk<T> {
    override fun evaluer(): T = verdi

    override fun notasjon(): String = verdi.toString()

    override fun konkret(): String = verdi.toString()

    override fun grunnlagListe(): List<Grunnlag<out Any>> = emptyList()

    override fun dybde(): Int = 1

    override fun strukturellHash(): String = "Const:${verdi::class.simpleName}:$verdi"

    override fun toString(): String = verdi.toString()
}

/**
 * Addisjon.
 */
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

    override fun grunnlagListe(): List<Grunnlag<out Any>> =
        venstre.grunnlagListe() + høyre.grunnlagListe()

    override fun dybde(): Int = 1 + maxOf(venstre.dybde(), høyre.dybde())

    override fun strukturellHash(): String = "Add:${venstre.strukturellHash()}:${høyre.strukturellHash()}"
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

    override fun grunnlagListe(): List<Grunnlag<out Any>> =
        venstre.grunnlagListe() + høyre.grunnlagListe()

    override fun dybde(): Int = 1 + maxOf(venstre.dybde(), høyre.dybde())

    override fun strukturellHash(): String = "Sub:${venstre.strukturellHash()}:${høyre.strukturellHash()}"
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

    override fun grunnlagListe(): List<Grunnlag<out Any>> =
        venstre.grunnlagListe() + høyre.grunnlagListe()

    override fun dybde(): Int = 1 + maxOf(venstre.dybde(), høyre.dybde())

    override fun strukturellHash(): String = "Mul:${venstre.strukturellHash()}:${høyre.strukturellHash()}"
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

    override fun grunnlagListe(): List<Grunnlag<out Any>> =
        venstre.grunnlagListe() + høyre.grunnlagListe()

    override fun dybde(): Int = 1 + maxOf(venstre.dybde(), høyre.dybde())

    override fun strukturellHash(): String = "Div:${venstre.strukturellHash()}:${høyre.strukturellHash()}"
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
) : Uttrykk<Int> {
    override fun evaluer(): Int {
        val v = venstre.evaluer().toDouble()
        val h = høyre.evaluer().toDouble()

        if (h == 0.0) {
            throw ArithmeticException("Heltallsdivisjon med null: $v // $h")
        }

        return (v / h).toInt()  // Truncate mot null
    }

    override fun notasjon(): String {
        val v = venstre.notasjon().medParentesVedBehov(venstre)
        val h = høyre.notasjon().medParentesVedBehov(høyre, true)
        return "$v // $h"
    }

    override fun konkret(): String {
        val v = venstre.konkret().medParentesVedBehov(venstre)
        val h = høyre.konkret().medParentesVedBehov(høyre, true)
        return "$v // $h"
    }

    override fun grunnlagListe(): List<Grunnlag<out Any>> =
        venstre.grunnlagListe() + høyre.grunnlagListe()

    override fun dybde(): Int = 1 + maxOf(venstre.dybde(), høyre.dybde())

    override fun strukturellHash(): String = "IntDiv:${venstre.strukturellHash()}:${høyre.strukturellHash()}"
}

internal data class Min(
    val venstre: Uttrykk<Number>,
    val høyre: Uttrykk<Number>
) : Uttrykk<Double> {
    override fun evaluer(): Double =
        min(venstre.evaluer().toDouble(), høyre.evaluer().toDouble())


    override fun notasjon(): String {
        val v = venstre.notasjon().medParentesVedBehov(venstre)
        val h = høyre.notasjon().medParentesVedBehov(høyre,true)
        return "min($v,$h)"
    }

    override fun konkret(): String {
        val v = venstre.konkret().medParentesVedBehov(venstre)
        val h = høyre.konkret().medParentesVedBehov(høyre, true)
        return "min($v,$h)"
    }

    override fun grunnlagListe(): List<Grunnlag<out Any>> =
        venstre.grunnlagListe() + høyre.grunnlagListe()

    override fun dybde(): Int = 1 + maxOf(venstre.dybde(), høyre.dybde())

    override fun strukturellHash(): String = "Min:${venstre.strukturellHash()}:${høyre.strukturellHash()}"
}

/**
 * Negasjon (unær minus).
 */
internal data class Neg<T : Number>(
    val uttrykk: Uttrykk<Number>
) : Uttrykk<T> {
    @Suppress("UNCHECKED_CAST")
    override fun evaluer(): T {
        // Cache evalueringen for å unngå dobbel-evaluering
        val verdi = uttrykk.evaluer()
        val v = verdi.toDouble()
        val resultat = -v

        return if (verdi is Int) {
            resultat.toInt() as T
        } else {
            resultat as T
        }
    }

    override fun notasjon(): String = "-${uttrykk.notasjon().medParentesVedBehov(uttrykk)}"

    override fun konkret(): String = "-${uttrykk.konkret().medParentesVedBehov(uttrykk)}"

    override fun grunnlagListe(): List<Grunnlag<out Any>> = uttrykk.grunnlagListe()

    override fun dybde(): Int = 1 + uttrykk.dybde()

    override fun strukturellHash(): String = "Neg:${uttrykk.strukturellHash()}"
}

/**
 * Grunnlag uttrykk - gir et navn til et kompleks uttrykk.
 * Tilsvarer "locked" formler i dagens Formel-implementasjon.
 */
data class Grunnlag<T : Any>(
    val navn: String,
    val uttrykk: Uttrykk<T>,
    val rvsId: String? = null,
    var funksjon: String? = null
) : Uttrykk<T> {
    override fun evaluer(): T = uttrykk.evaluer()

    override fun notasjon(): String = navn

    override fun konkret(): String = evaluer().toString()

    override fun grunnlagListe(): List<Grunnlag<out Any>> = listOf(this)

    override fun dybde(): Int = 1  // Grunnlag uttrykk teller som atomisk

    override fun strukturellHash(): String = "Grunnlag:$navn:${uttrykk.strukturellHash()}"

    /**
     * Returnerer det underliggende uttrykket.
     */
    fun utpakk(): Uttrykk<T> = uttrykk
}

/**
 * Logisk OG-operator.
 */
internal data class Og(
    val venstre: Uttrykk<Boolean>,
    val høyre: Uttrykk<Boolean>
) : Uttrykk<Boolean> {
    override fun evaluer(): Boolean = venstre.evaluer() && høyre.evaluer()

    override fun notasjon(): String {
        val v = venstre.notasjon().medParentesVedBehov(venstre)
        val h = høyre.notasjon().medParentesVedBehov(høyre)
        return "$v OG $h"
    }

    override fun konkret(): String {
        val v = venstre.konkret().medParentesVedBehov(venstre)
        val h = høyre.konkret().medParentesVedBehov(høyre)
        return "$v OG $h"
    }

    override fun grunnlagListe(): List<Grunnlag<out Any>> =
        venstre.grunnlagListe() + høyre.grunnlagListe()

    override fun dybde(): Int = 1 + maxOf(venstre.dybde(), høyre.dybde())

    override fun strukturellHash(): String = "Og:${venstre.strukturellHash()}:${høyre.strukturellHash()}"
}

/**
 * Logisk ELLER-operator.
 */
internal data class Eller(
    val venstre: Uttrykk<Boolean>,
    val høyre: Uttrykk<Boolean>
) : Uttrykk<Boolean> {
    override fun evaluer(): Boolean = venstre.evaluer() || høyre.evaluer()

    override fun notasjon(): String {
        val v = venstre.notasjon().medParentesVedBehov(venstre)
        val h = høyre.notasjon().medParentesVedBehov(høyre)
        return "$v ELLER $h"
    }

    override fun konkret(): String {
        val v = venstre.konkret().medParentesVedBehov(venstre)
        val h = høyre.konkret().medParentesVedBehov(høyre)
        return "$v ELLER $h"
    }

    override fun grunnlagListe(): List<Grunnlag<out Any>> =
        venstre.grunnlagListe() + høyre.grunnlagListe()

    override fun dybde(): Int = 1 + maxOf(venstre.dybde(), høyre.dybde())

    override fun strukturellHash(): String = "Eller:${venstre.strukturellHash()}:${høyre.strukturellHash()}"
}

/**
 * Logisk IKKE-operator (negasjon).
 */
internal data class Ikke(
    val uttrykk: Uttrykk<Boolean>
) : Uttrykk<Boolean> {
    override fun evaluer(): Boolean = !uttrykk.evaluer()

    override fun notasjon(): String = "IKKE ${uttrykk.notasjon().medParentesVedBehov(uttrykk)}"

    override fun konkret(): String = "IKKE ${uttrykk.konkret().medParentesVedBehov(uttrykk)}"

    override fun grunnlagListe(): List<Grunnlag<out Any>> = uttrykk.grunnlagListe()

    override fun dybde(): Int = 1 + uttrykk.dybde()

    override fun strukturellHash(): String = "Ikke:${uttrykk.strukturellHash()}"
}

/**
 * Sammenligning: Lik (==).
 */
internal data class Lik<T : Comparable<T>>(
    val venstre: Uttrykk<T>,
    val høyre: Uttrykk<T>
) : Uttrykk<Boolean> {
    override fun evaluer(): Boolean = venstre.evaluer() == høyre.evaluer()

    override fun notasjon(): String = "${venstre.notasjon()} = ${høyre.notasjon()}"

    override fun konkret(): String = "${venstre.konkret()} = ${høyre.konkret()}"

    override fun grunnlagListe(): List<Grunnlag<out Any>> =
        venstre.grunnlagListe() + høyre.grunnlagListe()

    override fun dybde(): Int = 1 + maxOf(venstre.dybde(), høyre.dybde())

    override fun strukturellHash(): String = "Lik:${venstre.strukturellHash()}:${høyre.strukturellHash()}"
}

/**
 * Sammenligning: Ulik (!=).
 */
internal data class Ulik<T : Comparable<T>>(
    val venstre: Uttrykk<T>,
    val høyre: Uttrykk<T>
) : Uttrykk<Boolean> {
    override fun evaluer(): Boolean = venstre.evaluer() != høyre.evaluer()

    override fun notasjon(): String = "${venstre.notasjon()} ≠ ${høyre.notasjon()}"

    override fun konkret(): String = "${venstre.konkret()} ≠ ${høyre.konkret()}"

    override fun grunnlagListe(): List<Grunnlag<out Any>> =
        venstre.grunnlagListe() + høyre.grunnlagListe()

    override fun dybde(): Int = 1 + maxOf(venstre.dybde(), høyre.dybde())

    override fun strukturellHash(): String = "Ulik:${venstre.strukturellHash()}:${høyre.strukturellHash()}"
}

/**
 * Sammenligning: Større enn (>).
 */
internal data class StørreEnn<T : Comparable<T>>(
    val venstre: Uttrykk<T>,
    val høyre: Uttrykk<T>
) : Uttrykk<Boolean> {
    override fun evaluer(): Boolean = venstre.evaluer() > høyre.evaluer()

    override fun notasjon(): String = "${venstre.notasjon()} > ${høyre.notasjon()}"

    override fun konkret(): String = "${venstre.konkret()} > ${høyre.konkret()}"

    override fun grunnlagListe(): List<Grunnlag<out Any>> =
        venstre.grunnlagListe() + høyre.grunnlagListe()

    override fun dybde(): Int = 1 + maxOf(venstre.dybde(), høyre.dybde())

    override fun strukturellHash(): String = "StørreEnn:${venstre.strukturellHash()}:${høyre.strukturellHash()}"
}

/**
 * Sammenligning: Mindre enn (<).
 */
internal data class MindreEnn<T : Comparable<T>>(
    val venstre: Uttrykk<T>,
    val høyre: Uttrykk<T>
) : Uttrykk<Boolean> {
    override fun evaluer(): Boolean = venstre.evaluer() < høyre.evaluer()

    override fun notasjon(): String = "${venstre.notasjon()} < ${høyre.notasjon()}"

    override fun konkret(): String = "${venstre.konkret()} < ${høyre.konkret()}"

    override fun grunnlagListe(): List<Grunnlag<out Any>> =
        venstre.grunnlagListe() + høyre.grunnlagListe()

    override fun dybde(): Int = 1 + maxOf(venstre.dybde(), høyre.dybde())

    override fun strukturellHash(): String = "MindreEnn:${venstre.strukturellHash()}:${høyre.strukturellHash()}"
}

/**
 * Sammenligning: Større eller lik (>=).
 */
internal data class StørreEllerLik<T : Comparable<T>>(
    val venstre: Uttrykk<T>,
    val høyre: Uttrykk<T>
) : Uttrykk<Boolean> {
    override fun evaluer(): Boolean = venstre.evaluer() >= høyre.evaluer()

    override fun notasjon(): String = "${venstre.notasjon()} ≥ ${høyre.notasjon()}"

    override fun konkret(): String = "${venstre.konkret()} ≥ ${høyre.konkret()}"

    override fun grunnlagListe(): List<Grunnlag<out Any>> =
        venstre.grunnlagListe() + høyre.grunnlagListe()

    override fun dybde(): Int = 1 + maxOf(venstre.dybde(), høyre.dybde())

    override fun strukturellHash(): String = "StørreEllerLik:${venstre.strukturellHash()}:${høyre.strukturellHash()}"
}

/**
 * Sammenligning: Mindre eller lik (<=).
 */
internal data class MindreEllerLik<T : Comparable<T>>(
    val venstre: Uttrykk<T>,
    val høyre: Uttrykk<T>
) : Uttrykk<Boolean> {
    override fun evaluer(): Boolean = venstre.evaluer() <= høyre.evaluer()

    override fun notasjon(): String = "${venstre.notasjon()} ≤ ${høyre.notasjon()}"

    override fun konkret(): String = "${venstre.konkret()} ≤ ${høyre.konkret()}"

    override fun grunnlagListe(): List<Grunnlag<out Any>> =
        venstre.grunnlagListe() + høyre.grunnlagListe()

    override fun dybde(): Int = 1 + maxOf(venstre.dybde(), høyre.dybde())

    override fun strukturellHash(): String = "MindreEllerLik:${venstre.strukturellHash()}:${høyre.strukturellHash()}"
}

/**
 * Sammenligning: Verdi er blant liste (in).
 */
internal data class ErBlant<T : Any>(
    val verdi: Uttrykk<T>,
    val liste: Uttrykk<List<T>>
) : Uttrykk<Boolean> {
    override fun evaluer(): Boolean = verdi.evaluer() in liste.evaluer()

    override fun notasjon(): String = "${verdi.notasjon()} ER BLANT ${liste.notasjon()}"

    override fun konkret(): String = "${verdi.konkret()} ER BLANT ${liste.konkret()}"

    override fun grunnlagListe(): List<Grunnlag<out Any>> =
        verdi.grunnlagListe() + liste.grunnlagListe()

    override fun dybde(): Int = 1 + maxOf(verdi.dybde(), liste.dybde())

    override fun strukturellHash(): String = "ErBlant:${verdi.strukturellHash()}:${liste.strukturellHash()}"
}

/**
 * Sammenligning: Verdi er ikke blant liste (not in).
 */
internal data class ErIkkeBlant<T : Any>(
    val verdi: Uttrykk<T>,
    val liste: Uttrykk<List<T>>
) : Uttrykk<Boolean> {
    override fun evaluer(): Boolean = verdi.evaluer() !in liste.evaluer()

    override fun notasjon(): String = "${verdi.notasjon()} ER IKKE BLANT ${liste.notasjon()}"

    override fun konkret(): String = "${verdi.konkret()} ER IKKE BLANT ${liste.konkret()}"

    override fun grunnlagListe(): List<Grunnlag<out Any>> =
        verdi.grunnlagListe() + liste.grunnlagListe()

    override fun dybde(): Int = 1 + maxOf(verdi.dybde(), liste.dybde())

    override fun strukturellHash(): String = "ErIkkeBlant:${verdi.strukturellHash()}:${liste.strukturellHash()}"
}

/**
 * Betinget uttrykk som velger mellom to verdier basert på en Boolean-betingelse.
 *
 * Både SÅ og ELLERS må returnere verdier av samme type T.
 * Hvis-uttrykket kan navngis som et Grunnlag for sporbarhet.
 */
internal data class Hvis<T : Any>(
    val betingelse: Uttrykk<Boolean>,
    val såUttrykk: Uttrykk<T>,
    val ellersUttrykk: Uttrykk<T>
) : Uttrykk<T> {
    override fun evaluer(): T =
        if (betingelse.evaluer()) såUttrykk.evaluer() else ellersUttrykk.evaluer()

    override fun notasjon(): String = notasjonMedInnrykk(0)

    /**
     * Formaterer hvis-uttrykk med linjeskift og innrykk ved nøsting.
     */
    private fun notasjonMedInnrykk(nivå: Int): String {
        val indent = "  ".repeat(nivå)
        val nextIndent = "  ".repeat(nivå + 1)

        // Sjekk om ellers-grenen er et nøstet Hvis-uttrykk
        return if (ellersUttrykk is Hvis<*>) {
            buildString {
                append("HVIS ${betingelse.notasjon()}\n")
                append("${nextIndent}SÅ ${såUttrykk.notasjon()}\n")
                append("${nextIndent}ELLERS ")
                append(ellersUttrykk.notasjonMedInnrykk(nivå + 1))
            }
        } else {
            // Ikke nøstet - skriv på en linje
            "HVIS ${betingelse.notasjon()} SÅ ${såUttrykk.notasjon()} ELLERS ${ellersUttrykk.notasjon()}"
        }
    }

    override fun konkret(): String = konkretMedInnrykk(0)

    /**
     * Formaterer konkret hvis-uttrykk med linjeskift og innrykk ved nøsting.
     * Viser kun den grenen som faktisk ble valgt.
     */
    private fun konkretMedInnrykk(nivå: Int): String {
        val nextIndent = "  ".repeat(nivå + 1)

        return if (betingelse.evaluer()) {
            // SÅ-grenen ble valgt
            if (ellersUttrykk is Hvis<*>) {
                // Nøstet struktur - vis med formatering selv om vi ikke går inn i ellers-grenen
                buildString {
                    append("HVIS ${betingelse.konkret()}\n")
                    append("${nextIndent}SÅ ${såUttrykk.konkret()}")
                }
            } else {
                "HVIS ${betingelse.konkret()} SÅ ${såUttrykk.konkret()}"
            }
        } else {
            // ELLERS-grenen ble valgt
            if (ellersUttrykk is Hvis<*>) {
                buildString {
                    append("HVIS ${betingelse.konkret()}\n")
                    append("${nextIndent}ELLERS ")
                    append(ellersUttrykk.konkretMedInnrykk(nivå + 1))
                }
            } else {
                "HVIS ${betingelse.konkret()} ELLERS ${ellersUttrykk.konkret()}"
            }
        }
    }

    override fun grunnlagListe(): List<Grunnlag<out Any>> =
        betingelse.grunnlagListe() + såUttrykk.grunnlagListe() + ellersUttrykk.grunnlagListe()

    override fun dybde(): Int =
        1 + maxOf(betingelse.dybde(), såUttrykk.dybde(), ellersUttrykk.dybde())

    override fun strukturellHash(): String =
        "Hvis:${betingelse.strukturellHash()}:${såUttrykk.strukturellHash()}:${ellersUttrykk.strukturellHash()}"
}

/**
 * Hjelpefunksjon for å legge til parenteser ved behov.
 */
private fun String.medParentesVedBehov(uttrykk: Uttrykk<*>, høyreSide: Boolean = false): String {
    val trengerParentes = when (uttrykk) {
        is Add, is Sub -> true
        is Og -> true  // OG trenger parenteser når brukt i ELLER
        is Eller -> true  // ELLER har lavere presedens enn OG
        else -> false
    }

    return if (trengerParentes) "($this)" else this
}

/**
 * Operator overloading for naturlig syntaks.
 */
@Suppress("REDUNDANT_PROJECTION")
operator fun <T : Number> Uttrykk<T>.plus(other: Uttrykk<out Number>): Uttrykk<T> = Add(this, other)
operator fun <T : Number> Uttrykk<T>.plus(other: Number): Uttrykk<T> = Add(this, Const(other))
operator fun <T : Number> Number.plus(other: Uttrykk<T>): Uttrykk<T> = Add(Const(this), other)

@Suppress("REDUNDANT_PROJECTION")
operator fun <T : Number> Uttrykk<T>.minus(other: Uttrykk<out Number>): Uttrykk<T> = Sub(this, other)
operator fun <T : Number> Uttrykk<T>.minus(other: Number): Uttrykk<T> = Sub(this, Const(other))
operator fun <T : Number> Number.minus(other: Uttrykk<T>): Uttrykk<T> = Sub(Const(this), other)

@Suppress("REDUNDANT_PROJECTION")
operator fun <T : Number> Uttrykk<T>.times(other: Uttrykk<out Number>): Uttrykk<T> = Mul(this, other)
operator fun <T : Number> Uttrykk<T>.times(other: Number): Uttrykk<T> = Mul(this, Const(other))
operator fun <T : Number> Number.times(other: Uttrykk<T>): Uttrykk<T> = Mul(Const(this), other)

@Suppress("REDUNDANT_PROJECTION")
operator fun Uttrykk<out Number>.div(other: Uttrykk<out Number>): Uttrykk<Double> = Div(this, other)
@Suppress("REDUNDANT_PROJECTION")
operator fun Uttrykk<out Number>.div(other: Number): Uttrykk<Double> = Div(this, Const(other))
@Suppress("REDUNDANT_PROJECTION")
operator fun Number.div(other: Uttrykk<out Number>): Uttrykk<Double> = Div(Const(this), other)

/**
 * Heltallsdivisjon (integer division) med infix syntaks.
 *
 * Syntaks: `teller intdiv nevner`
 * Notasjon: `teller // nevner`
 *
 * Eksempler:
 * ```
 * val a = Const(10)
 * val b = Const(3)
 * val resultat = a intdiv b  // Returns Uttrykk<Int> with value 3
 * resultat.notasjon()        // Returns "10 // 3"
 * ```
 */
@Suppress("REDUNDANT_PROJECTION")
infix fun Uttrykk<out Number>.intdiv(other: Uttrykk<out Number>): Uttrykk<Int> = IntDiv(this, other)
@Suppress("REDUNDANT_PROJECTION")
infix fun Uttrykk<out Number>.intdiv(other: Number): Uttrykk<Int> = IntDiv(this, Const(other))
@Suppress("REDUNDANT_PROJECTION")
infix fun Number.intdiv(other: Uttrykk<out Number>): Uttrykk<Int> = IntDiv(Const(this), other)

operator fun <T : Number> Uttrykk<T>.unaryMinus(): Uttrykk<T> = Neg(this)

/**
 * Builder function for navngitte uttrykk.
 *
 * @param navn navnet på grunnlaget
 * @param memoise om resultatet skal caches (default: true for komplekse uttrykk)
 *
 * ## Eksempel
 * ```kotlin
 * val trygdetidFaktor = (faktiskTrygdetid / fullTrygdetid).navngi("trygdetidFaktor")
 * // Samme som: Grunnlag("trygdetidFaktor", Memo(faktiskTrygdetid / fullTrygdetid))
 * ```
 *
 * Memoisering er viktig når samme navngitte uttrykk brukes flere steder:
 * ```kotlin
 * val angittFlyktning = (betingelse1 eller betingelse2).navngi("angittFlyktning")
 * tabell {
 *     regel { når { angittFlyktning } ... }          // Evalueres én gang
 *     regel { når { angittFlyktning og x } ... }     // Bruker cachet verdi
 *     regel { når { angittFlyktning og y } ... }     // Bruker cachet verdi
 * }
 * ```
 */
fun <T : Any> Uttrykk<T>.navngi(navn: String, memoise: Boolean = true): Grunnlag<T> {
    val uttrykk = if (memoise && this !is Const && this !is Grunnlag) {
        this.memoise()
    } else {
        this
    }
    return Grunnlag(navn, uttrykk)
}

/**
 * Konverterer et Faktum til et Grunnlag (Uttrykk).
 * Dette gjør det enkelt å bruke Faktum-verdier i Uttrykk-systemet.
 *
 * Eksempel:
 * ```kotlin
 * val faktum = Faktum("alder", 67)
 * val grunnlag = faktum.toGrunnlag()  // Grunnlag("alder", Const(67))
 * ```
 */
fun <T : Any> no.nav.system.rule.dsl.rettsregel.Faktum<T>.toGrunnlag(): Grunnlag<T> =
    Grunnlag(this.name, Const(this.value))

/**
 * Setter rvsId på et navngitt uttrykk.
 */
fun <T : Any> Grunnlag<T>.id(rvsId: String): Grunnlag<T> = this.copy(rvsId = rvsId)

/**
 * Min-funksjon for Grunnlag.
 */
@Suppress("REDUNDANT_PROJECTION")
fun <T : Number> min(venstre: Grunnlag<T>, høyre: Grunnlag<out Number>): Uttrykk<Double> = Min(venstre, høyre)
@Suppress("REDUNDANT_PROJECTION")
fun <T : Number> min(venstre: Grunnlag<T>, høyre: Uttrykk<out Number>): Uttrykk<Double> = Min(venstre, høyre)
@Suppress("REDUNDANT_PROJECTION")
fun <T : Number> min(venstre: Uttrykk<out Number>, høyre: Grunnlag<T>): Uttrykk<Double> = Min(venstre, høyre)
fun <T : Number> min(venstre: Grunnlag<T>, høyre: Number): Uttrykk<Double> = Min(venstre, Const(høyre))
fun <T : Number> min(venstre: Number, høyre: Grunnlag<T>): Uttrykk<Double> = Min(Const(venstre), høyre)

/**
 * Boolean operator overloading (norske navn).
 */
infix fun Uttrykk<Boolean>.og(other: Uttrykk<Boolean>): Uttrykk<Boolean> = Og(this, other)
infix fun Uttrykk<Boolean>.eller(other: Uttrykk<Boolean>): Uttrykk<Boolean> = Eller(this, other)
fun ikke(uttrykk: Uttrykk<Boolean>): Uttrykk<Boolean> = Ikke(uttrykk)

/**
 * Sammenlignings-operatorer (norske navn, infix).
 */
infix fun <T : Comparable<T>> Uttrykk<T>.erLik(other: Uttrykk<T>): Uttrykk<Boolean> = Lik(this, other)
infix fun <T : Comparable<T>> Uttrykk<T>.erLik(other: T): Uttrykk<Boolean> = Lik(this, Const(other))
infix fun <T : Comparable<T>> T.erLik(other: Uttrykk<T>): Uttrykk<Boolean> = Lik(Const(this), other)

infix fun <T : Comparable<T>> Uttrykk<T>.erUlik(other: Uttrykk<T>): Uttrykk<Boolean> = Ulik(this, other)
infix fun <T : Comparable<T>> Uttrykk<T>.erUlik(other: T): Uttrykk<Boolean> = Ulik(this, Const(other))
infix fun <T : Comparable<T>> T.erUlik(other: Uttrykk<T>): Uttrykk<Boolean> = Ulik(Const(this), other)

infix fun <T : Comparable<T>> Uttrykk<T>.erStørreEnn(other: Uttrykk<T>): Uttrykk<Boolean> = StørreEnn(this, other)
infix fun <T : Comparable<T>> Uttrykk<T>.erStørreEnn(other: T): Uttrykk<Boolean> = StørreEnn(this, Const(other))
infix fun <T : Comparable<T>> T.erStørreEnn(other: Uttrykk<T>): Uttrykk<Boolean> = StørreEnn(Const(this), other)

infix fun <T : Comparable<T>> Uttrykk<T>.erMindreEnn(other: Uttrykk<T>): Uttrykk<Boolean> = MindreEnn(this, other)
infix fun <T : Comparable<T>> Uttrykk<T>.erMindreEnn(other: T): Uttrykk<Boolean> = MindreEnn(this, Const(other))
infix fun <T : Comparable<T>> T.erMindreEnn(other: Uttrykk<T>): Uttrykk<Boolean> = MindreEnn(Const(this), other)

infix fun <T : Comparable<T>> Uttrykk<T>.erStørreEllerLik(other: Uttrykk<T>): Uttrykk<Boolean> = StørreEllerLik(this, other)
infix fun <T : Comparable<T>> Uttrykk<T>.erStørreEllerLik(other: T): Uttrykk<Boolean> = StørreEllerLik(this, Const(other))
infix fun <T : Comparable<T>> T.erStørreEllerLik(other: Uttrykk<T>): Uttrykk<Boolean> = StørreEllerLik(Const(this), other)

infix fun <T : Comparable<T>> Uttrykk<T>.erMindreEllerLik(other: Uttrykk<T>): Uttrykk<Boolean> = MindreEllerLik(this, other)
infix fun <T : Comparable<T>> Uttrykk<T>.erMindreEllerLik(other: T): Uttrykk<Boolean> = MindreEllerLik(this, Const(other))
infix fun <T : Comparable<T>> T.erMindreEllerLik(other: Uttrykk<T>): Uttrykk<Boolean> = MindreEllerLik(Const(this), other)

/**
 * Liste-sammenlignings-operatorer (norske navn, infix).
 */
infix fun <T : Any> Uttrykk<T>.erBlant(other: Uttrykk<List<T>>): Uttrykk<Boolean> = ErBlant(this, other)
infix fun <T : Any> Uttrykk<T>.erBlant(other: List<T>): Uttrykk<Boolean> = ErBlant(this, Const(other))
infix fun <T : Any> T.erBlant(other: Uttrykk<List<T>>): Uttrykk<Boolean> = ErBlant(Const(this), other)

infix fun <T : Any> Uttrykk<T>.erIkkeBlant(other: Uttrykk<List<T>>): Uttrykk<Boolean> = ErIkkeBlant(this, other)
infix fun <T : Any> Uttrykk<T>.erIkkeBlant(other: List<T>): Uttrykk<Boolean> = ErIkkeBlant(this, Const(other))
infix fun <T : Any> T.erIkkeBlant(other: Uttrykk<List<T>>): Uttrykk<Boolean> = ErIkkeBlant(Const(this), other)

/**
 * Dato-sammenlignings-operatorer (norske navn, infix).
 * Disse operatorene er spesifikke for LocalDate og gir mer naturlig språk enn > og <.
 */
infix fun Uttrykk<java.time.LocalDate>.erEtter(other: Uttrykk<java.time.LocalDate>): Uttrykk<Boolean> =
    StørreEnn(this, other)
infix fun Uttrykk<java.time.LocalDate>.erEtter(other: java.time.LocalDate): Uttrykk<Boolean> =
    StørreEnn(this, Const(other))
infix fun java.time.LocalDate.erEtter(other: Uttrykk<java.time.LocalDate>): Uttrykk<Boolean> =
    StørreEnn(Const(this), other)

infix fun Uttrykk<java.time.LocalDate>.erEtterEllerLik(other: Uttrykk<java.time.LocalDate>): Uttrykk<Boolean> =
    StørreEllerLik(this, other)
infix fun Uttrykk<java.time.LocalDate>.erEtterEllerLik(other: java.time.LocalDate): Uttrykk<Boolean> =
    StørreEllerLik(this, Const(other))
infix fun java.time.LocalDate.erEtterEllerLik(other: Uttrykk<java.time.LocalDate>): Uttrykk<Boolean> =
    StørreEllerLik(Const(this), other)

infix fun Uttrykk<java.time.LocalDate>.erFør(other: Uttrykk<java.time.LocalDate>): Uttrykk<Boolean> =
    MindreEnn(this, other)
infix fun Uttrykk<java.time.LocalDate>.erFør(other: java.time.LocalDate): Uttrykk<Boolean> =
    MindreEnn(this, Const(other))
infix fun java.time.LocalDate.erFør(other: Uttrykk<java.time.LocalDate>): Uttrykk<Boolean> =
    MindreEnn(Const(this), other)

infix fun Uttrykk<java.time.LocalDate>.erFørEllerLik(other: Uttrykk<java.time.LocalDate>): Uttrykk<Boolean> =
    MindreEllerLik(this, other)
infix fun Uttrykk<java.time.LocalDate>.erFørEllerLik(other: java.time.LocalDate): Uttrykk<Boolean> =
    MindreEllerLik(this, Const(other))
infix fun java.time.LocalDate.erFørEllerLik(other: Uttrykk<java.time.LocalDate>): Uttrykk<Boolean> =
    MindreEllerLik(Const(this), other)

/**
 * Hvis DSL-funksjon (funksjonell stil).
 */
fun <T : Any> hvis(
    betingelse: Uttrykk<Boolean>,
    så: () -> Uttrykk<T>,
    ellers: () -> Uttrykk<T>
): Uttrykk<T> = Hvis(betingelse, så(), ellers())

/**
 * Builder class for betingede uttrykk med fluent API.
 *
 * Brukes av `.så {...}.ellers {...}` syntaksen.
 */
data class BetingetBuilder<T : Any>(
    val betingelse: Uttrykk<Boolean>,
    val såUttrykk: Uttrykk<T>
)

/**
 * Extension function for å starte et betinget uttrykk med fluent syntax.
 *
 * Eksempel:
 * ```kotlin
 * val resultat = (alder erStørreEllerLik 67)
 *     .så { Const(300000) }
 *     .ellers { Const(0) }
 * ```
 */
infix fun <T : Any> Uttrykk<Boolean>.så(såBlock: () -> Uttrykk<T>): BetingetBuilder<T> {
    return BetingetBuilder(this, såBlock())
}

/**
 * Extension function for å fullføre et betinget uttrykk med ellers-gren.
 *
 * Må brukes sammen med `.så` for å konstruere et komplett Hvis-uttrykk.
 */
infix fun <T : Any> BetingetBuilder<T>.ellers(ellersBlock: () -> Uttrykk<T>): Uttrykk<T> {
    return Hvis(betingelse, såUttrykk, ellersBlock())
}

internal data class Feil<T : Any>(val melding: String) : Uttrykk<T> {
    override fun evaluer(): T = throw IllegalStateException(melding)
    override fun notasjon(): String = "FEIL($melding)"
    override fun konkret(): String = melding
    override fun grunnlagListe() = emptyList<Grunnlag<out Any>>()
    override fun dybde(): Int = 1
    override fun strukturellHash(): String = "Feil:$melding"
}

fun <T : Any> feilUttrykk(melding: String): Uttrykk<T> = Feil(melding)

/**
 * Memoiserings-node som cacher resultater fra et underliggende uttrykk.
 *
 * Denne noden løser to problemer:
 * 1. **Repetert evaluering**: Samme uttrykk evaluert flere ganger caches kun én gang
 * 2. **Dyre beregninger**: Komplekse uttrykk som gjenbrukes får bedre ytelse
 *
 * ## Når skal memoisering brukes?
 * - Uttrykk som brukes flere steder i samme regeltre
 * - Dyre beregninger som ikke endrer seg
 * - Automatisk i `Grunnlag.navngi()` (valgfritt via parameter)
 *
 * ## Eksempel
 * ```kotlin
 * val dyrtUttrykk = (kompleksBeregning() * annenBeregning()).memoise()
 * val resultat = tabell {
 *     regel { når { dyrtUttrykk } ... }  // Evalueres kun én gang
 *     regel { når { dyrtUttrykk og annen } ... }  // Bruker cachet verdi
 * }
 * ```
 *
 * ## Thread-safety
 * Bruker Kotlin's `lazy` delegate som er thread-safe by default.
 * Cachen er per Memo-instans, så samme Memo-objekt kan gjenbrukes trygt.
 */
internal data class Memo<T : Any>(
    val uttrykk: Uttrykk<T>
) : Uttrykk<T> {

    // Lazy-cached fields - evalueres kun ved første tilgang
    private val cachedEvaluer: Lazy<T> = lazy { uttrykk.evaluer() }
    private val cachedNotasjon: Lazy<String> = lazy { uttrykk.notasjon() }
    private val cachedKoncret: Lazy<String> = lazy { uttrykk.konkret() }
    private val cachedGrunnlagListe: Lazy<List<Grunnlag<out Any>>> = lazy { uttrykk.grunnlagListe() }
    private val cachedDybde: Lazy<Int> = lazy { uttrykk.dybde() }

    override fun evaluer(): T = cachedEvaluer.value

    override fun notasjon(): String = cachedNotasjon.value

    override fun konkret(): String = cachedKoncret.value

    override fun grunnlagListe(): List<Grunnlag<out Any>> = cachedGrunnlagListe.value

    override fun dybde(): Int = cachedDybde.value

    override fun strukturellHash(): String = "Memo:${uttrykk.strukturellHash()}"

    /**
     * Returnerer det underliggende uttrykket.
     * Nyttig for inspeksjon og debugging.
     */
    fun utpakk(): Uttrykk<T> = uttrykk

    override fun toString(): String = "Memo($uttrykk)"
}

/**
 * Extension function for å legge til memoisering på et uttrykk.
 *
 * ## Eksempel
 * ```kotlin
 * val cachetUttrykk = (a + b * c).memoise()
 * ```
 */
fun <T : Any> Uttrykk<T>.memoise(): Uttrykk<T> =
    if (this is Memo) this else Memo(this)