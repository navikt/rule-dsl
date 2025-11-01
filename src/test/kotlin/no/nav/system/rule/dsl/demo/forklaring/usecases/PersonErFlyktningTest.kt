package no.nav.system.rule.dsl.demo.forklaring.usecases

import no.nav.system.rule.dsl.demo.domain.ForsteVirkningsdatoGrunnlag
import no.nav.system.rule.dsl.demo.domain.InngangOgEksportgrunnlag
import no.nav.system.rule.dsl.demo.domain.Person
import no.nav.system.rule.dsl.demo.domain.Trygdetid
import no.nav.system.rule.dsl.demo.domain.koder.UtfallType
import no.nav.system.rule.dsl.demo.domain.koder.YtelseEnum
import no.nav.system.rule.dsl.demo.helper.localDate
import no.nav.system.rule.dsl.forklaring.Const
import no.nav.system.rule.dsl.forklaring.Grunnlag
import no.nav.system.rule.dsl.forklaring.forklarDetaljert
import no.nav.system.rule.dsl.forklaring.treVisning
import no.nav.system.rule.dsl.rettsregel.Faktum
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * Testklasse for personErFlyktning() funksjonen som bruker Uttrykk DSL.
 * Tilsvarer testene i PersonenErFlyktningRSTest.kt men for den nye Uttrykk-baserte implementasjonen.
 */
class PersonErFlyktningTest {

    @Test
    fun `Person er ikke flyktning`() {
        val person = Person(
            fødselsdato = Faktum("Fødselsdato", localDate(1980, 1, 1)),
            flyktning = Faktum("Angitt flyktning", false),
        )

        val resultat = personErFlyktning(
            persongrunnlag = person,
            ytelseType = Grunnlag("Ytelsestype", Const(YtelseEnum.AP)),
            erKapittel20 = Grunnlag("Kapittel20", Const(false)),
            virk = Grunnlag("Virkningstidspunkt", Const(localDate(2020, 1, 1))),
            kravlinjeFremsattDatoFom2021 = Grunnlag("HarKravlinjeFremsattDatoFom2021", Const(true))
        )

        Assertions.assertEquals(UtfallType.IKKE_RELEVANT, resultat.evaluer())

        println(resultat.forklarDetaljert(resultat.navn))
        println()
        println(resultat.treVisning())
    }

    @Test
    fun `Person er flyktning - virk før 2021`() {
        val person = Person(
            fødselsdato = Faktum("Fødselsdato", localDate(1980, 1, 1)),
            flyktning = Faktum("Angitt flyktning", true),
        )

        val resultat = personErFlyktning(
            persongrunnlag = person,
            ytelseType = Grunnlag("Ytelsestype", Const(YtelseEnum.AP)),
            erKapittel20 = Grunnlag("Kapittel20", Const(false)),
            virk = Grunnlag("Virkningstidspunkt", Const(localDate(2020, 1, 1))),
            kravlinjeFremsattDatoFom2021 = Grunnlag("HarKravlinjeFremsattDatoFom2021", Const(false))
        )

        Assertions.assertEquals(UtfallType.OPPFYLT, resultat.evaluer())

        println(resultat.forklarDetaljert(resultat.navn))
        println()
        println(resultat.treVisning())
    }

    @Test
    fun `Person er ikke flyktning - virk fom 2021 uten overgangsregel`() {
        val person = Person(
            fødselsdato = Faktum("Fødselsdato", localDate(1980, 1, 1)),
            inngangOgEksportgrunnlag = InngangOgEksportgrunnlag().apply {
                unntakFraForutgaendeMedlemskap.unntak.value = true
            }
        )

        val resultat = personErFlyktning(
            persongrunnlag = person,
            ytelseType = Grunnlag("Ytelsestype", Const(YtelseEnum.AP)),
            erKapittel20 = Grunnlag("Kapittel20", Const(false)),
            virk = Grunnlag("Virkningstidspunkt", Const(localDate(2021, 1, 1))),
            kravlinjeFremsattDatoFom2021 = Grunnlag("HarKravlinjeFremsattDatoFom2021", Const(true))
        )

        Assertions.assertEquals(UtfallType.IKKE_OPPFYLT, resultat.evaluer())

        println(resultat.forklarDetaljert(resultat.navn))
        println()
        println(resultat.treVisning())
    }

