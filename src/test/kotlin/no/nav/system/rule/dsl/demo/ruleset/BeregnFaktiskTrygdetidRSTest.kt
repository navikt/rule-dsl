package no.nav.system.rule.dsl.demo.ruleset

import no.nav.system.rule.dsl.AbstractRuleComponent
import no.nav.system.rule.dsl.Rule
import no.nav.system.rule.dsl.demo.domain.Boperiode
import no.nav.system.rule.dsl.demo.domain.koder.LandEnum
import no.nav.system.rule.dsl.demo.domain.koder.UtfallType.IKKE_OPPFYLT
import no.nav.system.rule.dsl.demo.helper.localDate
import no.nav.system.rule.dsl.inspections.find
import no.nav.system.rule.dsl.rettsregel.Faktum
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * Prototype for produksjon av regelsporing på predikatnivå.
 */
class BeregnFaktiskTrygdetidRSTest {

    @Test
    fun `test fagregel 'Redusert fremtidig trygdetid' har truffet`() {
        val result = BeregnFaktiskTrygdetidRS(
            fødselsdato = Faktum("Fødselsdato", localDate(1990, 1, 1)),
            virkningstidspunkt = Faktum(navn = "virkningstidspunkt", localDate(2000, 1, 1)),
            boperiodeListe = listOf(
                Boperiode(fom = localDate(1990, 1, 1), tom = localDate(2018, 12, 31), LandEnum.NOR)
            ),
            Faktum("Anvendt flyktning", IKKE_OPPFYLT)
        ).run {
            test()
            find { regel -> regel.name() == "BeregnFaktiskTrygdetidRS.Skal ha redusert fremtidig trygdetid" }
        }

        assertTrue(result.isNotEmpty())
        val redFttRegel = result.first() as Rule<*>
        assertTrue(redFttRegel.evaluated)
        assertTrue(redFttRegel.fired())
        Assertions.assertEquals(
            "JA 'virkningstidspunkt' (2000-01-01) er etter eller lik '1991-01-01'", redFttRegel.children[0].toString()
        )
        Assertions.assertEquals(
            "JA 'faktisk trygdetid i måneder' (155) er mindre enn 'firefemtedelskrav' (480)",
            redFttRegel.children[1].toString()
        )
    }

    @Test
    fun `test fagregel 'Redusert fremtidig trygdetid' har ikke truffet`() {
        var result: MutableList<AbstractRuleComponent> = mutableListOf()

        BeregnFaktiskTrygdetidRS(
            fødselsdato = Faktum("Fødselsdato", localDate(1990, 1, 1)),
            virkningstidspunkt = Faktum("virkningstidspunkt", localDate(2000, 1, 1)),
            boperiodeListe = listOf(
                Boperiode(fom = localDate(1990, 1, 1), tom = localDate(2048, 12, 31), LandEnum.NOR)
            ),
            Faktum("Anvendt flyktning", IKKE_OPPFYLT)
        ).run {
            test()
            result = find { regel -> regel.name() == "BeregnFaktiskTrygdetidRS.Skal ha redusert fremtidig trygdetid" }
        }

        assertTrue(result.isNotEmpty())
        val regelSkalHaRedusertFremtidigTrygdetid = result.first() as Rule<*>
        assertTrue(regelSkalHaRedusertFremtidigTrygdetid.evaluated)
        Assertions.assertFalse(regelSkalHaRedusertFremtidigTrygdetid.fired())
        Assertions.assertEquals(
            "JA 'virkningstidspunkt' (2000-01-01) er etter eller lik '1991-01-01'",
            regelSkalHaRedusertFremtidigTrygdetid.children[0].toString()
        )
        Assertions.assertEquals(
            "NEI 'faktisk trygdetid i måneder' (515) må være mindre enn 'firefemtedelskrav' (480)",
            regelSkalHaRedusertFremtidigTrygdetid.children[1].toString()
        )
    }
}