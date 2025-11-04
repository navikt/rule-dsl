package no.nav.pensjon.sliterordning.regelsett

import no.nav.pensjon.sliterordning.fagdata.FagKonstanter.ETT_ÅR
import no.nav.pensjon.sliterordning.fagdata.FagKonstanter.FULL_TRYGDETID
import no.nav.pensjon.sliterordning.fagdata.FagKonstanter.TRE_ÅR
import no.nav.pensjon.sliterordning.grunnlag.Person
import no.nav.system.rule.dsl.DslDomainPredicate
import no.nav.system.rule.dsl.demo.ruleservice.grunnbeløpByYearMonth
import no.nav.system.rule.dsl.demo.ruleset.AbstractDemoRuleset
import no.nav.system.rule.dsl.forklaring.*
import no.nav.system.rule.dsl.rettsregel.Faktum
import no.nav.system.rule.dsl.rettsregel.erFør
import no.nav.system.rule.dsl.rettsregel.forklartfaktum.ForklartFaktum
import java.time.LocalDate
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
class baseRS(
    val someStartDate: Faktum<LocalDate>,
    val someEndDate: Faktum<LocalDate>,
) : AbstractDemoRuleset<ForklartFaktum<Double>>() {

    private lateinit var numOfMonths: Faktum<Int>
    private lateinit var trygdetidFaktor: Grunnlag<Double>

    @OptIn(DslDomainPredicate::class)
    override fun create() {

        regel("Calculate numOfMonths") {
            HVIS { someStartDate erFør someEndDate }
            SÅ {
                /**
                 * The resulting new Faktum now tracks the math operation and its operands
                 * and the rule that created it, including the predicate resulting from the
                 * comparison someStartDate erFør someEndDate
                 */
                //numOfMonths = Faktum("startingValue", 12) + Faktum("additionalMonths", 3)

            }
        }

        regel("Approve application based on number of months") {
            HVIS {true} //{ numOfMonths erStørreEnn Faktum("",12) }
            SÅ {
                /**
                 * - faktum function wires the new faktum using the underlying AbstractRuleComponent structure using origin trace.
                 * - Part of the HVORDAN explanation is now relying on the numOfMonths faktum, which has its own HVORFOR and HVORDAN trace.
                 */
//                result = faktum(
//                    "status",
//                    APPROVED
//                )
            }
        }
    }
}
