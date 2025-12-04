package no.nav.pensjon.regler.sliterordning.regelsett

import no.nav.pensjon.regler.sliterordning.config.AbstractDemoRuleset
import no.nav.pensjon.regler.sliterordning.domain.Person
import no.nav.pensjon.regler.sliterordning.fagdata.FagKonstanter.MND_36
import no.nav.system.ruledsl.core.model.DslDomainPredicate
import no.nav.system.ruledsl.core.rettsregel.Faktum
import no.nav.system.ruledsl.core.rettsregel.operators.div
import no.nav.system.ruledsl.core.rettsregel.operators.erMindreEnn
import no.nav.system.ruledsl.core.rettsregel.operators.erStørreEllerLik
import no.nav.system.ruledsl.core.rettsregel.operators.minus
import no.nav.system.ruledsl.core.rettsregel.operators.times
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
        ChronoUnit.MONTHS.between(nedrePensjonsDato.verdi, uttakstidspunkt.verdi).toInt().coerceAtMost(MND_36)
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


        /**
         * Uttaket er innen 36 måneder etter nedre pensjonsdato.
         */
        regel("SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT-TIDLIG") {
            HVIS { antallMånederEtterNedrePensjonsDato erMindreEnn MND_36 }
            SÅ {
                justeringsFaktor = sporing(
                    "justeringsFaktor", (MND_36 - antallMånederEtterNedrePensjonsDato) / MND_36
                )
            }
            REF("SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT", "https://confluence.adeo.no/spaces/PEN/pages/658103196/Regelverkspesifisering")
        }

        /**
         * Uttaket er fom 36 måneder etter nedre pensjonsdato.
         */
        regel("SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT-SENT") {
            HVIS { antallMånederEtterNedrePensjonsDato erStørreEllerLik MND_36 }
            SÅ {
                justeringsFaktor = sporing(
                    "justeringsFaktor", 0.0
                )
            }
            REF("SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT", "https://confluence.adeo.no/spaces/PEN/pages/658103196/Regelverkspesifisering")
        }

        /**
         * Forklaring:
         *      HVA
         *          slitertillegg = 954.86
         *
         *      REFERANSE
         *          SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT-OG-AVKORTING-TRYGDETID
         *
         *      HVORDAN
         *          slitertillegg = fulltSlitertillegg * justeringsFaktor * trygdetidFaktor
         *          slitertillegg = 5 * 5 * 5
         *
         *          HVORDAN
         *              fulltSlitertillegg = 0.25 * G / 12
         *              fulltSlitertillegg = 0.25 * 110000 / 12
         *
         *          HVORDAN
         *              justeringsFaktor = (36 - antallMånederEtterNedrePensjonsDato) / 36
         *              justeringsFaktor = (36 - 0) / 36
         *
         *              HVORFOR
         *                  antallMånederEtterNedrePensjonsDato erMindreEnn 36
         *                  0 erMindreEnn 36
         *
         *          HVORDAN
         *              trygdetidFaktor = faktiskTrygdetid / fullTrygdetid
         *              trygdetidFaktor = 30 / 40
         */
        regel("SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT-OG-AVKORTING-TRYGDETID") {
            HVIS { true }
            SÅ {
                RETURNER(
                    sporing("slitertillegg", fulltSlitertillegg * justeringsFaktor * trygdetidFaktor)
                )
            }
            REF(
                "SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT-OG-AVKORTING-TRYGDETID",
                "https://confluence.adeo.no/spaces/PEN/pages/658103196/Regelverkspesifisering"
            )
        }
    }
}