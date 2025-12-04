package no.nav.pensjon.regler.sliterordning.regelsett

import no.nav.pensjon.regler.sliterordning.config.AbstractDemoRuleset
import no.nav.system.ruledsl.core.rettsregel.Faktum

/**
 * Vilkårsprøving for slitertillegg.
 *
 * For testing purposes, this simple implementation always grants approval (returns true).
 * In a production system, this would contain actual eligibility rules.
 */
class VilkårsprøvSlitertilleggRS() : AbstractDemoRuleset<Faktum<Boolean>>() {
    override fun create() {
        regel("ALLTID-INNVILGET") {
            HVIS { true }
            SÅ {
                RETURNER(
                    sporing(
                        "Vilkår Slitertillegg", true
                    )
                )
            }
        }
    }
}