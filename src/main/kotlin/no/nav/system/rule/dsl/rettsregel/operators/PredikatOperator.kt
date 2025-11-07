package no.nav.system.rule.dsl.rettsregel.operators

import no.nav.system.rule.dsl.enums.ListOperator.ER_BLANDT
import no.nav.system.rule.dsl.enums.ListOperator.ER_IKKE_BLANDT
import no.nav.system.rule.dsl.enums.PairOperator.*
import no.nav.system.rule.dsl.rettsregel.*
import java.time.LocalDate
import java.time.YearMonth

/**
 * Datoer (localDate)
 */
@JvmName("faktum_localdate_erFørEllerLik_faktum_localdate")
infix fun Faktum<LocalDate>.erFørEllerLik(other: Faktum<LocalDate>) =
    ComparisonOperation(this, other, FØR_ELLER_LIK) { this.evaluer() <= other.evaluer() }

@JvmName("faktum_localdate_erFørEllerLik_localdate")
infix fun Faktum<LocalDate>.erFørEllerLik(other: LocalDate) =
    ComparisonOperation(this, Const(other), FØR_ELLER_LIK) { this.evaluer() <= other }

@JvmName("localdate_erFørEllerLik_localdate")
infix fun LocalDate.erFørEllerLik(other: LocalDate) =
    ComparisonOperation(Const(this), Const(other), FØR_ELLER_LIK) { this <= other }

@JvmName("localdate_erFørEllerLik_faktum_localdate")
infix fun LocalDate.erFørEllerLik(other: Faktum<LocalDate>) =
    ComparisonOperation(Const(this), other, FØR_ELLER_LIK) { this <= other.evaluer() }

@JvmName("faktum_localdate_erFør_faktum_localdate")
infix fun Faktum<LocalDate>.erFør(other: Faktum<LocalDate>) =
    ComparisonOperation(this, other, FØR) { evaluer() < other.evaluer() }

@JvmName("faktum_localdate_erFør_localdate")
infix fun Faktum<LocalDate>.erFør(other: LocalDate) =
    ComparisonOperation(this, Const(other), FØR) { evaluer() < other }

@JvmName("localdate_erFør_faktum_localdate")
infix fun LocalDate.erFør(other: Faktum<LocalDate>) =
    ComparisonOperation(Const(this), other, FØR) { this < other.evaluer() }

@JvmName("localdate_erFør_localdate")
infix fun LocalDate.erFør(other: LocalDate) =
    ComparisonOperation(Const(this), Const(other), FØR) { this < other }

@JvmName("faktum_localdate_erEtterEllerLik_faktum_localdate")
infix fun Faktum<LocalDate>.erEtterEllerLik(other: Faktum<LocalDate>) =
    ComparisonOperation(this, other, ETTER_ELLER_LIK) { evaluer() >= other.evaluer() }

@JvmName("faktum_localdate_erEtterEllerLik_localdate")
infix fun Faktum<LocalDate>.erEtterEllerLik(other: LocalDate) =
    ComparisonOperation(this, Const(other), ETTER_ELLER_LIK) { evaluer() >= other }

@JvmName("faktum_localdate_erEtter_localdate")
infix fun Faktum<LocalDate>.erEtter(other: LocalDate) =
    ComparisonOperation(this, Const(other), ETTER) { evaluer() > other }

@JvmName("faktum_localdate_erEtter_faktum_localdate")
infix fun Faktum<LocalDate>.erEtter(other: Faktum<LocalDate>) =
    ComparisonOperation(this, other, ETTER) { evaluer() > other.evaluer() }

// yearMonth -> localDate
@JvmName("faktum_yearmonth_erFørEllerLik_faktum_localdate")
infix fun Faktum<YearMonth>.erFørEllerLik(other: Faktum<LocalDate>) =
    ComparisonOperation(this, other, FØR_ELLER_LIK) { this.evaluer().atDay(1) <= other.evaluer() }

@JvmName("faktum_yearmonth_erFør_faktum_localdate")
infix fun Faktum<YearMonth>.erFør(other: Faktum<LocalDate>) =
    ComparisonOperation(this, other, FØR) { evaluer().atDay(1) < other.evaluer() }

