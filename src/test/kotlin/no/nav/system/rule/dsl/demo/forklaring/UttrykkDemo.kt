package no.nav.system.rule.dsl.demo.forklaring

import no.nav.system.rule.dsl.forklaring.*
import no.nav.system.rule.dsl.rettsregel.Faktum
import kotlin.math.min

/**
 * Demonstrasjon av rekursiv Uttrykk-struktur for regelsporing.
 *
 * Viser hvordan den rekursive strukturen forenkler:
 * - Traversering av uttrykkstre
 * - Forklaringsgenerering
 * - Transformasjoner og forenkling
 */

fun main() {
    println("=".repeat(80))
    println("REKURSIV UTTRYKK-STRUKTUR DEMO")
    println("=".repeat(80))
    println()

    uttrykkDirekteFaktumSyntaks()
    println("\n" + "=".repeat(80) + "\n")

    uttrykkEnkelBeregning()
    println("\n" + "=".repeat(80) + "\n")

    uttrykkKompleksBeregning()
    println("\n" + "=".repeat(80) + "\n")

    uttrykkNavngitteUttrykk()
    println("\n" + "=".repeat(80) + "\n")

    uttrykkTreVisning()
    println("\n" + "=".repeat(80) + "\n")

    uttrykkTransformasjoner()
}

/**
 * Demonstrasjon av direkte Faktum syntaks (uten Var).
 */
fun uttrykkDirekteFaktumSyntaks() {
    println("1. DIREKTE FAKTUM SYNTAKS - Enkel og ren kode")
    println("-".repeat(80))

    val G = Faktum("G", 110000)
    val sats = Faktum("sats", 0.25)
    val måneder = Faktum("måneder", 12)

    // Ny syntaks: Direkte bruk av Faktum uten Var()
    val nytt = sats * G / måneder

    // Gammel syntaks: Med Var()
    val gammelt = Var(sats) * Var(G) / Var(måneder)

    println("NY SYNTAKS (direkte Faktum):")
    println("  Kode: val uttrykk = sats * G / måneder")
    println("  Resultat: ${nytt.evaluer()}")
    println()

    println("Kompakt forklaring:")
    println(nytt.forklarKompakt("fulltSlitertillegg"))
}

/**
 * Enkel beregning med faktum.
 */
fun uttrykkEnkelBeregning() {
    println("2. ENKEL BEREGNING - Grunnbeløp per måned")
    println("-".repeat(80))

    val G = Faktum("G", 110000)
    val måneder = Faktum("måneder", 12)

    // Direkte syntaks
    val uttrykk = G / måneder

    println("Resultat: ${uttrykk.evaluer()}")
    println()
    println("Kompakt forklaring:")
    println(uttrykk.forklarKompakt("grunnbeløpPerMåned"))
}

/**
 * Kompleks beregning med flere operasjoner.
 */
fun uttrykkKompleksBeregning() {
    println("3. KOMPLEKS BEREGNING - Slitertillegg formel")
    println("-".repeat(80))

    val G = Faktum("G", 110000)
    val sats = Faktum("sats", 0.25)
    val måneder = Faktum("måneder", 12)

    // Direkte syntaks: sats * G / måneder
    val uttrykk = sats * G / måneder

    println("Symbolsk: ${uttrykk.notasjon()}")
    println("Konkret: ${uttrykk.konkret()}")
    println("Resultat: ${uttrykk.evaluer()}")
    println()

    println("Detaljert forklaring:")
    println(uttrykk.forklarDetaljert("fulltSlitertillegg"))
}

/**
 * Navngitte subforklaringer (tilsvarer "locked" formler).
 */
fun uttrykkNavngitteUttrykk() {
    println("4. NAVNGITTE UTTRYKK - Slitertillegg med justeringer")
    println("-".repeat(80))

    // Grunnverdier
    val G = Faktum("G", 110000)
    val faktiskTrygdetid = Faktum("faktiskTrygdetid", 20)
    val fullTrygdetid = Faktum("FULL_TRYGDETID", 40)
    val MND_36 = Faktum("MND_36", 36)
    val antallMåneder = Faktum("antallMånederEtterNedreAldersgrense", 24)

    // Subberegninger med direkte Faktum-bruk (mye renere!)
    val fulltSlitertillegg = (0.25 * G / 12).navngi("fulltSlitertillegg")
    val trygdetidFaktor = (faktiskTrygdetid / fullTrygdetid).navngi("trygdetidFaktor")
    val justeringsFaktor = ((MND_36 - Min(antallMåneder.tilUttrykk(), MND_36.tilUttrykk())) / MND_36).navngi("justeringsFaktor")

    // Hovedberegning
    val slitertillegg = (fulltSlitertillegg * justeringsFaktor * trygdetidFaktor).navngi("slitertillegg")

    println("Resultat: ${slitertillegg.evaluer()}")
    println()
    println("Detaljert forklaring:")
//    println(slitertillegg.forklarDetaljert("slitertillegg", maxDybde = 3))
    println(slitertillegg.uttrykk.forklarDetaljert(slitertillegg.navn, maxDybde = 3))

    println()
    println("Kompakt forklaring:")
    println(slitertillegg.uttrykk.forklarKompakt(slitertillegg.navn))

    println()
    println("Strukturtre:")
    println(slitertillegg.uttrykk.treVisning())
}

/**
 * Visualisering av uttrykkstre.
 */
fun uttrykkTreVisning() {
    println("5. TRE-VISUALISERING - Struktur av uttrykkstre")
    println("-".repeat(80))

    val a = Faktum("a", 10)
    val b = Faktum("b", 20)
    val c = Faktum("c", 30)

    // (a + b) * c - med direkte Faktum syntaks
    val uttrykk = (a + b) * c

    println("Uttrykk: ${uttrykk.notasjon()}")
    println("Resultat: ${uttrykk.evaluer()}")
    println()
    println("Tre-struktur:")
    println(uttrykk.treVisning())
}

/**
 * Transformasjoner og manipulering av uttrykk.
 */
fun uttrykkTransformasjoner() {
    println("6. TRANSFORMASJONER - Visitor pattern og forenkling")
    println("-".repeat(80))

    val x = Faktum("x", 5)
    val y = Faktum("y", 3)

    // Komplekst uttrykk: (2 * 3) + (x * y) - direkte Faktum syntaks
    val uttrykk = (Const<Int>(2) * Const<Int>(3)) + (x * y)

    println("Originalt uttrykk:")
    println("  Notasjon: ${uttrykk.notasjon()}")
    println("  Resultat: ${uttrykk.evaluer()}")
    println()

    // Forenkling (konstante subtre evalueres)
    val forenklet = uttrykk.forenkel()
    println("Forenklet uttrykk:")
    println("  Notasjon: ${forenklet.notasjon()}")
    println("  Resultat: ${forenklet.evaluer()}")
    println()

    // Finn alle variabler med visitor
    val variabler = uttrykk.visit { expr ->
        when (expr) {
            is Var -> listOf(expr.faktum.name)
            else -> emptyList()
        }
    }
    println("Variabler i uttrykket: ${variabler.distinct()}")
    println()

    // Substitusjon (erstatt variabel)
    val substituert = uttrykk.erstatt("x") { Const(100) }
    println("Etter substituering av x=100:")
    println("  Notasjon: ${substituert.notasjon()}")
    println("  Resultat: ${substituert.evaluer()}")
    println()

    // Dybde av uttrykkstre
    println("Dybde av uttrykkstre: ${uttrykk.dybde()}")
    println("Antall faktum: ${uttrykk.faktumListe().size}")
}
