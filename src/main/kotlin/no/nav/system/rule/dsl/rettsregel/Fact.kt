package no.nav.system.rule.dsl.rettsregel

import no.nav.system.rule.dsl.AbstractRuleComponent
import no.nav.system.rule.dsl.enums.RuleComponentType
import no.nav.system.rule.dsl.enums.RuleComponentType.FAKTUM
import java.io.Serializable

open class Fact<T : Any> internal constructor(open val name: String) : AbstractRuleComponent(), Serializable {
    lateinit var value: T
    private var anonymous = false

    constructor(navn: String, verdi: T) : this(navn) {
        this.value = verdi
    }

    constructor(verdi: T) : this(verdi.toString(), verdi) {
        anonymous = true
    }

    override fun name(): String = name

    override fun type(): RuleComponentType = FAKTUM

    override fun fired(): Boolean {
        throw IllegalAccessError("Fired() should not be used on Faktum.")
    }

    override fun toString(): String {
        return if (anonymous) {
            "'$value'"
        } else {
            "'$name' ($value)"
        }
    }
}