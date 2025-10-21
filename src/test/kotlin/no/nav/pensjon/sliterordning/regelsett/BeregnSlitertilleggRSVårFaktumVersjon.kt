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

    private val antallMånederEtterNedreAldersgrense = Formel.variable<Int>(
        "antallMånederEtterNedreAldersgrense",
        ChronoUnit.MONTHS.between(person.nedrePensjonsDato(), uttakstidspunkt).toInt()
    )

    private var justeringsFaktor: Formel<Double> = Formel.variable("SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT", 0.0)

    @OptIn(DslDomainPredicate::class)
    override fun create() {

        regel("SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT") {
            HVIS { antallMånederEtterNedreAldersgrense erMindreEnn MND_36 }
            SÅ {
                justeringsFaktor = FormelBuilder.create<Double>()
                    .name("SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT")
                    .expression((MND_36 - antallMånederEtterNedreAldersgrense) / MND_36)
                    .build()
            }
        }

        SlitertilleggFaktum(
            slitertilleggBeregnet = FormelBuilder.create<Double>()
                .name("SLITERTILEGG-BEREGNET")
                .expression(fulltSlitertillegg * justeringsFaktor * trygdetidFaktor)
                .build()
        )
    }
}