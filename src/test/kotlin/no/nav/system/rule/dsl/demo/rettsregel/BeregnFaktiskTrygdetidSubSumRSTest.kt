package no.nav.system.rule.dsl.demo.rettsregel

import no.nav.system.rule.dsl.demo.domain.Boperiode
import no.nav.system.rule.dsl.demo.domain.koder.LandEnum
import no.nav.system.rule.dsl.demo.helper.localDate
import no.nav.system.rule.dsl.rettsregel.Faktum
import no.nav.system.rule.dsl.treevisitor.visitor.RuleVisitor
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Prototype for produksjon av regelsporing på predikatnivå.
 */
class BeregnFaktiskTrygdetidSubSumRSTest {

    @Test
    fun `test fagregel 'Redusert fremtidig trygdetid' har truffet`() {

        val redFttVisitor = RuleVisitor { regel ->
            regel.name() == "BeregnFaktiskTrygdetidSubSumRS.Skal ha redusert fremtidig trygdetid"
        }

        BeregnFaktiskTrygdetidSubSumRS(
            fødselsdato = localDate(1990, 1, 1),
            virkningstidspunkt = Faktum(navn = "virkningstidspunkt", localDate(2000, 1, 1)),
            boperiodeListe = listOf(
                Boperiode(fom = localDate(1990, 1, 1), tom = localDate(2018, 12, 31), LandEnum.NOR)
            )
        ).run {
            test()
            accept(redFttVisitor)
        }

        val redFttKonklusjon = redFttVisitor.conclusion
        assertTrue(redFttKonklusjon.evaluated)
        assertTrue(redFttKonklusjon.fired)
        assertEquals(
            "JA: Virkningsdato i saken, 2000-01-01, er fom 1991-01-01.",
            redFttKonklusjon.reasons[0]
        )
        assertEquals(
            "JA: Faktisk trygdetid, 155, er lavere enn fire-femtedelskravet (480).",
            redFttKonklusjon.reasons[1]
        )
    }

    @Test
    fun `test fagregel 'Redusert fremtidig trygdetid' har ikke truffet`() {

        val redFttVisitor = RuleVisitor { regel ->
            regel.name() == "BeregnFaktiskTrygdetidSubSumRS.Skal ha redusert fremtidig trygdetid"
        }

        BeregnFaktiskTrygdetidSubSumRS(
            fødselsdato = localDate(1990, 1, 1),
            virkningstidspunkt = Faktum("virkningstidspunkt", localDate(2000, 1, 1)),
            boperiodeListe = listOf(
                Boperiode(fom = localDate(1990, 1, 1), tom = localDate(2048, 12, 31), LandEnum.NOR)
            )
        ).run {
            test()
            accept(redFttVisitor)
        }

        val redFttKonklusjon = redFttVisitor.conclusion
        assertTrue(redFttKonklusjon.evaluated)
        assertFalse(redFttKonklusjon.fired)
        assertEquals(
            "JA: Virkningsdato i saken, 2000-01-01, er fom 1991-01-01.",
            redFttKonklusjon.reasons[0]
        )
        assertEquals(
            "NEI: Faktisk trygdetid, 515, er høyere enn fire-femtedelskravet (480).",
            redFttKonklusjon.reasons[1]
        )
    }
}