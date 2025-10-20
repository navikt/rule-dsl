package no.nav.pensjon.sliterordning.flyt

import no.nav.pensjon.sliterordning.grunnlag.Person
import no.nav.pensjon.sliterordning.regelsett.BeregnSlitertilleggRS
import no.nav.pensjon.sliterordning.resultat.Slitertillegg
import no.nav.system.rule.dsl.AbstractRuleflow
import java.time.YearMonth

class BeregnSlitertilleggFlyt(val virkningstidspunkt: YearMonth, val person: Person) :
    AbstractRuleflow<Slitertillegg>() {
    override var ruleflow: () -> Slitertillegg = {
        BeregnSlitertilleggRS(virkningstidspunkt, person).run(this)
    }
}