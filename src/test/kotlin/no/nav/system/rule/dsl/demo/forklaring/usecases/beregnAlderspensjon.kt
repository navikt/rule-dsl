package no.nav.system.rule.dsl.demo.forklaring.usecases

import no.nav.system.rule.dsl.demo.domain.Boperiode
import no.nav.system.rule.dsl.demo.domain.Person
import no.nav.system.rule.dsl.demo.domain.Request
import no.nav.system.rule.dsl.demo.domain.koder.LandEnum
import no.nav.system.rule.dsl.demo.domain.koder.UtfallType
import no.nav.system.rule.dsl.demo.domain.koder.YtelseEnum
import no.nav.system.rule.dsl.demo.helper.localDate
import no.nav.system.rule.dsl.forklaring.*
import no.nav.system.rule.dsl.rettsregel.Faktum
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

fun main() {

    val params = Request(
        virkningstidspunkt = localDate(2020, 1, 1), person = Person(
            id = 1, fødselsdato = Faktum("Fødselsdato", localDate(1980, 3, 3)), erGift = false, boperioder = listOf(
                Boperiode(fom = localDate(1990, 1, 1), tom = localDate(1998, 12, 31), LandEnum.NOR)
            )
        )
    )

    beregnAlderspensjon(params).also { netto ->
        println()
        println("Detaljert forklaring: ${netto.navn}")
        println(netto.forklarDetaljert(netto.navn, maxDybde = 3))

            println()
            println("Strukturtre:")
            println(netto.treVisning())

            // Print call trace på slutten
            CallTracker.printTrace()
    }

}

fun beregnAlderspensjon(
    request: Request
) = tracked {

    val flykningUtfall = personErFlyktning(
        persongrunnlag = request.person,
        ytelseType = Grunnlag("Ytelsetype", Const(YtelseEnum.AP)),
        erKapittel20 = Grunnlag("erKapittel20", Const(false)),
        virk = Grunnlag("virkningstidspunkt", Const(request.virkningstidspunkt)),
        kravlinjeFremsattDatoFom2021 = Grunnlag("kravlinjeFremsattDatoFom2021", Const(true))
    )

    val faktiskTrygdetidAr = beregnFaktiskTrygdetidAr(
        fødselsdato = request.person.fødselsdato.toGrunnlag(),
        boperiodeListe = request.person.boperioder,
        flykningUtfall = flykningUtfall
    )

    val sivilstandSats = Const(request.person.erGift).navngi("erGift").let { erGift ->
        erGift
            .så { Const(0.9) }
            .ellers { Const(1.0) }
    }.navngi("sivilstandSats")

    val trygdetidsFaktor = (faktiskTrygdetidAr / maksTrygdetidAr()).navngi("trygdetidFaktor")

    (grunnbelop() * sivilstandSats * trygdetidsFaktor).navngi("netto")
}

fun grunnbelop() = tracked {Const(120_000.0).navngi("grunnbelop") }
fun maksTrygdetidAr() = tracked { Const(40).navngi("maksTrygdetidAr") }

fun beregnFaktiskTrygdetidAr(
    fødselsdato: Grunnlag<LocalDate>,
    boperiodeListe: List<Boperiode>,
    flykningUtfall: Grunnlag<UtfallType>
) = tracked {
    tabell("trygdetidAr") {

        val faktiskTrygdetidIMåneder = akkumulerBotidIMånederNorge(fødselsdato, boperiodeListe)

        regel {
            når { flykningUtfall erLik UtfallType.OPPFYLT }
            resultat { maksTrygdetidAr() }
        }

        regel {
            når { flykningUtfall erUlik UtfallType.OPPFYLT }
            resultat { Const((faktiskTrygdetidIMåneder / Const(12)).evaluer().roundToInt()) }
        }
    }.navngi("faktiskTrygdetidAr")
}

fun akkumulerBotidIMånederNorge(
    fødselsdato: Grunnlag<LocalDate>,
    boperiodeListe: List<Boperiode>,
) = tracked {
    Grunnlag("dato16år", Const(fødselsdato.evaluer().plusYears(16))).let { dato16år ->
        Grunnlag(
            "norske boperioder",
            Const(boperiodeListe.filter { it.land == LandEnum.NOR })
        ).let { norskeBoperioder ->

            val monthsBetween: (LocalDate, LocalDate?) -> Const<Int> = { fom, tom ->
                Const(ChronoUnit.MONTHS.between(fom, tom).toInt())
            }

            Grunnlag(
                "faktiskTrygdetidMåneder",
                norskeBoperioder.evaluer().fold(Const(0) as Uttrykk<Int>) { acc, periode ->
                    val fom = Grunnlag("fom", Const(periode.fom))
                    acc +
                            (fom erMindreEnn dato16år)
                                .så { monthsBetween(dato16år.evaluer(), periode.tom) }
                                .ellers { Const(0) } +
                            (fom erStørreEllerLik dato16år)
                                .så { monthsBetween(periode.fom, periode.tom) }
                                .ellers { Const(0) }
                }
            )
        }
    }.navngi("akkumulertBotidMånederINorge")
}

fun erFremtidigTrygdetidRedusert(
    fødselsdato: Grunnlag<LocalDate>,
    virkningstidspunkt: Grunnlag<LocalDate>,
    boperiodeListe: List<Boperiode>,
) = tracked {
    akkumulerBotidIMånederNorge(fødselsdato, boperiodeListe).let { faktiskTrygdetidIMåneder ->

        val dato1991 = Grunnlag("januar 1991", Const(localDate(1991, 1, 1)))
        val fireFemtedelsKrav = Grunnlag("fireFemtedelskrav", Const(480))

        ((virkningstidspunkt erStørreEllerLik dato1991) og (faktiskTrygdetidIMåneder erMindreEnn fireFemtedelsKrav))
            .så { Const(UtfallType.OPPFYLT) }
            .ellers { Const(UtfallType.IKKE_OPPFYLT) }
    }.navngi("erFremtidigTrygdetidRedusert")
}





