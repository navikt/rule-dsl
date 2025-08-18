package no.nav.system.rule.dsl.demo.domain

import no.nav.system.rule.dsl.demo.domain.koder.LandEnum
import no.nav.system.rule.dsl.rettsregel.Faktum
import java.time.LocalDate

data class Person(
    val id: Int = 1,
    val rolle: String = "UKJENT",
    val fødselsdato: Faktum<LocalDate>,
    val erGift: Boolean = false,
    val boperioder: List<Boperiode> = mutableListOf(),
    val flyktning: Faktum<Boolean> = Faktum("Angitt flyktning", false),
    var trygdetidK19: Trygdetid = Trygdetid(5),
    var trygdetidK20: Trygdetid = Trygdetid(4),
    var inngangOgEksportgrunnlag: InngangOgEksportgrunnlag? = null,
    var forsteVirkningsdatoGrunnlagListe: MutableList<ForsteVirkningsdatoGrunnlag> = mutableListOf()
)

fun Faktum<Person>.boperioderLand(land: LandEnum) =
    this.value.boperioder.filter { it.land == land }.map { Faktum("boperiode $land", it)}