package no.nav.pensjon.sliterordning.regelsett

import no.nav.pensjon.sliterordning.fagdata.FagKonstanter.MND_36
import no.nav.pensjon.sliterordning.grunnlag.Person
import no.nav.system.rule.dsl.DslDomainPredicate
import no.nav.system.rule.dsl.demo.ruleset.AbstractDemoRuleset
import no.nav.system.rule.dsl.rettsregel.Faktum
import no.nav.system.rule.dsl.rettsregel.operators.*
import java.time.YearMonth
import java.time.temporal.ChronoUnit

/**
 * Regelsett for beregning av slitertillegg
 *
 * Denne versjonen legger mer vekt på Domenepredikatene, dvs reglene er spisset mot den aktuelle formel.
 *
 * https://confluence.adeo.no/spaces/PEN/pages/658103196/Regelverkspesifisering#
 *
 */
class BeregnSlitertilleggRSForklartFaktumMedDomenePredikatVersjon(
    innUttakstidspunkt: YearMonth,
    innPerson: Person,
    innGrunnbeløp: Int
) : AbstractDemoRuleset<Faktum<Double>>() {
    /**
     * Faktum data
     */
    private val faktiskTrygdetid = Faktum("faktiskTrygdetid", innPerson.trygdetid.faktiskTrygdetid)
    private val uttakstidspunkt = Faktum("uttakstidspunkt", innUttakstidspunkt)
    private val nedrePensjonsDato = Faktum("nedrePensjonsDato", innPerson.nedrePensjonsDato())
    private val fullTrygdetid = Faktum("fullTrygdetid", 40)

    /**
     * Faktum beregnet
     */
    private val antallMånederEtterNedrePensjonsDato = Faktum(
        "antallMånederEtterNedrePensjonsDato",
        ChronoUnit.MONTHS.between(nedrePensjonsDato.verdi, uttakstidspunkt.verdi).toInt().coerceAtMost(MND_36)
    )
    private val G = Faktum("G", innGrunnbeløp)
    private val fulltSlitertillegg: Faktum<Double> = Faktum("fulltSlitertillegg", 0.25 * G / 12)
    private val justeringsFaktor: Faktum<Double> = Faktum("justeringsFaktor", (MND_36 - antallMånederEtterNedrePensjonsDato) / MND_36)
    private val trygdetidFaktor: Faktum<Double> = Faktum("trygdetidFaktor", faktiskTrygdetid / fullTrygdetid.verdi)

    @OptIn(DslDomainPredicate::class)
    override fun create() {

        /**
         * Forklaring:
         *      HVA
         *          slitertillegg = 2291.67
         *
         *      REFERANSE
         *          SLITERTILLEGG-BEREGNING-UAVKORTET
         *
         *      FORDI
         *          nedrePensjonsDato er lik uttakstidspunkt
         *          2024-01 er lik 2024-01
         *
         *          OG
         *
         *          faktiskTrygdetid er lik fullTrygdetid
         *          40 er lik 40
         *
         *      HVORDAN
         *          slitertillegg = 0.25 * G / 12
         *          slitertillegg = 0.25 * 110000 / 12
         */
        regel("SLITERTILLEGG-BEREGNING-UAVKORTET") {
            HVIS { nedrePensjonsDato erLik uttakstidspunkt } // evt antallMånederEtterNedrePensjonsDato erLik 0
            OG { faktiskTrygdetid erLik fullTrygdetid }
            SÅ {
                RETURNER(
                    sporing("slitertillegg", fulltSlitertillegg)
                )
            }
        }

        /**
         * Forklaring:
         *      HVA
         *          slitertillegg = 1718.75
         *
         *      REFERANSE
         *          SLITERTILLEGG-AVKORTING-TRYGDETID
         *
         *      FORDI
         *          nedrePensjonsDato er lik uttakstidspunkt
         *          2024-01 er lik 2024-01
         *
         *          OG
         *
         *          faktiskTrygdetid er mindre enn fullTrygdetid
         *          30 er mindre enn 40
         *
         *      HVORDAN
         *          slitertillegg = fulltSlitertillegg * trygdetidFaktor
         *          slitertillegg = 5 * 5
         *
         *          fulltSlitertillegg = 0.25 * G / 12
         *          fulltSlitertillegg = 0.25 * 110000 / 12
         *
         *          trygdetidFaktor = faktiskTrygdetid / fullTrygdetid
         *          trygdetidFaktor = 30 / 40
         */
        regel("SLITERTILLEGG-AVKORTING-TRYGDETID") {
            HVIS { nedrePensjonsDato erLik uttakstidspunkt } // evt antallMånederEtterNedrePensjonsDato erLik 0
            OG { faktiskTrygdetid erMindreEnn fullTrygdetid }
            SÅ {
                RETURNER(
                    sporing("slitertillegg", fulltSlitertillegg * trygdetidFaktor)
                )
            }
        }

        /**
         * Forklaring:
         *      HVA
         *          slitertillegg = 1273.15
         *
         *      REFERANSE
         *          SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT
         *
         *      FORDI
         *          nedrePensjonsDato er før uttakstidspunkt
         *          2022-09 er før 2024-01
         *
         *          OG
         *
         *          faktiskTrygdetid er lik fullTrygdetid
         *          40 er lik 40
         *
         *      HVORDAN
         *          slitertillegg = fulltSlitertillegg * justeringsFaktor
         *          slitertillegg = 5 * 5
         *
         *          fulltSlitertillegg = 0.25 * G / 12
         *          fulltSlitertillegg = 0.25 * 110000 / 12
         *
         *          justeringsFaktor = MND_36 - antallMånederEtterNedreAldersgrense) / MND_36
         *          justeringsFaktor = (36 - 16) / 36
         */
        regel("SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT") {
            HVIS { nedrePensjonsDato erFør uttakstidspunkt } // evt antallMånederEtterNedrePensjonsDato erLik 0
            OG { faktiskTrygdetid erLik fullTrygdetid }
            SÅ {
                RETURNER(
                    sporing("slitertillegg", fulltSlitertillegg * justeringsFaktor)
                )
            }
        }

        /**
         * Forklaring:
         *      HVA
         *          slitertillegg = 954.86
         *
         *      REFERANSE
         *          SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT-OG-AVKORTING-TRYGDETID
         *
         *      FORDI
         *          nedrePensjonsDato er før uttakstidspunkt
         *          2022-09 er før 2024-01
         *
         *          OG
         *
         *          faktiskTrygdetid er mindre enn fullTrygdetid
         *          30 er mindre enn 40
         *
         *      HVORDAN
         *          slitertillegg = fulltSlitertillegg * justeringsFaktor * trygdetidFaktor
         *          slitertillegg = 5 * 5 * 5
         *
         *          fulltSlitertillegg = 0.25 * G / 12
         *          fulltSlitertillegg = 0.25 * 110000 / 12
         *
         *          justeringsFaktor = MND_36 - antallMånederEtterNedreAldersgrense) / MND_36
         *          justeringsFaktor = (36 - 16) / 36
         *
         *          trygdetidFaktor = faktiskTrygdetid / fullTrygdetid
         *          trygdetidFaktor = 30 / 40
         */
        regel("SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT-OG-AVKORTING-TRYGDETID") {
            HVIS { nedrePensjonsDato erFør uttakstidspunkt } // evt antallMånederEtterNedrePensjonsDato erLik 0
            OG { faktiskTrygdetid erMindreEnn fullTrygdetid }
            SÅ {
                RETURNER(
                    sporing("slitertillegg", fulltSlitertillegg * justeringsFaktor * trygdetidFaktor)
                )
            }
        }

    }
}