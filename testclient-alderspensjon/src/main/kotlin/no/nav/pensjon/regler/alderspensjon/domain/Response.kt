package no.nav.pensjon.regler.alderspensjon.domain

import no.nav.system.ruledsl.core.rettsregel.Faktum

sealed class Response {
    data class Alderspensjon(val anvendtTrygdetid: Trygdetid?, val grunnpensjon: Grunnpensjon?) : Response()

}
