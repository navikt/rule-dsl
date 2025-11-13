package no.nav.system.rule.dsl.forklaring

/**
 * Forklaringsfunksjoner for rekursive Uttrykk.
 *
 * Den rekursive strukturen gjør det mye enklere å generere forklaringer
 * fordi vi kan traverse treet på en naturlig måte.
 *
 * ## Eksempel
 * ```kotlin
 * val uttrykk = Div(Mul(Var(sats), Var(G)), Var(måneder))
 * val forklaring = uttrykk.forklar("beregning")
 *
 * println(forklaring.toText())
 * // Output:
 * // beregning = sats * G / måneder
 * // beregning = 0.25 * 110000 / 12
 * // beregning = 2291.67
 * //
 * // HVORDAN
 * //     sats = 0.25
 * //     G = 110000
 * //     måneder = 12
 * ```
 */

/**
 * Genererer komplett forklaring av et uttrykk.
 *
 * @param navn navnet på resultatet
 * @param maxDybde maksimal dybde for subformel-ekspansjon
 */
fun <T : Any> Uttrykk<T>.forklar(navn: String, maxDybde: Int = 3): HvordanForklaring {
    // Hvis dette er et Grunnlag uttrykk, bruk det underliggende for notasjon og konkret
    val uttrykkForVisning = if (this is Grunnlag) this.utpakk() else this

    val hva = HvaForklaring(
        navn = navn,
        symbolskUttrykk = uttrykkForVisning.notasjon(),
        konkretUttrykk = uttrykkForVisning.konkret(),
        resultat = this.evaluer()
    )

    // For navngitte underuttrykk, lag subforklaringer
    // Dedupliser basert på navn for å unngå gjentakelser
    val subforklaringer = uttrykkForVisning.finnNavngitteUttrykk(0, maxDybde)
        .distinctBy { it.hvaForklaring.navn }

    return HvordanForklaring(
        hvaForklaring = hva,
        subformler = subforklaringer,
        hvorfor = null
    )
}

/**
 * Genererer HvaForklaring for uttrykket.
 */
fun <T : Any> Uttrykk<T>.forklarHva(navn: String): HvaForklaring {
    return HvaForklaring(
        navn = navn,
        symbolskUttrykk = this.notasjon(),
        konkretUttrykk = this.konkret(),
        resultat = this.evaluer()
    )
}

/**
 * Sjekker om et uttrykk er en triviell konstant som ikke skal vises i forklaring.
 *
 * En triviell konstant er:
 * - En Const direkte
 * - Et Grunnlag som DIREKTE inneholder Const (ikke rekursivt)
 *
 * Dette betyr at Grunnlag som peker til andre Grunnlag (selv om de til slutt
 * ender i Const) fortsatt skal vises fordi de kan være viktige mellomsteg.
 */
private fun Uttrykk<*>.erTrivielKonstant(): Boolean {
    return when (this) {
        is Const -> true
        is Grunnlag -> this.utpakk() is Const  // Kun ett nivå, ikke rekursivt
        else -> false
    }
}

/**
 * Traverser uttrykkstre og finn alle navngitte underuttrykk.
 * Dette tilsvarer "locked" subformler i dagens Formel-implementasjon.
 */
private fun <T : Any> Uttrykk<T>.finnNavngitteUttrykk(
    nivå: Int,
    maxDybde: Int
): List<HvordanForklaring> {
    if (nivå >= maxDybde) return emptyList()

    return when (this) {
        is Grunnlag -> {
            // Hvis Grunnlag bare inneholder konstanter (rekursivt), hopp over
            if (this.erTrivielKonstant()) {
                return emptyList()
            }

            // Dette er en navngitt subformel - lag forklaring for den
            val hva = this.utpakk().forklarHva(this.navn)

            // Fortsett å søke etter flere navngitte uttrykk i de underliggende operatorene
            val subSub = this.utpakk().finnNavngitteUttrykk(nivå + 1, maxDybde)

            listOf(
                HvordanForklaring(
                    hvaForklaring = hva,
                    subformler = subSub,
                    hvorfor = null,
                    nivå = nivå
                )
            ) + when (val unpacked = this.utpakk()) {
                // Søk også gjennom operatorene for å finne andre navngitte uttrykk på samme nivå
                is Add -> unpacked.venstre.finnNavngitteUttrykk(nivå, maxDybde) +
                        unpacked.høyre.finnNavngitteUttrykk(nivå, maxDybde)
                is Sub -> unpacked.venstre.finnNavngitteUttrykk(nivå, maxDybde) +
                        unpacked.høyre.finnNavngitteUttrykk(nivå, maxDybde)
                is Mul -> unpacked.venstre.finnNavngitteUttrykk(nivå, maxDybde) +
                        unpacked.høyre.finnNavngitteUttrykk(nivå, maxDybde)
                is Div -> unpacked.venstre.finnNavngitteUttrykk(nivå, maxDybde) +
                        unpacked.høyre.finnNavngitteUttrykk(nivå, maxDybde)
                is IntDiv -> unpacked.venstre.finnNavngitteUttrykk(nivå, maxDybde) +
                        unpacked.høyre.finnNavngitteUttrykk(nivå, maxDybde)
                is Min -> unpacked.venstre.finnNavngitteUttrykk(nivå, maxDybde) +
                        unpacked.høyre.finnNavngitteUttrykk(nivå, maxDybde)
                is Neg -> unpacked.uttrykk.finnNavngitteUttrykk(nivå, maxDybde)
                is Og -> unpacked.venstre.finnNavngitteUttrykk(nivå, maxDybde) +
                        unpacked.høyre.finnNavngitteUttrykk(nivå, maxDybde)
                is Eller -> unpacked.venstre.finnNavngitteUttrykk(nivå, maxDybde) +
                        unpacked.høyre.finnNavngitteUttrykk(nivå, maxDybde)
                is Ikke -> unpacked.uttrykk.finnNavngitteUttrykk(nivå, maxDybde)
                is Lik<*> -> unpacked.venstre.finnNavngitteUttrykk(nivå, maxDybde) +
                        unpacked.høyre.finnNavngitteUttrykk(nivå, maxDybde)
                is Ulik<*> -> unpacked.venstre.finnNavngitteUttrykk(nivå, maxDybde) +
                        unpacked.høyre.finnNavngitteUttrykk(nivå, maxDybde)
                is StørreEnn<*> -> unpacked.venstre.finnNavngitteUttrykk(nivå, maxDybde) +
                        unpacked.høyre.finnNavngitteUttrykk(nivå, maxDybde)
                is MindreEnn<*> -> unpacked.venstre.finnNavngitteUttrykk(nivå, maxDybde) +
                        unpacked.høyre.finnNavngitteUttrykk(nivå, maxDybde)
                is StørreEllerLik<*> -> unpacked.venstre.finnNavngitteUttrykk(nivå, maxDybde) +
                        unpacked.høyre.finnNavngitteUttrykk(nivå, maxDybde)
                is MindreEllerLik<*> -> unpacked.venstre.finnNavngitteUttrykk(nivå, maxDybde) +
                        unpacked.høyre.finnNavngitteUttrykk(nivå, maxDybde)
                is ErBlant<*> -> unpacked.verdi.finnNavngitteUttrykk(nivå, maxDybde) +
                        unpacked.liste.finnNavngitteUttrykk(nivå, maxDybde)
                is ErIkkeBlant<*> -> unpacked.verdi.finnNavngitteUttrykk(nivå, maxDybde) +
                        unpacked.liste.finnNavngitteUttrykk(nivå, maxDybde)
                is Hvis<*> -> unpacked.betingelse.finnNavngitteUttrykk(nivå, maxDybde) +
                        unpacked.såUttrykk.finnNavngitteUttrykk(nivå, maxDybde) +
                        unpacked.ellersUttrykk.finnNavngitteUttrykk(nivå, maxDybde)
                is Feil<*> -> emptyList()
                is Tabell<*> -> unpacked.regler.flatMap { regel ->
                        regel.betingelse.finnNavngitteUttrykk(nivå, maxDybde) +
                        regel.resultat.finnNavngitteUttrykk(nivå, maxDybde)
                    } + (unpacked.ellersUttrykk?.finnNavngitteUttrykk(nivå, maxDybde) ?: emptyList())
                else -> emptyList()
            }
        }

        is Add -> this.venstre.finnNavngitteUttrykk(nivå, maxDybde) +
                this.høyre.finnNavngitteUttrykk(nivå, maxDybde)

        is Sub -> this.venstre.finnNavngitteUttrykk(nivå, maxDybde) +
                this.høyre.finnNavngitteUttrykk(nivå, maxDybde)

        is Mul -> this.venstre.finnNavngitteUttrykk(nivå, maxDybde) +
                this.høyre.finnNavngitteUttrykk(nivå, maxDybde)

        is Div -> this.venstre.finnNavngitteUttrykk(nivå, maxDybde) +
                this.høyre.finnNavngitteUttrykk(nivå, maxDybde)

        is IntDiv -> this.venstre.finnNavngitteUttrykk(nivå, maxDybde) +
                this.høyre.finnNavngitteUttrykk(nivå, maxDybde)

        is Min -> this.venstre.finnNavngitteUttrykk(nivå, maxDybde) +
                this.høyre.finnNavngitteUttrykk(nivå, maxDybde)

        is Neg -> this.uttrykk.finnNavngitteUttrykk(nivå, maxDybde)

        is Og -> this.venstre.finnNavngitteUttrykk(nivå, maxDybde) +
                this.høyre.finnNavngitteUttrykk(nivå, maxDybde)

        is Eller -> this.venstre.finnNavngitteUttrykk(nivå, maxDybde) +
                this.høyre.finnNavngitteUttrykk(nivå, maxDybde)

        is Ikke -> this.uttrykk.finnNavngitteUttrykk(nivå, maxDybde)

        is Lik<*> -> this.venstre.finnNavngitteUttrykk(nivå, maxDybde) +
                this.høyre.finnNavngitteUttrykk(nivå, maxDybde)

        is Ulik<*> -> this.venstre.finnNavngitteUttrykk(nivå, maxDybde) +
                this.høyre.finnNavngitteUttrykk(nivå, maxDybde)

        is StørreEnn<*> -> this.venstre.finnNavngitteUttrykk(nivå, maxDybde) +
                this.høyre.finnNavngitteUttrykk(nivå, maxDybde)

        is MindreEnn<*> -> this.venstre.finnNavngitteUttrykk(nivå, maxDybde) +
                this.høyre.finnNavngitteUttrykk(nivå, maxDybde)

        is StørreEllerLik<*> -> this.venstre.finnNavngitteUttrykk(nivå, maxDybde) +
                this.høyre.finnNavngitteUttrykk(nivå, maxDybde)

        is MindreEllerLik<*> -> this.venstre.finnNavngitteUttrykk(nivå, maxDybde) +
                this.høyre.finnNavngitteUttrykk(nivå, maxDybde)

        is ErBlant<*> -> this.verdi.finnNavngitteUttrykk(nivå, maxDybde) +
                this.liste.finnNavngitteUttrykk(nivå, maxDybde)

        is ErIkkeBlant<*> -> this.verdi.finnNavngitteUttrykk(nivå, maxDybde) +
                this.liste.finnNavngitteUttrykk(nivå, maxDybde)

        is Hvis<*> -> this.betingelse.finnNavngitteUttrykk(nivå, maxDybde) +
                this.såUttrykk.finnNavngitteUttrykk(nivå, maxDybde) +
                this.ellersUttrykk.finnNavngitteUttrykk(nivå, maxDybde)

        is Feil<*> -> emptyList()

        is Tabell<*> -> this.regler.flatMap { regel ->
                regel.betingelse.finnNavngitteUttrykk(nivå, maxDybde) +
                regel.resultat.finnNavngitteUttrykk(nivå, maxDybde)
            } + (this.ellersUttrykk?.finnNavngitteUttrykk(nivå, maxDybde) ?: emptyList())

        is Memo<*> -> this.uttrykk.finnNavngitteUttrykk(nivå, maxDybde)

        else -> emptyList()
    }
}

