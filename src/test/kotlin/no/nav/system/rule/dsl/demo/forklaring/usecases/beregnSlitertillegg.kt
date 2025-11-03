package no.nav.system.rule.dsl.demo.forklaring.usecases

import no.nav.system.rule.dsl.forklaring.*

fun main() {


    println("Slitertillegg demo ")
    println("-".repeat(80))

    listOf(
        Pair(
            fullTrygdetid().navngi("faktiskTrygdetid"),
            Grunnlag("antallMånederEtterNedreAldersgrense", Const(0))
        ),

        Pair(
            (fullTrygdetid() intdiv 2).navngi("faktiskTrygdetid"),
            (uttaksgrense() intdiv 2).navngi("antallMånederEtterNedreAldersgrense")
        ),
        Pair(
            Grunnlag("faktiskTrygdetid", Const(0)),
            (uttaksgrense() intdiv 2).navngi("antallMånederEtterNedreAldersgrense")

        ),
        Pair(
            (fullTrygdetid() intdiv 2).navngi("faktiskTrygdetid"),
            uttaksgrense().navngi("antallMånederEtterNedreAldersgrense")
        ),
    )
        .forEach { par ->
            println()
            println("Grunnlag:\n${par.first.navn} = ${par.first.evaluer()}\n${par.second.navn} = ${par.second.evaluer()}")
            beregnSlitertillegg(
                faktiskTrygdetid = par.first,
                antallMåneder = par.second
            ).also { slitertillegg ->
                println()
                println("Detaljert forklaring: ${slitertillegg.navn}")
                println(slitertillegg.forklarDetaljert(slitertillegg.navn, maxDybde = 3))

                //    println()
                //    println("Strukturtre:")
                //    println(slitertillegg.treVisning())
                //
                //    // Print call trace på slutten
                //    CallTracker.printTrace()
            }
        }
}

fun beregnSlitertillegg(
    faktiskTrygdetid: Grunnlag<Int>,
    antallMåneder: Grunnlag<Int>,
) = tracked {
    (fulltSlitertillegg() * trygdetidFaktor(faktiskTrygdetid) * justeringsFaktorUttak(antallMåneder))
        .navngi("slitertillegg")
        .id("SLITERTILEGG-BEREGNET")
}

fun G() = tracked { Grunnlag("G", Const(110000)) }
fun fullTrygdetid() = tracked { Grunnlag("FULL_TRYGDETID", Const(40)) }
fun uttaksgrense() = tracked { Grunnlag("UTTAKSGRENSE_MND", Const(36)) }

fun trygdetidFaktor(faktiskTrygdetid: Grunnlag<Int>) = tracked {
    fullTrygdetid().let { fullTrygdetid ->
        (min(faktiskTrygdetid, fullTrygdetid) / fullTrygdetid)
            .navngi("trygdetidFaktor")
            .id("SLITERTILLEGG-AVKORTING-TRYGDETID")
    }
}

fun justeringsFaktorUttak(antallMåneder: Grunnlag<Int>) = tracked {
    uttaksgrense().let { mnd_36 ->
        ((mnd_36 - min(antallMåneder,mnd_36)) / mnd_36)
            .navngi("justeringsFaktorUttak")
            .id("SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT")
    }
}

fun fulltSlitertillegg() = tracked {
    (0.25 * G() / 12)
        .navngi("fulltSlitertillegg")
        .id("SLITERTILLEGG-BEREGNING-UAVKORTET")
}


