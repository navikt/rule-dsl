package no.nav.system.rule.dsl.rettsregel

class Faktum<T>(val navn: String, var verdi: T) {
    var anonymous = false

    constructor(verdi: T) : this(verdi.toString(), verdi) {
        anonymous = true
    }

    override fun toString(): String {
        return if (anonymous) {
            "'$navn'"
        } else {
            "'$navn' ($verdi)"
        }
    }
}