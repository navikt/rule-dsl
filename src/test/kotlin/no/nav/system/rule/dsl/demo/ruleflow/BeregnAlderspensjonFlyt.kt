package no.nav.system.rule.dsl.demo.ruleflow

import no.nav.system.rule.dsl.AbstractRuleflow
import no.nav.system.rule.dsl.demo.domain.Person
import no.nav.system.rule.dsl.demo.domain.koder.UtfallType
import no.nav.system.rule.dsl.demo.domain.koder.YtelseEnum
import no.nav.system.rule.dsl.demo.domain.param.AlderspensjonOutput
import no.nav.system.rule.dsl.demo.ruleservice.grunnbeløpByDate
import no.nav.system.rule.dsl.demo.ruleset.BeregnFaktiskTrygdetidRS
import no.nav.system.rule.dsl.demo.ruleset.BeregnGrunnpensjonRS
import no.nav.system.rule.dsl.demo.ruleset.PersonenErFlyktningRS
import no.nav.system.rule.dsl.rettsregel.Faktum
import java.time.LocalDate

class BeregnAlderspensjonFlyt(
    private val person: Person,
    private val virkningstidspunkt: Faktum<LocalDate>,
) : AbstractRuleflow<AlderspensjonOutput>() {
    private var grunnpensjonSats = 0.0
    private lateinit var flyktningUtfall: Faktum<UtfallType>
    private var output = AlderspensjonOutput()

    override var ruleflow: () -> AlderspensjonOutput = {

        /**
         * Sjekk om anvendtFlyktning
         */
        flyktningUtfall = PersonenErFlyktningRS(
            person,
            Faktum("Ytelsestype", YtelseEnum.AP),
            Faktum("Kapittel 20", false),
            virkningstidspunkt,
            Faktum("Søknadstidspunkt fom 2021", true)
        ).run(this).get()

        /**
         * Task: Beregn Trygdetid
         */
        output.anvendtTrygdetid = BeregnFaktiskTrygdetidRS(
            person.fødselsdato,
            virkningstidspunkt,
            person.boperioder,
            flyktningUtfall
        ).run(this).get()

        forgrening("Sivilstand gift?") {
            gren {
                betingelse { person.erGift }
                flyt {
                    /**
                     * Task: Lav Sats
                     */
                    grunnpensjonSats = 0.90
                }
            }
            gren {
                betingelse { !person.erGift }
                flyt {
                    /**
                     * Task: Høy Sats
                     */
                    grunnpensjonSats = 1.00
                }
            }
        }

        /**
         * Task: Beregn Grunnpensjon
         */
        output.grunnpensjon = BeregnGrunnpensjonRS(
            grunnbeløpByDate(virkningstidspunkt.value),
            output.anvendtTrygdetid!!.år,
            grunnpensjonSats
        ).run(this).get()

        output
    }
}