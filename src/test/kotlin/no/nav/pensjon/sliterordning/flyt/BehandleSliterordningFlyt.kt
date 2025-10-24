package no.nav.pensjon.sliterordning.flyt

import no.nav.pensjon.sliterordning.grunnlag.Person
import no.nav.pensjon.sliterordning.regelsett.BeregnSlitertilleggRS
import no.nav.pensjon.sliterordning.regelsett.VilkårsprøvSlitertilleggRS
import no.nav.system.rule.dsl.AbstractRuleflow
import no.nav.system.rule.dsl.demo.domain.Response
import no.nav.system.rule.dsl.demo.domain.Response.Sliterordning.*
import java.time.YearMonth

class BehandleSliterordningFlyt(
    val uttakstidspunkt: YearMonth,
    val virkningstidspunkt: YearMonth,
    val person: Person
) : AbstractRuleflow<Response.Sliterordning>() {
    override var ruleflow: () -> Response.Sliterordning = {

        val innvilget = VilkårsprøvSlitertilleggRS().run(this)
        var sliterordning: Response.Sliterordning? = null

        forgrening("innvilget?") {
            gren {
                betingelse("JA") { innvilget }
                flyt {
                    sliterordning = Innvilget(
                        BeregnSlitertilleggRS(uttakstidspunkt, virkningstidspunkt, person).run(this)
                    )
                }
            }

            gren {
                betingelse("NEI") { !innvilget }
                flyt {
                    sliterordning = Avslag("avslag")
                }
            }
        }

        sliterordning!!
    }
}