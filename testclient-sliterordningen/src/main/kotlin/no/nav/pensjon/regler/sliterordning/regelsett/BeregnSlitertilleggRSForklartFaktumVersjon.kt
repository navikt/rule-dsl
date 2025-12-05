package no.nav.pensjon.regler.sliterordning.regelsett

import no.nav.pensjon.regler.sliterordning.config.AbstractDemoRuleset
import no.nav.pensjon.regler.sliterordning.domain.Person
import no.nav.pensjon.regler.sliterordning.fagdata.FagKonstanter.FULL_TRYGDETID
import no.nav.pensjon.regler.sliterordning.fagdata.FagKonstanter.MND_36
import no.nav.system.ruledsl.core.model.DslDomainPredicate
import no.nav.system.ruledsl.core.model.Faktum
import no.nav.system.ruledsl.core.operators.div
import no.nav.system.ruledsl.core.operators.erMindreEnn
import no.nav.system.ruledsl.core.operators.minus
import no.nav.system.ruledsl.core.operators.times
import java.time.YearMonth
import java.time.temporal.ChronoUnit

/**
 * Regelsett for beregning av slitertillegg - vår versjon
 *
 * https://confluence.adeo.no/spaces/PEN/pages/658103196/Regelverkspesifisering#
 *
 */
class BeregnSlitertilleggRSForklartFaktumVersjon(
    val uttakstidspunkt: YearMonth,
    val virkningstidspunkt: YearMonth,
    val person: Person,
    val grunnbeløp: Int
) : AbstractDemoRuleset<Faktum<Double>>() {
    private val G = Faktum("G", grunnbeløp)
    private val fulltSlitertillegg: Faktum<Double> = Faktum(
        "SLITERTILLEGG-BEREGNING-UAVKORTET",
        0.25 * G / 12
    )

    private val faktiskTrygdetid = Faktum("faktiskTrygdetid", person.trygdetid.faktiskTrygdetid)
    private val trygdetidFaktor: Faktum<Double> = Faktum(
        "SLITERTILLEGG-AVKORTING-TRYGDETID",
        faktiskTrygdetid / FULL_TRYGDETID
    )

    private val antallMånederEtterNedreAldersgrense = Faktum(
        "antallMånederEtterNedreAldersgrense",
        ChronoUnit.MONTHS.between(person.nedrePensjonsDato(), uttakstidspunkt).toInt()
    )

    private var justeringsFaktor: Faktum<Double> = Faktum("SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT", 0.0)

    @OptIn(DslDomainPredicate::class)
    override fun create() {

        /**
         * Forklaring:
         *      justeringsFaktor = (MND_36 - antallMånederEtterNedreAldersgrense) / MND_36
         *      justeringsFaktor = (36 - 24)  / 36
         *      justeringsFaktor = 0.33
         *
         *      FORDI
         *          antallMånederEtterNedreAldersgrense er mindre enn MND_36
         *          24 er mindre enn 36
         *
         *          FORDI
         *              antallMånederEtterNedreAldersgrense = 24
         *
         *      HVORDAN
         *          antallMånederEtterNedreAldersgrense = 24
         *
         */
        regel("SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT") {
            HVIS { antallMånederEtterNedreAldersgrense erMindreEnn MND_36 }
            SÅ {
                justeringsFaktor = Faktum(
                    "SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT",
                    (MND_36 - antallMånederEtterNedreAldersgrense) / MND_36
                )
            }
        }

        /**
         * Forklaring:
         *      slitertillegg = fulltSlitertillegg * justeringsFaktor * trygdetidFaktor
         *      slitertillegg = 2292 * 0.33 * 0.5
         *      slitertillegg = 378
         *
         *      REFERANSE
         *          SLITERTILEGG-BEREGNET
         *
         *      FORDI
         *          BeregnSlitertilleggForklartFaktumService
         *              BehandleSliterordningForklartFaktumFlyt
         *                  forgrening: innvilget?
         *                      BetingelseNavn: JA
         *                        BeregnSlitertilleggRSForklartFaktumVersjon
         *                              BeregnSlitertillegg <---- HER ER VI. VI LAGER FAKTUM.
         *                                  predikat: JA ..
         *
         *      HVORDAN
         *          fulltSlitertillegg = 0.25 * G / 12
         *          fulltSlitertillegg = 0.25 * 110000 / 12
         *
         *          justeringsFaktor = (MND_36 - antallMånederEtterNedreAldersgrense) / MND_36
         *          justeringsFaktor = (36 - 24)  / 36
         *          justeringsFaktor = 0.33
         *          FORDI
         *              antallMånederEtterNedreAldersgrense er mindre enn MND_36
         *              24 er mindre enn 36
         *
         *              FORDI
         *                  antallMånederEtterNedreAldersgrense = 24
         *
         *          trygdetidFaktor = faktiskTrygdetid / FULL_TRYGDETID
         *          trygdetidFaktor = 20 / 40
         *          trygdetidFaktor = 0.5
         */
        regel("BeregnSlitertillegg") {
            HVIS { true }
            SÅ {
                RETURNER(
                    sporing(
                        "slitertillegg",
                        fulltSlitertillegg * justeringsFaktor * trygdetidFaktor
                    )
                )
            }
        }
    }
}