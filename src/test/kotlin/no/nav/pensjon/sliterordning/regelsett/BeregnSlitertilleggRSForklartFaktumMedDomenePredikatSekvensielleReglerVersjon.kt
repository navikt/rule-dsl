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
 *
 * Regelsett for beregning av slitertillegg
 *
 * Denne versjonen legger mer vekt på Domenepredikatene, dvs reglene er spisset mot den aktuelle formel.
 *
 * https://confluence.adeo.no/spaces/PEN/pages/658103196/Regelverkspesifisering#
 *
 */
class BeregnSlitertilleggRSFaktumMedDomenePredikatSekvensielleReglerVersjon(
    innUttakstidspunkt: YearMonth,
    innPerson: Person,
    innGrunnbeløp: Int
) : AbstractDemoRuleset<Faktum<Double>>() {
    /**
     * Faktum
     */
    private val faktiskTrygdetid = Faktum("faktiskTrygdetid", innPerson.trygdetid.faktiskTrygdetid)
    private val uttakstidspunkt = Faktum("uttakstidspunkt", innUttakstidspunkt)
    private val nedrePensjonsDato = Faktum("nedrePensjonsDato", innPerson.nedrePensjonsDato())
    private val fullTrygdetid = Faktum("fullTrygdetid", 40)

    /**
     * Formler
     */
    private val antallMånederEtterNedrePensjonsDato = Faktum(
        "antallMånederEtterNedrePensjonsDato",
        ChronoUnit.MONTHS.between(nedrePensjonsDato.evaluer(), uttakstidspunkt.evaluer()).toInt().coerceAtMost(MND_36)
    )

    private val G = Faktum("G", innGrunnbeløp)
    private val fulltSlitertillegg = Faktum("fulltSlitertillegg", 0.25 * G / 12)
    private var justeringsFaktor = Faktum("justeringsFaktor", 0.0)
    private val trygdetidFaktor = Faktum("trygdetidFaktor", faktiskTrygdetid / fullTrygdetid)

    @OptIn(DslDomainPredicate::class)
    override fun create() {

        /**
         * justeringsFaktor.forklar():
         * HVA
         *    justeringsFaktor = 1.0
         *
         * HVORFOR
         *    antallMånederEtterNedrePensjonsDato erMindreEnn 36
         *    0 erMindreEnn 36
         *
         * HVORDAN
         *    justeringsFaktor = (36 - antallMånederEtterNedrePensjonsDato) / 36
         *    justeringsFaktor = (36 - 0) / 36
         *    justeringsFaktor = 1.0
         */
        regel("SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT-TIDLIG") {
            HVIS { antallMånederEtterNedrePensjonsDato erMindreEnn MND_36 }
            SÅ {
                justeringsFaktor = forklaring(
                    "justeringsFaktor", (MND_36 - antallMånederEtterNedrePensjonsDato) / MND_36
                )
            }
        }

        regel("SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT-SENT") {
            HVIS { antallMånederEtterNedrePensjonsDato erStørreEllerLik MND_36 }
            SÅ {
                justeringsFaktor = forklaring(
                    "justeringsFaktor", 0.0
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
         *          HVA
         *              fulltSlitertillegg = 1.0
         *
         *          HVORDAN
         *              fulltSlitertillegg = 0.25 * G / 12
         *              fulltSlitertillegg = 0.25 * 110000 / 12
         *
         *          HVA
         *              justeringsFaktor = 1.0
         *
         *           HVORFOR
         *              antallMånederEtterNedrePensjonsDato erMindreEnn 36
         *              0 erMindreEnn 36
         *
         *           HVORDAN
         *              justeringsFaktor = (36 - antallMånederEtterNedrePensjonsDato) / 36
         *              justeringsFaktor = (36 - 0) / 36
         *              justeringsFaktor = 1.0
         *
         *          trygdetidFaktor = faktiskTrygdetid / fullTrygdetid
         *          trygdetidFaktor = 30 / 40
         */
        regel("SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT-OG-AVKORTING-TRYGDETID") {
            HVIS { true }
            SÅ {
                RETURNER(
                    forklaring("slitertillegg", fulltSlitertillegg * justeringsFaktor * trygdetidFaktor)
                )
            }
        }
    }
}