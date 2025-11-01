package no.nav.system.rule.dsl.demo.forklaring

import no.nav.system.rule.dsl.demo.forklaring.usecases.beregnSlitertillegg
import no.nav.system.rule.dsl.forklaring.*

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

    uttrykkDirekteGrunnlagSyntaks()
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
 * Demonstrasjon av direkte Grunnlag syntaks.
 */
fun uttrykkDirekteGrunnlagSyntaks() {
    println("1. DIREKTE GRUNNLAG SYNTAKS - Enkel og ren kode")
    println("-".repeat(80))

    val G = Grunnlag("G", Const(110000))
    val sats = Grunnlag("sats", Const(0.25))
    val måneder = Grunnlag("måneder", Const(12))

    // Direkte bruk av Grunnlag
    val uttrykk = sats * G / måneder

    println("SYNTAKS (direkte Grunnlag):")
    println("  Kode: val uttrykk = sats * G / måneder")
    println("  Resultat: ${uttrykk.evaluer()}")
    println()

    println("Kompakt forklaring:")
    println(uttrykk.forklarKompakt("fulltSlitertillegg"))
}

/**
 * Enkel beregning.
 */
fun uttrykkEnkelBeregning() {
    println("2. ENKEL BEREGNING - Grunnbeløp per måned")
    println("-".repeat(80))

    val G = Grunnlag("G", Const(110000))
    val måneder = Grunnlag("måneder", Const(12))

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

    val G = Grunnlag("G", Const(110000))
    val sats = Grunnlag("sats", Const(0.25))
    val måneder = Grunnlag("måneder", Const(12))

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

    val slitertillegg = beregnSlitertillegg(
        faktiskTrygdetid = Grunnlag("faktiskTrygdetid", Const(20)),
        antallMåneder = Grunnlag("antallMånederEtterNedreAldersgrense", Const(24))
    )
    println("4. NAVNGITTE UTTRYKK - ${slitertillegg.navn} med justeringer")
    println("-".repeat(80))

    println("Resultat: ${slitertillegg.evaluer()}")
    println()
    println("Detaljert forklaring:")
    println(slitertillegg.forklarDetaljert(slitertillegg.navn, maxDybde = 3))

    println()
    println("Strukturtre:")
    println(slitertillegg.treVisning())
}

/**
 * Visualisering av uttrykkstre.
 */
fun uttrykkTreVisning() {
    println("5. TRE-VISUALISERING - Struktur av uttrykkstre")
    println("-".repeat(80))

    val a = Grunnlag("a", Const(10))
    val b = Grunnlag("b", Const(20))
    val c = Grunnlag("c", Const(30))

    // (a + b) * c - med direkte Grunnlag syntaks
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

    val x = Grunnlag("x", Const(5))
    val y = Grunnlag("y", Const(3))

    // Komplekst uttrykk: (2 * 3) + (x * y) - direkte Grunnlag syntaks
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

    // Finn alle Grunnlag med visitor
    val grunnlag = uttrykk.visit { expr ->
        when (expr) {
            is Grunnlag -> listOf(expr.navn)
            else -> emptyList()
        }
    }
    println("Grunnlag i uttrykket: ${grunnlag.distinct()}")
    println()

    // Substitusjon (erstatt variabel) - fungerer ikke med Grunnlag
    // siden erstatt() er laget for Var med faktum.name
    println("Substitusjon er ikke støttet for Grunnlag (kun for Var)")
    println()

    // Dybde av uttrykkstre
    println("Dybde av uttrykkstre: ${uttrykk.dybde()}")
    println("Antall grunnlag: ${uttrykk.grunnlagListe().size}")
}