@JvmName("faktum_yearmonth_erFør_localdate")
infix fun Faktum<YearMonth>.erFør(other: LocalDate) =
    ComparisonOperation(this, Const(other), FØR) { evaluer().atDay(1) < other }

@JvmName("faktum_yearmonth_erEtterEllerLik_faktum_localdate")
infix fun Faktum<YearMonth>.erEtterEllerLik(other: Faktum<LocalDate>) =
    ComparisonOperation(this, other, ETTER_ELLER_LIK) { evaluer().atDay(1) >= other.evaluer() }

@JvmName("faktum_yearmonth_erEtter_faktum_localdate")
infix fun Faktum<YearMonth>.erEtter(other: Faktum<LocalDate>) =
    ComparisonOperation(this, other, ETTER) { evaluer().atDay(1) > other.evaluer() }

// localDate -> yearMonth

@JvmName("faktum_localdate_erFørEllerLik_faktum_yearmonth")
infix fun Faktum<LocalDate>.erFørEllerLik(other: Faktum<YearMonth>) =
    ComparisonOperation(this, other, FØR_ELLER_LIK) { this.evaluer() <= other.evaluer().atDay(1) }

@JvmName("faktum_localdate_erFør_faktum_yearmonth")
infix fun Faktum<LocalDate>.erFør(other: Faktum<YearMonth>) =
    ComparisonOperation(this, other, FØR) { evaluer() < other.evaluer().atDay(1) }

@JvmName("faktum_localdate_erFør_yearmonth")
infix fun Faktum<LocalDate>.erFør(other: YearMonth) =
    ComparisonOperation(this, Const(other), FØR) { evaluer() < other.atDay(1) }

@JvmName("faktum_localdate_erEtterEllerLik_faktum_yearmonth")
infix fun Faktum<LocalDate>.erEtterEllerLik(other: Faktum<YearMonth>) =
    ComparisonOperation(this, other, ETTER_ELLER_LIK) { evaluer() >= other.evaluer().atDay(1) }

@JvmName("faktum_localdate_erEtter_faktum_yearmonth")
infix fun Faktum<LocalDate>.erEtter(other: Faktum<YearMonth>) =
    ComparisonOperation(this, other, ETTER) { evaluer() > other.evaluer().atDay(1) }

// YearMonth -> YearMonth
@JvmName("faktum_yearmonth_erFørEllerLik_faktum_yearmonth")
infix fun Faktum<YearMonth>.erFørEllerLik(other: Faktum<YearMonth>) =
    ComparisonOperation(this, other, FØR_ELLER_LIK) { this.evaluer() <= other.evaluer() }

@JvmName("faktum_yearmonth_erFør_faktum_yearmonth")
infix fun Faktum<YearMonth>.erFør(other: Faktum<YearMonth>) =
    ComparisonOperation(this, other, FØR) { evaluer() < other.evaluer() }

@JvmName("faktum_yearmonth_erFør_yearmonth")
infix fun Faktum<YearMonth>.erFør(other: YearMonth) =
    ComparisonOperation(this, Const(other), FØR) { evaluer() < other }

@JvmName("faktum_yearmonth_erEtterEllerLik_faktum_yearmonth")
infix fun Faktum<YearMonth>.erEtterEllerLik(other: Faktum<YearMonth>) =
    ComparisonOperation(this, other, ETTER_ELLER_LIK) { evaluer() >= other.evaluer() }

@JvmName("faktum_yearmonth_erEtter_faktum_yearmonth")
infix fun Faktum<YearMonth>.erEtter(other: Faktum<YearMonth>) =
    ComparisonOperation(this, other, ETTER) { evaluer() > other.evaluer() }

/**
 * Tall
 */
@JvmName("faktum_number_erMindreEllerLik_faktum_number")
infix fun Faktum<out Number>.erMindreEllerLik(other: Faktum<out Number>) =
    ComparisonOperation(this, other, MINDRE_ELLER_LIK) { evaluer().toDouble() <= other.evaluer().toDouble() }

@JvmName("faktum_number_erMindreEllerLik_number")
infix fun Faktum<out Number>.erMindreEllerLik(other: Number) =
    ComparisonOperation(this, Const(other), MINDRE_ELLER_LIK) { evaluer().toDouble() <= other.toDouble() }

