package no.nav.system.rule.dsl.demo.ruleset

import no.nav.system.rule.dsl.demo.domain.Boperiode
import no.nav.system.rule.dsl.demo.domain.koder.LandEnum
import no.nav.system.rule.dsl.demo.helper.localDate
import no.nav.system.rule.dsl.demo.domain.koder.UtfallType.IKKE_OPPFYLT
import no.nav.system.rule.dsl.rettsregel.Faktum
import no.nav.system.rule.dsl.treevisitor.visitor.RuleVisitor
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

/**
 * Prototype for produksjon av regelsporing på predikatnivå.
 */
class BeregnFaktiskTrygdetidRSTest {

    @Test
    fun `test fagregel 'Redusert fremtidig trygdetid' har truffet`() {

        val redFttVisitor = RuleVisitor { regel ->
            regel.name() == "BeregnFaktiskTrygdetidRS.Skal ha redusert fremtidig trygdetid"
        }

        BeregnFaktiskTrygdetidRS(
            fødselsdato = Faktum("Fødselsdato", localDate(1990, 1, 1)),
            virkningstidspunkt = Faktum(navn = "virkningstidspunkt", localDate(2000, 1, 1)),
            boperiodeListe = listOf(
                Boperiode(fom = localDate(1990, 1, 1), tom = localDate(2018, 12, 31), LandEnum.NOR)
            ),
            Faktum("Anvendt flyktning", IKKE_OPPFYLT)
        ).run {
            test()
            accept(redFttVisitor)
//            println(debug())
        }

        assertNotNull(redFttVisitor.rule)
        val redFttRegel = redFttVisitor.rule!!
        Assertions.assertTrue(redFttRegel.evaluated)
        Assertions.assertTrue(redFttRegel.fired())
        Assertions.assertEquals(
            "par_subsumsjon: JA 'virkningstidspunkt' (2000-01-01) er etter eller lik '1991-01-01'", redFttRegel.children[0].toString()
        )
        Assertions.assertEquals(
            "par_subsumsjon: JA 'faktisk trygdetid i måneder' (155) er mindre enn 'firefemtedelskrav' (480)", redFttRegel.children[1].toString()
        )
    }

    @Test
    fun `test fagregel 'Redusert fremtidig trygdetid' har ikke truffet`() {

        val redFttVisitor = RuleVisitor { regel ->
            regel.name() == "BeregnFaktiskTrygdetidRS.Skal ha redusert fremtidig trygdetid"
        }

        BeregnFaktiskTrygdetidRS(
            fødselsdato = Faktum("Fødselsdato", localDate(1990, 1, 1)),
            virkningstidspunkt = Faktum("virkningstidspunkt", localDate(2000, 1, 1)),
            boperiodeListe = listOf(
                Boperiode(fom = localDate(1990, 1, 1), tom = localDate(2048, 12, 31), LandEnum.NOR)
            ),
            Faktum("Anvendt flyktning", IKKE_OPPFYLT)
        ).run {
            test()
            accept(redFttVisitor)
        }

        assertNotNull(redFttVisitor.rule)
        val redFttKonklusjon = redFttVisitor.rule!!
        Assertions.assertTrue(redFttKonklusjon.evaluated)
        Assertions.assertFalse(redFttKonklusjon.fired())
        Assertions.assertEquals(
            "par_subsumsjon: JA 'virkningstidspunkt' (2000-01-01) er etter eller lik '1991-01-01'", redFttKonklusjon.children[0].toString()
        )
        Assertions.assertEquals(
            "par_subsumsjon: NEI 'faktisk trygdetid i måneder' (515) må være mindre enn 'firefemtedelskrav' (480)",
            redFttKonklusjon.children[1].toString()
        )
    }
}