package no.nav.system.rdsl2

/**
 * Skriver ut forklaringen av et faktum som tekst, ved å nøste gjennom [Faktum.uttrykk]
 * (Utregning/Betingelser) og [Faktum.regel].
 *
 * ```
 * slitertillegg = 926.72
 *   Utregning    avrund2(avrund2(G * 0.25 / 12) * justeringsFaktor * trygdetid / fullTrygdetid)
 *                avrund2(avrund2(118620 * 0.25 / 12) * 0.5 * 30 / 40)
 *   └─ G = 118620
 *   └─ justeringsFaktor = 0.5
 *        Utregning    (36 - antallMnd) / 36
 *                     (36 - 18) / 36
 *        └─ antallMnd = 18
 *        Regel        UTTAK-TIDLIG = ja
 *                       Betingelser  antallMnd < 36
 *                                    18 < 36
 *                       └─ antallMnd = 18
 *   └─ trygdetid = 30
 *   └─ fullTrygdetid = 40
 *   Regel        AVKORTING-TRYGDETID = ja
 * ```
 */
fun Faktum<*>.forklar(): String = buildString {
    appendLine(this@forklar)
    detaljer(this@forklar, "  ")
}

private const val BREDDE = 13

private fun StringBuilder.detaljer(faktum: Faktum<*>, innrykk: String) {
    val uttrykk = faktum.uttrykk
    if (uttrykk !is Konstant) {
        val etikett = if (faktum.verdi is Boolean) "Betingelser" else "Utregning"
        appendLine("$innrykk${etikett.padEnd(BREDDE)}${uttrykk.notasjon()}")
        if (uttrykk.konkret() != uttrykk.notasjon()) {
            appendLine("$innrykk${" ".repeat(BREDDE)}${uttrykk.konkret()}")
        }
        uttrykk.grunnlag().forEach {
            appendLine("$innrykk└─ $it")
            detaljer(it, "$innrykk     ")
        }
    }
    faktum.regel?.let {
        appendLine("$innrykk${"Regel".padEnd(BREDDE)}$it")
        detaljer(it, "$innrykk${" ".repeat(BREDDE + 2)}")
    }
}
