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
            // Hvis Grunnlag bare inneholder en Const, er det en konstant - ikke en formel
            // Hopp over forklaring for rene konstanter
            if (this.utpakk() is Const) {
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
 * Finner alle konstante Grunnlag-verdier i uttrykkstre.
 */
private fun <T : Any> Uttrykk<T>.finnKonstanteGrunnlag(): List<Pair<String, Any>> {
    return when (this) {
        is Grunnlag -> {
            if (this.utpakk() is Const) {
                // Dette er en konstant verdi
                listOf(this.navn to this.evaluer())
            } else {
                // Søk videre i det underliggende uttrykket
                this.utpakk().finnKonstanteGrunnlag()
            }
        }
        is Add -> venstre.finnKonstanteGrunnlag() + høyre.finnKonstanteGrunnlag()
        is Sub -> venstre.finnKonstanteGrunnlag() + høyre.finnKonstanteGrunnlag()
        is Mul -> venstre.finnKonstanteGrunnlag() + høyre.finnKonstanteGrunnlag()
        is Div -> venstre.finnKonstanteGrunnlag() + høyre.finnKonstanteGrunnlag()
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
        else -> emptyList()
    }
}

/**
 * Genererer detaljert forklaring med alle faktum.
 * Kan valgfritt inkludere rvsId for hvert uttrykk.
 */
fun <T : Any> Uttrykk<T>.forklarDetaljert(navn: String, maxDybde: Int = 3, inkluderRvsId: Boolean = true): String {
    val forklaring = this.forklar(navn, maxDybde)

    // Hent rvsId fra hoveduttrykket hvis det er Grunnlag
    val hovedRvsId = if (inkluderRvsId && this is Grunnlag) this.rvsId else null

    return buildString {
        appendLine("HVORDAN")

        // Vis rvsId hvis det finnes
        if (hovedRvsId != null) {
            appendLine("    $hovedRvsId")
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

            // Finn rvsId for dette subuttryket hvis det eksisterer
            if (inkluderRvsId) {
                val subRvsId = this@forklarDetaljert.finnRvsIdFor(sub.hvaForklaring.navn)
                if (subRvsId != null) {
                    appendLine("    $subRvsId")
                }
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
            .distinctBy { it.first }  // Unike navn
        if (konstanteGrunnlag.isNotEmpty()) {
            appendLine()
            konstanteGrunnlag.forEach { (navn, verdi) ->
                appendLine("    $navn = $verdi")
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
                Add<Number>(v as Uttrykk<out Number>, h as Uttrykk<out Number>) as Uttrykk<T>
            }
        }

        is Sub<*> -> {
            val v = venstre.forenkel()
            val h = høyre.forenkel()
            if (v is Const && h is Const) {
                Const(evaluer()) as Uttrykk<T>
            } else {
                Sub<Number>(v as Uttrykk<out Number>, h as Uttrykk<out Number>) as Uttrykk<T>
            }
        }

        is Mul<*> -> {
            val v = venstre.forenkel()
            val h = høyre.forenkel()
            if (v is Const && h is Const) {
                Const(evaluer()) as Uttrykk<T>
            } else {
                Mul<Number>(v as Uttrykk<out Number>, h as Uttrykk<out Number>) as Uttrykk<T>
            }
        }

        is Div -> {
            val v = venstre.forenkel()
            val h = høyre.forenkel()
            if (v is Const && h is Const) {
                Const(evaluer()) as Uttrykk<T>
            } else {
                Div(v as Uttrykk<out Number>, h as Uttrykk<out Number>) as Uttrykk<T>
            }
        }

        is Min -> {
            val v = venstre.forenkel()
            val h = høyre.forenkel()
            if (v is Const && h is Const) {
                Const(evaluer()) as Uttrykk<T>
            } else {
                Min(v as Uttrykk<out Number>, h as Uttrykk<out Number>) as Uttrykk<T>
            }
        }

        is Neg<*> -> {
            val u = uttrykk.forenkel()
            if (u is Const) {
                Const(-u.verdi.toDouble()) as Uttrykk<T>
            } else {
                Neg<Number>(u as Uttrykk<out Number>) as Uttrykk<T>
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
            if (bet is Const && bet.verdi is Boolean) {
                if (bet.verdi) so as Uttrykk<T> else els as Uttrykk<T>
            } else {
                Hvis(bet as Uttrykk<Boolean>, so, els) as Uttrykk<T>
            }
        }

        is Grunnlag -> Grunnlag(navn, uttrykk.forenkel(), rvsId)
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
fun <T : Any> Uttrykk<T>.erstatt(variabelNavn: String, med: () -> Uttrykk<out Any>): Uttrykk<T> {
    @Suppress("UNCHECKED_CAST")
    return when (this) {
        is Const -> this
        is Add<*> -> {
            val v = venstre.erstatt(variabelNavn, med)
            val h = høyre.erstatt(variabelNavn, med)
            Add<Number>(v as Uttrykk<out Number>, h as Uttrykk<out Number>) as Uttrykk<T>
        }
        is Sub<*> -> {
            val v = venstre.erstatt(variabelNavn, med)
            val h = høyre.erstatt(variabelNavn, med)
            Sub<Number>(v as Uttrykk<out Number>, h as Uttrykk<out Number>) as Uttrykk<T>
        }
        is Mul<*> -> {
            val v = venstre.erstatt(variabelNavn, med)
            val h = høyre.erstatt(variabelNavn, med)
            Mul<Number>(v as Uttrykk<out Number>, h as Uttrykk<out Number>) as Uttrykk<T>
        }
        is Div -> {
            val v = venstre.erstatt(variabelNavn, med)
            val h = høyre.erstatt(variabelNavn, med)
            Div(v as Uttrykk<out Number>, h as Uttrykk<out Number>) as Uttrykk<T>
        }
        is Min -> {
            val v = venstre.erstatt(variabelNavn, med)
            val h = høyre.erstatt(variabelNavn, med)
            Min(v as Uttrykk<out Number>, h as Uttrykk<out Number>) as Uttrykk<T>
        }
        is Neg<*> -> {
            val u = uttrykk.erstatt(variabelNavn, med)
            Neg<Number>(u as Uttrykk<out Number>) as Uttrykk<T>
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
        else -> null
    }
}
