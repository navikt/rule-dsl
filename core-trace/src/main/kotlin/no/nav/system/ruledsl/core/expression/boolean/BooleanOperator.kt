package no.nav.system.ruledsl.core.expression.boolean

import no.nav.system.ruledsl.core.expression.Faktum
import no.nav.system.ruledsl.core.expression.Expression
import no.nav.system.ruledsl.core.expression.Verdi
import java.time.LocalDate
import java.time.YearMonth

/**
 * Internal helper functions to reduce boilerplate in predicate operator definitions.
 */
private inline fun <T : Any> comparison(
    left: Expression<T>,
    right: Expression<T>,
    operator: PairOperator,
    crossinline eval: () -> Boolean
): Expression<Boolean> = Comparison(left, right, operator) { eval() }

private inline fun <T : Any> faktumValue(
    faktum: Faktum<T>,
    value: T,
    operator: PairOperator,
    crossinline eval: () -> Boolean
): Expression<Boolean> = comparison(faktum, Verdi(value), operator, eval)

private inline fun <T : Any> valueFaktum(
    value: T,
    faktum: Faktum<T>,
    operator: PairOperator,
    crossinline eval: () -> Boolean
): Expression<Boolean> = comparison(Verdi(value), faktum, operator, eval)

private inline fun <T : Any> valueValue(
    left: T,
    right: T,
    operator: PairOperator,
    crossinline eval: () -> Boolean
): Expression<Boolean> = comparison(Verdi(left), Verdi(right), operator, eval)

/**
 * LocalDate comparison operators
 */
@JvmName("faktum_localdate_erFørEllerLik_faktum_localdate")
infix fun Faktum<LocalDate>.erFørEllerLik(other: Faktum<LocalDate>): Expression<Boolean> =
    comparison(this, other, PairOperator.FØR_ELLER_LIK) { this.value <= other.value }

@JvmName("faktum_localdate_erFørEllerLik_localdate")
infix fun Faktum<LocalDate>.erFørEllerLik(other: LocalDate): Expression<Boolean> =
    faktumValue(this, other, PairOperator.FØR_ELLER_LIK) { this.value <= other }

@JvmName("localdate_erFørEllerLik_localdate")
infix fun LocalDate.erFørEllerLik(other: LocalDate): Expression<Boolean> =
    valueValue(this, other, PairOperator.FØR_ELLER_LIK) { this <= other }

@JvmName("localdate_erFørEllerLik_faktum_localdate")
infix fun LocalDate.erFørEllerLik(other: Faktum<LocalDate>): Expression<Boolean> =
    valueFaktum(this, other, PairOperator.FØR_ELLER_LIK) { this <= other.value }

@JvmName("faktum_localdate_erFør_faktum_localdate")
infix fun Faktum<LocalDate>.erFør(other: Faktum<LocalDate>): Expression<Boolean> =
    comparison(this, other, PairOperator.FØR) { value < other.value }

@JvmName("faktum_localdate_erFør_localdate")
infix fun Faktum<LocalDate>.erFør(other: LocalDate): Expression<Boolean> =
    faktumValue(this, other, PairOperator.FØR) { value < other }

@JvmName("localdate_erFør_faktum_localdate")
infix fun LocalDate.erFør(other: Faktum<LocalDate>): Expression<Boolean> =
    valueFaktum(this, other, PairOperator.FØR) { this < other.value }

@JvmName("localdate_erFør_localdate")
infix fun LocalDate.erFør(other: LocalDate): Expression<Boolean> =
    valueValue(this, other, PairOperator.FØR) { this < other }

@JvmName("faktum_localdate_erEtterEllerLik_faktum_localdate")
infix fun Faktum<LocalDate>.erEtterEllerLik(other: Faktum<LocalDate>): Expression<Boolean> =
    comparison(this, other, PairOperator.ETTER_ELLER_LIK) { value >= other.value }

@JvmName("faktum_localdate_erEtterEllerLik_localdate")
infix fun Faktum<LocalDate>.erEtterEllerLik(other: LocalDate): Expression<Boolean> =
    faktumValue(this, other, PairOperator.ETTER_ELLER_LIK) { value >= other }

@JvmName("faktum_localdate_erEtter_localdate")
infix fun Faktum<LocalDate>.erEtter(other: LocalDate): Expression<Boolean> =
    faktumValue(this, other, PairOperator.ETTER) { value > other }

