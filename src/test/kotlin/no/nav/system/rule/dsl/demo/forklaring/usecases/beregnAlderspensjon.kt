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
            (akkumulerBotidIMånederNorge(
                request.person.fødselsdato.toGrunnlag(),
                request.person.boperioder
            ) / 12).avrund()
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

    val norskeBoperioder = boperiodeListe.filter { it.land == LandEnum.NOR }

    val monthsBetween: (LocalDate, LocalDate?) -> Grunnlag<Int> = { fom, tom ->
        Const(ChronoUnit.MONTHS.between(fom, tom).toInt()).navngi("måneder(${fom} til ${tom})")
    }

    val norskBotidRef16årMåneder = norskeBoperioder.mapIndexed { index, periode ->
        tabell("periode${index + 1}") {
            regel {
                når { periode.fom erMindreEnn dato16år }
                resultat { monthsBetween(dato16år.evaluer(), periode.tom) }
            }
            regel {
                når { periode.fom erStørreEllerLik dato16år }
                resultat { monthsBetween(periode.fom, periode.tom) }
            }
            ellers { Grunnlag("måneder(ingen)", 0) }
        }
    }

    summerAlle(*norskBotidRef16årMåneder.toTypedArray()).navngi("akkumulertBotidMånederINorge")
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





