package no.nav.system.rule.dsl.forklaring

import no.nav.system.rule.dsl.rettsregel.Faktum
import java.io.Serializable

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
sealed interface Uttrykk<out T : Number> : Serializable {
    /**
     * Evaluerer uttrykket til en numerisk verdi.
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
     * Returnerer liste av alle faktum brukt i uttrykket.
     */
    fun faktumListe(): List<Faktum<out Number>>

    /**
     * Returnerer dybde av uttrykkstre.
     */
    fun dybde(): Int
}

/**
 * Variabel - referanse til et Faktum.
 */
data class Var<T : Number>(
    val faktum: Faktum<T>
) : Uttrykk<T> {
    override fun evaluer(): T = faktum.value

    override fun notasjon(): String = faktum.name

    override fun konkret(): String = faktum.value.toString()

    override fun faktumListe(): List<Faktum<out Number>> = listOf(faktum)

    override fun dybde(): Int = 1

    override fun toString(): String = notasjon()
}

/**
 * Konstant verdi.
 */
data class Const<T : Number>(
    val verdi: T
) : Uttrykk<T> {
    override fun evaluer(): T = verdi

    override fun notasjon(): String = verdi.toString()

    override fun konkret(): String = verdi.toString()

    override fun faktumListe(): List<Faktum<out Number>> = emptyList()

    override fun dybde(): Int = 1

    override fun toString(): String = verdi.toString()
}

/**
 * Addisjon.
 */
