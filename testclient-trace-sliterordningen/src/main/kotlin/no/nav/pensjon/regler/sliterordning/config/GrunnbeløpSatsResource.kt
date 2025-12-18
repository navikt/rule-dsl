package no.nav.pensjon.regler.sliterordning.config

import no.nav.system.ruledsl.core.resource.ResourceAccessor
import java.time.LocalDate
import java.time.YearMonth

/**
 * Resource for grunnbeløp lookup by date.
 */
class GrunnbeløpSatsResource {
    val grunnbeløpMap: Map<ClosedRange<LocalDate>, Int> = mapOf(
        LocalDate.of(2000, 5, 1)..LocalDate.of(2010, 4, 30) to 110000,
        LocalDate.of(2010, 5, 1)..LocalDate.of(2020, 4, 30) to 120000,
        LocalDate.of(2020, 5, 1)..LocalDate.of(2030, 4, 30) to 130000,
    )
}

/**
 * Extension function to lookup grunnbeløp by YearMonth.
 * Available in any context that implements ResourceAccessor.
 */
fun ResourceAccessor.grunnbeløpByYearMonth(dato: YearMonth): Int =
    getResource(GrunnbeløpSatsResource::class).grunnbeløpMap.entries
        .find { entry -> dato.atDay(1) in entry.key }?.value ?: 0
