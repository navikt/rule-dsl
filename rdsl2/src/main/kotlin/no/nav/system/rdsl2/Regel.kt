package no.nav.system.rdsl2

import kotlin.experimental.ExperimentalTypeInference

@DslMarker
annotation class RegelDsl

/**
 * Definerer og evaluerer en regel. Resultatet er et `Faktum<Boolean>` som kan brukes som
 * betingelse i andre regler.
 *
 * ```
 * val tidlig = regel("UTTAK-TIDLIG") {
 *     HVIS { antallMnd erMindreEnn 36 }
 *     SÅ { justeringsFaktor = faktum("justeringsFaktor", (36 - antallMnd) / 36) }
 * }
 * ```
 *
 * En regel uten betingelser treffer alltid. Tekniske betingelser (`HVIS { x != null }`) styrer
 * om regelen kan treffe, men vises ikke i forklaringen.
 */
fun regel(navn: String, definisjon: Regel.() -> Unit): Faktum<Boolean> = utfør(navn, null, definisjon)

@RegelDsl
class Regel internal constructor() {
    private val predikater = mutableListOf<Predikat>()
    private var kropp: (RegelKropp.() -> Unit)? = null

    /** Fagregel som spores. */
    @OptIn(ExperimentalTypeInference::class)
    @OverloadResolutionByLambdaReturnType
    fun HVIS(betingelse: () -> Uttrykk<Boolean>) {
        predikater += Predikat.Sporet(betingelse)
    }

    /**
     * Teknisk betingelse (guard). Spores ikke. Er den usann, treffer ikke regelen, og
     * betingelsene etter den evalueres ikke.
     */
    @OptIn(ExperimentalTypeInference::class)
    @OverloadResolutionByLambdaReturnType
    @JvmName("HVISGuard")
    fun HVIS(betingelse: () -> Boolean) {
        predikater += Predikat.Guard(betingelse)
    }

    @OptIn(ExperimentalTypeInference::class)
    @OverloadResolutionByLambdaReturnType
    fun OG(betingelse: () -> Uttrykk<Boolean>) = HVIS(betingelse)

    @OptIn(ExperimentalTypeInference::class)
    @OverloadResolutionByLambdaReturnType
    @JvmName("OGGuard")
    fun OG(betingelse: () -> Boolean) = HVIS(betingelse)

    fun SÅ(kropp: RegelKropp.() -> Unit) {
        check(this.kropp == null) { "SÅ kan bare brukes én gang per regel" }
        this.kropp = kropp
    }

    internal fun betingelser(): Uttrykk<Boolean> {
        val sporet = mutableListOf<Uttrykk<Boolean>>()
        for (predikat in predikater) when (predikat) {
            is Predikat.Sporet -> sporet += predikat.uttrykk()
            is Predikat.Guard -> if (!predikat.oppfylt()) return Konstant(false)
        }
        return sporet.reduceOrNull { a, b -> a og b } ?: Konstant(true)
    }

    internal fun kropp(): (RegelKropp.() -> Unit)? = kropp

    private sealed interface Predikat {
        class Sporet(val uttrykk: () -> Uttrykk<Boolean>) : Predikat
        class Guard(val oppfylt: () -> Boolean) : Predikat
    }
}

/**
 * Mottakeren i en SÅ-blokk. Alt som lages her, får regelen som eier blokken som sin [Faktum.regel].
 */
@RegelDsl
class RegelKropp internal constructor(private val eier: Faktum<Boolean>) {

    fun <T : Any> faktum(navn: String, uttrykk: Uttrykk<T>): Faktum<T> = Faktum(navn, uttrykk, eier)

    fun <T : Any> faktum(navn: String, verdi: T): Faktum<T> = faktum(navn, Konstant(verdi))

    /** En regel inne i en SÅ-blokk er betinget av regelen som eier blokken. */
    fun regel(navn: String, definisjon: Regel.() -> Unit): Faktum<Boolean> = utfør(navn, eier, definisjon)
}

private fun utfør(navn: String, overordnet: Faktum<Boolean>?, definisjon: Regel.() -> Unit): Faktum<Boolean> {
    val regel = Regel().apply(definisjon)
    val resultat = Faktum(navn, regel.betingelser(), overordnet)
    if (resultat.verdi) regel.kropp()?.invoke(RegelKropp(resultat))
    return resultat
}
