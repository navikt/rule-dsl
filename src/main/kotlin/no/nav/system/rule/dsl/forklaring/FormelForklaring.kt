package no.nav.system.rule.dsl.forklaring

import no.nav.system.rule.dsl.formel.Formel

/**
 * Extension functions for å generere forklaringer fra Formel.
 *
 * ## Eksempel
 * ```kotlin
 * val G = Formel.variable("G", 110000)
 * val fulltSlitertillegg = FormelBuilder.create<Double>()
 *     .name("fulltSlitertillegg")
 *     .expression(0.25 * G / 12)
 *     .build()
 *
 * // Generer forklaring
 * val forklaring = fulltSlitertillegg.forklar()
 *
 * // Output:
 * // fulltSlitertillegg = 0.25 * G / 12
 * // fulltSlitertillegg = 0.25 * 110000 / 12
 * // fulltSlitertillegg = 2291.67
 * //
 * // HVORDAN
 * //     G = 110000
 * ```
 */

/**
 * Genererer en komplett forklaring av formelen.
 *
 * @param maxDybde maksimal dybde for rekursjon i subformler (default: 3)
 * @return HvordanForklaring med alle detaljer
 */
fun <T : Number> Formel<T>.forklar(maxDybde: Int = 3): HvordanForklaring {
    return forklarHvordan(nivå = 0, maxDybde = maxDybde)
}

/**
 * Genererer "HVA" forklaring - viser resultatet med symbolsk og konkret uttrykk.
 */
fun <T : Number> Formel<T>.forklarHva(nivå: Int = 0): HvaForklaring {
    return HvaForklaring(
        navn = this.name,
        symbolskUttrykk = this.notasjon,
        konkretUttrykk = this.innhold,
        resultat = this.value,
        nivå = nivå
    )
}

/**
 * Genererer "HVORDAN" forklaring - dekomponerer formelen i subformler.
 *
 * @param nivå indentering nivå (default: 0)
 * @param maxDybde maksimal dybde for rekursjon (default: 3)
 */
fun <T : Number> Formel<T>.forklarHvordan(
    nivå: Int = 0,
    maxDybde: Int = 3
): HvordanForklaring {
    val hva = this.forklarHva(nivå)

    // Hvis formelen er locked, vis den som atomisk enhet
    val subformler = if (!this.locked && nivå < maxDybde && this.subFormelList.isNotEmpty()) {
        this.subFormelList.map { subformel ->
            subformel.forklarHvordan(nivå = nivå, maxDybde = maxDybde)
        }
    } else {
        emptyList()
    }

    return HvordanForklaring(
        hvaForklaring = hva,
        subformler = subformler,
        hvorfor = null,
        nivå = nivå
    )
}

/**
 * Genererer en kompakt tekstforklaring.
 *
 * ## Eksempel output:
 * ```
 * slitertillegg = fulltSlitertillegg * justeringsFaktor * trygdetidFaktor
 * slitertillegg = 2292 * 0.33 * 0.5
 * slitertillegg = 378
 * ```
 */
fun <T : Number> Formel<T>.forklarKompakt(): String {
    return buildString {
        appendLine("${this@forklarKompakt.name} = ${this@forklarKompakt.notasjon}")
        appendLine("${this@forklarKompakt.name} = ${this@forklarKompakt.innhold}")
        appendLine("${this@forklarKompakt.name} = ${this@forklarKompakt.value}")
    }
}

/**
 * Genererer en detaljert forklaring med alle subformler.
 *
 * ## Eksempel output:
 * ```
 * HVORDAN
 *     slitertillegg = fulltSlitertillegg * justeringsFaktor * trygdetidFaktor
 *     slitertillegg = 2292 * 0.33 * 0.5
 *     slitertillegg = 378
 *
 *     fulltSlitertillegg = 0.25 * G / 12
 *     fulltSlitertillegg = 0.25 * 110000 / 12
 *     fulltSlitertillegg = 2292
 *
 *     justeringsFaktor = (MND_36 - antallMånederEtterNedreAldersgrense) / MND_36
 *     justeringsFaktor = (36 - 24) / 36
 *     justeringsFaktor = 0.33
 *
 *     trygdetidFaktor = faktiskTrygdetid / FULL_TRYGDETID
 *     trygdetidFaktor = 20 / 40
 *     trygdetidFaktor = 0.5
 * ```
 */
fun <T : Number> Formel<T>.forklarDetaljert(maxDybde: Int = 3): String {
    return this.forklar(maxDybde).toText()
}

/**
 * Generer HTML-forklaring for web-visning.
 */
fun <T : Number> Formel<T>.forklarHTML(maxDybde: Int = 3): String {
    return this.forklar(maxDybde).toHTML()
}

/**
 * Hjelpemetode for å liste alle variable i formelen.
 * Nyttig for debugging og forståelse.
 */
fun <T : Number> Formel<T>.variabelOversikt(): String {
    return buildString {
        appendLine("Variabler i ${this@variabelOversikt.name}:")
        this@variabelOversikt.namedVarMap.forEach { (navn, verdi) ->
            appendLine("  $navn = $verdi")
        }
    }
}

/**
 * Hjelpemetode for å vise formelstruktur som tre.
 * Viser hierarkiet av subformler.
 */
fun <T : Number> Formel<T>.strukturTre(nivå: Int = 0): String {
    val indent = "  ".repeat(nivå)
    return buildString {
        appendLine("$indent├─ ${this@strukturTre.name} = ${this@strukturTre.value}")
        if (this@strukturTre.locked) {
            appendLine("$indent│  (locked)")
        }
        this@strukturTre.subFormelList.forEach { subformel ->
            append(subformel.strukturTre(nivå + 1))
        }
    }
}
