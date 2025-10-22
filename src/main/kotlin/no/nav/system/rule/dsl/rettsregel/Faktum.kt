package no.nav.system.rule.dsl.rettsregel

import java.io.Serializable
//// Common interface for values used in rules
//interface RuleValue<T : Any> {
//    val value: T
//    val name: String
//}
//
//// For non-numeric facts
//class Faktum<T : Any>(...) : AbstractRuleComponent(), RuleValue<T> {
//    override val value: T
//    // No math operators
//}
//
//// For numeric computations
//class Formel<T : Number>(...) : AbstractRuleComponent(), RuleValue<T> {
//    override val value: T get() = resultat()
//    // Has +, -, *, / operators
//    // Has notation, innhold, subformulas
//}
//Benefit: Clear separation. Can't accidentally mix them.

interface Verdi<T : Any> {
    val value: T
    val name: String
}

open class Faktum<T : Any> internal constructor(
    override val name: String,
    override var value : T,
    internal var anonymous: Boolean = false
) : Verdi<T>, Serializable {
//    lateinit var value: T
//    internal var anonymous = false

//    constructor(value : T): this(name = "anonymous", value = value, anonymous = true)


//    constructor(navn: String, verdi: T) : this(navn) {
//        this.value = verdi
//    }

    constructor(verdi: T) : this(verdi.toString(), verdi, anonymous = true)

//    override fun name(): String = name
//    override fun type(): RuleComponentType = FAKTUM
//    override fun fired(): Boolean = throw IllegalAccessError("Fired() should not be used on Faktum.")
    override fun toString(): String {
        return if (anonymous) {
            "'$value'"
        } else {
            "'$name' ($value)"
        }
    }
}