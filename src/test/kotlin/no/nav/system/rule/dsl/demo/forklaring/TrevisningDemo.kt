package no.nav.system.rule.dsl.demo.forklaring

import no.nav.system.rule.dsl.demo.domain.Boperiode
import no.nav.system.rule.dsl.demo.domain.Person
import no.nav.system.rule.dsl.demo.domain.Request
import no.nav.system.rule.dsl.demo.domain.koder.LandEnum
import no.nav.system.rule.dsl.demo.forklaring.usecases.beregnAlderspensjon
import no.nav.system.rule.dsl.demo.helper.localDate
import no.nav.system.rule.dsl.forklaring.treVisningKompakt
import no.nav.system.rule.dsl.rettsregel.Faktum

fun main() {
    val params = Request(
        virkningstidspunkt = localDate(2020, 1, 1),
        person = Person(
            id = 1,
            fødselsdato = Faktum("Fødselsdato", localDate(1980, 3, 3)),
            erGift = false,
            boperioder = listOf(
                Boperiode(fom = localDate(1990, 1, 1), tom = localDate(1998, 12, 31), LandEnum.NOR)
            )
        )
    )

    val netto = beregnAlderspensjon(params)

    println("=" * 80)
    println("TREVISNING KOMPAKT - beregnAlderspensjon")
    println("=" * 80)
    println()
    println(netto.treVisningKompakt())
    println()
    println("=" * 80)

    // Hent faktiskTrygdetidAr og vis dens struktur
    val grunnlagListe = netto.grunnlagListe()
    val faktiskTrygdetidAr = grunnlagListe.find { it.navn == "faktiskTrygdetidAr" }

    if (faktiskTrygdetidAr != null) {
        println()
        println("=" * 80)
        println("FAKTISK TRYGDETID ÅR - Detaljert trevisning")
        println("=" * 80)
        println()
        println("Grunnlag notasjon: ${faktiskTrygdetidAr.notasjon()}")
        println("Grunnlag verdi: ${faktiskTrygdetidAr.evaluer()}")
        println()
        println("Underliggende uttrykk:")
        println(faktiskTrygdetidAr.utpakk().treVisningKompakt())
        println()
        println("=" * 80)
    }
}

operator fun String.times(n: Int) = this.repeat(n)
