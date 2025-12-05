package no.nav.system.rule.dsl.demo.forklaring

import no.nav.system.ruledsl.core.rettsregel.Const
import no.nav.system.ruledsl.core.rettsregel.Faktum
import no.nav.system.ruledsl.core.operators.*

/**
 * Demonstrasjon av Boolean-uttrykk, sammenligninger og Hvis-logikk.
 *
 * Viser hvordan det rekursive Uttrykk-systemet støtter både matematiske
 * og logiske uttrykk med full sporbarhet.
 */

fun main() {
    println("=".repeat(80))
    println("BOOLEAN-UTTRYKK DEMO")
    println("=".repeat(80))
    println()

    booleanSammenligninger()
    println("\n" + "=".repeat(80) + "\n")

}

/**
 * Sammenligninger av verdier
 */
fun booleanSammenligninger() {
    println("2. SAMMENLIGNINGER")
    println("-".repeat(80))

    val trygdetid = Faktum("trygdetid", Const(35))
    val fullTrygdetid = Faktum("fullTrygdetid", Const(40))
    val inntekt = Faktum("inntekt", Const(500000))

    // Mindre enn
    val harRedusertTrygdetid = trygdetid erMindreEnn fullTrygdetid
    println("Mindre enn:")
    println("  Notasjon: ${harRedusertTrygdetid.notasjon()}")
    println("  Resultat: ${harRedusertTrygdetid.verdi}")
    println()

    // Større eller lik med konstant
    val harHøyInntekt = inntekt erStørreEllerLik 300000
    println("Større eller lik (med konstant):")
    println("  Notasjon: ${harHøyInntekt.notasjon()}")
    println("  Resultat: ${harHøyInntekt.verdi}")
    println()

    // Lik
    val harFullTrygdetid = trygdetid erLik fullTrygdetid
    println("Lik:")
    println("  Notasjon: ${harFullTrygdetid.notasjon()}")
    println("  Resultat: ${harFullTrygdetid.verdi}")
}