/**
 * Genererer kompakt 3-linjers forklaring.
 */
fun <T : Any> Uttrykk<T>.forklarKompakt(navn: String): String {
    return buildString {
        appendLine("$navn = ${notasjon()}")
        appendLine("$navn = ${konkret()}")
        appendLine("$navn = ${evaluer()}")
    }
}

/**
 * Informasjon om et konstant grunnlag.
 */
private data class KonstantGrunnlagInfo(
    val navn: String,
    val verdi: Any,
    val funksjon: String? = null
)

/**
 * Finner alle konstante Grunnlag-verdier i uttrykkstre.
 */
private fun <T : Any> Uttrykk<T>.finnKonstanteGrunnlag(): List<KonstantGrunnlagInfo> {
    return when (this) {
        is Grunnlag -> {
            if (this.utpakk() is Const) {
                // Dette er en konstant verdi
                listOf(KonstantGrunnlagInfo(this.navn, this.evaluer(), this.funksjon))
            } else {
                // Søk videre i det underliggende uttrykket
                this.utpakk().finnKonstanteGrunnlag()
            }
        }
        is Add -> venstre.finnKonstanteGrunnlag() + høyre.finnKonstanteGrunnlag()
        is Sub -> venstre.finnKonstanteGrunnlag() + høyre.finnKonstanteGrunnlag()
        is Mul -> venstre.finnKonstanteGrunnlag() + høyre.finnKonstanteGrunnlag()
        is Div -> venstre.finnKonstanteGrunnlag() + høyre.finnKonstanteGrunnlag()
        is IntDiv -> venstre.finnKonstanteGrunnlag() + høyre.finnKonstanteGrunnlag()
        is Neg -> uttrykk.finnKonstanteGrunnlag()
        is Min -> venstre.finnKonstanteGrunnlag() + høyre.finnKonstanteGrunnlag()
        is Og -> venstre.finnKonstanteGrunnlag() + høyre.finnKonstanteGrunnlag()
        is Eller -> venstre.finnKonstanteGrunnlag() + høyre.finnKonstanteGrunnlag()
        is Ikke -> uttrykk.finnKonstanteGrunnlag()
        is Lik<*> -> venstre.finnKonstanteGrunnlag() + høyre.finnKonstanteGrunnlag()
        is Ulik<*> -> venstre.finnKonstanteGrunnlag() + høyre.finnKonstanteGrunnlag()
        is StørreEnn<*> -> venstre.finnKonstanteGrunnlag() + høyre.finnKonstanteGrunnlag()
        is MindreEnn<*> -> venstre.finnKonstanteGrunnlag() + høyre.finnKonstanteGrunnlag()
        is StørreEllerLik<*> -> venstre.finnKonstanteGrunnlag() + høyre.finnKonstanteGrunnlag()
        is MindreEllerLik<*> -> venstre.finnKonstanteGrunnlag() + høyre.finnKonstanteGrunnlag()
        is ErBlant<*> -> verdi.finnKonstanteGrunnlag() + liste.finnKonstanteGrunnlag()
        is ErIkkeBlant<*> -> verdi.finnKonstanteGrunnlag() + liste.finnKonstanteGrunnlag()
        is Hvis<*> -> betingelse.finnKonstanteGrunnlag() + såUttrykk.finnKonstanteGrunnlag() + ellersUttrykk.finnKonstanteGrunnlag()
        is Feil<*> -> emptyList()
        is Tabell<*> -> regler.flatMap { regel ->
                regel.betingelse.finnKonstanteGrunnlag() + regel.resultat.finnKonstanteGrunnlag()
            } + (ellersUttrykk?.finnKonstanteGrunnlag() ?: emptyList())
        is Memo<*> -> uttrykk.finnKonstanteGrunnlag()
        else -> emptyList()
    }
}

/**
 * Genererer detaljert forklaring med alle faktum.
 * Kan valgfritt inkludere rvsId for hvert uttrykk.
 */
