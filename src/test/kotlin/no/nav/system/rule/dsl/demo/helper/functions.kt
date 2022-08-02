package no.nav.system.rule.dsl.demo.helper

import java.time.LocalDate

fun localDate(year: Int, month: Int, day: Int): LocalDate {
    return LocalDate.of(year, month, day)
}