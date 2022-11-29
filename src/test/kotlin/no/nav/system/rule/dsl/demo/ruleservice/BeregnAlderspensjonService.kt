package no.nav.system.rule.dsl.demo.ruleservice

import no.nav.system.rule.dsl.demo.domain.Request
import no.nav.system.rule.dsl.demo.domain.Response
import no.nav.system.rule.dsl.demo.domain.param.AlderspensjonInput
import no.nav.system.rule.dsl.demo.domain.param.AlderspensjonOutput
import no.nav.system.rule.dsl.demo.domain.param.AlderspensjonParameter
import no.nav.system.rule.dsl.demo.ruleflow.BeregnAlderspensjonFlyt
import no.nav.system.rule.dsl.rettsregel.Faktum

class BeregnAlderspensjonService(
    private val request: Request,
) : AbstractDemoRuleService<Response>() {
    override val ruleService: () -> Response = {

        val parameter = AlderspensjonParameter(
            input = AlderspensjonInput(
                person = request.person,
                virkningstidspunkt = Faktum("virkningstidspunkt", request.virkningstidspunkt)
            ),
            output = AlderspensjonOutput()
        )

        BeregnAlderspensjonFlyt(parameter).run(this)

        Response(
            anvendtTrygdetid = parameter.output.anvendtTrygdetid,
            grunnpensjon = parameter.output.grunnpensjon
        )
    }
}

