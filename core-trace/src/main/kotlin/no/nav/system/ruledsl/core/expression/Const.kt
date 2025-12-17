package no.nav.system.ruledsl.core.expression

/**
 * Unnamed constant - treated as atomic unit.
 * This is can NOT be created by users.
 */
internal data class Const<T : Any>(
    override val value: T
) : Expression<T> {

    override fun notation(): String = value.toString()

    override fun concrete(): String = value.toString()

    override fun faktumSet(): Set<Faktum<out Any>> = emptySet()

    override fun toString(): String = "'$value'"
}