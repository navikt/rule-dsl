package no.nav.pensjon.sliterordning.regelsett

import no.nav.pensjon.sliterordning.fagdata.FagKonstanter.ETT_ÅR
import no.nav.pensjon.sliterordning.fagdata.FagKonstanter.FULL_TRYGDETID
import no.nav.pensjon.sliterordning.fagdata.FagKonstanter.TRE_ÅR
import no.nav.pensjon.sliterordning.grunnlag.Person
import no.nav.pensjon.sliterordning.resultat.Slitertillegg
import no.nav.system.rule.dsl.DslDomainPredicate
import no.nav.system.rule.dsl.demo.ruleservice.grunnbeløpByYearMonth
import no.nav.system.rule.dsl.demo.ruleset.AbstractDemoRuleset
import no.nav.system.rule.dsl.rettsregel.Faktum
import no.nav.system.rule.dsl.rettsregel.erMindreEnn
import java.time.YearMonth
import java.time.temporal.ChronoUnit

class BeregnSlitertilleggRS(
    val uttakstidspunkt: YearMonth,
    val virkningstidspunkt: YearMonth,
    val person: Person
) : AbstractDemoRuleset<Slitertillegg>() {

    private val grunnbeløp by lazy { grunnbeløpByYearMonth(virkningstidspunkt) }
    private val antallMånederEtterNedreAldersgrense = Faktum(
        "antallMånederEtterNedreAldersgrense",
        ChronoUnit.MONTHS.between(person.nedrePensjonsDato(), uttakstidspunkt)
    )

    private var fulltSlitertillegg: Double = 0.0
    private var justertSlitertillegg: Double = 0.0
    private var avkortetSlitertilleggEtterTrygdetid: Double = 0.0
    private var slitertilleggBeregnet: Double = 0.0

    @OptIn(DslDomainPredicate::class)
    override fun create() {
        regel("SLITERTILLEGG-BEREGNING-UAVKORTET") {
            HVIS { true }
            SÅ {
                fulltSlitertillegg = 0.25 * grunnbeløp / ETT_ÅR
            }
        }

        regel("SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT") {
            HVIS { antallMånederEtterNedreAldersgrense erMindreEnn TRE_ÅR }
            SÅ {
                justertSlitertillegg =
                    fulltSlitertillegg * ((TRE_ÅR - antallMånederEtterNedreAldersgrense.value) / TRE_ÅR.toDouble())
            }
            ELLERS {
                justertSlitertillegg = 0.0
            }
        }

        regel("SLITERTILLEGG-AVKORTING-TRYGDETID") {
            HVIS { true }
            SÅ {
                avkortetSlitertilleggEtterTrygdetid =
                    fulltSlitertillegg * (person.trygdetid.faktiskTrygdetid / FULL_TRYGDETID.toDouble())
            }
        }

        regel("SLITERTILEGG-BEREGNET") {
            HVIS { true }
            SÅ {
                slitertilleggBeregnet =
                    fulltSlitertillegg * ((TRE_ÅR - antallMånederEtterNedreAldersgrense.value) / TRE_ÅR.toDouble()) * (person.trygdetid.faktiskTrygdetid / FULL_TRYGDETID.toDouble())
            }
        }

        regel("SLITERTILLEGG-RESULTAT") {
            HVIS { true }
            SÅ {
                RETURNER(
                    Slitertillegg(
                        grunnbeløp = grunnbeløp,
                        antallMånederEtterNedreAldersgrense = antallMånederEtterNedreAldersgrense.value,
                        fulltSlitertillegg = fulltSlitertillegg,
                        justertSlitertillegg = justertSlitertillegg,
                        avkortetSlitertilleggEtterTrygdetid = avkortetSlitertilleggEtterTrygdetid,
                        slitertilleggBeregnet = slitertilleggBeregnet
                    )
                )
            }
        }
    }
}