@JvmName("faktum_localdate_erEtter_faktum_localdate")
infix fun Faktum<LocalDate>.erEtter(other: Faktum<LocalDate>): Expression<Boolean> =
    comparison(this, other, PairOperator.ETTER) { value > other.value }

/**
 * YearMonth -> LocalDate cross-type comparison operators
 */
@JvmName("faktum_yearmonth_erFørEllerLik_faktum_localdate")
infix fun Faktum<YearMonth>.erFørEllerLik(other: Faktum<LocalDate>): Expression<Boolean> =
    Comparison(this, other, PairOperator.FØR_ELLER_LIK) { this.value.atDay(1) <= other.value }

@JvmName("faktum_yearmonth_erFør_faktum_localdate")
infix fun Faktum<YearMonth>.erFør(other: Faktum<LocalDate>): Expression<Boolean> =
    Comparison(this, other, PairOperator.FØR) { value.atDay(1) < other.value }

@JvmName("faktum_yearmonth_erFør_localdate")
infix fun Faktum<YearMonth>.erFør(other: LocalDate): Expression<Boolean> =
    Comparison(this, Verdi(other), PairOperator.FØR) { value.atDay(1) < other }

@JvmName("faktum_yearmonth_erEtterEllerLik_faktum_localdate")
infix fun Faktum<YearMonth>.erEtterEllerLik(other: Faktum<LocalDate>): Expression<Boolean> =
    Comparison(this, other, PairOperator.ETTER_ELLER_LIK) { value.atDay(1) >= other.value }

@JvmName("faktum_yearmonth_erEtter_faktum_localdate")
infix fun Faktum<YearMonth>.erEtter(other: Faktum<LocalDate>): Expression<Boolean> =
    Comparison(this, other, PairOperator.ETTER) { value.atDay(1) > other.value }

/**
 * LocalDate -> YearMonth cross-type comparison operators
 */
@JvmName("faktum_localdate_erFørEllerLik_faktum_yearmonth")
infix fun Faktum<LocalDate>.erFørEllerLik(other: Faktum<YearMonth>): Expression<Boolean> =
    Comparison(this, other, PairOperator.FØR_ELLER_LIK) { this.value <= other.value.atDay(1) }

@JvmName("faktum_localdate_erFør_faktum_yearmonth")
infix fun Faktum<LocalDate>.erFør(other: Faktum<YearMonth>): Expression<Boolean> =
    Comparison(this, other, PairOperator.FØR) { value < other.value.atDay(1) }

@JvmName("faktum_localdate_erFør_yearmonth")
infix fun Faktum<LocalDate>.erFør(other: YearMonth): Expression<Boolean> =
    Comparison(this, Verdi(other), PairOperator.FØR) { value < other.atDay(1) }

@JvmName("faktum_localdate_erEtterEllerLik_faktum_yearmonth")
infix fun Faktum<LocalDate>.erEtterEllerLik(other: Faktum<YearMonth>): Expression<Boolean> =
    Comparison(this, other, PairOperator.ETTER_ELLER_LIK) { value >= other.value.atDay(1) }

@JvmName("faktum_localdate_erEtter_faktum_yearmonth")
infix fun Faktum<LocalDate>.erEtter(other: Faktum<YearMonth>): Expression<Boolean> =
    Comparison(this, other, PairOperator.ETTER) { value > other.value.atDay(1) }

/**
 * YearMonth comparison operators
 */
@JvmName("faktum_yearmonth_erFørEllerLik_faktum_yearmonth")
infix fun Faktum<YearMonth>.erFørEllerLik(other: Faktum<YearMonth>): Expression<Boolean> =
    comparison(this, other, PairOperator.FØR_ELLER_LIK) { this.value <= other.value }

@JvmName("faktum_yearmonth_erFør_faktum_yearmonth")
infix fun Faktum<YearMonth>.erFør(other: Faktum<YearMonth>): Expression<Boolean> =
    comparison(this, other, PairOperator.FØR) { value < other.value }

@JvmName("faktum_yearmonth_erFør_yearmonth")
infix fun Faktum<YearMonth>.erFør(other: YearMonth): Expression<Boolean> =
    faktumValue(this, other, PairOperator.FØR) { value < other }

@JvmName("faktum_yearmonth_erEtterEllerLik_faktum_yearmonth")
infix fun Faktum<YearMonth>.erEtterEllerLik(other: Faktum<YearMonth>): Expression<Boolean> =
    comparison(this, other, PairOperator.ETTER_ELLER_LIK) { value >= other.value }

