package no.nav.system.rule.dsl.rettsregel

import no.nav.system.rule.dsl.Predicate
import no.nav.system.rule.dsl.rettsregel.KOMPARATOR.*
import no.nav.system.rule.dsl.treevisitor.svarord
import java.time.LocalDate

class Subsumsjon(
    val komparator: KOMPARATOR,
    val pair: Pair<Faktum<*>, Faktum<*>>,
    val utfallFunksjon: () -> Boolean
) : Predicate(function = utfallFunksjon) {

    /**
     * Evaluates the predicate function.
     *
     * @return returns true if further evaluation of remaining predicates in the rule should be prevented.
     */
    override fun evaluate(): Boolean {
        fired = utfallFunksjon.invoke()
        return false
    }

    override fun toString(): String {
        val komparatorText = if (fired) komparator.text else komparator.negated()
        return "${fired.svarord()}: ${pair.first}$komparatorText${pair.second}"
    }
}


data class Kilde(val paragraf: String, val bokstav: String)

class Faktum<T>(val navn: String, var verdi: T) {
    var anonymous = false

    constructor(verdi: T) : this(verdi.toString(), verdi) {
        anonymous = true
    }

    override fun toString(): String {
        return if (anonymous) {
            "'$navn'"
        } else {
            "'$navn' ($verdi)"
        }
    }
}

enum class KOMPARATOR(val text: String) {
    FØR_ELLER_LIK(" er tom "),
    FØR(" er før "),
    ETTER_ELLER_LIK(" er fom "),
    ETTER(" er etter "),
    MINDRE_ELLER_LIK(" er mindre eller lik "),
    MINDRE(" er mindre enn "),
    STØRRE_ELLER_LIK(" er større eller lik "),
    STØRRE(" er større enn "),
    LIK(" er lik "),
    ULIK(" er ulik ");

    fun negated(): String {
       return when (this) {
            FØR_ELLER_LIK -> " må være tom "
            FØR -> " må være før "
            ETTER_ELLER_LIK -> " må være fom "
            ETTER -> " må være etter "
            MINDRE_ELLER_LIK -> " må være mindre eller lik"
            MINDRE -> " må være mindre enn "
            STØRRE_ELLER_LIK -> " må være større eller lik "
            STØRRE -> " må være større enn "
            LIK -> " må være lik "
            ULIK -> " må være ulik "
        }
    }
}

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

/**
 * Boolean
 */
fun Faktum<Boolean>.erSann() =
    Subsumsjon(LIK, Pair(this, Faktum("SANN", true))) { verdi }

fun Faktum<Boolean>.erUsann() =
    Subsumsjon(ULIK, Pair(this, Faktum("USANN", false))) { verdi }

fun main() {
    val f1i = Faktum("G", 2)
    val f2i = Faktum("SATS", 3)
    val subsumi: Subsumsjon = f1i erMindreEnn f2i

    val f1d = Faktum("G", 2.2)
    val f2d = Faktum("SATS", 3.3)
    val subsumd: Subsumsjon = f1i erMindreEnn f2i

    val f1dato = Faktum("67m", LocalDate.of(2020, 5, 1))
    val f2dato = Faktum("skjæringstidspunktet", LocalDate.of(2030, 1, 1))
    val subsumdato: Subsumsjon = f1dato erFør f2dato

    val f2b = Faktum("Flagg", true)
    val subsumb: Subsumsjon = f2b.erUsann()
}

