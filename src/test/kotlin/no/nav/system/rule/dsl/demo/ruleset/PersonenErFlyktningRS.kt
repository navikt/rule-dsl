package no.nav.system.rule.dsl.demo.ruleset

import no.nav.system.rule.dsl.AbstractRuleset
import no.nav.system.rule.dsl.demo.domain.*
import no.nav.system.rule.dsl.demo.domain.koder.*
import no.nav.system.rule.dsl.rettsregel.Faktum
import java.time.LocalDate
import java.util.*

/**
 * Hvis unntak fra forutgående medlemsskap er angitt i inngang og eksportgrunnlag og unntaktype er
 * flyktning så har personen status som flyktning.
 */
class PersonenErFlyktningRS(
    private val innPersongrunnlag: Person,
    private val innHarKravfremsattdatoFom2021: Faktum<Boolean>,
//    private val innInngangOgEksportgrunnlag: InngangOgEksportgrunnlag,
//    private val innTrygdetidK19: Trygdetid,
//    private val innTrygdetidK20: Trygdetid,
//    private val innAngittFlyktning: Faktum<Boolean>,
//    private val innFødselsdato: Faktum<LocalDate>,
    private val innYtelseType: Faktum<YtelseEnum>,
    private val innKapittel20: Faktum<Boolean>,
    private val innVirk: Faktum<Date>
) : AbstractRuleset<Boolean>() {

    private var erFlyktning: Boolean = false
    private var overgangsregel: Boolean = false
    private var dato67m: LocalDate = innPersongrunnlag.fødselsdato

//    var datoClone = innFødselsdato.clone() as Date
//    datoClone += 67.år
//    datoClone += 1.måneder
//    date(datoClone.år, datoClone.måned.value, 1)

    override fun create() {
        regel("DEBUG_PersonenErFlyktningRS") {
            HVIS { REGLER_DEBUG }
            SÅ {
                log_debug("[DBG] PersonenErFlyktningRS")
                log_debug("[   ]    pg.flyktning: ${innPersongrunnlag?.flyktning}")
                log_debug("[   ]    ytelsestype: $innYtelsestype")
                log_debug("[   ]    innKapittel20: $innKapittel20")
                log_debug("[   ]    innVirk: $innVirk")
                log_debug("[   ]    dato67m: $dato67m")
                log_debug("[   ]    harKravlinjeFremsattDatoFom2021: $harKravlinjeFremsattDatoFom2021")
            }
        }
        regel("HarUnntakFraForutgaendeMedlemskapTypeFlyktning") {
            HVIS { innPersongrunnlag?.inngangOgEksportGrunnlag != null }
            OG { innPersongrunnlag?.inngangOgEksportGrunnlag?.unntakFraForutgaendeMedlemskap != null }
            OG { innPersongrunnlag?.inngangOgEksportGrunnlag?.unntakFraForutgaendeMedlemskap?.unntak == true }
            OG { innPersongrunnlag?.inngangOgEksportGrunnlag?.unntakFraForutgaendeMedlemskap?.unntakType != null }
            OG {
                innPersongrunnlag?.inngangOgEksportGrunnlag?.unntakFraForutgaendeMedlemskap?.unntakType?.kode == InngangUnntakEnum.FLYKT_ALDER.toString()
                        || innPersongrunnlag?.inngangOgEksportGrunnlag?.unntakFraForutgaendeMedlemskap?.unntakType?.kode == InngangUnntakEnum.FLYKT_BARNEP.toString()
                        || innPersongrunnlag?.inngangOgEksportGrunnlag?.unntakFraForutgaendeMedlemskap?.unntakType?.kode == InngangUnntakEnum.FLYKT_GJENLEV.toString()
                        || innPersongrunnlag?.inngangOgEksportGrunnlag?.unntakFraForutgaendeMedlemskap?.unntakType?.kode == InngangUnntakEnum.FLYKT_UFOREP.toString()
            }
            SÅ {
                log_debug("[HIT] PersonenErFlyktningRS.HarUnntakFraForutgaendeMedlemskapTypeFlyktning")
                erFlyktning = true
            }
            kommentar(
                """Hvis unntak fra forutgående medlemsskap er angitt i inngang og eksportgrunnlag
            og unntaktype er flyktning så har personen status som flyktning."""
            )
        }
        regel("HarUnntakFraForutgaendeTTTypeFlyktning") {
            HVIS { innPersongrunnlag?.inngangOgEksportGrunnlag != null }
            OG { innPersongrunnlag?.inngangOgEksportGrunnlag?.unntakFraForutgaendeTT != null }
            OG { innPersongrunnlag?.inngangOgEksportGrunnlag?.unntakFraForutgaendeTT?.unntak == true }
            OG { innPersongrunnlag?.inngangOgEksportGrunnlag?.unntakFraForutgaendeTT?.unntakType != null }
            OG {
                innPersongrunnlag?.inngangOgEksportGrunnlag?.unntakFraForutgaendeTT?.unntakType?.kode == InngangUnntakEnum.FLYKT_ALDER.toString()
                        || innPersongrunnlag?.inngangOgEksportGrunnlag?.unntakFraForutgaendeTT?.unntakType?.kode == InngangUnntakEnum.FLYKT_BARNEP.toString()
                        || innPersongrunnlag?.inngangOgEksportGrunnlag?.unntakFraForutgaendeTT?.unntakType?.kode == InngangUnntakEnum.FLYKT_GJENLEV.toString()
                        || innPersongrunnlag?.inngangOgEksportGrunnlag?.unntakFraForutgaendeTT?.unntakType?.kode == InngangUnntakEnum.FLYKT_UFOREP.toString()
            }
            SÅ {
                log_debug("[HIT] PersonenErFlyktningRS.HarUnntakFraForutgaendeTTTypeFlyktning")
                erFlyktning = true
            }
            kommentar(
                """Hvis unntak fra forutgående trygdetid er angitt i inngang og eksportgrunnlag og
            unntaktype er flyktning så har personen status som flyktning."""
            )
        }
        regel("HarFlyktningFlaggetSatt") {
            HVIS { innPersongrunnlag?.flyktning != null }
            OG { innPersongrunnlag?.flyktning!! }
            SÅ {
                log_debug("[HIT] PersonenErFlyktningRS.HarFlyktningFlaggetSatt")
                erFlyktning = true
            }
            kommentar(
                """Hvis flyktningflagget er satt på persongrunnlaget så har personen status som
            flyktning."""
            )
        }

        regel("Overgangsregel_APk19") {
            HVIS { innYtelsestype == YtelsetypeEnum.AP.toString() }
            OG { innPersongrunnlag?.fodselsdato?.år!! <= 1959 }
            OG { !innKapittel20!! }
            OG { innPersongrunnlag?.trygdetid?.tt_fa_F2021 != null }
            OG { innPersongrunnlag?.trygdetid?.tt_fa_F2021?.antallAr!! >= 20 } // nullsjekk ny i v2
            SÅ {
                log_debug("[HIT] PersonenErFlyktningRS.Overgangsregel_APk19")
                overgangsregel = true
            }
            kommentar(
                """Det skal tas hensyn til særbestemmelsene for beregning av alderspensjon (både
            kapittel 19 og 20) til
        flyktninger dersom søker er født i 1959 eller tidligere, og på ikrafttredelsestidspunktet
            (01.01.2021)
        har vært medlem av folketrygden i minst 20 år etter fylte 16 år.

        HVIS søker.vedtak = Alderspensjon
        OG søker.kull <= 1959
        OG søker.trygdetid.trygdetid_F2021 >= 20
        OG søker.inngangOgEksportGrunnlag.minstTyveArBotidNorge = JA"""
            )
        }
        regel("Overgangsregel_APk20") {
            HVIS { innYtelsestype == YtelsetypeEnum.AP.toString() }
            OG { innPersongrunnlag?.fodselsdato?.år!! <= 1959 }
            OG { innKapittel20!! }
            OG { innPersongrunnlag?.trygdetidKapittel20?.tt_fa_F2021 != null }
            OG { innPersongrunnlag.trygdetidKapittel20?.tt_fa_F2021?.verdi!! >= 20 } // nullsjekk ny i v2
            SÅ {
                log_debug("[HIT] PersonenErFlyktningRS.Overgangsregel_APk20")
                overgangsregel = true
            }
            kommentar(
                """Det skal tas hensyn til særbestemmelsene for beregning av alderspensjon (både
            kapittel 19 og 20) til
        flyktninger dersom søker er født i 1959 eller tidligere, og på ikrafttredelsestidspunktet
            (01.01.2021)
        har vært medlem av folketrygden i minst 20 år etter fylte 16 år.

        HVIS søker.vedtak = Alderspensjon
        OG søker.kull <= 1959
        OG søker.trygdetid.trygdetid_F2021 >= 20
        OG søker.inngangOgEksportGrunnlag.minstTyveArBotidNorge = JA"""
            )
        }
        regel("Overgangsregel_APk19_tidligereUT") {
            HVIS { innYtelsestype == YtelsetypeEnum.AP.toString() }
            OG { innPersongrunnlag?.fodselsdato?.år!! <= 1959 }
            OG { innVirk != null } // nullsjekk ny i v2
            OG { innVirk?.toLocalDate()!! >= dato67m!!.toLocalDate() }
            OG { !innKapittel20!! }
            OG { innPersongrunnlag.trygdetid?.tt_fa_F2021 != null }
            OG { innPersongrunnlag.trygdetid?.tt_fa?.antallAr!! >= 20 }
            OG {
                innPersongrunnlag?.forsteVirkningsdatoGrunnlagListe!!.minst(1) {
                    it.kravlinjeType?.kode == YtelsetypeEnum.UT.toString()
                            && it.virkningsdato?.toLocalDate()!! < localDate(2021, 1, 1)
                }
            }
            SÅ {
                log_debug("[HIT] PersonenErFlyktningRS.Overgangsregel_APk19_tidligereUT")
                overgangsregel = true
            }
            kommentar(
                """Flyktninger som på ikrafttredelsestidspunktet (01.01.2021) hadde uføretrygd
            innvilget på grunnlag av
        særbestemmelsene for flyktninger, og som er født i 1959 eller tidligere, og som etter fylte
            16 år har
        vært medlemmer av folketrygden i minst 20 år ved fylte 67 år, skal ved fylte 67 år få
            alderspensjon
        etter særbestemmelsene for flyktninger.

        HVIS søker.vedtak = Alderspensjon
        OG søker.alder ved virkningstidspunkt >= 67m
        OG søker.kull <= 1959
        OG søker.forsteVirkningsdatoGrunnlagListe inneholder minst ett innslag hvor (kravlinjeType =
            UT og virkningstidspunktFom < 01.01.2021)
        (kap19) OG bruker.trygdetid.tt_fa.ar >= 20
        (kap20) OG bruker.trygdetidKap20.tt_fa.ar >= 20"""
            )
        }
        regel("Overgangsregel_APk20_tidligereUT") {
            HVIS { innYtelsestype == YtelsetypeEnum.AP.toString() }
            OG { innPersongrunnlag?.fodselsdato?.år!! <= 1959 }
            OG { innVirk != null } // nullsjekk ny i v2
            OG { innVirk!!.toLocalDate() >= dato67m!!.toLocalDate() }
            OG { innKapittel20!! }
            OG { innPersongrunnlag?.trygdetidKapittel20?.tt_fa_F2021 != null && innPersongrunnlag.trygdetidKapittel20?.tt_fa_F2021?.antallAr!! >= 20 } // nullsjekk ny i v2
            OG {
                innPersongrunnlag?.forsteVirkningsdatoGrunnlagListe!!.minst(1) {
                    it.kravlinjeType?.kode == YtelsetypeEnum.UT.toString()
                            && it.virkningsdato!!.toLocalDate() < localDate(2021, 1, 1)
                }
            }
            SÅ {
                log_debug("[HIT] PersonenErFlyktningRS.Overgangsregel_APk20_tidligereUT")
                overgangsregel = true
            }
            kommentar(
                """Flyktninger som på ikrafttredelsestidspunktet (01.01.2021) hadde uføretrygd
            innvilget på grunnlag av
        særbestemmelsene for flyktninger, og som er født i 1959 eller tidligere, og som etter fylte
            16 år har
        vært medlemmer av folketrygden i minst 20 år ved fylte 67 år, skal ved fylte 67 år få
            alderspensjon
        etter særbestemmelsene for flyktninger.

        HVIS søker.vedtak = Alderspensjon
        OG søker.alder ved virkningstidspunkt >= 67m
        OG søker.kull <= 1959
        OG søker.forsteVirkningsdatoGrunnlagListe inneholder minst ett innslag hvor (kravlinjeType =
            UT og virkningstidspunktFom < 01.01.2021)
        (kap19) OG bruker.trygdetid.tt_fa.ar >= 20
        (kap20) OG bruker.trygdetidKap20.tt_fa.ar >= 20"""
            )
        }
        regel("Overgangsregel_GJRk19_tidligereUT_GJT") {
            HVIS { innYtelsestype == YtelsetypeEnum.GJR.toString() }
            OG { innPersongrunnlag?.fodselsdato?.år!! <= 1959 }
            OG { innVirk != null } // nullsjekk ny i v2
            OG { innVirk!!.toLocalDate() >= dato67m!!.toLocalDate() }
            OG { !innKapittel20!! }
            OG { innPersongrunnlag?.trygdetid?.tt_fa_F2021?.antallAr != null && innPersongrunnlag.trygdetid?.tt_fa_F2021?.antallAr!! >= 20 } // nullsjekk er ny i v2
            OG {
                innPersongrunnlag?.forsteVirkningsdatoGrunnlagListe!!.minst(1) {
                    it.kravlinjeType?.kode == YtelsetypeEnum.UT_GJT.toString()
                            && it.virkningsdato!!.toLocalDate() < localDate(2021, 1, 1)
                }
            }
            SÅ {
                log_debug("[HIT] PersonenErFlyktningRS.Overgangsregel_GJRk19_tidligereUT_GJT")
                overgangsregel = true
            }
            kommentar(
                """Flyktninger som på ikrafttredelsestidspunktet (01.01.2021) hadde uføretrygd
            innvilget på grunnlag av særbestemmelsene
        for flyktninger, og som er født i 1959 eller tidligere, og som etter fylte 16 år har vært
            medlemmer av folketrygden i
        minst 20 år ved fylte 67 år, skal ved fylte 67 år få alderspensjon etter særbestemmelsene
            for flyktninger.

        HVIS avdød.vedtak = Gjenlevenderett
        OG avdød.alder ved virkningstidspunkt >= 67m
        OG avdød.kull <= 1959
        OG avdød.forsteVirkningsdatoGrunnlagListe inneholder minst ett innslag hvor (kravlinjeType =
            GJT_UT OG virkningstidspunktFom < 01.01.2021)
        (kap19) OG avdød.trygdetid.tt_fa.ar >= 20
        (kap20) OG avdød.trygdetidKap20.tt_fa.ar >= 20"""
            )
        }
        regel("Overgangsregel_GJRk20_tidligereUT_GJT") {
            HVIS { innYtelsestype == YtelsetypeEnum.GJR.toString() }
            OG { innPersongrunnlag?.fodselsdato?.år!! <= 1959 }
            OG { innVirk != null } // nullsjekk ny i v2
            OG { innVirk!!.toLocalDate() >= dato67m!!.toLocalDate() }
            OG { innKapittel20!! }
            OG { innPersongrunnlag?.trygdetidKapittel20?.tt_fa_F2021?.antallAr != null && innPersongrunnlag.trygdetidKapittel20?.tt_fa_F2021?.antallAr!! >= 20 } // nullsjekk er ny i v2
            OG {
                innPersongrunnlag?.forsteVirkningsdatoGrunnlagListe!!.minst(1) {
                    it.kravlinjeType?.kode == YtelsetypeEnum.UT_GJT.toString()
                            && it.virkningsdato!!.toLocalDate() < localDate(2021, 1, 1)
                }
            }
            SÅ {
                log_debug("[HIT] PersonenErFlyktningRS.Overgangsregel_GJRk20_tidligereUT_GJT")
                overgangsregel = true
            }
            kommentar(
                """Flyktninger som på ikrafttredelsestidspunktet (01.01.2021) hadde uføretrygd
            innvilget på grunnlag av særbestemmelsene
        for flyktninger, og som er født i 1959 eller tidligere, og som etter fylte 16 år har vært
            medlemmer av folketrygden i
        minst 20 år ved fylte 67 år, skal ved fylte 67 år få alderspensjon etter særbestemmelsene
            for flyktninger.

        HVIS avdød.vedtak = Gjenlevenderett
        OG avdød.alder ved virkningstidspunkt >= 67m
        OG avdød.kull <= 1959
        OG avdød.forsteVirkningsdatoGrunnlagListe inneholder minst ett innslag hvor (kravlinjeType =
            GJT_UT OG virkningstidspunktFom < 01.01.2021)
        (kap19) OG avdød.trygdetid.tt_fa.ar >= 20
        (kap20) OG avdød.trygdetidKap20.tt_fa.ar >= 20"""
            )
        }
        regel("Overgangsregel_APk19_tidligereGJP") {
            HVIS { innYtelsestype == YtelsetypeEnum.AP.toString() }
            OG { innPersongrunnlag?.fodselsdato?.år!! <= 1959 }
            OG { innVirk != null } // nullsjekk ny i v2
            OG { innVirk!!.toLocalDate() >= dato67m!!.toLocalDate() }
            OG { !innKapittel20!! }
            OG { innPersongrunnlag?.trygdetid?.tt_fa_F2021?.antallAr != null && innPersongrunnlag.trygdetid?.tt_fa_F2021?.antallAr!! >= 20 } // nullsjekk er ny i v2
            OG {
                innPersongrunnlag?.forsteVirkningsdatoGrunnlagListe!!.minst(1) {
                    it.kravlinjeType?.kode == YtelsetypeEnum.GJP.toString()
                            && it.virkningsdato!!.toLocalDate() < localDate(2021, 1, 1)
                }
            }
            SÅ {
                log_debug("[HIT] PersonenErFlyktningRS.Overgangsregel_APk19_tidligereGJP")
                overgangsregel = true
            }
            kommentar(
                """Flyktninger som på ikrafttredelsestidspunktet (01.01.2021) hadde
            gjenlevendepensjon innvilget på grunnlag
        av særbestemmelsene for flyktninger, og som er født i 1959 eller tidligere, og som etter
            fylte 16 år har
        vært medlemmer av folketrygden i minst 20 år ved fylte 67 år, skal ved fylte 67 år få
            alderspensjon etter
        særbestemmelsene for flyktninger.

        HVIS søker.vedtak = Alderspensjon
        OG søker.alder ved virkningstidspunkt >= 67m
        OG søker.kull <= 1959
        OG søker.forsteVirkningsdatoGrunnlagListe inneholder minst ett innslag hvor (kravlinjeType =
            GJP OG virkningstidspunktFom < 01.01.2021)
        (kap19) OG søker.trygdetid.tt_fa.ar >= 20
        (kap20) OG søker.trygdetidKap20.tt_fa.ar >= 20"""
            )
        }
        regel("Overgangsregel_APk20_tidligereGJP") {
            HVIS { innYtelsestype == YtelsetypeEnum.AP.toString() }
            OG { innPersongrunnlag?.fodselsdato?.år!! <= 1959 }
            OG { innVirk != null } // nullsjekk ny i v2
            OG { innVirk!!.toLocalDate() >= dato67m!!.toLocalDate() }
            OG { innKapittel20!! }
            OG { innPersongrunnlag?.trygdetidKapittel20?.tt_fa_F2021?.antallAr != null && innPersongrunnlag.trygdetidKapittel20?.tt_fa_F2021?.antallAr!! >= 20 } // nullsjekk er ny i v2
            OG {
                innPersongrunnlag?.forsteVirkningsdatoGrunnlagListe!!.minst(1) {
                    it.kravlinjeType?.kode == YtelsetypeEnum.GJP.toString()
                            && it.virkningsdato!!.toLocalDate() < localDate(2021, 1, 1)
                }
            }
            SÅ {
                log_debug("[HIT] PersonenErFlyktningRS.Overgangsregel_APk20_tidligereGJP")
                overgangsregel = true
            }
            kommentar(
                """Flyktninger som på ikrafttredelsestidspunktet (01.01.2021) hadde
            gjenlevendepensjon innvilget på grunnlag
        av særbestemmelsene for flyktninger, og som er født i 1959 eller tidligere, og som etter
            fylte 16 år har
        vært medlemmer av folketrygden i minst 20 år ved fylte 67 år, skal ved fylte 67 år få
            alderspensjon etter
        særbestemmelsene for flyktninger.

        HVIS søker.vedtak = Alderspensjon
        OG søker.alder ved virkningstidspunkt >= 67m
        OG søker.kull <= 1959
        OG søker.forsteVirkningsdatoGrunnlagListe inneholder minst ett innslag hvor (kravlinjeType =
            GJP OG virkningstidspunktFom < 01.01.2021)
        (kap19) OG søker.trygdetid.tt_fa.ar >= 20
        (kap20) OG søker.trygdetidKap20.tt_fa.ar >= 20"""
            )
        }
        regel("Overgangsregel_GJRk19_tidligereGJR") {
            HVIS { innYtelsestype == YtelsetypeEnum.GJR.toString() }
            OG { innPersongrunnlag?.fodselsdato?.år!! <= 1959 }
            OG { innVirk != null } // nullsjekk ny i v2
            OG { innVirk!!.toLocalDate() >= dato67m!!.toLocalDate() }
            OG { !innKapittel20!! }
            OG { innPersongrunnlag?.trygdetid?.tt_fa_F2021?.antallAr != null && innPersongrunnlag.trygdetid?.tt_fa_F2021?.antallAr!! >= 20 } // nullsjekk er ny i v2
            OG {
                innPersongrunnlag?.forsteVirkningsdatoGrunnlagListe!!.minst(1) {
                    it.kravlinjeType?.kode == YtelsetypeEnum.GJR.toString()
                            && it.virkningsdato!!.toLocalDate() < localDate(2021, 1, 1)
                }
            }
            SÅ {
                log_debug("[HIT] PersonenErFlyktningRS.Overgangsregel_GJRk19_tidligereGJR")
                overgangsregel = true
            }
            kommentar(
                """Flyktninger som på ikrafttredelsestidspunktet (01.01.2021) hadde
            gjenlevendepensjon innvilget på grunnlag
        av særbestemmelsene for flyktninger, og som er født i 1959 eller tidligere, og som etter
            fylte 16 år har
        vært medlemmer av folketrygden i minst 20 år ved fylte 67 år, skal ved fylte 67 år få
            alderspensjon etter
        særbestemmelsene for flyktninger.

        HVIS avdød.vedtak = Gjenlevenderett
        OG avdød.alder ved virkningstidspunkt >= 67m
        OG avdød.kull <= 1959
        OG avdød.forsteVirkningsdatoGrunnlagListe inneholder minst ett innslag hvor (kravlinjeType =
            GJR OG virkningstidspunktFom < 01.01.2021)
        (kap19) OG avdød.trygdetid.tt_fa.ar >= 20
        (kap20) OG avdød.trygdetidKap20.tt_fa.ar >= 20"""
            )
        }
        regel("Overgangsregel_GJRk20_tidligereGJR") {
            HVIS { innYtelsestype == YtelsetypeEnum.GJR.toString() }
            OG { innPersongrunnlag?.fodselsdato?.år!! <= 1959 }
            OG { innVirk != null } // nullsjekk ny i v2
            OG { innVirk!!.toLocalDate() >= dato67m!!.toLocalDate() }
            OG { innKapittel20!! }
            OG { innPersongrunnlag?.trygdetidKapittel20?.tt_fa_F2021?.antallAr != null && innPersongrunnlag.trygdetidKapittel20?.tt_fa_F2021?.antallAr!! >= 20 } // nullsjekk er ny i v2
            OG {
                innPersongrunnlag?.forsteVirkningsdatoGrunnlagListe!!.minst(1) {
                    it.kravlinjeType?.kode == YtelsetypeEnum.GJR.toString()
                            && it.virkningsdato!!.toLocalDate() < localDate(2021, 1, 1)
                }
            }
            SÅ {
                log_debug("[HIT] PersonenErFlyktningRS.Overgangsregel_GJRk20_tidligereGJR")
                overgangsregel = true
            }
        }
        regel("Unntak_kravlinjeFremsattDatoForSent") {
            HVIS { harKravlinjeFremsattDatoFom2021 }
            OG { !overgangsregel }
            SÅ {
                log_debug("[HIT] PersonenErFlyktningRS.Unntak_kravlinjeFremsattDatoForSent")
                erFlyktning = false
            }
        }
        regel("ReturRegel") {
            HVIS { true }
            SÅ {
                log_debug("[HIT] PersonenErFlyktningRS.ReturRegel erFlykning: $erFlyktning")
                RETURNER(erFlyktning)
            }
        }

    }
}
