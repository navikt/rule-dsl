package no.nav.system.rule.dsl.rettsregel

import no.nav.system.rule.dsl.AbstractRuleComponent
import no.nav.system.rule.dsl.enums.RuleComponentType
import no.nav.system.rule.dsl.enums.RuleComponentType.FAKTUM

class Faktum<T>(val navn: String, var verdi: T) : AbstractRuleComponent() {
    var anonymous = false

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