package no.nav.pensjon.sliterordning.regelsett

import no.nav.pensjon.sliterordning.fagdata.FagKonstanter.FULL_TRYGDETID
import no.nav.pensjon.sliterordning.fagdata.FagKonstanter.MND_36
import no.nav.pensjon.sliterordning.grunnlag.Person
import no.nav.pensjon.sliterordning.resultat.SlitertilleggFaktum
import no.nav.system.rule.dsl.DslDomainPredicate
import no.nav.system.rule.dsl.demo.ruleset.AbstractDemoRuleset
import no.nav.system.rule.dsl.formel.*
import no.nav.system.rule.dsl.rettsregel.erMindreEnn
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
    private val G = Formel.variable("G", grunnbeløp)
    private val fulltSlitertillegg: Formel<Double> = FormelBuilder.create<Double>()
        .name("SLITERTILLEGG-BEREGNING-UAVKORTET")
        .expression(0.25 * G / 12)
        .build()

    private val faktiskTrygdetid = Formel.variable("faktiskTrygdetid", person.trygdetid.faktiskTrygdetid)
    private val trygdetidFaktor: Formel<Double> = FormelBuilder.create<Double>()
        .name("SLITERTILLEGG-AVKORTING-TRYGDETID")
        .expression(faktiskTrygdetid / FULL_TRYGDETID)
        .build()

    private val antallMånederEtterNedreAldersgrense = Formel.variable(
        "antallMånederEtterNedreAldersgrense",
        ChronoUnit.MONTHS.between(person.nedrePensjonsDato(), uttakstidspunkt).toInt()
    )

    private var justeringsFaktor: Formel<Double> = Formel.variable("justeringsFaktor", 0.0)

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
                justeringsFaktor = FormelBuilder.create<Double>()
                    .name("justeringsFaktor")
                    .expression((MND_36 - antallMånederEtterNedreAldersgrense) / MND_36)
                    .build()
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
                        slitertilleggBeregnet = formula("slitertillegg") {
                            expression(fulltSlitertillegg * justeringsFaktor * trygdetidFaktor)
                        }
                    )
                )
            }
//            REFERANSE("SLITERTILEGG-BEREGNET")
        }
    }
}