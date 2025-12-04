package no.nav.pensjon.regler.sliterordning.flyt

import no.nav.pensjon.regler.sliterordning.domain.Person
import no.nav.pensjon.regler.sliterordning.regelsett.BeregnSlitertilleggRS
import no.nav.pensjon.regler.sliterordning.regelsett.VilkårsprøvSlitertilleggRS
import no.nav.pensjon.regler.sliterordning.to.Response
import no.nav.system.ruledsl.core.model.AbstractRuleflow
import no.nav.system.ruledsl.core.model.DslDomainPredicate
import no.nav.system.ruledsl.core.rettsregel.operators.erLik

import java.time.YearMonth

class BehandleSliterordningFlyt(
    val uttakstidspunkt: YearMonth,
    val virkningstidspunkt: YearMonth,
    val person: Person
) : AbstractRuleflow<Response.Sliterordning>() {
    @OptIn(DslDomainPredicate::class)
    override var ruleflow: () -> Response.Sliterordning = {

        val innvilget = VilkårsprøvSlitertilleggRS().run(this)

        var sliterordning: Response.Sliterordning? = null

        forgrening("innvilget?") {
            gren {
                betingelse("JA") { innvilget erLik true }
                flyt {
                    sliterordning = Response.Sliterordning.Innvilget(
                        BeregnSlitertilleggRS(uttakstidspunkt, virkningstidspunkt, person).run(this)
                    )
                }
            }

            gren {
                betingelse("NEI") { innvilget erLik false }
                flyt {
                    sliterordning = Response.Sliterordning.Avslag("avslag")
                }
            }
        }

        sliterordning!!
    }
}