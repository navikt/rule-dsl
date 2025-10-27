package no.nav.system.rule.dsl.demo.forklaring

import no.nav.system.rule.dsl.forklaring.*

/**
 * Eksempler på bruk av Tabell (beslutningstabell) i Uttrykk-systemet.
 *
 * En beslutningstabell gjør kompleks if-else-logikk mer lesbar, testbar og vedlikeholdbar
 * ved å samle alle betingelser og utfall i én eksplisitt struktur.
 */
fun main() {
    println("=" .repeat(80))
    println("TABELL DEMO - Beslutningstabell eksempler")
    println("=" .repeat(80))

    simpleDecisionTable()
    println("\n" + "-".repeat(80) + "\n")

    ageBasedDecisionTable()
    println("\n" + "-".repeat(80) + "\n")

    multiConditionDecisionTable()
    println("\n" + "-".repeat(80) + "\n")

    decisionTableWithElse()
    println("\n" + "-".repeat(80) + "\n")

    decisionTableNotationVsKonkret()
    println("\n" + "-".repeat(80) + "\n")

    complexRealWorldExample()
}

/**
 * Eksempel 1: Enkel beslutningstabell
 */
fun simpleDecisionTable() {
    println("EKSEMPEL 1: Enkel beslutningstabell")
    println()

    val harInntekt = Grunnlag("harInntekt", Const(true))
    val erOverMinsteInntekt = Grunnlag("erOverMinsteInntekt", Const(false))

    val resultat = tabell<String>("inntektVurdering") {
        regel {
            når { ikke(harInntekt) }
            resultat { Const("AVSLÅTT - Ingen inntekt") }
        }
        regel {
            når { harInntekt og ikke(erOverMinsteInntekt) }
            resultat { Const("AVSLÅTT - Under minsteinntekt") }
        }
        regel {
            når { harInntekt og erOverMinsteInntekt }
            resultat { Const("INNVILGET") }
        }
    }.navngi("inntektResultat")

    println("Notasjon (alle regler):")
    println(resultat.notasjon())
    println()
    println("Konkret (kun matchende regel):")
    println(resultat.konkret())
    println()
    println("Resultat: ${resultat.evaluer()}")
}

/**
 * Eksempel 2: Aldersbasert beslutningstabell
 */
fun ageBasedDecisionTable() {
    println("EKSEMPEL 2: Aldersbasert beslutningstabell")
    println()

    val alder = Grunnlag("alder", Const(65))

    val kategori = tabell<String>("alderKategori") {
        regel {
            når { alder erMindreEnn 18 }
            resultat { Const("BARN") }
        }
        regel {
            når { alder erMindreEnn 67 }
            resultat { Const("VOKSEN") }
        }
        regel {
            når { alder erStørreEllerLik 67 }
            resultat { Const("PENSJONIST") }
        }
    }

    println("Alder: ${alder.evaluer()}")
    println("Kategori: ${kategori.evaluer()}")
    println()
    println("Tabell konkret:")
    println(kategori.konkret())
}

/**
 * Eksempel 3: Tabell med flere betingelser per regel
 */
fun multiConditionDecisionTable() {
    println("EKSEMPEL 3: Tabell med flere betingelser")
    println()

    val harBodd5År = Grunnlag("harBodd5År", Const(true))
    val harJobbet3År = Grunnlag("harJobbet3År", Const(true))
    val erFlyktning = Grunnlag("erFlyktning", Const(false))

    val vurdering = tabell<String>("medlemskapVurdering") {
        regel {
            når { erFlyktning }
            resultat { Const("INNVILGET - Flyktning") }
        }
        regel {
            når { harBodd5År og harJobbet3År }
            resultat { Const("INNVILGET - Oppfyller krav") }
        }
        regel {
            når { harBodd5År og ikke(harJobbet3År) }
            resultat { Const("AVSLÅTT - Mangler arbeidstid") }
        }
        regel {
            når { ikke(harBodd5År) og harJobbet3År }
            resultat { Const("AVSLÅTT - Mangler botid") }
        }
        ellers { Const("AVSLÅTT - Oppfyller ikke krav") }
    }

    println("Notasjon:")
    println(vurdering.notasjon())
    println()
    println("Resultat: ${vurdering.evaluer()}")
}

/**
 * Eksempel 4: Ellers-klausul som fallback
 */