fun <T : Any> Uttrykk<T>.forklarDetaljert(navn: String, maxDybde: Int = 3, inkluderRvsId: Boolean = true): String {
    val forklaring = this.forklar(navn, maxDybde)

    // Hent rvsId og funksjon fra hoveduttrykket hvis det er Grunnlag
    val hovedRvsId = if (inkluderRvsId && this is Grunnlag) this.rvsId else null
    val hovedFunksjon = if (this is Grunnlag) this.funksjon else if (this is Const) this.funksjon else null

    return buildString {
        appendLine("HVORDAN")

        // Vis rvsId og funksjon hvis de finnes
        if (hovedRvsId != null && hovedFunksjon != null) {
            appendLine("    $hovedRvsId ($hovedFunksjon)")
        } else if (hovedRvsId != null) {
            appendLine("    $hovedRvsId")
        } else if (hovedFunksjon != null) {
            appendLine("    ($hovedFunksjon)")
        }

        // Symbolsk uttrykk - med innrykk for hver linje
        val symbolskLinjer = forklaring.hvaForklaring.symbolskUttrykk.lines()
        appendLine("    ${forklaring.hvaForklaring.navn} = ${symbolskLinjer.first()}")
        symbolskLinjer.drop(1).forEach { linje ->
            appendLine("    $linje")
        }

        // Konkret uttrykk - med innrykk for hver linje
        val konkretLinjer = forklaring.hvaForklaring.konkretUttrykk.lines()
        appendLine("    ${forklaring.hvaForklaring.navn} = ${konkretLinjer.first()}")
        konkretLinjer.drop(1).forEach { linje ->
            appendLine("    $linje")
        }

        appendLine("    ${forklaring.hvaForklaring.navn} = ${forklaring.hvaForklaring.resultat}")

        // Legg til navngitte subforklaringer
        forklaring.subformler.forEach { sub ->
            appendLine()

            // Finn rvsId og funksjon for dette subuttryket hvis de eksisterer
            val subRvsId = if (inkluderRvsId) this@forklarDetaljert.finnRvsIdFor(sub.hvaForklaring.navn) else null
            val subFunksjon = this@forklarDetaljert.finnFunksjonFor(sub.hvaForklaring.navn)

            if (subRvsId != null && subFunksjon != null) {
                appendLine("    $subRvsId ($subFunksjon)")
            } else if (subRvsId != null) {
                appendLine("    $subRvsId")
            } else if (subFunksjon != null) {
                appendLine("    ($subFunksjon)")
            }

            // Symbolsk uttrykk for subformel - med innrykk for hver linje
            val subSymbolskLinjer = sub.hvaForklaring.symbolskUttrykk.lines()
            appendLine("    ${sub.hvaForklaring.navn} = ${subSymbolskLinjer.first()}")
            subSymbolskLinjer.drop(1).forEach { linje ->
                appendLine("    $linje")
            }

            // Konkret uttrykk for subformel - med innrykk for hver linje
            val subKonkretLinjer = sub.hvaForklaring.konkretUttrykk.lines()
            appendLine("    ${sub.hvaForklaring.navn} = ${subKonkretLinjer.first()}")
            subKonkretLinjer.drop(1).forEach { linje ->
                appendLine("    $linje")
            }

            appendLine("    ${sub.hvaForklaring.navn} = ${sub.hvaForklaring.resultat}")
        }

        // Legg til konstante Grunnlag-verdier
        val konstanteGrunnlag = this@forklarDetaljert.finnKonstanteGrunnlag()
            .distinctBy { it.navn }  // Unike navn
        if (konstanteGrunnlag.isNotEmpty()) {
            appendLine()
            konstanteGrunnlag.forEach { info ->
                if (info.funksjon != null) {
                    appendLine("    ${info.navn} = ${info.verdi} (${info.funksjon})")
                } else {
                    appendLine("    ${info.navn} = ${info.verdi}")
                }
            }
        }
    }
}

/**
 * Visualiserer uttrykkstre som ASCII-tre.
 *
 * ## Eksempel output:
 * ```
 * Div
 * ├─ Mul
 * │  ├─ Var(sats)
 * │  └─ Var(G)
 * └─ Var(måneder)
 * ```
 */
fun <T : Any> Uttrykk<T>.treVisning(nivå: Int = 0): String {
    val indent = "│  ".repeat(nivå)
    val prefix = if (nivå == 0) "" else "├─ "

    return when (this) {
        // is Var -> "$indent$prefix Var(${faktum.name})"
        is Const -> "$indent$prefix Const($verdi)"
        is Add -> buildString {
            appendLine("${indent}${prefix}Add")
            appendLine(venstre.treVisning(nivå + 1))
            append(høyre.treVisning(nivå + 1).replace("├─", "└─"))
        }
        is Sub -> buildString {
            appendLine("${indent}${prefix}Sub")
            appendLine(venstre.treVisning(nivå + 1))
            append(høyre.treVisning(nivå + 1).replace("├─", "└─"))
        }
        is Mul -> buildString {
            appendLine("${indent}${prefix}Mul")
            appendLine(venstre.treVisning(nivå + 1))
            append(høyre.treVisning(nivå + 1).replace("├─", "└─"))
        }
        is Div -> buildString {
            appendLine("${indent}${prefix}Div")
            appendLine(venstre.treVisning(nivå + 1))
            append(høyre.treVisning(nivå + 1).replace("├─", "└─"))
        }
        is IntDiv -> buildString {
            appendLine("${indent}${prefix}IntDiv")
            appendLine(venstre.treVisning(nivå + 1))
            append(høyre.treVisning(nivå + 1).replace("├─", "└─"))
        }

        is Min -> buildString {
            appendLine("${indent}${prefix}Min")
            appendLine(venstre.treVisning(nivå + 1))
            append(høyre.treVisning(nivå + 1).replace("├─", "└─"))
        }
        is Neg -> buildString {
            appendLine("${indent}${prefix}Neg")
            append(uttrykk.treVisning(nivå + 1))
        }
        is Og -> buildString {
            appendLine("${indent}${prefix}Og")
            appendLine(venstre.treVisning(nivå + 1))
            append(høyre.treVisning(nivå + 1).replace("├─", "└─"))
        }
        is Eller -> buildString {
            appendLine("${indent}${prefix}Eller")
            appendLine(venstre.treVisning(nivå + 1))
            append(høyre.treVisning(nivå + 1).replace("├─", "└─"))
        }
        is Ikke -> buildString {
            appendLine("${indent}${prefix}Ikke")
            append(uttrykk.treVisning(nivå + 1))
        }
        is Lik<*> -> buildString {
            appendLine("${indent}${prefix}Lik")
            appendLine(venstre.treVisning(nivå + 1))
            append(høyre.treVisning(nivå + 1).replace("├─", "└─"))
        }
        is Ulik<*> -> buildString {
            appendLine("${indent}${prefix}Ulik")
            appendLine(venstre.treVisning(nivå + 1))
            append(høyre.treVisning(nivå + 1).replace("├─", "└─"))
        }
        is StørreEnn<*> -> buildString {
            appendLine("${indent}${prefix}StørreEnn")
            appendLine(venstre.treVisning(nivå + 1))
            append(høyre.treVisning(nivå + 1).replace("├─", "└─"))
        }
        is MindreEnn<*> -> buildString {
            appendLine("${indent}${prefix}MindreEnn")
            appendLine(venstre.treVisning(nivå + 1))
            append(høyre.treVisning(nivå + 1).replace("├─", "└─"))
        }
        is StørreEllerLik<*> -> buildString {
            appendLine("${indent}${prefix}StørreEllerLik")
            appendLine(venstre.treVisning(nivå + 1))
            append(høyre.treVisning(nivå + 1).replace("├─", "└─"))
        }
        is MindreEllerLik<*> -> buildString {
            appendLine("${indent}${prefix}MindreEllerLik")
            appendLine(venstre.treVisning(nivå + 1))
            append(høyre.treVisning(nivå + 1).replace("├─", "└─"))
        }
        is ErBlant<*> -> buildString {
            appendLine("${indent}${prefix}ErBlant")
            appendLine(verdi.treVisning(nivå + 1))
            append(liste.treVisning(nivå + 1).replace("├─", "└─"))
        }
        is ErIkkeBlant<*> -> buildString {
            appendLine("${indent}${prefix}ErIkkeBlant")
            appendLine(verdi.treVisning(nivå + 1))
            append(liste.treVisning(nivå + 1).replace("├─", "└─"))
        }
        is Hvis<*> -> buildString {
            appendLine("${indent}${prefix}Hvis")
            appendLine("${indent}│  ├─ Betingelse:")
            appendLine(betingelse.treVisning(nivå + 2))
            appendLine("${indent}│  ├─ Så:")
            appendLine(såUttrykk.treVisning(nivå + 2))
            appendLine("${indent}│  └─ Ellers:")
            append(ellersUttrykk.treVisning(nivå + 2))
        }
        is Grunnlag -> buildString {
            appendLine("${indent}${prefix}Grunnlag($navn)")
            append(uttrykk.treVisning(nivå + 1))
        }
        is Feil<*> -> "$indent$prefix Feil($melding)"
        is Tabell<*> -> buildString {
            appendLine("${indent}${prefix}Tabell${if (navn != null) "($navn)" else ""}")
            regler.forEachIndexed { index, regel ->
                appendLine("${indent}│  ├─ Regel ${index + 1}:")
                appendLine("${indent}│  │  ├─ Når:")
                appendLine(regel.betingelse.treVisning(nivå + 3))
                appendLine("${indent}│  │  └─ Resultat:")
                append(regel.resultat.treVisning(nivå + 3))
                if (index < regler.size - 1) appendLine()
            }
            ellersUttrykk?.let {
                appendLine()
                appendLine("${indent}│  └─ Ellers:")
                append(it.treVisning(nivå + 2))
            }
        }
        is Memo<*> -> buildString {
            appendLine("${indent}${prefix}Memo (cached)")
            append(uttrykk.treVisning(nivå + 1))
        }
    }
}

