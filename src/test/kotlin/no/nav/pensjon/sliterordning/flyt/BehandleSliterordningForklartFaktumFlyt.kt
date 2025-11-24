package no.nav.pensjon.sliterordning.flyt

import no.nav.pensjon.sliterordning.grunnlag.Person
import no.nav.pensjon.sliterordning.regelsett.BeregnSlitertilleggRSFaktumMedDomenePredikatSekvensielleReglerVersjon
import no.nav.pensjon.sliterordning.regelsett.VilkårsprøvSlitertilleggRS
import no.nav.system.rule.dsl.AbstractRuleflow
import no.nav.system.rule.dsl.demo.domain.Response
import no.nav.system.rule.dsl.demo.domain.Response.SliterordningForklartFaktum.Avslag
import no.nav.system.rule.dsl.demo.domain.Response.SliterordningForklartFaktum.Innvilget
import no.nav.system.rule.dsl.demo.ruleservice.grunnbeløpByYearMonth
import no.nav.system.rule.dsl.rettsregel.operators.erLik
import java.time.YearMonth

class BehandleSliterordningForklartFaktumFlyt(
    val uttakstidspunkt: YearMonth,
    val virkningstidspunkt: YearMonth,
    val person: Person
) : AbstractRuleflow<Response.SliterordningForklartFaktum>() {
    override var ruleflow: () -> Response.SliterordningForklartFaktum = {

        val vilkårStatus = VilkårsprøvSlitertilleggRS().run(this)
        var sliterordning: Response.SliterordningForklartFaktum? = null

        forgrening("Vilkår status?") {

            gren {
                betingelse("Innvilget") { vilkårStatus erLik true }
                flyt {
                    sliterordning = Innvilget(
                        slitertillegg = BeregnSlitertilleggRSFaktumMedDomenePredikatSekvensielleReglerVersjon(
                            uttakstidspunkt,
                            person,
                            grunnbeløpByYearMonth(virkningstidspunkt)
                        ).run(this)
                    )
                }
            }

            gren {
                betingelse("Avslag") { vilkårStatus erLik false }
                flyt {
                    sliterordning = Avslag("avslag")
                }
            }
        }

        sliterordning!!
    }
}