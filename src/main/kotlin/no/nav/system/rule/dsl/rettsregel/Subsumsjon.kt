package no.nav.system.rule.dsl.rettsregel

import no.nav.system.rule.dsl.rettsregel.KOMPARATOR.*
import java.time.LocalDate

class Subsumsjon(
    val komparator: KOMPARATOR,
    val pair: Pair<Faktum<*>, Faktum<*>>,
    val svar: () -> Boolean,
    val tekst: String
) {

    //    val pair = Pair<Faktum<*>, Faktum<*>>()
    override fun toString(): String {
        return tekst
    }
}


data class Kilde(val paragraf: String, val bokstav: String)

class Faktum<T>(val navn: String, var verdi: T) {

    override fun toString(): String {
        return "'$navn' ($verdi)"
    }
}

enum class KOMPARATOR(val text: String) {
    LESS_OR_EQUAL(" <= "),
    LESS(" < "),
    GREATER_OR_EQUAL(" >= "),
    GREATER(" > "),
    EQUAL(" = "),
    NOT_EQUAL(" != ")
}


/**
 * Datoer
 */
infix fun Faktum<LocalDate>.erFørEllerLik(other: Faktum<LocalDate>) =
    Subsumsjon(LESS_OR_EQUAL, Pair(this, other), { verdi <= other.verdi }, "$this, er før eller lik $other")

infix fun Faktum<LocalDate>.erFør(other: Faktum<LocalDate>) =
    Subsumsjon(LESS, Pair(this, other), { verdi < other.verdi }, "$this, er før $other")

infix fun Faktum<LocalDate>.erEtterEllerLik(other: Faktum<LocalDate>) =
    Subsumsjon(GREATER_OR_EQUAL, Pair(this, other), { verdi >= other.verdi }, "$this, er etter eller lik $other")

infix fun Faktum<LocalDate>.erEtter(other: Faktum<LocalDate>) =
    Subsumsjon(GREATER, Pair(this, other), { verdi > other.verdi }, "$this, er etter $other")

/**
 * Tall
 */
infix fun Faktum<out Number>.erMindreEllerLik(other: Faktum<out Number>) =
    Subsumsjon(
        LESS_OR_EQUAL,
        Pair(this, other),
        { verdi.toDouble() <= other.verdi.toDouble() },
        "$this, er mindre eller lik $other"
    )

infix fun Faktum<out Number>.erMindreEnn(other: Faktum<out Number>) =
    Subsumsjon(
        LESS,
        Pair(this, other),
        { verdi.toDouble() < other.verdi.toDouble() },
        "$this, er mindre enn $other"
    )

infix fun Faktum<out Number>.erStørreEllerLik(other: Faktum<out Number>) =
    Subsumsjon(
        GREATER_OR_EQUAL,
        Pair(this, other),
        { verdi.toDouble() >= other.verdi.toDouble() },
        "$this, er større eller lik $other"
    )

infix fun Faktum<out Number>.erStørre(other: Faktum<out Number>) = Subsumsjon(
    GREATER,
    Pair(this, other),
    { verdi.toDouble() > other.verdi.toDouble() },
    "$this, er større enn $other"
)

/**
 * Boolean
 */
fun Faktum<Boolean>.erSann() =
    Subsumsjon(EQUAL, Pair(this, Faktum("SANN", true)), { verdi }, "$this, er SANN.")

fun Faktum<Boolean>.erUsann() =
    Subsumsjon(NOT_EQUAL, Pair(this, Faktum("USANN", false)), { verdi }, "$this, er USANN")

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

//  RETTSREGEL            regel("Skal ha redusert fremtidig trygdetid") {
//    SUBSUM                  HVIS("Virkningsdato i saken, $virkningstidspunkt, er [fom|før] $dato1991.") {
//       FAKTUM1 >= FAKTUM2       virkningstidspunkt >= dato1991
//                            }
//    SUBSUM                  OG("Faktisk trygdetid, ${svar.faktiskTrygdetidIMåneder}, er [lavere|høyere] enn fire-femtedelskravet (${svar.firefemtedelskrav}).") {
//       FAKTUM1 < FAKTUM2       svar.faktiskTrygdetidIMåneder < svar.firefemtedelskrav
//                            }
//    RETTSVIRKNING           SÅ {
//       FAKTUM<Boolean>         svar.redusertFremtidigTrygdetid = true
//                            }
//                            kommentar(
//                                """Dersom faktisk trygdetid i Norge er mindre enn 4/5 av
//                                             opptjeningstiden skal den framtidige trygdetiden være redusert."""
//                            )
//                        }

//svar.redusertFremtidigTrygdetid : Boolean
//svar.redusertFremtidigTrygdetid : Rule??

