package no.nav.system.rdsl2

import java.io.Serializable

/**
 * Alt som har en verdi og kan forklare seg selv.
 *
 * - [notasjon] viser regnestykket symbolsk: `G * 0.25`
 * - [konkret] viser det samme med verdier: `118620 * 0.25`
 * - [grunnlag] gir de navngitte leddene ([Faktum]) som inngår direkte, og stopper ved første
 *   faktum. Det er herfra man nøster seg videre nedover.
 *
 * Brukeren forholder seg i praksis kun til [Faktum]. Øvrige implementasjoner lages av DSL-en
 * (konstanter, operatorer, funksjoner og sammenligninger).
 */
interface Uttrykk<out T : Any> : Serializable {
    val verdi: T
    fun notasjon(): String
    fun konkret(): String
    fun grunnlag(): List<Faktum<*>>
}
