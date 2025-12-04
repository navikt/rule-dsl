package no.nav.pensjon.regler.alderspensjon.ruleset

import no.nav.pensjon.regler.alderspensjon.config.AbstractDemoRuleset
import no.nav.pensjon.regler.alderspensjon.domain.Grunnpensjon
import kotlin.math.roundToInt

class BeregnGrunnpensjonRS(
    private val grunnbeløp: Int,
    private val trygdetid: Int,
    private val sats: Double
) : AbstractDemoRuleset<Grunnpensjon>() {
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