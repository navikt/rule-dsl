package no.nav.system.rule.dsl.forklaring

import java.io.Serializable

/**
 * Abstrakt baseklasse for binære operatorer med to operander.
 *
 * Abstraherer felles logikk for alle binære operatorer (Add, Sub, Mul, Og, Lik, etc.).
 * Subklasser trenger kun å implementere:
 * - `evaluer()` - den unike operasjonslogikken
 * - `operatorNavn()` - navnet brukt i strukturellHash
 * - `operatorSymbol()` - symbolet brukt i notasjon ("+", "-", "OG", etc.)
 * - (valgfritt) `høyreSideParentes()` - om høyre side trenger parentes
 *
 * Note: Subklasser må definere venstre og høyre som egne properties for data class semantics.
 */
internal abstract class BinærOperator<L : Any, R : Any, T : Any> : Uttrykk<T> {

    // Subklasser må implementere disse som accessor methods
    // Dette sikrer at data class properties får riktige JVM getters
    internal abstract fun venstre(): Uttrykk<L>
    internal abstract fun høyre(): Uttrykk<R>

    // Felles implementasjon - identisk for alle binære operatorer
    override fun grunnlagListe(): List<Grunnlag<out Any>> =
        venstre().grunnlagListe() + høyre().grunnlagListe()

    override fun dybde(): Int = 1 + maxOf(venstre().dybde(), høyre().dybde())

    override fun strukturellHash(): String =
        "${operatorNavn()}:${venstre().strukturellHash()}:${høyre().strukturellHash()}"

    override fun notasjon(): String {
        val v = venstre().notasjon().medParentesVedBehov(venstre())
        val h = høyre().notasjon().medParentesVedBehov(høyre(), høyreSideParentes())
        return "$v ${operatorSymbol()} $h"
    }

    override fun konkret(): String {
        val v = venstre().konkret().medParentesVedBehov(venstre())
        val h = høyre().konkret().medParentesVedBehov(høyre(), høyreSideParentes())
        return "$v ${operatorSymbol()} $h"
    }

    // Template methods - må implementeres av subklasser
    internal abstract fun operatorNavn(): String
    internal abstract fun operatorSymbol(): String
    internal open fun høyreSideParentes(): Boolean = false

    // Må implementeres av subklasser - unik operasjonslogikk
    abstract override fun evaluer(): T
}

/**
 * Abstrakt baseklasse for unære operatorer med én operand.
 *
 * Abstraherer felles logikk for alle unære operatorer (Neg, Ikke).
 * Subklasser trenger kun å implementere:
 * - `evaluer()` - den unike operasjonslogikken
 * - `operatorNavn()` - navnet brukt i strukturellHash
 * - `operatorSymbol()` - symbolet brukt i notasjon ("-", "IKKE", etc.)
 *
 * Note: Subklasser må definere uttrykk som egen property for data class semantics.
 */
internal abstract class UnærOperator<I : Any, T : Any> : Uttrykk<T> {

    // Subklasser må implementere denne som accessor method
    // Dette sikrer at data class properties får riktige JVM getters
    internal abstract fun uttrykk(): Uttrykk<I>

    // Felles implementasjon - identisk for alle unære operatorer
    override fun grunnlagListe(): List<Grunnlag<out Any>> = uttrykk().grunnlagListe()

    override fun dybde(): Int = 1 + uttrykk().dybde()

    override fun strukturellHash(): String =
        "${operatorNavn()}:${uttrykk().strukturellHash()}"

    override fun notasjon(): String {
        val v = uttrykk().notasjon().medParentesVedBehov(uttrykk())
        return "${operatorSymbol()} $v"
    }

    override fun konkret(): String {
        val v = uttrykk().konkret().medParentesVedBehov(uttrykk())
        return "${operatorSymbol()} $v"
    }

    // Template methods - må implementeres av subklasser
    internal abstract fun operatorNavn(): String
    internal abstract fun operatorSymbol(): String

    // Må implementeres av subklasser - unik operasjonslogikk
    abstract override fun evaluer(): T
}

/**
 * Abstrakt baseklasse for sammenligningsoperatorer (Lik, Ulik, StørreEnn, etc.).
 *
 * Sammenligningsoperatorer trenger ikke parentesbehandling i notasjon.
 */
internal abstract class SammenligningOperator<T : Comparable<T>> : BinærOperator<T, T, Boolean>() {

    // Override notasjon og konkret - ingen parentesbehandling
    override fun notasjon(): String =
        "${venstre().notasjon()} ${operatorSymbol()} ${høyre().notasjon()}"

    override fun konkret(): String =
        "${venstre().konkret()} ${operatorSymbol()} ${høyre().konkret()}"
}
