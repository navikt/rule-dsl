package no.nav.pensjon.regler.alderspensjon.ruleset

import no.nav.pensjon.regler.alderspensjon.domain.Person
import no.nav.pensjon.regler.alderspensjon.domain.Trygdetid
import no.nav.pensjon.regler.alderspensjon.domain.koder.UnntakEnum
import no.nav.pensjon.regler.alderspensjon.domain.koder.UtfallType
import no.nav.pensjon.regler.alderspensjon.domain.koder.YtelseEnum
import no.nav.system.ruledsl.core.expression.Faktum
import no.nav.system.ruledsl.core.expression.boolean.erEtterEllerLik
import no.nav.system.ruledsl.core.expression.boolean.erLik
import no.nav.system.ruledsl.core.expression.boolean.erMindreEllerLik
import no.nav.system.ruledsl.core.expression.boolean.erStørreEllerLik
import no.nav.system.ruledsl.core.trace.RuleContext
import no.nav.system.ruledsl.core.trace.traced
import java.time.LocalDate
import java.time.Period

/**
 * Checks if person qualifies as refugee.
 *
 * NOTE: This is a partial port. The following features from core are NOT available in core-trace:
 * - Rule introspection: "AngittFlyktning".ingenHarTruffet(), "Overgangsregel".minstEnHarTruffet()
 * - kommentar() (rule documentation)
 *
 * Workaround: Use boolean flags to track rule firing status manually.
 */
