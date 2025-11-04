package no.nav.system.rule.dsl.rettsregel.operators

import no.nav.system.rule.dsl.enums.ListComparator
import no.nav.system.rule.dsl.enums.ListComparator.ER_BLANDT
import no.nav.system.rule.dsl.enums.ListComparator.ER_IKKE_BLANDT
import no.nav.system.rule.dsl.enums.PairComparator.*
import no.nav.system.rule.dsl.rettsregel.Const
import no.nav.system.rule.dsl.rettsregel.Faktum
import no.nav.system.rule.dsl.rettsregel.ListDomainPredicate
import no.nav.system.rule.dsl.rettsregel.PairDomainPredicate
import no.nav.system.rule.dsl.rettsregel.Uttrykk
import java.time.LocalDate
import java.time.YearMonth

/**
 * Datoer (localDate)
 */
@JvmName("faktum_localdate_erFørEllerLik_faktum_localdate")
infix fun Faktum<LocalDate>.erFørEllerLik(other: Faktum<LocalDate>) =
    PairDomainPredicate(FØR_ELLER_LIK, this, other) { this.evaluer() <= other.evaluer() }

@JvmName("faktum_localdate_erFørEllerLik_localdate")
infix fun Faktum<LocalDate>.erFørEllerLik(other: LocalDate) =
    PairDomainPredicate(FØR_ELLER_LIK, this, Const(other)) { this.evaluer() <= other }

@JvmName("faktum_localdate_erFør_faktum_localdate")
infix fun Faktum<LocalDate>.erFør(other: Faktum<LocalDate>) =
    PairDomainPredicate(FØR, this, other) { evaluer() < other.evaluer() }

@JvmName("faktum_localdate_erFør_localdate")
infix fun Faktum<LocalDate>.erFør(other: LocalDate) =
    PairDomainPredicate(FØR, this, Const(other)) { evaluer() < other }

@JvmName("faktum_localdate_erEtterEllerLik_faktum_localdate")
infix fun Faktum<LocalDate>.erEtterEllerLik(other: Faktum<LocalDate>) =
    PairDomainPredicate(ETTER_ELLER_LIK, this, other) { evaluer() >= other.evaluer() }

@JvmName("faktum_localdate_erEtterEllerLik_localdate")
infix fun Faktum<LocalDate>.erEtterEllerLik(other: LocalDate) =
    PairDomainPredicate(ETTER_ELLER_LIK, this, Const(other)) { evaluer() >= other }

@JvmName("faktum_localdate_erEtter_localdate")
infix fun Faktum<LocalDate>.erEtter(other: LocalDate) =
    PairDomainPredicate(ETTER, this, Const(other)) { evaluer() > other }

@JvmName("faktum_localdate_erEtter_faktum_localdate")
infix fun Faktum<LocalDate>.erEtter(other: Faktum<LocalDate>) =
    PairDomainPredicate(ETTER, this, other) { evaluer() > other.evaluer() }

// yearMonth -> localDate
@JvmName("faktum_yearmonth_erFørEllerLik_faktum_localdate")
infix fun Faktum<YearMonth>.erFørEllerLik(other: Faktum<LocalDate>) =
    PairDomainPredicate(FØR_ELLER_LIK, this, other) { this.evaluer().atDay(1) <= other.evaluer() }

@JvmName("faktum_yearmonth_erFør_faktum_localdate")
infix fun Faktum<YearMonth>.erFør(other: Faktum<LocalDate>) =
    PairDomainPredicate(FØR, this, other) { evaluer().atDay(1) < other.evaluer() }

@JvmName("faktum_yearmonth_erFør_localdate")
infix fun Faktum<YearMonth>.erFør(other: LocalDate) =
    PairDomainPredicate(FØR, this, Const(other)) { evaluer().atDay(1) < other }

@JvmName("faktum_yearmonth_erEtterEllerLik_faktum_localdate")
infix fun Faktum<YearMonth>.erEtterEllerLik(other: Faktum<LocalDate>) =
    PairDomainPredicate(ETTER_ELLER_LIK, this, other) { evaluer().atDay(1) >= other.evaluer() }

@JvmName("faktum_yearmonth_erEtter_faktum_localdate")
infix fun Faktum<YearMonth>.erEtter(other: Faktum<LocalDate>) =
    PairDomainPredicate(ETTER, this, other) { evaluer().atDay(1) > other.evaluer() }

