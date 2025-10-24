package no.nav.pensjon.sliterordning.flyt

import no.nav.pensjon.sliterordning.grunnlag.Person
import no.nav.pensjon.sliterordning.regelsett.BeregnSlitertilleggRSForklartFaktumVersjon
import no.nav.pensjon.sliterordning.regelsett.VilkårsprøvSlitertilleggRS
import no.nav.system.rule.dsl.AbstractRuleflow
import no.nav.system.rule.dsl.demo.domain.Response
import no.nav.system.rule.dsl.demo.domain.Response.SliterordningForklartFaktum.Avslag
import no.nav.system.rule.dsl.demo.domain.Response.SliterordningForklartFaktum.Innvilget
import no.nav.system.rule.dsl.demo.ruleservice.grunnbeløpByYearMonth
import java.time.YearMonth

class BehandleSliterordningForklartFaktumFlyt(
    val uttakstidspunkt: YearMonth,
    val virkningstidspunkt: YearMonth,
    val person: Person
) : AbstractRuleflow<Response.SliterordningForklartFaktum>() {
    override var ruleflow: () -> Response.SliterordningForklartFaktum = {

        val innvilget = VilkårsprøvSlitertilleggRS().run(this)
        var sliterordning: Response.SliterordningForklartFaktum? = null

        forgrening("innvilget?") {

            gren {
                betingelse("Ja") { innvilget }
                flyt {
                    sliterordning = Innvilget(
                        slitertillegg = BeregnSlitertilleggRSForklartFaktumVersjon(
                            uttakstidspunkt,
                            virkningstidspunkt,
                            person,
                            grunnbeløpByYearMonth(virkningstidspunkt)
                        ).run(this)
                    )
                }
            }

            gren {
                betingelse("Nei") { !innvilget }
                flyt {
                    sliterordning = Avslag("avslag")
                }
            }
        }

        sliterordning!!
    }
}