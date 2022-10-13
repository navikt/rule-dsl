package no.nav.system.rule.dsl.demo.ruleset

import no.nav.system.rule.dsl.AbstractRuleset
import no.nav.system.rule.dsl.DslDomainPredicate
import no.nav.system.rule.dsl.demo.domain.*
import no.nav.system.rule.dsl.demo.domain.koder.*
import no.nav.system.rule.dsl.demo.domain.koder.UnntakEnum.*
import no.nav.system.rule.dsl.demo.domain.koder.YtelseEnum.*
import no.nav.system.rule.dsl.demo.helper.måneder
import no.nav.system.rule.dsl.demo.helper.år
import no.nav.system.rule.dsl.rettsregel.*
import java.time.LocalDate
import java.util.*

/**
 * Hvis unntak fra forutgående medlemsskap er angitt i inngang og eksportgrunnlag og unntaktype er
 * flyktning så har personen status som flyktning.
 */
@OptIn(DslDomainPredicate::class)
class PersonenErFlyktningRS(
    private val innPersongrunnlag: Person,
    private val innYtelseType: Faktum<YtelseEnum>,
    private val innKapittel20: Faktum<Boolean>,
    private val innVirk: Faktum<Date>
) : AbstractRuleset<Boolean>() {

    private var erFlyktning: Boolean = false
    private var overgangsregel: Boolean = false
    private var dato67m: LocalDate = innPersongrunnlag.fødselsdato.withDayOfMonth(1) + 67.år + 1.måneder
    private val unntakFraForutgaendeMedlemskap =
        innPersongrunnlag.inngangOgEksportgrunnlag?.unntakFraForutgaendeMedlemskap
    private val unntakFraForutgaendeTT = innPersongrunnlag.inngangOgEksportgrunnlag?.unntakFraForutgaendeTT
    val aktuelleUnntakstyper = listOf(
        FLYKT_ALDER,
        FLYKT_BARNEP,
        FLYKT_GJENLEV,
        FLYKT_UFOREP
    )
//    var datoClone = innFødselsdato.clone() as Date
//    datoClone += 67.år
//    datoClone += 1.måneder
//    date(datoClone.år, datoClone.måned.value, 1)

    override fun create() {


        rettsregel("HarUnntakFraForutgaendeMedlemskapTypeFlyktning") {
            HVIS { unntakFraForutgaendeMedlemskap != null }
            OG { unntakFraForutgaendeMedlemskap!!.unntak.erSann() }
            OG { unntakFraForutgaendeMedlemskap?.unntakType != null }
            OG { unntakFraForutgaendeMedlemskap!!.unntakType erBlant aktuelleUnntakstyper }
            SÅ {
                erFlyktning = true
            }
            kommentar(
                """Hvis unntak fra forutgående medlemsskap er angitt i inngang og eksportgrunnlag
            og unntaktype er flyktning så har personen status som flyktning."""
            )
        }
        rettsregel("HarUnntakFraForutgaendeTTTypeFlyktning") {
            HVIS { unntakFraForutgaendeTT != null }
            OG { unntakFraForutgaendeTT!!.unntak.erSann() }
            OG { unntakFraForutgaendeTT?.unntakType != null }
            OG { unntakFraForutgaendeTT!!.unntakType erBlant aktuelleUnntakstyper }
            SÅ {
                erFlyktning = true
            }
            kommentar(
                """Hvis unntak fra forutgående trygdetid er angitt i inngang og eksportgrunnlag og
            unntaktype er flyktning så har personen status som flyktning."""
            )
        }
        rettsregel("HarFlyktningFlaggetSatt") {
            HVIS { innPersongrunnlag.flyktning.erSann() }
            SÅ {
                erFlyktning = true
            }
            kommentar("")
        }

        rettsregel("Overgangsregel_APk19") {
            HVIS { innYtelseType erLik AP }
            OG { innPersongrunnlag.fødselsdato erMindreEnn 1959 }
            OG { innKapittel20.erUsann() }
            OG { innPersongrunnlag.trygdetidK19?.tt_fa_F2021 erStørreEllerLik 20 }
            SÅ {
                overgangsregel = true
            }
            kommentar("")
        }
//        rettsregel("Overgangsregel_APk20") {
//            HVIS { innYtelseType erLik YtelseEnum.AP }
//            OG { innPersongrunnlag.fødselsdato erMindreEllerLik 1959 }
//            OG { innKapittel20.erSann() }
//            OG { innPersongrunnlag.trygdetidK20?.tt_fa_F2021 erStørreEllerLik 20 }
//            SÅ {
//                overgangsregel = true
//            }
//            kommentar("")
//        }
//        rettsregel("Overgangsregel_APk19_tidligereUT") {
//            HVIS { innYtelseType erLik YtelseEnum.AP }
//            OG { innPersongrunnlag.fødselsdato erMindreEllerLik 1959 }
//            OG { innVirk erEtterEllerLik dato67m }
//            OG { innKapittel20.erUsann() }
//            OG { innPersongrunnlag.trygdetidK19?.tt_fa_F2021 erStørreEllerLik 20 }
//            OG {
//                innPersongrunnlag.forsteVirkningsdatoGrunnlagListe!!.minst(1) {
//                    it.kravlinjeTypeUnntakEnum == YtelseEnum.UT
//                            && it.virkningsdato?!! < localDate(2021, 1, 1)
//                }
//            }
//            SÅ {
//                overgangsregel = true
//            }
//            kommentar("")
//        }
//        rettsregel("Overgangsregel_APk20_tidligereUT") {
//            HVIS { innYtelseType erLik YtelseEnum.AP }
//            OG { innPersongrunnlag.fødselsdato erMindreEllerLik 1959 }
//            OG { innVirk erEtterEllerLik dato67m }
//            OG { innKapittel20 }
//            OG { innPersongrunnlag.trygdetidK20?.tt_fa_F2021? erStørreEllerLik 20 }
//            OG {
//                innPersongrunnlag.forsteVirkningsdatoGrunnlagListe!!.minst(1) {
//                    it.kravlinjeTypeUnntakEnum == YtelseEnum.UT
//                            && it.virkningsdato!! < localDate(2021, 1, 1)
//                }
//            }
//            SÅ {
//                overgangsregel = true
//            }
//            kommentar("")
//        }
//        rettsregel("Overgangsregel_GJRk19_tidligereUT_GJT") {
//            HVIS { innYtelseType erLik YtelseEnum.GJR }
//            OG { innPersongrunnlag.fødselsdato erMindreEllerLik 1959 }
//            OG { innVirk størreEllerLik dato67m }
//            OG { innKapittel20.erUsann() }
//            OG { innPersongrunnlag.trygdetidK19?.tt_fa_F2021 erStørreEllerLik 20 }
//            OG {
//                innPersongrunnlag.forsteVirkningsdatoGrunnlagListe!!.minst(1) {
//                    it.kravlinjeTypeUnntakEnum == YtelseEnum.UT_GJR
//                            && it.virkningsdato!! < localDate(2021, 1, 1)
//                }
//            }
//            SÅ {
//                overgangsregel = true
//            }
//            kommentar("")
//        }
//        rettsregel("Overgangsregel_GJRk20_tidligereUT_GJT") {
//            HVIS { innYtelseType erLik YtelseEnum.GJR }
//            OG { innPersongrunnlag.fødselsdato.year erMindreEllerLik 1959 }
//            OG { innVirk erEtterEllerLik dato67m }
//            OG { innKapittel20.erSann() }
//            OG { innPersongrunnlag.trygdetidK20?.tt_fa_F2021 erStørreEllerLik 20 }
//            OG {
//                innPersongrunnlag.forsteVirkningsdatoGrunnlagListe!!.minst(1) {
//                    it.kravlinjeTypeUnntakEnum == YtelseEnum.UT_GJR
//                            && it.virkningsdato!! < localDate(2021, 1, 1)
//                }
//            }
//            SÅ {
//                overgangsregel = true
//            }
//            kommentar("")
//        }
//        rettsregel("Overgangsregel_APk19_tidligereGJP") {
//            HVIS { innYtelseType erLik YtelseEnum.AP }
//            OG { innPersongrunnlag.fødselsdato erMindreEllerLik 1959 }
//            OG { innVirk erEtterEllerLik dato67m }
//            OG { innKapittel20.erUsann() }
//            OG { innPersongrunnlag.trygdetidK19?.tt_fa_F2021 erStørreEllerLik 20 }
//            OG {
//                innPersongrunnlag.forsteVirkningsdatoGrunnlagListe!!.minst(1) {
//                    it.kravlinjeTypeUnntakEnum == YtelseEnum.GJP
//                            && it.virkningsdato!! < localDate(2021, 1, 1)
//                }
//            }
//            SÅ {
//                overgangsregel = true
//            }
//            kommentar("")
//        }
//        rettsregel("Overgangsregel_APk20_tidligereGJP") {
//            HVIS { innYtelseType erLik YtelseEnum.AP }
//            OG { innPersongrunnlag.fødselsdato erMindreEllerLik 1959 }
//            OG { innVirk erEtterEllerLik dato67m }
//            OG { innKapittel20.erSann() }
//            OG { innPersongrunnlag.trygdetidK20?.tt_fa_F2021 erStørreEllerLik 20 }
//            OG {
//                innPersongrunnlag.forsteVirkningsdatoGrunnlagListe!!.minst(1) {
//                    it.kravlinjeTypeUnntakEnum == YtelseEnum.GJP
//                            && it.virkningsdato!! < localDate(2021, 1, 1)
//                }
//            }
//            SÅ {
//                overgangsregel = true
//            }
//            kommentar("")
//        }
//        rettsregel("Overgangsregel_GJRk19_tidligereGJR") {
//            HVIS { innYtelseType erLik YtelseEnum.GJR }
//            OG { innPersongrunnlag.fødselsdato erMindreEllerLik 1959 }
//            OG { innVirk erEtterEllerLik dato67m }
//            OG { innKapittel20.erUsann() }
//            OG { innPersongrunnlag.trygdetidK19?.tt_fa_F2021 erStørreEllerLik 20 }
//            OG {
//                innPersongrunnlag.forsteVirkningsdatoGrunnlagListe!!.minst(1) {
//                    it.kravlinjeTypeUnntakEnum == YtelseEnum.GJR
//                            && it.virkningsdato!! < localDate(2021, 1, 1)
//                }
//            }
//            SÅ {
//                overgangsregel = true
//            }
//            kommentar("")
//        }
//        rettsregel("Overgangsregel_GJRk20_tidligereGJR") {
//            HVIS { innYtelseType erLik YtelseEnum.GJR }
//            OG { innPersongrunnlag.fødselsdato erMindreEllerLik 1959 }
//            OG { innVirk størreEllerLik dato67m }
//            OG { innKapittel20.erSann() }
//            OG { innPersongrunnlag.trygdetidK20?.tt_fa_F2021 erStørreEllerLik 20 }
//            OG {
//                innPersongrunnlag.forsteVirkningsdatoGrunnlagListe!!.minst(1) {
//                    it.kravlinjeTypeUnntakEnum == YtelseEnum.GJR
//                            && it.virkningsdato!! < localDate(2021, 1, 1)
//                }
//            }
//            SÅ {
//                overgangsregel = true
//            }
//        }
//        rettsregel("Unntak_kravlinjeFremsattDatoForSent") {
//            HVIS { harKravlinjeFremsattDatoFom2021.erSann() }
//            OG { overgangsregel.erUsann() }
//            SÅ {
//                erFlyktning = false
//            }
//        }
//        regel("ReturRegel") {
//            HVIS { true }
//            SÅ {
//                RETURNER(erFlyktning)
//            }
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
}
