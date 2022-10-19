package no.nav.system.rule.dsl.rettsregel

import no.nav.system.rule.dsl.enums.Komparator.*
import java.time.LocalDate

/**
 * Datoer
 */
infix fun Faktum<LocalDate>.erFørEllerLik(other: Faktum<LocalDate>) =
    Subsumsjon(FØR_ELLER_LIK, Pair(this, other)) { verdi <= other.verdi }

infix fun Faktum<LocalDate>.erFør(other: Faktum<LocalDate>) =
    Subsumsjon(FØR, Pair(this, other)) { verdi < other.verdi }

infix fun Faktum<LocalDate>.erEtterEllerLik(other: Faktum<LocalDate>) =
    Subsumsjon(ETTER_ELLER_LIK, Pair(this, other)) { verdi >= other.verdi }

infix fun Faktum<LocalDate>.erEtter(other: Faktum<LocalDate>) =
    Subsumsjon(ETTER, Pair(this, other)) { verdi > other.verdi }

/**
 * Tall
 */
infix fun Faktum<out Number>.erMindreEllerLik(other: Faktum<out Number>) =
    Subsumsjon(
        MINDRE_ELLER_LIK,
        Pair(this, other)
    ) { verdi.toDouble() <= other.verdi.toDouble() }

infix fun Faktum<out Number>.erMindreEnn(other: Faktum<out Number>) =
    Subsumsjon(
        MINDRE,
        Pair(this, other)
    ) { verdi.toDouble() < other.verdi.toDouble() }

infix fun Faktum<out Number>.erStørreEllerLik(other: Faktum<out Number>) =
    Subsumsjon(
        STØRRE_ELLER_LIK,
        Pair(this, other)
    ) { verdi.toDouble() >= other.verdi.toDouble() }

infix fun Faktum<out Number>.erStørre(other: Faktum<out Number>) = Subsumsjon(
    STØRRE,
    Pair(this, other)
) { verdi.toDouble() > other.verdi.toDouble() }

infix fun Faktum<out Number>.erMindreEllerLik(other: Number) =
    Subsumsjon(
        MINDRE_ELLER_LIK,
        Pair(this, Faktum(other))
    ) { verdi.toDouble() <= other.toDouble() }

infix fun Faktum<out Number>.erMindreEnn(other: Number) =
    Subsumsjon(
        MINDRE,
        Pair(this, Faktum(other))
    ) { verdi.toDouble() < other.toDouble() }

infix fun Faktum<out Number>.erStørreEllerLik(other: Number) =
    Subsumsjon(
        STØRRE_ELLER_LIK,
        Pair(this, Faktum(other))
    ) { verdi.toDouble() >= other.toDouble() }

infix fun Faktum<out Number>.erStørre(other: Number) = Subsumsjon(
    STØRRE,
    Pair(this, Faktum(other))
) { verdi.toDouble() > other.toDouble() }


/**
 * Boolean
 * TODO Burde ikke trenge disse. Faktum/regler (generellt alle RuleComponents) bør kunne brukes fritt som regelbetingelser. Vurder å legge inn Faktum.not override.
 */
fun Faktum<Boolean>.erSann() =
    Subsumsjon(LIK, Pair(this, Faktum("SANN", true))) { verdi }

fun Faktum<Boolean>.erUsann() =
    Subsumsjon(LIK, Pair(this, Faktum("USANN", false))) { !verdi }

operator fun Faktum<Boolean>.not(): Faktum<Boolean> = Faktum(this.navn, !this.verdi)

/**
 * Dato > Tall
 */
infix fun Faktum<out LocalDate>.erMindreEllerLik(other: Int) =
    Subsumsjon(
        MINDRE_ELLER_LIK,
        Pair(this, Faktum(other))
    ) { verdi.year <= other }

infix fun Faktum<out LocalDate>.erMindreEnn(other: Int) =
    Subsumsjon(
        MINDRE,
        Pair(this, Faktum(other))
    ) { verdi.year < other }

infix fun Faktum<out LocalDate>.erStørreEllerLik(other: Int) =
    Subsumsjon(
        STØRRE_ELLER_LIK,
        Pair(this, Faktum(other))
    ) { verdi.year >= other }

infix fun Faktum<out LocalDate>.erStørreEnn(other: Int) =
    Subsumsjon(
        STØRRE,
        Pair(this, Faktum(other))
    ) { verdi.year > other }

/**
 * Generisk
 */
infix fun <T> Faktum<T>.erLik(ap: T) = Subsumsjon(
    LIK,
    Pair(this, Faktum(ap))
) { this.verdi == ap }

infix fun <T> Faktum<T>.erLik(ap: Faktum<T>) = Subsumsjon(
    LIK,
    Pair(this, ap)
) { this.verdi == ap.verdi }


infix fun <T> Faktum<T>.erBlant(others: List<T>) = Subsumsjon(
    ER_BLANDT,
    Pair(this, Faktum(others.joinToString(", ")))
) { this.verdi in others }