package no.nav.pensjon.regler.sliterordning.math

import no.nav.pensjon.regler.sliterordning.functions.avrund2desimal
import no.nav.system.ruledsl.core.model.Faktum
import no.nav.system.ruledsl.core.operators.div
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CustomMathFunctionsTest {

    @Test
    fun `avrund2desimal should round correctly`() {
        val input = Faktum("beløp", 1234.5678)
        val rounded = avrund2desimal(input)

        assertEquals(1234.57, rounded.verdi)
        assertEquals("avrund2desimal(beløp)", rounded.notasjon())
        assertEquals("avrund2desimal(1234.5678)", rounded.konkret())
    }

    @Test
    fun `custom functions can be used in complex expressions`() {
        // Simulate: calculate monthly benefit, round to 2 decimals
        val årligBeløp = Faktum("årligBeløp", 123456.789)
        val månedsBeløp = årligBeløp / 12
        val avrundet = Faktum("månedligUtbetaling", avrund2desimal(månedsBeløp))

        assertEquals(10288.07, avrundet.verdi)
        assertEquals("avrund2desimal(årligBeløp / 12)", avrundet.uttrykk.notasjon())
    }


}
