package no.nav.system.rule.dsl.rettsregel

import no.nav.system.rule.dsl.enums.MengdeKomparator.ER_BLANDT
import no.nav.system.rule.dsl.enums.MengdeKomparator.ER_IKKE_BLANDT
import no.nav.system.rule.dsl.enums.ParKomparator.*
import java.time.LocalDate

/**
 * Datoer
 */
infix fun Faktum<LocalDate>.erFørEllerLik(other: Faktum<LocalDate>) =
    ParSubsumsjon(FØR_ELLER_LIK, this, other) { this.verdi <= other.verdi }

infix fun Faktum<LocalDate>.erFør(other: Faktum<LocalDate>) =
    ParSubsumsjon(FØR, this, other) { verdi < other.verdi }

infix fun Faktum<LocalDate>.erFør(other: LocalDate) =
    ParSubsumsjon(FØR, this, Faktum(other)) { verdi < other }

infix fun Faktum<LocalDate>.erEtterEllerLik(other: Faktum<LocalDate>) =
    ParSubsumsjon(ETTER_ELLER_LIK, this, other) { verdi >= other.verdi }

infix fun Faktum<LocalDate>.erEtter(other: Faktum<LocalDate>) =
    ParSubsumsjon(ETTER, this, other) { verdi > other.verdi }

/**
 * Tall
 */
infix fun Faktum<out Number>.erMindreEllerLik(other: Faktum<out Number>) =
    ParSubsumsjon(MINDRE_ELLER_LIK, this, other) { verdi.toDouble() <= other.verdi.toDouble() }

infix fun Faktum<out Number>.erMindreEnn(other: Faktum<out Number>) =
    ParSubsumsjon(    MINDRE, this, other) { verdi.toDouble() < other.verdi.toDouble() }

infix fun Faktum<out Number>.erStørreEllerLik(other: Faktum<out Number>) =
    ParSubsumsjon(    STØRRE_ELLER_LIK, this, other) { verdi.toDouble() >= other.verdi.toDouble() }

infix fun Faktum<out Number>.erStørre(other: Faktum<out Number>) =
    ParSubsumsjon(    STØRRE, this, other) { verdi.toDouble() > other.verdi.toDouble() }

infix fun Faktum<out Number>.erMindreEllerLik(other: Number) =
    ParSubsumsjon(    MINDRE_ELLER_LIK, this, Faktum(other)) { verdi.toDouble() <= other.toDouble() }

infix fun Faktum<out Number>.erMindreEnn(other: Number) =
    ParSubsumsjon(    MINDRE, this, Faktum(other)) { verdi.toDouble() < other.toDouble() }

infix fun Faktum<out Number>.erStørreEllerLik(other: Number) =
    ParSubsumsjon(    STØRRE_ELLER_LIK, this, Faktum(other)) { verdi.toDouble() >= other.toDouble() }

infix fun Faktum<out Number>.erStørre(other: Number) =
    ParSubsumsjon(    STØRRE, this, Faktum(other)) { verdi.toDouble() > other.toDouble() }

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
 * Faktum
 */
infix fun <T : Any> Faktum<T>.erLik(ap: T) = ParSubsumsjon(
    LIK, this, Faktum(ap)
) { this.verdi == ap }

infix fun <T : Any> Faktum<T>.erLik(ap: Faktum<T>) = ParSubsumsjon(
    LIK, this, ap
) { this.verdi == ap.verdi }

infix fun <T : Any> Faktum<T>.erUlik(ap: T) = ParSubsumsjon(
    ULIK, this, Faktum(ap)
) { this.verdi != ap }

infix fun <T : Any> Faktum<T>.erUlik(ap: Faktum<T>) = ParSubsumsjon(
    ULIK, this, ap
) { this.verdi != ap.verdi }

infix fun <T : Any> Faktum<T>.erBlant(others: List<T>) = MengdeSubsumsjon(
    ER_BLANDT,
    Faktum(navn = this.navn, verdi = this.verdi.toString()),
    others.map { Faktum(it) }
) { this.verdi in others }

infix fun <T : Any> Faktum<T>.erIkkeBlant(others: List<T>) = MengdeSubsumsjon(
    ER_IKKE_BLANDT,
    Faktum(navn = this.navn, verdi = this.verdi.toString()),
    others.map { Faktum(it) }
) { this.verdi !in others }