/**
 * Pattern matching visitor for uttrykkstre.
 *
 * Denne funksjonen lar deg traverse treet og transformere det.
 *
 * ## Eksempel: Finn alle variabler
 * ```kotlin
 * val variabler = uttrykk.visit { expr ->
 *     when (expr) {
 *         is Var -> listOf(expr.faktum.name)
 *         else -> emptyList()
 *     }
 * }
 * ```
 */
fun <T : Any, R> Uttrykk<T>.visit(transform: (Uttrykk<*>) -> List<R>): List<R> {
    val current = transform(this)

    val children = when (this) {
        is Add -> venstre.visit(transform) + høyre.visit(transform)
        is Sub -> venstre.visit(transform) + høyre.visit(transform)
        is Mul -> venstre.visit(transform) + høyre.visit(transform)
        is Div -> venstre.visit(transform) + høyre.visit(transform)
        is IntDiv -> venstre.visit(transform) + høyre.visit(transform)
        is Min -> venstre.visit(transform) + høyre.visit(transform)
        is Neg -> uttrykk.visit(transform)
        is Og -> venstre.visit(transform) + høyre.visit(transform)
        is Eller -> venstre.visit(transform) + høyre.visit(transform)
        is Ikke -> uttrykk.visit(transform)
        is Lik<*> -> venstre.visit(transform) + høyre.visit(transform)
        is Ulik<*> -> venstre.visit(transform) + høyre.visit(transform)
        is StørreEnn<*> -> venstre.visit(transform) + høyre.visit(transform)
        is MindreEnn<*> -> venstre.visit(transform) + høyre.visit(transform)
        is StørreEllerLik<*> -> venstre.visit(transform) + høyre.visit(transform)
        is MindreEllerLik<*> -> venstre.visit(transform) + høyre.visit(transform)
        is ErBlant<*> -> verdi.visit(transform) + liste.visit(transform)
        is ErIkkeBlant<*> -> verdi.visit(transform) + liste.visit(transform)
        is Hvis<*> -> betingelse.visit(transform) + såUttrykk.visit(transform) + ellersUttrykk.visit(transform)
        is Grunnlag -> uttrykk.visit(transform)
        is Feil<*> -> emptyList()
        is Tabell<*> -> regler.flatMap { regel ->
                regel.betingelse.visit(transform) + regel.resultat.visit(transform)
            } + (ellersUttrykk?.visit(transform) ?: emptyList())
        is Memo<*> -> uttrykk.visit(transform)
        else -> emptyList()
    }

    return current + children
}

/**
 * Forenkler uttrykket ved å evaluere konstante subtre.
 *
 * Eksempel: `Mul(Const(2), Const(3))` blir til `Const(6)`
 */
