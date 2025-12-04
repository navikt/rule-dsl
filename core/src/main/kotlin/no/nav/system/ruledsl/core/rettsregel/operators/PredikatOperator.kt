package no.nav.system.ruledsl.core.rettsregel.operators

import no.nav.system.ruledsl.core.rettsregel.ComparisonOperation
import no.nav.system.ruledsl.core.rettsregel.Const
import no.nav.system.ruledsl.core.rettsregel.Faktum
import no.nav.system.ruledsl.core.rettsregel.ListOperation
import no.nav.system.ruledsl.core.rettsregel.Uttrykk
import java.time.LocalDate
import java.time.YearMonth

/**
 * Datoer (localDate)
 */
@JvmName("faktum_localdate_erFørEllerLik_faktum_localdate")
infix fun Faktum<LocalDate>.erFørEllerLik(other: Faktum<LocalDate>) : Uttrykk<Boolean> =
    ComparisonOperation(this, other, PairOperator.FØR_ELLER_LIK) { this.verdi <= other.verdi }

@JvmName("faktum_localdate_erFørEllerLik_localdate")
infix fun Faktum<LocalDate>.erFørEllerLik(other: LocalDate) : Uttrykk<Boolean> =
    ComparisonOperation(this, Const(other), PairOperator.FØR_ELLER_LIK) { this.verdi <= other }

@JvmName("localdate_erFørEllerLik_localdate")
infix fun LocalDate.erFørEllerLik(other: LocalDate) : Uttrykk<Boolean> =
    ComparisonOperation(Const(this), Const(other), PairOperator.FØR_ELLER_LIK) { this <= other }

@JvmName("localdate_erFørEllerLik_faktum_localdate")
infix fun LocalDate.erFørEllerLik(other: Faktum<LocalDate>) : Uttrykk<Boolean> =
    ComparisonOperation(Const(this), other, PairOperator.FØR_ELLER_LIK) { this <= other.verdi }

@JvmName("faktum_localdate_erFør_faktum_localdate")
infix fun Faktum<LocalDate>.erFør(other: Faktum<LocalDate>) : Uttrykk<Boolean> =
    ComparisonOperation(this, other, PairOperator.FØR) { verdi < other.verdi }

@JvmName("faktum_localdate_erFør_localdate")
infix fun Faktum<LocalDate>.erFør(other: LocalDate) : Uttrykk<Boolean> =
    ComparisonOperation(this, Const(other), PairOperator.FØR) { verdi < other }

@JvmName("localdate_erFør_faktum_localdate")
infix fun LocalDate.erFør(other: Faktum<LocalDate>) : Uttrykk<Boolean> =
    ComparisonOperation(Const(this), other, PairOperator.FØR) { this < other.verdi }

@JvmName("localdate_erFør_localdate")
infix fun LocalDate.erFør(other: LocalDate) : Uttrykk<Boolean> =
    ComparisonOperation(Const(this), Const(other), PairOperator.FØR) { this < other }

@JvmName("faktum_localdate_erEtterEllerLik_faktum_localdate")
infix fun Faktum<LocalDate>.erEtterEllerLik(other: Faktum<LocalDate>) : Uttrykk<Boolean> =
    ComparisonOperation(this, other, PairOperator.ETTER_ELLER_LIK) { verdi >= other.verdi }

@JvmName("faktum_localdate_erEtterEllerLik_localdate")
infix fun Faktum<LocalDate>.erEtterEllerLik(other: LocalDate) : Uttrykk<Boolean> =
    ComparisonOperation(this, Const(other), PairOperator.ETTER_ELLER_LIK) { verdi >= other }

@JvmName("faktum_localdate_erEtter_localdate")
infix fun Faktum<LocalDate>.erEtter(other: LocalDate) : Uttrykk<Boolean> =
    ComparisonOperation(this, Const(other), PairOperator.ETTER) { verdi > other }

