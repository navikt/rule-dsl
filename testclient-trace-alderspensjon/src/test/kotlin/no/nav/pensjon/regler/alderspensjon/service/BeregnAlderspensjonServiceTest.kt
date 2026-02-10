package no.nav.pensjon.regler.alderspensjon.service

import no.nav.pensjon.regler.alderspensjon.domain.Boperiode
import no.nav.pensjon.regler.alderspensjon.domain.Person
import no.nav.pensjon.regler.alderspensjon.domain.Request
import no.nav.pensjon.regler.alderspensjon.domain.koder.LandEnum
import no.nav.system.ruledsl.core.expression.Verdi
import no.nav.system.ruledsl.core.trace.debugTree
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.time.LocalDate

class BeregnAlderspensjonServiceTest {

    @Test
    fun `beregn alderspensjon for ugift person with full trygdetid`() {
        val person = Person(
            fødselsdato = Verdi("fødselsdato", LocalDate.of(1960, 1, 1)),
            erGift = false,
            boperioder = listOf(
                Boperiode(
                    fom = LocalDate.of(1976, 1, 1),  // Age 16
                    tom = LocalDate.of(2024, 1, 1),  // 48 years later
                    land = LandEnum.NOR
                )
            )
        )
        
        val request = Request(
            virkningstidspunkt = LocalDate.of(2025, 1, 1),
            person = person
        )
        
        val (response, trace) = beregnAlderspensjon(request)
        
        println(trace.debugTree())
        
        assertNotNull(response.anvendtTrygdetid)
        assertNotNull(response.grunnpensjon)
        assertEquals(40, response.anvendtTrygdetid?.år)
        assertEquals(1.0, response.grunnpensjon?.prosentsats)
    }

    @Test
    fun `beregn alderspensjon for gift person with reduced trygdetid`() {
        val person = Person(
            fødselsdato = Verdi("fødselsdato", LocalDate.of(1960, 1, 1)),
            erGift = true,
            boperioder = listOf(
                Boperiode(
                    fom = LocalDate.of(1976, 1, 1),
                    tom = LocalDate.of(1996, 1, 1),  // 20 years
                    land = LandEnum.NOR
                )
            )
        )
        
        val request = Request(
            virkningstidspunkt = LocalDate.of(2025, 1, 1),
            person = person
        )
        
        val (response, trace) = beregnAlderspensjon(request)
        
        println(trace.debugTree())
        
        assertNotNull(response.anvendtTrygdetid)
        assertNotNull(response.grunnpensjon)
        assertEquals(20, response.anvendtTrygdetid?.år)
        assertEquals(0.9, response.grunnpensjon?.prosentsats)
    }
}
