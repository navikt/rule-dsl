package no.nav.system.ruledsl.core.trace

import no.nav.system.ruledsl.core.expression.Expression
import no.nav.system.ruledsl.core.expression.Faktum
import no.nav.system.ruledsl.core.expression.boolean.GuardExpression
import no.nav.system.ruledsl.core.expression.boolean.erLik
import no.nav.system.ruledsl.core.expression.boolean.erMindreEnn
import no.nav.system.ruledsl.core.expression.boolean.erStørreEllerLik
import no.nav.system.ruledsl.core.expression.math.div
import no.nav.system.ruledsl.core.expression.math.times
import kotlin.experimental.ExperimentalTypeInference


data class User(val name: String, val age: Int, val trygdetid: Int, val limitOptions: Int?)

val bob = User("Bob", 25, 14, null)

fun main() {
    println("=== Example 1: Original (English keywords) ===")
    val root = Trace("ROOT")
    val result = with(root) {
        topLevelLogic(bob)
    }
    println("Result: ${result.value}")
    println()
    println(root.debugTree())
}

/**
 * Regelsett - collects results from rules, implements first-match-wins semantics
 */
class Regelsett<T>(private val trace: Trace) {
    var result: T? = null
        private set
    var hasResult = false
        private set

    /**
     * Rule that supports both side-effects (SÅ) and value-producing (RETURNER).
     * Use SÅ for side-effects, RETURNER for returning Expression<T>.
     * Cannot use both in the same rule.
     */
    fun regel(name: String, builder: context(Trace) Rule<T>.() -> Unit) {
        if (hasResult) return

        val rule = Rule<T>(trace)
        with(trace) {
            rule.builder()
        }

        val ruleFired = rule.evaluate()
        val execution = trace.recordRule(name, ruleFired, rule.expressions())
        trace.pushContext(execution)

        if (ruleFired) {
            when {
                rule.hasReturner() && rule.hasAction() -> 
                    throw IllegalStateException("regel '$name' cannot have both SÅ and RETURNER")
                
                rule.hasReturner() -> {
                    val expression = rule.executeReturner()!!
                    @Suppress("UNCHECKED_CAST")
                    result = expression as T
                    hasResult = true
                }
                
                rule.hasAction() -> {
                    rule.executeAction()
                }
            }
        }

        trace.popContext()
    }
}

/**
 * traced - evaluates rules in sequence, returns first match
 * For non-Unit types: throws IllegalStateException if no rule matched
 * For Unit: returns without throwing
 */
context(trace: Trace)
inline fun <reified T> traced(block: context(Trace) Regelsett<T>.() -> Unit): T {
    val scope = Regelsett<T>(trace)
    with(trace) {
        scope.block()
    }

    return when {
        scope.hasResult -> scope.result!!
        T::class == Unit::class -> Unit as T
        else -> throw IllegalStateException("No rule matched")
    }
}

/**
 * Rule supporting both side-effects (SÅ) and value-producing (RETURNER).
 * Use SÅ for side-effects, RETURNER for returning Expression<T>.
 * Cannot use both in the same rule.
 */
@OptIn(ExperimentalTypeInference::class)
class Rule<T>(private val trace: Trace) {
    private val predicates = mutableListOf<Expression<Boolean>>()
    private var action: (context(Trace) () -> Unit)? = null
    private var resultBlock: (context(Trace) () -> Faktum<out Any>)? = null
    private var resultFaktum: Faktum<out Any>? = null

    /**
     * HVIS - technical predicate (null checks, validations). Terminates evaluation on false.
     */
    fun HVIS(booleanFunction: () -> Boolean) {
        predicates.add(GuardExpression(booleanFunction))
    }

    /**
     * OG - technical predicate (additional condition)
     */
    fun OG(predicate: () -> Boolean) = HVIS(predicate)

    /**
     * HVIS - domain predicate (functional business logic)
     */
    @JvmName("HVIS_domain")
    @OverloadResolutionByLambdaReturnType
    fun HVIS(predicateFunction: () -> Expression<Boolean>) {
        predicates.add(predicateFunction())
    }

