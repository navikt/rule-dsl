package no.nav.system.rdsl2

/*
 * Kun operatorene eksemplet trenger. Alle regneoperasjoner gir Double.
 */

operator fun Uttrykk<Number>.times(høyre: Uttrykk<Number>): Uttrykk<Double> =
    Infiks(this, "*", høyre, Infiks.MULTIPLIKASJON, verdi.toDouble() * høyre.verdi.toDouble())

operator fun Uttrykk<Number>.times(høyre: Number): Uttrykk<Double> = this * Konstant(høyre)

operator fun Uttrykk<Number>.div(høyre: Uttrykk<Number>): Uttrykk<Double> {
    if (høyre.verdi.toDouble() == 0.0) throw ArithmeticException("Divisjon med null: ${høyre.notasjon()}")
    return Infiks(this, "/", høyre, Infiks.MULTIPLIKASJON, verdi.toDouble() / høyre.verdi.toDouble())
}

operator fun Uttrykk<Number>.div(høyre: Number): Uttrykk<Double> = this / Konstant(høyre)

operator fun Number.minus(høyre: Uttrykk<Number>): Uttrykk<Double> =
    Infiks(Konstant(this), "-", høyre, Infiks.ADDISJON, toDouble() - høyre.verdi.toDouble())

infix fun Uttrykk<Number>.erMindreEnn(høyre: Number): Uttrykk<Boolean> =
    Infiks(this, "<", Konstant(høyre), Infiks.SAMMENLIGNING, verdi.toDouble() < høyre.toDouble())

infix fun Uttrykk<Number>.erStørreEllerLik(høyre: Number): Uttrykk<Boolean> =
    Infiks(this, "≥", Konstant(høyre), Infiks.SAMMENLIGNING, verdi.toDouble() >= høyre.toDouble())

internal infix fun Uttrykk<Boolean>.og(høyre: Uttrykk<Boolean>): Uttrykk<Boolean> =
    Infiks(this, "og", høyre, Infiks.OG, verdi && høyre.verdi)

/**
 * Lager en egendefinert funksjon, f.eks. avrunding:
 *
 * ```
 * fun avrund2(x: Uttrykk<Number>) = funksjon("avrund2", x) { (x.verdi.toDouble() * 100).roundToLong() / 100.0 }
 * ```
 *
 * [beregning] kjøres umiddelbart. Noden lagrer kun resultatet.
 */
fun <T : Any> funksjon(navn: String, vararg argumenter: Uttrykk<*>, beregning: () -> T): Uttrykk<T> =
    Funksjon(navn, argumenter.toList(), beregning())
