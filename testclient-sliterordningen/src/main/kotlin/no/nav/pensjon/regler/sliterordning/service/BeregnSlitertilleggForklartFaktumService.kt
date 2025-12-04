package no.nav.pensjon.regler.sliterordning.service

import no.nav.pensjon.regler.sliterordning.config.AbstractDemoRuleService
import no.nav.pensjon.regler.sliterordning.flyt.BehandleSliterordningForklartFaktumFlyt
import no.nav.pensjon.regler.sliterordning.to.BeregnSlitertilleggRequest
import no.nav.pensjon.regler.sliterordning.to.Response

class BeregnSlitertilleggForklartFaktumService(
    val request: BeregnSlitertilleggRequest
) : AbstractDemoRuleService<Response.SliterordningForklartFaktum>() {
    override val ruleService: () -> Response.SliterordningForklartFaktum = {
        BehandleSliterordningForklartFaktumFlyt(request.uttakstidspunkt, request.virkningstidspunkt, request.person).run(this)
    }
}