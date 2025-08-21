package no.nav.system.rule.dsl.formel

class Builder<T : Number>(
) {
    companion object {
        inline fun <reified T : Number> kmath(): Builder<T> {
            if (T::class == Int::class || T::class == Double::class) {
                return Builder<T>()
            } else throw IllegalArgumentException("Illegal ${T::class}. Legal types are Int or Double.")
        }
    }

    private var builderPrefix: String = ""
    private var builderEmne: String = ""
    private var builderPostfix: String = ""
    private var builderLocked: Boolean = true
    private var builderFormel: Formel<T>? = null

    fun prefix(prefix: String): Builder<T> = apply { this.builderPrefix = prefix }
    fun emne(emne: String): Builder<T> = apply { this.builderEmne = emne }
    fun postfix(postfix: String): Builder<T> = apply { this.builderPostfix = postfix }
    fun unlock() = apply { this.builderLocked = false }
    fun formel(kFormel: Formel<T>): Builder<T> = apply { this.builderFormel = kFormel }

    fun build(): Formel<T> {
        val prettyEmne = builderEmne.trim().replace(" ", "_")
        val formel = builderFormel ?: throw IllegalStateException("Ingen formel angitt av funksjon .formel(..).")
        formel.locked = builderLocked
        validateState(prettyEmne, formel)
        return formel.apply {
            prefix = builderPrefix.trim()
            emne = prettyEmne
            postfix = builderPostfix.trim()
        }
    }

    private fun validateState(prettyName: String, formel: Formel<*>) {
        if (formel.namedVarMap.containsKey(prettyName)) {
            throw IllegalArgumentException("Illegal circular reference. Formula name $prettyName cannot contain named variables of same name.")
        }
    }
}