package no.nav.system.rule.dsl.demo.ruleflow

import no.nav.system.rule.dsl.AbstractRuleflow
import no.nav.system.rule.dsl.demo.domain.param.AlderspensjonParameter
import no.nav.system.rule.dsl.demo.ruleset.BeregnFaktiskTrygdetidRS
import no.nav.system.rule.dsl.demo.ruleset.BeregnGrunnpensjonRS

class BeregnAlderspensjonFlyt(
    private val parameter: AlderspensjonParameter
) : AbstractRuleflow() {

    private var grunnpensjonSats = 0.0

    override var ruleflow: () -> Unit = {

        /**
         * Task: Beregn Trygdetid
         */
        parameter.output.anvendtTrygdetid = BeregnFaktiskTrygdetidRS(
            parameter.input.person.fødselsdato,
            parameter.input.virkningstidspunkt,
            parameter.input.person.boperioder
        ).run(this).get()

        decision("Sivilstand gift?") {
            branch {
                condition { parameter.input.person.erGift }
                flow {
                    /**
                     * Task: Lav Sats
                     */
                    grunnpensjonSats = 0.90
                }
            }
            branch {
                condition { !parameter.input.person.erGift }
                flow {
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
            parameter.input.grunnbeløpVedVirk,
            parameter.output.anvendtTrygdetid!!.år,
            grunnpensjonSats
        ).run(this).get()
    }
}