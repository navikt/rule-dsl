package no.nav.system.rule.dsl.demo.forklaring

import no.nav.system.rule.dsl.forklaring.*

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

    booleanEnkeltOperatorer()
    println("\n" + "=".repeat(80) + "\n")

    booleanSammenligninger()
    println("\n" + "=".repeat(80) + "\n")

    booleanKombinerte()
    println("\n" + "=".repeat(80) + "\n")

    booleanHvisUttrykk()
    println("\n" + "=".repeat(80) + "\n")

    booleanAFPKategoriEksempel()
    println("\n" + "=".repeat(80) + "\n")

    booleanSlitertilleggEksempel()
}

/**
 * Enkle Boolean-operatorer: OG, ELLER, IKKE
 */
fun booleanEnkeltOperatorer() {
    println("1. ENKLE BOOLEAN-OPERATORER")
    println("-".repeat(80))

    val harTrygdetid = Grunnlag("harTrygdetid", Const(true))
    val harInntekt = Grunnlag("harInntekt", Const(false))

    // OG
    val beggeKrav = harTrygdetid og harInntekt
    println("OG-operator:")
    println("  Notasjon: ${beggeKrav.notasjon()}")
    println("  Konkret: ${beggeKrav.konkret()}")
    println("  Resultat: ${beggeKrav.evaluer()}")
    println()

    // ELLER
    val etAvKravene = harTrygdetid eller harInntekt
    println("ELLER-operator:")
    println("  Notasjon: ${etAvKravene.notasjon()}")
    println("  Konkret: ${etAvKravene.konkret()}")
    println("  Resultat: ${etAvKravene.evaluer()}")
    println()

    // IKKE
    val ikkeHarInntekt = ikke(harInntekt)
    println("IKKE-operator:")
    println("  Notasjon: ${ikkeHarInntekt.notasjon()}")
    println("  Konkret: ${ikkeHarInntekt.konkret()}")
    println("  Resultat: ${ikkeHarInntekt.evaluer()}")
}

/**
 * Sammenligninger av verdier
 */
fun booleanSammenligninger() {
    println("2. SAMMENLIGNINGER")
    println("-".repeat(80))

    val trygdetid = Grunnlag("trygdetid", Const(35))
    val fullTrygdetid = Grunnlag("fullTrygdetid", Const(40))
    val inntekt = Grunnlag("inntekt", Const(500000))

    // Mindre enn
    val harRedusertTrygdetid = trygdetid erMindreEnn fullTrygdetid
    println("Mindre enn:")
    println("  Notasjon: ${harRedusertTrygdetid.notasjon()}")
    println("  Resultat: ${harRedusertTrygdetid.evaluer()}")
    println()

    // Større eller lik med konstant
    val harHøyInntekt = inntekt erStørreEllerLik 300000
    println("Større eller lik (med konstant):")
    println("  Notasjon: ${harHøyInntekt.notasjon()}")
    println("  Resultat: ${harHøyInntekt.evaluer()}")
    println()

    // Lik
    val harFullTrygdetid = trygdetid erLik fullTrygdetid
    println("Lik:")
    println("  Notasjon: ${harFullTrygdetid.notasjon()}")
    println("  Resultat: ${harFullTrygdetid.evaluer()}")
}

/**
 * Kombinerte Boolean-uttrykk med parenteser
 */
fun booleanKombinerte() {
    println("3. KOMBINERTE BOOLEAN-UTTRYKK")
    println("-".repeat(80))

    val alder = Grunnlag("alder", Const(67))
    val trygdetid = Grunnlag("trygdetid", Const(35))
    val inntekt = Grunnlag("inntekt", Const(200000))

    // Kompleks betingelse med parenteser
    val kvalifiserer = ((alder erStørreEllerLik 67) og (trygdetid erStørreEllerLik 30)) eller
                       (inntekt erMindreEnn 100000)

    println("Kompleks betingelse:")
    println("  Notasjon: ${kvalifiserer.notasjon()}")
    println("  Konkret: ${kvalifiserer.konkret()}")
    println("  Resultat: ${kvalifiserer.evaluer()}")
    println()

    // Navngitt Boolean-uttrykk med ID
    val harRettTilPensjon: Grunnlag<Boolean> = kvalifiserer
        .navngi("harRettTilPensjon")
        .id("PENSJON-KVALIFISERING")

    println("Navngitt uttrykk:")
    println("  Navn: ${harRettTilPensjon.navn}")
    println("  RvsId: ${harRettTilPensjon.rvsId}")
    println("  Notasjon: ${harRettTilPensjon.notasjon()}")
    println("  Resultat: ${harRettTilPensjon.evaluer()}")
}