@JvmName("faktum_localdate_erEtter_faktum_localdate")
infix fun Faktum<LocalDate>.erEtter(other: Faktum<LocalDate>) : Uttrykk<Boolean> =
    ComparisonOperation(this, other, PairOperator.ETTER) { verdi > other.verdi }

// yearMonth -> localDate
@JvmName("faktum_yearmonth_erFørEllerLik_faktum_localdate")
infix fun Faktum<YearMonth>.erFørEllerLik(other: Faktum<LocalDate>) : Uttrykk<Boolean> =
    ComparisonOperation(this, other, PairOperator.FØR_ELLER_LIK) { this.verdi.atDay(1) <= other.verdi }

@JvmName("faktum_yearmonth_erFør_faktum_localdate")
infix fun Faktum<YearMonth>.erFør(other: Faktum<LocalDate>) : Uttrykk<Boolean> =
    ComparisonOperation(this, other, PairOperator.FØR) { verdi.atDay(1) < other.verdi }

@JvmName("faktum_yearmonth_erFør_localdate")
infix fun Faktum<YearMonth>.erFør(other: LocalDate) : Uttrykk<Boolean> =
    ComparisonOperation(this, Const(other), PairOperator.FØR) { verdi.atDay(1) < other }

@JvmName("faktum_yearmonth_erEtterEllerLik_faktum_localdate")
infix fun Faktum<YearMonth>.erEtterEllerLik(other: Faktum<LocalDate>) : Uttrykk<Boolean> =
    ComparisonOperation(this, other, PairOperator.ETTER_ELLER_LIK) { verdi.atDay(1) >= other.verdi }

@JvmName("faktum_yearmonth_erEtter_faktum_localdate")
infix fun Faktum<YearMonth>.erEtter(other: Faktum<LocalDate>) : Uttrykk<Boolean> =
    ComparisonOperation(this, other, PairOperator.ETTER) { verdi.atDay(1) > other.verdi }

// localDate -> yearMonth

@JvmName("faktum_localdate_erFørEllerLik_faktum_yearmonth")
infix fun Faktum<LocalDate>.erFørEllerLik(other: Faktum<YearMonth>) : Uttrykk<Boolean> =
    ComparisonOperation(this, other, PairOperator.FØR_ELLER_LIK) { this.verdi <= other.verdi.atDay(1) }

@JvmName("faktum_localdate_erFør_faktum_yearmonth")
infix fun Faktum<LocalDate>.erFør(other: Faktum<YearMonth>) : Uttrykk<Boolean> =
    ComparisonOperation(this, other, PairOperator.FØR) { verdi < other.verdi.atDay(1) }

@JvmName("faktum_localdate_erFør_yearmonth")
infix fun Faktum<LocalDate>.erFør(other: YearMonth) : Uttrykk<Boolean> =
    ComparisonOperation(this, Const(other), PairOperator.FØR) { verdi < other.atDay(1) }

@JvmName("faktum_localdate_erEtterEllerLik_faktum_yearmonth")
infix fun Faktum<LocalDate>.erEtterEllerLik(other: Faktum<YearMonth>) : Uttrykk<Boolean> =
    ComparisonOperation(this, other, PairOperator.ETTER_ELLER_LIK) { verdi >= other.verdi.atDay(1) }

@JvmName("faktum_localdate_erEtter_faktum_yearmonth")
infix fun Faktum<LocalDate>.erEtter(other: Faktum<YearMonth>) : Uttrykk<Boolean> =
    ComparisonOperation(this, other, PairOperator.ETTER) { verdi > other.verdi.atDay(1) }

// YearMonth -> YearMonth
@JvmName("faktum_yearmonth_erFørEllerLik_faktum_yearmonth")
infix fun Faktum<YearMonth>.erFørEllerLik(other: Faktum<YearMonth>) : Uttrykk<Boolean> =
    ComparisonOperation(this, other, PairOperator.FØR_ELLER_LIK) { this.verdi <= other.verdi }

