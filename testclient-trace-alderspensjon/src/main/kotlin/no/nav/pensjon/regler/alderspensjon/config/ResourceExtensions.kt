package no.nav.pensjon.regler.alderspensjon.config

import no.nav.system.ruledsl.core.resource.ResourceAccessor
import java.time.LocalDate

/**
 * Extension function to lookup grunnbeløp by date.
 * Available in SÅ/RETURNER blocks via ResourceAccessor.
 */
fun ResourceAccessor.grunnbeløpByDate(dato: LocalDate): Int =
    getResource(GrunnbeløpSatsResource::class).grunnbeløpMap.entries
        .find { entry -> dato in entry.key }?.value ?: 0
