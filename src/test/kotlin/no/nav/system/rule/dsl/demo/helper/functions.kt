package no.nav.system.rule.dsl.demo.helper

import java.time.LocalDate
import java.time.Month
import java.time.Period
import java.util.*

fun localDate(year: Int, month: Int, day: Int): LocalDate {
    return LocalDate.of(year, month, day)
}


inline val Int.måneder: Period
    get() = Period.ofMonths(this)

inline val Int.år: Period
    get() = Period.ofYears(this)


inline val LocalDate.år: Int
    get() = this.year

inline val LocalDate.måned: Month
    get() = this.month