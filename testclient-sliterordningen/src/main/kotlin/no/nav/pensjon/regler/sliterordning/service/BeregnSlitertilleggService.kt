package no.nav.pensjon.regler.sliterordning.service

import no.nav.pensjon.regler.sliterordning.config.AbstractDemoRuleService
import no.nav.pensjon.regler.sliterordning.flyt.BehandleSliterordningFlyt
import no.nav.pensjon.regler.sliterordning.to.BeregnSlitertilleggRequest
import no.nav.pensjon.regler.sliterordning.to.Response

class BeregnSlitertilleggService(val request: BeregnSlitertilleggRequest) :
    AbstractDemoRuleService<Response.Sliterordning>() {
    override val ruleService: () -> Response.Sliterordning = {
        BehandleSliterordningFlyt(request.uttakstidspunkt, request.virkningstidspunkt, request.person).run(this)
    }
}