package no.nav.pensjon.regler.sliterordning.to

import no.nav.pensjon.regler.sliterordning.domain.Slitertillegg
import no.nav.system.ruledsl.core.rettsregel.Faktum

sealed class Response {
    sealed class Sliterordning() : Response() {
        data class Innvilget(val slitertillegg: Slitertillegg) : Sliterordning()
        data class Avslag(val årsak: String) : Sliterordning()
    }

    sealed class SliterordningForklartFaktum() : Response() {
        data class Innvilget(val slitertillegg: Faktum<Double>) : SliterordningForklartFaktum()
        data class Avslag(val årsak: String) : SliterordningForklartFaktum()
    }
}