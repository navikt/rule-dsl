package no.nav.system.rule.dsl.demo.helper

import java.time.LocalDate
import java.time.Period

fun localDate(year: Int, month: Int, day: Int): LocalDate = LocalDate.of(year, month, day)

inline val Int.måneder: Period
    get() = Period.ofMonths(this)

inline val Int.år: Period
    get() = Period.ofYears(this)