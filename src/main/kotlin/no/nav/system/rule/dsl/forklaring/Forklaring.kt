package no.nav.system.rule.dsl.forklaring

/**
 * Hierarkisk forklaringsstruktur for regelsporing.
 *
 * Forklaring besvarer:
 * - **HVA**: Hva er resultatet? (konkrete verdier)
 * - **HVORFOR**: Hvorfor ble regelen aktivert? (subsumsjoner/betingelser)
 * - **HVORDAN**: Hvordan ble resultatet beregnet? (formler og subformler)
 *
 * ## Eksempel
 * ```
 * slitertillegg = fulltSlitertillegg * justeringsFaktor * trygdetidFaktor
 * slitertillegg = 2292 * 0.33 * 0.5
 * slitertillegg = 378
 *
 * FORDI
 *     antallMånederEtterNedreAldersgrense er mindre enn MND_36
 *     24 er mindre enn 36
 *
 *     FORDI
 *         antallMånederEtterNedreAldersgrense = 24
 *
 * HVORDAN
 *     fulltSlitertillegg = 0.25 * G / 12
 *     fulltSlitertillegg = 0.25 * 110000 / 12
 *     fulltSlitertillegg = 2292
 *     ...
 * ```
 */
sealed interface Forklaring {
    val nivå: Int
    fun toText(): String
    fun toHTML(): String
}

/**
 * Forklarer **hva** resultatet er.
 * Viser både symbolsk og konkret beregning.
 */
data class HvaForklaring(
    val navn: String,
    val symbolskUttrykk: String,
    val konkretUttrykk: String,
    val resultat: Any,
    override val nivå: Int = 0
) : Forklaring {
    override fun toText(): String = buildString {
        val indent = "    ".repeat(nivå)
        appendLine("${indent}$navn = $symbolskUttrykk")
        appendLine("${indent}$navn = $konkretUttrykk")
        appendLine("${indent}$navn = $resultat")
    }

    override fun toHTML(): String = buildString {
        val indent = "&nbsp;".repeat(nivå * 4)
        appendLine("$indent<span class='hva-navn'>$navn</span> = <span class='hva-symbolsk'>$symbolskUttrykk</span><br>")
        appendLine("$indent<span class='hva-navn'>$navn</span> = <span class='hva-konkret'>$konkretUttrykk</span><br>")
        appendLine("$indent<span class='hva-navn'>$navn</span> = <span class='hva-resultat'>$resultat</span><br>")
    }
}

/**
 * Forklarer **hvorfor** en regel ble aktivert.
 * Inneholder betingelser og underliggende forklaringer.
 */
data class HvorforForklaring(
    val betingelser: List<String> = emptyList(),
    val underliggende: List<HvorforForklaring> = emptyList(),
    override val nivå: Int = 0
) : Forklaring {
    override fun toText(): String = buildString {
        val indent = "    ".repeat(nivå)
        if (betingelser.isNotEmpty()) {
            appendLine("${indent}FORDI")
            betingelser.forEach { betingelse ->
                appendLine("${indent}    $betingelse")
            }
        }
        underliggende.forEach { forklaring ->
            append(forklaring.copy(nivå = nivå + 1).toText())
        }
    }

    override fun toHTML(): String = buildString {
        val indent = "&nbsp;".repeat(nivå * 4)
        if (betingelser.isNotEmpty()) {
            appendLine("$indent<div class='hvorfor'>")
            appendLine("$indent<strong>FORDI</strong><br>")
            betingelser.forEach { betingelse ->
                appendLine("$indent&nbsp;&nbsp;&nbsp;&nbsp;<span class='betingelse'>$betingelse</span><br>")
            }
            appendLine("$indent</div>")
        }
        underliggende.forEach { forklaring ->
            append(forklaring.copy(nivå = nivå + 1).toHTML())
        }
    }

    fun copy(nivå: Int) = HvorforForklaring(betingelser, underliggende, nivå)
}

/**
 * Forklarer **hvordan** en beregning ble gjort.
 * Inneholder formeldekomponering med subformler.
 */
data class HvordanForklaring(
    val hvaForklaring: HvaForklaring,
    val subformler: List<HvordanForklaring> = emptyList(),
    val hvorfor: HvorforForklaring? = null,
    override val nivå: Int = 0
) : Forklaring {
    override fun toText(): String = buildString {
        val indent = "    ".repeat(nivå)
        if (nivå == 0) appendLine("${indent}HVORDAN")
        append(hvaForklaring.copy(nivå = nivå).toText())

        hvorfor?.let {
            append(it.copy(nivå = nivå + 1).toText())
        }

        subformler.forEach { subformel ->
            appendLine() // blank line before subformula
            append(subformel.copy(nivå = nivå).toText())
        }
    }

    override fun toHTML(): String = buildString {
        val indent = "&nbsp;".repeat(nivå * 4)
        if (nivå == 0) appendLine("$indent<div class='hvordan'><strong>HVORDAN</strong><br>")
        append(hvaForklaring.copy(nivå = nivå).toHTML())

        hvorfor?.let {
            append(it.copy(nivå = nivå + 1).toHTML())
        }

        subformler.forEach { subformel ->
            appendLine("$indent<br>") // blank line before subformula
            append(subformel.copy(nivå = nivå).toHTML())
        }

        if (nivå == 0) appendLine("$indent</div>")
    }

    fun copy(nivå: Int) = HvordanForklaring(hvaForklaring, subformler, hvorfor, nivå)
}

/**
 * Komplett forklaring som kombinerer hva, hvorfor og hvordan.
 */
data class KomplettForklaring(
    val hva: HvaForklaring,
    val hvorfor: HvorforForklaring? = null,
    val hvordan: HvordanForklaring? = null,
    val referanser: List<String> = emptyList(),
    override val nivå: Int = 0
) : Forklaring {
    override fun toText(): String = buildString {
        append(hva.toText())

        if (referanser.isNotEmpty()) {
            appendLine()
            appendLine("REFERANSE")
            referanser.forEach { ref ->
                appendLine("    $ref")
            }
        }

        hvorfor?.let {
            appendLine()
            append(it.toText())
        }

        hvordan?.let {
            appendLine()
            append(it.toText())
        }
    }

    override fun toHTML(): String = buildString {
        appendLine("<div class='komplett-forklaring'>")
        append(hva.toHTML())

        if (referanser.isNotEmpty()) {
            appendLine("<div class='referanser'>")
            appendLine("<strong>REFERANSE</strong><br>")
            referanser.forEach { ref ->
                appendLine("&nbsp;&nbsp;&nbsp;&nbsp;<span class='referanse'>$ref</span><br>")
            }
            appendLine("</div>")
        }

        hvorfor?.let {
            append(it.toHTML())
        }

        hvordan?.let {
            append(it.toHTML())
        }

        appendLine("</div>")
    }
}
