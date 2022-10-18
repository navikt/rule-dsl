package no.nav.system.rule.dsl

import no.nav.system.rule.dsl.rettsregel.UtfallType
import no.nav.system.rule.dsl.treevisitor.visitor.debug

open class Utfall(var utfallType: UtfallType) {
    lateinit var regel: Rule

    override fun toString(): String {
        return "utfall: $utfallType\n${regel.debug()}"
    }
}

class TomtUtfall: Utfall(UtfallType.IKKE_RELEVANT)


