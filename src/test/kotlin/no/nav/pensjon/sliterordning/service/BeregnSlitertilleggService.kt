package no.nav.pensjon.sliterordning.service

import no.nav.pensjon.sliterordning.BeregnSlitertilleggRequest
import no.nav.pensjon.sliterordning.flyt.BehandleSliterordningFlyt
import no.nav.system.rule.dsl.demo.domain.Response
import no.nav.system.rule.dsl.demo.ruleservice.AbstractDemoRuleService

class BeregnSlitertilleggService(val request: BeregnSlitertilleggRequest) :
    AbstractDemoRuleService<Response.Sliterordning>() {
    override val ruleService: () -> Response.Sliterordning = {
        BehandleSliterordningFlyt(request.uttakstidspunkt, request.virkningstidspunkt, request.person).run(this)

    }
}