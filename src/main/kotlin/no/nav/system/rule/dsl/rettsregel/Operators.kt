package no.nav.system.rule.dsl.rettsregel

import no.nav.system.rule.dsl.enums.ListComparator.ER_BLANDT
import no.nav.system.rule.dsl.enums.ListComparator.ER_IKKE_BLANDT
import no.nav.system.rule.dsl.enums.PairComparator.*
import java.time.LocalDate

/**
 * Datoer
 */
infix fun Faktum<LocalDate>.erFørEllerLik(other: Faktum<LocalDate>) =
    PairSubsumtion(FØR_ELLER_LIK, this, other) { this.value <= other.value }

infix fun Faktum<LocalDate>.erFør(other: Faktum<LocalDate>) =
    PairSubsumtion(FØR, this, other) { value < other.value }

infix fun Faktum<LocalDate>.erFør(other: LocalDate) =
    PairSubsumtion(FØR, this, Faktum(other)) { value < other }

infix fun Faktum<LocalDate>.erEtterEllerLik(other: Faktum<LocalDate>) =
    PairSubsumtion(ETTER_ELLER_LIK, this, other) { value >= other.value }

infix fun Faktum<LocalDate>.erEtter(other: Faktum<LocalDate>) =
    PairSubsumtion(ETTER, this, other) { value > other.value }

/**
 * Tall
 */
infix fun Faktum<out Number>.erMindreEllerLik(other: Faktum<out Number>) =
    PairSubsumtion(MINDRE_ELLER_LIK, this, other) { value.toDouble() <= other.value.toDouble() }

infix fun Faktum<out Number>.erMindreEnn(other: Faktum<out Number>) =
    PairSubsumtion(MINDRE, this, other) { value.toDouble() < other.value.toDouble() }

infix fun Faktum<out Number>.erStørreEllerLik(other: Faktum<out Number>) =
    PairSubsumtion(STØRRE_ELLER_LIK, this, other) { value.toDouble() >= other.value.toDouble() }

infix fun Faktum<out Number>.erStørre(other: Faktum<out Number>) =
    PairSubsumtion(STØRRE, this, other) { value.toDouble() > other.value.toDouble() }

infix fun Faktum<out Number>.erMindreEllerLik(other: Number) =
    PairSubsumtion(MINDRE_ELLER_LIK, this, Faktum(other)) { value.toDouble() <= other.toDouble() }

infix fun Faktum<out Number>.erMindreEnn(other: Number) =
    PairSubsumtion(MINDRE, this, Faktum(other)) { value.toDouble() < other.toDouble() }

infix fun Faktum<out Number>.erStørreEllerLik(other: Number) =
    PairSubsumtion(STØRRE_ELLER_LIK, this, Faktum(other)) { value.toDouble() >= other.toDouble() }

infix fun Faktum<out Number>.erStørre(other: Number) =
    PairSubsumtion(STØRRE, this, Faktum(other)) { value.toDouble() > other.toDouble() }

/**
 * Dato > Tall
 */
infix fun Faktum<out LocalDate>.erMindreEllerLik(other: Int) = PairSubsumtion(
    MINDRE_ELLER_LIK, this, Faktum(other)
) { value.year <= other }

infix fun Faktum<out LocalDate>.erMindreEnn(other: Int) = PairSubsumtion(
    MINDRE, this, Faktum(other)
) { value.year < other }

infix fun Faktum<out LocalDate>.erStørreEllerLik(other: Int) = PairSubsumtion(
    STØRRE_ELLER_LIK, this, Faktum(other)
) { value.year >= other }

infix fun Faktum<out LocalDate>.erStørreEnn(other: Int) = PairSubsumtion(
    STØRRE, this, Faktum(other)
) { value.year > other }

/**
 * Faktum
 */
infix fun <T : Any> Faktum<T>.erLik(ap: T) = PairSubsumtion(
    LIK, this, Faktum(ap)
) { this.value == ap }

infix fun <T : Any> Faktum<T>.erLik(ap: Faktum<T>) = PairSubsumtion(
    LIK, this, ap
) { this.value == ap.value }

infix fun <T : Any> Faktum<T>.erUlik(ap: T) = PairSubsumtion(
    ULIK, this, Faktum(ap)
) { this.value != ap }

infix fun <T : Any> Faktum<T>.erUlik(ap: Faktum<T>) = PairSubsumtion(
    ULIK, this, ap
) { this.value != ap.value }

infix fun <T : Any> Faktum<T>.erBlant(others: List<T>) = ListSubsumtion(
    ER_BLANDT,
    Faktum(navn = this.name, verdi = this.value.toString()),
    others.map { Faktum(it) }
) { this.value in others }

infix fun <T : Any> Faktum<T>.erIkkeBlant(others: List<T>) = ListSubsumtion(
    ER_IKKE_BLANDT,
    Faktum(navn = this.name, verdi = this.value.toString()),
    others.map { Faktum(it) }
) { this.value !in others }