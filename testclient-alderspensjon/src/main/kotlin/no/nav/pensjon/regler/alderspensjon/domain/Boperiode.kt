package no.nav.pensjon.regler.alderspensjon.domain

import no.nav.pensjon.regler.alderspensjon.domain.koder.LandEnum
import java.time.LocalDate

data class Boperiode(
    val fom: LocalDate,
    val tom: LocalDate? = null,
    val land: LandEnum
)