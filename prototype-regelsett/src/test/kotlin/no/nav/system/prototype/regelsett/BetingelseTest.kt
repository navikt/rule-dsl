package no.nav.system.prototype.regelsett

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BetingelseTest {

    private val alder = Faktum("alder", 60)
    private val trygdetid = Faktum("trygdetid", 10)

    private fun vurder(definisjon: RegelBygger<Unit>.() -> Unit): Faktum<Boolean> {
        lateinit var regel: Faktum<Boolean>
        regelsett<Unit>("RS") { regel = regel("R", definisjon) }
        return regel.also { println(it.forklar()) }
    }

    @Test
    fun `usann funksjonell betingelse stopper ikke resten`() {
        val regel = vurder {
            HVIS { alder erStørreEllerLik 62 }
            OG { trygdetid erStørreEllerLik 20 }
        }
        assertFalse(regel.verdi)
        assertTrue("60 >= 62" in regel.forklar())
        assertTrue("10 >= 20" in regel.forklar())
    }

    @Test
    fun `usann teknisk betingelse stopper resten`() {
        val mangler: Faktum<Int>? = null
        val regel = vurder {
            HVIS { alder erStørreEllerLik 62 }
            OG { mangler != null }
            OG { mangler!! erStørreEllerLik 20 }
        }
        assertFalse(regel.verdi)
        assertTrue("60 >= 62" in regel.forklar())
        assertTrue("teknisk betingelse ikke oppfylt" in regel.forklar())
    }

    @Test
    fun `sann teknisk betingelse spores ikke`() {
        val regel = vurder {
            HVIS { trygdetid.verdi > 0 }
            OG { alder erMindreEnn 62 }
        }
        assertTrue(regel.verdi)
        assertTrue("betingelse: alder < 62" in regel.forklar())
    }
}
