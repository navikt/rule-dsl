package no.nav.system.rule.dsl.demo.ruleservice

import no.nav.system.rule.dsl.demo.domain.Boperiode
import no.nav.system.rule.dsl.demo.domain.Person
import no.nav.system.rule.dsl.demo.domain.Request
import no.nav.system.rule.dsl.demo.domain.Response
import no.nav.system.rule.dsl.demo.domain.koder.LandEnum
import no.nav.system.rule.dsl.demo.helper.localDate
import no.nav.system.rule.dsl.enums.UtfallType.IKKE_OPPFYLT
import no.nav.system.rule.dsl.enums.UtfallType.OPPFYLT
import no.nav.system.rule.dsl.rettsregel.Faktum
import no.nav.system.rule.dsl.treevisitor.visitor.debug
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BeregnAlderspensjonServiceTest {

    @Test
    fun `redusert fremtidig trygdetid og høy sats`() {
        val params = Request(
            virkningstidspunkt = localDate(2020, 1, 1), person = Person(
                id = 1, fødselsdato = Faktum("Fødselsdato", localDate(1980, 3, 3)), erGift = false, boperioder = listOf(
                    Boperiode(fom = localDate(1990, 1, 1), tom = localDate(1998, 12, 31), LandEnum.NOR)
                )
            )
        )

        val response: Response = BeregnAlderspensjonService(params).let {
            val run = it.run()
            println(it.debug())
            run
        }

        assertEquals(3, response.anvendtTrygdetid?.år)
        assertEquals(480, response.anvendtTrygdetid?.firefemtedelskrav!!.verdi)
        assertEquals(OPPFYLT, response.anvendtTrygdetid.redusertFremtidigTrygdetid.verdi)

        assertEquals(9000, response.grunnpensjon?.netto)
        assertEquals(1.0, response.grunnpensjon?.prosentsats)
        assertEquals(120000, response.grunnpensjon?.grunnbeløp)
    }

    @Test
    fun `ikke redusert fremtidig trygdetid og lav sats`() {
        val params = Request(
            virkningstidspunkt = localDate(1990, 5, 1), person = Person(
                id = 1, fødselsdato = Faktum("Fødselsdato", localDate(1974, 3, 3)), erGift = true, boperioder = listOf(
                    Boperiode(fom = localDate(1990, 1, 1), tom = localDate(2003, 12, 31), LandEnum.NOR),
                    Boperiode(fom = localDate(2004, 1, 1), tom = localDate(2010, 12, 31), LandEnum.SWE),
                    Boperiode(fom = localDate(2011, 1, 1), tom = localDate(2015, 12, 31), LandEnum.NOR),
                    Boperiode(fom = localDate(2016, 1, 1), tom = localDate(2020, 12, 31), LandEnum.SWE)
                )
            )
        )

        val response = BeregnAlderspensjonService(params).let {
            val run = it.run()
            println(it.debug())
            run
        }

        assertEquals(19, response.anvendtTrygdetid?.år)
        assertEquals(480, response.anvendtTrygdetid?.firefemtedelskrav!!.verdi)
        assertEquals(IKKE_OPPFYLT, response.anvendtTrygdetid.redusertFremtidigTrygdetid.verdi)

        assertEquals(42750, response.grunnpensjon?.netto)
        assertEquals(0.9, response.grunnpensjon?.prosentsats)
        assertEquals(100000, response.grunnpensjon?.grunnbeløp)
    }

}