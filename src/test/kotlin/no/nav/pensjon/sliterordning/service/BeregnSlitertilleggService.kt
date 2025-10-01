package no.nav.pensjon.sliterordning.service

import no.nav.pensjon.sliterordning.BeregnSlitertilleggRequest
import no.nav.pensjon.sliterordning.resultat.Slitertillegg
import no.nav.system.rule.dsl.demo.domain.Response
import no.nav.system.rule.dsl.demo.ruleservice.AbstractDemoRuleService

class BeregnSlitertilleggService(val request: BeregnSlitertilleggRequest) : AbstractDemoRuleService<Response.Slitertillegg>(){
    override val ruleService: () -> Response.Slitertillegg = {
        Response.Slitertillegg(slitertillegg = Slitertillegg(100000.00))
    }
}