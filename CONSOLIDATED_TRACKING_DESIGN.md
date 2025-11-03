# Consolidated Tracking Design

## Executive Summary

This document specifies the unified tracking mechanism for the rule-dsl framework. The design consolidates the experimental approaches from `Faktum`, `Formel`, `Uttrykk`, `DomainPredicate`, and `ForklartFaktum` into a single, coherent system that enables comprehensive rule decision tracking with mathematical expression tracing.

### Key Objectives

1. **Unify Faktum and Formel** - Single `Faktum<T>` class handles both simple values and mathematical computations
2. **Simplify Uttrykk** - Focus on numeric operations only, removing boolean/conditional expressions
3. **Comprehensive Tracking** - Support HVA (what), HVORFOR (why), and HVORDAN (how) explanations
4. **Seamless Integration** - Work naturally with existing Rule and AbstractRuleComponent infrastructure

---

## Architecture Overview

### Core Principle

**Faktum is the single source of truth for all values in the rule system.**

- For simple values: `Faktum("alder", 67)`
- For calculations: `Faktum("beløp", 100) + Faktum("tillegg", 50)` creates a new `Faktum<Int>`
- For explanations: `faktum.forklaring()` provides HVA, HVORFOR, and HVORDAN

### Component Relationships

```
┌─────────────────────────────────────────────────────────┐
│                    Faktum<T>                            │
│  ┌───────────────────────────────────────────────────┐  │
│  │ name: String                                      │  │
│  │ value: T                                          │  │
│  │ uttrykk: Uttrykk<T>? (for numeric types)         │  │
│  │ regelContext: RegelContext? (HVORFOR tracking)   │  │
│  │ rvsId: String? (legal reference ID)              │  │
│  └───────────────────────────────────────────────────┘  │
│                                                          │
│  Methods:                                                │
│  - hva(): String                                        │
│  - hvorfor(): String                                    │
│  - hvordan(): String                                    │
│  - forklaring(): KomplettForklaring                    │
│  - Math operators: +, -, *, /, min (for Number)       │
│  - Comparison: erLik, erUlik, erMindreEnn, etc.       │
└─────────────────────────────────────────────────────────┘
                           │
                           │ uses internally
                           ▼
          ┌────────────────────────────────┐
          │       Uttrykk<T>               │
          │  (Numeric operations only)     │
          └────────────────────────────────┘
                           │
                ┌──────────┴──────────┐
                │                     │
          ┌─────▼─────┐        ┌────▼─────┐
          │   Const   │        │ Grunnlag │
          └───────────┘        └──────────┘
                │
    ┌───────────┼───────────┬─────────┐
    │           │           │         │
┌───▼───┐   ┌──▼──┐    ┌──▼──┐   ┌──▼──┐
│  Add  │   │ Sub │    │ Mul │   │ Div │
└───────┘   └─────┘    └─────┘   └─────┘
```

---

## Core Classes

### 1. Unified Faktum Class

The new `Faktum<T>` class replaces both the old `Faktum` and `Formel` classes.

