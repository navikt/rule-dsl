package no.nav.system.rule.dsl.demo.domain.param

import no.nav.system.rule.dsl.demo.domain.Grunnpensjon
import no.nav.system.rule.dsl.demo.domain.Trygdetid
import no.nav.system.rule.dsl.demo.domain.koder.UtfallType
import no.nav.system.rule.dsl.rettsregel.Faktum

data class AlderspensjonOutput(
    var anvendtTrygdetid: Trygdetid? = null,
    var grunnpensjon: Grunnpensjon? = null,
    var anvendtFlyktning: Faktum<UtfallType>? = null
)