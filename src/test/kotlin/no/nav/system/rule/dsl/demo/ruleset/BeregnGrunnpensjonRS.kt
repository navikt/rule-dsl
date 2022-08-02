package no.nav.system.rule.dsl.demo.ruleset

import no.nav.system.rule.dsl.AbstractRuleset
import no.nav.system.rule.dsl.demo.domain.Grunnpensjon
import kotlin.math.roundToInt

class BeregnGrunnpensjonRS(
    val grunnbeløp: Int,
    val trygdetid: Int,
    val sats: Double
) : AbstractRuleset<Grunnpensjon>() {
    override fun create() {

        regel("FullTrygdetid") {
            HVIS { trygdetid == 40 }
            SÅ {
                RETURNER(
                    Grunnpensjon(
                        grunnbeløp = grunnbeløp,
                        prosentsats = sats,
                        netto = (grunnbeløp * sats).roundToInt()
                    )
                )
            }
        }

        regel("RedusertTrygdetid") {
            HVIS { trygdetid < 40 }
            SÅ {
                RETURNER(
                    Grunnpensjon(
                        grunnbeløp = grunnbeløp,
                        prosentsats = sats,
                        netto = (grunnbeløp * sats * trygdetid / 40.0).roundToInt()
                    )
                )
            }
        }
    }

}