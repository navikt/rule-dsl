package no.nav.pensjon.regler.sliterordning.flyt

import no.nav.pensjon.regler.sliterordning.config.grunnbeløpByYearMonth
import no.nav.pensjon.regler.sliterordning.domain.Person
import no.nav.pensjon.regler.sliterordning.regelsett.BeregnSlitertilleggRS
import no.nav.pensjon.regler.sliterordning.regelsett.VilkårsprøvSlitertilleggRS
import no.nav.pensjon.regler.sliterordning.to.SliterordningResponse
import no.nav.pensjon.regler.sliterordning.to.SliterordningResponse.*
import no.nav.system.ruledsl.core.model.AbstractRuleflow
import no.nav.system.ruledsl.core.model.DslDomainPredicate
import no.nav.system.ruledsl.core.operators.erLik
import java.time.YearMonth

class BehandleSliterordningFlyt(
    val uttakstidspunkt: YearMonth,
    val virkningstidspunkt: YearMonth,
    val person: Person
) : AbstractRuleflow<SliterordningResponse>() {
    @OptIn(DslDomainPredicate::class)
    override var ruleflow: () -> SliterordningResponse = {

        val vilkårStatus = VilkårsprøvSlitertilleggRS().run(this)
        var sliterordning: SliterordningResponse? = null

        forgrening("Vilkår status?") {

            gren {
                betingelse("Innvilget") { vilkårStatus erLik true }
                flyt {
                    sliterordning = Innvilget(
                        slitertillegg = BeregnSlitertilleggRS(
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