fun <T : Any> Uttrykk<T>.forenkel(): Uttrykk<T> {
    @Suppress("UNCHECKED_CAST")
    return when (this) {
        is Const -> this

        is Add<*> -> {
            val v = venstre.forenkel()
            val h = høyre.forenkel()
            if (v is Const && h is Const) {
                Const(evaluer()) as Uttrykk<T>
            } else {
                @Suppress("UNCHECKED_CAST")
                Add<Number>(v as Uttrykk<Number>, h as Uttrykk<Number>) as Uttrykk<T>
            }
        }

        is Sub<*> -> {
            val v = venstre.forenkel()
            val h = høyre.forenkel()
            if (v is Const && h is Const) {
                Const(evaluer()) as Uttrykk<T>
            } else {
                @Suppress("UNCHECKED_CAST")
                Sub<Number>(v as Uttrykk<Number>, h as Uttrykk<Number>) as Uttrykk<T>
            }
        }

        is Mul<*> -> {
            val v = venstre.forenkel()
            val h = høyre.forenkel()
            if (v is Const && h is Const) {
                Const(evaluer()) as Uttrykk<T>
            } else {
                @Suppress("UNCHECKED_CAST")
                Mul<Number>(v as Uttrykk<Number>, h as Uttrykk<Number>) as Uttrykk<T>
            }
        }

        is Div -> {
            val v = venstre.forenkel()
            val h = høyre.forenkel()
            if (v is Const && h is Const) {
                Const(evaluer()) as Uttrykk<T>
            } else {
                @Suppress("UNCHECKED_CAST")
                Div(v as Uttrykk<Number>, h as Uttrykk<Number>) as Uttrykk<T>
            }
        }

        is IntDiv -> {
            val v = venstre.forenkel()
            val h = høyre.forenkel()
            if (v is Const && h is Const) {
                Const(evaluer()) as Uttrykk<T>
            } else {
                @Suppress("UNCHECKED_CAST")
                IntDiv(v as Uttrykk<Number>, h as Uttrykk<Number>) as Uttrykk<T>
            }
        }

        is Min -> {
            val v = venstre.forenkel()
            val h = høyre.forenkel()
            if (v is Const && h is Const) {
                Const(evaluer()) as Uttrykk<T>
            } else {
                @Suppress("UNCHECKED_CAST")
                Min(v as Uttrykk<Number>, h as Uttrykk<Number>) as Uttrykk<T>
            }
        }

        is Neg<*> -> {
            val u = uttrykk.forenkel()
            if (u is Const) {
                Const(-u.verdi.toDouble()) as Uttrykk<T>
            } else {
                @Suppress("UNCHECKED_CAST")
                Neg<Number>(u as Uttrykk<Number>) as Uttrykk<T>
            }
        }

        is Og -> {
            val v = venstre.forenkel()
            val h = høyre.forenkel()
            if (v is Const && h is Const) {
                Const(evaluer()) as Uttrykk<T>
            } else {
                Og(v as Uttrykk<Boolean>, h as Uttrykk<Boolean>) as Uttrykk<T>
            }
        }

        is Eller -> {
            val v = venstre.forenkel()
            val h = høyre.forenkel()
            if (v is Const && h is Const) {
                Const(evaluer()) as Uttrykk<T>
            } else {
                Eller(v as Uttrykk<Boolean>, h as Uttrykk<Boolean>) as Uttrykk<T>
            }
        }

        is Ikke -> {
            val u = uttrykk.forenkel()
            if (u is Const) {
                Const(!u.verdi as Boolean) as Uttrykk<T>
            } else {
                Ikke(u as Uttrykk<Boolean>) as Uttrykk<T>
            }
        }

        is Lik<*> -> {
            val v = venstre.forenkel()
            val h = høyre.forenkel()
            if (v is Const && h is Const) {
                Const(evaluer()) as Uttrykk<T>
            } else {
                Lik(v as Uttrykk<Comparable<Any>>, h as Uttrykk<Comparable<Any>>) as Uttrykk<T>
            }
        }

        is Ulik<*> -> {
            val v = venstre.forenkel()
            val h = høyre.forenkel()
            if (v is Const && h is Const) {
                Const(evaluer()) as Uttrykk<T>
            } else {
                Ulik(v as Uttrykk<Comparable<Any>>, h as Uttrykk<Comparable<Any>>) as Uttrykk<T>
            }
        }

        is StørreEnn<*> -> {
            val v = venstre.forenkel()
            val h = høyre.forenkel()
            if (v is Const && h is Const) {
                Const(evaluer()) as Uttrykk<T>
            } else {
                StørreEnn(v as Uttrykk<Comparable<Any>>, h as Uttrykk<Comparable<Any>>) as Uttrykk<T>
            }
        }

        is MindreEnn<*> -> {
            val v = venstre.forenkel()
            val h = høyre.forenkel()
            if (v is Const && h is Const) {
                Const(evaluer()) as Uttrykk<T>
            } else {
                MindreEnn(v as Uttrykk<Comparable<Any>>, h as Uttrykk<Comparable<Any>>) as Uttrykk<T>
            }
        }

        is StørreEllerLik<*> -> {
            val v = venstre.forenkel()
            val h = høyre.forenkel()
            if (v is Const && h is Const) {
                Const(evaluer()) as Uttrykk<T>
            } else {
                StørreEllerLik(v as Uttrykk<Comparable<Any>>, h as Uttrykk<Comparable<Any>>) as Uttrykk<T>
            }
        }

        is MindreEllerLik<*> -> {
            val v = venstre.forenkel()
            val h = høyre.forenkel()
            if (v is Const && h is Const) {
                Const(evaluer()) as Uttrykk<T>
            } else {
                MindreEllerLik(v as Uttrykk<Comparable<Any>>, h as Uttrykk<Comparable<Any>>) as Uttrykk<T>
            }
        }

        is ErBlant<*> -> {
            val v = verdi.forenkel()
            val l = liste.forenkel()
            if (v is Const && l is Const) {
                Const(evaluer()) as Uttrykk<T>
            } else {
                ErBlant(v as Uttrykk<Any>, l as Uttrykk<List<Any>>) as Uttrykk<T>
            }
        }

        is ErIkkeBlant<*> -> {
            val v = verdi.forenkel()
            val l = liste.forenkel()
            if (v is Const && l is Const) {
                Const(evaluer()) as Uttrykk<T>
            } else {
                ErIkkeBlant(v as Uttrykk<Any>, l as Uttrykk<List<Any>>) as Uttrykk<T>
            }
        }

        is Hvis<*> -> {
            val bet = betingelse.forenkel()
            val so = såUttrykk.forenkel()
            val els = ellersUttrykk.forenkel()
            // Hvis betingelsen er konstant, velg riktig gren
            if (bet is Const) {
                @Suppress("UNCHECKED_CAST")
                if (bet.verdi as Boolean) so as Uttrykk<T> else els as Uttrykk<T>
            } else {
                Hvis(bet as Uttrykk<Boolean>, so, els) as Uttrykk<T>
            }
        }

        is Grunnlag -> Grunnlag(navn, uttrykk.forenkel(), rvsId)

        is Feil<*> -> this

        is Tabell<*> -> {
            val forenkledeRegler = regler.map { regel ->
                TabellRegel(
                    betingelse = regel.betingelse.forenkel() as Uttrykk<Boolean>,
                    resultat = regel.resultat.forenkel()
                )
            }
            val forenkletEllers = ellersUttrykk?.forenkel()
            Tabell(navn, forenkledeRegler, forenkletEllers) as Uttrykk<T>
        }

        is Memo<*> -> {
            // Forenkle det underliggende uttrykket, men behold memoisering
            Memo(uttrykk.forenkel()) as Uttrykk<T>
        }
    }
}

/**
 * Erstatter alle forekomster av en variabel med et nytt uttrykk.
 *
 * ## Eksempel: Substituering
 * ```kotlin
 * val uttrykk = Add(Var(x), Var(y))
 * val substitued = uttrykk.erstatt("x") { Const(10) }
 * // Resultat: Add(Const(10), Var(y))
 * ```
 */
fun <T : Any> Uttrykk<T>.erstatt(variabelNavn: String, med: () -> Uttrykk<Any>): Uttrykk<T> {
    @Suppress("UNCHECKED_CAST")
    return when (this) {
        is Const -> this
        is Add<*> -> {
            val v = venstre.erstatt(variabelNavn, med)
            val h = høyre.erstatt(variabelNavn, med)
            @Suppress("UNCHECKED_CAST")
            Add<Number>(v as Uttrykk<Number>, h as Uttrykk<Number>) as Uttrykk<T>
        }
        is Sub<*> -> {
            val v = venstre.erstatt(variabelNavn, med)
            val h = høyre.erstatt(variabelNavn, med)
            @Suppress("UNCHECKED_CAST")
            Sub<Number>(v as Uttrykk<Number>, h as Uttrykk<Number>) as Uttrykk<T>
        }
        is Mul<*> -> {
            val v = venstre.erstatt(variabelNavn, med)
            val h = høyre.erstatt(variabelNavn, med)
            @Suppress("UNCHECKED_CAST")
            Mul<Number>(v as Uttrykk<Number>, h as Uttrykk<Number>) as Uttrykk<T>
        }
        is Div -> {
            val v = venstre.erstatt(variabelNavn, med)
            val h = høyre.erstatt(variabelNavn, med)
            @Suppress("UNCHECKED_CAST")
            Div(v as Uttrykk<Number>, h as Uttrykk<Number>) as Uttrykk<T>
        }
        is IntDiv -> {
            val v = venstre.erstatt(variabelNavn, med)
            val h = høyre.erstatt(variabelNavn, med)
            @Suppress("UNCHECKED_CAST")
            IntDiv(v as Uttrykk<Number>, h as Uttrykk<Number>) as Uttrykk<T>
        }
        is Min -> {
            val v = venstre.erstatt(variabelNavn, med)
            val h = høyre.erstatt(variabelNavn, med)
            @Suppress("UNCHECKED_CAST")
            Min(v as Uttrykk<Number>, h as Uttrykk<Number>) as Uttrykk<T>
        }
        is Neg<*> -> {
            val u = uttrykk.erstatt(variabelNavn, med)
            @Suppress("UNCHECKED_CAST")
            Neg<Number>(u as Uttrykk<Number>) as Uttrykk<T>
        }
        is Og -> {
            val v = venstre.erstatt(variabelNavn, med)
            val h = høyre.erstatt(variabelNavn, med)
            Og(v as Uttrykk<Boolean>, h as Uttrykk<Boolean>) as Uttrykk<T>
        }
        is Eller -> {
            val v = venstre.erstatt(variabelNavn, med)
            val h = høyre.erstatt(variabelNavn, med)
            Eller(v as Uttrykk<Boolean>, h as Uttrykk<Boolean>) as Uttrykk<T>
        }
        is Ikke -> {
            val u = uttrykk.erstatt(variabelNavn, med)
            Ikke(u as Uttrykk<Boolean>) as Uttrykk<T>
        }
        is Lik<*> -> {
            val v = venstre.erstatt(variabelNavn, med)
            val h = høyre.erstatt(variabelNavn, med)
            Lik(v as Uttrykk<Comparable<Any>>, h as Uttrykk<Comparable<Any>>) as Uttrykk<T>
        }
        is Ulik<*> -> {
            val v = venstre.erstatt(variabelNavn, med)
            val h = høyre.erstatt(variabelNavn, med)
            Ulik(v as Uttrykk<Comparable<Any>>, h as Uttrykk<Comparable<Any>>) as Uttrykk<T>
        }
        is StørreEnn<*> -> {
            val v = venstre.erstatt(variabelNavn, med)
            val h = høyre.erstatt(variabelNavn, med)
            StørreEnn(v as Uttrykk<Comparable<Any>>, h as Uttrykk<Comparable<Any>>) as Uttrykk<T>
        }
        is MindreEnn<*> -> {
            val v = venstre.erstatt(variabelNavn, med)
            val h = høyre.erstatt(variabelNavn, med)
            MindreEnn(v as Uttrykk<Comparable<Any>>, h as Uttrykk<Comparable<Any>>) as Uttrykk<T>
        }
        is StørreEllerLik<*> -> {
            val v = venstre.erstatt(variabelNavn, med)
            val h = høyre.erstatt(variabelNavn, med)
            StørreEllerLik(v as Uttrykk<Comparable<Any>>, h as Uttrykk<Comparable<Any>>) as Uttrykk<T>
        }
        is MindreEllerLik<*> -> {
            val v = venstre.erstatt(variabelNavn, med)
            val h = høyre.erstatt(variabelNavn, med)
            MindreEllerLik(v as Uttrykk<Comparable<Any>>, h as Uttrykk<Comparable<Any>>) as Uttrykk<T>
        }
        is ErBlant<*> -> {
            val v = verdi.erstatt(variabelNavn, med)
            val l = liste.erstatt(variabelNavn, med)
            ErBlant(v as Uttrykk<Any>, l as Uttrykk<List<Any>>) as Uttrykk<T>
        }
        is ErIkkeBlant<*> -> {
            val v = verdi.erstatt(variabelNavn, med)
            val l = liste.erstatt(variabelNavn, med)
            ErIkkeBlant(v as Uttrykk<Any>, l as Uttrykk<List<Any>>) as Uttrykk<T>
        }
        is Hvis<*> -> {
            val bet = betingelse.erstatt(variabelNavn, med)
            val so = såUttrykk.erstatt(variabelNavn, med)
            val els = ellersUttrykk.erstatt(variabelNavn, med)
            Hvis(bet as Uttrykk<Boolean>, so, els) as Uttrykk<T>
        }
        is Grunnlag -> Grunnlag(navn, uttrykk.erstatt(variabelNavn, med), rvsId)
        is Feil<*> -> this
        is Tabell<*> -> {
            val erstattedeRegler = regler.map { regel ->
                TabellRegel(
                    betingelse = regel.betingelse.erstatt(variabelNavn, med) as Uttrykk<Boolean>,
                    resultat = regel.resultat.erstatt(variabelNavn, med)
                )
            }
            val erstattetEllers = ellersUttrykk?.erstatt(variabelNavn, med)
            Tabell(navn, erstattedeRegler, erstattetEllers) as Uttrykk<T>
        }
        is Memo<*> -> {
            // Erstatt i det underliggende uttrykket, men behold memoisering
            Memo(uttrykk.erstatt(variabelNavn, med)) as Uttrykk<T>
        }
    }
}

