package no.nav.system.ruledsl.core.model

import no.nav.system.ruledsl.core.model.arc.AbstractRuleset
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

/**
 * Regelsett som demonstrerer rulechaining.
 */
class RuleChainingRSTest {

    /**
     * Regelsett som demonstrerer rulechaining.
     */
    class RuleChainingRS(private val kjørRegel: Boolean) : AbstractRuleset<Int>() {
        private var tak: Int = 1000

        override fun create() {
            regel("førsteRegel") {
                HVIS { kjørRegel }
                SÅ {
                    tak++
                }
                kommentar("Default regel som alltid skal treffe.")
            }

            regel("førsteChain") {
                HVIS { "førsteRegel".harTruffet() }
                SÅ {
                    tak++
                }
                kommentar("Skal treffe hvis 'førsteRegel' har truffet.")
            }

            regel("sisteChain") {
                HVIS { "førsteRegel".harTruffet() }
                OG { "førsteChain".harTruffet() }
                SÅ {
                    tak++
                }
                kommentar("Skal treffe hvis både 'førsteRegel' og 'førsteChain' har truffet.")
            }

            regel("negativRegel") {
                HVIS { "førsteRegel".harIkkeTruffet() }
                SÅ {
                    tak--
                }
                kommentar("Skal treffe hvis 'førsteRegel' ikke har truffet.")
            }

            regel("negativChain") {
                HVIS { "negativRegel".harTruffet() }
                OG { "førsteChain".harTruffet() }
                SÅ {
                    tak--
                }
                kommentar("Skal treffe hvis både 'negativRegel' og 'førsteChain' har truffet.")
            }

            regel("DefaultRetur") {
                HVIS { true }
                SÅ {
                    RETURNER(tak)
                }
            }
        }

    }

    @Test
    fun `test hvor førstRegel kjører`() {
        val ruleChainingRS = RuleChainingRS(true).test()
        assertEquals(1003, ruleChainingRS)
    }

    @Test
    fun `test hvor førsteRegel ikke kjører`() {
        val ruleChainingRS = RuleChainingRS(false).test()
        assertEquals(999, ruleChainingRS)
    }

}