@JvmName("faktum_yearmonth_erEtter_faktum_yearmonth")
infix fun Faktum<YearMonth>.erEtter(other: Faktum<YearMonth>): Expression<Boolean> =
    comparison(this, other, PairOperator.ETTER) { value > other.value }

/**
 * Number comparison operators
 */
@JvmName("faktum_number_erMindreEllerLik_faktum_number")
infix fun Faktum<out Number>.erMindreEllerLik(other: Faktum<out Number>): Expression<Boolean> =
    Comparison(this, other, PairOperator.MINDRE_ELLER_LIK) { value.toDouble() <= other.value.toDouble() }

@JvmName("faktum_number_erMindreEllerLik_number")
infix fun Faktum<out Number>.erMindreEllerLik(other: Number): Expression<Boolean> =
    Comparison(this, Verdi(other), PairOperator.MINDRE_ELLER_LIK) { value.toDouble() <= other.toDouble() }

@JvmName("number_erMindreEllerLik_faktum_number")
infix fun Number.erMindreEllerLik(other: Faktum<out Number>): Expression<Boolean> =
    Comparison(Verdi(this), other, PairOperator.MINDRE_ELLER_LIK) { toDouble() <= other.value.toDouble() }

@JvmName("number_erMindreEllerLik_number")
infix fun Number.erMindreEllerLik(other: Number): Expression<Boolean> =
    Comparison(Verdi(this), Verdi(other), PairOperator.MINDRE_ELLER_LIK) { toDouble() <= other.toDouble() }

@JvmName("faktum_number_erMindreEnn_faktum_number")
infix fun Faktum<out Number>.erMindreEnn(other: Faktum<out Number>): Expression<Boolean> =
    Comparison(this, other, PairOperator.MINDRE) { value.toDouble() < other.value.toDouble() }

@JvmName("faktum_number_erMindreEnn_number")
infix fun Faktum<out Number>.erMindreEnn(other: Number): Expression<Boolean> =
    Comparison(this, Verdi(other), PairOperator.MINDRE) { value.toDouble() < other.toDouble() }

@JvmName("number_erMindreEnn_faktum_number")
infix fun Number.erMindreEnn(other: Faktum<out Number>): Expression<Boolean> =
    Comparison(Verdi(this), other, PairOperator.MINDRE) { toDouble() < other.value.toDouble() }

@JvmName("number_erMindreEnn_number")
infix fun Number.erMindreEnn(other: Number): Expression<Boolean> =
    Comparison(Verdi(this), Verdi(other), PairOperator.MINDRE) { toDouble() < other.toDouble() }

@JvmName("faktum_number_erStørreEllerLik_faktum_number")
infix fun Faktum<out Number>.erStørreEllerLik(other: Faktum<out Number>): Expression<Boolean> =
    Comparison(this, other, PairOperator.STØRRE_ELLER_LIK) { value.toDouble() >= other.value.toDouble() }

@JvmName("faktum_number_erStørreEnn_faktum_number")
infix fun Faktum<out Number>.erStørreEnn(other: Faktum<out Number>): Expression<Boolean> =
    Comparison(this, other, PairOperator.STØRRE) { value.toDouble() > other.value.toDouble() }

@JvmName("faktum_number_erStørreEllerLik_number")
infix fun Faktum<out Number>.erStørreEllerLik(other: Number): Expression<Boolean> =
    Comparison(this, Verdi(other), PairOperator.STØRRE_ELLER_LIK) { value.toDouble() >= other.toDouble() }

@JvmName("faktum_number_erStørreEnn_number")
infix fun Faktum<out Number>.erStørreEnn(other: Number): Expression<Boolean> =
    Comparison(this, Verdi(other), PairOperator.STØRRE) { value.toDouble() > other.toDouble() }

@JvmName("number_erStørreEllerLik_faktum_number")
infix fun Number.erStørreEllerLik(other: Faktum<out Number>): Expression<Boolean> =
    Comparison(Verdi(this), other, PairOperator.STØRRE_ELLER_LIK) { toDouble() >= other.value.toDouble() }

@JvmName("number_erStørreEnn_faktum_number")
infix fun Number.erStørreEnn(other: Faktum<out Number>): Expression<Boolean> =
    Comparison(Verdi(this), other, PairOperator.STØRRE) { toDouble() > other.value.toDouble() }

