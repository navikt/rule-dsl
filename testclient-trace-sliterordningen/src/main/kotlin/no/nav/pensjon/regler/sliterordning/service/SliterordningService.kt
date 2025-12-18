package no.nav.pensjon.regler.sliterordning.service

import no.nav.pensjon.regler.sliterordning.config.GrunnbeløpSatsResource
import no.nav.pensjon.regler.sliterordning.config.grunnbeløpByYearMonth
import no.nav.pensjon.regler.sliterordning.domain.Person
import no.nav.pensjon.regler.sliterordning.fagdata.FagKonstanter.MND_36
import no.nav.pensjon.regler.sliterordning.functions.avrund2desimal
import no.nav.pensjon.regler.sliterordning.to.SliterordningRequest
import no.nav.pensjon.regler.sliterordning.to.SliterordningResponse
import no.nav.pensjon.regler.sliterordning.to.SliterordningResponse.*
import no.nav.system.ruledsl.core.expression.Faktum
import no.nav.system.ruledsl.core.expression.boolean.erMindreEnn
import no.nav.system.ruledsl.core.expression.boolean.erStørreEllerLik
import no.nav.system.ruledsl.core.expression.math.div
import no.nav.system.ruledsl.core.expression.math.minus
import no.nav.system.ruledsl.core.expression.math.times
import no.nav.system.ruledsl.core.trace.RuleResult
import no.nav.system.ruledsl.core.trace.RuleContext
import no.nav.system.ruledsl.core.trace.traced
import java.time.YearMonth
import java.time.temporal.ChronoUnit

/**
 * Sliterordning service using core-trace module.
 * 
 * This demonstrates the new functional approach with context parameters.
 */
class SliterordningService(private val request: SliterordningRequest) {
    
    fun run(): Pair<SliterordningResponse, RuleContext> {
        val ruleContext = RuleContext("SliterordningService")
        ruleContext.putResource(GrunnbeløpSatsResource::class, GrunnbeløpSatsResource())
        
        val response = with(ruleContext) {
            behandleSliterordning(request.uttakstidspunkt, request.virkningstidspunkt, request.person)
        }
        
        return Pair(response, ruleContext)
    }
}

/**
 * Main flow: determines if sliterordning should be granted or denied.
 * 
 * The branching logic is expressed as rules, making the decision visible in the trace.
 */
context(ruleContext: RuleContext)
fun behandleSliterordning(
    uttakstidspunkt: YearMonth,
    virkningstidspunkt: YearMonth,
    person: Person
): SliterordningResponse = traced<SliterordningResponse> {
    
    // First, evaluate vilkårsprøving - this produces a RuleResult we can reference
    val vilkårOppfylt = vilkårsprøvSlitertillegg()
    
    // Branch: Innvilget when vilkår is met
    regel("Innvilget slitertillegg") {
        HVIS { vilkårOppfylt }
        RETURNER {
            val grunnbeløp = grunnbeløpByYearMonth(virkningstidspunkt)
            Innvilget(
                slitertillegg = beregnSlitertillegg(uttakstidspunkt, person, grunnbeløp)
            )
        }
    }
    
    // Branch: Avslag when vilkår is not met
    regel("Avslag slitertillegg") {
        HVIS { true }  // Fallback - only reached if above didn't fire
        RETURNER {
            Avslag("Vilkår ikke oppfylt")
        }
    }
}

/**
 * Vilkårsprøving for slitertillegg.
 * For testing purposes, this simple implementation always grants approval.
 * 
 * Returns a RuleResult that can be used directly in HVIS predicates of subsequent rules.
 */
context(ruleContext: RuleContext)
fun vilkårsprøvSlitertillegg(): RuleResult {
    var result: RuleResult? = null
    traced<Unit> {
        result = regel("Vilkårsprøving slitertillegg") {
            HVIS { true }
            SÅ { }  // Side-effect rule that just records whether vilkår is met
        }
    }
    return result!!
}

/**
 * Beregning av slitertillegg.
 * 
 * Calculates the pension supplement based on:
 * - Time since lower pension age (justeringsfaktor)
 * - Trygdetid (insurance time factor)
 * - Grunnbeløp (base amount)
 */
context(ruleContext: RuleContext)
fun beregnSlitertillegg(
    uttakstidspunkt: YearMonth,
    person: Person,
    grunnbeløp: Int
): Faktum<Double> = traced<Faktum<Double>> {
    
    // Faktum
    val faktiskTrygdetid = Faktum("faktiskTrygdetid", person.trygdetid.faktiskTrygdetid)
    val nedrePensjonsDato = Faktum("nedrePensjonsDato", person.nedrePensjonsDato())
    val fullTrygdetid = Faktum("fullTrygdetid", 40)
    
    val antallMånederEtterNedrePensjonsDato = Faktum(
        "antallMånederEtterNedrePensjonsDato",
        ChronoUnit.MONTHS.between(nedrePensjonsDato.value, uttakstidspunkt).toInt().coerceAtMost(MND_36)
    )
    
    // Formler
    val G = Faktum("G", grunnbeløp)
    val fulltSlitertillegg = Faktum("fulltSlitertillegg", avrund2desimal(G * 0.25 / 12))
    var justeringsFaktor = Faktum("justeringsFaktor", 0.0)
    val trygdetidFaktor = Faktum("trygdetidFaktor", faktiskTrygdetid / fullTrygdetid)
    
    /**
     * Uttaket er innen 36 måneder etter nedre pensjonsdato.
     */
    regel("SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT-TIDLIG") {
        HVIS { antallMånederEtterNedrePensjonsDato erMindreEnn MND_36 }
        SÅ {
            justeringsFaktor = SPOR(
                Faktum("justeringsFaktor", (MND_36 - antallMånederEtterNedrePensjonsDato) / MND_36)
            )
        }
    }
    
    /**
     * Uttaket er fom 36 måneder etter nedre pensjonsdato.
     */
    regel("SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT-SENT") {
        HVIS { antallMånederEtterNedrePensjonsDato erStørreEllerLik MND_36 }
        SÅ {
            justeringsFaktor = SPOR(
                Faktum("justeringsFaktor", 0.0)
            )
        }
    }
    
    /**
     * Final calculation with all factors applied.
     */
    regel("SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT-OG-AVKORTING-TRYGDETID") {
        HVIS { true }
        RETURNER {
            Faktum("slitertillegg", avrund2desimal(fulltSlitertillegg * justeringsFaktor * trygdetidFaktor))
        }
    }
}
