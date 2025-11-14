package no.nav.pensjon.sliterordning.regelsett

import no.nav.system.rule.dsl.demo.ruleset.AbstractDemoRuleset
import no.nav.system.rule.dsl.rettsregel.Faktum

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