/**
 * Hvis-uttrykk for betinget verdi
 */
fun booleanHvisUttrykk() {
    println("4. HVIS-UTTRYKK")
    println("-".repeat(80))

    val alder = Grunnlag("alder", Const(65))

    // Enkel Hvis
    val pensjon = hvis(
        betingelse = alder erStørreEllerLik 67,
        så = { Const(300000) },
        ellers = { Const(0) }
    )

    println("Enkel Hvis:")
    println("  Notasjon: ${pensjon.notasjon()}")
    println("  Konkret: ${pensjon.konkret()}")  // Viser kun valgt gren
    println("  Resultat: ${pensjon.evaluer()}")
    println()

    // Nøstet Hvis med navngiving
    val trygdetid = Grunnlag("trygdetid", Const(25))

    val pensjonstype: Grunnlag<String> = hvis(
        betingelse = trygdetid erStørreEllerLik 40,
        så = { Const("Full pensjon") },
        ellers = {
            hvis(
                betingelse = trygdetid erStørreEllerLik 20,
                så = { Const("Redusert pensjon") },
                ellers = { Const("Ingen pensjon") }
            )
        }
    )
        .navngi("pensjonstype")
        .id("PENSJON-TYPE")

    println("Nøstet Hvis med navngiving:")
    println("  Navn: ${pensjonstype.navn}")
    println("  RvsId: ${pensjonstype.rvsId}")
    println("  Notasjon: ${pensjonstype.notasjon()}")
    println("  Konkret: ${pensjonstype.konkret()}")
    println("  Resultat: ${pensjonstype.evaluer()}")
}

/**
 * Realistisk eksempel: AFP kategori klassifisering
 */
fun booleanAFPKategoriEksempel() {
    println("5. AFP KATEGORI KLASSIFISERING")
    println("-".repeat(80))

    // Input data
    val inntektsavvik = Grunnlag("inntektsavvik", Const(1500))
    val avviksSats = Grunnlag("avviksSats", Const(1000))
    val IIAP = Grunnlag("IIAP", Const(50000))
    val FPI = Grunnlag("FPI", Const(48000))
    val UtbetaltAFP = Grunnlag("UtbetaltAFP", Const(5000))

    // Definer betingelser som navngitte Boolean-uttrykk
    val avvikUnderSats = ((inntektsavvik erStørreEllerLik 0) og (inntektsavvik erMindreEllerLik avviksSats))
        .navngi("avvikUnderSats")
        .id("AVVIK-UNDER-SATS")

    val positivtAvvik = ((inntektsavvik erStørreEnn avviksSats) og
                        (IIAP erStørreEnn FPI) og
                        (UtbetaltAFP erStørreEnn 0))
        .navngi("positivtAvvik")
        .id("POSITIVT-AVVIK")

    val negativtAvvik = ((inntektsavvik erStørreEnn avviksSats) og (FPI erStørreEnn IIAP))
        .navngi("negativtAvvik")
        .id("NEGATIVT-AVVIK")

    // Bestem kategori med nøstet Hvis
    val kategori: Grunnlag<String> = hvis(
        betingelse = avvikUnderSats,
        så = { Const("AVVIK_UNDER_SATS") },
        ellers = {
            hvis(
                betingelse = positivtAvvik,
                så = { Const("POSITIVT_AVVIK") },
                ellers = {
                    hvis(
                        betingelse = negativtAvvik,
                        så = { Const("NEGATIVT_AVVIK") },
                        ellers = { Const("ANDRE_AVVIK") }
                    )
                }
            )
        }
    )
        .navngi("kategori")
        .id("AFP-KATEGORI")

    println("Betingelser:")
    println("  avvikUnderSats: ${avvikUnderSats.evaluer()}")
    println("  positivtAvvik: ${positivtAvvik.evaluer()}")
    println("  negativtAvvik: ${negativtAvvik.evaluer()}")
    println()

    println("Kategori:")
    println("  Navn: ${kategori.navn}")
    println("  RvsId: ${kategori.rvsId}")
    println("  Resultat: ${kategori.evaluer()}")
    println()

    println("Detaljert forklaring:")
    println(kategori.forklarDetaljert("kategori", maxDybde = 2, inkluderRvsId = true))
}

/**
 * Kombinert Boolean og Number: Slitertillegg med betinget beregning
 */
