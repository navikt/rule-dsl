package no.nav.pensjon.sliterordning

import no.nav.system.rule.dsl.forklaring.*

fun beregnSlitertillegg(
    faktiskTrygdetid: Grunnlag<Int>,
    antallMåneder: Grunnlag<Int>,
): Grunnlag<Double> =
    (fulltSlitertillegg() * justeringsFaktorUttak(antallMåneder) * trygdetidFaktor(faktiskTrygdetid))
        .navngi("slitertillegg")
        .id("SLITERTILEGG-BEREGNET")

fun G() = Grunnlag("G", Const(110000))
fun fullTrygdetid() = Grunnlag("FULL_TRYGDETID", Const(40))

fun trygdetidFaktor(faktiskTrygdetid: Grunnlag<Int>) =
    (faktiskTrygdetid / fullTrygdetid())
        .navngi("trygdetidFaktor")
        .id("SLITERTILLEGG-AVKORTING-TRYGDETID")

fun justeringsFaktorUttak(antallMåneder: Grunnlag<Int>) =
    Grunnlag("MND_36", Const(36)).let { MND_36 ->
        (antallMåneder erMindreEnn MND_36)
            .så { (MND_36 - antallMåneder) / MND_36 }
            .ellers { Const(0.0) }
        //((MND_36 - min(antallMåneder, MND_36)) / MND_36)
            .navngi("justeringsFaktorUttak")
            .id("SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT")
    }

fun fulltSlitertillegg() =
    (0.25 * G() / 12)
        .navngi("fulltSlitertillegg")
        .id("SLITERTILLEGG-BEREGNING-UAVKORTET")