```kotlin
/**
 * Unified value and computation class.
 *
 * Faktum represents all values in the rule system:
 * - Simple values: Faktum("status", APPROVED)
 * - Numeric values: Faktum("alder", 67)
 * - Calculations: Faktum("sum", 100) + Faktum("tillegg", 50)
 *
 * Each Faktum can explain:
 * - HVA: What is the value? (name and value)
 * - HVORFOR: Why was it created? (rule trace from root)
 * - HVORDAN: How was it calculated? (mathematical expression)
 */
sealed class Faktum<T : Any> : Verdi<T>, Serializable {
    abstract val name: String
    abstract val value: T
    abstract val anonymous: Boolean
    abstract val regelContext: RegelContext?
    abstract val rvsId: String?

    // Core explanation methods
    abstract fun hva(): String
    abstract fun hvorfor(): String
    abstract fun hvordan(): String
    abstract fun forklaring(): KomplettForklaring

    /**
     * Creates a named version of this Faktum.
     * Used by the faktum() function in AbstractRuleComponent.
     */
    abstract fun navngi(navn: String, context: RegelContext?, rvsId: String? = null): Faktum<T>
}

/**
 * Simple value Faktum (non-numeric).
 * Used for enums, strings, dates, booleans, etc.
 */
data class SimpleFaktum<T : Any>(
    override val name: String,
    override val value: T,
    override val anonymous: Boolean = false,
    override val regelContext: RegelContext? = null,
    override val rvsId: String? = null
) : Faktum<T>() {

    constructor(value: T) : this(
        name = "anonymous#${System.identityHashCode(value)}",
        value = value,
        anonymous = true
    )

    override fun hva(): String = if (anonymous) {
        "'$value'"
    } else {
        "'$name' = $value"
    }

    override fun hvorfor(): String = regelContext?.toForklaring() ?: "Ingen regelsporing tilgjengelig"

    override fun hvordan(): String = hva() // Simple values have no calculation

    override fun forklaring(): KomplettForklaring {
        return KomplettForklaring(
            hva = HvaForklaring(
                navn = name,
                symbolskUttrykk = name,
                konkretUttrykk = value.toString(),
                resultat = value
            ),
            hvorfor = regelContext?.toHvorforForklaring(),
            hvordan = null, // No calculation for simple values
            referanser = listOfNotNull(rvsId)
        )
    }

    override fun navngi(navn: String, context: RegelContext?, rvsId: String?): Faktum<T> {
        return copy(name = navn, anonymous = false, regelContext = context, rvsId = rvsId)
    }
}

/**
 * Numeric Faktum with expression tracking.
 * Used for Int, Long, Double, etc.
 */
data class NumericFaktum<T : Number>(
    override val name: String,
    val uttrykk: Uttrykk<T>,
    override val anonymous: Boolean = false,
    override val regelContext: RegelContext? = null,
    override val rvsId: String? = null
) : Faktum<T>(), Comparable<NumericFaktum<T>> {

    override val value: T get() = uttrykk.evaluer()

    constructor(name: String, value: T) : this(
        name = name,
        uttrykk = Const(value),
        anonymous = false
    )

    constructor(value: T) : this(
        name = "anonymous#${System.identityHashCode(value)}",
        uttrykk = Const(value),
        anonymous = true
    )

    override fun hva(): String = if (anonymous) {
        "$value"
    } else {
        "$name = $value"
    }

    override fun hvorfor(): String = regelContext?.toForklaring() ?: "Ingen regelsporing tilgjengelig"

    override fun hvordan(): String {
        return if (uttrykk is Const) {
            hva() // No calculation for constants
        } else {
            buildString {
                appendLine("$name = ${uttrykk.notasjon()}")
                appendLine("$name = ${uttrykk.konkret()}")
                appendLine("$name = $value")
            }
        }
    }

    override fun forklaring(): KomplettForklaring {
        val hvordan = if (uttrykk !is Const) {
            uttrykk.forklar(name, maxDybde = 3)
        } else null

        return KomplettForklaring(
            hva = HvaForklaring(
                navn = name,
                symbolskUttrykk = uttrykk.notasjon(),
                konkretUttrykk = uttrykk.konkret(),
                resultat = value
            ),
            hvorfor = regelContext?.toHvorforForklaring(),
            hvordan = hvordan,
            referanser = listOfNotNull(rvsId)
        )
    }

    override fun navngi(navn: String, context: RegelContext?, rvsId: String?): Faktum<T> {
        // When naming a numeric faktum, wrap it in a Grunnlag for atomic reference
        val navngittUttrykk = if (uttrykk is Grunnlag) {
            uttrykk.copy(navn = navn, rvsId = rvsId)
        } else {
            Grunnlag(navn, uttrykk.memoise(), rvsId)
        }
        return copy(name = navn, uttrykk = navngittUttrykk, anonymous = false, regelContext = context, rvsId = rvsId)
    }

    override fun compareTo(other: NumericFaktum<T>): Int {
        return value.toDouble().compareTo(other.value.toDouble())
    }

    // Math operators (hidden from user, used via extension functions)
    internal fun <R : Number> plus(other: NumericFaktum<out Number>): NumericFaktum<R> {
        return NumericFaktum(
            name = "anonymous#${System.identityHashCode(this)}${System.identityHashCode(other)}",
            uttrykk = Add(this.uttrykk, other.uttrykk),
            anonymous = true
        )
    }

    internal fun <R : Number> minus(other: NumericFaktum<out Number>): NumericFaktum<R> {
        return NumericFaktum(
            name = "anonymous#${System.identityHashCode(this)}${System.identityHashCode(other)}",
            uttrykk = Sub(this.uttrykk, other.uttrykk),
            anonymous = true
        )
    }

    internal fun <R : Number> times(other: NumericFaktum<out Number>): NumericFaktum<R> {
        return NumericFaktum(
            name = "anonymous#${System.identityHashCode(this)}${System.identityHashCode(other)}",
            uttrykk = Mul(this.uttrykk, other.uttrykk),
            anonymous = true
        )
    }

    internal fun div(other: NumericFaktum<out Number>): NumericFaktum<Double> {
        return NumericFaktum(
            name = "anonymous#${System.identityHashCode(this)}${System.identityHashCode(other)}",
            uttrykk = Div(this.uttrykk, other.uttrykk),
            anonymous = true
        )
    }

    internal fun min(other: NumericFaktum<out Number>): NumericFaktum<Double> {
        return NumericFaktum(
            name = "anonymous#${System.identityHashCode(this)}${System.identityHashCode(other)}",
            uttrykk = Min(this.uttrykk, other.uttrykk),
            anonymous = true
        )
    }
}
```

