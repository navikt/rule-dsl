package no.nav.system.rule.dsl.demo.ruleset

import no.nav.system.rule.dsl.AbstractRuleset
import no.nav.system.rule.dsl.pattern.Pattern
import no.nav.system.rule.dsl.pattern.createPattern
import no.nav.system.rule.dsl.pattern.pairItemsByFunction

/**
 * Regelsett som tar inn to lister som blir slått sammen til et DOublePattern.
 * Patternet filtrerer ut basert på en funksjon [int1 < int2].
 * Ved eksempel input som dette:
 * - list1: 2, 4, 6, 8, 10
 * - list2: 1, 3, 5, 7, 9
 *
 * Vil det være disse tallene som treffer, som gir en sum på 110.
 * - (2, 3), (2, 5), (2, 7), (2, 9)
 * - (4, 5), (4, 7), (4, 9)
 * - (6, 7), (6, 9)
 * - (8, 9)
 */
class PatternRS(
    list1: MutableList<Int>,
    list2: MutableList<Int>
) : AbstractRuleset<Int>() {

    private val doublePatternList = pairItemsByFunction(list1, list2, pairingFunction = { int1, int2 -> int1 < int2 })
    private val uforeperiodePattern: Pattern<Pair<Int, Int>> =
        doublePatternList.createPattern { (it1, it2) -> it1 != it2 }
    private var sum: Int = 0

    override fun create() {

        regel("pattern", uforeperiodePattern) { (int1, int2) ->
            HVIS { true }
            SÅ {
                sum += int1
                sum += int2
            }
        }

        regel("return") {
            HVIS { true }
            SÅ {
            }
            RETURNER(sum)
        }
    }
}