package no.nav.pensjon.sliterordning.service

import no.nav.pensjon.sliterordning.BeregnSlitertilleggRequest
import no.nav.pensjon.sliterordning.flyt.BehandleSliterordningForklartFaktumFlyt
import no.nav.system.rule.dsl.demo.domain.Response
import no.nav.system.rule.dsl.demo.ruleservice.AbstractDemoRuleService

class BeregnSlitertilleggForklartFaktumService(
    val request: BeregnSlitertilleggRequest
) : AbstractDemoRuleService<Response.SliterordningForklartFaktum>() {
    override val ruleService: () -> Response.SliterordningForklartFaktum = {
        BehandleSliterordningForklartFaktumFlyt(request.uttakstidspunkt, request.virkningstidspunkt, request.person).run(this)
    }
}