### 2. RegelContext

Tracks the rule context for HVORFOR explanations.

```kotlin
/**
 * Captures the context of where a Faktum was created.
 * Provides full tree path from root to the current rule.
 */
data class RegelContext(
    val root: AbstractRuleComponent,
    val currentRule: Rule<*>,
    val predicates: List<DomainPredicate> = emptyList()
) : Serializable {

    /**
     * Generates HVORFOR explanation string.
     */
    fun toForklaring(): String {
        val trace = root.trace { it == currentRule }
        return buildString {
            appendLine("FORDI")
            trace.forEach { component ->
                when (component) {
                    is Rule<*> -> {
                        appendLine("    regel: ${component.name()}")
                        component.children.filterIsInstance<DomainPredicate>().forEach { pred ->
                            appendLine("        predikat: $pred")
                        }
                    }
                    is AbstractRuleset -> appendLine("  regelsett: ${component.name()}")
                    is AbstractRuleflow -> appendLine("  regelflyt: ${component.name()}")
                }
            }
        }
    }

    /**
     * Generates structured HvorforForklaring.
     */
    fun toHvorforForklaring(): HvorforForklaring {
        val subsumsjoner = predicates.map { pred ->
            when (pred) {
                is PairDomainPredicate -> SubsumsjonForklaring(
                    beskrivelse = pred.toString(),
                    verdi1 = pred.verdi1.hva(),
                    komparator = pred.comparator.text,
                    verdi2 = pred.verdi2.hva(),
                    oppfylt = pred.fired
                )
                is ListDomainPredicate -> SubsumsjonForklaring(
                    beskrivelse = pred.toString(),
                    verdi1 = pred.verdi.hva(),
                    komparator = pred.comparator.text,
                    verdi2 = pred.verdiList.toString(),
                    oppfylt = pred.fired
                )
                else -> SubsumsjonForklaring(
                    beskrivelse = pred.toString(),
                    verdi1 = "",
                    komparator = "",
                    verdi2 = "",
                    oppfylt = pred.fired
                )
            }
        }

        return HvorforForklaring(
            subsumsjoner = subsumsjoner,
            underliggende = emptyList() // Can be extended for nested contexts
        )
    }
}
```

