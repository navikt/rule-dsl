package no.nav.pensjon.regler.sliterordning.regelsett


import no.nav.pensjon.regler.sliterordning.config.AbstractDemoRuleset
import no.nav.pensjon.regler.sliterordning.config.grunnbeløpByYearMonth
import no.nav.pensjon.regler.sliterordning.domain.Person
import no.nav.pensjon.regler.sliterordning.domain.Slitertillegg
import no.nav.pensjon.regler.sliterordning.fagdata.FagKonstanter.FULL_TRYGDETID
import no.nav.pensjon.regler.sliterordning.fagdata.FagKonstanter.MND_12
import no.nav.pensjon.regler.sliterordning.fagdata.FagKonstanter.MND_36
import no.nav.system.ruledsl.core.model.DslDomainPredicate
import no.nav.system.ruledsl.core.model.Faktum
import no.nav.system.ruledsl.core.operators.erMindreEnn

import java.time.YearMonth
import java.time.temporal.ChronoUnit

class BeregnSlitertilleggRS(
    val uttakstidspunkt: YearMonth,
    val virkningstidspunkt: YearMonth,
    val person: Person
) : AbstractDemoRuleset<Slitertillegg>() {

    private val grunnbeløp by lazy { grunnbeløpByYearMonth(virkningstidspunkt) }
    private val antallMånederEtterNedreAldersgrense = Faktum(
        "antallMånederEtterNedreAldersgrense",
        ChronoUnit.MONTHS.between(person.nedrePensjonsDato(), uttakstidspunkt)
    )

    private var fulltSlitertillegg: Double = 0.0
    private var justertSlitertillegg: Double = 0.0
    private var avkortetSlitertilleggEtterTrygdetid: Double = 0.0
    private var slitertilleggBeregnet: Double = 0.0

    @OptIn(DslDomainPredicate::class)
    override fun create() {
        regel("SLITERTILLEGG-BEREGNING-UAVKORTET") {
            HVIS { true }
            SÅ {
                fulltSlitertillegg = 0.25 * grunnbeløp / MND_12
            }
        }

        regel("SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT") {
            HVIS { antallMånederEtterNedreAldersgrense erMindreEnn MND_36 }
            SÅ {
                justertSlitertillegg =
                    fulltSlitertillegg * ((MND_36 - antallMånederEtterNedreAldersgrense.verdi) / MND_36.toDouble())
            }
            ELLERS {
                justertSlitertillegg = 0.0
            }
        }

        regel("SLITERTILLEGG-AVKORTING-TRYGDETID") {
            HVIS { true }
            SÅ {
                avkortetSlitertilleggEtterTrygdetid =
                    fulltSlitertillegg * (person.trygdetid.faktiskTrygdetid / FULL_TRYGDETID.toDouble())
            }
        }

        regel("SLITERTILEGG-BEREGNET") {
            HVIS { true }
            SÅ {
                slitertilleggBeregnet =
                    fulltSlitertillegg * ((MND_36 - antallMånederEtterNedreAldersgrense.verdi) / MND_36.toDouble()) * (person.trygdetid.faktiskTrygdetid / FULL_TRYGDETID.toDouble())
            }
        }

        regel("SLITERTILLEGG-RESULTAT") {
            HVIS { true }
            SÅ {
                RETURNER(
                    Slitertillegg(
                        grunnbeløp = grunnbeløp,
                        antallMånederEtterNedreAldersgrense = antallMånederEtterNedreAldersgrense.verdi,
                        fulltSlitertillegg = fulltSlitertillegg,
                        justertSlitertillegg = justertSlitertillegg,
                        avkortetSlitertilleggEtterTrygdetid = avkortetSlitertilleggEtterTrygdetid,
                        slitertilleggBeregnet = slitertilleggBeregnet
                    )
                )
            }
        }
    }
}