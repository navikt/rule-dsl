package no.nav.system.rule.dsl.demo.ruleset

import no.nav.system.rule.dsl.AbstractRuleset
import no.nav.system.rule.dsl.demo.domain.*
import no.nav.system.rule.dsl.demo.domain.koder.*
import no.nav.system.rule.dsl.demo.helper.localDate
import no.nav.system.rule.dsl.rettsregel.Faktum
import java.time.LocalDate
import java.util.*

/**
 * Hvis unntak fra forutgående medlemsskap er angitt i inngang og eksportgrunnlag og unntaktype er
 * flyktning så har personen status som flyktning.
 */
class PersonenErFlyktningRS(
    //    private val inninngangOgEksportgrunnlag: inngangOgEksportgrunnlag,
//    private val innTrygdetidK19: Trygdetid,
//    private val innTrygdetidK20: Trygdetid,
//    private val innAngittFlyktning: Faktum<Boolean>,
//    private val innFødselsdato: Faktum<LocalDate>,
    private val inninngangOgEksportgrunnlag: InngangOgEksportgrunnlag,
    private val innTrygdetidK19: Trygdetid,
    private val innTrygdetidK20: Trygdetid,
    private val innPersongrunnlag: Person,
    private val innHarKravfremsattdatoFom2021: Faktum<Boolean>,
    private val innYtelseType: Faktum<YtelseEnum>,
    private val innKapittel20: Faktum<Boolean>,
    private val innVirk: Faktum<Date>,
    private val innYtelsestype: String
) : AbstractRuleset<Boolean>() {

    private var erFlyktning: Boolean = false
    private var overgangsregel: Boolean = false
    private var dato67m: LocalDate = innPersongrunnlag.fødselsdato

//    var datoClone = innFødselsdato.clone() as Date
//    datoClone += 67.år
//    datoClone += 1.måneder
//    date(datoClone.år, datoClone.måned.value, 1)

    override fun create() {

        regel("HarUnntakFraForutgaendeMedlemskapTypeFlyktning") {
            HVIS { innPersongrunnlag.inngangOgEksportgrunnlag != null }
            OG { innPersongrunnlag.inngangOgEksportgrunnlag?.unntakFraForutgaendeMedlemskap != null }
            OG { innPersongrunnlag.inngangOgEksportgrunnlag?.unntakFraForutgaendeMedlemskap?.unntak == true }
            OG { innPersongrunnlag.inngangOgEksportgrunnlag?.unntakFraForutgaendeMedlemskap?.unntakType != null }
            OG {
                innPersongrunnlag.inngangOgEksportgrunnlag?.unntakFraForutgaendeMedlemskap?.unntakType!! == UnntakEnum.FLYKT_ALDER
                        || innPersongrunnlag.inngangOgEksportgrunnlag?.unntakFraForutgaendeMedlemskap?.unntakType!! == UnntakEnum.FLYKT_BARNEP
                        || innPersongrunnlag.inngangOgEksportgrunnlag?.unntakFraForutgaendeMedlemskap?.unntakType!! == UnntakEnum.FLYKT_GJENLEV
                        || innPersongrunnlag.inngangOgEksportgrunnlag?.unntakFraForutgaendeMedlemskap?.unntakType!! == UnntakEnum.FLYKT_UFOREP
            }
            SÅ {
                erFlyktning = true
            }
            kommentar(
                """Hvis unntak fra forutgående medlemsskap er angitt i inngang og eksportgrunnlag
            og unntaktype er flyktning så har personen status som flyktning."""
            )
        }
        regel("HarUnntakFraForutgaendeTTTypeFlyktning") {
            HVIS { innPersongrunnlag.inngangOgEksportgrunnlag != null }
            OG { innPersongrunnlag.inngangOgEksportgrunnlag?.unntakFraForutgaendeTT != null }
            OG { innPersongrunnlag.inngangOgEksportgrunnlag?.unntakFraForutgaendeTT?.unntak == true }
            OG { innPersongrunnlag.inngangOgEksportgrunnlag?.unntakFraForutgaendeTT?.unntakType != null }
            OG {
                innPersongrunnlag.inngangOgEksportgrunnlag?.unntakFraForutgaendeTT?.unntakType!! == UnntakEnum.FLYKT_ALDER
                        || innPersongrunnlag.inngangOgEksportgrunnlag?.unntakFraForutgaendeTT?.unntakType!! == UnntakEnum.FLYKT_BARNEP
                        || innPersongrunnlag.inngangOgEksportgrunnlag?.unntakFraForutgaendeTT?.unntakType!! == UnntakEnum.FLYKT_GJENLEV
                        || innPersongrunnlag.inngangOgEksportgrunnlag?.unntakFraForutgaendeTT?.unntakType!! == UnntakEnum.FLYKT_UFOREP
            }
            SÅ {
                erFlyktning = true
            }
            kommentar(
                """Hvis unntak fra forutgående trygdetid er angitt i inngang og eksportgrunnlag og
            unntaktype er flyktning så har personen status som flyktning."""
            )
        }
        regel("HarFlyktningFlaggetSatt") {
            HVIS { innPersongrunnlag.flyktning }
            SÅ {
                erFlyktning = true
            }
            kommentar("")
        }

        regel("Overgangsregel_APk19") {
            HVIS { innYtelsestype == YtelseEnum.AP.toString() }
            OG { innPersongrunnlag.fødselsdato.år!! <= 1959 }
            OG { !innKapittel20 }
            OG { innPersongrunnlag.trygdetid?.tt_fa_F2021 != null }
            OG { innPersongrunnlag.trygdetid?.tt_fa_F2021?.antallAr!! >= 20 }
            SÅ {
                overgangsregel = true
            }
            kommentar("")
        }
        regel("Overgangsregel_APk20") {
            HVIS { innYtelsestype == YtelseEnum.AP.toString() }
            OG { innPersongrunnlag.fødselsdato.year <= 1959 }
            OG { innKapittel20 }
            OG { innPersongrunnlag.trygdetidKapittel20?.tt_fa_F2021 != null }
            OG { innPersongrunnlag.trygdetidKapittel20?.tt_fa_F2021?.verdi!! >= 20 }
            SÅ {
                overgangsregel = true
            }
            kommentar("")
        }
        regel("Overgangsregel_APk19_tidligereUT") {
            HVIS { innYtelsestype == YtelseEnum.AP.toString() }
            OG { innPersongrunnlag.fødselsdato.year <= 1959 }
            OG { innVirk!! >= dato67m }
            OG { !innKapittel20 }
            OG { innPersongrunnlag.trygdetid?.tt_fa_F2021 != null }
            OG { innPersongrunnlag.trygdetid?.tt_fa?.antallAr!! >= 20 }
            OG {
                innPersongrunnlag.forsteVirkningsdatoGrunnlagListe!!.minst(1) {
                    it.kravlinjeTypeUnntakEnum == YtelseEnum.UT
                            && it.virkningsdato?!! < localDate(2021, 1, 1)
                }
            }
            SÅ {
                overgangsregel = true
            }
            kommentar("")
        }
        regel("Overgangsregel_APk20_tidligereUT") {
            HVIS { innYtelsestype == YtelseEnum.AP.toString() }
            OG { innPersongrunnlag.fødselsdato.year <= 1959 }
            OG { innVirk >= dato67m }
            OG { innKapittel20 }
            OG { innPersongrunnlag.trygdetidKapittel20?.tt_fa_F2021 != null && innPersongrunnlag.trygdetidKapittel20?.tt_fa_F2021?.antallAr!! >= 20 }
            OG {
                innPersongrunnlag.forsteVirkningsdatoGrunnlagListe!!.minst(1) {
                    it.kravlinjeTypeUnntakEnum == YtelseEnum.UT
                            && it.virkningsdato!! < localDate(2021, 1, 1)
                }
            }
            SÅ {
                overgangsregel = true
            }
            kommentar("")
        }
        regel("Overgangsregel_GJRk19_tidligereUT_GJT") {
            HVIS { innYtelsestype == YtelseEnum.GJR.toString() }
            OG { innPersongrunnlag.fødselsdato.year <= 1959 }
            OG { innVirk != null }
            OG { innVirk >= dato67m }
            OG { !innKapittel20 }
            OG { innPersongrunnlag.trygdetid?.tt_fa_F2021?.antallAr != null && innPersongrunnlag.trygdetid?.tt_fa_F2021?.antallAr!! >= 20 }
            OG {
                innPersongrunnlag.forsteVirkningsdatoGrunnlagListe!!.minst(1) {
                    it.kravlinjeTypeUnntakEnum == YtelseEnum.UT_GJR
                            && it.virkningsdato!! < localDate(2021, 1, 1)
                }
            }
            SÅ {
                overgangsregel = true
            }
            kommentar("")
        }
        regel("Overgangsregel_GJRk20_tidligereUT_GJT") {
            HVIS { innYtelsestype == YtelseEnum.GJR.toString() }
            OG { innPersongrunnlag.fødselsdato.year <= 1959 }
            OG { innVirk >= dato67m }
            OG { innKapittel20 }
            OG { innPersongrunnlag.trygdetidKapittel20?.tt_fa_F2021?.antallAr != null && innPersongrunnlag.trygdetidKapittel20?.tt_fa_F2021?.antallAr!! >= 20 }
            OG {
                innPersongrunnlag.forsteVirkningsdatoGrunnlagListe!!.minst(1) {
                    it.kravlinjeTypeUnntakEnum == YtelseEnum.UT_GJR
                            && it.virkningsdato!! < localDate(2021, 1, 1)
                }
            }
            SÅ {
                log_debug("[HIT] PersonenErFlyktningRS.Overgangsregel_GJRk20_tidligereUT_GJT")
                overgangsregel = true
            }
            kommentar("")
        }
        regel("Overgangsregel_APk19_tidligereGJP") {
            HVIS { innYtelsestype == YtelseEnum.AP.toString() }
            OG { innPersongrunnlag.fødselsdato.year <= 1959 }
            OG { innVirk >= dato67m }
            OG { !innKapittel20 }
            OG { innPersongrunnlag.trygdetid?.tt_fa_F2021?.antallAr != null && innPersongrunnlag.trygdetid?.tt_fa_F2021?.antallAr!! >= 20 }
            OG {
                innPersongrunnlag.forsteVirkningsdatoGrunnlagListe!!.minst(1) {
                    it.kravlinjeTypeUnntakEnum == YtelseEnum.GJP
                            && it.virkningsdato!! < localDate(2021, 1, 1)
                }
            }
            SÅ {
                overgangsregel = true
            }
            kommentar("")
        }
        regel("Overgangsregel_APk20_tidligereGJP") {
            HVIS { innYtelsestype == YtelseEnum.AP.toString() }
            OG { innPersongrunnlag.fødselsdato.year <= 1959 }
            OG { innVirk >= dato67m }
            OG { innKapittel20 }
            OG { innPersongrunnlag.trygdetidKapittel20?.tt_fa_F2021?.antallAr != null && innPersongrunnlag.trygdetidKapittel20?.tt_fa_F2021?.antallAr!! >= 20 }
            OG {
                innPersongrunnlag.forsteVirkningsdatoGrunnlagListe!!.minst(1) {
                    it.kravlinjeTypeUnntakEnum == YtelseEnum.GJP
                            && it.virkningsdato!! < localDate(2021, 1, 1)
                }
            }
            SÅ {
                overgangsregel = true
            }
            kommentar("")
        }
        regel("Overgangsregel_GJRk19_tidligereGJR") {
            HVIS { innYtelsestype == YtelseEnum.GJR.toString() }
            OG { innPersongrunnlag.fødselsdato.year!! <= 1959 }
            OG { innVirk >= dato67m }
            OG { !innKapittel20 }
            OG { innPersongrunnlag.trygdetid?.tt_fa_F2021?.antallAr != null && innPersongrunnlag.trygdetid?.tt_fa_F2021?.antallAr!! >= 20 }
            OG {
                innPersongrunnlag.forsteVirkningsdatoGrunnlagListe!!.minst(1) {
                    it.kravlinjeTypeUnntakEnum == YtelseEnum.GJR
                            && it.virkningsdato!! < localDate(2021, 1, 1)
                }
            }
            SÅ {
                overgangsregel = true
            }
            kommentar("")
        }
        regel("Overgangsregel_GJRk20_tidligereGJR") {
            HVIS { innYtelsestype == YtelseEnum.GJR.toString() }
            OG { innPersongrunnlag.fødselsdato.year!! <= 1959 }
            OG { innVirk >= dato67m }
            OG { innKapittel20 }
            OG { innPersongrunnlag.trygdetidKapittel20?.tt_fa_F2021?.antallAr != null && innPersongrunnlag.trygdetidKapittel20?.tt_fa_F2021?.antallAr!! >= 20 }
            OG {
                innPersongrunnlag.forsteVirkningsdatoGrunnlagListe!!.minst(1) {
                    it.kravlinjeTypeUnntakEnum == YtelseEnum.GJR
                            && it.virkningsdato!! < localDate(2021, 1, 1)
                }
            }
            SÅ {
                overgangsregel = true
            }
        }
        regel("Unntak_kravlinjeFremsattDatoForSent") {
            HVIS { harKravlinjeFremsattDatoFom2021 }
            OG { !overgangsregel }
            SÅ {
                erFlyktning = false
            }
        }
        regel("ReturRegel") {
            HVIS { true }
            SÅ {
                RETURNER(erFlyktning)
            }
        }

    }
}
