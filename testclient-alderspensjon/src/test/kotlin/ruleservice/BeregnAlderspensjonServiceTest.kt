package no.nav.system.rule.dsl.demo.ruleservice

import no.nav.pensjon.regler.alderspensjon.domain.Boperiode
import no.nav.pensjon.regler.alderspensjon.domain.Person
import no.nav.pensjon.regler.alderspensjon.domain.Request
import no.nav.pensjon.regler.alderspensjon.domain.koder.LandEnum
import no.nav.pensjon.regler.alderspensjon.domain.koder.UtfallType
import no.nav.pensjon.regler.alderspensjon.service.BeregnAlderspensjonService
import no.nav.system.ruledsl.core.rettsregel.Faktum
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

class BeregnAlderspensjonServiceTest {

    @Test
    fun `redusert fremtidig trygdetid og høy sats`() {
        val params = Request(
            virkningstidspunkt = LocalDate.of(2020, 1, 1),
            person = Person(
                id = 1,
                fødselsdato = Faktum("Fødselsdato", LocalDate.of(1980, 3, 3)),
                erGift = false,
                boperioder = listOf(
                    Boperiode(fom = LocalDate.of(1990, 1, 1), tom = LocalDate.of(1998, 12, 31), LandEnum.NOR)
                )
            )
        )

        val response = BeregnAlderspensjonService(params).run()

        assertEquals(3, response.anvendtTrygdetid?.år)
        assertEquals(480, response.anvendtTrygdetid?.firefemtedelskrav!!.verdi)
        assertEquals(UtfallType.OPPFYLT, response.anvendtTrygdetid.redusertFremtidigTrygdetid.verdi)

        assertEquals(9000, response.grunnpensjon?.netto)
        assertEquals(1.0, response.grunnpensjon?.prosentsats)
        assertEquals(120000, response.grunnpensjon?.grunnbeløp)
    }

    @Test
    fun `ikke redusert fremtidig trygdetid og lav sats`() {
        val params = Request(
            virkningstidspunkt = LocalDate.of(1990, 5, 1),
            person = Person(
                id = 1,
                fødselsdato = Faktum("Fødselsdato", LocalDate.of(1974, 3, 3)),
                erGift = true,
                boperioder = listOf(
                    Boperiode(fom = LocalDate.of(1990, 1, 1), tom = LocalDate.of(2003, 12, 31), LandEnum.NOR),
                    Boperiode(fom = LocalDate.of(2004, 1, 1), tom = LocalDate.of(2010, 12, 31), LandEnum.SWE),
                    Boperiode(fom = LocalDate.of(2011, 1, 1), tom = LocalDate.of(2015, 12, 31), LandEnum.NOR),
                    Boperiode(fom = LocalDate.of(2016, 1, 1), tom = LocalDate.of(2020, 12, 31), LandEnum.SWE)
                )
            )
        )

        val response = BeregnAlderspensjonService(params).run()

        assertEquals(19, response.anvendtTrygdetid?.år)
        assertEquals(480, response.anvendtTrygdetid?.firefemtedelskrav!!.verdi)
        assertEquals(UtfallType.IKKE_OPPFYLT, response.anvendtTrygdetid.redusertFremtidigTrygdetid.verdi)

        assertEquals(42750, response.grunnpensjon?.netto)
        assertEquals(0.9, response.grunnpensjon?.prosentsats)
        assertEquals(100000, response.grunnpensjon?.grunnbeløp)
    }

}