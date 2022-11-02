package no.nav.system.rule.dsl.demo.ruleset

import no.nav.system.rule.dsl.demo.domain.*
import no.nav.system.rule.dsl.demo.domain.koder.YtelseEnum
import no.nav.system.rule.dsl.demo.helper.localDate
import no.nav.system.rule.dsl.enums.ListComparator.INGEN
import no.nav.system.rule.dsl.enums.ListComparator.MINST_EN_AV
import no.nav.system.rule.dsl.demo.domain.koder.UtfallType.*
import no.nav.system.rule.dsl.rettsregel.Fact
import no.nav.system.rule.dsl.rettsregel.ListSubsumtion
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PersonenErFlyktningRSTest {

    @Test
    fun testErIkkeFlyktning() {
        val person = Person(
            fødselsdato = Fact("Fødselsdato", localDate(1980, 1, 1)),
            flyktning = Fact("Angitt flyktning", false),
        )

        val flyktningUtfall = PersonenErFlyktningRS(
            person,
            Fact("Ytelsestype", YtelseEnum.AP),
            Fact("Kapittel20", false),
            Fact("Virkningstidspunkt", localDate(2020, 1, 1)),
            Fact("HarKravlinjeFremsattDatoFom2021", true)
        ).test().get()

        assertEquals(IKKE_RELEVANT, flyktningUtfall.value)
        assertTrue(flyktningUtfall.children[0].fired())

        val flyktningSubsum = flyktningUtfall.children[0].children.first() as ListSubsumtion
        assertEquals(INGEN, flyktningSubsum.comparator)
        assertTrue(flyktningSubsum.children.all { !it.fired() })
    }

    @Test
    fun testErFlyktning_virkFør2021() {
        val person = Person(
            fødselsdato = Fact("Fødselsdato", localDate(1980, 1, 1)),
            flyktning = Fact("Angitt flyktning", true),
        )

        val flyktningUtfall = PersonenErFlyktningRS(
            person,
            Fact("Ytelsestype", YtelseEnum.AP),
            Fact("Kapittel20", false),
            Fact("Virkningstidspunkt", localDate(2020, 1, 1)),
            Fact("HarKravlinjeFremsattDatoFom2021", false)
        ).test().get()

        assertEquals(OPPFYLT, flyktningUtfall.value)
        assertEquals(1, flyktningUtfall.children[0].children[0].children.size)
        assertEquals(2, flyktningUtfall.children[0].children[1].children.size)
    }

    @Test
    fun testErIkkeFlyktning_virkFom2021_ikkeOvergang() {
        val person = Person(
            fødselsdato = Fact("Fødselsdato", localDate(1980, 1, 1)),
            inngangOgEksportgrunnlag = InngangOgEksportgrunnlag().apply { unntakFraForutgaendeMedlemskap.unntak.value = true }
        )

        val flyktningUtfall = PersonenErFlyktningRS(
            person,
            Fact("Ytelsestype", YtelseEnum.AP),
            Fact("Kapittel20", false),
            Fact("Virkningstidspunkt", localDate(2021, 1, 1)),
            Fact("HarKravlinjeFremsattDatoFom2021", true)
        ).test().get()

        val regelOvergangsregelAP = flyktningUtfall.children[0].children[2].children[0]

        assertEquals(2, regelOvergangsregelAP.children.size)
        assertEquals(IKKE_OPPFYLT, flyktningUtfall.value)
        assertTrue(flyktningUtfall.children[0].fired())
        assertEquals(3, flyktningUtfall.children[0].children.size)
    }

    @Test
    fun testErFlyktning_virkFom2021_Overgangsregel_AP() {
        val person = Person(
            fødselsdato = Fact("Fødselsdato", localDate(1958, 12, 31)),
            flyktning = Fact("Angitt flyktning", true),
            trygdetidK19 = Trygdetid().apply { tt_fa_F2021.value = 20 }
        )

        val flyktningUtfall = PersonenErFlyktningRS(
            person,
            Fact("Ytelsestype", YtelseEnum.AP),
            Fact("Kapittel20", false),
            Fact("Virkningstidspunkt", localDate(2021, 1, 1)),
            Fact("HarKravlinjeFremsattDatoFom2021", true)
        ).test().get()

        assertEquals(OPPFYLT, flyktningUtfall.value)
        assertTrue(flyktningUtfall.children[0].fired())
        val overgangsregelTreSubsumsjon = flyktningUtfall.children[0].children[2] as ListSubsumtion
        assertEquals(MINST_EN_AV, overgangsregelTreSubsumsjon.comparator)
        assertEquals(3, overgangsregelTreSubsumsjon.children.size)
        assertTrue(overgangsregelTreSubsumsjon.children[0].fired())
        assertFalse(overgangsregelTreSubsumsjon.children[1].fired())
        assertFalse(overgangsregelTreSubsumsjon.children[2].fired())
    }

    @Test
    fun testErFlyktning_virkFom2021_Overgangsregel_GJR_tidligereGJR() {
        val person = Person(
            fødselsdato = Fact("Fødselsdato", localDate(1958, 12, 31)),
            flyktning = Fact("Angitt flyktning", true),
            trygdetidK19 = Trygdetid().apply { tt_fa_F2021.value = 20 }
        ).apply {
            this.forsteVirkningsdatoGrunnlagListe.add(
                ForsteVirkningsdatoGrunnlag(
                    localDate(2020, 1, 1),
                    YtelseEnum.GJR
                )
            )
        }

        val flyktningUtfall = PersonenErFlyktningRS(
            person,
            Fact("Ytelsestype", YtelseEnum.GJR),
            Fact("Kapittel20", false),
            Fact("Virkningstidspunkt", localDate(2027, 1, 1)),
            Fact("HarKravlinjeFremsattDatoFom2021", true)
        ).test().get()

        assertEquals(OPPFYLT, flyktningUtfall.value)
        assertTrue(flyktningUtfall.children[0].fired())
        val overgangsregelTreSubsumsjon = flyktningUtfall.children[0].children[2] as ListSubsumtion
        assertEquals(MINST_EN_AV, overgangsregelTreSubsumsjon.comparator)
        assertEquals(2, overgangsregelTreSubsumsjon.children.size)
        assertFalse(overgangsregelTreSubsumsjon.children[0].fired())
        assertTrue(overgangsregelTreSubsumsjon.children[1].fired())
    }
}