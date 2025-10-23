package no.nav.system.rule.dsl.demo.forklaring

import no.nav.system.rule.dsl.forklaring.*
import no.nav.system.rule.dsl.formel.*
import no.nav.system.rule.dsl.rettsregel.Faktum
import no.nav.system.rule.dsl.rettsregel.erMindreEnn

/**
 * Demonstrasjon av regelsporing med Forklaring API.
 *
 * Viser hvordan Faktum og Formel kan gi strukturerte forklaringer
 * som besvarer HVA, HVORFOR og HVORDAN.
 */

fun main() {
    println("=".repeat(80))
    println("REGELSPORING DEMO - Slitertillegg Beregning")
    println("=".repeat(80))
    println()

    demonstrerEnkelFormel()
    println("\n" + "=".repeat(80) + "\n")

    demonstrerSubsumtion()
    println("\n" + "=".repeat(80) + "\n")

    demonstrerKompleksBeregning()
    println("\n" + "=".repeat(80) + "\n")

    demonstrerKomplettForklaring()
}

/**
 * Demonstrerer enkel formelforklaring.
 */
fun demonstrerEnkelFormel() {
    println("1. ENKEL FORMEL - Grunnbeløp beregning")
    println("-".repeat(80))

    val G = Formel.variable("G", 110000)
    val sats = Formel.variable("sats", 0.25)
    val måneder = Formel.variable("måneder", 12)

    val fulltSlitertillegg = FormelBuilder.create<Double>()
        .name("fulltSlitertillegg")
        .expression(sats * G / måneder)
        .build()

    // Kompakt forklaring
    println("Kompakt forklaring:")
    println(fulltSlitertillegg.forklarKompakt())

    println("\nDetaljert forklaring:")
    println(fulltSlitertillegg.forklarDetaljert())

    println("\nStrukturtre:")
    println(fulltSlitertillegg.strukturTre())
}

/**
 * Demonstrerer domain predicate forklaring (FORDI).
 */
fun demonstrerSubsumtion() {
    println("2. DOMAIN PREDICATE - Betingelsesforklaring")
    println("-".repeat(80))

    val antallMåneder = Faktum("antallMånederEtterNedreAldersgrense", 24)
    val grense = Faktum("MND_36", 36)

    val betingelse = antallMåneder erMindreEnn grense

    println("Evaluering: ${betingelse.fired}")
    println()

    val forklaring = betingelse.forklar()
    println(forklaring.toText())

    println("\nFaktum detaljer:")
    println(antallMåneder.forklar())
    println(grense.forklar())
}

/**
 * Demonstrerer kompleks beregning med nestede formler.
 */
fun demonstrerKompleksBeregning() {
    println("3. KOMPLEKS BEREGNING - Slitertillegg med justeringer")
    println("-".repeat(80))

    // Grunnverdier
    val G = Formel.variable("G", 110000)
    val FULL_TRYGDETID = Formel.variable("FULL_TRYGDETID", 40)
    val MND_36 = Formel.variable("MND_36", 36)

    // Input verdier
    val faktiskTrygdetid = Formel.variable("faktiskTrygdetid", 20)
    val antallMånederEtterNedreAldersgrense = Formel.variable(
        "antallMånederEtterNedreAldersgrense",
        24
    )

    // Subberegninger (locked formler)
    val fulltSlitertillegg = FormelBuilder.create<Double>()
        .name("fulltSlitertillegg")
        .expression(0.25 * G / 12)
        .locked()
        .build()

    val trygdetidFaktor = FormelBuilder.create<Double>()
        .name("trygdetidFaktor")
        .expression(faktiskTrygdetid / FULL_TRYGDETID)
        .locked()
        .build()

    val justeringsFaktor = FormelBuilder.create<Double>()
        .name("justeringsFaktor")
        .expression((MND_36 - antallMånederEtterNedreAldersgrense) / MND_36)
        .locked()
        .build()

    // Hovedberegning
    val slitertillegg = FormelBuilder.create<Double>()
        .name("slitertillegg")
        .expression(fulltSlitertillegg * justeringsFaktor * trygdetidFaktor)
        .build()

    println("KOMPLETT BEREGNING:")
    println(slitertillegg.forklarDetaljert(maxDybde = 5))
}

/**
 * Demonstrerer komplett regelforklaring som kombinerer HVA, HVORFOR og HVORDAN.
 */
fun demonstrerKomplettForklaring() {
    println("4. KOMPLETT FORKLARING - HVA + HVORFOR + HVORDAN")
    println("-".repeat(80))

    // Setup
    val G = Formel.variable("G", 110000)
    val faktiskTrygdetid = Formel.variable("faktiskTrygdetid", 20)
    val FULL_TRYGDETID = Formel.variable("FULL_TRYGDETID", 40)
    val MND_36 = Formel.variable("MND_36", 36)
    val antallMåneder = Formel.variable("antallMånederEtterNedreAldersgrense", 24)

    // Betingelse
    val betingelse = antallMåneder erMindreEnn MND_36

    // Beregning
    val justeringsFaktor = FormelBuilder.create<Double>()
        .name("justeringsFaktor")
        .expression((MND_36 - antallMåneder) / MND_36)
        .locked()
        .build()

    // Lag komplett forklaring
    val hva = justeringsFaktor.forklarHva()
    val hvorfor = betingelse.forklar()
    val hvordan = justeringsFaktor.forklarHvordan(maxDybde = 3)

    val komplettForklaring = KomplettForklaring(
        hva = hva,
        hvorfor = hvorfor,
        hvordan = hvordan,
        referanser = listOf("SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT")
    )

    println("TEXT FORMAT:")
    println(komplettForklaring.toText())

    println("\n\nHTML FORMAT:")
    println(komplettForklaring.toHTML())
}

/**
 * Hjelpefunksjon for å vise hvordan forklaringer kan integreres i faktisk regelkode.
 */
fun eksempelIntegrasjon() {
    // Dette viser hvordan forklaring kan genereres i en regel:
    //
    // regel("SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT") {
    //     HVIS { antallMånederEtterNedreAldersgrense erMindreEnn MND_36 }
    //     SÅ {
    //         justeringsFaktor = FormelBuilder.create<Double>()
    //             .name("justeringsFaktor")
    //             .expression((MND_36 - antallMånederEtterNedreAldersgrense) / MND_36)
    //             .build()
    //
    //         // Generer og lagre forklaring
    //         val forklaring = KomplettForklaring(
    //             hva = justeringsFaktor.forklarHva(),
    //             hvorfor = (antallMånederEtterNedreAldersgrense erMindreEnn MND_36).forklar(),
    //             hvordan = justeringsFaktor.forklarHvordan()
    //         )
    //
    //         // Lagre forklaring for senere visning
    //         lagreForklaring(forklaring)
    //     }
    // }
}
