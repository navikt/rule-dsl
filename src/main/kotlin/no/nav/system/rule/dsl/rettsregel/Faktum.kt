package no.nav.system.rule.dsl.rettsregel

import no.nav.system.rule.dsl.AbstractRuleComponent

class Faktum<T>(val navn: String, var verdi: T) : AbstractRuleComponent() {
    var anonymous = false

    constructor(verdi: T) : this(verdi.toString(), verdi) {
        anonymous = true
    }

    override fun name(): String {
        TODO("Not yet implemented")
    }

    override fun type(): String {
        TODO("Not yet implemented")
    }

    override fun fired(): Boolean {
        throw IllegalAccessError("Fired should not be used on Faktum.")
    }

    override fun toString(): String {
        return if (anonymous) {
            "'$navn'"
        } else {
            "'$navn' ($verdi)"
        }
    }
}