/**
 * Finner rvsId for et navngitt uttrykk med gitt navn.
 * Traverserer uttrykkstre og returnerer rvsId til første Grunnlag med matchende navn.
 */
fun <T : Any> Uttrykk<T>.finnRvsIdFor(uttrykkNavn: String): String? {
    return when (this) {
        is Grunnlag -> {
            if (this.navn == uttrykkNavn) {
                this.rvsId
            } else {
                this.uttrykk.finnRvsIdFor(uttrykkNavn)
            }
        }
        is Add -> venstre.finnRvsIdFor(uttrykkNavn) ?: høyre.finnRvsIdFor(uttrykkNavn)
        is Sub -> venstre.finnRvsIdFor(uttrykkNavn) ?: høyre.finnRvsIdFor(uttrykkNavn)
        is Mul -> venstre.finnRvsIdFor(uttrykkNavn) ?: høyre.finnRvsIdFor(uttrykkNavn)
        is Div -> venstre.finnRvsIdFor(uttrykkNavn) ?: høyre.finnRvsIdFor(uttrykkNavn)
        is IntDiv -> venstre.finnRvsIdFor(uttrykkNavn) ?: høyre.finnRvsIdFor(uttrykkNavn)
        is Min -> venstre.finnRvsIdFor(uttrykkNavn) ?: høyre.finnRvsIdFor(uttrykkNavn)
        is Neg -> uttrykk.finnRvsIdFor(uttrykkNavn)
        is Og -> venstre.finnRvsIdFor(uttrykkNavn) ?: høyre.finnRvsIdFor(uttrykkNavn)
        is Eller -> venstre.finnRvsIdFor(uttrykkNavn) ?: høyre.finnRvsIdFor(uttrykkNavn)
        is Ikke -> uttrykk.finnRvsIdFor(uttrykkNavn)
        is Lik<*> -> venstre.finnRvsIdFor(uttrykkNavn) ?: høyre.finnRvsIdFor(uttrykkNavn)
        is Ulik<*> -> venstre.finnRvsIdFor(uttrykkNavn) ?: høyre.finnRvsIdFor(uttrykkNavn)
        is StørreEnn<*> -> venstre.finnRvsIdFor(uttrykkNavn) ?: høyre.finnRvsIdFor(uttrykkNavn)
        is MindreEnn<*> -> venstre.finnRvsIdFor(uttrykkNavn) ?: høyre.finnRvsIdFor(uttrykkNavn)
        is StørreEllerLik<*> -> venstre.finnRvsIdFor(uttrykkNavn) ?: høyre.finnRvsIdFor(uttrykkNavn)
        is MindreEllerLik<*> -> venstre.finnRvsIdFor(uttrykkNavn) ?: høyre.finnRvsIdFor(uttrykkNavn)
        is ErBlant<*> -> verdi.finnRvsIdFor(uttrykkNavn) ?: liste.finnRvsIdFor(uttrykkNavn)
        is ErIkkeBlant<*> -> verdi.finnRvsIdFor(uttrykkNavn) ?: liste.finnRvsIdFor(uttrykkNavn)
        is Hvis<*> -> betingelse.finnRvsIdFor(uttrykkNavn) ?: såUttrykk.finnRvsIdFor(uttrykkNavn) ?: ellersUttrykk.finnRvsIdFor(uttrykkNavn)
        is Feil<*> -> null
        is Tabell<*> -> {
            regler.asSequence()
                .mapNotNull { regel ->
                    regel.betingelse.finnRvsIdFor(uttrykkNavn) ?: regel.resultat.finnRvsIdFor(uttrykkNavn)
                }
                .firstOrNull()
                ?: ellersUttrykk?.finnRvsIdFor(uttrykkNavn)
        }
        is Memo<*> -> uttrykk.finnRvsIdFor(uttrykkNavn)
        else -> null
    }
}

/**
 * Finner funksjonsnavn for et navngitt uttrykk med gitt navn.
 * Traverserer uttrykkstre og returnerer funksjon til første Grunnlag med matchende navn.
 */
