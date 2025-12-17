package no.nav.pensjon.regler.alderspensjon.domain

sealed class Response {
    data class Alderspensjon(val anvendtTrygdetid: Trygdetid?, val grunnpensjon: Grunnpensjon?) : Response()
}
