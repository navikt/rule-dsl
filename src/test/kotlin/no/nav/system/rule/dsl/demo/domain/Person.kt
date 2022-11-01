package no.nav.system.rule.dsl.demo.domain

import no.nav.system.rule.dsl.rettsregel.Fact
import java.time.LocalDate

data class Person(
    val id: Int = 1,
    val rolle: String = "UKJENT",
    val fødselsdato: Fact<LocalDate>,
    val erGift: Boolean = false,
    val boperioder: List<Boperiode> = mutableListOf(),
    val flyktning: Fact<Boolean> = Fact("Angitt flyktning", false),
    var trygdetidK19: Trygdetid = Trygdetid(5),
    var trygdetidK20: Trygdetid = Trygdetid(4),
    var inngangOgEksportgrunnlag: InngangOgEksportgrunnlag? = null,
    var forsteVirkningsdatoGrunnlagListe: MutableList<ForsteVirkningsdatoGrunnlag> = mutableListOf()
)