// localDate -> yearMonth

@JvmName("faktum_localdate_erFørEllerLik_faktum_yearmonth")
infix fun Faktum<LocalDate>.erFørEllerLik(other: Faktum<YearMonth>) =
    PairDomainPredicate(FØR_ELLER_LIK, this, other) { this.evaluer() <= other.evaluer().atDay(1) }

@JvmName("faktum_localdate_erFør_faktum_yearmonth")
infix fun Faktum<LocalDate>.erFør(other: Faktum<YearMonth>) =
    PairDomainPredicate(FØR, this, other) { evaluer() < other.evaluer().atDay(1) }

@JvmName("faktum_localdate_erFør_yearmonth")
infix fun Faktum<LocalDate>.erFør(other: YearMonth) =
    PairDomainPredicate(FØR, this, Const(other)) { evaluer() < other.atDay(1) }

@JvmName("faktum_localdate_erEtterEllerLik_faktum_yearmonth")
infix fun Faktum<LocalDate>.erEtterEllerLik(other: Faktum<YearMonth>) =
    PairDomainPredicate(ETTER_ELLER_LIK, this, other) { evaluer() >= other.evaluer().atDay(1) }

@JvmName("faktum_localdate_erEtter_faktum_yearmonth")
infix fun Faktum<LocalDate>.erEtter(other: Faktum<YearMonth>) =
    PairDomainPredicate(ETTER, this, other) { evaluer() > other.evaluer().atDay(1) }

// YearMonth -> YearMonth
@JvmName("faktum_yearmonth_erFørEllerLik_faktum_yearmonth")
infix fun Faktum<YearMonth>.erFørEllerLik(other: Faktum<YearMonth>) =
    PairDomainPredicate(FØR_ELLER_LIK, this, other) { this.evaluer() <= other.evaluer() }
@JvmName("faktum_yearmonth_erFør_faktum_yearmonth")
infix fun Faktum<YearMonth>.erFør(other: Faktum<YearMonth>) =
    PairDomainPredicate(FØR, this, other) { evaluer() < other.evaluer() }
@JvmName("faktum_yearmonth_erFør_yearmonth")
infix fun Faktum<YearMonth>.erFør(other: YearMonth) =
    PairDomainPredicate(FØR, this, Const(other)) { evaluer() < other }
@JvmName("faktum_yearmonth_erEtterEllerLik_faktum_yearmonth")
infix fun Faktum<YearMonth>.erEtterEllerLik(other: Faktum<YearMonth>) =
    PairDomainPredicate(ETTER_ELLER_LIK, this, other) { evaluer() >= other.evaluer() }
@JvmName("faktum_yearmonth_erEtter_faktum_yearmonth")
infix fun Faktum<YearMonth>.erEtter(other: Faktum<YearMonth>) =
    PairDomainPredicate(ETTER, this, other) { evaluer() > other.evaluer() }

/**
 * Tall
 */
infix fun Faktum<out Number>.erMindreEllerLik(other: Faktum<out Number>) =
    PairDomainPredicate(MINDRE_ELLER_LIK, this, other) { evaluer().toDouble() <= other.evaluer().toDouble() }

infix fun Faktum<out Number>.erMindreEnn(other: Faktum<out Number>) =
    PairDomainPredicate(MINDRE, this, other) { evaluer().toDouble() < other.evaluer().toDouble() }

infix fun Faktum<out Number>.erStørreEllerLik(other: Faktum<out Number>) =
    PairDomainPredicate(STØRRE_ELLER_LIK, this, other) { evaluer().toDouble() >= other.evaluer().toDouble() }

infix fun Faktum<out Number>.erStørreEnn(other: Faktum<out Number>) =
    PairDomainPredicate(STØRRE, this, other) { evaluer().toDouble() > other.evaluer().toDouble() }

infix fun Faktum<out Number>.erMindreEllerLik(other: Number) =
    PairDomainPredicate(MINDRE_ELLER_LIK, this, Const(other)) { evaluer().toDouble() <= other.toDouble() }

infix fun Faktum<out Number>.erMindreEnn(other: Number) =
    PairDomainPredicate(MINDRE, this, Const(other)) { evaluer().toDouble() < other.toDouble() }

