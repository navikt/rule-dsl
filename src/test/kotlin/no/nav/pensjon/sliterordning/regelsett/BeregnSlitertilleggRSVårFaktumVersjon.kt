package no.nav.pensjon.sliterordning.regelsett

import no.nav.pensjon.sliterordning.fagdata.FagKonstanter.FULL_TRYGDETID
import no.nav.pensjon.sliterordning.fagdata.FagKonstanter.MND_36
import no.nav.pensjon.sliterordning.grunnlag.Person
import no.nav.pensjon.sliterordning.resultat.SlitertilleggFaktum
import no.nav.system.rule.dsl.DslDomainPredicate
import no.nav.system.rule.dsl.demo.ruleset.AbstractDemoRuleset
import no.nav.system.rule.dsl.rettsregel.Faktum
import no.nav.system.rule.dsl.rettsregel.operators.div
import no.nav.system.rule.dsl.rettsregel.operators.erMindreEnn
import no.nav.system.rule.dsl.rettsregel.operators.minus
import no.nav.system.rule.dsl.rettsregel.operators.times
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
class BeregnSlitertilleggRSVårFaktumVersjon(
    val uttakstidspunkt: YearMonth,
    val virkningstidspunkt: YearMonth,
    val person: Person,
    val grunnbeløp: Int
) : AbstractDemoRuleset<SlitertilleggFaktum>() {
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

    private var justeringsFaktor: Faktum<Double> = Faktum("justeringsFaktor", 0.0)

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
                justeringsFaktor = sporing(
                    "justeringsFaktor",
                    (MND_36 - antallMånederEtterNedreAldersgrense) / MND_36
                )
            }
        }

        /**
         * Forklaring:
         *      HVA
         *      slitertillegg = fulltSlitertillegg * justeringsFaktor * trygdetidFaktor
         *      slitertillegg = 2292 * 0.33 * 0.5
         *      slitertillegg = 378
         *
         *      REFERANSE
         *          SLITERTILEGG-BEREGNET
         *
         *      FORDI / HVORFOR
         *          this.trace()..... INNVILGET = JA... FORDI PGIsnitt > 4000
         *              HVORDAN ... HVORDAN FORDI ....
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
                    SlitertilleggFaktum(
                        slitertilleggBeregnet = sporing(
                            "slitertillegg",
                            fulltSlitertillegg * justeringsFaktor * trygdetidFaktor
                        )
                    )
                )
            }
//            REFERANSE("SLITERTILEGG-BEREGNET")
        }
    }
}