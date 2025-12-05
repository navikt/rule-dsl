package no.nav.pensjon.regler.alderspensjon.ruleset

import no.nav.pensjon.regler.alderspensjon.domain.ForsteVirkningsdatoGrunnlag
import no.nav.pensjon.regler.alderspensjon.domain.InngangOgEksportgrunnlag
import no.nav.pensjon.regler.alderspensjon.domain.Person
import no.nav.pensjon.regler.alderspensjon.domain.Trygdetid
import no.nav.pensjon.regler.alderspensjon.domain.koder.UtfallType
import no.nav.pensjon.regler.alderspensjon.domain.koder.YtelseEnum
import no.nav.pensjon.regler.alderspensjon.ruleset.PersonenErFlyktningRS
import no.nav.system.ruledsl.core.model.Faktum
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.temporal.TemporalQueries.localDate

class PersonenErFlyktningRSTest {

    @Test
    fun testErIkkeFlyktning() {
        val person = Person(
            fødselsdato = Faktum("Fødselsdato", LocalDate.of(1980, 1, 1)),
            flyktning = Faktum("Angitt flyktning", false),
        )

        val flyktningUtfall = PersonenErFlyktningRS(
            person,
            Faktum("Ytelsestype", YtelseEnum.AP),
            Faktum("Kapittel20", false),
            Faktum("Virkningstidspunkt", LocalDate.of(2020, 1, 1)),
            Faktum("HarKravlinjeFremsattDatoFom2021", true)
        ).run {
            test()
            this.returnValue
        }

        assertEquals(UtfallType.IKKE_RELEVANT, flyktningUtfall.verdi)
//        assertTrue(flyktningUtfall.children[0].fired())

//        val flyktningSubsum = flyktningUtfall.children[0].children.first() as ListSubsumtion
//        assertEquals(INGEN, flyktningSubsum.comparator)
//        assertTrue(flyktningSubsum.children.all { !it.fired() })
    }

    @Test
    fun testErFlyktning_virkFør2021() {
        val person = Person(
            fødselsdato = Faktum("Fødselsdato", LocalDate.of(1980, 1, 1)),
            flyktning = Faktum("Angitt flyktning", true),
        )

        val flyktningUtfall = PersonenErFlyktningRS(
            person,
            Faktum("Ytelsestype", YtelseEnum.AP),
            Faktum("Kapittel20", false),
            Faktum("Virkningstidspunkt", LocalDate.of(2020, 1, 1)),
            Faktum("HarKravlinjeFremsattDatoFom2021", false)
        ).run {
            test()
            this.returnValue
        }

        assertEquals(UtfallType.OPPFYLT, flyktningUtfall.verdi)
//        assertEquals(1, flyktningUtfall.children[0].children[0].children.size)
//        assertEquals(1, flyktningUtfall.children[0].children[1].children.size)
    }

    @Test
    fun testErIkkeFlyktning_virkFom2021_ikkeOvergang() {
        val person = Person(
            fødselsdato = Faktum("Fødselsdato", LocalDate.of(1980, 1, 1)),
            inngangOgEksportgrunnlag = InngangOgEksportgrunnlag().apply {
                unntakFraForutgaendeMedlemskap.unntak = Faktum("unntak", true)
            }
        )

        val flyktningUtfall = PersonenErFlyktningRS(
            person,
            Faktum("Ytelsestype", YtelseEnum.AP),
            Faktum("Kapittel20", false),
            Faktum("Virkningstidspunkt", LocalDate.of(2021, 1, 1)),
            Faktum("HarKravlinjeFremsattDatoFom2021", true)
        ).run {
            test()
            this.returnValue
        }

//        val regelOvergangsregelAP = flyktningUtfall.children[0].children[2].children[0]

//        assertEquals(2, regelOvergangsregelAP.children.size)
        assertEquals(UtfallType.IKKE_OPPFYLT, flyktningUtfall.verdi)
//        assertTrue(flyktningUtfall.children[0].fired())
//        assertEquals(3, flyktningUtfall.children[0].children.size)
    }

    @Test
    fun testErFlyktning_virkFom2021_Overgangsregel_AP() {
        val person = Person(
            fødselsdato = Faktum("Fødselsdato", LocalDate.of(1958, 12, 31)),
            flyktning = Faktum("Angitt flyktning", true),
            trygdetidK19 = Trygdetid().apply { tt_fa_F2021 = Faktum(tt_fa_F2021.navn, 20) }
        )

        val flyktningUtfall = PersonenErFlyktningRS(
            person,
            Faktum("Ytelsestype", YtelseEnum.AP),
            Faktum("Kapittel20", false),
            Faktum("Virkningstidspunkt", LocalDate.of(2021, 1, 1)),
            Faktum("HarKravlinjeFremsattDatoFom2021", true)
        ).run {
            test()
            this.returnValue
        }

        assertEquals(UtfallType.OPPFYLT, flyktningUtfall.verdi)
//        assertTrue(flyktningUtfall.children[0].fired())
//        val overgangsregelTreSubsumsjon = flyktningUtfall.children[0].children[2] as ListSubsumtion
//        assertEquals(MINST_EN_AV, overgangsregelTreSubsumsjon.comparator)
//        assertEquals(3, overgangsregelTreSubsumsjon.children.size)
//        assertTrue(overgangsregelTreSubsumsjon.children[0].fired())
//        assertFalse(overgangsregelTreSubsumsjon.children[1].fired())
//        assertFalse(overgangsregelTreSubsumsjon.children[2].fired())
    }

    @Test
    fun testErFlyktning_virkFom2021_Overgangsregel_GJR_tidligereGJR() {
        val person = Person(
            fødselsdato = Faktum("Fødselsdato", LocalDate.of(1958, 12, 31)),
            flyktning = Faktum("Angitt flyktning", true),
            trygdetidK19 = Trygdetid().apply { tt_fa_F2021 = Faktum(tt_fa_F2021.navn, 20) }
        ).apply {
            this.forsteVirkningsdatoGrunnlagListe.add(
                ForsteVirkningsdatoGrunnlag(
                    LocalDate.of(2020, 1, 1),
                    YtelseEnum.GJR
                )
            )
        }

        val flyktningUtfall = PersonenErFlyktningRS(
            person,
            Faktum("Ytelsestype", YtelseEnum.GJR),
            Faktum("Kapittel20", false),
            Faktum("Virkningstidspunkt", LocalDate.of(2027, 1, 1)),
            Faktum("HarKravlinjeFremsattDatoFom2021", true)
        ).run {
            test()
            this.returnValue
        }

        assertEquals(UtfallType.OPPFYLT, flyktningUtfall.verdi)
//        assertTrue(flyktningUtfall.children[0].fired())
//        val overgangsregelTreSubsumsjon = flyktningUtfall.children[0].children[2] as ListSubsumtion
//        assertEquals(MINST_EN_AV, overgangsregelTreSubsumsjon.comparator)
//        assertEquals(2, overgangsregelTreSubsumsjon.children.size)
//        assertFalse(overgangsregelTreSubsumsjon.children[0].fired())
//        assertTrue(overgangsregelTreSubsumsjon.children[1].fired())
    }
}