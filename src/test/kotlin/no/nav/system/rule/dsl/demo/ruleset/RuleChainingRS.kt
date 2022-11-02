package no.nav.system.rule.dsl.demo.ruleset

import no.nav.system.rule.dsl.AbstractRuleset

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
