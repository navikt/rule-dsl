package no.nav.system.rule.dsl.demo.ruleservice

import no.nav.system.rule.dsl.demo.domain.Request
import no.nav.system.rule.dsl.demo.domain.Response
import no.nav.system.rule.dsl.demo.ruleflow.BeregnAlderspensjonFlyt
import no.nav.system.rule.dsl.rettsregel.Faktum

class BeregnAlderspensjonService(
    private val request: Request,
) : AbstractDemoRuleService<Response.Alderspensjon>() {
    override val ruleService: () -> Response.Alderspensjon = {

        val output =
            BeregnAlderspensjonFlyt(
                request.person,
                Faktum("virkningstidspunkt", request.virkningstidspunkt)
            ).run(this)

        Response.Alderspensjon(
            anvendtTrygdetid = output.anvendtTrygdetid,
            grunnpensjon = output.grunnpensjon
        )
    }
}