context(ruleContext: RuleContext)
fun personenErFlyktning(
    innPersongrunnlag: Person,
    innYtelseType: Faktum<YtelseEnum>,
    innKapittel20: Faktum<Boolean>,
    innVirk: Faktum<LocalDate>,
    innKravlinjeFremsattDatoFom2021: Faktum<Boolean>,
): Faktum<UtfallType> = traced {

    val dato67m = Faktum(
        "Fødselsdato67m",
        innPersongrunnlag.fødselsdato.value.withDayOfMonth(1) + 67.år + 1.måneder
    )
    val unntakFraForutgaendeMedlemskap =
        innPersongrunnlag.inngangOgEksportgrunnlag?.unntakFraForutgaendeMedlemskap
    val unntakFraForutgaendeTT = 
        innPersongrunnlag.inngangOgEksportgrunnlag?.unntakFraForutgaendeTT
    val aktuelleUnntakstyper = listOf(
        UnntakEnum.FLYKT_ALDER, UnntakEnum.FLYKT_BARNEP, UnntakEnum.FLYKT_GJENLEV, UnntakEnum.FLYKT_UFOREP
    )
    val dato2021 = LocalDate.of(2021, 1, 1)
    val harUTfør2021 = Faktum(
        "Uføretrygd før 2021",
        innPersongrunnlag.forsteVirkningsdatoGrunnlagListe.any {
            it.kravlinjeType == YtelseEnum.UT && it.virkningsdato < dato2021
        }
    )
    val harGJPfør2021 = Faktum(
        "Gjenlevendepensjon før 2021",
        innPersongrunnlag.forsteVirkningsdatoGrunnlagListe.any {
            it.kravlinjeType == YtelseEnum.UT && it.virkningsdato < dato2021
        }
    )
    val harUTGJRfør2021 = Faktum(
        "Gjenlevendetillegg før 2021",
        innPersongrunnlag.forsteVirkningsdatoGrunnlagListe.any {
            it.kravlinjeType == YtelseEnum.UT_GJR && it.virkningsdato < dato2021
        }
    )
    val harGJRfør2021 = Faktum(
        "Gjenlevenderett før 2021",
        innPersongrunnlag.forsteVirkningsdatoGrunnlagListe.any {
            it.kravlinjeType == YtelseEnum.GJR && it.virkningsdato < dato2021
        }
    )

    var trygdetid: Trygdetid = Trygdetid()

    // Manual tracking for rule introspection replacement
    var angittFlyktningHarTruffet = false
    var overgangsregelHarTruffet = false

    regel("SettRelevantTrygdetid_kap19") {
        HVIS { innKapittel20 erLik false }
        SÅ {
            trygdetid = innPersongrunnlag.trygdetidK19
        }
    }

    regel("SettRelevantTrygdetid_kap20") {
        HVIS { innKapittel20 erLik true }
        SÅ {
            trygdetid = innPersongrunnlag.trygdetidK20
        }
    }

    // "AngittFlyktning" rules - track if any fires
    regel("AngittFlyktning_HarFlyktningFlaggetSatt") {
        HVIS { innPersongrunnlag.flyktning erLik true }
        SÅ {
            angittFlyktningHarTruffet = true
        }
    }

    regel("AngittFlyktning_HarUnntakFraForutgaendeMedlemskapTypeFlyktning") {
        HVIS { unntakFraForutgaendeMedlemskap != null }
        OG { unntakFraForutgaendeMedlemskap!!.unntak.value == true }
        OG { unntakFraForutgaendeMedlemskap?.unntakType != null }
        OG { unntakFraForutgaendeMedlemskap!!.unntakType.value in aktuelleUnntakstyper }
        SÅ {
            angittFlyktningHarTruffet = true
        }
    }

    regel("AngittFlyktning_HarUnntakFraForutgaendeTTTypeFlyktning") {
        HVIS { unntakFraForutgaendeTT != null }
        OG { unntakFraForutgaendeTT!!.unntak.value == true }
        OG { unntakFraForutgaendeTT?.unntakType != null }
        OG { unntakFraForutgaendeTT!!.unntakType.value in aktuelleUnntakstyper }
        SÅ {
            angittFlyktningHarTruffet = true
        }
    }

    // "Overgangsregel" rules - track if any fires
    regel("Overgangsregel_AP") {
        HVIS { innYtelseType.value == YtelseEnum.AP }
        OG { innPersongrunnlag.fødselsdato erMindreEllerLik 1959 }
        OG { trygdetid.tt_fa_F2021 erStørreEllerLik 20 }
        SÅ {
            overgangsregelHarTruffet = true
        }
    }

    regel("Overgangsregel_AP_tidligereUT") {
        HVIS { innYtelseType.value == YtelseEnum.AP }
        OG { innPersongrunnlag.fødselsdato erMindreEllerLik 1959 }
        OG { innVirk erEtterEllerLik dato67m }
        OG { trygdetid.tt_fa_F2021 erStørreEllerLik 20 }
        OG { harUTfør2021 erLik true }
        SÅ {
            overgangsregelHarTruffet = true
        }
    }

    regel("Overgangsregel_AP_tidligereGJP") {
        HVIS { innYtelseType.value == YtelseEnum.AP }
        OG { innPersongrunnlag.fødselsdato erMindreEllerLik 1959 }
        OG { innVirk erEtterEllerLik dato67m }
        OG { trygdetid.tt_fa_F2021 erStørreEllerLik 20 }
        OG { harGJPfør2021 erLik true }
        SÅ {
            overgangsregelHarTruffet = true
        }
    }

    regel("Overgangsregel_GJR_tidligereUT_GJT") {
        HVIS { innYtelseType.value == YtelseEnum.GJR }
        OG { innPersongrunnlag.fødselsdato erMindreEllerLik 1959 }
        OG { innVirk erEtterEllerLik dato67m }
        OG { trygdetid.tt_fa_F2021 erStørreEllerLik 20 }
        OG { harUTGJRfør2021 erLik true }
        SÅ {
            overgangsregelHarTruffet = true
        }
    }

    regel("Overgangsregel_GJR_tidligereGJR") {
        HVIS { innYtelseType.value == YtelseEnum.GJR }
        OG { innPersongrunnlag.fødselsdato erMindreEllerLik 1959 }
        OG { innVirk erEtterEllerLik dato67m }
        OG { trygdetid.tt_fa_F2021 erStørreEllerLik 20 }
        OG { harGJRfør2021 erLik true }
        SÅ {
            overgangsregelHarTruffet = true
        }
    }

    // Final decision rules using tracked flags instead of introspection
    regel("AnvendtFlyktning_ikkeRelevant") {
        HVIS { !angittFlyktningHarTruffet }
        RETURNER {
            Faktum("Anvendt flyktning", UtfallType.IKKE_RELEVANT)
        }
    }

    regel("AnvendtFlyktning_oppfylt") {
        HVIS { angittFlyktningHarTruffet }
        OG { innKravlinjeFremsattDatoFom2021 erLik false }
        RETURNER {
            Faktum("Anvendt flyktning", UtfallType.OPPFYLT)
        }
    }

    regel("AnvendtFlyktning_ingenOvergang") {
        HVIS { angittFlyktningHarTruffet }
        OG { innKravlinjeFremsattDatoFom2021 erLik true }
        OG { !overgangsregelHarTruffet }
        RETURNER {
            Faktum("Anvendt flyktning", UtfallType.IKKE_OPPFYLT)
        }
    }

    regel("AnvendtFlyktning_harOvergang") {
        HVIS { angittFlyktningHarTruffet }
        OG { innKravlinjeFremsattDatoFom2021 erLik true }
        OG { overgangsregelHarTruffet }
        RETURNER {
            Faktum("Anvendt flyktning", UtfallType.OPPFYLT)
        }
    }
}

inline val Int.måneder: Period
    get() = Period.ofMonths(this)

inline val Int.år: Period
    get() = Period.ofYears(this)