    /**
     * OG - domain predicate (additional functional condition)
     */
    @JvmName("OG_domain")
    @OverloadResolutionByLambdaReturnType
    fun OG(predicateFunction: () -> Expression<Boolean>) = HVIS(predicateFunction)

    /**
     * SÅ - side-effect action (no return value).
     * Use SPOR inside to trace Faktum calculations.
     */
    fun SÅ(block: context(Trace) () -> Unit) {
        action = block
    }

    /**
     * SPOR - explicitly records a Faktum to the trace within a SÅ block.
     * Returns the Faktum for further use.
     */
    fun <R : Any> SPOR(faktum: Faktum<R>): Faktum<R> {
        trace.recordFaktum(faktum)
        return faktum
    }

    /**
     * RETURNER - value-producing action. Returns Faktum<R>.
     * The returned Faktum is automatically traced.
     * Stores the block for deferred execution (after trace context is pushed).
     */
    fun <R : Any> RETURNER(block: context(Trace) () -> Faktum<R>) {
        resultBlock = block
    }

    /**
     * Evaluates all predicates and returns true if all pass.
     *
     * Short-circuit behavior:
     * - Technical predicates (null-checks, validations): terminate immediately on false
     *   to prevent NPE in subsequent predicates that depend on the checked value.
     * - Functional predicates (domain logic): continue evaluating even if false,
     *   so all functional predicate results are available for explanation/tracing.
     */
    fun evaluate(): Boolean {
        var result = true
        for (predicate in predicates) {
            result = result && predicate.value

            if (!result && predicate is GuardExpression) {
                return false
            }
        }

        return result
    }

    fun hasAction(): Boolean = action != null

    fun executeAction() {
        action?.let { 
            with(trace) {
                it()
            }
        }
    }

    fun hasReturner(): Boolean = resultBlock != null

    /**
     * Executes the RETURNER block. Must be called after trace context is pushed.
     * Automatically records the returned Faktum to the trace.
     */
    fun executeReturner(): Faktum<out Any>? {
        resultBlock?.let { block ->
            with(trace) {
                resultFaktum = block()
                resultFaktum?.let { trace.recordFaktum(it) }
            }
        }
        return resultFaktum
    }

    fun expressions(): List<Expression<Boolean>> = predicates.filterNot { it is GuardExpression }
}


// Function-based logic instead of classes
context(trace: Trace)
fun topLevelLogic(user: User): Faktum<Int> = traced<Faktum<Int>> {
    var ageLimit = 67

    regel("technical default agelimit") {
        HVIS { user.limitOptions == null }
        SÅ {
            ageLimit = 67 + 3
        }
    }

    regel("technical agelimit using options") {
        HVIS { user.limitOptions != null }
        OG { user.limitOptions!! >= 0 }
        SÅ {
            ageLimit = 67 + user.limitOptions!!
        }
    }

    regel("age fom limit") {
        HVIS { user.age erStørreEllerLik ageLimit }
        RETURNER {
            normalRetirementCalculation(user)
        }
    }

    regel("age before limit") {
        HVIS { user.age erMindreEnn ageLimit }
        RETURNER {
            earlyRetirementCalculation(user)
        }
    }
}

context(trace: Trace)
fun earlyRetirementCalculation(user: User): Faktum<Int> = traced<Faktum<Int>> {
    val sats = Faktum("HØY SATS", 7000)

    regel("early with reduced trygdetid") {
        HVIS { user.trygdetid erMindreEnn 40 }
        RETURNER {
            Faktum("tidlig pensjon", sats * user.trygdetid / 40)
        }
    }

    regel("early with full trygdetid") {
        HVIS { user.trygdetid erLik 40 }
        RETURNER {
            Faktum("tidlig pensjon", sats)
        }
    }
}

context(trace: Trace)
fun normalRetirementCalculation(user: User): Faktum<Int> = traced<Faktum<Int>> {
    val sats = Faktum("LAV SATS", 4000)

    regel("normal with reduced trygdetid") {
        HVIS { user.trygdetid erMindreEnn 40 }
        RETURNER {
            Faktum("normal pensjon", sats * user.trygdetid / 40)
        }
    }

    regel("normal with full trygdetid") {
        HVIS { user.trygdetid erLik 40 }
        RETURNER {
            Faktum("normal pensjon", sats)
        }
    }
}