### 3. Simplified Uttrykk Hierarchy

Simplified to **numeric operations only**. Boolean operators removed.

```kotlin
/**
 * Numeric expression tree.
 *
 * IMPORTANT: This is now INTERNAL to Faktum<Number>.
 * Users do not work with Uttrykk directly - they use Faktum operators.
 *
 * Supported operations:
 * - Const: constant value
 * - Add, Sub, Mul, Div: arithmetic operations
 * - Min: minimum function
 * - Grunnlag: named expression (like locked formulas)
 * - Memo: memoization node
 *
 * Removed from Uttrykk (compared to current implementation):
 * - Og, Eller, Ikke (boolean operators)
 * - Lik, Ulik, StørreEnn, MindreEnn, etc. (comparison operators)
 * - Hvis (conditional expression)
 * - Feil (error expression)
 * - Tabell (table-based rules)
 * - ErBlant, ErIkkeBlant (list membership)
 */
sealed interface Uttrykk<out T : Any> : Serializable {
    fun evaluer(): T
    fun notasjon(): String
    fun konkret(): String
    fun grunnlagListe(): List<Grunnlag<out Any>>
    fun dybde(): Int
}

// Only numeric expression nodes remain:
data class Const<T : Any>(val verdi: T) : Uttrykk<T>
data class Add<T : Number>(val venstre: Uttrykk<out Number>, val høyre: Uttrykk<out Number>) : Uttrykk<T>
data class Sub<T : Number>(val venstre: Uttrykk<out Number>, val høyre: Uttrykk<out Number>) : Uttrykk<T>
data class Mul<T : Number>(val venstre: Uttrykk<out Number>, val høyre: Uttrykk<out Number>) : Uttrykk<T>
data class Div(val venstre: Uttrykk<out Number>, val høyre: Uttrykk<out Number>) : Uttrykk<Double>
data class Min(val venstre: Uttrykk<out Number>, val høyre: Uttrykk<out Number>) : Uttrykk<Double>
data class Neg<T : Number>(val uttrykk: Uttrykk<out Number>) : Uttrykk<T>

/**
 * Named expression - treated as atomic unit.
 * This is the ONLY Uttrykk that can be created by users (via Const.navngi).
 */
data class Grunnlag<T : Any>(
    val navn: String,
    val uttrykk: Uttrykk<T>,
    val rvsId: String? = null
) : Uttrykk<T>

/**
 * Memoization node.
 */
data class Memo<T : Any>(val uttrykk: Uttrykk<T>) : Uttrykk<T>
```

**Key Restriction**: Only `Const` can be transformed to `Grunnlag`:

```kotlin
/**
 * ONLY available on Const.
 * Transforms an anonymous constant into a named Grunnlag.
 */
fun <T : Any> Const<T>.navngi(navn: String, rvsId: String? = null): Grunnlag<T> {
    return Grunnlag(navn, this, rvsId)
}

// This is NOT available on other Uttrykk types
// Example: (a + b).navngi("sum") is NOT allowed
```

### 4. Integration with AbstractRuleComponent