@JvmName("number_erStørreEllerLik_number")
infix fun Number.erStørreEllerLik(other: Number): Expression<Boolean> =
    Comparison(Verdi(this), Verdi(other), PairOperator.STØRRE_ELLER_LIK) { toDouble() >= other.toDouble() }

@JvmName("number_erStørreEnn_number")
infix fun Number.erStørreEnn(other: Number): Expression<Boolean> =
    Comparison(Verdi(this), Verdi(other), PairOperator.STØRRE) { toDouble() > other.toDouble() }

/**
 * LocalDate -> Number comparison operators
 * TODO: These appear imprecise. Consider removing or renaming to erÅrTidligereEllerLik, etc.
 */
infix fun Faktum<out LocalDate>.erMindreEllerLik(other: Int): Expression<Boolean> =
    Comparison(this, Verdi(other), PairOperator.MINDRE_ELLER_LIK) { value.year <= other }

infix fun Faktum<out LocalDate>.erMindreEnn(other: Int): Expression<Boolean> =
    Comparison(this, Verdi(other), PairOperator.MINDRE) { value.year < other }

infix fun Faktum<out LocalDate>.erStørreEllerLik(other: Int): Expression<Boolean> =
    Comparison(this, Verdi(other), PairOperator.STØRRE_ELLER_LIK) { value.year >= other }

infix fun Faktum<out LocalDate>.erStørreEnn(other: Int): Expression<Boolean> =
    Comparison(this, Verdi(other), PairOperator.STØRRE) { value.year > other }

/**
 * Generic equality operators (work for any type)
 */
@JvmName("faktum_erLik_value")
infix fun <T : Any> Faktum<T>.erLik(ap: T): Expression<Boolean> =
    faktumValue(this, ap, PairOperator.LIK) { this.value == ap }

@JvmName("faktum_erLik_faktum")
infix fun <T : Any> Faktum<T>.erLik(ap: Faktum<T>): Expression<Boolean> =
    comparison(this, ap, PairOperator.LIK) { this.value == ap.value }

@JvmName("faktum_erUlik_value")
infix fun <T : Any> Faktum<T>.erUlik(ap: T): Expression<Boolean> =
    faktumValue(this, ap, PairOperator.ULIK) { this.value != ap }

@JvmName("faktum_erUlik_faktum")
infix fun <T : Any> Faktum<T>.erUlik(ap: Faktum<T>): Expression<Boolean> =
    comparison(this, ap, PairOperator.ULIK) { this.value != ap.value }

/**
 * Number-specific equality operators (for value-to-value comparisons)
 */
@JvmName("number_erLik_number")
infix fun Number.erLik(other: Number): Expression<Boolean> =
    valueValue(this, other, PairOperator.LIK) { this == other }

@JvmName("number_erLik_faktum_number")
infix fun Number.erLik(other: Faktum<out Number>): Expression<Boolean> =
    Comparison(Verdi(this), other, PairOperator.LIK) { this == other.value }

@JvmName("number_erUlik_number")
infix fun Number.erUlik(other: Number): Expression<Boolean> =
    valueValue(this, other, PairOperator.ULIK) { this != other }

@JvmName("number_erUlik_faktum_number")
infix fun Number.erUlik(other: Faktum<out Number>): Expression<Boolean> =
    Comparison(Verdi(this), other, PairOperator.ULIK) { this != other.value }

/**
 * List membership operators
 */
@JvmName("uttrykk_erBlant_list")
infix fun <T : Any> Expression<T>.erBlant(others: List<T>): Expression<Boolean> = ListOperation(
    operator = ListOperator.ER_BLANDT,
    expression = this,
    list = Verdi(others)
) { this.value in others }

@JvmName("uttrykk_erBlant_list_faktum")
infix fun <T : Any> Expression<T>.erBlant(others: List<Faktum<T>>): Expression<Boolean> = ListOperation(
    operator = ListOperator.ER_BLANDT,
    expression = this,
    list = Verdi(others.map { faktum -> faktum.value })
) { this.value in others.map { it.value } }

@JvmName("uttrykk_erBlant_faktum_list")
infix fun <T : Any> Expression<T>.erBlant(other: Faktum<List<T>>): Expression<Boolean> = ListOperation(
    operator = ListOperator.ER_BLANDT,
    expression = this,
    list = other
) { this.value in other.value }

