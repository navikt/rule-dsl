package no.nav.system.rule.dsl.rettsregel

import no.nav.system.rule.dsl.enums.ListComparator.ER_BLANDT
import no.nav.system.rule.dsl.enums.ListComparator.ER_IKKE_BLANDT
import no.nav.system.rule.dsl.enums.PairComparator.*
import java.time.LocalDate
import java.time.YearMonth

/**
 * Datoer (localDate)
 */
@JvmName("faktum_localdate_erFørEllerLik_faktum_localdate")
infix fun Faktum<LocalDate>.erFørEllerLik(other: Faktum<LocalDate>) =
    PairDomainPredicate(FØR_ELLER_LIK, this, other) { this.value <= other.value }

@JvmName("faktum_localdate_erFør_faktum_localdate")
infix fun Faktum<LocalDate>.erFør(other: Faktum<LocalDate>) =
    PairDomainPredicate(FØR, this, other) { value < other.value }

@JvmName("faktum_localdate_erFør_localdate")
infix fun Faktum<LocalDate>.erFør(other: LocalDate) =
    PairDomainPredicate(FØR, this, Faktum(other)) { value < other }

@JvmName("faktum_localdate_erEtterEllerLik_faktum_localdate")
infix fun Faktum<LocalDate>.erEtterEllerLik(other: Faktum<LocalDate>) =
    PairDomainPredicate(ETTER_ELLER_LIK, this, other) { value >= other.value }

@JvmName("faktum_localdate_erEtter_faktum_localdate")
infix fun Faktum<LocalDate>.erEtter(other: Faktum<LocalDate>) =
    PairDomainPredicate(ETTER, this, other) { value > other.value }

// yearMonth -> localDate
@JvmName("faktum_yearmonth_erFørEllerLik_faktum_localdate")
infix fun Faktum<YearMonth>.erFørEllerLik(other: Faktum<LocalDate>) =
    PairDomainPredicate(FØR_ELLER_LIK, this, other) { this.value.atDay(1) <= other.value }

@JvmName("faktum_yearmonth_erFør_faktum_localdate")
infix fun Faktum<YearMonth>.erFør(other: Faktum<LocalDate>) =
    PairDomainPredicate(FØR, this, other) { value.atDay(1) < other.value }

@JvmName("faktum_yearmonth_erFør_localdate")
infix fun Faktum<YearMonth>.erFør(other: LocalDate) =
    PairDomainPredicate(FØR, this, Faktum(other)) { value.atDay(1) < other }

@JvmName("faktum_yearmonth_erEtterEllerLik_faktum_localdate")
infix fun Faktum<YearMonth>.erEtterEllerLik(other: Faktum<LocalDate>) =
    PairDomainPredicate(ETTER_ELLER_LIK, this, other) { value.atDay(1) >= other.value }

@JvmName("faktum_yearmonth_erEtter_faktum_localdate")
infix fun Faktum<YearMonth>.erEtter(other: Faktum<LocalDate>) =
    PairDomainPredicate(ETTER, this, other) { value.atDay(1) > other.value }

// localDate -> yearMonth

@JvmName("faktum_localdate_erFørEllerLik_faktum_yearmonth")
infix fun Faktum<LocalDate>.erFørEllerLik(other: Faktum<YearMonth>) =
    PairDomainPredicate(FØR_ELLER_LIK, this, other) { this.value <= other.value.atDay(1) }

@JvmName("faktum_localdate_erFør_faktum_yearmonth")
infix fun Faktum<LocalDate>.erFør(other: Faktum<YearMonth>) =
    PairDomainPredicate(FØR, this, other) { value < other.value.atDay(1) }

@JvmName("faktum_localdate_erFør_yearmonth")
infix fun Faktum<LocalDate>.erFør(other: YearMonth) =
    PairDomainPredicate(FØR, this, Faktum(other)) { value < other.atDay(1) }

@JvmName("faktum_localdate_erEtterEllerLik_faktum_yearmonth")
infix fun Faktum<LocalDate>.erEtterEllerLik(other: Faktum<YearMonth>) =
    PairDomainPredicate(ETTER_ELLER_LIK, this, other) { value >= other.value.atDay(1) }

@JvmName("faktum_localdate_erEtter_faktum_yearmonth")
infix fun Faktum<LocalDate>.erEtter(other: Faktum<YearMonth>) =
    PairDomainPredicate(ETTER, this, other) { value > other.value.atDay(1) }

// YearMonth -> YearMonth
@JvmName("faktum_yearmonth_erFørEllerLik_faktum_yearmonth")
infix fun Faktum<YearMonth>.erFørEllerLik(other: Faktum<YearMonth>) =
    PairDomainPredicate(FØR_ELLER_LIK, this, other) { this.value <= other.value }
