package no.nav.system.rule.dsl.demo.ruleset

import no.nav.system.rule.dsl.demo.domain.*
import no.nav.system.rule.dsl.demo.domain.koder.YtelseEnum
import no.nav.system.rule.dsl.demo.helper.localDate
import no.nav.system.rule.dsl.enums.MengdeKomparator.INGEN
import no.nav.system.rule.dsl.enums.MengdeKomparator.MINST_EN_AV
import no.nav.system.rule.dsl.enums.UtfallType.*
import no.nav.system.rule.dsl.rettsregel.Faktum
import no.nav.system.rule.dsl.rettsregel.MengdeSubsumsjon
import no.nav.system.rule.dsl.treevisitor.visitor.debug
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class PersonenErFlyktningRSTest {

    @Test
    fun testErIkkeFlyktning() {
        val person = Person(
            fødselsdato = Faktum("Fødselsdato", localDate(1980, 1, 1)),
            flyktning = Faktum("Angitt flyktning", false),
        )

        val flyktningUtfall = PersonenErFlyktningRS(
            person,
            Faktum("Ytelsestype", YtelseEnum.AP),
            Faktum("Kapittel20", false),
            Faktum("Virkningstidspunkt", localDate(2020, 1, 1)),
            Faktum("HarKravlinjeFremsattDatoFom2021", true)
        ).testAndDebug().get()

        assertEquals(IKKE_RELEVANT, flyktningUtfall.verdi)
        assertTrue(flyktningUtfall.children[0].fired())

        val flyktningSubsum = flyktningUtfall.children[0].children.first() as MengdeSubsumsjon
        assertEquals(INGEN, flyktningSubsum.komparator)
        assertTrue(flyktningSubsum.children.all { !it.fired() })
    }

    @Test
    fun testErFlyktning_virkFør2021() {
        val person = Person(
            fødselsdato = Faktum("Fødselsdato", localDate(1980, 1, 1)),
            flyktning = Faktum("Angitt flyktning", true),
        )

        val flyktningUtfall = PersonenErFlyktningRS(
            person,
            Faktum("Ytelsestype", YtelseEnum.AP),
            Faktum("Kapittel20", false),
            Faktum("Virkningstidspunkt", localDate(2020, 1, 1)),
            Faktum("HarKravlinjeFremsattDatoFom2021", false)
        ).testAndDebug().get()

        assertEquals(OPPFYLT, flyktningUtfall.verdi)
        assertEquals(1, flyktningUtfall.children[0].children[0].children.size)
        assertEquals(2, flyktningUtfall.children[0].children[1].children.size)
    }

    @Test
    fun testErIkkeFlyktning_virkFom2021_ikkeOvergang() {
        val person = Person(
            fødselsdato = Faktum("Fødselsdato", localDate(1980, 1, 1)),
            inngangOgEksportgrunnlag = InngangOgEksportgrunnlag().apply { unntakFraForutgaendeMedlemskap.unntak.verdi = true }
        )

        val flyktningUtfall = PersonenErFlyktningRS(
            person,
            Faktum("Ytelsestype", YtelseEnum.AP),
            Faktum("Kapittel20", false),
            Faktum("Virkningstidspunkt", localDate(2021, 1, 1)),
            Faktum("HarKravlinjeFremsattDatoFom2021", true)
        ).testAndDebug().get()

        println(flyktningUtfall.debug())

        val regelOvergangsregel_AP = flyktningUtfall.children[0].children[2].children[0]

        assertEquals(2, regelOvergangsregel_AP.children.size)
        assertEquals(IKKE_OPPFYLT, flyktningUtfall.verdi)
        assertTrue(flyktningUtfall.children[0].fired())
        assertEquals(3, flyktningUtfall.children[0].children.size)
    }

    @Test
    fun testErFlyktning_virkFom2021_Overgangsregel_AP() {
        val person = Person(
            fødselsdato = Faktum("Fødselsdato", localDate(1958, 12, 31)),
            flyktning = Faktum("Angitt flyktning", true),
            trygdetidK19 = Trygdetid().apply { tt_fa_F2021.verdi = 20 }
        )

        val flyktningUtfall = PersonenErFlyktningRS(
            person,
            Faktum("Ytelsestype", YtelseEnum.AP),
            Faktum("Kapittel20", false),
            Faktum("Virkningstidspunkt", localDate(2021, 1, 1)),
            Faktum("HarKravlinjeFremsattDatoFom2021", true)
        ).testAndDebug().get()

        assertEquals(OPPFYLT, flyktningUtfall.verdi)
        assertTrue(flyktningUtfall.children[0].fired())
        val overgangsregelTreSubsumsjon = flyktningUtfall.children[0].children[2] as MengdeSubsumsjon
        assertEquals(MINST_EN_AV, overgangsregelTreSubsumsjon.komparator)
        assertEquals(3, overgangsregelTreSubsumsjon.children.size)
        assertTrue(overgangsregelTreSubsumsjon.children[0].fired())
        assertFalse(overgangsregelTreSubsumsjon.children[1].fired())
        assertFalse(overgangsregelTreSubsumsjon.children[2].fired())
    }

    @Test
    fun testErFlyktning_virkFom2021_Overgangsregel_GJR_tidligereGJR() {
        val person = Person(
            fødselsdato = Faktum("Fødselsdato", localDate(1958, 12, 31)),
            flyktning = Faktum("Angitt flyktning", true),
            trygdetidK19 = Trygdetid().apply { tt_fa_F2021.verdi = 20 }
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
            Faktum("Ytelsestype", YtelseEnum.GJR),
            Faktum("Kapittel20", false),
            Faktum("Virkningstidspunkt", localDate(2027, 1, 1)),
            Faktum("HarKravlinjeFremsattDatoFom2021", true)
        ).testAndDebug().get()


        println(flyktningUtfall.debug())

        assertEquals(OPPFYLT, flyktningUtfall.verdi)
        assertTrue(flyktningUtfall.children[0].fired())
        val overgangsregelTreSubsumsjon = flyktningUtfall.children[0].children[2] as MengdeSubsumsjon
        assertEquals(MINST_EN_AV, overgangsregelTreSubsumsjon.komparator)
        assertEquals(2, overgangsregelTreSubsumsjon.children.size)
        assertFalse(overgangsregelTreSubsumsjon.children[0].fired())
        assertTrue(overgangsregelTreSubsumsjon.children[1].fired())
    }
}