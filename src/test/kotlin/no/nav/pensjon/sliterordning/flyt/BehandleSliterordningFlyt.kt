package no.nav.pensjon.sliterordning.flyt

import no.nav.pensjon.sliterordning.grunnlag.Person
import no.nav.pensjon.sliterordning.regelsett.BeregnSlitertilleggRS
import no.nav.pensjon.sliterordning.regelsett.VilkårsprøvSlitertilleggRS
import no.nav.system.rule.dsl.AbstractRuleflow
import no.nav.system.rule.dsl.demo.domain.Response
import java.time.YearMonth

class BehandleSliterordningFlyt(
    val uttakstidspunkt: YearMonth,
    val virkningstidspunkt: YearMonth,
    val person: Person
) : AbstractRuleflow<Response.Sliterordning>() {
    override var ruleflow: () -> Response.Sliterordning = {
        val innvilget = VilkårsprøvSlitertilleggRS().run(this)
        val resultat: Response.Sliterordning =
            forgrening("innvilget?") {

                gren {
                    betingelse("JA") {
                        innvilget == true
                    }
                    flyt {
                        return BeregnSlitertilleggRS(uttakstidspunkt, virkningstidspunkt, person).run(this)
                    }

                }

            }
    }
}