fun booleanSlitertilleggEksempel() {
    println("6. SLITERTILLEGG MED BETINGET BEREGNING")
    println("-".repeat(80))

    val G = Grunnlag("G", Const(110000))
    val faktiskTrygdetid = Grunnlag("faktiskTrygdetid", Const(35))
    val fullTrygdetid = Grunnlag("FULL_TRYGDETID", Const(40))
    val antallMåneder = Grunnlag("antallMånederEtterNedreAldersgrense", Const(24))

    // Beregn fullt slitertillegg
    val fulltSlitertillegg = (0.25 * G / 12)
        .navngi("fulltSlitertillegg")
        .id("SLITERTILLEGG-FULLT")

    // Trygdetid faktor
    val trygdetidFaktor = (faktiskTrygdetid / fullTrygdetid)
        .navngi("trygdetidFaktor")
        .id("SLITERTILLEGG-TRYGDETID-FAKTOR")

    // Justering for uttakstidspunkt
    val MND_36 = Grunnlag("MND_36", Const(36))
    val justeringsFaktorUttak = ((MND_36 - min(antallMåneder, MND_36)) / MND_36)
        .navngi("justeringsFaktorUttak")
        .id("SLITERTILLEGG-JUSTERING-UTTAK")

    // Endelig slitertillegg med betinget beregning
    val slitertillegg: Grunnlag<Double> = hvis(
        betingelse = (faktiskTrygdetid erMindreEnn fullTrygdetid) og (antallMåneder erStørreEnn 0) ,
        så = { fulltSlitertillegg * justeringsFaktorUttak * trygdetidFaktor },
        ellers = {
            hvis(
                betingelse = (faktiskTrygdetid erMindreEnn  fullTrygdetid) og (antallMåneder erLik 0),
                så = { fulltSlitertillegg * trygdetidFaktor },
                ellers = {
                    hvis(
                        betingelse = (faktiskTrygdetid erLik fullTrygdetid) og (antallMåneder erStørreEnn 0),
                        så = { fulltSlitertillegg * justeringsFaktorUttak },
                        ellers = { fulltSlitertillegg }
                    )
                }
            )
        }
    )
        .navngi("slitertillegg")
        .id("SLITERTILLEGG-BEREGNET")

    // Samme beregning med ny fluent syntax (.så .ellers)
    val slitertillegg2: Grunnlag<Double> =
        ((faktiskTrygdetid erMindreEnn fullTrygdetid)
                    og (antallMåneder erStørreEnn 0))
            .så { fulltSlitertillegg * justeringsFaktorUttak * trygdetidFaktor }
            .ellers {
                ((faktiskTrygdetid erMindreEnn  fullTrygdetid)
                            og (antallMåneder erLik 0))
                    .så { fulltSlitertillegg * trygdetidFaktor }
                    .ellers {
                        ((faktiskTrygdetid erLik fullTrygdetid)
                                og (antallMåneder erStørreEnn 0))
                            .så { fulltSlitertillegg * justeringsFaktorUttak }
                            .ellers { fulltSlitertillegg }
                    }
            }
            .navngi("slitertillegg2")
            .id("SLITERTILLEGG-BEREGNET-2")


    println("Input:")
    println("  G: ${G.evaluer()}")
    println("  Faktisk trygdetid: ${faktiskTrygdetid.evaluer()}")
    println("  Full trygdetid: ${fullTrygdetid.evaluer()}")
    println("  Måneder etter nedre aldersgrense: ${antallMåneder.evaluer()}")
    println()

    println("Mellomregninger:")
    println("  Fullt slitertillegg: ${fulltSlitertillegg.evaluer()}")
    println("  Trygdetid faktor: ${trygdetidFaktor.evaluer()}")
    println("  Justering uttak: ${justeringsFaktorUttak.evaluer()}")
    println()

    println("Resultat:")
    println("  Slitertillegg (hvis-syntax): ${slitertillegg.evaluer()}")
    println("  Slitertillegg2 (fluent-syntax): ${slitertillegg2.evaluer()}")
    println("  Begge gir samme resultat: ${slitertillegg.evaluer() == slitertillegg2.evaluer()}")
    println()

    println("Detaljert forklaring med RvsId (hvis-syntax):")
    println(slitertillegg.forklarDetaljert("slitertillegg", maxDybde = 3, inkluderRvsId = true))
    println()
    println("Detaljert forklaring med RvsId (fluent-syntax):")
    println(slitertillegg2.forklarDetaljert("slitertillegg2", maxDybde = 3, inkluderRvsId = true))
}