@JvmName("uttrykk_erIkkeBlant_list")
infix fun <T : Any> Expression<T>.erIkkeBlant(others: List<T>): Expression<Boolean> = ListOperation(
    operator = ListOperator.ER_IKKE_BLANDT,
    expression = this,
    list = Verdi(others)
) { this.value !in others }

@JvmName("uttrykk_erIkkeBlant_list_faktum")
infix fun <T : Any> Expression<T>.erIkkeBlant(others: List<Faktum<T>>): Expression<Boolean> = ListOperation(
    operator = ListOperator.ER_IKKE_BLANDT,
    expression = this,
    list = Verdi(others.map { faktum -> faktum.value })
) { this.value !in others.map { it.value } }

@JvmName("uttrykk_erIkkeBlant_faktum_list")
infix fun <T : Any> Expression<T>.erIkkeBlant(other: Faktum<List<T>>): Expression<Boolean> = ListOperation(
    operator = ListOperator.ER_IKKE_BLANDT,
    expression = this,
    list = other
) { this.value !in other.value }

/**
 * Verdi number comparison operators
 */
@JvmName("verdi_number_erMindreEllerLik_verdi_number")
infix fun Verdi<out Number>.erMindreEllerLik(other: Verdi<out Number>): Expression<Boolean> =
    Comparison(this, other, PairOperator.MINDRE_ELLER_LIK) { value.toDouble() <= other.value.toDouble() }

@JvmName("verdi_number_erMindreEllerLik_number")
infix fun Verdi<out Number>.erMindreEllerLik(other: Number): Expression<Boolean> =
    Comparison(this, Verdi(other), PairOperator.MINDRE_ELLER_LIK) { value.toDouble() <= other.toDouble() }

@JvmName("verdi_number_erMindreEnn_verdi_number")
infix fun Verdi<out Number>.erMindreEnn(other: Verdi<out Number>): Expression<Boolean> =
    Comparison(this, other, PairOperator.MINDRE) { value.toDouble() < other.value.toDouble() }

@JvmName("verdi_number_erMindreEnn_number")
infix fun Verdi<out Number>.erMindreEnn(other: Number): Expression<Boolean> =
    Comparison(this, Verdi(other), PairOperator.MINDRE) { value.toDouble() < other.toDouble() }

@JvmName("verdi_number_erStørreEllerLik_verdi_number")
infix fun Verdi<out Number>.erStørreEllerLik(other: Verdi<out Number>): Expression<Boolean> =
    Comparison(this, other, PairOperator.STØRRE_ELLER_LIK) { value.toDouble() >= other.value.toDouble() }

@JvmName("verdi_number_erStørreEllerLik_number")
infix fun Verdi<out Number>.erStørreEllerLik(other: Number): Expression<Boolean> =
    Comparison(this, Verdi(other), PairOperator.STØRRE_ELLER_LIK) { value.toDouble() >= other.toDouble() }

@JvmName("verdi_number_erStørreEnn_verdi_number")
infix fun Verdi<out Number>.erStørreEnn(other: Verdi<out Number>): Expression<Boolean> =
    Comparison(this, other, PairOperator.STØRRE) { value.toDouble() > other.value.toDouble() }

@JvmName("verdi_number_erStørreEnn_number")
infix fun Verdi<out Number>.erStørreEnn(other: Number): Expression<Boolean> =
    Comparison(this, Verdi(other), PairOperator.STØRRE) { value.toDouble() > other.toDouble() }

/**
 * Verdi generic equality operators
 */
@JvmName("verdi_erLik_value")
infix fun <T : Any> Verdi<T>.erLik(other: T): Expression<Boolean> =
    Comparison(this, Verdi(other), PairOperator.LIK) { this.value == other }

@JvmName("verdi_erLik_verdi")
infix fun <T : Any> Verdi<T>.erLik(other: Verdi<T>): Expression<Boolean> =
    Comparison(this, other, PairOperator.LIK) { this.value == other.value }

@JvmName("verdi_erUlik_value")
infix fun <T : Any> Verdi<T>.erUlik(other: T): Expression<Boolean> =
    Comparison(this, Verdi(other), PairOperator.ULIK) { this.value != other }

@JvmName("verdi_erUlik_verdi")
infix fun <T : Any> Verdi<T>.erUlik(other: Verdi<T>): Expression<Boolean> =
    Comparison(this, other, PairOperator.ULIK) { this.value != other.value }
