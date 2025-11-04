package no.nav.pensjon.sliterordning.regelsett

import no.nav.pensjon.sliterordning.fagdata.FagKonstanter.ETT_ÅR
import no.nav.pensjon.sliterordning.fagdata.FagKonstanter.FULL_TRYGDETID
import no.nav.pensjon.sliterordning.fagdata.FagKonstanter.TRE_ÅR
import no.nav.pensjon.sliterordning.grunnlag.Person
import no.nav.system.rule.dsl.demo.ruleservice.grunnbeløpByYearMonth
import no.nav.system.rule.dsl.demo.ruleset.AbstractDemoRuleset
import no.nav.system.rule.dsl.formel.Formel
import java.time.YearMonth
import java.time.temporal.ChronoUnit

/**
 * Regelsett for beregning av slitertillegg - Formel-versjon med regelflyt-forklaring.
 *
 * Denne versjonen demonstrerer tradisjonell tilnærming:
 * - Bruker vanlig Double og Formel for beregninger
 * - Bruker medForklaring() fra AbstractRuleset for regelflyt-sporing
 * - Gir kun HVORFOR forklaring (regelflyt), ikke HVORDAN (beregningsdetaljer)
 *
 * Sammenlign med BeregnSlitertilleggRSUttrykk for å se forskjellen.
 *
 * https://confluence.adeo.no/spaces/PEN/pages/658103196/Regelverkspesifisering#
 */
class BeregnSlitertilleggRSFormel(
    val uttakstidspunkt: YearMonth,
    val virkningstidspunkt: YearMonth,
    val person: Person
) : AbstractDemoRuleset<Double>() {

    private val grunnbeløp by lazy { grunnbeløpByYearMonth(virkningstidspunkt) }
    private val antallMånederEtterNedreAldersgrense =
        ChronoUnit.MONTHS.between(person.nedrePensjonsDato(), uttakstidspunkt)

    // Vanlige Double-verdier (ikke Uttrykk/Grunnlag)
    private var fulltSlitertillegg: Double = 0.0
    private var justeringsFaktor: Double = 0.0
    private var trygdetidFaktor: Double = 0.0

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
            ELLERS {
                justeringsFaktor = 0.0
            }
        }

        regel("SLITERTILLEGG-AVKORTING-TRYGDETID") {
            HVIS { true }
            SÅ {
                trygdetidFaktor = person.trygdetid.faktiskTrygdetid.toDouble() / FULL_TRYGDETID
            }
        }

        regel("SLITERTILEGG-BEREGNET") {
            HVIS { true }
            SÅ {
                val slitertillegg = fulltSlitertillegg * justeringsFaktor * trygdetidFaktor
                RETURNER(slitertillegg)
            }
        }
    }
}
