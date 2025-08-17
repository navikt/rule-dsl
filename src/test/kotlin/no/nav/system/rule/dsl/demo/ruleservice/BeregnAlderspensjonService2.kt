package no.nav.system.rule.dsl.demo.ruleservice

import no.nav.system.rule.dsl.demo.domain.ForsteVirkningsdatoGrunnlag
import no.nav.system.rule.dsl.demo.domain.Person
import no.nav.system.rule.dsl.demo.domain.Request
import no.nav.system.rule.dsl.demo.domain.Response
import no.nav.system.rule.dsl.demo.domain.koder.UnntakEnum
import no.nav.system.rule.dsl.demo.domain.koder.UnntakEnum.*
import no.nav.system.rule.dsl.demo.domain.koder.UtfallType
import no.nav.system.rule.dsl.demo.domain.koder.YtelseEnum
import no.nav.system.rule.dsl.demo.ruleflow.BeregnAlderspensjonFlyt
import no.nav.system.rule.dsl.rettsregel.Faktum
import java.time.LocalDate

fun <T : Any> Faktum<Boolean>.hvis(ja: () -> T, nei: () -> T) = if (this.value) ja() else nei()
fun <T : Any> Faktum<Boolean>.hvis(ja: T, nei: () -> T) = if (this.value) ja else nei()
fun <T : Any> Faktum<Boolean>.hvis(ja: () -> T, nei: T) = if (this.value) ja() else nei
fun <T : Any> Faktum<Boolean>.hvis(ja: T, nei: T) = if (this.value) ja else nei

fun eller(vararg fakta: Faktum<Boolean>): Faktum<Boolean> =
    Faktum(
        navn = fakta.joinToString(" eller ") { it.name },
        verdi = fakta.any { it.value },
    )

fun og(vararg fakta: Faktum<Boolean>): Faktum<Boolean> =
    Faktum(
        navn = fakta.joinToString(" og ") { it.name },
        verdi = fakta.all { it.value },
    )

fun <T : Any> Faktum<T>.verdi(): T = this.value

fun <T : Any> Faktum<T>.erIListen(others: List<T>) =
    if (this.value in others) Faktum("er i listen", true) else Faktum("er ikke i listen", false)

fun <T> Faktum<T>.erStørreEllerLik(other: T)
        where T : Any, T : Comparable<T> = if (this.value >= other)
    Faktum("er større eller lik", true)
else
    Faktum("er ikke større eller lik", false)

fun <T> Faktum<T>.erMindreEllerLik(other: T)
        where T : Any, T : Comparable<T> = if (this.value <= other)
    Faktum("er mindre eller lik", true)
else
    Faktum("er ikke mindre eller lik", false)

fun <T> Faktum<T>.erLik(other: T)
        where T : Any, T : Comparable<T> = if (this.value == other)
    Faktum("er lik", true)
else
    Faktum("er ikke lik", false)

fun <T> Faktum<T>.erStørreEllerLik(other: Faktum<T>)
        where T : Any, T : Comparable<T> = if (this.value >= other.value)
    Faktum("er større eller lik", true)
else
    Faktum("er ikke større eller lik", false)

fun Faktum<Boolean>.erUsant() = !this.value

fun Faktum<Boolean>.usant() = if (!this.value) Faktum("", true) else Faktum("", false)

fun Faktum<Boolean>.er() = if (!this.value) Faktum("", true) else Faktum("", false)

fun harYtelseFørDato(
    ytelse: YtelseEnum,
    forsteVirkningsdatoGrunnlagListe: List<ForsteVirkningsdatoGrunnlag>,
    dato: LocalDate
): Faktum<Boolean> = Faktum(
    "$ytelse før $dato",
    forsteVirkningsdatoGrunnlagListe.any { it.kravlinjeType == ytelse && it.virkningsdato < dato }
)


