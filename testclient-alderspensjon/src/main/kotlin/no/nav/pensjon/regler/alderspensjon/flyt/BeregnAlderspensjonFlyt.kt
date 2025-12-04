package no.nav.pensjon.regler.alderspensjon.flyt

import no.nav.pensjon.regler.alderspensjon.config.grunnbeløpByDate
import no.nav.pensjon.regler.alderspensjon.domain.Person
import no.nav.pensjon.regler.alderspensjon.domain.koder.YtelseEnum
import no.nav.pensjon.regler.alderspensjon.domain.param.AlderspensjonOutput
import no.nav.pensjon.regler.alderspensjon.ruleset.BeregnFaktiskTrygdetidRS
import no.nav.pensjon.regler.alderspensjon.ruleset.BeregnGrunnpensjonRS
import no.nav.pensjon.regler.alderspensjon.ruleset.PersonenErFlyktningRS
import no.nav.system.ruledsl.core.model.AbstractRuleflow
import no.nav.system.ruledsl.core.rettsregel.Faktum
import java.time.LocalDate

class BeregnAlderspensjonFlyt(
    private val person: Person,
    private val virkningstidspunkt: Faktum<LocalDate>,
) : AbstractRuleflow<AlderspensjonOutput>() {
    private var grunnpensjonSats = 0.0
    private val output = AlderspensjonOutput()

    override var ruleflow: () -> AlderspensjonOutput = {

        /**
         * Sjekk om anvendtFlyktning
         */
        output.anvendtFlyktning = PersonenErFlyktningRS(
            person,
            Faktum("Ytelsestype", YtelseEnum.AP),
            Faktum("Kapittel 20", false),
            virkningstidspunkt,
            Faktum("Søknadstidspunkt fom 2021", true)
        ).run(this)

        /**
         * Task: Beregn no.nav.pensjon.sliterordning.grunnlag.Trygdetid
         */
        output.anvendtTrygdetid = BeregnFaktiskTrygdetidRS(
            person.fødselsdato,
            virkningstidspunkt,
            person.boperioder,
            output.anvendtFlyktning!!
        ).run(this)

        forgrening("Sivilstand?") {
            gren {
                betingelse("Gift") { person.erGift }
                flyt {
                    /**
                     * Task: Lav Sats
                     */
                    grunnpensjonSats = 0.90
                }
            }
            gren {
                betingelse("Ugift") { !person.erGift }
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
            grunnbeløpByDate(virkningstidspunkt.verdi),
            output.anvendtTrygdetid!!.år,
            grunnpensjonSats
        ).run(this)

        output
    }
}