@JvmName("faktum_yearmonth_erFør_faktum_yearmonth")
infix fun Faktum<YearMonth>.erFør(other: Faktum<YearMonth>) : Uttrykk<Boolean> =
    ComparisonOperation(this, other, PairOperator.FØR) { verdi < other.verdi }

@JvmName("faktum_yearmonth_erFør_yearmonth")
infix fun Faktum<YearMonth>.erFør(other: YearMonth) : Uttrykk<Boolean> =
    ComparisonOperation(this, Const(other), PairOperator.FØR) { verdi < other }

@JvmName("faktum_yearmonth_erEtterEllerLik_faktum_yearmonth")
infix fun Faktum<YearMonth>.erEtterEllerLik(other: Faktum<YearMonth>) : Uttrykk<Boolean> =
    ComparisonOperation(this, other, PairOperator.ETTER_ELLER_LIK) { verdi >= other.verdi }

@JvmName("faktum_yearmonth_erEtter_faktum_yearmonth")
infix fun Faktum<YearMonth>.erEtter(other: Faktum<YearMonth>) : Uttrykk<Boolean> =
    ComparisonOperation(this, other, PairOperator.ETTER) { verdi > other.verdi }

/**
 * Tall
 */
@JvmName("faktum_number_erMindreEllerLik_faktum_number")
infix fun Faktum<out Number>.erMindreEllerLik(other: Faktum<out Number>) : Uttrykk<Boolean> =
    ComparisonOperation(this, other, PairOperator.MINDRE_ELLER_LIK) { verdi.toDouble() <= other.verdi.toDouble() }

@JvmName("faktum_number_erMindreEllerLik_number")
infix fun Faktum<out Number>.erMindreEllerLik(other: Number) : Uttrykk<Boolean> =
    ComparisonOperation(this, Const(other), PairOperator.MINDRE_ELLER_LIK) { verdi.toDouble() <= other.toDouble() }

@JvmName("number_erMindreEllerLik_faktum_number")
infix fun Number.erMindreEllerLik(other: Faktum<out Number>) : Uttrykk<Boolean> =
    ComparisonOperation(Const(this), other, PairOperator.MINDRE_ELLER_LIK) { toDouble() <= other.verdi.toDouble() }

@JvmName("number_erMindreEllerLik_number")
infix fun Number.erMindreEllerLik(other: Number) : Uttrykk<Boolean> =
    ComparisonOperation(Const(this), Const(other), PairOperator.MINDRE_ELLER_LIK) { toDouble() <= other.toDouble() }


@JvmName("faktum_number_erMindreEnn_faktum_number")
infix fun Faktum<out Number>.erMindreEnn(other: Faktum<out Number>) : Uttrykk<Boolean> =
    ComparisonOperation(this, other, PairOperator.MINDRE) { verdi.toDouble() < other.verdi.toDouble() }

@JvmName("faktum_number_erMindreEnn_number")
infix fun Faktum<out Number>.erMindreEnn(other: Number) : Uttrykk<Boolean> =
    ComparisonOperation(this, Const(other), PairOperator.MINDRE) { verdi.toDouble() < other.toDouble() }

@JvmName("number_erMindreEnn_faktum_number")
infix fun Number.erMindreEnn(other: Faktum<out Number>) : Uttrykk<Boolean> =
    ComparisonOperation(Const(this), other, PairOperator.MINDRE) { toDouble() < other.verdi.toDouble() }

@JvmName("number_erMindreEnn_number")
infix fun Number.erMindreEnn(other: Number) : Uttrykk<Boolean> =
    ComparisonOperation(Const(this), Const(other), PairOperator.MINDRE) { toDouble() < other.toDouble() }

@JvmName("faktum_number_erStørreEllerLik_faktum_number")
infix fun Faktum<out Number>.erStørreEllerLik(other: Faktum<out Number>) : Uttrykk<Boolean> =
    ComparisonOperation(this, other, PairOperator.STØRRE_ELLER_LIK) { verdi.toDouble() >= other.verdi.toDouble() }