@JvmName("number_erMindreEllerLik_faktum_number")
infix fun Number.erMindreEllerLik(other: Faktum<out Number>) =
    ComparisonOperation(Const(this), other, MINDRE_ELLER_LIK) { toDouble() <= other.evaluer().toDouble() }

@JvmName("number_erMindreEllerLik_number")
infix fun Number.erMindreEllerLik(other: Number) =
    ComparisonOperation(Const(this), Const(other), MINDRE_ELLER_LIK) { toDouble() <= other.toDouble() }


@JvmName("faktum_number_erMindreEnn_faktum_number")
infix fun Faktum<out Number>.erMindreEnn(other: Faktum<out Number>) =
    ComparisonOperation(this, other, MINDRE) { evaluer().toDouble() < other.evaluer().toDouble() }

@JvmName("faktum_number_erMindreEnn_number")
infix fun Faktum<out Number>.erMindreEnn(other: Number) =
    ComparisonOperation(this, Const(other), MINDRE) { evaluer().toDouble() < other.toDouble() }

@JvmName("number_erMindreEnn_faktum_number")
infix fun Number.erMindreEnn(other: Faktum<out Number>) =
    ComparisonOperation(Const(this), other, MINDRE) { toDouble() < other.evaluer().toDouble() }

@JvmName("number_erMindreEnn_number")
infix fun Number.erMindreEnn(other: Number) =
    ComparisonOperation(Const(this), Const(other), MINDRE) { toDouble() < other.toDouble() }

@JvmName("faktum_number_erStørreEllerLik_faktum_number")
infix fun Faktum<out Number>.erStørreEllerLik(other: Faktum<out Number>) =
    ComparisonOperation(this, other, STØRRE_ELLER_LIK) { evaluer().toDouble() >= other.evaluer().toDouble() }

@JvmName("faktum_number_erStørreEnn_faktum_number")
infix fun Faktum<out Number>.erStørreEnn(other: Faktum<out Number>) =
    ComparisonOperation(this, other, STØRRE) { evaluer().toDouble() > other.evaluer().toDouble() }

@JvmName("faktum_number_erStørreEllerLik_number")
infix fun Faktum<out Number>.erStørreEllerLik(other: Number) =
    ComparisonOperation(this, Const(other), STØRRE_ELLER_LIK) { evaluer().toDouble() >= other.toDouble() }

@JvmName("faktum_number_erStørreEnn_number")
infix fun Faktum<out Number>.erStørreEnn(other: Number) =
    ComparisonOperation(this, Const(other), STØRRE) { evaluer().toDouble() > other.toDouble() }

@JvmName("number_erStørreEllerLik_faktum_number")
infix fun Number.erStørreEllerLik(other: Faktum<out Number>) =
    ComparisonOperation(Const(this), other, STØRRE_ELLER_LIK) { toDouble() >= other.evaluer().toDouble() }

@JvmName("number_erStørreEnn_faktum_number")
infix fun Number.erStørreEnn(other: Faktum<out Number>) =
    ComparisonOperation(Const(this), other, STØRRE) { toDouble() > other.evaluer().toDouble() }

@JvmName("number_erStørreEllerLik_number")
infix fun Number.erStørreEllerLik(other: Number) =
    ComparisonOperation(Const(this), Const(other), STØRRE_ELLER_LIK) { toDouble() >= other.toDouble() }

@JvmName("number_erStørreEnn_number")
infix fun Number.erStørreEnn(other: Number) =
    ComparisonOperation(Const(this), Const(other), STØRRE) { toDouble() > other.toDouble() }

/**
 * Dato > Tall
 */
// TODO: Disse ser upresise ut. Vurder behov og evt fjern. Dersom de skal beholdes,
// TODO: burde de trolig hete erÅrTidligereEllerLik, erÅrTidligereEnn, senere osv.
infix fun Faktum<out LocalDate>.erMindreEllerLik(other: Int) = ComparisonOperation(
    this, Const(other), MINDRE_ELLER_LIK
) { evaluer().year <= other }

infix fun Faktum<out LocalDate>.erMindreEnn(other: Int) = ComparisonOperation(
    this, Const(other), MINDRE
) { evaluer().year < other }

