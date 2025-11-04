package no.nav.pensjon.sliterordning.regelsett

import no.nav.pensjon.sliterordning.fagdata.FagKonstanter.ETT_ÅR
import no.nav.pensjon.sliterordning.fagdata.FagKonstanter.FULL_TRYGDETID
import no.nav.pensjon.sliterordning.fagdata.FagKonstanter.TRE_ÅR
import no.nav.pensjon.sliterordning.grunnlag.Person
import no.nav.system.rule.dsl.demo.ruleservice.grunnbeløpByYearMonth
import no.nav.system.rule.dsl.demo.ruleset.AbstractDemoRuleset
import no.nav.system.rule.dsl.forklaring.*
import no.nav.system.rule.dsl.rettsregel.forklartfaktum.ForklartFaktum
import java.time.YearMonth
import java.time.temporal.ChronoUnit

/**
 * Regelsett for beregning av slitertillegg - Uttrykk-versjon med full forklaring
 *
 * Denne versjonen demonstrerer integrasjonen mellom Rule DSL og Uttrykk-systemet:
 * - Bruker Uttrykk/Grunnlag for beregninger (AST-basert)
 * - Bruker faktum() for å kombinere HVORFOR (regelflyt) og HVORDAN (beregning)
 * - Gir automatisk forklaringsgenerering via Uttrykk.forklarDetaljert()
 *
 * Sammenlign med BeregnSlitertilleggRSVårVersjon for å se forskjellen.
 *
 * https://confluence.adeo.no/spaces/PEN/pages/658103196/Regelverkspesifisering#
 */
class BeregnSlitertilleggRSUttrykk(
    val uttakstidspunkt: YearMonth,
    val virkningstidspunkt: YearMonth,
    val person: Person
) : AbstractDemoRuleset<ForklartFaktum<Double>>() {

    private val grunnbeløp by lazy { grunnbeløpByYearMonth(virkningstidspunkt) }
    private val antallMånederEtterNedreAldersgrense =
        ChronoUnit.MONTHS.between(person.nedrePensjonsDato(), uttakstidspunkt)

    // Uttrykk-baserte mellomresultater (Grunnlag inneholder AST)
    private lateinit var fulltSlitertillegg: Grunnlag<Double>
    private lateinit var justeringsFaktor: Grunnlag<Double>
    private lateinit var trygdetidFaktor: Grunnlag<Double>

    override fun create() {
        regel("SLITERTILLEGG-BEREGNING-UAVKORTET") {
            HVIS { true }
            SÅ {
                fulltSlitertillegg = (Const(0.25) * Const(grunnbeløp) / Const(ETT_ÅR.toDouble()))
                    .navngi("fulltSlitertillegg")
                    .id("SLITERTILLEGG-BEREGNING-UAVKORTET")
            }
        }

        regel("SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT") {
            HVIS { antallMånederEtterNedreAldersgrense < TRE_ÅR }
            SÅ {
                justeringsFaktor = (
                    (Const(TRE_ÅR) - Const(antallMånederEtterNedreAldersgrense)) /
                    Const(TRE_ÅR.toDouble())
                ).navngi("justeringsFaktor")
                 .id("SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT")
            }
            ELLERS {
                justeringsFaktor = Const(0.0).navngi("justeringsFaktor")
            }
        }

        regel("SLITERTILLEGG-AVKORTING-TRYGDETID") {
            HVIS { true }
            SÅ {
                trygdetidFaktor = (
                    Const(person.trygdetid.faktiskTrygdetid.toDouble()) /
                    Const(FULL_TRYGDETID.toDouble())
                ).navngi("trygdetidFaktor")
                 .id("SLITERTILLEGG-AVKORTING-TRYGDETID")
            }
        }

        regel("SLITERTILEGG-BEREGNET") {
            HVIS { true }
            SÅ {
                val slitertillegg = (fulltSlitertillegg * justeringsFaktor * trygdetidFaktor)
                    .navngi("slitertillegg")
                    .id("SLITERTILLEGG-BEREGNET")

                // faktum() kombinerer:
                // - HVORFOR: Regelflyt-sporing (root().hvorfor(target = this))
                // - HVORDAN: AST-forklaring (slitertillegg.forklarDetaljert())
                RETURNER(faktum(slitertillegg))
            }
        }
    }
}
