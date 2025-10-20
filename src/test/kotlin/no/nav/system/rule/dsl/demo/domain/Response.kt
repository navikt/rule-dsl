package no.nav.system.rule.dsl.demo.domain

sealed class Response {

    data class Alderspensjon(val anvendtTrygdetid: Trygdetid?, val grunnpensjon: Grunnpensjon?) : Response()
    data class Slitertillegg(val slitertillegg: no.nav.pensjon.sliterordning.resultat.Slitertillegg) : Response()
}