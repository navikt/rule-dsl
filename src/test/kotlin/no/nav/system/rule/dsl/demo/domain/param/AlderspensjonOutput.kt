package no.nav.system.rule.dsl.demo.domain.param

import no.nav.system.rule.dsl.demo.domain.Grunnpensjon
import no.nav.system.rule.dsl.demo.domain.Trygdetid

data class AlderspensjonOutput(
    var anvendtTrygdetid: Trygdetid? = null,
    var grunnpensjon: Grunnpensjon? = null
)