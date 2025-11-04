package no.nav.system.rule.dsl.demo.domain

import no.nav.pensjon.sliterordning.resultat.Slitertillegg
import no.nav.system.rule.dsl.rettsregel.Faktum

sealed class Response {

    data class Alderspensjon(val anvendtTrygdetid: Trygdetid?, val grunnpensjon: Grunnpensjon?) : Response()
    sealed class Sliterordning() : Response() {
        data class Innvilget(val slitertillegg: Slitertillegg) : Sliterordning()
        data class Avslag(val årsak: String) : Sliterordning()
    }

    sealed class SliterordningForklartFaktum() : Response() {
        data class Innvilget(val slitertillegg: Faktum<Double>) : SliterordningForklartFaktum()
        data class Avslag(val årsak: String) : SliterordningForklartFaktum()
    }
}
