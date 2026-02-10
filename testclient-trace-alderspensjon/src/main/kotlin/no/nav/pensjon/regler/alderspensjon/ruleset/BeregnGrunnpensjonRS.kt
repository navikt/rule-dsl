package no.nav.pensjon.regler.alderspensjon.ruleset

import no.nav.pensjon.regler.alderspensjon.domain.Grunnpensjon
import no.nav.system.ruledsl.core.expression.Faktum
import no.nav.system.ruledsl.core.trace.RuleContext
import no.nav.system.ruledsl.core.trace.traced
import kotlin.math.roundToInt

/**
 * Calculates Grunnpensjon based on grunnbeløp, trygdetid and sats.
 */
context(ruleContext: RuleContext)
fun beregnGrunnpensjon(
    grunnbeløp: Int,
    trygdetid: Int,
    sats: Double
): Faktum<Grunnpensjon> = traced {

    regel("FullEllerOverTrygdetid") {
        HVIS { trygdetid >= 40 }
        RETURNER {
            faktum("grunnpensjon", Grunnpensjon(
                grunnbeløp = grunnbeløp,
                prosentsats = sats,
                netto = (grunnbeløp * sats).roundToInt()
            ))
        }
    }

    regel("RedusertTrygdetid") {
        HVIS { trygdetid < 40 }
        RETURNER {
            faktum("grunnpensjon", Grunnpensjon(
                grunnbeløp = grunnbeløp,
                prosentsats = sats,
                netto = (grunnbeløp * sats * trygdetid / 40.0).roundToInt()
            ))
        }
    }
}
