package no.nav.pensjon.regler.sliterordning.to

import no.nav.system.ruledsl.core.model.Faktum

sealed class SliterordningResponse() {
    data class Innvilget(val slitertillegg: Faktum<Double>) : SliterordningResponse()
    data class Avslag(val årsak: String) : SliterordningResponse()
}
