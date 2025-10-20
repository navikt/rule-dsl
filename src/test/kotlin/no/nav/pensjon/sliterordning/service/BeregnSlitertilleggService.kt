package no.nav.pensjon.sliterordning.service

import no.nav.pensjon.sliterordning.BeregnSlitertilleggRequest
import no.nav.pensjon.sliterordning.fagdata.FagKonstanter.ETT_ÅR
import no.nav.pensjon.sliterordning.fagdata.FagKonstanter.FULL_TRYGDETID
import no.nav.pensjon.sliterordning.fagdata.FagKonstanter.TRE_ÅR
import no.nav.pensjon.sliterordning.flyt.BeregnSlitertilleggFlyt
import no.nav.pensjon.sliterordning.grunnlag.Person
import no.nav.pensjon.sliterordning.regelsett.BeregnSlitertilleggRS
import no.nav.pensjon.sliterordning.resultat.Slitertillegg
import no.nav.pensjon.sliterordning.resultat.SlitertilleggVårVersjon
import no.nav.system.rule.dsl.AbstractRuleflow
import no.nav.system.rule.dsl.AbstractRuleset
import no.nav.system.rule.dsl.demo.domain.Response
import no.nav.system.rule.dsl.demo.ruleservice.AbstractDemoRuleService
import no.nav.system.rule.dsl.demo.ruleservice.grunnbeløpByYearMonth
import java.time.YearMonth
import java.time.temporal.ChronoUnit

class BeregnSlitertilleggService(val request: BeregnSlitertilleggRequest) :
    AbstractDemoRuleService<Response.Slitertillegg>() {
    override val ruleService: () -> Response.Slitertillegg = {
        Response.Slitertillegg(
            BeregnSlitertilleggFlyt(request.virkningstidspunkt, request.person).run(this)
        )
    }
}