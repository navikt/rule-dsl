package no.nav.system.ruledsl.core.trace.demo

import no.nav.system.ruledsl.core.expression.Faktum
import no.nav.system.ruledsl.core.expression.boolean.erLik
import no.nav.system.ruledsl.core.expression.boolean.erMindreEnn
import no.nav.system.ruledsl.core.expression.boolean.erStørreEllerLik
import no.nav.system.ruledsl.core.expression.math.div
import no.nav.system.ruledsl.core.expression.math.times
import no.nav.system.ruledsl.core.trace.DefaultTracer
import no.nav.system.ruledsl.core.trace.RuleContext
import no.nav.system.ruledsl.core.trace.Tracer
import no.nav.system.ruledsl.core.trace.traced

/**
 * Demo: Pension calculation with rule tracing.
 *
 * This demonstrates the core-trace DSL for defining business rules
 * with automatic tracing of decisions (WHY) and calculations (HOW).
 */

data class User(val name: String, val age: Int, val trygdetid: Int, val limitOptions: Int?)

fun main() {
    val bob = User("Bob", 25, 14, null)

    println("=== Pension Calculation Demo ===")
    println("User: ${bob.name}, age: ${bob.age}, trygdetid: ${bob.trygdetid}")
    println()

    val ruleContext = RuleContext(
        mutableMapOf(Tracer::class to DefaultTracer("PensionDemo"))
    )
    val result = with(ruleContext) {
        calculatePension(bob)
    }

    println("Result: ${result.name} = ${result.value}")
    println()
    println(ruleContext.debugTree())
}

/**
 * Top-level pension calculation logic.
 * Determines age limit and routes to appropriate calculation.
 */
context(ruleContext: RuleContext)
fun calculatePension(user: User): Faktum<Double> = traced<Faktum<Double>> {
    var ageLimit = 67

    regel("set default age limit") {
        HVIS { user.limitOptions == null }
        SÅ {
            ageLimit = 67 + 3
        }
    }

    // null-check guards against further evaluation that would lead to NPE.
    regel("set age limit from options") {
        HVIS { user.limitOptions != null }
        OG { user.limitOptions!! >= 0 }
        OG { user.limitOptions!! erStørreEllerLik 0 }
        SÅ {
            ageLimit = 67 + user.limitOptions!!
        }
    }

    regel("normal retirement (age >= limit)") {
        HVIS { user.age erStørreEllerLik ageLimit }
        RETURNER {
            normalRetirementCalculation(user)
        }
    }

    regel("early retirement (age < limit)") {
        HVIS { user.age erMindreEnn ageLimit }
        RETURNER {
            earlyRetirementCalculation(user)
        }
    }
}

/**
 * Calculate early retirement pension.
 * Uses higher rate, reduced by trygdetid if less than 40 years.
 */
context(ruleContext: RuleContext)
fun earlyRetirementCalculation(user: User): Faktum<Double> = traced<Faktum<Double>> {
    val sats = Faktum("høy sats", 7000)

    regel("reduced by trygdetid") {
        HVIS { user.trygdetid erMindreEnn 40 }
        RETURNER {
            Faktum("tidlig pensjon", sats * user.trygdetid / 40)
        }
    }

    regel("full trygdetid") {
        HVIS { user.trygdetid erLik 40 }
        RETURNER {
            Faktum("tidlig pensjon", sats.value.toDouble())
        }
    }
}

/**
 * Calculate normal retirement pension.
 * Uses lower rate, reduced by trygdetid if less than 40 years.
 */
context(ruleContext: RuleContext)
fun normalRetirementCalculation(user: User): Faktum<Double> = traced<Faktum<Double>> {
    val sats = Faktum("lav sats", 4000)

    regel("reduced by trygdetid") {
        HVIS { user.trygdetid erMindreEnn 40 }
        RETURNER {
            Faktum("normal pensjon", sats * user.trygdetid / 40)
        }
    }

    regel("full trygdetid") {
        HVIS { user.trygdetid erLik 40 }
        RETURNER {
            Faktum("normal pensjon", sats.value.toDouble())
        }
    }
}
