package no.nav.system.prototype.regelsett

/**
 * Forklarer et faktum og nøster nedover til faktumene det bygger på.
 *
 * ```
 * slitertillegg = 1647.5
 *   formel: (fulltSlitertillegg * justeringsFaktor) * trygdetidsbrøk
 *   beregning: (2471.25 * 0.6666666666666666) * 1.0
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
    val erRegel = this@forklar is Regel
    val regel: Regel? = if (erRegel) this@forklar as Regel else regel.takeIf { it !== DefaultRegel }

    val linjer = buildList {
        if (!erRegel && uttrykk !is Konstant<*>) {
            add("formel: ${uttrykk.notasjon()}")
            add("beregning: ${uttrykk.konkret()}")
        }
        if (regel != null) {
            if (!erRegel) add("regel: ${regel.navn}")
            if (regel.harFunksjonellInnhold) {
                add("betingelse: ${regel.uttrykk.notasjon()}")
                add("vurdering: ${regel.uttrykk.konkret()}")
            }
            regel.referanser.forEach { add("referanse: ${it.id} ${it.url}") }
        }
    }

    append("$innrykk$navn = $verdi")
    linjer.forEach { append("\n$innrykk  $it") }

    val under = buildList {
        if (!erRegel) addAll(uttrykk.grunnlag())
        if (regel != null && regel.harFunksjonellInnhold) addAll(regel.uttrykk.grunnlag())
    }.filter { it.harNoeÅForklare() }.distinct()
    for (f in under) append("\n").append(f.forklar("$innrykk    "))
}

/**
 * Navn og verdi står allerede i `formel:`/`beregning:` til den som bruker faktumet. Det får
 * derfor bare egen linje hvis det har mer å fortelle: en formel, en regel med funksjonelle
 * betingelser, eller en referanse.
 */
private fun Faktum<*>.harNoeÅForklare(): Boolean {
    val regel = if (this is Regel) this else regel
    return (this !is Regel && uttrykk !is Konstant<*>) ||
        (regel !== DefaultRegel && regel.harFunksjonellInnhold) ||
        regel.referanser.isNotEmpty()
}