fun overgangsregler(
    person: Faktum<Person>,
    ytelse: Faktum<YtelseEnum>,
    virkningstidspunkt: Faktum<LocalDate>,
    kap20: Faktum<Boolean>
): Faktum<Boolean> {
    val fødselsdato = person.value.fødselsdato
    val fødselår = Faktum("fødselsår", fødselsdato.value.year)
    val dato67m = Faktum<LocalDate>("Fødselsdato67m", fødselsdato.value.withDayOfMonth(1).plusYears(67).plusMonths(1))

    val harYtelseFør2021 = listOf(
        YtelseEnum.UT,
        YtelseEnum.GJP,
        YtelseEnum.UT_GJR,
        YtelseEnum.GJR
    ).associateWith {
        harYtelseFørDato(it, person.value.forsteVirkningsdatoGrunnlagListe, LocalDate.of(2021, 1, 1))
    }

    val trygdetid = kap20.hvis(
        ja = person.value.trygdetidK20.tt_fa_F2021,
        nei = person.value.trygdetidK19.tt_fa_F2021
    )

    val fødtFør1960OgTrygdetid20 = og(
        fødselår.erMindreEllerLik(1959),
        trygdetid.erStørreEllerLik(20)
    )

    return when {
        og(
            ytelse.erLik(YtelseEnum.AP),
            fødtFør1960OgTrygdetid20
        ).verdi() -> Faktum("Overgangsregel_AP", true)

        og(
            ytelse.erLik(YtelseEnum.AP),
            fødtFør1960OgTrygdetid20,
            virkningstidspunkt.erStørreEllerLik(dato67m),
            eller(
                harYtelseFør2021[YtelseEnum.UT]!!,
                harYtelseFør2021[YtelseEnum.GJP]!!
            )
        ).verdi() -> Faktum("Overgangsregel_AP_tidligereYtelse", true)

        og(
            ytelse.erLik(YtelseEnum.GJR),
            fødtFør1960OgTrygdetid20,
            virkningstidspunkt.erStørreEllerLik(dato67m),
            eller(
                harYtelseFør2021[YtelseEnum.UT_GJR]!!,
                harYtelseFør2021[YtelseEnum.GJR]!!
            )
        ).verdi() -> Faktum("Overgangsregel_GJR_tidligereUT_GJT", true)

        else -> Faktum("Ingen overgangsregel", false)
    }
}

fun unntakForutgående(
    unntak: Faktum<Boolean>?,
    unntakType: Faktum<UnntakEnum>?,
    tekst: String
): Faktum<Boolean> = listOf(
    FLYKT_ALDER, FLYKT_BARNEP, FLYKT_GJENLEV, FLYKT_UFOREP
).let { aktuelleUnntakstype ->
    og(
        unntak ?: Faktum(tekst, false),
        unntakType?.erIListen(aktuelleUnntakstype) ?: Faktum(tekst, false)
    )
}

fun angittFlyktning(person: Faktum<Person>): Faktum<Boolean> =
    eller(
        person.value.flyktning,
        unntakForutgående(
            person.value.inngangOgEksportgrunnlag?.unntakFraForutgaendeMedlemskap?.unntak,
            person.value.inngangOgEksportgrunnlag?.unntakFraForutgaendeMedlemskap?.unntakType,
            "InngangOgEksportgrunnlag/unntakFraForutgaendeMedlemskap er ikke oppgitt"
        ),
        unntakForutgående(
            person.value.inngangOgEksportgrunnlag?.unntakFraForutgaendeTT?.unntak,
            person.value.inngangOgEksportgrunnlag?.unntakFraForutgaendeTT?.unntakType,
            "InngangOgEksportgrunnlag/unntakFraForutgaendeTT er ikke oppgitt"
        )
    )

fun personErFlyktning(
    person: Faktum<Person>,
    ytelse: Faktum<YtelseEnum>,
    kap20: Faktum<Boolean>,
    virkningstidspunkt: Faktum<LocalDate>,
    kravlinjeFremsattDatoFom2021: Faktum<Boolean>
): Faktum<UtfallType> =
    angittFlyktning(person).hvis(
        nei = Faktum("Anvendt flyktning", UtfallType.IKKE_RELEVANT),
        ja = {
            kravlinjeFremsattDatoFom2021.hvis(
                nei = Faktum("Anvendt flyktning", UtfallType.OPPFYLT),
                ja = {
                    overgangsregler(person, ytelse, virkningstidspunkt, kap20).hvis(
                        nei = Faktum("Anvendt flyktning", UtfallType.IKKE_OPPFYLT),
                        ja = Faktum("Anvendt flyktning", UtfallType.OPPFYLT)
                    )
                }
            )
        }
    )

class BeregnAlderspensjonService2(
    private val request: Request,
) : AbstractDemoRuleService<Response>() {
    override val ruleService: () -> Response = {

        val output =
            BeregnAlderspensjonFlyt(
                request.person,
                Faktum("virkningstidspunkt", request.virkningstidspunkt)
            ).run(this)


        Response(
            anvendtTrygdetid = output.anvendtTrygdetid,
            grunnpensjon = output.grunnpensjon
        )
    }
}

