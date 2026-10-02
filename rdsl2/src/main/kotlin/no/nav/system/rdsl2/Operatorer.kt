package no.nav.system.rdsl2

/*
 * Kun operatorene testklientene trenger. Alle regneoperasjoner gir Double.
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

/*
 * Sammenligninger finnes i to varianter: mot et annet uttrykk og mot en ren verdi.
 * `fomDato erStørreEnn dato` er altså like gyldig som `fomDato erStørreEnn forrigeTomDato`.
 */

infix fun <T : Comparable<T>> Uttrykk<T>.erMindreEnn(høyre: Uttrykk<T>): Uttrykk<Boolean> = sammenlign(this, "<", høyre) { it < 0 }

infix fun <T : Comparable<T>> Uttrykk<T>.erMindreEnn(høyre: T): Uttrykk<Boolean> = this erMindreEnn Konstant(høyre)

infix fun <T : Comparable<T>> Uttrykk<T>.erStørreEllerLik(høyre: Uttrykk<T>): Uttrykk<Boolean> = sammenlign(this, "≥", høyre) { it >= 0 }

infix fun <T : Comparable<T>> Uttrykk<T>.erStørreEllerLik(høyre: T): Uttrykk<Boolean> = this erStørreEllerLik Konstant(høyre)

infix fun <T : Comparable<T>> Uttrykk<T>.erStørreEnn(høyre: Uttrykk<T>): Uttrykk<Boolean> = sammenlign(this, ">", høyre) { it > 0 }

infix fun <T : Comparable<T>> Uttrykk<T>.erStørreEnn(høyre: T): Uttrykk<Boolean> = this erStørreEnn Konstant(høyre)

private fun <T : Comparable<T>> sammenlign(venstre: Uttrykk<T>, symbol: String, høyre: Uttrykk<T>, test: (Int) -> Boolean) =
    Infiks(venstre, symbol, høyre, Infiks.SAMMENLIGNING, test(venstre.verdi.compareTo(høyre.verdi)))

infix fun <T : Any> Uttrykk<T>.erLik(høyre: Uttrykk<T>): Uttrykk<Boolean> =
    Infiks(this, "=", høyre, Infiks.SAMMENLIGNING, verdi == høyre.verdi)

infix fun <T : Any> Uttrykk<T>.erLik(høyre: T): Uttrykk<Boolean> = this erLik Konstant(høyre)

operator fun Uttrykk<Boolean>.not(): Uttrykk<Boolean> = Funksjon("ikke", listOf(this), !verdi)

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
