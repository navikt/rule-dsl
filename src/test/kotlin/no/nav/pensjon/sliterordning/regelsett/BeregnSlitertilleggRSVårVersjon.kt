package no.nav.pensjon.sliterordning.regelsett

import no.nav.pensjon.sliterordning.fagdata.FagKonstanter.ETT_ÅR
import no.nav.pensjon.sliterordning.fagdata.FagKonstanter.FULL_TRYGDETID
import no.nav.pensjon.sliterordning.fagdata.FagKonstanter.TRE_ÅR
import no.nav.pensjon.sliterordning.grunnlag.Person
import no.nav.pensjon.sliterordning.resultat.SlitertilleggVårVersjon
import no.nav.system.rule.dsl.AbstractRuleset
import no.nav.system.rule.dsl.demo.ruleservice.grunnbeløpByYearMonth
import no.nav.system.rule.dsl.demo.ruleset.AbstractDemoRuleset
import java.time.YearMonth
import java.time.temporal.ChronoUnit


class BeregnSlitertilleggRSVårVersjon(
    val virkningstidspunkt: YearMonth,
    val person: Person
) : AbstractDemoRuleset<SlitertilleggVårVersjon>() {

    private val grunnbeløp by lazy { grunnbeløpByYearMonth(virkningstidspunkt) }
    private val antallMånederEtterNedreAldersgrense =
        ChronoUnit.MONTHS.between(person.nedrePensjonsDato(), virkningstidspunkt)

    private var fulltSlitertillegg: Double = 0.0
    private var justeringsFaktor: Double = 0.0
    private var trygdetidFaktor: Double = 0.0
    private var slitertilleggBeregnet: Double = 0.0

    override fun create() {
        regel("SLITERTILLEGG-BEREGNING-UAVKORTET") {
            HVIS { true }
            SÅ {
                fulltSlitertillegg = 0.25 * grunnbeløp / ETT_ÅR
            }
        }

        regel("SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT") {
            HVIS { antallMånederEtterNedreAldersgrense < TRE_ÅR }
            SÅ {
                justeringsFaktor = (TRE_ÅR - antallMånederEtterNedreAldersgrense) / TRE_ÅR.toDouble()
            }
        }

        regel("SLITERTILLEGG-AVKORTING-TRYGDETID") {
            HVIS { true }
            SÅ {
                trygdetidFaktor = person.trygdetid.faktiskTrygdetid / FULL_TRYGDETID.toDouble()
            }
        }

        regel("SLITERTILEGG-BEREGNET") {
            HVIS { true }
            SÅ {
                slitertilleggBeregnet = fulltSlitertillegg * justeringsFaktor * trygdetidFaktor
            }
        }

        regel("SLITERTILLEGG-RESULTAT") {
            HVIS { true }
            SÅ {
                RETURNER(
                    SlitertilleggVårVersjon(
                        grunnbeløp = grunnbeløp,
                        antallMånederEtterNedreAldersgrense = antallMånederEtterNedreAldersgrense,
                        slitertilleggBeregnet = slitertilleggBeregnet
                    )
                )
            }
        }
    }
}