@JvmName("faktum_yearmonth_erFør_faktum_yearmonth")
infix fun Faktum<YearMonth>.erFør(other: Faktum<YearMonth>) =
    PairDomainPredicate(FØR, this, other) { value < other.value }
@JvmName("faktum_yearmonth_erFør_yearmonth")
infix fun Faktum<YearMonth>.erFør(other: YearMonth) =
    PairDomainPredicate(FØR, this, Faktum(other)) { value < other }
@JvmName("faktum_yearmonth_erEtterEllerLik_faktum_yearmonth")
infix fun Faktum<YearMonth>.erEtterEllerLik(other: Faktum<YearMonth>) =
    PairDomainPredicate(ETTER_ELLER_LIK, this, other) { value >= other.value }
@JvmName("faktum_yearmonth_erEtter_faktum_yearmonth")
infix fun Faktum<YearMonth>.erEtter(other: Faktum<YearMonth>) =
    PairDomainPredicate(ETTER, this, other) { value > other.value }

/**
 * Tall
 */
infix fun Verdi<out Number>.erMindreEllerLik(other: Verdi<out Number>) =
    PairDomainPredicate(MINDRE_ELLER_LIK, this, other) { value.toDouble() <= other.value.toDouble() }

infix fun Verdi<out Number>.erMindreEnn(other: Verdi<out Number>) =
    PairDomainPredicate(MINDRE, this, other) { value.toDouble() < other.value.toDouble() }

infix fun Verdi<out Number>.erStørreEllerLik(other: Verdi<out Number>) =
    PairDomainPredicate(STØRRE_ELLER_LIK, this, other) { value.toDouble() >= other.value.toDouble() }

infix fun Verdi<out Number>.erStørre(other: Verdi<out Number>) =
    PairDomainPredicate(STØRRE, this, other) { value.toDouble() > other.value.toDouble() }

infix fun Verdi<out Number>.erMindreEllerLik(other: Number) =
    PairDomainPredicate(MINDRE_ELLER_LIK, this, Faktum(other)) { value.toDouble() <= other.toDouble() }

infix fun Verdi<out Number>.erMindreEnn(other: Number) =
    PairDomainPredicate(MINDRE, this, Faktum(other)) { value.toDouble() < other.toDouble() }

infix fun Verdi<out Number>.erStørreEllerLik(other: Number) =
    PairDomainPredicate(STØRRE_ELLER_LIK, this, Faktum(other)) { value.toDouble() >= other.toDouble() }

infix fun Verdi<out Number>.erStørre(other: Number) =
    PairDomainPredicate(STØRRE, this, Faktum(other)) { value.toDouble() > other.toDouble() }

/**
 * Dato > Tall
 */
// TODO: Disse ser upresise ut. Vurder behov og evt fjern. Dersom de skal beholdes,
// TODO: burde de trolig hete erÅrTidligereEllerLik, erÅrTidligereEnn, senere osv.
infix fun Faktum<out LocalDate>.erMindreEllerLik(other: Int) = PairDomainPredicate(
    MINDRE_ELLER_LIK, this, Faktum(other)
) { value.year <= other }

infix fun Faktum<out LocalDate>.erMindreEnn(other: Int) = PairDomainPredicate(
    MINDRE, this, Faktum(other)
) { value.year < other }

infix fun Faktum<out LocalDate>.erStørreEllerLik(other: Int) = PairDomainPredicate(
    STØRRE_ELLER_LIK, this, Faktum(other)
) { value.year >= other }

infix fun Faktum<out LocalDate>.erStørreEnn(other: Int) = PairDomainPredicate(
    STØRRE, this, Faktum(other)
) { value.year > other }

/**
 * Faktum
 */
infix fun <T : Any> Verdi<T>.erLik(ap: T) = PairDomainPredicate(
    LIK, this, Faktum(ap)
) { this.value == ap }

infix fun <T : Any> Verdi<T>.erLik(ap: Verdi<T>) = PairDomainPredicate(
    LIK, this, ap
) { this.value == ap.value }

infix fun <T : Any> Verdi<T>.erUlik(ap: T) = PairDomainPredicate(
    ULIK, this, Faktum(ap)
) { this.value != ap }

infix fun <T : Any> Verdi<T>.erUlik(ap: Verdi<T>) = PairDomainPredicate(
    ULIK, this, ap
) { this.value != ap.value }

infix fun <T : Any> Verdi<T>.erBlant(others: List<T>) = ListDomainPredicate(
    ER_BLANDT,
    Faktum(name = this.name, value = this.value.toString()),
    others.map { Faktum(it) }
) { this.value in others }

infix fun <T : Any> Verdi<T>.erIkkeBlant(others: List<T>) = ListDomainPredicate(
    ER_IKKE_BLANDT,
    Faktum(name = this.name, value = this.value.toString()),
    others.map { Faktum(it) }
) { this.value !in others }