package no.nav.pensjon.regler.alderspensjon.domain.param

import no.nav.pensjon.regler.alderspensjon.domain.Grunnpensjon
import no.nav.pensjon.regler.alderspensjon.domain.Trygdetid
import no.nav.pensjon.regler.alderspensjon.domain.koder.UtfallType
import no.nav.system.ruledsl.core.rettsregel.Faktum

data class AlderspensjonOutput(
    var anvendtTrygdetid: Trygdetid? = null,
    var grunnpensjon: Grunnpensjon? = null,
    var anvendtFlyktning: Faktum<UtfallType>? = null
)