    @Test
    fun `Person er flyktning - virk fom 2021 med Overgangsregel AP`() {
        val person = Person(
            fødselsdato = Faktum("Fødselsdato", localDate(1958, 12, 31)),
            flyktning = Faktum("Angitt flyktning", true),
            trygdetidK19 = Trygdetid().apply { tt_fa_F2021.value = 20 }
        )

        val resultat = personErFlyktning(
            persongrunnlag = person,
            ytelseType = Grunnlag("Ytelsestype", Const(YtelseEnum.AP)),
            erKapittel20 = Grunnlag("Kapittel20", Const(false)),
            virk = Grunnlag("Virkningstidspunkt", Const(localDate(2021, 1, 1))),
            kravlinjeFremsattDatoFom2021 = Grunnlag("HarKravlinjeFremsattDatoFom2021", Const(true))
        )

        Assertions.assertEquals(UtfallType.OPPFYLT, resultat.evaluer())

        println(resultat.forklarDetaljert(resultat.navn))
        println()
        println(resultat.treVisning())
    }

    @Test
    fun `Person er flyktning - virk fom 2021 med Overgangsregel GJR tidligereGJR`() {
        val person = Person(
            fødselsdato = Faktum("Fødselsdato", localDate(1958, 12, 31)),
            flyktning = Faktum("Angitt flyktning", true),
            trygdetidK19 = Trygdetid().apply { tt_fa_F2021.value = 20 }
        ).apply {
            this.forsteVirkningsdatoGrunnlagListe.add(
                ForsteVirkningsdatoGrunnlag(
                    localDate(2020, 1, 1),
                    YtelseEnum.GJR
                )
            )
        }

        val resultat = personErFlyktning(
            persongrunnlag = person,
            ytelseType = Grunnlag("Ytelsestype", Const(YtelseEnum.GJR)),
            erKapittel20 = Grunnlag("Kapittel20", Const(false)),
            virk = Grunnlag("Virkningstidspunkt", Const(localDate(2027, 1, 1))),
            kravlinjeFremsattDatoFom2021 = Grunnlag("HarKravlinjeFremsattDatoFom2021", Const(true))
        )

        Assertions.assertEquals(UtfallType.OPPFYLT, resultat.evaluer())

        println(resultat.forklarDetaljert(resultat.navn))
        println()
        println(resultat.treVisning())
    }

    @Test
    fun `Person har unntak fra forutgående medlemskap med flyktningtype`() {
        val person = Person(
            fødselsdato = Faktum("Fødselsdato", localDate(1980, 1, 1)),
            flyktning = Faktum("Angitt flyktning", true)
        )

        val resultat = personErFlyktning(
            persongrunnlag = person,
            ytelseType = Grunnlag("Ytelsestype", Const(YtelseEnum.AP)),
            erKapittel20 = Grunnlag("Kapittel20", Const(false)),
            virk = Grunnlag("Virkningstidspunkt", Const(localDate(2020, 1, 1))),
            kravlinjeFremsattDatoFom2021 = Grunnlag("HarKravlinjeFremsattDatoFom2021", Const(false))
        )

        Assertions.assertEquals(UtfallType.OPPFYLT, resultat.evaluer())

        println(resultat.forklarDetaljert(resultat.navn))
        println()
        println(resultat.treVisning())
    }

    @Test
    fun `Person har unntak fra forutgående TT med flyktningtype`() {
        val person = Person(
            fødselsdato = Faktum("Fødselsdato", localDate(1980, 1, 1)),
            flyktning = Faktum("Angitt flyktning", true)
        )

        val resultat = personErFlyktning(
            persongrunnlag = person,
            ytelseType = Grunnlag("Ytelsestype", Const(YtelseEnum.AP)),
            erKapittel20 = Grunnlag("Kapittel20", Const(false)),
            virk = Grunnlag("Virkningstidspunkt", Const(localDate(2020, 1, 1))),
            kravlinjeFremsattDatoFom2021 = Grunnlag("HarKravlinjeFremsattDatoFom2021", Const(false))
        )

        Assertions.assertEquals(UtfallType.OPPFYLT, resultat.evaluer())

        println(resultat.forklarDetaljert(resultat.navn))
        println()
        println(resultat.treVisning())
    }

