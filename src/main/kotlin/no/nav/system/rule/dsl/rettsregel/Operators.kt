package no.nav.system.rule.dsl.rettsregel

import no.nav.system.rule.dsl.enums.ListComparator.ER_BLANDT
import no.nav.system.rule.dsl.enums.ListComparator.ER_IKKE_BLANDT
import no.nav.system.rule.dsl.enums.PairComparator.*
import java.time.LocalDate

/**
 * Datoer
 */
infix fun Fact<LocalDate>.erFørEllerLik(other: Fact<LocalDate>) =
    PairSubsumtion(FØR_ELLER_LIK, this, other) { this.value <= other.value }

infix fun Fact<LocalDate>.erFør(other: Fact<LocalDate>) =
    PairSubsumtion(FØR, this, other) { value < other.value }

infix fun Fact<LocalDate>.erFør(other: LocalDate) =
    PairSubsumtion(FØR, this, Fact(other)) { value < other }

infix fun Fact<LocalDate>.erEtterEllerLik(other: Fact<LocalDate>) =
    PairSubsumtion(ETTER_ELLER_LIK, this, other) { value >= other.value }

infix fun Fact<LocalDate>.erEtter(other: Fact<LocalDate>) =
    PairSubsumtion(ETTER, this, other) { value > other.value }

/**
 * Tall
 */
infix fun Fact<out Number>.erMindreEllerLik(other: Fact<out Number>) =
    PairSubsumtion(MINDRE_ELLER_LIK, this, other) { value.toDouble() <= other.value.toDouble() }

infix fun Fact<out Number>.erMindreEnn(other: Fact<out Number>) =
    PairSubsumtion(MINDRE, this, other) { value.toDouble() < other.value.toDouble() }

infix fun Fact<out Number>.erStørreEllerLik(other: Fact<out Number>) =
    PairSubsumtion(STØRRE_ELLER_LIK, this, other) { value.toDouble() >= other.value.toDouble() }

infix fun Fact<out Number>.erStørre(other: Fact<out Number>) =
    PairSubsumtion(STØRRE, this, other) { value.toDouble() > other.value.toDouble() }

infix fun Fact<out Number>.erMindreEllerLik(other: Number) =
    PairSubsumtion(MINDRE_ELLER_LIK, this, Fact(other)) { value.toDouble() <= other.toDouble() }

infix fun Fact<out Number>.erMindreEnn(other: Number) =
    PairSubsumtion(MINDRE, this, Fact(other)) { value.toDouble() < other.toDouble() }

infix fun Fact<out Number>.erStørreEllerLik(other: Number) =
    PairSubsumtion(STØRRE_ELLER_LIK, this, Fact(other)) { value.toDouble() >= other.toDouble() }

infix fun Fact<out Number>.erStørre(other: Number) =
    PairSubsumtion(STØRRE, this, Fact(other)) { value.toDouble() > other.toDouble() }

/**
 * Dato > Tall
 */
infix fun Fact<out LocalDate>.erMindreEllerLik(other: Int) = PairSubsumtion(
    MINDRE_ELLER_LIK, this, Fact(other)
) { value.year <= other }

infix fun Fact<out LocalDate>.erMindreEnn(other: Int) = PairSubsumtion(
    MINDRE, this, Fact(other)
) { value.year < other }

infix fun Fact<out LocalDate>.erStørreEllerLik(other: Int) = PairSubsumtion(
    STØRRE_ELLER_LIK, this, Fact(other)
) { value.year >= other }

infix fun Fact<out LocalDate>.erStørreEnn(other: Int) = PairSubsumtion(
    STØRRE, this, Fact(other)
) { value.year > other }

/**
 * Faktum
 */
infix fun <T : Any> Fact<T>.erLik(ap: T) = PairSubsumtion(
    LIK, this, Fact(ap)
) { this.value == ap }

infix fun <T : Any> Fact<T>.erLik(ap: Fact<T>) = PairSubsumtion(
    LIK, this, ap
) { this.value == ap.value }

infix fun <T : Any> Fact<T>.erUlik(ap: T) = PairSubsumtion(
    ULIK, this, Fact(ap)
) { this.value != ap }

infix fun <T : Any> Fact<T>.erUlik(ap: Fact<T>) = PairSubsumtion(
    ULIK, this, ap
) { this.value != ap.value }

infix fun <T : Any> Fact<T>.erBlant(others: List<T>) = ListSubsumtion(
    ER_BLANDT,
    Fact(navn = this.name, verdi = this.value.toString()),
    others.map { Fact(it) }
) { this.value in others }

infix fun <T : Any> Fact<T>.erIkkeBlant(others: List<T>) = ListSubsumtion(
    ER_IKKE_BLANDT,
    Fact(navn = this.name, verdi = this.value.toString()),
    others.map { Fact(it) }
) { this.value !in others }