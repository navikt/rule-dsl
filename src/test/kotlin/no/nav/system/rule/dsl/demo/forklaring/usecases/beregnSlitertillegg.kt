package no.nav.system.rule.dsl.demo.forklaring.usecases

import no.nav.system.rule.dsl.forklaring.*

fun main() {
    val slitertillegg = beregnSlitertillegg(
        faktiskTrygdetid = Grunnlag("faktiskTrygdetid", Const(20)),
        antallMåneder = Grunnlag("antallMånederEtterNedreAldersgrense", Const(24))
    )

    println("4. NAVNGITTE UTTRYKK - ${slitertillegg.navn} med justeringer")
    println("-".repeat(80))

    println("Resultat: ${slitertillegg.navn}")
    println()
    println("Detaljert forklaring:")
    println(slitertillegg.forklarDetaljert(slitertillegg.navn, maxDybde = 3))

    println()
    println("Strukturtre:")
    println(slitertillegg.treVisning())

    // Print call trace på slutten
    CallTracker.printTrace()
}

fun beregnSlitertillegg(
    faktiskTrygdetid: Grunnlag<Int>,
    antallMåneder: Grunnlag<Int>,
) = tracked {
    (fulltSlitertillegg() * justeringsFaktorUttak(antallMåneder) * trygdetidFaktor(faktiskTrygdetid))
        .navngi("slitertillegg")
        .id("SLITERTILEGG-BEREGNET")
}

fun G() = tracked {  Grunnlag("G", Const(110000)) }
fun fullTrygdetid() = tracked {Grunnlag("FULL_TRYGDETID", Const(40)) }

fun trygdetidFaktor(faktiskTrygdetid: Grunnlag<Int>) = tracked {
    (faktiskTrygdetid / fullTrygdetid())
        .navngi("trygdetidFaktor")
        .id("SLITERTILLEGG-AVKORTING-TRYGDETID")
}

fun justeringsFaktorUttak(antallMåneder: Grunnlag<Int>) = tracked {
    Grunnlag("MND_36", Const(36)).let { MND_36 ->
        (antallMåneder erMindreEnn MND_36)
            .så { (MND_36 - antallMåneder) / MND_36 }
            .ellers { Const(0.0) }
            //((MND_36 - min(antallMåneder, MND_36)) / MND_36)
            .navngi("justeringsFaktorUttak")
            .id("SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT")
    }
}

fun fulltSlitertillegg() = tracked {
    (0.25 * G() / 12)
        .navngi("fulltSlitertillegg")
        .id("SLITERTILLEGG-BEREGNING-UAVKORTET")
}


