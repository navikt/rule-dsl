package no.nav.system.prototype.regelsett

/**
 * Forklarer et faktum og nøster nedover til faktumene det bygger på.
 *
 * ```
 * slitertillegg = 1647.5
 *   formel: fulltSlitertillegg * justeringsFaktor * trygdetidsbrøk
 *   beregning: 2471.25 * 0.6666666666666666 * 1.0
 *   regel: BEREGN-SLITERTILLEGG
 *   referanse: §3-2 Folketrygdloven https://lovdata.no
 * ```
 *
 * - `betingelse`/`vurdering` vises bare for regler med funksjonelle betingelser.
 * - Faktum med [DefaultRegel] får ingen regel-linje.
 * - Faktum som bare er navn og verdi, får ikke egen linje. De står allerede i formelen.
 * - Hvert faktum vises én gang per nivå, selv om det brukes både i formel og betingelse.
 */
fun Faktum<*>.forklar(innrykk: String = ""): String = buildString {
    append("$innrykk$navn = $verdi")
    for (linje in linjer()) append("\n$innrykk  $linje")
    for (f in underliggende()) append("\n").append(f.forklar("$innrykk    "))
}

private fun Faktum<*>.linjer(): List<String> = when (this) {
    is Regel -> vurderingslinjer()
    else -> formellinjer() + regel.kildelinjer()
}

private fun Faktum<*>.formellinjer(): List<String> =
    if (uttrykk is Konstant<*>) emptyList()
    else listOf("formel: ${uttrykk.notasjon()}", "beregning: ${uttrykk.konkret()}")

internal fun Regel.vurderingslinjer(): List<String> = buildList {
    if (harFunksjonellInnhold) {
        add("betingelse: ${uttrykk.notasjon()}")
        add("vurdering: ${uttrykk.konkret()}")
    }
    referanser.forEach { add("referanse: ${it.id} ${it.url}") }
}

/** Faktum brukt i formelen eller i regelens betingelser, som har mer å fortelle. */
private fun Faktum<*>.underliggende(): List<Faktum<*>> =
    (uttrykk.grunnlag() + regel.uttrykk.grunnlag()).distinct().filter { it.harNoeÅForklare() }

/**
 * Navn og verdi står allerede i `formel:`/`beregning:` til den som bruker faktumet. Det får
 * derfor bare egen linje hvis det har en formel, funksjonelle betingelser eller en referanse.
 */
private fun Faktum<*>.harNoeÅForklare(): Boolean = when (this) {
    is Regel -> vurderingslinjer().isNotEmpty()
    else -> formellinjer().isNotEmpty() || regel.vurderingslinjer().isNotEmpty()
}