@JvmName("faktum_number_erStørreEnn_faktum_number")
infix fun Faktum<out Number>.erStørreEnn(other: Faktum<out Number>) : Uttrykk<Boolean> =
    ComparisonOperation(this, other, PairOperator.STØRRE) { verdi.toDouble() > other.verdi.toDouble() }

@JvmName("faktum_number_erStørreEllerLik_number")
infix fun Faktum<out Number>.erStørreEllerLik(other: Number) : Uttrykk<Boolean> =
    ComparisonOperation(this, Const(other), PairOperator.STØRRE_ELLER_LIK) { verdi.toDouble() >= other.toDouble() }

@JvmName("faktum_number_erStørreEnn_number")
infix fun Faktum<out Number>.erStørreEnn(other: Number) : Uttrykk<Boolean> =
    ComparisonOperation(this, Const(other), PairOperator.STØRRE) { verdi.toDouble() > other.toDouble() }

@JvmName("number_erStørreEllerLik_faktum_number")
infix fun Number.erStørreEllerLik(other: Faktum<out Number>) : Uttrykk<Boolean> =
    ComparisonOperation(Const(this), other, PairOperator.STØRRE_ELLER_LIK) { toDouble() >= other.verdi.toDouble() }

@JvmName("number_erStørreEnn_faktum_number")
infix fun Number.erStørreEnn(other: Faktum<out Number>) : Uttrykk<Boolean> =
    ComparisonOperation(Const(this), other, PairOperator.STØRRE) { toDouble() > other.verdi.toDouble() }

@JvmName("number_erStørreEllerLik_number")
infix fun Number.erStørreEllerLik(other: Number) : Uttrykk<Boolean> =
    ComparisonOperation(Const(this), Const(other), PairOperator.STØRRE_ELLER_LIK) { toDouble() >= other.toDouble() }

@JvmName("number_erStørreEnn_number")
infix fun Number.erStørreEnn(other: Number) : Uttrykk<Boolean> =
    ComparisonOperation(Const(this), Const(other), PairOperator.STØRRE) { toDouble() > other.toDouble() }

/**
 * Dato > Tall
 */
// TODO: Disse ser upresise ut. Vurder behov og evt fjern. Dersom de skal beholdes,
// TODO: burde de trolig hete erÅrTidligereEllerLik, erÅrTidligereEnn, senere osv.
infix fun Faktum<out LocalDate>.erMindreEllerLik(other: Int) : Uttrykk<Boolean> = ComparisonOperation(
    this, Const(other), PairOperator.MINDRE_ELLER_LIK
) { verdi.year <= other }

infix fun Faktum<out LocalDate>.erMindreEnn(other: Int) : Uttrykk<Boolean> = ComparisonOperation(
    this, Const(other), PairOperator.MINDRE
) { verdi.year < other }

infix fun Faktum<out LocalDate>.erStørreEllerLik(other: Int) : Uttrykk<Boolean> = ComparisonOperation(
    this, Const(other), PairOperator.STØRRE_ELLER_LIK
) { verdi.year >= other }

infix fun Faktum<out LocalDate>.erStørreEnn(other: Int) : Uttrykk<Boolean> = ComparisonOperation(
    this, Const(other), PairOperator.STØRRE
) { verdi.year > other }

/**
 * Faktum - Generic
 */
@JvmName("faktum_erLik_value")
infix fun <T : Any> Faktum<T>.erLik(ap: T) : Uttrykk<Boolean> = ComparisonOperation(
    this, Const(ap), PairOperator.LIK
) { this.verdi == ap }

@JvmName("faktum_erLik_faktum")
infix fun <T : Any> Faktum<T>.erLik(ap: Faktum<T>) : Uttrykk<Boolean> = ComparisonOperation(
    this, ap, PairOperator.LIK
) { this.verdi == ap.verdi }

