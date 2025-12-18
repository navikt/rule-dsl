package no.nav.pensjon.regler.alderspensjon.service

import no.nav.pensjon.regler.alderspensjon.config.GrunnbeløpSatsResource
import no.nav.pensjon.regler.alderspensjon.config.grunnbeløpByDate
import no.nav.pensjon.regler.alderspensjon.domain.Request
import no.nav.pensjon.regler.alderspensjon.domain.Response
import no.nav.pensjon.regler.alderspensjon.domain.koder.YtelseEnum
import no.nav.pensjon.regler.alderspensjon.ruleset.beregnFaktiskTrygdetid
import no.nav.pensjon.regler.alderspensjon.ruleset.beregnGrunnpensjon
import no.nav.pensjon.regler.alderspensjon.ruleset.personenErFlyktning
import no.nav.system.ruledsl.core.expression.Faktum
import no.nav.system.ruledsl.core.trace.RuleContext

/**
 * Service for calculating alderspensjon.
 *
 * NOTE: In the old core module, this extended AbstractRuleService which
 * provided a class-based execution model with automatic resource propagation
 * and component tree building.
 *
 * In core-trace, we use function-based rules with Trace as context parameter.
 * Resources must be registered on Trace before calling the calculation functions.
 */
fun beregnAlderspensjon(request: Request): Pair<Response.Alderspensjon, RuleContext> {
    val ruleContext = RuleContext("BeregnAlderspensjon")
    
    // Register resources
    ruleContext.putResource(GrunnbeløpSatsResource::class, GrunnbeløpSatsResource())
    
    val response = with(ruleContext) {
        val virkningstidspunkt = Faktum("virkningstidspunkt", request.virkningstidspunkt)
        
        // Check flyktning status
        val anvendtFlyktning = personenErFlyktning(
            request.person,
            Faktum("Ytelsestype", YtelseEnum.AP),
            Faktum("Kapittel 20", false),
            virkningstidspunkt,
            Faktum("Søknadstidspunkt fom 2021", true)
        )
        
        // Calculate trygdetid
        val trygdetid = beregnFaktiskTrygdetid(
            request.person.fødselsdato,
            virkningstidspunkt,
            request.person.boperioder,
            anvendtFlyktning
        )
        
        // Determine grunnpensjon sats based on sivilstand
        val grunnpensjonSats = if (request.person.erGift) 0.90 else 1.00
        
        // Calculate grunnpensjon
        val grunnbeløp = grunnbeløpByDate(request.virkningstidspunkt)
        val grunnpensjon = beregnGrunnpensjon(
            grunnbeløp,
            trygdetid.value.år,
            grunnpensjonSats
        )
        
        Response.Alderspensjon(
            anvendtTrygdetid = trygdetid.value,
            grunnpensjon = grunnpensjon.value
        )
    }
    
    return response to ruleContext
}