fun decisionTableWithElse() {
    println("EKSEMPEL 4: Tabell med ellers-klausul")
    println()

    val kode = Grunnlag("kode", Const("X"))

    val beskrivelse = tabell<String> {
        regel {
            når { kode erLik "A" }
            resultat { Const("Alderspensjon") }
        }
        regel {
            når { kode erLik "U" }
            resultat { Const("Uføretrygd") }
        }
        regel {
            når { kode erLik "G" }
            resultat { Const("Gjenlevendepensjon") }
        }
        ellers { Const("Ukjent kode") }
    }

    println("Kode: ${kode.evaluer()}")
    println("Beskrivelse: ${beskrivelse.evaluer()}")
    println()
    println("Konkret:")
    println(beskrivelse.konkret())
}

/**
 * Eksempel 5: Notasjon vs Konkret
 */
fun decisionTableNotationVsKonkret() {
    println("EKSEMPEL 5: Forskjell på notasjon() og konkret()")
    println()

    val poeng = Grunnlag("poeng", Const(75))

    val karakter = tabell<String>("karakterBeregning") {
        regel {
            når { poeng erStørreEllerLik 90 }
            resultat { Const("A") }
        }
        regel {
            når { poeng erStørreEllerLik 80 }
            resultat { Const("B") }
        }
        regel {
            når { poeng erStørreEllerLik 70 }
            resultat { Const("C") }
        }
        regel {
            når { poeng erStørreEllerLik 60 }
            resultat { Const("D") }
        }
        ellers { Const("F") }
    }

    println("=== NOTASJON (alle regler) ===")
    println(karakter.notasjon())
    println()
    println("=== KONKRET (kun matchende regel) ===")
    println(karakter.konkret())
    println()
    println("Resultat: ${karakter.evaluer()}")
}

/**
 * Eksempel 6: Komplekst virkelighetseksempel
 *
 * Dette eksemplet viser hvordan en beslutningstabell kan erstatte
 * dyp nøsting av HVIS-ELLERS-uttrykk med en flat, lesbar struktur.
 */
fun complexRealWorldExample() {
    println("EKSEMPEL 6: Komplekst virkelighetseksempel - Lånevurdering")
    println()

    // Grunnlagsdata
    val harFastInntekt = Grunnlag("harFastInntekt", Const(true))
    val inntekt = Grunnlag("inntekt", Const(450000))
    val harGjeld = Grunnlag("harGjeld", Const(true))
    val gjeldsgrad = Grunnlag("gjeldsgrad", Const(3.2))
    val harBetalingsanmerkning = Grunnlag("harBetalingsanmerkning", Const(false))

    // Beslutningstabell
    val låneVurdering = tabell<String>("låneVurdering") {
        regel {
            når { harBetalingsanmerkning }
            resultat { Const("AVSLÅTT - Betalingsanmerkning") }
        }
        regel {
            når { ikke(harFastInntekt) }
            resultat { Const("AVSLÅTT - Mangler fast inntekt") }
        }
        regel {
            når { inntekt erMindreEnn 300000 }
            resultat { Const("AVSLÅTT - For lav inntekt") }
        }
        regel {
            når { harGjeld og (gjeldsgrad erStørreEnn 5.0) }
            resultat { Const("AVSLÅTT - For høy gjeldsgrad") }
        }
        regel {
            når { inntekt erStørreEllerLik 500000 }
            resultat { Const("INNVILGET - Høy inntekt") }
        }
        regel {
            når {
                (inntekt erStørreEllerLik 300000) og
                        (ikke(harGjeld) eller (gjeldsgrad erMindreEllerLik 5.0))
            }
            resultat { Const("INNVILGET - Godkjent") }
        }
        ellers { Const("TIL MANUELL VURDERING") }
    }.navngi("lånebeslutning")
        .id("LåneBeslutning")

    println("Notasjon (alle regler):")
    println(låneVurdering.notasjon())
    println()
    println("Konkret (evaluert med verdier):")
    println(låneVurdering.konkret())
    println()
    println("==> Beslutning: ${låneVurdering.evaluer()}")
    println()

    // Vis grunnlagsdata
    println("Grunnlagsdata brukt:")
    låneVurdering.grunnlagListe().distinctBy { it.navn }.forEach { grunnlag ->
        println("  - ${grunnlag.navn} = ${grunnlag.evaluer()}")
    }
}
