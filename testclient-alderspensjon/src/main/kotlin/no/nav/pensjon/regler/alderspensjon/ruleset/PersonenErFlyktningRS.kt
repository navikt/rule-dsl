package no.nav.pensjon.regler.alderspensjon.ruleset

import no.nav.pensjon.regler.alderspensjon.config.AbstractDemoRuleset
import no.nav.pensjon.regler.alderspensjon.domain.Person
import no.nav.pensjon.regler.alderspensjon.domain.Trygdetid
import no.nav.pensjon.regler.alderspensjon.domain.koder.UnntakEnum
import no.nav.pensjon.regler.alderspensjon.domain.koder.UtfallType
import no.nav.pensjon.regler.alderspensjon.domain.koder.YtelseEnum
import no.nav.system.ruledsl.core.model.DslDomainPredicate
import no.nav.system.ruledsl.core.model.Faktum
import no.nav.system.ruledsl.core.operators.*
import java.time.LocalDate
import java.time.Period

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
) : AbstractDemoRuleset<Faktum<UtfallType>>() {
    private var dato67m: Faktum<LocalDate> =
        Faktum("Fødselsdato67m", innPersongrunnlag.fødselsdato.verdi.withDayOfMonth(1) + 67.år + 1.måneder)
    private val unntakFraForutgaendeMedlemskap =
        innPersongrunnlag.inngangOgEksportgrunnlag?.unntakFraForutgaendeMedlemskap
    private val unntakFraForutgaendeTT = innPersongrunnlag.inngangOgEksportgrunnlag?.unntakFraForutgaendeTT
    private val aktuelleUnntakstyper = listOf(
        UnntakEnum.FLYKT_ALDER, UnntakEnum.FLYKT_BARNEP, UnntakEnum.FLYKT_GJENLEV, UnntakEnum.FLYKT_UFOREP
    )
    private val dato2021 = LocalDate.of(2021, 1, 1)
    private val harUTfør2021 = Faktum(
        "Uføretrygd før 2021",
        innPersongrunnlag.forsteVirkningsdatoGrunnlagListe.any {
            it.kravlinjeType == YtelseEnum.UT && it.virkningsdato < dato2021
        }
    )
    private val harGJPfør2021 = Faktum(
        "Gjenlevendepensjon før 2021",
        innPersongrunnlag.forsteVirkningsdatoGrunnlagListe.any {
            it.kravlinjeType == YtelseEnum.UT && it.virkningsdato < dato2021
        }
    )
    private val harUTGJRfør2021 = Faktum(
        "Gjenlevendetillegg før 2021",
        innPersongrunnlag.forsteVirkningsdatoGrunnlagListe.any {
            it.kravlinjeType == YtelseEnum.UT_GJR && it.virkningsdato < dato2021
        }
    )
    private val harGJRfør2021 = Faktum(
        "Gjenlevenderett før 2021",
        innPersongrunnlag.forsteVirkningsdatoGrunnlagListe.any {
            it.kravlinjeType == YtelseEnum.GJR && it.virkningsdato < dato2021
        }
    )
    private lateinit var trygdetid: Trygdetid

    override fun create() {
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
        regel("AngittFlyktning_HarFlyktningFlaggetSatt") {
            HVIS { innPersongrunnlag.flyktning }
            kommentar("Flyktningerflagget er angitt av saksbehandler.")
        }
        regel("AngittFlyktning_HarUnntakFraForutgaendeMedlemskapTypeFlyktning") {
            HVIS { unntakFraForutgaendeMedlemskap != null }
            OG { unntakFraForutgaendeMedlemskap!!.unntak }
            OG { unntakFraForutgaendeMedlemskap?.unntakType != null }
            OG { unntakFraForutgaendeMedlemskap!!.unntakType.erBlant(aktuelleUnntakstyper) }
        }
        regel("AngittFlyktning_HarUnntakFraForutgaendeTTTypeFlyktning") {
            HVIS { unntakFraForutgaendeTT != null }
            OG { unntakFraForutgaendeTT!!.unntak }
            OG { unntakFraForutgaendeTT?.unntakType != null }
            OG { unntakFraForutgaendeTT!!.unntakType erBlant aktuelleUnntakstyper }
        }
        regel("Overgangsregel_AP") {
            HVIS { innYtelseType.verdi == YtelseEnum.AP }
            OG { innPersongrunnlag.fødselsdato erMindreEllerLik 1959 }
            OG { trygdetid.tt_fa_F2021 erStørreEllerLik 20 }
        }
        regel("Overgangsregel_AP_tidligereUT") {
            HVIS { innYtelseType.verdi == YtelseEnum.AP }
            OG { innPersongrunnlag.fødselsdato erMindreEllerLik 1959 }
            OG { innVirk erEtterEllerLik dato67m }
            OG { trygdetid.tt_fa_F2021 erStørreEllerLik 20 }
            OG { harUTfør2021 }
        }
        regel("Overgangsregel_AP_tidligereGJP") {
            HVIS { innYtelseType.verdi == YtelseEnum.AP }
            OG { innPersongrunnlag.fødselsdato erMindreEllerLik 1959 }
            OG { innVirk erEtterEllerLik dato67m }
            OG { trygdetid.tt_fa_F2021 erStørreEllerLik 20 }
            OG { harGJPfør2021 }
        }
        regel("Overgangsregel_GJR_tidligereUT_GJT") {
            HVIS { innYtelseType.verdi == YtelseEnum.GJR }
            OG { innPersongrunnlag.fødselsdato erMindreEllerLik 1959 }
            OG { innVirk erEtterEllerLik dato67m }
            OG { trygdetid.tt_fa_F2021 erStørreEllerLik 20 }
            OG { harUTGJRfør2021 }
        }
        regel("Overgangsregel_GJR_tidligereGJR") {
            HVIS { innYtelseType.verdi == YtelseEnum.GJR }
            OG { innPersongrunnlag.fødselsdato erMindreEllerLik 1959 }
            OG { innVirk erEtterEllerLik dato67m }
            OG { trygdetid.tt_fa_F2021 erStørreEllerLik 20 }
            OG { harGJRfør2021 }
        }
        regel("AnvendtFlyktning_ikkeRelevant") {
            HVIS { "AngittFlyktning".ingenHarTruffet() }
            SÅ {
                RETURNER(sporing("Anvendt flyktning", UtfallType.IKKE_RELEVANT))
            }
        }
        regel("AnvendtFlyktning_oppfylt") {
            HVIS { "AngittFlyktning".minstEnHarTruffet() }
            OG { innKravlinjeFremsattDatoFom2021 erLik false }
            SÅ {
                RETURNER(sporing("Anvendt flyktning", UtfallType.OPPFYLT))
            }
        }
        regel("AnvendtFlyktning_ingenOvergang") {
            HVIS { "AngittFlyktning".minstEnHarTruffet() }
            OG { innKravlinjeFremsattDatoFom2021 }
            OG { "Overgangsregel".ingenHarTruffet() }
            SÅ {
                RETURNER(sporing("Anvendt flyktning", UtfallType.IKKE_OPPFYLT))
            }
        }
        regel("AnvendtFlyktning_harOvergang") {
            HVIS { "AngittFlyktning".minstEnHarTruffet() }
            OG { innKravlinjeFremsattDatoFom2021 }
            OG { "Overgangsregel".minstEnHarTruffet() }
            SÅ {
                RETURNER(sporing("Anvendt flyktning", UtfallType.OPPFYLT))
            }
        }
    }
}


inline val Int.måneder: Period
    get() = Period.ofMonths(this)

inline val Int.år: Period
    get() = Period.ofYears(this)