infix fun Faktum<out LocalDate>.erStørreEllerLik(other: Int) = ComparisonOperation(
    this, Const(other), STØRRE_ELLER_LIK
) { evaluer().year >= other }

infix fun Faktum<out LocalDate>.erStørreEnn(other: Int) = ComparisonOperation(
    this, Const(other), STØRRE
) { evaluer().year > other }

/**
 * Faktum - Generic
 */
@JvmName("faktum_erLik_value")
infix fun <T : Any> Faktum<T>.erLik(ap: T) = ComparisonOperation(
    this, Const(ap), LIK
) { this.evaluer() == ap }

@JvmName("faktum_erLik_faktum")
infix fun <T : Any> Faktum<T>.erLik(ap: Faktum<T>) = ComparisonOperation(
    this, ap, LIK
) { this.evaluer() == ap.evaluer() }

@JvmName("faktum_erUlik_value")
infix fun <T : Any> Faktum<T>.erUlik(ap: T) = ComparisonOperation(
    this, Const(ap), ULIK
) { this.evaluer() != ap }

@JvmName("faktum_erUlik_faktum")
infix fun <T : Any> Faktum<T>.erUlik(ap: Faktum<T>) = ComparisonOperation(
    this, ap, ULIK
) { this.evaluer() != ap.evaluer() }

/**
 * Number-specific erLik/erUlik (for value-to-value comparisons)
 */
@JvmName("number_erLik_number")
infix fun Number.erLik(other: Number) = ComparisonOperation(
    Const(this), Const(other), LIK
) { this == other }

@JvmName("number_erLik_faktum_number")
infix fun Number.erLik(other: Faktum<out Number>) = ComparisonOperation(
    Const(this), other, LIK
) { this == other.evaluer() }

@JvmName("number_erUlik_number")
infix fun Number.erUlik(other: Number) = ComparisonOperation(
    Const(this), Const(other), ULIK
) { this != other }

@JvmName("number_erUlik_faktum_number")
infix fun Number.erUlik(other: Faktum<out Number>) = ComparisonOperation(
    Const(this), other, ULIK
) { this != other.evaluer() }

/**
 * Uttrykk
 */

@JvmName("uttrykk_erBlant_list")
infix fun <T : Any> Uttrykk<T>.erBlant(others: List<T>) = ListOperation(
    operator = ER_BLANDT,
    uttrykk = this,
    mengdeUttrykk = Const(others)
) { this.evaluer() in others }

@JvmName("uttrykk_erBlant_list_faktum")
infix fun <T : Any> Uttrykk<T>.erBlant(others: List<Faktum<T>>) = ListOperation(
    operator = ER_BLANDT,
    uttrykk = this,
    mengdeUttrykk = Const(others.map { faktum -> faktum.evaluer() })
) { this.evaluer() in others.map { it.evaluer() } }

@JvmName("uttrykk_erBlant_faktum_list")
infix fun <T : Any> Uttrykk<T>.erBlant(other: Faktum<List<T>>) = ListOperation(
    operator = ER_BLANDT,
    uttrykk = this,
    mengdeUttrykk = other
) { this.evaluer() in other.evaluer() }

@JvmName("uttrykk_erIkkeBlant_list")
infix fun <T : Any> Uttrykk<T>.erIkkeBlant(others: List<T>) = ListOperation(
    operator = ER_IKKE_BLANDT,
    uttrykk = this,
    mengdeUttrykk = Const(others)
) { this.evaluer() !in others }

@JvmName("uttrykk_erIkkeBlant_list_faktum")
infix fun <T : Any> Uttrykk<T>.erIkkeBlant(others: List<Faktum<T>>) = ListOperation(
    operator = ER_IKKE_BLANDT,
    uttrykk = this,
    mengdeUttrykk = Const(others.map { faktum -> faktum.evaluer() })
) { this.evaluer() !in others.map { it.evaluer() } }

@JvmName("uttrykk_erIkkeBlant_faktum_list")
infix fun <T : Any> Uttrykk<T>.erIkkeBlant(other: Faktum<List<T>>) = ListOperation(
    operator = ER_IKKE_BLANDT,
    uttrykk = this,
    mengdeUttrykk = other
) { this.evaluer() !in other.evaluer() }