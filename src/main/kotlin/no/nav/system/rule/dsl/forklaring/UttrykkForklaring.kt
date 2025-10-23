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
    val hva = HvaForklaring(
        navn = navn,
        symbolskUttrykk = this.notasjon(),
        konkretUttrykk = this.konkret(),
        resultat = this.evaluer()
    )

    // For navngitte underuttrykk, lag subforklaringer
    val subforklaringer = this.finnNavngitteUttrykk(0, maxDybde)

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
        is Navngitt -> {
            // Dette er en navngitt subformel - lag forklaring for den
            val hva = this.utpakk().forklarHva(this.navn)
            val subSub = this.utpakk().finnNavngitteUttrykk(nivå + 1, maxDybde)

            listOf(
                HvordanForklaring(
                    hvaForklaring = hva,
                    subformler = subSub,
                    hvorfor = null,
                    nivå = nivå
                )
            )
        }

        is Add -> this.venstre.finnNavngitteUttrykk(nivå, maxDybde) +
                this.høyre.finnNavngitteUttrykk(nivå, maxDybde)

        is Sub -> this.venstre.finnNavngitteUttrykk(nivå, maxDybde) +
                this.høyre.finnNavngitteUttrykk(nivå, maxDybde)

        is Mul -> this.venstre.finnNavngitteUttrykk(nivå, maxDybde) +
                this.høyre.finnNavngitteUttrykk(nivå, maxDybde)

        is Div -> this.venstre.finnNavngitteUttrykk(nivå, maxDybde) +
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
 * Genererer detaljert forklaring med alle faktum.
 */
fun <T : Number> Uttrykk<T>.forklarDetaljert(navn: String, maxDybde: Int = 3): String {
    val forklaring = this.forklar(navn, maxDybde)
    return buildString {
        appendLine("HVORDAN")
        appendLine("    $navn = ${notasjon()}")
        appendLine("    $navn = ${konkret()}")
        appendLine("    $navn = ${evaluer()}")

        // Legg til navngitte subforklaringer
        forklaring.subformler.forEach { sub ->
            appendLine()
            appendLine("    ${sub.hvaForklaring.navn} = ${sub.hvaForklaring.symbolskUttrykk}")
            appendLine("    ${sub.hvaForklaring.navn} = ${sub.hvaForklaring.konkretUttrykk}")
            appendLine("    ${sub.hvaForklaring.navn} = ${sub.hvaForklaring.resultat}")
        }

        // Legg til faktum som ikke er i navngitte subforklaringer
        val faktum = this@forklarDetaljert.faktumListe()
        if (faktum.isNotEmpty()) {
            appendLine()
            faktum.forEach { f ->
                if (!f.anonymous) {
                    appendLine("    ${f.name} = ${f.value}")
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
fun <T : Number> Uttrykk<T>.treVisning(nivå: Int = 0): String {
    val indent = "│  ".repeat(nivå)
    val prefix = if (nivå == 0) "" else "├─ "

    return when (this) {
        is Var -> "$indent$prefix Var(${faktum.name})"
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
        is Neg -> buildString {
            appendLine("${indent}${prefix}Neg")
            append(uttrykk.treVisning(nivå + 1))
        }
        is Navngitt -> buildString {
            appendLine("${indent}${prefix}Navngitt($navn)")
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
        is Neg -> uttrykk.visit(transform)
        is Navngitt -> uttrykk.visit(transform)
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
        is Var, is Const -> this

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

        is Neg -> {
            val u = uttrykk.forenkel()
            if (u is Const) {
                Const(-u.verdi.toDouble()) as Uttrykk<T>
            } else {
                Neg(u)
            }
        }

        is Navngitt -> Navngitt(navn, uttrykk.forenkel())
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
        is Var -> if (faktum.name == variabelNavn) med() as Uttrykk<T> else this
        is Const -> this
        is Add -> Add<T>(venstre.erstatt(variabelNavn, med), høyre.erstatt(variabelNavn, med))
        is Sub -> Sub<T>(venstre.erstatt(variabelNavn, med), høyre.erstatt(variabelNavn, med))
        is Mul -> Mul<T>(venstre.erstatt(variabelNavn, med), høyre.erstatt(variabelNavn, med))
        is Div -> Div(venstre.erstatt(variabelNavn, med), høyre.erstatt(variabelNavn, med)) as Uttrykk<T>
        is Neg -> Neg(uttrykk.erstatt(variabelNavn, med))
        is Navngitt -> Navngitt(navn, uttrykk.erstatt(variabelNavn, med))
    }
}