    @Test
    fun `Person født 1959 med trygdetid 20 oppfyller overgangsregel AP`() {
        val person = Person(
            fødselsdato = Faktum("Fødselsdato", localDate(1959, 1, 1)),
            flyktning = Faktum("Angitt flyktning", true),
            trygdetidK19 = Trygdetid().apply { tt_fa_F2021.value = 20 }
        )

        val resultat = personErFlyktning(
            persongrunnlag = person,
            ytelseType = Grunnlag("Ytelsestype", Const(YtelseEnum.AP)),
            erKapittel20 = Grunnlag("Kapittel20", Const(false)),
            virk = Grunnlag("Virkningstidspunkt", Const(localDate(2021, 1, 1))),
            kravlinjeFremsattDatoFom2021 = Grunnlag("HarKravlinjeFremsattDatoFom2021", Const(true))
        )

        Assertions.assertEquals(UtfallType.OPPFYLT, resultat.evaluer())

        println(resultat.forklarDetaljert(resultat.navn))
        println()
        println(resultat.treVisning())
    }

    @Test
    fun `Person født 1960 med trygdetid 20 oppfyller ikke overgangsregel AP`() {
        val person = Person(
            fødselsdato = Faktum("Fødselsdato", localDate(1960, 1, 1)),
            flyktning = Faktum("Angitt flyktning", true),
            trygdetidK19 = Trygdetid().apply { tt_fa_F2021.value = 20 }
        )

        val resultat = personErFlyktning(
            persongrunnlag = person,
            ytelseType = Grunnlag("Ytelsestype", Const(YtelseEnum.AP)),
            erKapittel20 = Grunnlag("Kapittel20", Const(false)),
            virk = Grunnlag("Virkningstidspunkt", Const(localDate(2021, 1, 1))),
            kravlinjeFremsattDatoFom2021 = Grunnlag("HarKravlinjeFremsattDatoFom2021", Const(true))
        )

        Assertions.assertEquals(UtfallType.IKKE_OPPFYLT, resultat.evaluer())

        println(resultat.forklarDetaljert(resultat.navn))
        println()
        println(resultat.treVisning())
    }

    @Test
    fun `Person født 1959 med trygdetid 19 oppfyller ikke overgangsregel AP`() {
        val person = Person(
            fødselsdato = Faktum("Fødselsdato", localDate(1959, 1, 1)),
            flyktning = Faktum("Angitt flyktning", true),
            trygdetidK19 = Trygdetid().apply { tt_fa_F2021.value = 19 }
        )

        val resultat = personErFlyktning(
            persongrunnlag = person,
            ytelseType = Grunnlag("Ytelsestype", Const(YtelseEnum.AP)),
            erKapittel20 = Grunnlag("Kapittel20", Const(false)),
            virk = Grunnlag("Virkningstidspunkt", Const(localDate(2021, 1, 1))),
            kravlinjeFremsattDatoFom2021 = Grunnlag("HarKravlinjeFremsattDatoFom2021", Const(true))
        )

        Assertions.assertEquals(UtfallType.IKKE_OPPFYLT, resultat.evaluer())

        println(resultat.forklarDetaljert(resultat.navn))
        println()
        println(resultat.treVisning())
    }

    @Test
    fun `Person med UT før 2021 oppfyller overgangsregel AP tidligereUT`() {
        val person = Person(
            fødselsdato = Faktum("Fødselsdato", localDate(1957, 6, 1)),
            flyktning = Faktum("Angitt flyktning", true),
            trygdetidK19 = Trygdetid().apply { tt_fa_F2021.value = 20 }
        ).apply {
            this.forsteVirkningsdatoGrunnlagListe.add(
                ForsteVirkningsdatoGrunnlag(
                    localDate(2020, 1, 1),
                    YtelseEnum.UT
                )
            )
        }

        val resultat = personErFlyktning(
            persongrunnlag = person,
            ytelseType = Grunnlag("Ytelsestype", Const(YtelseEnum.AP)),
            erKapittel20 = Grunnlag("Kapittel20", Const(false)),
            virk = Grunnlag("Virkningstidspunkt", Const(localDate(2024, 8, 1))),
            kravlinjeFremsattDatoFom2021 = Grunnlag("HarKravlinjeFremsattDatoFom2021", Const(true))
        )

        Assertions.assertEquals(UtfallType.OPPFYLT, resultat.evaluer())

        println(resultat.forklarDetaljert(resultat.navn))
        println()
        println(resultat.treVisning())
    }

