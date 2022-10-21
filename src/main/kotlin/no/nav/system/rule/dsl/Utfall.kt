package no.nav.system.rule.dsl

import no.nav.system.rule.dsl.enums.UtfallType
import no.nav.system.rule.dsl.rettsregel.Faktum
import no.nav.system.rule.dsl.treevisitor.visitor.debug

//open class Utfall(var type: UtfallType) {
//    lateinit var regel: Rule
//
//    override fun toString(): String {
//        return "utfall: $type\n${regel.debug()}"
//    }
//}
//
//class TomtUtfall: Utfall<UtfallType>("TomtUtfall", UtfallType.IKKE_RELEVANT)
//
//open class Utfall<T>(override val navn: String, override var verdi: T): Faktum<T>(navn, verdi) {
//    lateinit var regel: Rule
//
//    override fun toString(): String {
//        return "utfall: \n${regel.debug()}"
//    }
//}
