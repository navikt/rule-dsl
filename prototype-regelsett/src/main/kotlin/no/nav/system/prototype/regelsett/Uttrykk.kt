package no.nav.system.prototype.regelsett

/** Et uttrykk med verdi som kan vises med navn (notasjon) og med verdier (konkret). */
interface Uttrykk<out T : Any> {
    val verdi: T
    fun notasjon(): String
    fun konkret(): String

    /** Navngitte ledd som brukes direkte. Stopper ved første [Faktum]. */
    fun grunnlag(): List<Faktum<*>>
}

/** Bare operatorer og `Faktum(navn, verdi)` lager konstanter. */
internal class Konstant<out T : Any>(override val verdi: T) : Uttrykk<T> {
    override fun notasjon() = verdi.toString()
    override fun konkret() = verdi.toString()
    override fun grunnlag() = emptyList<Faktum<*>>()
}

internal class Binær<out T : Any>(
    val op: String,
    val a: Uttrykk<*>,
    val b: Uttrykk<*>,
    override val verdi: T,
) : Uttrykk<T> {
    private val presedens get() = presedensFor(op)

    /** Parentes bare når regnereglene krever det: `a * (b + c)`, `a - (b - c)`, `a / (b * c)`. */
    private fun Uttrykk<*>.ledd(høyre: Boolean, f: Uttrykk<*>.() -> String): String {
        if (this !is Binær<*>) return f()
        val forelder = this@Binær
        val trengs = presedens < forelder.presedens ||
            presedens == forelder.presedens &&
            (presedens == SAMMENLIGNING || høyre && forelder.op in setOf("-", "/"))
        return if (trengs) "(${f()})" else f()
    }

    override fun notasjon() = "${a.ledd(false) { notasjon() }} $op ${b.ledd(true) { notasjon() }}"
    override fun konkret() = "${a.ledd(false) { konkret() }} $op ${b.ledd(true) { konkret() }}"
    override fun grunnlag() = a.grunnlag() + b.grunnlag()

    private companion object {
        const val SAMMENLIGNING = 1
        fun presedensFor(op: String) = when (op) {
            "*", "/" -> 3
            "+", "-" -> 2
            else -> SAMMENLIGNING
        }
    }
}

/** Betingelsene i en regel. */
internal class Og(val ledd: List<Uttrykk<Boolean>>) : Uttrykk<Boolean> {
    override val verdi = ledd.all { it.verdi }
    override fun notasjon() = ledd.joinToString(" og ") { it.notasjon() }.ifEmpty { "true" }
    override fun konkret() = ledd.joinToString(" og ") { it.konkret() }.ifEmpty { "true" }
    override fun grunnlag() = ledd.flatMap { it.grunnlag() }
}

private fun Any.d() = (this as Number).toDouble()

operator fun Uttrykk<Number>.times(o: Uttrykk<Number>): Uttrykk<Double> = Binær("*", this, o, verdi.d() * o.verdi.d())
operator fun Uttrykk<Number>.times(o: Number): Uttrykk<Double> = this * Konstant(o)
operator fun Uttrykk<Number>.div(o: Uttrykk<Number>): Uttrykk<Double> = Binær("/", this, o, verdi.d() / o.verdi.d())
operator fun Uttrykk<Number>.div(o: Number): Uttrykk<Double> = this / Konstant(o)
operator fun Number.minus(o: Uttrykk<Number>): Uttrykk<Double> = Binær("-", Konstant(this), o, d() - o.verdi.d())

infix fun Uttrykk<Int>.erMindreEnn(o: Int): Uttrykk<Boolean> = Binær("<", this, Konstant(o), verdi < o)
infix fun Uttrykk<Int>.erMindreEnn(o: Uttrykk<Int>): Uttrykk<Boolean> = Binær("<", this, o, verdi < o.verdi)
infix fun Uttrykk<Int>.erStørreEllerLik(o: Int): Uttrykk<Boolean> = Binær(">=", this, Konstant(o), verdi >= o)
infix fun Uttrykk<Int>.erStørreEllerLik(o: Uttrykk<Int>): Uttrykk<Boolean> = Binær(">=", this, o, verdi >= o.verdi)
infix fun Uttrykk<Number>.erStørreEnn(o: Number): Uttrykk<Boolean> = Binær(">", this, Konstant(o), verdi.d() > o.d())
infix fun Uttrykk<Boolean>.erLik(o: Boolean): Uttrykk<Boolean> = Binær("=", this, Konstant(o), verdi == o)