fun <T : Any> Uttrykk<T>.finnFunksjonFor(uttrykkNavn: String): String? {
    return when (this) {
        is Grunnlag -> {
            if (this.navn == uttrykkNavn) {
                this.funksjon
            } else {
                this.uttrykk.finnFunksjonFor(uttrykkNavn)
            }
        }
        is Add -> venstre.finnFunksjonFor(uttrykkNavn) ?: høyre.finnFunksjonFor(uttrykkNavn)
        is Sub -> venstre.finnFunksjonFor(uttrykkNavn) ?: høyre.finnFunksjonFor(uttrykkNavn)
        is Mul -> venstre.finnFunksjonFor(uttrykkNavn) ?: høyre.finnFunksjonFor(uttrykkNavn)
        is Div -> venstre.finnFunksjonFor(uttrykkNavn) ?: høyre.finnFunksjonFor(uttrykkNavn)
        is IntDiv -> venstre.finnFunksjonFor(uttrykkNavn) ?: høyre.finnFunksjonFor(uttrykkNavn)
        is Min -> venstre.finnFunksjonFor(uttrykkNavn) ?: høyre.finnFunksjonFor(uttrykkNavn)
        is Neg -> uttrykk.finnFunksjonFor(uttrykkNavn)
        is Og -> venstre.finnFunksjonFor(uttrykkNavn) ?: høyre.finnFunksjonFor(uttrykkNavn)
        is Eller -> venstre.finnFunksjonFor(uttrykkNavn) ?: høyre.finnFunksjonFor(uttrykkNavn)
        is Ikke -> uttrykk.finnFunksjonFor(uttrykkNavn)
        is Lik<*> -> venstre.finnFunksjonFor(uttrykkNavn) ?: høyre.finnFunksjonFor(uttrykkNavn)
        is Ulik<*> -> venstre.finnFunksjonFor(uttrykkNavn) ?: høyre.finnFunksjonFor(uttrykkNavn)
        is StørreEnn<*> -> venstre.finnFunksjonFor(uttrykkNavn) ?: høyre.finnFunksjonFor(uttrykkNavn)
        is MindreEnn<*> -> venstre.finnFunksjonFor(uttrykkNavn) ?: høyre.finnFunksjonFor(uttrykkNavn)
        is StørreEllerLik<*> -> venstre.finnFunksjonFor(uttrykkNavn) ?: høyre.finnFunksjonFor(uttrykkNavn)
        is MindreEllerLik<*> -> venstre.finnFunksjonFor(uttrykkNavn) ?: høyre.finnFunksjonFor(uttrykkNavn)
        is ErBlant<*> -> verdi.finnFunksjonFor(uttrykkNavn) ?: liste.finnFunksjonFor(uttrykkNavn)
        is ErIkkeBlant<*> -> verdi.finnFunksjonFor(uttrykkNavn) ?: liste.finnFunksjonFor(uttrykkNavn)
        is Hvis<*> -> betingelse.finnFunksjonFor(uttrykkNavn) ?: såUttrykk.finnFunksjonFor(uttrykkNavn) ?: ellersUttrykk.finnFunksjonFor(uttrykkNavn)
        is Feil<*> -> null
        is Tabell<*> -> {
            regler.asSequence()
                .mapNotNull { regel ->
                    regel.betingelse.finnFunksjonFor(uttrykkNavn) ?: regel.resultat.finnFunksjonFor(uttrykkNavn)
                }
                .firstOrNull()
                ?: ellersUttrykk?.finnFunksjonFor(uttrykkNavn)
        }
        is Memo<*> -> uttrykk.finnFunksjonFor(uttrykkNavn)
        is Const -> this.funksjon
        else -> null
    }
}

/**
 * Data class for tracking structural duplicates
 */
private data class SubtreeInfo(
    val hash: String,
    val firstOccurrence: Uttrykk<*>,
    val referenceId: Int,
    var occurrenceCount: Int = 1
)

/**
 * Visualiserer uttrykkstre som ASCII-tre med strukturell deduplikasjon.
 *
 * Strukturelt identiske subtrær vises kun én gang.  Duplikater erstattes med referanser.
 * Dette gjør komplekse trær med repeterende mønstre mye mer lesbare.
 *
 * ## Eksempel output:
 * ```
 * Tabell(flyktningVurdering)
 * ├─ Regel 1:
 * │  ├─ Når: Ikke
 * │  │  └─ angittFlyktning → [1]
 * │  └─ Resultat: Const(IKKE_RELEVANT)
 * ├─ Regel 2:
 * │  ├─ Når: Og
 * │  │  ├─ angittFlyktning → [1]
 * │  │  └─ kravFom2021 → [2]
 * │  └─ Resultat: Const(OPPFYLT)
 *
 * [Appendix - Strukturelt Dedupliserte Uttrykk]
 * [1] angittFlyktning:
 *     Memo (cached)
 *     └─ Eller
 *        ├─ flyktningFlagg → [3]
 *        └─ ...
 *
 * [2] kravFom2021:
 *     Const(true)
 * ```
 *
 * @param nivå Startnivå for innrykk (default 0)
 * @return String med kompakt trevisning inkludert appendix
 */
