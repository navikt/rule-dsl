package no.nav.system.rule.dsl.forklaring

import java.io.Serializable

/**
 * Beslutningstabell for å håndtere komplekse if-else-kjeder på en strukturert måte.
 *
 * En tabell evaluerer regler sekvensiellt og returnerer resultatet fra første matchende regel.
 * Dette gjør kompleks beslutningslogikk mer lesbar, testbar og vedlikeholdbar.
 *
 * ## Eksempel
 * ```kotlin
 * val resultat = tabell<UtfallType>("flyktningVurdering") {
 *     regel {
 *         når { ikke(angittFlyktning) }
 *         resultat { Const(IKKE_RELEVANT) }
 *     }
 *     regel {
 *         når { angittFlyktning og ikke(kravFom2021) }
 *         resultat { Const(OPPFYLT) }
 *     }
 *     ellers { feilUttrykk("Ugyldig tilstand") }
 * }
 * ```
 *
 * ## Fordeler over nøstet HVIS-ELLERS
 * - **Lesbarhet**: Alle regler samlet i én struktur
 * - **Testing**: Lett å se alle mulige utfall
 * - **Vedlikeholdbarhet**: Enkel å legge til/endre regler
 * - **Dokumentasjon**: Selvforklarende struktur
 */
data class Tabell<T : Any>(
    val navn: String? = null,
    val regler: List<TabellRegel<T>>,
    val ellersUttrykk: Uttrykk<T>? = null
) : Uttrykk<T> {

    override fun evaluer(): T {
        // Evaluer regler i rekkefølge, returner første match
        for (regel in regler) {
            if (regel.betingelse.evaluer()) {
                return regel.resultat.evaluer()
            }
        }

        // Hvis ingen regler matcher, bruk ellers-uttrykk
        return ellersUttrykk?.evaluer()
            ?: throw IllegalStateException("Ingen regler i tabell '$navn' matchet, og ingen ellers-klausul er definert")
    }

    override fun notasjon(): String {
        val sb = StringBuilder()

        if (navn != null) {
            sb.append("TABELL $navn:\n")
        } else {
            sb.append("TABELL:\n")
        }

        regler.forEachIndexed { index, regel ->
            sb.append("  ${index + 1}. NÅR ${regel.betingelse.notasjon()}\n")
            sb.append("     → ${regel.resultat.notasjon()}\n")
        }

        if (ellersUttrykk != null) {
            sb.append("  ELLERS → ${ellersUttrykk.notasjon()}")
        }

        return sb.toString()
    }

    override fun konkret(): String {
        // Finn matchende regel
        for ((index, regel) in regler.withIndex()) {
            if (regel.betingelse.evaluer()) {
                return buildString {
                    if (navn != null) {
                        append("TABELL $navn:\n")
                    } else {
                        append("TABELL:\n")
                    }
                    append("  ${index + 1}. NÅR ${regel.betingelse.konkret()}\n")
                    append("     → ${regel.resultat.konkret()}")
                }
            }
        }

        // Ingen regel matchet, bruk ellers
        return buildString {
            if (navn != null) {
                append("TABELL $navn:\n")
            } else {
                append("TABELL:\n")
            }
            append("  ELLERS → ${ellersUttrykk?.konkret() ?: "INGEN MATCH"}")
        }
    }

    override fun grunnlagListe(): List<Grunnlag<out Any>> {
        val grunnlag = mutableListOf<Grunnlag<out Any>>()

        for (regel in regler) {
            grunnlag.addAll(regel.betingelse.grunnlagListe())
            grunnlag.addAll(regel.resultat.grunnlagListe())
        }

        ellersUttrykk?.let { grunnlag.addAll(it.grunnlagListe()) }

        return grunnlag
    }

    override fun dybde(): Int {
        val regelDybder = regler.map { maxOf(it.betingelse.dybde(), it.resultat.dybde()) }
        val ellersDybde = ellersUttrykk?.dybde() ?: 0
        return 1 + maxOf((regelDybder.maxOrNull() ?: 0), ellersDybde)
    }
}

/**
 * En enkelt regel i en beslutningstabell.
 *
 * En regel består av:
 * - **betingelse**: Boolean-uttrykk som må være sant
 * - **resultat**: Uttrykk som returneres hvis betingelsen er sann
 */
data class TabellRegel<T : Any>(
    val betingelse: Uttrykk<Boolean>,
    val resultat: Uttrykk<T>
) : Serializable

/**
 * Builder for TabellRegel.
 */
class TabellRegelBuilder<T : Any> {
    private var betingelse: Uttrykk<Boolean>? = null
    private var resultat: Uttrykk<T>? = null

    /**
     * Definerer betingelsen for denne regelen.
     */
    fun når(block: () -> Uttrykk<Boolean>) {
        betingelse = block()
    }

    /**
     * Definerer resultatet hvis betingelsen er sann.
     */
    fun resultat(block: () -> Uttrykk<T>) {
        resultat = block()
    }

    internal fun build(): TabellRegel<T> {
        return TabellRegel(
            betingelse = betingelse ?: throw IllegalStateException("Regel mangler betingelse (når)"),
            resultat = resultat ?: throw IllegalStateException("Regel mangler resultat")
        )
    }
}

/**
 * Builder for Tabell.
 */
class TabellBuilder<T : Any>(private val navn: String? = null) {
    private val regler = mutableListOf<TabellRegel<T>>()
    private var ellersUttrykk: Uttrykk<T>? = null

    /**
     * Legger til en regel i tabellen.
     */
    fun regel(block: TabellRegelBuilder<T>.() -> Unit) {
        val builder = TabellRegelBuilder<T>()
        builder.block()
        regler.add(builder.build())
    }

    /**
     * Definerer ellers-klausul (fallback hvis ingen regler matcher).
     */
    fun ellers(block: () -> Uttrykk<T>) {
        ellersUttrykk = block()
    }

    internal fun build(): Tabell<T> {
        return Tabell(navn, regler, ellersUttrykk)
    }
}

/**
 * DSL-funksjon for å opprette en beslutningstabell.
 *
 * ## Eksempel
 * ```kotlin
 * val resultat = tabell<UtfallType>("vurdering") {
 *     regel {
 *         når { betingelse1 }
 *         resultat { Const(OPPFYLT) }
 *     }
 *     regel {
 *         når { betingelse2 }
 *         resultat { Const(IKKE_OPPFYLT) }
 *     }
 *     ellers { feilUttrykk("Ugyldig tilstand") }
 * }
 * ```
 */
fun <T : Any> tabell(navn: String? = null, block: TabellBuilder<T>.() -> Unit): Tabell<T> {
    val builder = TabellBuilder<T>(navn)
    builder.block()
    return builder.build()
}
