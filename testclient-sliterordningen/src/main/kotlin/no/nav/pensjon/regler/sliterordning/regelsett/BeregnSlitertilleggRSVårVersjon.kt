package no.nav.pensjon.regler.sliterordning.regelsett

import no.nav.pensjon.regler.sliterordning.config.AbstractDemoRuleset
import no.nav.pensjon.regler.sliterordning.config.grunnbeløpByYearMonth
import no.nav.pensjon.regler.sliterordning.domain.Person
import no.nav.pensjon.regler.sliterordning.domain.SlitertilleggVårVersjon
import no.nav.pensjon.regler.sliterordning.fagdata.FagKonstanter.FULL_TRYGDETID
import no.nav.pensjon.regler.sliterordning.fagdata.FagKonstanter.MND_12
import no.nav.pensjon.regler.sliterordning.fagdata.FagKonstanter.MND_36
import java.time.YearMonth
import java.time.temporal.ChronoUnit

/**
 * Regelsett for beregning av slitertillegg - vår versjon
 *
 * https://confluence.adeo.no/spaces/PEN/pages/658103196/Regelverkspesifisering#
 *
 *
 *
 */
class BeregnSlitertilleggRSVårVersjon(
    val uttakstidspunkt: YearMonth,
    val virkningstidspunkt: YearMonth,
    val person: Person
) : AbstractDemoRuleset<SlitertilleggVårVersjon>() {

    private val grunnbeløp by lazy { grunnbeløpByYearMonth(virkningstidspunkt) }
    private val antallMånederEtterNedreAldersgrense =
        ChronoUnit.MONTHS.between(person.nedrePensjonsDato(), uttakstidspunkt)

    private var fulltSlitertillegg: Double = 0.0
    private var justeringsFaktor: Double = 0.0
    private var trygdetidFaktor: Double = 0.0
    private var slitertilleggBeregnet: Double = 0.0

    override fun create() {
        regel("SLITERTILLEGG-BEREGNING-UAVKORTET") {
            HVIS { true }
            SÅ {
                fulltSlitertillegg = 0.25 * grunnbeløp / MND_12
            }
        }

        regel("SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT") {
            HVIS { antallMånederEtterNedreAldersgrense < MND_36 }
            SÅ {
                justeringsFaktor = (MND_36 - antallMånederEtterNedreAldersgrense) / MND_36.toDouble()
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