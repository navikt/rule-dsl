package no.nav.pensjon.regler.sliterordning.service

import no.nav.pensjon.regler.sliterordning.config.AbstractDemoRuleService
import no.nav.pensjon.regler.sliterordning.flyt.BehandleSliterordningFlyt
import no.nav.pensjon.regler.sliterordning.to.SliterordningRequest
import no.nav.pensjon.regler.sliterordning.to.SliterordningResponse

class SliterordningService(
    val request: SliterordningRequest
) : AbstractDemoRuleService<SliterordningResponse>() {
    override val ruleService: () -> SliterordningResponse = {
        BehandleSliterordningFlyt(request.uttakstidspunkt, request.virkningstidspunkt, request.person).run(this)
    }
}