package no.nav.pensjon.regler.alderspensjon.service

import no.nav.pensjon.regler.alderspensjon.config.AbstractDemoRuleService
import no.nav.pensjon.regler.alderspensjon.domain.Request
import no.nav.pensjon.regler.alderspensjon.domain.Response
import no.nav.pensjon.regler.alderspensjon.flyt.BeregnAlderspensjonFlyt
import no.nav.system.ruledsl.core.rettsregel.Faktum

class BeregnAlderspensjonService(
    private val request: Request,
) : AbstractDemoRuleService<Response.Alderspensjon>() {
    override val ruleService: () -> Response.Alderspensjon = {

        val output = BeregnAlderspensjonFlyt(
            request.person,
            Faktum("virkningstidspunkt", request.virkningstidspunkt)
        ).run(this)

        Response.Alderspensjon(
            anvendtTrygdetid = output.anvendtTrygdetid,
            grunnpensjon = output.grunnpensjon
        )
    }
}