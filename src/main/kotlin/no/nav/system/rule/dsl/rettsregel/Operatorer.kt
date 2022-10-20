package no.nav.system.rule.dsl.rettsregel

import no.nav.system.rule.dsl.enums.Komparator.*
import java.time.LocalDate

/**
 * Datoer
 */
infix fun Faktum<LocalDate>.erFørEllerLik(other: Faktum<LocalDate>) =
    ParSubsumsjon(FØR_ELLER_LIK, this, other) { verdi <= other.verdi }

infix fun Faktum<LocalDate>.erFør(other: Faktum<LocalDate>) = ParSubsumsjon(FØR, this, other) { verdi < other.verdi }

infix fun Faktum<LocalDate>.erEtterEllerLik(other: Faktum<LocalDate>) =
    ParSubsumsjon(ETTER_ELLER_LIK, this, other) { verdi >= other.verdi }

infix fun Faktum<LocalDate>.erEtter(other: Faktum<LocalDate>) = ParSubsumsjon(ETTER, this, other) { verdi > other.verdi }

/**
 * Tall
 */
infix fun Faktum<out Number>.erMindreEllerLik(other: Faktum<out Number>) = ParSubsumsjon(
    MINDRE_ELLER_LIK, this, other
) { verdi.toDouble() <= other.verdi.toDouble() }

infix fun Faktum<out Number>.erMindreEnn(other: Faktum<out Number>) = ParSubsumsjon(
    MINDRE, this, other
) { verdi.toDouble() < other.verdi.toDouble() }

infix fun Faktum<out Number>.erStørreEllerLik(other: Faktum<out Number>) = ParSubsumsjon(
    STØRRE_ELLER_LIK, this, other
) { verdi.toDouble() >= other.verdi.toDouble() }

infix fun Faktum<out Number>.erStørre(other: Faktum<out Number>) = ParSubsumsjon(
    STØRRE, this, other
) { verdi.toDouble() > other.verdi.toDouble() }

infix fun Faktum<out Number>.erMindreEllerLik(other: Number) = ParSubsumsjon(
    MINDRE_ELLER_LIK, this, Faktum(other)
) { verdi.toDouble() <= other.toDouble() }

infix fun Faktum<out Number>.erMindreEnn(other: Number) = ParSubsumsjon(
    MINDRE, this, Faktum(other)
) { verdi.toDouble() < other.toDouble() }

infix fun Faktum<out Number>.erStørreEllerLik(other: Number) = ParSubsumsjon(
    STØRRE_ELLER_LIK, this, Faktum(other)
) { verdi.toDouble() >= other.toDouble() }

infix fun Faktum<out Number>.erStørre(other: Number) = ParSubsumsjon(
    STØRRE, this, Faktum(other)
) { verdi.toDouble() > other.toDouble() }


/**
 * Boolean
 * TODO Burde ikke trenge disse. Faktum/regler (generellt alle RuleComponents) bør kunne brukes fritt som regelbetingelser. Vurder å legge inn Faktum.not override.
 */
fun Faktum<Boolean>.erSann() = ParSubsumsjon(LIK, this, Faktum("SANN", true)) { verdi }

fun Faktum<Boolean>.erUsann() = ParSubsumsjon(LIK, this, Faktum("USANN", false)) { !verdi }

operator fun Faktum<Boolean>.not(): Faktum<Boolean> = Faktum(this.navn, !this.verdi)

/**
 * Dato > Tall
 */
infix fun Faktum<out LocalDate>.erMindreEllerLik(other: Int) = ParSubsumsjon(
    MINDRE_ELLER_LIK, this, Faktum(other)
) { verdi.year <= other }

infix fun Faktum<out LocalDate>.erMindreEnn(other: Int) = ParSubsumsjon(
    MINDRE, this, Faktum(other)
) { verdi.year < other }

infix fun Faktum<out LocalDate>.erStørreEllerLik(other: Int) = ParSubsumsjon(
    STØRRE_ELLER_LIK, this, Faktum(other)
) { verdi.year >= other }

infix fun Faktum<out LocalDate>.erStørreEnn(other: Int) = ParSubsumsjon(
    STØRRE, this, Faktum(other)
) { verdi.year > other }

/**
 * Generisk
 */
infix fun <T> Faktum<T>.erLik(ap: T) = ParSubsumsjon(
    LIK, this, Faktum(ap)
) { this.verdi == ap }

infix fun <T> Faktum<T>.erLik(ap: Faktum<T>) = ParSubsumsjon(
    LIK, this, ap
) { this.verdi == ap.verdi }


infix fun <T> Faktum<T>.erBlant(others: List<T>) = ParSubsumsjon(
    ER_BLANDT, this, Faktum(others.joinToString(", "))
) { this.verdi in others }