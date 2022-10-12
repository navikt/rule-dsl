package no.nav.system.rule.dsl.demo.ruleset

import no.nav.system.rule.dsl.demo.domain.Boperiode
import no.nav.system.rule.dsl.demo.domain.koder.LandEnum
import no.nav.system.rule.dsl.demo.helper.localDate
import no.nav.system.rule.dsl.rettsregel.Faktum
import no.nav.system.rule.dsl.treevisitor.visitor.DebugVisitor
import no.nav.system.rule.dsl.treevisitor.visitor.RettsregelVisitor
import no.nav.system.rule.dsl.treevisitor.visitor.RuleVisitor
import no.nav.system.rule.dsl.treevisitor.visitor.debug
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * Prototype for produksjon av regelsporing på predikatnivå.
 */
class BeregnFaktiskTrygdetidRSTest {

    @Test
    fun `test fagregel 'Redusert fremtidig trygdetid' har truffet`() {

        val redFttVisitor = RettsregelVisitor { regel ->
            regel.name() == "BeregnFaktiskTrygdetidRS.Skal ha redusert fremtidig trygdetid"
        }

        BeregnFaktiskTrygdetidRS(
            fødselsdato = localDate(1990, 1, 1),
            virkningstidspunkt = Faktum(navn = "virkningstidspunkt", localDate(2000, 1, 1)),
            boperiodeListe = listOf(
                Boperiode(fom = localDate(1990, 1, 1), tom = localDate(2018, 12, 31), LandEnum.NOR)
            )
        ).run {
            test()
            accept(redFttVisitor)
            println(debug())
        }

        val redFttRegel = redFttVisitor.rettsregel
        Assertions.assertTrue(redFttRegel.evaluated)
        Assertions.assertTrue(redFttRegel.fired())
        Assertions.assertEquals(
            "JA: 'virkningstidspunkt' (2000-01-01) er fom '1991-01-01'",
            redFttRegel.predicateList[0].toString()
        )
        Assertions.assertEquals(
            "JA: 'faktisk trygdetid i måneder' (155) er mindre enn 'firefemtedelskrav' (480)",
            redFttRegel.predicateList[1].toString()
        )
    }

    @Test
    fun `test fagregel 'Redusert fremtidig trygdetid' har ikke truffet`() {

        val redFttVisitor = RettsregelVisitor { regel ->
            regel.name() == "BeregnFaktiskTrygdetidRS.Skal ha redusert fremtidig trygdetid"
        }

        BeregnFaktiskTrygdetidRS(
            fødselsdato = localDate(1990, 1, 1),
            virkningstidspunkt = Faktum("virkningstidspunkt", localDate(2000, 1, 1)),
            boperiodeListe = listOf(
                Boperiode(fom = localDate(1990, 1, 1), tom = localDate(2048, 12, 31), LandEnum.NOR)
            )
        ).run {
            test()
            accept(redFttVisitor)
        }

        val redFttKonklusjon = redFttVisitor.rettsregel
        Assertions.assertTrue(redFttKonklusjon.evaluated)
        Assertions.assertFalse(redFttKonklusjon.fired())
        Assertions.assertEquals(
            "JA: 'virkningstidspunkt' (2000-01-01) er fom '1991-01-01'",
            redFttKonklusjon.predicateList[0].toString()
        )
        Assertions.assertEquals(
            "NEI: 'faktisk trygdetid i måneder' (515) må være mindre enn 'firefemtedelskrav' (480)",
            redFttKonklusjon.predicateList[1].toString()
        )
    }
}