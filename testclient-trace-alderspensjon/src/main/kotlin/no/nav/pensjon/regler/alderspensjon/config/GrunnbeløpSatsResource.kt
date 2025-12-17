package no.nav.pensjon.regler.alderspensjon.config

import java.time.LocalDate

/**
 * Resource providing grunnbeløp lookup by date.
 */
class GrunnbeløpSatsResource {

    val grunnbeløpMap: Map<ClosedRange<LocalDate>, Int> = mapOf(
        LocalDate.of(1990, 5, 1)..LocalDate.of(2000, 4, 30) to 100000,
        LocalDate.of(2000, 5, 1)..LocalDate.of(2010, 4, 30) to 110000,
        LocalDate.of(2010, 5, 1)..LocalDate.of(2020, 4, 30) to 120000,
        LocalDate.of(2020, 5, 1)..LocalDate.of(2030, 4, 30) to 130000,
    )
}
