package no.nav.system.rule.dsl.demo.ruleset

import no.nav.system.rule.dsl.*
import no.nav.system.rule.dsl.demo.domain.Person
import no.nav.system.rule.dsl.demo.domain.koder.UnntakEnum.*
import no.nav.system.rule.dsl.demo.domain.koder.YtelseEnum
import no.nav.system.rule.dsl.demo.domain.koder.YtelseEnum.*
import no.nav.system.rule.dsl.demo.helper.måneder
import no.nav.system.rule.dsl.demo.helper.år
import no.nav.system.rule.dsl.rettsregel.*
import no.nav.system.rule.dsl.rettsregel.UtfallType.*
import java.time.LocalDate

/**
 * Hvis unntak fra forutgående medlemsskap er angitt i inngang og eksportgrunnlag og unntaktype er
 * flyktning så har personen status som flyktning.
 */
@OptIn(DslDomainPredicate::class)
class PersonenErFlyktningRS(
    private val innPersongrunnlag: Person,
    private val innYtelseType: Faktum<YtelseEnum>,
    private val innKapittel20: Faktum<Boolean>,
    private val innVirk: Faktum<LocalDate>,
    private val innKravlinjeFremsattDatoFom2021: Faktum<Boolean>,
) : AbstractRuleset<Utfall>() {
    private var dato67m: Faktum<LocalDate> =
        Faktum("Fødselsdato67m", innPersongrunnlag.fødselsdato.verdi.withDayOfMonth(1) + 67.år + 1.måneder)
    private val unntakFraForutgaendeMedlemskap =
        innPersongrunnlag.inngangOgEksportgrunnlag?.unntakFraForutgaendeMedlemskap
    private val unntakFraForutgaendeTT = innPersongrunnlag.inngangOgEksportgrunnlag?.unntakFraForutgaendeTT
    private val aktuelleUnntakstyper = listOf(
        FLYKT_ALDER, FLYKT_BARNEP, FLYKT_GJENLEV, FLYKT_UFOREP
    )
    private val svar = TomtUtfall()

    val SANN = Faktum("SANN", true)

    override fun create() {
        regel("AngittFlyktning_HarFlyktningFlaggetSatt") {
            OG { innPersongrunnlag.flyktning.erSann() }
            kommentar("Flyktningerflagget er angitt av saksbehandler.")
        }
        regel("AngittFlyktning_HarUnntakFraForutgaendeMedlemskapTypeFlyktning") {
            HVIS { unntakFraForutgaendeMedlemskap != null }
            OG { unntakFraForutgaendeMedlemskap!!.unntak erLik SANN }
            OG { unntakFraForutgaendeMedlemskap?.unntakType != null }
            OG { unntakFraForutgaendeMedlemskap!!.unntakType erBlant aktuelleUnntakstyper }
            kommentar("")
        }
        regel("AngittFlyktning_HarUnntakFraForutgaendeTTTypeFlyktning") {
            HVIS { unntakFraForutgaendeTT != null }
            OG { unntakFraForutgaendeTT!!.unntak.erSann() }
            OG { unntakFraForutgaendeTT?.unntakType != null }
            OG { unntakFraForutgaendeTT!!.unntakType erBlant aktuelleUnntakstyper }
            kommentar("")
        }
        regel("Konklusjon: ikkeAngittFlyktning") {
            OG { "AngittFlyktning_".ingenHarTruffet() }
            SVAR(IKKE_RELEVANT) { svar }
            RETURNER(svar)
        }
//        regel("Overgangsregel_APk19") {
//            HVIS { innYtelseType erLik AP }
//            OG { innPersongrunnlag.fødselsdato erMindreEnn 1959 }
//            OG { innKapittel20.erUsann() }
//            OG { innPersongrunnlag.trygdetidK19.tt_fa_F2021 erStørreEllerLik 20 }
//            kommentar("")
//        }
//        regel("Overgangsregel_APk20") {
//            HVIS { innYtelseType erLik AP }
//            OG { innPersongrunnlag.fødselsdato erMindreEllerLik 1959 }
//            OG { innKapittel20.erSann() }
//            OG { innPersongrunnlag.trygdetidK20?.tt_fa_F2021!! erStørreEllerLik 20 }
//            kommentar("")
//        }
//
//        // TODO Kom tibake til denne senere
//        fun <T> Iterable<T>.minst(target: Int, quantifier: (T) -> Boolean): Subsumsjon {
//            return Subsumsjon(
//                STØRRE_ELLER_LIK, Pair(Faktum(this), Faktum(target))
//            ) { this.count(quantifier) >= target }
//        }
//        regel("Overgangsregel_APk19_tidligereUT") {
//            HVIS { innYtelseType erLik AP }
//            OG { innPersongrunnlag.fødselsdato erMindreEllerLik 1959 }
//            OG { innVirk erEtterEllerLik dato67m }
//            OG { innKapittel20.erUsann() }
//            OG { innPersongrunnlag.trygdetidK19.tt_fa_F2021 erStørreEllerLik 20 }
//            OG {
//                innPersongrunnlag.forsteVirkningsdatoGrunnlagListe.minst(1) {
//                    it.kravlinjeType == UT && it.virkningsdato < localDate(2021, 1, 1)
//                }
//            }
//            kommentar("")
//        }
//        regel("Overgangsregel_APk20_tidligereUT") {
//            HVIS { innYtelseType erLik AP }
//            OG { innPersongrunnlag.fødselsdato erMindreEllerLik 1959 }
//            OG { innVirk erEtterEllerLik dato67m }
//            OG { innKapittel20.erSann() }
//            OG { innPersongrunnlag.trygdetidK20!!.tt_fa_F2021 erStørreEllerLik 20 }
//            OG {
//                innPersongrunnlag.forsteVirkningsdatoGrunnlagListe.minst(1) {
//                    it.kravlinjeType == UT && it.virkningsdato < localDate(2021, 1, 1)
//                }
//            }
//            kommentar("")
//        }
//        regel("Overgangsregel_GJRk19_tidligereUT_GJT") {
//            HVIS { innYtelseType erLik GJR }
//            OG { innPersongrunnlag.fødselsdato erMindreEllerLik 1959 }
//            OG { innVirk erEtterEllerLik dato67m }
//            OG { innKapittel20.erUsann() }
//            OG { innPersongrunnlag.trygdetidK19.tt_fa_F2021 erStørreEllerLik 20 }
//            OG {
//                innPersongrunnlag.forsteVirkningsdatoGrunnlagListe.minst(1) {
//                    it.kravlinjeType == UT_GJR && it.virkningsdato < localDate(2021, 1, 1)
//                }
//            }
//            kommentar("")
//        }
//        regel("Overgangsregel_GJRk20_tidligereUT_GJT") {
//            HVIS { innYtelseType erLik GJR }
//            OG { innPersongrunnlag.fødselsdato erMindreEllerLik 1959 }
//            OG { innVirk erEtterEllerLik dato67m }
//            OG { innKapittel20.erSann() }
//            OG { innPersongrunnlag.trygdetidK20!!.tt_fa_F2021 erStørreEllerLik 20 }
//            OG {
//                innPersongrunnlag.forsteVirkningsdatoGrunnlagListe.minst(1) {
//                    it.kravlinjeType == UT_GJR && it.virkningsdato < localDate(2021, 1, 1)
//                }
//            }
//            kommentar("")
//        }
//        regel("Overgangsregel_APk19_tidligereGJP") {
//            HVIS { innYtelseType erLik AP }
//            OG { innPersongrunnlag.fødselsdato erMindreEllerLik 1959 }
//            OG { innVirk erEtterEllerLik dato67m }
//            OG { innKapittel20.erUsann() }
//            OG { innPersongrunnlag.trygdetidK19.tt_fa_F2021 erStørreEllerLik 20 }
//            OG {
//                innPersongrunnlag.forsteVirkningsdatoGrunnlagListe.minst(1) {
//                    it.kravlinjeType == GJP && it.virkningsdato < localDate(2021, 1, 1)
//                }
//            }
//            kommentar("")
//        }
//        regel("Overgangsregel_APk20_tidligereGJP") {
//            HVIS { innYtelseType erLik AP }
//            OG { innPersongrunnlag.fødselsdato erMindreEllerLik 1959 }
//            OG { innVirk erEtterEllerLik dato67m }
//            OG { innKapittel20.erSann() }
//            OG { innPersongrunnlag.trygdetidK20!!.tt_fa_F2021 erStørreEllerLik 20 }
//            OG {
//                innPersongrunnlag.forsteVirkningsdatoGrunnlagListe.minst(1) {
//                    it.kravlinjeType == GJP && it.virkningsdato < localDate(2021, 1, 1)
//                }
//            }
//            kommentar("")
//        }
//        regel("Overgangsregel_GJRk19_tidligereGJR") {
//            HVIS { innYtelseType erLik GJR }
//            OG { innPersongrunnlag.fødselsdato erMindreEllerLik 1959 }
//            OG { innVirk erEtterEllerLik dato67m }
//            OG { innKapittel20.erUsann() }
//            OG { innPersongrunnlag.trygdetidK19.tt_fa_F2021 erStørreEllerLik 20 }
//            OG {
//                innPersongrunnlag.forsteVirkningsdatoGrunnlagListe.minst(1) {
//                    it.kravlinjeType == GJR && it.virkningsdato < localDate(2021, 1, 1)
//                }
//            }
//            kommentar("")
//        }
//        regel("Overgangsregel_GJRk20_tidligereGJR") {
//            HVIS { innYtelseType erLik GJR }
//            OG { innPersongrunnlag.fødselsdato erMindreEllerLik 1959 }
//            OG { innVirk erEtterEllerLik dato67m }
//            OG { innKapittel20.erSann() }
//            OG { innPersongrunnlag.trygdetidK20!!.tt_fa_F2021 erStørreEllerLik 20 }
//            OG {
//                innPersongrunnlag.forsteVirkningsdatoGrunnlagListe.minst(1) {
//                    it.kravlinjeType == GJR && it.virkningsdato < localDate(2021, 1, 1)
//                }
//            }
//        }

//        regel("AnvendtFlyktning_unntak") {
//            HVIS { "AngittFlyktning_".minstEnHarTruffet() }
//            OG { innKravlinjeFremsattDatoFom2021.erSann() }
//            OG { "Overgangsregel_".ingenHarTruffet() }
//            SÅ {
//                val fak = !innKapittel20
//            }
//            RETURNER(this)
//        }
//
//        regel("AnvendtFlyktning") {
//            HVIS { innKravlinjeFremsattDatoFom2021.erUsann() }
//            OG { "AngittFlyktning_".minstEnHarTruffet() }
//            RETURNER(this)
//        }
    }
}

fun main() {
    val ytelseFakta = Faktum("Ytelsetypen", AP)

    val x2: Subsumsjon = ytelseFakta erLik AP
    x2.evaluate()
    println(x2)

    val bb2: Subsumsjon = ytelseFakta erBlant listOf(AFP, UT_GJR, AP)
    bb2.evaluate()
    println(bb2)

    val tomregel = 20
    calc(tomregel)
    println(tomregel)


}


fun calc(x: Int) {
    var xIntern = x
    xIntern = 2000
}
