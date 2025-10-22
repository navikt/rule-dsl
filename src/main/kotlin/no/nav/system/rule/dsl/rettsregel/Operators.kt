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
infix fun Verdi<out Number>.erMindreEllerLik(other: Verdi<out Number>) =
    PairSubsumtion(MINDRE_ELLER_LIK, this, other) { value.toDouble() <= other.value.toDouble() }

infix fun Verdi<out Number>.erMindreEnn(other: Verdi<out Number>) =
    PairSubsumtion(MINDRE, this, other) { value.toDouble() < other.value.toDouble() }

infix fun Verdi<out Number>.erStørreEllerLik(other: Verdi<out Number>) =
    PairSubsumtion(STØRRE_ELLER_LIK, this, other) { value.toDouble() >= other.value.toDouble() }

infix fun Verdi<out Number>.erStørre(other: Verdi<out Number>) =
    PairSubsumtion(STØRRE, this, other) { value.toDouble() > other.value.toDouble() }

infix fun Verdi<out Number>.erMindreEllerLik(other: Number) =
    PairSubsumtion(MINDRE_ELLER_LIK, this, Faktum(other)) { value.toDouble() <= other.toDouble() }

infix fun Verdi<out Number>.erMindreEnn(other: Number) =
    PairSubsumtion(MINDRE, this, Faktum(other)) { value.toDouble() < other.toDouble() }

infix fun Verdi<out Number>.erStørreEllerLik(other: Number) =
    PairSubsumtion(STØRRE_ELLER_LIK, this, Faktum(other)) { value.toDouble() >= other.toDouble() }

infix fun Verdi<out Number>.erStørre(other: Number) =
    PairSubsumtion(STØRRE, this, Faktum(other)) { value.toDouble() > other.toDouble() }

/**
 * Dato > Tall
 */
// TODO: Disse ser upresise ut. Vurder behov og evt fjern. Dersom de skal beholdes,
// TODO: burde de trolig hete erÅrTidligereEllerLik, erÅrTidligereEnn, senere osv.
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
infix fun <T : Any> Verdi<T>.erLik(ap: T) = PairSubsumtion(
    LIK, this, Faktum(ap)
) { this.value == ap }

infix fun <T : Any> Verdi<T>.erLik(ap: Verdi<T>) = PairSubsumtion(
    LIK, this, ap
) { this.value == ap.value }

infix fun <T : Any> Verdi<T>.erUlik(ap: T) = PairSubsumtion(
    ULIK, this, Faktum(ap)
) { this.value != ap }

infix fun <T : Any> Verdi<T>.erUlik(ap: Verdi<T>) = PairSubsumtion(
    ULIK, this, ap
) { this.value != ap.value }

infix fun <T : Any> Verdi<T>.erBlant(others: List<T>) = ListSubsumtion(
    ER_BLANDT,
    Faktum(name = this.name, value = this.value.toString()),
    others.map { Faktum(it) }
) { this.value in others }

infix fun <T : Any> Verdi<T>.erIkkeBlant(others: List<T>) = ListSubsumtion(
    ER_IKKE_BLANDT,
    Faktum(name = this.name, value = this.value.toString()),
    others.map { Faktum(it) }
) { this.value !in others }