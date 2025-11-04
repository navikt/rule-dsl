package no.nav.system.rule.dsl.demo.forklaring

import no.nav.system.rule.dsl.rettsregel.Const
import no.nav.system.rule.dsl.rettsregel.Faktum
import no.nav.system.rule.dsl.rettsregel.operators.div
import no.nav.system.rule.dsl.rettsregel.operators.plus
import no.nav.system.rule.dsl.rettsregel.operators.times

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

    uttrykkTreVisning()
    println("\n" + "=".repeat(80) + "\n")

    uttrykkTransformasjoner()
}

/**
 * Demonstrasjon av direkte Faktum syntaks.
 */
fun uttrykkDirekteFaktumSyntaks() {
    println("1. DIREKTE Faktum SYNTAKS - Enkel og ren kode")
    println("-".repeat(80))

    val G = Faktum("G", Const(110000))
    val sats = Faktum("sats", Const(0.25))
    val måneder = Faktum("måneder", Const(12))

    // Direkte bruk av Faktum
    val uttrykk = sats * G / måneder

    println("SYNTAKS (direkte Faktum):")
    println("  Kode: val uttrykk = sats * G / måneder")
    println("  Resultat: ${uttrykk.evaluer()}")
    println()

    println("Kompakt forklaring:")
    println(uttrykk.notasjon())
}

/**
 * Enkel beregning.
 */
fun uttrykkEnkelBeregning() {
    println("2. ENKEL BEREGNING - Grunnbeløp per måned")
    println("-".repeat(80))

    val G = Faktum("G", Const(110000))
    val måneder = Faktum("måneder", Const(12))

    // Direkte syntaks
    val uttrykk = G / måneder

    println("Resultat: ${uttrykk.evaluer()}")
    println()
    println("Kompakt forklaring:")
    println(uttrykk.notasjon())
}

/**
 * Kompleks beregning med flere operasjoner.
 */
fun uttrykkKompleksBeregning() {
    println("3. KOMPLEKS BEREGNING - Slitertillegg formel")
    println("-".repeat(80))

    val G = Faktum("G", Const(110000))
    val sats = Faktum("sats", Const(0.25))
    val måneder = Faktum("måneder", Const(12))

    // Direkte syntaks: sats * G / måneder
    val uttrykk = sats * G / måneder

    println("Symbolsk: ${uttrykk.notasjon()}")
    println("Konkret: ${uttrykk.konkret()}")
    println("Resultat: ${uttrykk.evaluer()}")
    println()

    println("Detaljert forklaring:")
    println(uttrykk.notasjon())
}


/**
 * Visualisering av uttrykkstre.
 */
fun uttrykkTreVisning() {
    println("5. TRE-VISUALISERING - Struktur av uttrykkstre")
    println("-".repeat(80))

    val a = Faktum("a", Const(10))
    val b = Faktum("b", Const(20))
    val c = Faktum("c", Const(30))

    // (a + b) * c - med direkte Faktum syntaks
    val uttrykk = (a + b) * c

    println("Uttrykk: ${uttrykk.notasjon()}")
    println("Resultat: ${uttrykk.evaluer()}")
}

/**
 * Transformasjoner og manipulering av uttrykk.
 */
fun uttrykkTransformasjoner() {
    println("6. TRANSFORMASJONER - Visitor pattern og forenkling")
    println("-".repeat(80))

    val x = Faktum("x", Const(5))
    val y = Faktum("y", Const(3))

    // Komplekst uttrykk: (2 * 3) + (x * y) - direkte Faktum syntaks
    val uttrykk = (Const(2) * Const(3)) + (x * y)

    println("Originalt uttrykk:")
    println("  Notasjon: ${uttrykk.notasjon()}")
    println("  Resultat: ${uttrykk.evaluer()}")
}