fun <T : Any> Uttrykk<T>.treVisningKompakt(nivå: Int = 0): String {
    // Pass 1: Samle alle strukturelle hash-verdier og deres forekomster
    val seenSubtrees = mutableMapOf<String, SubtreeInfo>()
    var nextRefId = 1

    fun samleHashes(expr: Uttrykk<*>) {
        // Kun track Grunnlag-noder for deduplikasjon
        if (expr is Grunnlag) {
            val hash = expr.strukturellHash()

            if (hash in seenSubtrees) {
                seenSubtrees[hash]!!.occurrenceCount++
            } else {
                seenSubtrees[hash] = SubtreeInfo(hash, expr, nextRefId++)
            }
        }

        // Traverser children
        when (expr) {
            is Add -> { samleHashes(expr.venstre); samleHashes(expr.høyre) }
            is Sub -> { samleHashes(expr.venstre); samleHashes(expr.høyre) }
            is Mul -> { samleHashes(expr.venstre); samleHashes(expr.høyre) }
            is Div -> { samleHashes(expr.venstre); samleHashes(expr.høyre) }
            is IntDiv -> { samleHashes(expr.venstre); samleHashes(expr.høyre) }
            is Min -> { samleHashes(expr.venstre); samleHashes(expr.høyre) }
            is Neg -> samleHashes(expr.uttrykk)
            is Og -> { samleHashes(expr.venstre); samleHashes(expr.høyre) }
            is Eller -> { samleHashes(expr.venstre); samleHashes(expr.høyre) }
            is Ikke -> samleHashes(expr.uttrykk)
            is Lik<*> -> { samleHashes(expr.venstre); samleHashes(expr.høyre) }
            is Ulik<*> -> { samleHashes(expr.venstre); samleHashes(expr.høyre) }
            is StørreEnn<*> -> { samleHashes(expr.venstre); samleHashes(expr.høyre) }
            is MindreEnn<*> -> { samleHashes(expr.venstre); samleHashes(expr.høyre) }
            is StørreEllerLik<*> -> { samleHashes(expr.venstre); samleHashes(expr.høyre) }
            is MindreEllerLik<*> -> { samleHashes(expr.venstre); samleHashes(expr.høyre) }
            is ErBlant<*> -> { samleHashes(expr.verdi); samleHashes(expr.liste) }
            is ErIkkeBlant<*> -> { samleHashes(expr.verdi); samleHashes(expr.liste) }
            is Hvis<*> -> { samleHashes(expr.betingelse); samleHashes(expr.såUttrykk); samleHashes(expr.ellersUttrykk) }
            is Grunnlag -> samleHashes(expr.uttrykk)
            is Tabell<*> -> {
                expr.regler.forEach { regel ->
                    samleHashes(regel.betingelse)
                    samleHashes(regel.resultat)
                }
                expr.ellersUttrykk?.let { samleHashes(it) }
            }
            is Memo<*> -> samleHashes(expr.uttrykk)
            else -> { /* Leaf nodes */ }
        }
    }

    samleHashes(this)

    // Pass 2: Render treet med referanser for duplikater
    val shownHashes = mutableSetOf<String>()

    fun renderMedReferanser(expr: Uttrykk<*>, nivå: Int, erSiste: Boolean = false): String {
        val indent = "│  ".repeat(nivå)
        val prefix = if (nivå == 0) "" else if (erSiste) "└─ " else "├─ "

        // Kun sjekk deduplikasjon for Grunnlag-noder
        if (expr is Grunnlag) {
            val hash = expr.strukturellHash()
            val info = seenSubtrees[hash]!!

            // Hvis dette er en duplicate OG har mer enn 1 forekomst, vis referanse
            if (info.occurrenceCount > 1 && hash in shownHashes) {
                return "$indent$prefix${expr.navn} → [${info.referenceId}]"
            }

            // Marker som vist
            shownHashes.add(hash)
        }

        // Render normalt
        return when (expr) {
            is Const -> "$indent$prefix Const(${expr.verdi})"
            is Add -> buildString {
                appendLine("${indent}${prefix}Add")
                appendLine(renderMedReferanser(expr.venstre, nivå + 1, false))
                append(renderMedReferanser(expr.høyre, nivå + 1, true))
            }
            is Sub -> buildString {
                appendLine("${indent}${prefix}Sub")
                appendLine(renderMedReferanser(expr.venstre, nivå + 1, false))
                append(renderMedReferanser(expr.høyre, nivå + 1, true))
            }
            is Mul -> buildString {
                appendLine("${indent}${prefix}Mul")
                appendLine(renderMedReferanser(expr.venstre, nivå + 1, false))
                append(renderMedReferanser(expr.høyre, nivå + 1, true))
            }
            is Div -> buildString {
                appendLine("${indent}${prefix}Div")
                appendLine(renderMedReferanser(expr.venstre, nivå + 1, false))
                append(renderMedReferanser(expr.høyre, nivå + 1, true))
            }
            is IntDiv -> buildString {
                appendLine("${indent}${prefix}IntDiv")
                appendLine(renderMedReferanser(expr.venstre, nivå + 1, false))
                append(renderMedReferanser(expr.høyre, nivå + 1, true))
            }
            is Min -> buildString {
                appendLine("${indent}${prefix}Min")
                appendLine(renderMedReferanser(expr.venstre, nivå + 1, false))
                append(renderMedReferanser(expr.høyre, nivå + 1, true))
            }
            is Neg -> buildString {
                appendLine("${indent}${prefix}Neg")
                append(renderMedReferanser(expr.uttrykk, nivå + 1, true))
            }
            is Og -> buildString {
                appendLine("${indent}${prefix}Og")
                appendLine(renderMedReferanser(expr.venstre, nivå + 1, false))
                append(renderMedReferanser(expr.høyre, nivå + 1, true))
            }
            is Eller -> buildString {
                appendLine("${indent}${prefix}Eller")
                appendLine(renderMedReferanser(expr.venstre, nivå + 1, false))
                append(renderMedReferanser(expr.høyre, nivå + 1, true))
            }
            is Ikke -> buildString {
                appendLine("${indent}${prefix}Ikke")
                append(renderMedReferanser(expr.uttrykk, nivå + 1, true))
            }
            is Lik<*> -> buildString {
                appendLine("${indent}${prefix}Lik")
                appendLine(renderMedReferanser(expr.venstre, nivå + 1, false))
                append(renderMedReferanser(expr.høyre, nivå + 1, true))
            }
            is Ulik<*> -> buildString {
                appendLine("${indent}${prefix}Ulik")
                appendLine(renderMedReferanser(expr.venstre, nivå + 1, false))
                append(renderMedReferanser(expr.høyre, nivå + 1, true))
            }
            is StørreEnn<*> -> buildString {
                appendLine("${indent}${prefix}StørreEnn")
                appendLine(renderMedReferanser(expr.venstre, nivå + 1, false))
                append(renderMedReferanser(expr.høyre, nivå + 1, true))
            }
            is MindreEnn<*> -> buildString {
                appendLine("${indent}${prefix}MindreEnn")
                appendLine(renderMedReferanser(expr.venstre, nivå + 1, false))
                append(renderMedReferanser(expr.høyre, nivå + 1, true))
            }
            is StørreEllerLik<*> -> buildString {
                appendLine("${indent}${prefix}StørreEllerLik")
                appendLine(renderMedReferanser(expr.venstre, nivå + 1, false))
                append(renderMedReferanser(expr.høyre, nivå + 1, true))
            }
            is MindreEllerLik<*> -> buildString {
                appendLine("${indent}${prefix}MindreEllerLik")
                appendLine(renderMedReferanser(expr.venstre, nivå + 1, false))
                append(renderMedReferanser(expr.høyre, nivå + 1, true))
            }
            is ErBlant<*> -> buildString {
                appendLine("${indent}${prefix}ErBlant")
                appendLine(renderMedReferanser(expr.verdi, nivå + 1, false))
                append(renderMedReferanser(expr.liste, nivå + 1, true))
            }
            is ErIkkeBlant<*> -> buildString {
                appendLine("${indent}${prefix}ErIkkeBlant")
                appendLine(renderMedReferanser(expr.verdi, nivå + 1, false))
                append(renderMedReferanser(expr.liste, nivå + 1, true))
            }
            is Hvis<*> -> buildString {
                appendLine("${indent}${prefix}Hvis")
                appendLine("${indent}│  ├─ Betingelse:")
                appendLine(renderMedReferanser(expr.betingelse, nivå + 2, false))
                appendLine("${indent}│  ├─ Så:")
                appendLine(renderMedReferanser(expr.såUttrykk, nivå + 2, false))
                appendLine("${indent}│  └─ Ellers:")
                append(renderMedReferanser(expr.ellersUttrykk, nivå + 2, true))
            }
            is Grunnlag -> buildString {
                val hash = expr.strukturellHash()
                val info = seenSubtrees[hash]!!
                val refMarker = if (info.occurrenceCount > 1) " [${info.referenceId}]" else ""
                appendLine("${indent}${prefix}Grunnlag(${expr.navn})$refMarker")
                append(renderMedReferanser(expr.uttrykk, nivå + 1, true))
            }
            is Feil<*> -> "$indent$prefix Feil(${expr.melding})"
            is Tabell<*> -> buildString {
                appendLine("${indent}${prefix}Tabell${if (expr.navn != null) "(${expr.navn})" else ""}")
                expr.regler.forEachIndexed { index, regel ->
                    appendLine("${indent}│  ├─ Regel ${index + 1}:")
                    appendLine("${indent}│  │  ├─ Når:")
                    appendLine(renderMedReferanser(regel.betingelse, nivå + 3, false))
                    appendLine("${indent}│  │  └─ Resultat:")
                    append(renderMedReferanser(regel.resultat, nivå + 3, true))
                    if (index < expr.regler.size - 1) appendLine()
                }
                expr.ellersUttrykk?.let {
                    appendLine()
                    appendLine("${indent}│  └─ Ellers:")
                    append(renderMedReferanser(it, nivå + 2, true))
                }
            }
            is Memo<*> -> buildString {
                appendLine("${indent}${prefix}Memo (cached)")
                append(renderMedReferanser(expr.uttrykk, nivå + 1, true))
            }
            else -> "$indent$prefix${expr::class.simpleName}(...)"
        }
    }

    val hovedtre = renderMedReferanser(this, nivå)

    // Pass 3: Generer appendix for duplikater
    val duplikater = seenSubtrees.values
        .filter { it.occurrenceCount > 1 }
        .sortedBy { it.referenceId }

    return if (duplikater.isEmpty()) {
        hovedtre
    } else {
        buildString {
            append(hovedtre)
            appendLine()
            appendLine()
            appendLine("═══ Appendix - Strukturelt Dedupliserte Uttrykk ═══")

            duplikater.forEach { info ->
                appendLine()
                val navn = if (info.firstOccurrence is Grunnlag)
                    (info.firstOccurrence as Grunnlag<*>).navn
                else
                    info.firstOccurrence::class.simpleName
                appendLine("[${info.referenceId}] $navn (${info.occurrenceCount} forekomster):")

                // Reset shownHashes for appendix rendering
                val appendixShownHashes = mutableSetOf<String>()
                fun renderAppendix(expr: Uttrykk<*>, nivå: Int, erSiste: Boolean = false): String {
                    val indent = "│  ".repeat(nivå)
                    val prefix = if (nivå == 0) "" else if (erSiste) "└─ " else "├─ "

                    // Bruk samme dedup-logikk i appendix, men kun for Grunnlag-noder
                    if (expr is Grunnlag) {
                        val hash = expr.strukturellHash()
                        val subInfo = seenSubtrees[hash]!!

                        if (subInfo.occurrenceCount > 1 && hash in appendixShownHashes && hash != info.hash) {
                            return "$indent$prefix${expr.navn} → [${subInfo.referenceId}]"
                        }

                        appendixShownHashes.add(hash)
                    }

                    // Simplified rendering for appendix - just show structure
                    return expr.treVisning(nivå)
                }

                append(renderAppendix(info.firstOccurrence, 1))
            }
        }
    }
}
