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
        println(netto.treVisningKompakt())

        CallTracker.printTrace()
    }

}

fun beregnAlderspensjon(
    request: Request
) = tracked {

    val flykningUtfall = personErFlyktning(
        persongrunnlag = request.person,
        ytelseType = Grunnlag("Ytelsetype", Const(YtelseEnum.AP)),
        erKapittel20 = Grunnlag("erKapittel20", false),
        virk = Grunnlag("virkningstidspunkt", request.virkningstidspunkt),
        kravlinjeFremsattDatoFom2021 = Grunnlag("kravlinjeFremsattDatoFom2021", true)
    )

    val faktiskTrygdetidAr = (flykningUtfall erLik UtfallType.OPPFYLT)
        .så { maksTrygdetidAr() }
        .ellers {
            val akkumulertBotidIArNorge = akkumulerBotidIMånederNorge(
                request.person.fødselsdato.toGrunnlag(),
                request.person.boperioder
            ) / 12

            Const(akkumulertBotidIArNorge.evaluer().roundToInt())
        }
        .navngi("faktiskTrygdetidAr")

    val trygdetidsFaktor = (faktiskTrygdetidAr / maksTrygdetidAr()).navngi("trygdetidFaktor")

    val sivilstandSats = Grunnlag("erGift",request.person.erGift)
            .så { Const(0.9) }
            .ellers { Const(1.0) }
            .navngi("sivilstandSats")

    (grunnbelop() * sivilstandSats * trygdetidsFaktor).navngi("netto")
}

fun grunnbelop() = tracked { Const(120_000.0).navngi("grunnbelop") }
fun maksTrygdetidAr() = tracked { Const(40).navngi("maksTrygdetidAr") }

fun akkumulerBotidIMånederNorge(
    fødselsdato: Grunnlag<LocalDate>,
    boperiodeListe: List<Boperiode>,
) = tracked {

    val dato16år = Const(fødselsdato.evaluer().plusYears(16)).navngi("dato16år")

    val norskeBoperioder =
        Const(boperiodeListe.filter { it.land == LandEnum.NOR }).navngi("norskeBoperioder")

    val monthsBetween: (LocalDate, LocalDate?) -> Const<Int> = { fom, tom ->
        Const(ChronoUnit.MONTHS.between(fom, tom).toInt())
    }

    val norskBotidRef16årMåneder = norskeBoperioder.evaluer().map {
        tabell("periodeRef16år") {
            regel {
                når { it.fom erMindreEnn dato16år }
                resultat { monthsBetween(dato16år.evaluer(), it.tom) }
            }
            regel {
                når { it.fom erStørreEllerLik dato16år }
                resultat { monthsBetween(it.fom, it.tom) }
            }
            ellers { Const(0) }
        }.evaluer()
    }

    Const(norskBotidRef16årMåneder.sum()).navngi("akkumulertBotidMånederINorge")
}

fun erFremtidigTrygdetidRedusert(
    fødselsdato: Grunnlag<LocalDate>,
    virkningstidspunkt: Grunnlag<LocalDate>,
    boperiodeListe: List<Boperiode>,
) = tracked {
    akkumulerBotidIMånederNorge(fødselsdato, boperiodeListe).let { faktiskTrygdetidIMåneder ->

        val dato1991 = Grunnlag("januar 1991", localDate(1991, 1, 1))
        val fireFemtedelsKrav = Grunnlag("fireFemtedelskrav", 480)

        ((virkningstidspunkt erStørreEllerLik dato1991) og (faktiskTrygdetidIMåneder erMindreEnn fireFemtedelsKrav))
            .så { Const(UtfallType.OPPFYLT) }
            .ellers { Const(UtfallType.IKKE_OPPFYLT) }
    }.navngi("erFremtidigTrygdetidRedusert")
}





