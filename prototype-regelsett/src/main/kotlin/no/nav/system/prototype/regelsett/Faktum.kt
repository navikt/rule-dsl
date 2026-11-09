package no.nav.system.prototype.regelsett

/**
 * Et navngitt uttrykk. Brukes det i et annet uttrykk, vises bare navn og verdi.
 *
 * Alle faktum har en [regel]. Lages faktumet utenfor en regel, får det [DefaultRegel].
 * I en SÅ-blokk får `Faktum(...)` automatisk regelen som eier blokken.
 */
sealed class Faktum<out T : Any>(val navn: String, val uttrykk: Uttrykk<T>) : Uttrykk<T> {

    /** Regelen som produserte faktumet. En regel er sin egen regel. */
    abstract val regel: Regel

    override val verdi: T get() = uttrykk.verdi
    override fun notasjon() = navn
    override fun konkret() = verdi.toString()
    override fun grunnlag() = listOf<Faktum<*>>(this)
    override fun toString() = "$navn = $verdi"
}

fun <T : Any> Faktum(navn: String, uttrykk: Uttrykk<T>, regel: Regel = DefaultRegel): Faktum<T> =
    Produsert(navn, uttrykk, regel)

fun <T : Any> Faktum(navn: String, verdi: T, regel: Regel = DefaultRegel): Faktum<T> =
    Produsert(navn, Konstant(verdi), regel)

/** Faktum laget med `Faktum(...)`, enten som inndata eller i en regel. Alt som ikke er en [Regel]. */
private class Produsert<out T : Any>(navn: String, uttrykk: Uttrykk<T>, override val regel: Regel) :
    Faktum<T>(navn, uttrykk)

/**
 * En vurdert regel. Er selv et `Faktum<Boolean>`: verdien sier om regelen traff, og uttrykket er
 * betingelsene. Kjenner ikke de andre reglene i regelsettet.
 */
open class Regel internal constructor(
    navn: String,
    internal val betingelser: List<Uttrykk<Boolean>>,
    val referanser: List<Referanse> = emptyList(),
) : Faktum<Boolean>(navn, Og(betingelser)) {

    override val regel: Regel get() = this

    /** Har minst én funksjonell betingelse. Regler uten slike vises ikke i forklaringen. */
    internal val harFunksjonellInnhold: Boolean get() = betingelser.any { it !is TekniskIkkeOppfylt }

    /** Det regelen bidrar med i forklaringen til et faktum den har produsert. */
    internal open fun kildelinjer(): List<String> = listOf("regel: $navn") + vurderingslinjer()
}

/** Regelen til faktum som lages utenfor en regel. Bidrar ikke til forklaringen. */
object DefaultRegel : Regel("DefaultRegel", emptyList()) {
    override fun kildelinjer() = emptyList<String>()
}

/** Hjemmel for en regel, f.eks. en paragraf med lenke til Lovdata. */
data class Referanse(val id: String, val url: String)