    @Test
    fun `Person med GJP før 2021 oppfyller overgangsregel AP tidligereGJP`() {
        val person = Person(
            fødselsdato = Faktum("Fødselsdato", localDate(1957, 6, 1)),
            flyktning = Faktum("Angitt flyktning", true),
            trygdetidK19 = Trygdetid().apply { tt_fa_F2021.value = 20 }
        ).apply {
            this.forsteVirkningsdatoGrunnlagListe.add(
                ForsteVirkningsdatoGrunnlag(
                    localDate(2020, 1, 1),
                    YtelseEnum.GJP
                )
            )
        }

        val resultat = personErFlyktning(
            persongrunnlag = person,
            ytelseType = Grunnlag("Ytelsestype", Const(YtelseEnum.AP)),
            erKapittel20 = Grunnlag("Kapittel20", Const(false)),
            virk = Grunnlag("Virkningstidspunkt", Const(localDate(2024, 8, 1))),
            kravlinjeFremsattDatoFom2021 = Grunnlag("HarKravlinjeFremsattDatoFom2021", Const(true))
        )

        Assertions.assertEquals(UtfallType.OPPFYLT, resultat.evaluer())

        println(resultat.forklarDetaljert(resultat.navn))
        println()
        println(resultat.treVisning())
    }

    @Test
    fun `Person med UT_GJR før 2021 oppfyller overgangsregel GJR tidligereUT_GJT`() {
        val person = Person(
            fødselsdato = Faktum("Fødselsdato", localDate(1957, 6, 1)),
            flyktning = Faktum("Angitt flyktning", true),
            trygdetidK19 = Trygdetid().apply { tt_fa_F2021.value = 20 }
        ).apply {
            this.forsteVirkningsdatoGrunnlagListe.add(
                ForsteVirkningsdatoGrunnlag(
                    localDate(2020, 1, 1),
                    YtelseEnum.UT_GJR
                )
            )
        }

        val resultat = personErFlyktning(
            persongrunnlag = person,
            ytelseType = Grunnlag("Ytelsestype", Const(YtelseEnum.GJR)),
            erKapittel20 = Grunnlag("Kapittel20", Const(false)),
            virk = Grunnlag("Virkningstidspunkt", Const(localDate(2024, 8, 1))),
            kravlinjeFremsattDatoFom2021 = Grunnlag("HarKravlinjeFremsattDatoFom2021", Const(true))
        )

        Assertions.assertEquals(UtfallType.OPPFYLT, resultat.evaluer())

        println(resultat.forklarDetaljert(resultat.navn))
        println()
        println(resultat.treVisning())
    }

    @Test
    fun `Bruker kapittel 20 trygdetid når erKapittel20 er true`() {
        val person = Person(
            fødselsdato = Faktum("Fødselsdato", localDate(1959, 1, 1)),
            flyktning = Faktum("Angitt flyktning", true),
            trygdetidK19 = Trygdetid().apply { tt_fa_F2021.value = 15 },
            trygdetidK20 = Trygdetid().apply { tt_fa_F2021.value = 25 }
        )

        val resultat = personErFlyktning(
            persongrunnlag = person,
            ytelseType = Grunnlag("Ytelsestype", Const(YtelseEnum.AP)),
            erKapittel20 = Grunnlag("Kapittel20", Const(true)),
            virk = Grunnlag("Virkningstidspunkt", Const(localDate(2021, 1, 1))),
            kravlinjeFremsattDatoFom2021 = Grunnlag("HarKravlinjeFremsattDatoFom2021", Const(true))
        )

        // Skal bruke K20 trygdetid (25) og dermed oppfylle overgangsregel
        Assertions.assertEquals(UtfallType.OPPFYLT, resultat.evaluer())

        println(resultat.forklarDetaljert(resultat.navn))
        println()
        println(resultat.treVisning())
    }
}