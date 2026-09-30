package no.nav.system.rdsl2

/**
 * Et navngitt uttrykk.
 *
 * Faktum har to sider:
 * - **Utover** er det et [Uttrykk] som andre uttrykk kan bruke. Da vises bare navn og verdi,
 *   og faktumet utgjør en grense: `notasjon()` gir navnet, `grunnlag()` gir faktumet selv.
 * - **Innover** bærer det sin egen forklaring: [uttrykk] (utregningen) og [regel] (regelen
 *   som produserte det).
 *
 * Inndata lages med `Faktum(navn, verdi)` og har ingen regel. Et utledet faktum kan kun
 * oppstå i en regel, via `faktum(...)` i en SÅ-blokk. En regel er selv et `Faktum<Boolean>`
 * hvor uttrykket er betingelsene.
 *
 * Likhet er identitet: to like utregninger er to ulike hendelser.
 */
class Faktum<out T : Any> internal constructor(
    val navn: String,
    val uttrykk: Uttrykk<T>,
    val regel: Faktum<Boolean>? = null,
) : Uttrykk<T> {

    /** Inndata: et navngitt faktum med kjent verdi, uten regel. */
    constructor(navn: String, verdi: T) : this(navn, Konstant(verdi)) {
        require(verdi !is Uttrykk<*>) { "Inndata '$navn' kan ikke være et uttrykk. Bruk faktum(...) i en regel." }
    }

    override val verdi: T = uttrykk.verdi

    override fun notasjon(): String = navn
    override fun konkret(): String = verdi.vis()
    override fun grunnlag(): List<Faktum<*>> = listOf(this)

    override fun toString(): String = "$navn = ${konkret()}"
}
