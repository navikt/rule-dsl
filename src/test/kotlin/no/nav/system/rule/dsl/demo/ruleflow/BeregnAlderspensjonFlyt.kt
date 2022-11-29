package no.nav.system.rule.dsl.demo.ruleflow

import no.nav.system.rule.dsl.AbstractRuleflow
import no.nav.system.rule.dsl.demo.domain.koder.UtfallType
import no.nav.system.rule.dsl.demo.domain.koder.YtelseEnum
import no.nav.system.rule.dsl.demo.domain.param.AlderspensjonParameter
import no.nav.system.rule.dsl.demo.ruleservice.grunnbeløpByDate
import no.nav.system.rule.dsl.demo.ruleset.BeregnFaktiskTrygdetidRS
import no.nav.system.rule.dsl.demo.ruleset.BeregnGrunnpensjonRS
import no.nav.system.rule.dsl.demo.ruleset.PersonenErFlyktningRS
import no.nav.system.rule.dsl.rettsregel.Faktum

class BeregnAlderspensjonFlyt(
    private val parameter: AlderspensjonParameter,
) : AbstractRuleflow() {
    private var grunnpensjonSats = 0.0
    private lateinit var flyktningUtfall: Faktum<UtfallType>

    override var ruleflow: () -> Unit = {

        /**
         * Sjekk om anvendtFlyktning
         */
        flyktningUtfall = PersonenErFlyktningRS(
            parameter.input.person,
            Faktum("Ytelsestype", YtelseEnum.AP),
            Faktum("Kapittel 20", false),
            parameter.input.virkningstidspunkt,
            Faktum("Søknadstidspunkt fom 2021", true)
        ).run(this).get()

        /**
         * Task: Beregn Trygdetid
         */
        parameter.output.anvendtTrygdetid = BeregnFaktiskTrygdetidRS(
            parameter.input.person.fødselsdato,
            parameter.input.virkningstidspunkt,
            parameter.input.person.boperioder,
            flyktningUtfall
        ).run(this).get()

        forgrening("Sivilstand gift?") {
            gren {
                betingelse { parameter.input.person.erGift }
                flyt {
                    /**
                     * Task: Lav Sats
                     */
                    grunnpensjonSats = 0.90
                }
            }
            gren {
                betingelse { !parameter.input.person.erGift }
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
        parameter.output.grunnpensjon = BeregnGrunnpensjonRS(
            grunnbeløpByDate(parameter.input.virkningstidspunkt.value),
            parameter.output.anvendtTrygdetid!!.år,
            grunnpensjonSats
        ).run(this).get()
    }
}