data class Add<T : Number>(
    val venstre: Uttrykk<out Number>,
    val høyre: Uttrykk<out Number>
) : Uttrykk<T> {
    @Suppress("UNCHECKED_CAST")
    override fun evaluer(): T {
        val v = venstre.evaluer().toDouble()
        val h = høyre.evaluer().toDouble()
        val resultat = v + h

        // Returner riktig type basert på input
        return if (venstre.evaluer() is Int && høyre.evaluer() is Int) {
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

    override fun faktumListe(): List<Faktum<out Number>> =
        venstre.faktumListe() + høyre.faktumListe()

    override fun dybde(): Int = 1 + maxOf(venstre.dybde(), høyre.dybde())
}

/**
 * Subtraksjon.
 */
data class Sub<T : Number>(
    val venstre: Uttrykk<out Number>,
    val høyre: Uttrykk<out Number>
) : Uttrykk<T> {
    @Suppress("UNCHECKED_CAST")
    override fun evaluer(): T {
        val v = venstre.evaluer().toDouble()
        val h = høyre.evaluer().toDouble()
        val resultat = v - h

        return if (venstre.evaluer() is Int && høyre.evaluer() is Int) {
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

    override fun faktumListe(): List<Faktum<out Number>> =
        venstre.faktumListe() + høyre.faktumListe()

    override fun dybde(): Int = 1 + maxOf(venstre.dybde(), høyre.dybde())
}

/**
 * Multiplikasjon.
 */
data class Mul<T : Number>(
    val venstre: Uttrykk<out Number>,
    val høyre: Uttrykk<out Number>
) : Uttrykk<T> {
    @Suppress("UNCHECKED_CAST")
    override fun evaluer(): T {
        val v = venstre.evaluer().toDouble()
        val h = høyre.evaluer().toDouble()
        val resultat = v * h

        return if (venstre.evaluer() is Int && høyre.evaluer() is Int) {
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

    override fun faktumListe(): List<Faktum<out Number>> =
        venstre.faktumListe() + høyre.faktumListe()

    override fun dybde(): Int = 1 + maxOf(venstre.dybde(), høyre.dybde())
}

/**
 * Divisjon (gir alltid Double).
 */
data class Div(
    val venstre: Uttrykk<out Number>,
    val høyre: Uttrykk<out Number>
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

    override fun faktumListe(): List<Faktum<out Number>> =
        venstre.faktumListe() + høyre.faktumListe()

    override fun dybde(): Int = 1 + maxOf(venstre.dybde(), høyre.dybde())
}

/**
 * Negasjon (unær minus).
 */
data class Neg<T : Number>(
    val uttrykk: Uttrykk<out Number>
) : Uttrykk<T> {
    @Suppress("UNCHECKED_CAST")
    override fun evaluer(): T {
        val v = uttrykk.evaluer().toDouble()
        val resultat = -v

        return if (uttrykk.evaluer() is Int) {
            resultat.toInt() as T
        } else {
            resultat as T
        }
    }

    override fun notasjon(): String = "-${uttrykk.notasjon().medParentesVedBehov(uttrykk)}"

    override fun konkret(): String = "-${uttrykk.konkret().medParentesVedBehov(uttrykk)}"

    override fun faktumListe(): List<Faktum<out Number>> = uttrykk.faktumListe()

    override fun dybde(): Int = 1 + uttrykk.dybde()
}

/**
 * Navngitt uttrykk - gir et navn til et kompleks uttrykk.
 * Tilsvarer "locked" formler i dagens Formel-implementasjon.
 */
data class Navngitt<T : Number>(
    val navn: String,
    val uttrykk: Uttrykk<T>
) : Uttrykk<T> {
    override fun evaluer(): T = uttrykk.evaluer()

    override fun notasjon(): String = navn

    override fun konkret(): String = evaluer().toString()

    override fun faktumListe(): List<Faktum<out Number>> = uttrykk.faktumListe()

    override fun dybde(): Int = 1  // Navngitte uttrykk teller som atomisk

    /**
     * Returnerer det underliggende uttrykket.
     */
    fun utpakk(): Uttrykk<T> = uttrykk
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
 * Operator overloading for naturlig syntaks.
 */
operator fun <T : Number> Uttrykk<T>.plus(other: Uttrykk<out Number>): Add<T> = Add(this, other)
operator fun <T : Number> Uttrykk<T>.plus(other: Number): Add<T> = Add(this, Const(other))
operator fun <T : Number> Number.plus(other: Uttrykk<T>): Add<T> = Add(Const(this), other)

operator fun <T : Number> Uttrykk<T>.minus(other: Uttrykk<out Number>): Sub<T> = Sub(this, other)
operator fun <T : Number> Uttrykk<T>.minus(other: Number): Sub<T> = Sub(this, Const(other))
operator fun <T : Number> Number.minus(other: Uttrykk<T>): Sub<T> = Sub(Const(this), other)

operator fun <T : Number> Uttrykk<T>.times(other: Uttrykk<out Number>): Mul<T> = Mul(this, other)
operator fun <T : Number> Uttrykk<T>.times(other: Number): Mul<T> = Mul(this, Const(other))
operator fun <T : Number> Number.times(other: Uttrykk<T>): Mul<T> = Mul(Const(this), other)

operator fun Uttrykk<out Number>.div(other: Uttrykk<out Number>): Div = Div(this, other)
operator fun Uttrykk<out Number>.div(other: Number): Div = Div(this, Const(other))
operator fun Number.div(other: Uttrykk<out Number>): Div = Div(Const(this), other)

operator fun <T : Number> Uttrykk<T>.unaryMinus(): Neg<T> = Neg(this)

/**
 * Builder function for navngitte uttrykk.
 */
fun <T : Number> Uttrykk<T>.navngi(navn: String): Navngitt<T> = Navngitt(navn, this)

/**
 * Operator overloading for Faktum - tillater direkte bruk uten Var().
 *
 * Dette gjør det mulig å skrive:
 * ```kotlin
 * val G = Faktum("G", 110000)
 * val sats = Faktum("sats", 0.25)
 * val uttrykk = sats * G / 12  // Direkte uten Var()
 * ```
 *
 * Istedenfor:
 * ```kotlin
 * val uttrykk = Var(sats) * Var(G) / Const(12)
 * ```
 */

// Addisjon
operator fun <T : Number> Faktum<T>.plus(other: Uttrykk<out Number>): Add<T> = Add(Var(this), other)
operator fun <T : Number> Faktum<T>.plus(other: Faktum<out Number>): Add<T> = Add(Var(this), Var(other))
operator fun <T : Number> Faktum<T>.plus(other: Number): Add<T> = Add(Var(this), Const(other))
operator fun <T : Number> Uttrykk<T>.plus(other: Faktum<out Number>): Add<T> = Add(this, Var(other))
operator fun <T : Number> Number.plus(other: Faktum<T>): Add<T> = Add(Const(this), Var(other))

// Subtraksjon
operator fun <T : Number> Faktum<T>.minus(other: Uttrykk<out Number>): Sub<T> = Sub(Var(this), other)
operator fun <T : Number> Faktum<T>.minus(other: Faktum<out Number>): Sub<T> = Sub(Var(this), Var(other))
operator fun <T : Number> Faktum<T>.minus(other: Number): Sub<T> = Sub(Var(this), Const(other))
operator fun <T : Number> Uttrykk<T>.minus(other: Faktum<out Number>): Sub<T> = Sub(this, Var(other))
operator fun <T : Number> Number.minus(other: Faktum<T>): Sub<T> = Sub(Const(this), Var(other))

// Multiplikasjon
operator fun <T : Number> Faktum<T>.times(other: Uttrykk<out Number>): Mul<T> = Mul(Var(this), other)
operator fun <T : Number> Faktum<T>.times(other: Faktum<out Number>): Mul<T> = Mul(Var(this), Var(other))
operator fun <T : Number> Faktum<T>.times(other: Number): Mul<T> = Mul(Var(this), Const(other))
operator fun <T : Number> Uttrykk<T>.times(other: Faktum<out Number>): Mul<T> = Mul(this, Var(other))
operator fun <T : Number> Number.times(other: Faktum<T>): Mul<T> = Mul(Const(this), Var(other))

// Divisjon
operator fun Faktum<out Number>.div(other: Uttrykk<out Number>): Div = Div(Var(this), other)
operator fun Faktum<out Number>.div(other: Faktum<out Number>): Div = Div(Var(this), Var(other))
operator fun Faktum<out Number>.div(other: Number): Div = Div(Var(this), Const(other))
operator fun Uttrykk<out Number>.div(other: Faktum<out Number>): Div = Div(this, Var(other))
operator fun Number.div(other: Faktum<out Number>): Div = Div(Const(this), Var(other))

/**
 * Konverterer Faktum til Var-uttrykk for eksplisitt bruk når nødvendig.
 */
fun <T : Number> Faktum<T>.tilUttrykk(): Var<T> = Var(this)
