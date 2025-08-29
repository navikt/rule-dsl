package no.nav.system.rule.dsl.demo.ruleservice

/**
 * Lettvekts Faktum wrapper som automatisk sporer execution path
 * Brukes som drop-in replacement for no.nav.system.rule.dsl.rettsregel.Faktum
 */
data class TrackedFaktum<T : Any>(
    val name: String,
    val value: T
) {
    init {
        // Automatisk sporing ved opprettelse
        FaktumTracker.track(
            no.nav.system.rule.dsl.rettsregel.Faktum(name, value)
        )
    }
    
    companion object {
        /**
         * Wrapper funksjon som erstatter Faktum constructor
         */
        operator fun <T : Any> invoke(navn: String, verdi: T): TrackedFaktum<T> {
            return TrackedFaktum(navn, verdi)
        }
        
        /**
         * Start ny sporingsøkt
         */
        fun startTracking(): String = FaktumTracker.startSession()
        
        /**
         * Hent execution path
         */
        fun getExecutionPath() = FaktumTracker.getExecutionPath()
        
        /**
         * Eksporter til Neo4j
         */
        fun exportToNeo4j() = FaktumTracker.toCypherQueries()
        
        /**
         * Print execution path
         */
        fun printPath() = FaktumTracker.printPath()
        
        /**
         * Clear tracking
         */
        fun clearTracking() = FaktumTracker.clear()
    }
}

// Extension functions som matcher original Faktum API

fun <T : Any> TrackedFaktum<T>.verdi(): T = this.value

fun <T : Any> TrackedFaktum<Boolean>.hvis(ja: () -> T, nei: () -> T): T {
    return if (this.value) {
        ja()
    } else {
        nei()
    }
}

fun <T : Any> TrackedFaktum<Boolean>.hvis(ja: T, nei: () -> T): T =
    hvis({ ja }, nei)

fun <T : Any> TrackedFaktum<Boolean>.hvis(ja: () -> T, nei: T): T =
    hvis(ja, { nei })

fun <T : Any> TrackedFaktum<Boolean>.hvis(ja: T, nei: T): T =
    hvis({ ja }, { nei })

fun eller(vararg fakta: TrackedFaktum<Boolean>): TrackedFaktum<Boolean> =
    TrackedFaktum(
        name = fakta.joinToString(" eller ") { it.name },
        value = fakta.any { it.value }
    )

fun og(vararg fakta: TrackedFaktum<Boolean>): TrackedFaktum<Boolean> =
    TrackedFaktum(
        name = fakta.joinToString(" og ") { it.name },
        value = fakta.all { it.value }
    )

fun <T> TrackedFaktum<T>.erLik(other: T)
        where T : Any, T : Comparable<T> = 
    if (this.value == other)
        TrackedFaktum("${name} er lik $other", true)
    else
        TrackedFaktum("${name} er ikke lik $other", false)

fun <T> TrackedFaktum<T>.erStørreEllerLik(other: T)
        where T : Any, T : Comparable<T> = 
    if (this.value >= other)
        TrackedFaktum("${name} er større eller lik $other", true)
    else
        TrackedFaktum("${name} er ikke større eller lik $other", false)

fun <T> TrackedFaktum<T>.erMindreEllerLik(other: T)
        where T : Any, T : Comparable<T> = 
    if (this.value <= other)
        TrackedFaktum("${name} er mindre eller lik $other", true)
    else
        TrackedFaktum("${name} er ikke mindre eller lik $other", false)

fun <T> TrackedFaktum<T>.erMindre(other: TrackedFaktum<T>)
        where T : Any, T : Comparable<T> = 
    if (this.value < other.value)
        TrackedFaktum("${name} er mindre enn ${other.name}", true)
    else
        TrackedFaktum("${name} er ikke mindre enn ${other.name}", false)

fun <T> TrackedFaktum<T>.erStørreEllerLik(other: TrackedFaktum<T>)
        where T : Any, T : Comparable<T> = 
    if (this.value >= other.value)
        TrackedFaktum("${name} er større eller lik ${other.name}", true)
    else
        TrackedFaktum("${name} er ikke større eller lik ${other.name}", false)

fun <T : Any> TrackedFaktum<T>.erIListen(others: List<T>) =
    if (this.value in others) 
        TrackedFaktum("${name} er i listen", true) 
    else 
        TrackedFaktum("${name} er ikke i listen", false)