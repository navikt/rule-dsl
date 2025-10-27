package no.nav.system.rule.dsl.demo.forklaring

import no.nav.system.rule.dsl.demo.domain.ForsteVirkningsdatoGrunnlag
import no.nav.system.rule.dsl.demo.domain.Person
import no.nav.system.rule.dsl.demo.domain.koder.UnntakEnum
import no.nav.system.rule.dsl.demo.domain.koder.UnntakEnum.*
import no.nav.system.rule.dsl.demo.domain.koder.UtfallType
import no.nav.system.rule.dsl.demo.domain.koder.UtfallType.*
import no.nav.system.rule.dsl.demo.domain.koder.YtelseEnum
import no.nav.system.rule.dsl.demo.domain.koder.YtelseEnum.*
import no.nav.system.rule.dsl.demo.helper.localDate
import no.nav.system.rule.dsl.demo.helper.måneder
import no.nav.system.rule.dsl.demo.helper.år
import no.nav.system.rule.dsl.forklaring.*
import org.jetbrains.kotlin.utils.addToStdlib.ifTrue
import java.time.LocalDate

fun personErFlyktning(
    persongrunnlag: Person,
    ytelseType: Grunnlag<YtelseEnum>,
    erKapittel20: Grunnlag<Boolean>,
    virk: Grunnlag<LocalDate>,
    kravlinjeFremsattDatoFom2021: Grunnlag<Boolean>
): Grunnlag<UtfallType> {

    // ========================================================================
    // Grunnlagsdata
    // ========================================================================

    val dato67m = Grunnlag(
        "Fødselsdato67m",
        Const(persongrunnlag.fødselsdato.value.withDayOfMonth(1) + 67.år + 1.måneder)
    )

    val unntakFraForutgaendeMedlemskap =
        persongrunnlag.inngangOgEksportgrunnlag?.unntakFraForutgaendeMedlemskap

    val unntakFraForutgaendeTT =
        persongrunnlag.inngangOgEksportgrunnlag?.unntakFraForutgaendeTT

    val trygdetid = erKapittel20
        .så { Const(persongrunnlag.trygdetidK20) }
        .ellers { Const(persongrunnlag.trygdetidK19) }
        .navngi("trygdetid")
        .id("SettRelevantTrygdetid")

    // ========================================================================
    // Flyktning-indikatorer
    // ========================================================================

    val flyktningFlagg = persongrunnlag.flyktning
        .toGrunnlag()
        .navngi("flyktningFlagg")
        .id("AngittFlyktning_FlyktningFlagg")

    val unntakFraForutgaendeMedlemskapType: Uttrykk<Boolean>? =
        unntakFraForutgaendeMedlemskap?.let { unntak ->
            (unntak.unntak.toGrunnlag() og (unntak.unntakType.toGrunnlag() erBlant aktuelleUnntakstyper()))
                .navngi("unntakFraForutgaendeMedlemskapType")
                .id("AngittFlyktning_HarUnntakFraForutgaendeMedlemskapTypeFlyktning")
        }

    val unntakFraForutgaendeTTType: Uttrykk<Boolean>? =
        unntakFraForutgaendeTT?.let { unntak ->
            (unntak.unntak.toGrunnlag() og (unntak.unntakType.toGrunnlag() erBlant aktuelleUnntakstyper()))
                .navngi("unntakFraForutgaendeTTType")
                .id("AngittFlyktning_HarUnntakFraForutgaendeTTTypeFlyktning")
        }

    val angittFlyktning = kombinerMedEller(
        unntakFraForutgaendeMedlemskapType,
        unntakFraForutgaendeTTType,
        flyktningFlagg
    ).navngi("angittFlyktning")
        .id("AngittFlyktning")

    // ========================================================================
    // Fellesbetingelser for overgangsregler
    // ========================================================================

    val fødselsdatoÅr = Grunnlag(
        "fødselsdatoÅr",
        Const(persongrunnlag.fødselsdato.value.year)
    )

    val fødselsdatoErMindreEllerLik1959 = (fødselsdatoÅr erMindreEllerLik 1959)
        .navngi("fødselsdatoErMindreEllerLik1959")
        .id("FødselsdatoErMindreEllerLik1959")

    // Merk: Må evaluere trygdetid siden tt_fa_F2021 er en property på Trygdetid-objektet
    val trygdetidErStørreEllerLik20 =
        (trygdetid.evaluer().tt_fa_F2021.toGrunnlag() erStørreEllerLik 20)
            .navngi("trygdetidErStørreEllerLik20")
            .id("TrygdetidErStørreEllerLik20")

    val virkErEtterEllerLikdato67m = (virk erEtterEllerLik dato67m)
        .navngi("virkErEtterEllerLikDato67m")
        .id("VirkErEtterEllerLikDato67m")

    val basisOvergangsregelBetingelse = (
            fødselsdatoErMindreEllerLik1959 og trygdetidErStørreEllerLik20
            ).navngi("basisOvergangsregelBetingelse")
        .id("BasisOvergangsregelBetingelse")

    // ========================================================================
    // Historiske kravlinjer
    // ========================================================================

    val harUTfør2021 = harKravlinjeTypeFør2021(
        persongrunnlag.forsteVirkningsdatoGrunnlagListe,
        UT,
        "Uføretrygd før 2021"
    )

    val harGJPfør2021 = harKravlinjeTypeFør2021(
        persongrunnlag.forsteVirkningsdatoGrunnlagListe,
        GJP,
        "Gjenlevendepensjon før 2021"
    )

    val harUTGJRfør2021 = harKravlinjeTypeFør2021(
        persongrunnlag.forsteVirkningsdatoGrunnlagListe,
        UT_GJR,
        "Gjenlevendetillegg før 2021"
    )

    val harGJRfør2021 = harKravlinjeTypeFør2021(
        persongrunnlag.forsteVirkningsdatoGrunnlagListe,
        GJR,
        "Gjenlevenderett før 2021"
    )

    // ========================================================================
    // Overgangsregler for Alderspensjon (AP)
    // ========================================================================

    val overgangsRegelAP = (ytelseType.evaluer() == AP).ifTrue {
        basisOvergangsregelBetingelse
            .navngi("overgangsregelAP")
            .id("Overgangsregel_AP")
    }

    val overgangsRegelAPOgVirk = overgangsRegelAP?.let { ap ->
        (ap og virkErEtterEllerLikdato67m)
            .navngi("overgangsregelAPOgVirkEtterEllerLikDato67m")
            .id("Overgangsregel_AP_og_virk_etter_eller_lik_dato67m")
    }

    val overgangsregelAPTidligereUT = overgangsRegelAPOgVirk?.let { apOgVirk ->
        (apOgVirk og harUTfør2021)
            .navngi("overgangsregelAPTidligereUT")
            .id("Overgangsregel_AP_tidligereUT")
    }

    val overgangsregelAPTidligereGJP = overgangsRegelAPOgVirk?.let { apOgVirk ->
        (apOgVirk og harGJPfør2021)
            .navngi("overgangsregelAPTidligereGJP")
            .id("Overgangsregel_AP_tidligereGJP")
    }

    // ========================================================================
    // Overgangsregler for Gjenlevenderett (GJR)
    // ========================================================================

    val overgangsRegelGJR = (ytelseType.evaluer() == GJR).ifTrue {
        (basisOvergangsregelBetingelse og virkErEtterEllerLikdato67m)
            .navngi("overgangsregelGJR")
            .id("Overgangsregel_GJR")
    }

    val overgangsregelGJRTidligereUTGJT = overgangsRegelGJR?.let { gjr ->
        (gjr og harUTGJRfør2021)
            .navngi("overgangsregelGJRTidligereUTGJT")
            .id("Overgangsregel_GJR_tidligereUT_GJT")
    }

    val overgangsregelGJRtidligereGJR = overgangsRegelGJR?.let { gjr ->
        (gjr og harGJRfør2021)
            .navngi("overgangsregelGJRtidligereGJR")
            .id("Overgangsregel_GJR_tidligereGJR")
    }

    // ========================================================================
    // Kombiner alle overgangsregler
    // ========================================================================

    val overgangsRegler = kombinerMedEller(
        overgangsRegelAP,
        overgangsregelAPTidligereUT,
        overgangsregelAPTidligereGJP,
        overgangsregelGJRTidligereUTGJT,
        overgangsregelGJRtidligereGJR
    ).navngi("overgangsRegler")
        .id("OvergangsRegler")

    // ========================================================================
    // Beslutningslogikk
    // ========================================================================

    return ikke(angittFlyktning)
        .så { Const(IKKE_RELEVANT) }
        .ellers {
            (angittFlyktning og ikke(kravlinjeFremsattDatoFom2021))
                .så { Const(OPPFYLT) }
                .ellers {
                    (angittFlyktning og kravlinjeFremsattDatoFom2021 og ikke(overgangsRegler))
                        .så { Const(IKKE_OPPFYLT) }
                        .ellers { Const(OPPFYLT) }
                }
        }.navngi("erFlyktning")
        .id("ErFlyktning")
}

// ========================================================================
// Hjelpefunksjoner
// ========================================================================

/**
 * Kombinerer flere nullable Boolean-uttrykk med ELLER.
 * Returnerer false hvis alle er null.
 */
private fun kombinerMedEller(vararg uttrykk: Uttrykk<Boolean>?): Uttrykk<Boolean> =
    uttrykk.filterNotNull()
        .reduceOrNull { acc, expr -> acc.eller(expr) }
        ?: Const(false)

/**
 * Sjekker om en person har en gitt kravlinjetype før 2021.
 */
private fun harKravlinjeTypeFør2021(
    liste: List<ForsteVirkningsdatoGrunnlag>,
    kravlinjeType: YtelseEnum,
    beskrivelse: String
): Grunnlag<Boolean> = Grunnlag(
    beskrivelse,
    Const(liste.any {
        it.kravlinjeType == kravlinjeType &&
                it.virkningsdato < localDate(2021, 1, 1)
    })
)

fun aktuelleUnntakstyper(): Grunnlag<List<UnntakEnum>> = Grunnlag(
    "aktuelleUnntakstyper",
    Const(listOf(FLYKT_ALDER, FLYKT_BARNEP, FLYKT_GJENLEV, FLYKT_UFOREP))
)
