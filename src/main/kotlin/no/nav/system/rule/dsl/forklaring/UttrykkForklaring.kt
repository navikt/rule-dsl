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
fun <T : Number> Uttrykk<T>.forklar(navn: String, maxDybde: Int = 3): HvordanForklaring {
    // Hvis dette er et Grunnlag uttrykk, bruk det underliggende for notasjon og konkret
    val uttrykkForVisning = if (this is Grunnlag) this.utpakk() else this

    val hva = HvaForklaring(
        navn = navn,
        symbolskUttrykk = uttrykkForVisning.notasjon(),
        konkretUttrykk = uttrykkForVisning.konkret(),
        resultat = this.evaluer()
    )

    // For navngitte underuttrykk, lag subforklaringer
    val subforklaringer = uttrykkForVisning.finnNavngitteUttrykk(0, maxDybde)

    return HvordanForklaring(
        hvaForklaring = hva,
        subformler = subforklaringer,
        hvorfor = null
    )
}

/**
 * Genererer HvaForklaring for uttrykket.
 */
fun <T : Number> Uttrykk<T>.forklarHva(navn: String): HvaForklaring {
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
private fun <T : Number> Uttrykk<T>.finnNavngitteUttrykk(
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

        else -> emptyList()
    }
}

/**
 * Genererer kompakt 3-linjers forklaring.
 */
fun <T : Number> Uttrykk<T>.forklarKompakt(navn: String): String {
    return buildString {
        appendLine("$navn = ${notasjon()}")
        appendLine("$navn = ${konkret()}")
        appendLine("$navn = ${evaluer()}")
    }
}

/**
 * Finner alle konstante Grunnlag-verdier i uttrykkstre.
 */
private fun <T : Number> Uttrykk<T>.finnKonstanteGrunnlag(): List<Pair<String, Number>> {
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
        else -> emptyList()
    }
}

/**
 * Genererer detaljert forklaring med alle faktum.
 * Kan valgfritt inkludere rvsId for hvert uttrykk.
 */
fun <T : Number> Uttrykk<T>.forklarDetaljert(navn: String, maxDybde: Int = 3, inkluderRvsId: Boolean = true): String {
    val forklaring = this.forklar(navn, maxDybde)

    // Hent rvsId fra hoveduttrykket hvis det er Grunnlag
    val hovedRvsId = if (inkluderRvsId && this is Grunnlag) this.rvsId else null

    return buildString {
        appendLine("HVORDAN")

        // Vis rvsId hvis det finnes
        if (hovedRvsId != null) {
            appendLine("    $hovedRvsId")
        }

        appendLine("    ${forklaring.hvaForklaring.navn} = ${forklaring.hvaForklaring.symbolskUttrykk}")
        appendLine("    ${forklaring.hvaForklaring.navn} = ${forklaring.hvaForklaring.konkretUttrykk}")
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

            appendLine("    ${sub.hvaForklaring.navn} = ${sub.hvaForklaring.symbolskUttrykk}")
            appendLine("    ${sub.hvaForklaring.navn} = ${sub.hvaForklaring.konkretUttrykk}")
            appendLine("    ${sub.hvaForklaring.navn} = ${sub.hvaForklaring.resultat}")
        }

        // Legg til faktum som ikke er i navngitte subforklaringer
        val faktum = this@forklarDetaljert.faktumListe()
        if (faktum.isNotEmpty()) {
            appendLine()
            faktum.toSet().forEach { f ->
                if (!f.anonymous) {
                    appendLine("    ${f.name} = ${f.value}")
                }
            }
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
fun <T : Number> Uttrykk<T>.treVisning(nivå: Int = 0): String {
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
fun <T : Number, R> Uttrykk<T>.visit(transform: (Uttrykk<*>) -> List<R>): List<R> {
    val current = transform(this)

    val children = when (this) {
        is Add -> venstre.visit(transform) + høyre.visit(transform)
        is Sub -> venstre.visit(transform) + høyre.visit(transform)
        is Mul -> venstre.visit(transform) + høyre.visit(transform)
        is Div -> venstre.visit(transform) + høyre.visit(transform)
        is Min -> venstre.visit(transform) + høyre.visit(transform)
        is Neg -> uttrykk.visit(transform)
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
fun <T : Number> Uttrykk<T>.forenkel(): Uttrykk<T> {
    @Suppress("UNCHECKED_CAST")
    return when (this) {
        // is Var,
        is Const -> this

        is Add -> {
            val v = venstre.forenkel()
            val h = høyre.forenkel()
            if (v is Const && h is Const) {
                Const(evaluer()) as Uttrykk<T>
            } else {
                Add<T>(v, h)
            }
        }

        is Sub -> {
            val v = venstre.forenkel()
            val h = høyre.forenkel()
            if (v is Const && h is Const) {
                Const(evaluer()) as Uttrykk<T>
            } else {
                Sub<T>(v, h)
            }
        }

        is Mul -> {
            val v = venstre.forenkel()
            val h = høyre.forenkel()
            if (v is Const && h is Const) {
                Const(evaluer()) as Uttrykk<T>
            } else {
                Mul<T>(v, h)
            }
        }

        is Div -> {
            val v = venstre.forenkel()
            val h = høyre.forenkel()
            if (v is Const && h is Const) {
                Const(evaluer()) as Uttrykk<T>
            } else {
                Div(v, h) as Uttrykk<T>
            }
        }

        is Min -> {
            val v = venstre.forenkel()
            val h = høyre.forenkel()
            if (v is Const && h is Const) {
                Const(evaluer()) as Uttrykk<T>
            } else {
                Min(v, h) as Uttrykk<T>
            }
        }

        is Neg -> {
            val u = uttrykk.forenkel()
            if (u is Const) {
                Const(-u.verdi.toDouble()) as Uttrykk<T>
            } else {
                Neg(u)
            }
        }

        is Grunnlag -> Grunnlag(navn, uttrykk.forenkel())
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
fun <T : Number> Uttrykk<T>.erstatt(variabelNavn: String, med: () -> Uttrykk<out Number>): Uttrykk<T> {
    @Suppress("UNCHECKED_CAST")
    return when (this) {
        // is Var -> if (faktum.name == variabelNavn) med() as Uttrykk<T> else this
        is Const -> this
        is Add -> Add<T>(venstre.erstatt(variabelNavn, med), høyre.erstatt(variabelNavn, med))
        is Sub -> Sub<T>(venstre.erstatt(variabelNavn, med), høyre.erstatt(variabelNavn, med))
        is Mul -> Mul<T>(venstre.erstatt(variabelNavn, med), høyre.erstatt(variabelNavn, med))
        is Div -> Div(venstre.erstatt(variabelNavn, med), høyre.erstatt(variabelNavn, med)) as Uttrykk<T>
        is Min -> Min(venstre.erstatt(variabelNavn, med), høyre.erstatt(variabelNavn, med)) as Uttrykk<T>
        is Neg -> Neg(uttrykk.erstatt(variabelNavn, med))
        is Grunnlag -> Grunnlag(navn, uttrykk.erstatt(variabelNavn, med))
    }
}

/**
 * Finner rvsId for et navngitt uttrykk med gitt navn.
 * Traverserer uttrykkstre og returnerer rvsId til første Grunnlag med matchende navn.
 */
fun <T : Number> Uttrykk<T>.finnRvsIdFor(uttrykkNavn: String): String? {
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
        else -> null
    }
}