infix fun Faktum<out Number>.erStørreEllerLik(other: Number) =
    PairDomainPredicate(STØRRE_ELLER_LIK, this, Const(other)) { evaluer().toDouble() >= other.toDouble() }

infix fun Faktum<out Number>.erStørreEnn(other: Number) =
    PairDomainPredicate(STØRRE, this, Const(other)) { evaluer().toDouble() > other.toDouble() }

/**
 * Dato > Tall
 */
// TODO: Disse ser upresise ut. Vurder behov og evt fjern. Dersom de skal beholdes,
// TODO: burde de trolig hete erÅrTidligereEllerLik, erÅrTidligereEnn, senere osv.
infix fun Faktum<out LocalDate>.erMindreEllerLik(other: Int) = PairDomainPredicate(
    MINDRE_ELLER_LIK, this, Const(other)
) { evaluer().year <= other }

infix fun Faktum<out LocalDate>.erMindreEnn(other: Int) = PairDomainPredicate(
    MINDRE, this, Const(other)
) { evaluer().year < other }

infix fun Faktum<out LocalDate>.erStørreEllerLik(other: Int) = PairDomainPredicate(
    STØRRE_ELLER_LIK, this, Const(other)
) { evaluer().year >= other }

infix fun Faktum<out LocalDate>.erStørreEnn(other: Int) = PairDomainPredicate(
    STØRRE, this, Const(other)
) { evaluer().year > other }

/**
 * Faktum
 */
infix fun <T : Any> Faktum<T>.erLik(ap: T) = PairDomainPredicate(
    LIK, this, Const(ap)
) { this.evaluer() == ap }

infix fun <T : Any> Faktum<T>.erLik(ap: Faktum<T>) = PairDomainPredicate(
    LIK, this, ap
) { this.evaluer() == ap.evaluer() }

infix fun <T : Any> Faktum<T>.erUlik(ap: T) = PairDomainPredicate(
    ULIK, this, Const(ap)
) { this.evaluer() != ap }

infix fun <T : Any> Faktum<T>.erUlik(ap: Faktum<T>) = PairDomainPredicate(
    ULIK, this, ap
) { this.evaluer() != ap.evaluer() }

/**
 * Uttrykk
 */

@JvmName("uttrykk_erBlant_list")
infix fun <T : Any> Uttrykk<T>.erBlant(others: List<T>) = ListDomainPredicate(
    comparator = ER_BLANDT,
    uttrykk = this,
    uttrykkList = others.map { Const(it) }
) { this.evaluer() in others }

@JvmName("uttrykk_erBlant_list_faktum")
infix fun <T : Any> Uttrykk<T>.erBlant(others: List<Faktum<T>>) = ListDomainPredicate(
    comparator = ER_BLANDT,
    uttrykk = this,
    uttrykkList = others
) { this.evaluer() in others.map { it.evaluer() } }

@JvmName("uttrykk_erBlant_faktum_list")
infix fun <T : Any> Uttrykk<T>.erBlant(other: Faktum<List<T>>) = ListDomainPredicate(
    comparator = ER_BLANDT,
    uttrykk = this,
    uttrykkList = other.evaluer().map { Const(it) }
) { this.evaluer() in other.evaluer() }

@JvmName("uttrykk_erIkkeBlant_list")
infix fun <T : Any> Uttrykk<T>.erIkkeBlant(others: List<T>) = ListDomainPredicate(
    comparator = ER_IKKE_BLANDT,
    uttrykk = this,
    uttrykkList = others.map { Const(it) }
) { this.evaluer() !in others }

@JvmName("uttrykk_erIkkeBlant_list_faktum")
infix fun <T : Any> Uttrykk<T>.erIkkeBlant(others: List<Faktum<T>>) = ListDomainPredicate(
    comparator = ER_IKKE_BLANDT,
    uttrykk = this,
    uttrykkList = others
) { this.evaluer() !in others.map { it.evaluer() } }

@JvmName("uttrykk_erIkkeBlant_faktum_list")
infix fun <T : Any> Uttrykk<T>.erIkkeBlant(other: Faktum<List<T>>) = ListDomainPredicate(
    comparator = ER_IKKE_BLANDT,
    uttrykk = this,
    uttrykkList = other.evaluer().map { Const(it) }
) { this.evaluer() !in other.evaluer() }