```kotlin
abstract class AbstractRuleComponent : Serializable {
    // ... existing code ...

    /**
     * Creates a tracked Faktum with rule context.
     *
     * This is the PRIMARY way to create tracked Faktum instances in rules.
     *
     * Usage:
     * ```kotlin
     * regel("Calculate something") {
     *     HVIS { someCondition }
     *     SÅ {
     *         result = faktum("result", baseValue + adjustment)
     *     }
     * }
     * ```
     */
    fun <T : Any> faktum(name: String, value: T, rvsId: String? = null): Faktum<T> {
        val context = RegelContext(
            root = this.root(),
            currentRule = this as? Rule<*> ?: throw IllegalStateException("faktum() can only be called from within a Rule"),
            predicates = (this as Rule<*>).children.filterIsInstance<DomainPredicate>()
        )

        return when (value) {
            is Faktum<*> -> {
                // If value is already a Faktum, name it with context
                @Suppress("UNCHECKED_CAST")
                value.navngi(name, context, rvsId) as Faktum<T>
            }
            is Number -> {
                // Create NumericFaktum
                @Suppress("UNCHECKED_CAST")
                NumericFaktum(name, Const(value), anonymous = false, regelContext = context, rvsId = rvsId) as Faktum<T>
            }
            else -> {
                // Create SimpleFaktum
                SimpleFaktum(name, value, anonymous = false, regelContext = context, rvsId = rvsId)
            }
        }
    }

    /**
     * DEPRECATED: Old faktum() that took Formel.
     * Kept temporarily for backwards compatibility.
     */
    @Deprecated("Use faktum(name, value) instead", ReplaceWith("faktum(name, value)"))
    fun <T : Number> faktum(formel: Formel<T>): ForklartFaktum<T> {
        // ... migration path ...
    }
}
```

---

## Operator Overloading

### Numeric Operations

Math operators are exposed via extension functions on `Faktum<Number>`:

```kotlin
// Arithmetic operators
operator fun <T : Number> Faktum<T>.plus(other: Faktum<out Number>): Faktum<T> {
    require(this is NumericFaktum && other is NumericFaktum) { "Only NumericFaktum supports arithmetic" }
    return this.plus(other)
}

operator fun <T : Number> Faktum<T>.plus(other: Number): Faktum<T> {
    require(this is NumericFaktum) { "Only NumericFaktum supports arithmetic" }
    return this.plus(NumericFaktum(other))
}

operator fun <T : Number> Faktum<T>.minus(other: Faktum<out Number>): Faktum<T> {
    require(this is NumericFaktum && other is NumericFaktum) { "Only NumericFaktum supports arithmetic" }
    return this.minus(other)
}

operator fun <T : Number> Faktum<T>.minus(other: Number): Faktum<T> {
    require(this is NumericFaktum) { "Only NumericFaktum supports arithmetic" }
    return this.minus(NumericFaktum(other))
}

operator fun <T : Number> Faktum<T>.times(other: Faktum<out Number>): Faktum<T> {
    require(this is NumericFaktum && other is NumericFaktum) { "Only NumericFaktum supports arithmetic" }
    return this.times(other)
}

operator fun <T : Number> Faktum<T>.times(other: Number): Faktum<T> {
    require(this is NumericFaktum) { "Only NumericFaktum supports arithmetic" }
    return this.times(NumericFaktum(other))
}

operator fun Faktum<out Number>.div(other: Faktum<out Number>): Faktum<Double> {
    require(this is NumericFaktum && other is NumericFaktum) { "Only NumericFaktum supports arithmetic" }
    return this.div(other)
}

operator fun Faktum<out Number>.div(other: Number): Faktum<Double> {
    require(this is NumericFaktum) { "Only NumericFaktum supports arithmetic" }
    return this.div(NumericFaktum(other))
}

operator fun <T : Number> Faktum<T>.unaryMinus(): Faktum<T> {
    require(this is NumericFaktum) { "Only NumericFaktum supports arithmetic" }
    return NumericFaktum(
        name = "anonymous#${System.identityHashCode(this)}",
        uttrykk = Neg(this.uttrykk),
        anonymous = true
    )
}

// Utility functions
fun min(a: Faktum<out Number>, b: Faktum<out Number>): Faktum<Double> {
    require(a is NumericFaktum && b is NumericFaktum) { "Only NumericFaktum supports min" }
    return a.min(b)
}
```

### Comparison Operations

Comparison operators are integrated directly into `Faktum`, replacing `DomainPredicate`:

