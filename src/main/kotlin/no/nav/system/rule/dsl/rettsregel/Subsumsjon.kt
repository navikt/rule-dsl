package no.nav.system.rule.dsl.rettsregel

import no.nav.system.rule.dsl.Predicate
import no.nav.system.rule.dsl.rettsregel.KOMPARATOR.*
import svarord
import java.time.LocalDate

/**
 * TODO Vurder om en subsumsjon (og Predicate) burde ha en evaluate variabel slik sonm Rule. Hensikten er å oppdage bruk av Sumsumsjoner som ikke har blitt evaluert enda (feilsituasjon)
 */
class Subsumsjon(
    val komparator: KOMPARATOR,
    val pair: Pair<Faktum<*>, Faktum<*>>? = null,
    val utfallFunksjon: () -> Boolean
) : Predicate(function = utfallFunksjon) {

    /**
     * Evaluates the predicate function.
     *
     * @return returns true if further evaluation of remaining predicates in the rule should be prevented.
     */
    override fun evaluate(): Boolean {
        parent!!.children.add(this)
        fired = utfallFunksjon.invoke()
        return false
    }

    override fun type(): String = "Subsumsjon"

    override fun toString(): String {
        val komparatorText = if (fired) komparator.text else komparator.negated()
        return "${fired.svarord()}: ${pair?.first ?: ""}$komparatorText${pair?.second ?: ""}"
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

//    fun Faktum<out SuperEnum>.erBlandt(others: List<SuperEnum>) = Subsumsjon(
//        ER_BLANDT,
//        Pair(this, Faktum(others.map { it.navn() }.joinToString(", ")))
//    ) { others.any { this.verdi.navn() == it.navn() } }


//    fun erBlandtX(others: List<SuperEnum>) = Subsumsjon(
//        ER_BLANDT,
//        Pair(this, Faktum(others.map { it.navn() }.joinToString(", ")))
//    ) { others.any { this.verdi == it.navn() } }

//    fun erBlandt(vararg others: SuperEnum) = Subsumsjon(
//        ER_BLANDT,
//        Pair(this, Faktum(others.map { it.navn() }.joinToString(", ")))
//    ) { others.any { this.verdi == it.navn() } }

    infix fun String.erStringen(s: String): String {
        return ""
    }
}

interface SuperEnum {
    fun faktum(): Faktum<*>
    fun navn(): String
}

enum class UtfallType {
    OPPFYLT,
    IKKE_OPPFYLT,
    IKKE_RELEVANT;

    fun motsatt(): UtfallType {
        return when (this) {
            OPPFYLT -> IKKE_OPPFYLT
            IKKE_OPPFYLT -> OPPFYLT
            IKKE_RELEVANT -> IKKE_RELEVANT
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
    ULIK(" er ulik "),
    ER_BLANDT(" er blandt "),
    ER_IKKE_BLANDT(" er ikke blandt "),
    ALLE(" alle "),
    INGEN(" ingen ");

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
            ER_BLANDT -> " må være blandt "
            ER_IKKE_BLANDT -> " må ikke være blandt "
            ALLE -> " ingen "
            INGEN -> " alle "
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
    Subsumsjon(ULIK, Pair(this, Faktum("USANN", false))) { verdi }

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

enum class MinEnum : SuperEnum {
    A, B;

    override fun navn(): String = this.name

    override fun faktum(): Faktum<MinEnum> {
        return when (this) {
            A -> Faktum("a", this)
            B -> Faktum("b", this)
        }
    }
}


fun Faktum<SuperEnum>.hallo() = "hallo"


fun main() {
    val kode = Faktum(MinEnum.A)
    val listOfSuperEnum = listOf(MinEnum.A, MinEnum.B)
//    val x = kode erBlandtString "asd"


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



