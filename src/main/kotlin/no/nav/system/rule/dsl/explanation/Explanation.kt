package no.nav.system.rule.dsl.explanation

/**
 * HVA: Identity and result
 * "What is this component/value?"
 *
 * All components in the execution tree implement this interface to identify themselves.
 */
interface Hva {
    fun hva(): String
}

/**
 * HVORFOR: Execution context
 * "Why was this executed/created?"
 *
 * Components that can explain their execution context implement this interface.
 */
interface Hvorfor {
    fun hvorfor(): List<Hva>
}

/**
 * HVORDAN: Computation details
 * "How is this calculated/evaluated?"
 *
 * Components that perform calculations or evaluations implement this interface.
 */
interface Hvordan {
    fun hvordan(): String
}