```kotlin
/**
 * Comparison result that tracks the operands and operator.
 * Replaces DomainPredicate for Faktum comparisons.
 */
data class FaktumComparison(
    val verdi1: Faktum<*>,
    val comparator: PairComparator,
    val verdi2: Faktum<*>,
    val resultat: Boolean
) : Serializable {

    fun forklar(): SubsumsjonForklaring {
        return SubsumsjonForklaring(
            beskrivelse = "$verdi1 ${comparator.text} $verdi2",
            verdi1 = verdi1.hva(),
            komparator = comparator.text,
            verdi2 = verdi2.hva(),
            oppfylt = resultat
        )
    }

    override fun toString(): String {
        val komparatorText = if (resultat) comparator.text else comparator.negated()
        return "${verdi1.hva()} $komparatorText ${verdi2.hva()}"
    }
}

// Comparison operators on Faktum
infix fun <T : Comparable<T>> Faktum<T>.erLik(other: Faktum<T>): FaktumComparison {
    return FaktumComparison(this, PairComparator.EQUAL, other, this.value == other.value)
}

infix fun <T : Comparable<T>> Faktum<T>.erLik(other: T): FaktumComparison {
    return this erLik SimpleFaktum(other)
}

infix fun <T : Comparable<T>> Faktum<T>.erUlik(other: Faktum<T>): FaktumComparison {
    return FaktumComparison(this, PairComparator.NOT_EQUAL, other, this.value != other.value)
}

infix fun <T : Comparable<T>> Faktum<T>.erMindreEnn(other: Faktum<T>): FaktumComparison {
    return FaktumComparison(this, PairComparator.LESS_THAN, other, this.value < other.value)
}

infix fun <T : Comparable<T>> Faktum<T>.erStørreEnn(other: Faktum<T>): FaktumComparison {
    return FaktumComparison(this, PairComparator.GREATER_THAN, other, this.value > other.value)
}

infix fun <T : Comparable<T>> Faktum<T>.erMindreEllerLik(other: Faktum<T>): FaktumComparison {
    return FaktumComparison(this, PairComparator.LESS_THAN_OR_EQUAL, other, this.value <= other.value)
}

infix fun <T : Comparable<T>> Faktum<T>.erStørreEllerLik(other: Faktum<T>): FaktumComparison {
    return FaktumComparison(this, PairComparator.GREATER_THAN_OR_EQUAL, other, this.value >= other.value)
}

// Date-specific comparisons
infix fun Faktum<LocalDate>.erFør(other: Faktum<LocalDate>): FaktumComparison =
    this.erMindreEnn(other)

infix fun Faktum<LocalDate>.erEtter(other: Faktum<LocalDate>): FaktumComparison =
    this.erStørreEnn(other)

infix fun Faktum<LocalDate>.erFørEllerLik(other: Faktum<LocalDate>): FaktumComparison =
    this.erMindreEllerLik(other)

infix fun Faktum<LocalDate>.erEtterEllerLik(other: Faktum<LocalDate>): FaktumComparison =
    this.erStørreEllerLik(other)
```

### Rule Integration

Rules must be updated to work with `FaktumComparison`:

```kotlin
/**
 * DSL: Functional Predicate entry for FaktumComparison.
 */
@OverloadResolutionByLambdaReturnType
@JvmName("faktumComparisonHVIS")
fun HVIS(comparisonFunction: () -> FaktumComparison) {
    OG(comparisonFunction)
}

@OverloadResolutionByLambdaReturnType
@JvmName("faktumComparisonOG")
fun OG(comparisonFunction: () -> FaktumComparison) {
    predicateFunctionList.add {
        val comparison = comparisonFunction.invoke()
        // Convert FaktumComparison to Predicate
        object : Predicate({ comparison.resultat }) {
            override fun toString() = comparison.toString()
        }
    }
}
```

---

## Usage Examples

