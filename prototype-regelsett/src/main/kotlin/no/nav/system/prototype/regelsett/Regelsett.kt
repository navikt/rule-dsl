package no.nav.system.prototype.regelsett

import kotlin.experimental.ExperimentalTypeInference

@DslMarker
annotation class RegelDsl

/**
 * Kjører reglene i rekkefølge. Første `RETURNER` avslutter regelsettet og gir resultatet.
 * Uten `RETURNER` må [R] være `Unit`, ellers er det en kjøretidsfeil.
 *
 * ```
 * val justeringsFaktor = regelsett("justeringsFaktor") {
 *     regel("UTTAK-TIDLIG") {
 *         HVIS { antallMnd erMindreEnn 36 }
 *         SÅ { RETURNER(Faktum("justeringsFaktor", (36 - antallMnd) / 36)) }
 *     }
 * }
 * ```
 */
inline fun <reified R> regelsett(navn: String, noinline definisjon: RegelsettBygger<R>.() -> Unit): R =
    kjørRegelsett(navn, R::class == Unit::class, definisjon)

@PublishedApi
internal fun <R> kjørRegelsett(navn: String, erUnit: Boolean, definisjon: RegelsettBygger<R>.() -> Unit): R {
    try {
        RegelsettBygger<R>().definisjon()
    } catch (retur: Retur) {
        @Suppress("UNCHECKED_CAST")
        return retur.verdi as R
    }
    check(erUnit) { "Regelsett '$navn': ingen regel returnerte en verdi" }
    @Suppress("UNCHECKED_CAST")
    return Unit as R
}

@RegelDsl
class RegelsettBygger<R> internal constructor() {

    /**
     * Vurderer regelen med en gang og kjører SÅ hvis den treffer. Returnerer regelen som et
     * `Faktum<Boolean>`, slik at senere regler kan bruke den i en betingelse.
     *
     * Betingelsene vurderes i rekkefølge:
     * - teknisk (`Boolean`): spores ikke. Er den usann, vurderes ingen flere betingelser.
     * - funksjonell (`Uttrykk<Boolean>`): spores. Er den usann, vurderes resten likevel, slik at
     *   forklaringen viser alle grunnene til at regelen ikke slo til.
     */
    fun regel(navn: String, definisjon: RegelBygger<R>.() -> Unit): Faktum<Boolean> {
        val bygger = RegelBygger<R>().apply(definisjon)
        val vurdert = mutableListOf<Uttrykk<Boolean>>()
        for (betingelse in bygger.betingelser) {
            when (betingelse) {
                is Teknisk -> if (!betingelse.sjekk()) {
                    vurdert += TekniskIkkeOppfylt
                    break
                }
                is Funksjonell -> vurdert += betingelse.uttrykk()
            }
        }
        val regel = Regel(navn, vurdert, bygger.referanser)
        if (regel.verdi) bygger.kropp?.invoke(RegelKropp(regel))
        return regel
    }
}

@RegelDsl
class RegelBygger<R> internal constructor() {
    internal val betingelser = mutableListOf<Betingelse>()
    internal val referanser = mutableListOf<Referanse>()
    internal var kropp: (RegelKropp<R>.() -> Unit)? = null

    /** Hjemmel for regelen. Vises i forklaringen. */
    fun REF(id: String, url: String) {
        referanser += Referanse(id, url)
    }

    /** Funksjonell betingelse. Spores, og stopper ikke vurderingen av resten om den er usann. */
    @OptIn(ExperimentalTypeInference::class)
    @OverloadResolutionByLambdaReturnType
    fun HVIS(betingelse: () -> Uttrykk<Boolean>) {
        betingelser += Funksjonell(betingelse)
    }

    /** Teknisk betingelse, f.eks. null-sjekk. Spores ikke. Er den usann, vurderes ingen flere betingelser. */
    @JvmName("HVISBoolean")
    fun HVIS(betingelse: () -> Boolean) {
        betingelser += Teknisk(betingelse)
    }

    @OptIn(ExperimentalTypeInference::class)
    @OverloadResolutionByLambdaReturnType
    fun OG(betingelse: () -> Uttrykk<Boolean>) = HVIS(betingelse)

    @JvmName("OGBoolean")
    fun OG(betingelse: () -> Boolean) = HVIS(betingelse)

    fun SÅ(kropp: RegelKropp<R>.() -> Unit) {
        check(this.kropp == null) { "SÅ kan bare brukes én gang" }
        this.kropp = kropp
    }
}

/**
 * Mottakeren i SÅ. Funksjonene under heter `Faktum` med vilje: i SÅ vinner de over de vanlige `Faktum`-funksjonene,
 * slik at `Faktum(navn, ...)` automatisk får regelen som eier blokken.
 *
 * `@MustUseReturnValues` gir advarsel når et faktum lages i SÅ uten å bli brukt, typisk en glemt `RETURNER`.
 * Krever kompilatorflagget `-Xreturn-value-checker=check`.
 */
@MustUseReturnValues
@RegelDsl
@Suppress("FunctionName")
class RegelKropp<R> internal constructor(private val regel: Regel) {

    fun <T : Any> Faktum(navn: String, uttrykk: Uttrykk<T>): Faktum<T> =
        no.nav.system.prototype.regelsett.Faktum(navn, uttrykk, regel)

    fun <T : Any> Faktum(navn: String, verdi: T): Faktum<T> =
        no.nav.system.prototype.regelsett.Faktum(navn, verdi, regel)

    /** Setter regelsettets resultat. Ingen flere regler kjøres. */
    fun RETURNER(verdi: R): Nothing = throw Retur(verdi)
}

internal sealed interface Betingelse
internal class Teknisk(val sjekk: () -> Boolean) : Betingelse
internal class Funksjonell(val uttrykk: () -> Uttrykk<Boolean>) : Betingelse

/** Står i forklaringen når en teknisk betingelse stoppet regelen. Selve sjekken vises ikke. */
internal object TekniskIkkeOppfylt : Uttrykk<Boolean> {
    override val verdi = false
    override fun notasjon() = "teknisk betingelse ikke oppfylt"
    override fun konkret() = notasjon()
    override fun grunnlag() = emptyList<Faktum<*>>()
}

@PublishedApi
internal class Retur(val verdi: Any?) : Throwable(null, null, false, false)
