package no.nav.system.rule.dsl.rettsregel.forklartfaktum

import no.nav.system.rule.dsl.formel.Formel
import no.nav.system.rule.dsl.rettsregel.Faktum


open class ForklartFaktum<T : Any> internal constructor(
    navn: String,
    verdi: T,
    val hvorfor: String,
    val hvordan: Formel<out Number>
) : Faktum<T>(navn, verdi)

