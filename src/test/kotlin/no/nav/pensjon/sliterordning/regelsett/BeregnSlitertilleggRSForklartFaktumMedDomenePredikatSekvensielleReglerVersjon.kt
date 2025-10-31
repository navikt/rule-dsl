package no.nav.pensjon.sliterordning.regelsett

import no.nav.pensjon.sliterordning.fagdata.FagKonstanter.MND_36
import no.nav.pensjon.sliterordning.grunnlag.Person
import no.nav.system.rule.dsl.DslDomainPredicate
import no.nav.system.rule.dsl.demo.ruleset.AbstractDemoRuleset
import no.nav.system.rule.dsl.formel.*
import no.nav.system.rule.dsl.rettsregel.Faktum
import no.nav.system.rule.dsl.rettsregel.erMindreEnn
import no.nav.system.rule.dsl.rettsregel.erStørreEllerLik
import no.nav.system.rule.dsl.rettsregel.forklartfaktum.ForklartFaktum
import java.time.YearMonth
import java.time.temporal.ChronoUnit

/**
 *
 * WIP
 *
 * Regelsett for beregning av slitertillegg
 *
 * Denne versjonen legger mer vekt på Domenepredikatene, dvs reglene er spisset mot den aktuelle formel.
 *
 * https://confluence.adeo.no/spaces/PEN/pages/658103196/Regelverkspesifisering#
 *
 * WIP
 *
 */
class BeregnSlitertilleggRSForklartFaktumMedDomenePredikatSekvensielleReglerVersjon(
    innUttakstidspunkt: YearMonth,
    innPerson: Person,
    innGrunnbeløp: Int
) : AbstractDemoRuleset<ForklartFaktum<Double>>() {
    /**
     * Faktum
     */
    private val faktiskTrygdetid = Formel.variable("faktiskTrygdetid", innPerson.trygdetid.faktiskTrygdetid)
    private val uttakstidspunkt = Faktum("uttakstidspunkt", innUttakstidspunkt)
    private val nedrePensjonsDato = Faktum("nedrePensjonsDato", innPerson.nedrePensjonsDato())
    private val fullTrygdetid = Faktum("fullTrygdetid", 40)

    /**
     * Formler
     */
    private val antallMånederEtterNedrePensjonsDato = Formel.variable(
        "antallMånederEtterNedrePensjonsDato",
        ChronoUnit.MONTHS.between(nedrePensjonsDato.value, uttakstidspunkt.value).toInt().coerceAtMost(MND_36)
    )
    private val G = Formel.variable("G", innGrunnbeløp)
    private val fulltSlitertillegg: Formel<Double> = formula("fulltSlitertillegg") { expression(0.25 * G / 12) }
    private lateinit var justeringsFaktor: Faktum<Double>
    private val trygdetidFaktor: Formel<Double> = formula("trygdetidFaktor") { expression(faktiskTrygdetid / fullTrygdetid.value) }

    @OptIn(DslDomainPredicate::class)
    override fun create() {

        regel("SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT-TIDLIG") {
            HVIS { antallMånederEtterNedrePensjonsDato erMindreEnn MND_36 }
            SÅ {
                justeringsFaktor = faktum(
                    formula("justeringsFaktor") { expression((MND_36 - antallMånederEtterNedrePensjonsDato) / MND_36) }
                )
            }
        }

        regel("SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT-SENT") {
            HVIS { antallMånederEtterNedrePensjonsDato erStørreEllerLik MND_36 }
            SÅ {
                justeringsFaktor = faktum(
                    variable("justeringsFaktor", 0.0)
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
            HVIS { true }
            SÅ {
                RETURNER(
                    faktum(
                        formula("slitertillegg") {
                            expression(fulltSlitertillegg )
//                            expression(fulltSlitertillegg * justeringsFaktor)
                        }
                    )
                )
            }
        }

    }
}