@JvmName("faktum_erUlik_value")
infix fun <T : Any> Faktum<T>.erUlik(ap: T) : Uttrykk<Boolean> = ComparisonOperation(
    this, Const(ap), PairOperator.ULIK
) { this.verdi != ap }

@JvmName("faktum_erUlik_faktum")
infix fun <T : Any> Faktum<T>.erUlik(ap: Faktum<T>) : Uttrykk<Boolean> = ComparisonOperation(
    this, ap, PairOperator.ULIK
) { this.verdi != ap.verdi }

/**
 * Number-specific erLik/erUlik (for value-to-value comparisons)
 */
@JvmName("number_erLik_number")
infix fun Number.erLik(other: Number) : Uttrykk<Boolean> = ComparisonOperation(
    Const(this), Const(other), PairOperator.LIK
) { this == other }

@JvmName("number_erLik_faktum_number")
infix fun Number.erLik(other: Faktum<out Number>) : Uttrykk<Boolean> = ComparisonOperation(
    Const(this), other, PairOperator.LIK
) { this == other.verdi }

@JvmName("number_erUlik_number")
infix fun Number.erUlik(other: Number) : Uttrykk<Boolean> = ComparisonOperation(
    Const(this), Const(other), PairOperator.ULIK
) { this != other }

@JvmName("number_erUlik_faktum_number")
infix fun Number.erUlik(other: Faktum<out Number>) : Uttrykk<Boolean> = ComparisonOperation(
    Const(this), other, PairOperator.ULIK
) { this != other.verdi }

/**
 * Uttrykk
 */

@JvmName("uttrykk_erBlant_list")
infix fun <T : Any> Uttrykk<T>.erBlant(others: List<T>) : Uttrykk<Boolean> = ListOperation(
    operator = ListOperator.ER_BLANDT,
    uttrykk = this,
    mengdeUttrykk = Const(others)
) { this.verdi in others }

@JvmName("uttrykk_erBlant_list_faktum")
infix fun <T : Any> Uttrykk<T>.erBlant(others: List<Faktum<T>>) : Uttrykk<Boolean> = ListOperation(
    operator = ListOperator.ER_BLANDT,
    uttrykk = this,
    mengdeUttrykk = Const(others.map { faktum -> faktum.verdi })
) { this.verdi in others.map { it.verdi } }

@JvmName("uttrykk_erBlant_faktum_list")
infix fun <T : Any> Uttrykk<T>.erBlant(other: Faktum<List<T>>) : Uttrykk<Boolean> = ListOperation(
    operator = ListOperator.ER_BLANDT,
    uttrykk = this,
    mengdeUttrykk = other
) { this.verdi in other.verdi }

@JvmName("uttrykk_erIkkeBlant_list")
infix fun <T : Any> Uttrykk<T>.erIkkeBlant(others: List<T>) : Uttrykk<Boolean> = ListOperation(
    operator = ListOperator.ER_IKKE_BLANDT,
    uttrykk = this,
    mengdeUttrykk = Const(others)
) { this.verdi !in others }

@JvmName("uttrykk_erIkkeBlant_list_faktum")
infix fun <T : Any> Uttrykk<T>.erIkkeBlant(others: List<Faktum<T>>) : Uttrykk<Boolean> = ListOperation(
    operator = ListOperator.ER_IKKE_BLANDT,
    uttrykk = this,
    mengdeUttrykk = Const(others.map { faktum -> faktum.verdi })
) { this.verdi !in others.map { it.verdi } }

@JvmName("uttrykk_erIkkeBlant_faktum_list")
infix fun <T : Any> Uttrykk<T>.erIkkeBlant(other: Faktum<List<T>>) : Uttrykk<Boolean> = ListOperation(
    operator = ListOperator.ER_IKKE_BLANDT,
    uttrykk = this,
    mengdeUttrykk = other
) { this.verdi !in other.verdi }