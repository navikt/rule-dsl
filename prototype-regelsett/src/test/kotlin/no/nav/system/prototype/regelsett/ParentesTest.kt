package no.nav.system.prototype.regelsett

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ParentesTest {

    private val a = Faktum("a", 8)
    private val b = Faktum("b", 4)
    private val c = Faktum("c", 2)

    @Test
    fun `ingen parentes når rekkefølgen gir samme svar`() {
        assertEquals("a * b * c", (a * b * c).notasjon())
        assertEquals("a * b / c", (a * b / c).notasjon())
        assertEquals("a / b * c", (a / b * c).notasjon())
    }

    @Test
    fun `parentes når regnereglene krever det`() {
        assertEquals("a / (b * c)", (a / (b * c)).notasjon())
        assertEquals("a / (b / c)", (a / (b / c)).notasjon())
        assertEquals("(36 - a) / 36", ((36 - a) / 36).notasjon())
        assertEquals("a * (36 - b)", (a * (36 - b)).notasjon())
        assertEquals("1 - (36 - a)", (1 - (36 - a)).notasjon())
    }

    @Test
    fun `sammenligning i sammenligning får parentes`() {
        assertEquals("(a < 10) = false", ((a erMindreEnn 10) erLik false).notasjon())
    }
}
