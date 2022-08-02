package no.nav.system.rule.dsl.demo.ruleservice

import no.nav.system.rule.dsl.demo.helper.localDate
import no.nav.system.rule.dsl.AbstractResource
import java.time.LocalDate

class GrunnbeløpSatsResource : AbstractResource() {

    val grunnbeløpMap: Map<ClosedRange<LocalDate>, Int> = mapOf(
        localDate(1990, 5, 1)..localDate(2000, 4, 30) to 100000,
        localDate(2000, 5, 1)..localDate(2010, 4, 30) to 110000,
        localDate(2010, 5, 1)..localDate(2020, 4, 30) to 120000,
        localDate(2020, 5, 1)..localDate(2030, 4, 30) to 130000,
    )

}