package no.nav.system.rule.dsl.demo.domain

import no.nav.system.rule.dsl.demo.domain.koder.LandEnum
import java.time.LocalDate

data class Boperiode(
    val fom: LocalDate,
    val tom: LocalDate? = null,
    val land: LandEnum
)