### Example 1: Simple Calculation

```kotlin
regel("Calculate monthly benefit") {
    HVIS { person.alder erStørreEllerLik 67 }
    SÅ {
        val G = Faktum("G", 118620)
        val sats = Faktum("sats", 0.25)
        val måneder = Faktum("måneder", 12)

        // Math operations create anonymous Faktum
        val beregning = sats * G / måneder

        // faktum() names it and adds tracking
        monthlyBenefit = faktum("monthlyBenefit", beregning, rvsId = "BEREGNING-MÅNEDLIG")
    }
}

// Later:
println(monthlyBenefit.forklaring().toText())
```

**Output**:
```
HVA: monthlyBenefit = 2467.92

REFERANSE
    BEREGNING-MÅNEDLIG

FORDI
    regel: Calculate monthly benefit
        predikat: person.alder er større eller lik 67 -> 70 er større eller lik 67

HVORDAN
    monthlyBenefit = sats * G / måneder
    monthlyBenefit = 0.25 * 118620 / 12
    monthlyBenefit = 2467.92

    sats = 0.25
    G = 118620
    måneder = 12
```

### Example 2: Complex Calculation with Nested Faktum

```kotlin
regel("Calculate slitertillegg") {
    HVIS { trygdetid erMindreEnn fullTrygdetid }
    SÅ {
        // Step 1: Calculate base amount
        val G = Faktum("G", 110000)
        val fulltBeløp = faktum("fulltSlitertillegg", 0.25 * G / 12, rvsId = "SLITER-BEREGNING-UAVKORTET")

        // Step 2: Calculate adjustment factor
        val faktiskTrygdetid = Faktum("faktiskTrygdetid", person.trygdetid)
        val fullTrygdetid = Faktum("fullTrygdetid", 40)
        val faktor = faktum("trygdetidFaktor", faktiskTrygdetid / fullTrygdetid, rvsId = "SLITER-AVKORTING-TRYGDETID")

        // Step 3: Combine
        slitertillegg = faktum("slitertillegg", fulltBeløp * faktor, rvsId = "SLITER-BEREGNET")
    }
}

// Explanation:
println(slitertillegg.forklaring().toText())
```

**Output**:
```
HVA: slitertillegg = 1718.75

REFERANSE
    SLITER-BEREGNET

FORDI
    regel: Calculate slitertillegg
        predikat: trygdetid er mindre enn fullTrygdetid -> 30 er mindre enn 40

HVORDAN
    SLITER-BEREGNET
    slitertillegg = fulltSlitertillegg * trygdetidFaktor
    slitertillegg = 2291.67 * 0.75
    slitertillegg = 1718.75

    SLITER-BEREGNING-UAVKORTET
    fulltSlitertillegg = 0.25 * G / 12
    fulltSlitertillegg = 0.25 * 110000 / 12
    fulltSlitertillegg = 2291.67

    SLITER-AVKORTING-TRYGDETID
    trygdetidFaktor = faktiskTrygdetid / fullTrygdetid
    trygdetidFaktor = 30 / 40
    trygdetidFaktor = 0.75

    G = 110000
    faktiskTrygdetid = 30
    fullTrygdetid = 40
```

### Example 3: Chained Comparisons

```kotlin
regel("Approve application") {
    HVIS { numOfMonths erStørreEnn 12 }
    OG { applicant.status erLik ELIGIBLE }
    SÅ {
        result = faktum("approvalStatus", APPROVED)
    }
}

// Where numOfMonths was created earlier:
regel("Calculate duration") {
    HVIS { startDate erFør endDate }
    SÅ {
        val start = Faktum("startDate", LocalDate.of(2023, 1, 1))
        val end = Faktum("endDate", LocalDate.of(2024, 3, 1))
        val base = Faktum("baseMonths", 12)
        val extra = Faktum("extraMonths", 3)

        numOfMonths = faktum("numOfMonths", base + extra)
    }
}

// Full explanation chains both rules:
println(result.forklaring().toText())
```

