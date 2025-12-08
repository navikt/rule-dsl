package no.nav.system.ruledsl.core.math

import no.nav.system.ruledsl.core.functions.ceil
import no.nav.system.ruledsl.core.functions.floor
import no.nav.system.ruledsl.core.model.Faktum
import no.nav.system.ruledsl.core.operators.div
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MathFunctionTest {

    @Test
    fun `floor should return largest integer less than or equal`() {
        val input = Faktum("måneder", 37.8)
        val floored = floor(input)

        assertEquals(37.0, floored.verdi)
        assertEquals("floor(måneder)", floored.notasjon())
    }

    @Test
    fun `ceil should return smallest integer greater than or equal`() {
        val input = Faktum("måneder", 37.2)
        val ceiled = ceil(input)

        assertEquals(38.0, ceiled.verdi)
        assertEquals("ceil(måneder)", ceiled.notasjon())
    }

    @Test
    fun `integer division can be simulated with floor`() {
        // Example: Convert 37 months to years (integer division)
        val måneder = Faktum("måneder", 37)
        val år = Faktum("år", floor(måneder / 12))

        assertEquals(3.0, år.verdi)
        assertEquals("floor(måneder / 12)", år.uttrykk.notasjon())
        assertEquals("floor(37 / 12)", år.uttrykk.konkret())
    }
}