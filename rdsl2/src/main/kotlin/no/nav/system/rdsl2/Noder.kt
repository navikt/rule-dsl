package no.nav.system.rdsl2

/*
 * Interne byggeklosser som DSL-en lager. Verdien regnes ut én gang når noden lages,
 * slik at noden kun består av data og kan serialiseres.
 */

/** Anonym verdi, typisk et tall skrevet direkte i en formel: `0.25`. */
internal class Konstant<out T : Any>(override val verdi: T) : Uttrykk<T> {
    override fun notasjon(): String = verdi.vis()
    override fun konkret(): String = verdi.vis()
    override fun grunnlag(): List<Faktum<*>> = emptyList()
}

/** Operator mellom to ledd: `a * b`, `a < b`, `a og b`. */
internal class Infiks<out T : Any>(
    val venstre: Uttrykk<*>,
    val symbol: String,
    val høyre: Uttrykk<*>,
    val presedens: Int,
    override val verdi: T,
) : Uttrykk<T> {
    override fun notasjon(): String = skriv(Uttrykk<*>::notasjon)
    override fun konkret(): String = skriv(Uttrykk<*>::konkret)
    override fun grunnlag(): List<Faktum<*>> = (venstre.grunnlag() + høyre.grunnlag()).distinct()

    private fun skriv(tekst: Uttrykk<*>.() -> String): String {
        val v = venstre.tekst().let { if (venstre.presedens < presedens) "($it)" else it }
        val h = høyre.tekst().let { if (høyre.presedens < presedens || (høyre.presedens == presedens && symbol in IKKE_ASSOSIATIVE)) "($it)" else it }
        return "$v $symbol $h"
    }

    companion object {
        const val OG = 1
        const val SAMMENLIGNING = 2
        const val ADDISJON = 3
        const val MULTIPLIKASJON = 4

        private val IKKE_ASSOSIATIVE = setOf("-", "/")
    }
}

/** Navngitt funksjon: `avrund2(a)`, `min(a, b)`. */
internal class Funksjon<out T : Any>(
    val navn: String,
    val argumenter: List<Uttrykk<*>>,
    override val verdi: T,
) : Uttrykk<T> {
    override fun notasjon(): String = argumenter.joinToString(", ", "$navn(", ")") { it.notasjon() }
    override fun konkret(): String = argumenter.joinToString(", ", "$navn(", ")") { it.konkret() }
    override fun grunnlag(): List<Faktum<*>> = argumenter.flatMap { it.grunnlag() }.distinct()
}

private val Uttrykk<*>.presedens: Int
    get() = (this as? Infiks<*>)?.presedens ?: Int.MAX_VALUE

internal fun Any.vis(): String = when (this) {
    is Boolean -> if (this) "ja" else "nei"
    else -> toString()
}
