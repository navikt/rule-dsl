package no.nav.system.prototype.regelsett

/**
 * Et navngitt uttrykk. Brukes det i et annet uttrykk, vises bare navn og verdi.
 *
 * Alle faktum har en [regel]. Lages faktumet utenfor en regel, får det [DefaultRegel], som ikke vises i forklaringen.
 * I en SÅ-blokk får `Faktum(...)` automatisk regelen som eier blokken.
 */
open class Faktum<out T : Any> internal constructor(
    val navn: String,
    val uttrykk: Uttrykk<T>,
    private val _regel: Regel?,
    @Suppress("UNUSED_PARAMETER") intern: Unit,
) : Uttrykk<T> {

    constructor(navn: String, uttrykk: Uttrykk<T>, regel: Regel = DefaultRegel) : this(navn, uttrykk, regel, Unit)

    constructor(navn: String, verdi: T, regel: Regel = DefaultRegel) : this(navn, Konstant(verdi), regel, Unit)

    val regel: Regel get() = _regel ?: DefaultRegel

    override val verdi: T get() = uttrykk.verdi
    override fun notasjon() = navn
    override fun konkret() = verdi.toString()
    override fun grunnlag() = listOf<Faktum<*>>(this)
    override fun toString() = "$navn = $verdi"
}

/**
 * En vurdert regel. Er selv et `Faktum<Boolean>`: verdien sier om regelen traff, og uttrykket er
 * betingelsene. Kjenner ikke de andre reglene i regelsettet.
 */
class Regel internal constructor(
    navn: String,
    internal val betingelser: List<Uttrykk<Boolean>>,
    val referanser: List<Referanse> = emptyList(),
) : Faktum<Boolean>(navn, Og(betingelser), null, Unit) {

    /** Har minst én funksjonell betingelse. Regler uten slike vises ikke i forklaringen. */
    internal val harFunksjonellInnhold: Boolean get() = betingelser.any { it !is TekniskIkkeOppfylt }
}

/** Hjemmel for en regel, f.eks. en paragraf med lenke til Lovdata. */
data class Referanse(val id: String, val url: String)

/** Regelen til faktum som lages utenfor en regel. */
val DefaultRegel = Regel("DefaultRegel", emptyList())