**Output**:
```
HVA: approvalStatus = APPROVED

FORDI
    regel: Approve application
        predikat: numOfMonths er større enn 12 -> 15 er større enn 12
            faktum: numOfMonths = 15
                FORDI:
                    regel: Calculate duration
                        predikat: startDate er før endDate -> 2023-01-01 er før 2024-03-01
                HVORDAN:
                    numOfMonths = baseMonths + extraMonths
                    numOfMonths = 12 + 3
                    numOfMonths = 15

        predikat: applicant.status er lik ELIGIBLE -> ELIGIBLE er lik ELIGIBLE

HVORDAN: Resultat gitt av regelbesluttning
```

---

## Migration Path

### Phase 1: Add New Classes Alongside Old

1. Add `SimpleFaktum` and `NumericFaktum` classes
2. Add `RegelContext` class
3. Add `FaktumComparison` class
4. Add new `faktum()` function to `AbstractRuleComponent`
5. Mark old classes as `@Deprecated`

### Phase 2: Update Operators

1. Add new operator overloads for new `Faktum` types
2. Add comparison operators returning `FaktumComparison`
3. Update `Rule` to support `FaktumComparison` in predicates
4. Keep old operators for backwards compatibility

### Phase 3: Migrate Tests

1. Update test cases to use new API
2. Verify output matches old behavior
3. Compare explanations for correctness

### Phase 4: Update Documentation

1. Update CLAUDE.md with new patterns
2. Add migration guide for users
3. Document breaking changes

### Phase 5: Remove Old Code

1. Remove deprecated `Formel` class
2. Remove deprecated `ForklartFaktum` class
3. Remove deprecated `faktum(formel)` function
4. Remove boolean/conditional classes from `Uttrykk`
5. Clean up unused extension functions

---

## Implementation Notes

### Type Safety Considerations

1. **NumericFaktum type parameter**: Use `T : Number` but operations may return different types:
   - `Int + Int = Int`
   - `Int / Int = Double`
   - `Double + Int = Double`

2. **Comparison type bounds**: Use `T : Comparable<T>` for comparison operators

3. **Sealed class hierarchy**: Use sealed classes for exhaustive when expressions

### Performance Considerations

1. **Lazy evaluation**: `value` property should be lazy to avoid unnecessary calculations
2. **Memoization**: Use `Memo` nodes for expensive calculations
3. **RegelContext caching**: Consider caching trace results in `RegelContext`

### Testing Strategy

1. **Unit tests**: Each class and operator independently
2. **Integration tests**: Full rule execution with tracking
3. **Explanation tests**: Verify output format and content
4. **Migration tests**: Ensure old code still works during migration

### Open Questions

1. **Should we support mixed numeric types?** (e.g., `Int + Double`)
   - Recommendation: Yes, always promote to `Double` when mixing types

2. **How deep should HVORFOR trace go by default?**
   - Recommendation: Full path, but allow configurable depth

3. **Should we cache forklaring() results?**
   - Recommendation: Yes, cache in `Faktum` after first call

4. **How to handle circular dependencies in Faktum?**
   - Recommendation: Detect cycles and throw exception

---

## Conclusion

This design consolidates the tracking mechanisms into a single, coherent system centered on the unified `Faktum` class. The key benefits are:

1. **Simplicity**: One class for all values (numeric and non-numeric)
2. **Consistency**: Same API for simple values and calculations
3. **Traceability**: Complete HVA, HVORFOR, and HVORDAN explanations
4. **Type Safety**: Proper type checking via sealed classes and generics
5. **Performance**: Lazy evaluation and memoization
6. **Maintainability**: Clear separation of concerns

The migration path allows gradual adoption while maintaining backwards compatibility. Once complete, the framework will have a clean, unified tracking system that integrates seamlessly with the existing Rule DSL.
