package no.nav.pensjon.regler.sliterordning.flyt

import no.nav.pensjon.regler.sliterordning.config.grunnbeløpByYearMonth
import no.nav.pensjon.regler.sliterordning.domain.Person
import no.nav.pensjon.regler.sliterordning.regelsett.BeregnSlitertilleggRSFaktumMedDomenePredikatSekvensielleReglerVersjon
import no.nav.pensjon.regler.sliterordning.regelsett.VilkårsprøvSlitertilleggRS
import no.nav.pensjon.regler.sliterordning.to.Response
import no.nav.system.ruledsl.core.model.AbstractRuleflow
import no.nav.system.ruledsl.core.operators.erLik
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
                    sliterordning = Response.SliterordningForklartFaktum.Innvilget(
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
                    sliterordning = Response.SliterordningForklartFaktum.Avslag("avslag")
                }
            }
        }

        sliterordning!!
    }
}