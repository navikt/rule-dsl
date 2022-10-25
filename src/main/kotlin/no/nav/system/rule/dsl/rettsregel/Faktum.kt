package no.nav.system.rule.dsl.rettsregel

import no.nav.system.rule.dsl.AbstractRuleComponent
import no.nav.system.rule.dsl.enums.RuleComponentType
import no.nav.system.rule.dsl.enums.RuleComponentType.FAKTUM
import no.nav.system.rule.dsl.treevisitor.visitor.debug

open class Faktum<T:Any>(open val navn: String) : AbstractRuleComponent() {
    lateinit var verdi: T

    var anonymous = false

    constructor(navn: String, verdi: T) : this(navn) {
        this.verdi = verdi
    }

    constructor(verdi: T) : this(verdi.toString(), verdi) {
        anonymous = true
    }

    override fun name(): String {
        return navn
    }

    override fun type(): RuleComponentType {
        return FAKTUM
    }

    override fun fired(): Boolean {
        throw IllegalAccessError("Fired should not be used on Faktum.")
    }

    override fun toString(): String {
        return if (anonymous) {
            "${type()}: '$navn'"
        } else {
            "${type()}: '$navn' ($verdi)"
        }
    }
}