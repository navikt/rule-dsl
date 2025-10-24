# Faktum Trace Information Options

This document outlines different approaches to making it easy to create `Faktum` instances with trace information (HVORFOR/HVORDAN) in the `SÅ` lambda block.

## Problem Statement

When creating `Faktum` objects in rule `SÅ` blocks, we need to capture:
- **HVA** (What): The name and value of the faktum
- **HVORFOR** (Why): The trace of which predicates/rules led to this result
- **HVORDAN** (How): The formula/calculation that produced the value

This should work in multiple scenarios:
1. When returning a Faktum via `RETURNER()`
2. When creating multiple Faktum in a single rule
3. When assigning a Faktum to a domain class property

---

## Option 1: Context Parameters

Use Kotlin context receivers to automatically provide rule context to the `SÅ` block.

### Implementation

```kotlin
// Define a context that provides tracing capabilities
class RuleContext(private val rule: Rule<*>) {
    fun buildHvorfor(): String {
        // Build HVORFOR trace by examining fired predicates
        return rule.children
            .filterIsInstance<DomainPredicate>()
            .filter { it.fired }
            .joinToString("\n") { pred ->
                "    FORDI\n        ${pred.explanation()}"
            }
    }

    fun buildHvordan(formel: Formel<*>): String {
        // Build HVORDAN trace from formula structure
        return formel.render()
    }
}

// Modify Rule class to use context receiver
context(RuleContext)
class Rule<T : Any>(...) {

    fun SÅ(action: context(RuleContext) () -> Unit) {
        this.actionStatement = {
            val ruleContext = RuleContext(this)
            action(ruleContext)
        }
    }
}

// Extension function to create Faktum with automatic trace
context(RuleContext)
fun <T : Any> faktum(
    name: String,
    value: T,
    hvordan: Formel<*>? = null
): Faktum<T> {
    return Faktum(name, value).apply {
        this.hvorfor = this@RuleContext.buildHvorfor()
        hvordan?.let { this.hvordan = this@RuleContext.buildHvordan(it) }
    }
}
```

### Usage Example

```kotlin
regel("BeregnSlitertillegg") {
    HVIS { antallMånederEtterNedreAldersgrense erMindreEnn MND_36 }
    SÅ {
        val beregnet = faktum(
            name = "slitertilleggBeregnet",
            value = 378.0,
            hvordan = fulltSlitertillegg * justeringsFaktor * trygdetidFaktor
        )

        RETURNER(SlitertilleggFaktum(beregnet))
    }
}
```

### Pros
- ✅ Clean, minimal boilerplate
- ✅ Type-safe at compile time
- ✅ Context automatically available in `SÅ` blocks
- ✅ Works for all scenarios (RETURNER, assignment, multiple Faktum)
- ✅ Natural Kotlin idiom (context receivers)

### Cons
- ⚠️ Requires Kotlin 1.6.20+ (current project is on 2.1.0 ✓)
- ⚠️ Context receivers are still experimental (may need `-Xcontext-receivers` flag)
- ⚠️ Less familiar to developers not using context receivers

---

## Option 2: Receiver Extension on Rule

Make the `SÅ` lambda execute with `Rule` as its receiver, providing extension functions on `Rule` to create traced `Faktum`.

### Implementation

```kotlin
class Rule<T : Any>(...) {

    // SÅ block runs with Rule as receiver
    fun SÅ(action: Rule<T>.() -> Unit) {
        this.actionStatement = { this.action() }
    }

    // Extension function on Rule to create traced Faktum
    fun <V : Any> faktum(
        name: String,
        value: V,
        hvordan: Formel<*>? = null
    ): Faktum<V> {
        return Faktum(name, value).apply {
            // Build HVORFOR from this rule's predicates
            this.hvorfor = buildHvorfor()

            // Build HVORDAN from formula
            hvordan?.let { this.hvordan = it.render() }
        }
    }

    private fun buildHvorfor(): String {
        return children
            .filterIsInstance<DomainPredicate>()
            .filter { it.fired }
            .joinToString("\n") { pred ->
                "    FORDI\n        ${pred.explanation()}"
            }
    }
}
```

### Usage Example

```kotlin
regel("BeregnSlitertillegg") {
    HVIS { antallMånederEtterNedreAldersgrense erMindreEnn MND_36 }
    SÅ {
        // 'this' is Rule<T>, so faktum() is available
        val beregnet = faktum(
            name = "slitertilleggBeregnet",
            value = 378.0,
            hvordan = fulltSlitertillegg * justeringsFaktor * trygdetidFaktor
        )

        RETURNER(SlitertilleggFaktum(beregnet))
    }
}
```

### Pros
- ✅ No experimental features required
- ✅ Simple to implement and understand
- ✅ Direct access to rule context
- ✅ Works with current Kotlin version
- ✅ Familiar pattern (receiver lambdas are common in Kotlin)

### Cons
- ⚠️ Receiver type (`Rule<T>`) might conflict with domain model methods if they have same names
- ⚠️ Less elegant than context parameters
- ⚠️ Makes `this` refer to `Rule` instead of enclosing class (might be confusing)

---

## Option 3: AbstractResourceAccessor Extension

Leverage the existing `AbstractResourceAccessor` inheritance hierarchy to provide `faktum` creation. Since `Rule` inherits from `AbstractResourceAccessor`, it can access `root()` for tracing.

**Note:** This pattern is already partially implemented in `AbstractResourceAccessor.kt:34-41` and uses `ForklartFaktum<T>`.

### Implementation

```kotlin
abstract class AbstractResourceAccessor : AbstractRuleComponent() {

    /**
     * Creates a Faktum with full trace information.
     *
     * @param name the name of the faktum (HVA component)
     * @param value the value of the faktum (HVA component)
     * @param formel the formula explaining how the value was computed (HVORDAN component)
     * @return ForklartFaktum with HVORFOR trace automatically captured
     */
    fun <T : Any> faktum(
        name: String,
        value: T,
        formel: Formel<out T>
    ): ForklartFaktum<T> {
        return ForklartFaktum(
            navn = name,
            verdi = value,
            hvorfor = RootTrace(this@AbstractResourceAccessor),
            hvordan = formel
        )
    }

    /**
     * Creates a simple Faktum without formula (for non-computed values).
     */
    fun <T : Any> faktum(
        name: String,
        value: T
    ): ForklartFaktum<T> {
        return ForklartFaktum(
            navn = name,
            verdi = value,
            hvorfor = RootTrace(this@AbstractResourceAccessor),
            hvordan = Formel.constant(name, value)
        )
    }
}

/**
 * ForklartFaktum extends Faktum with trace information.
 */
class ForklartFaktum<T : Any>(
    navn: String,
    verdi: T,
    val hvorfor: RootTrace,
    val hvordan: Formel<out Any>
) : Faktum<T>(navn, verdi) {

    fun forklaring(): String = buildString {
        appendLine("HVA")
        appendLine("    $name = $value")
        appendLine()
        appendLine("HVORFOR")
        appendLine(hvorfor.toText().prependIndent("    "))
        appendLine()
        appendLine("HVORDAN")
        appendLine(hvordan.render().prependIndent("    "))
    }

    override fun toString(): String {
        return if (anonymous) {
            "'$value'"
        } else {
            "'$name' ($value)\n${forklaring()}"
        }
    }
}

/**
 * RootTrace captures the execution path from root to current component.
 */
data class RootTrace(
    val arc: AbstractResourceAccessor
) {
    fun toText(): String = arc.root().trace(target = { it == arc })
}
```

### Usage Example

```kotlin
class BeregnSlitertilleggRS(...) : AbstractRuleset<SlitertilleggFaktum>() {

    override fun create() {
        regel("BeregnSlitertillegg") {
            HVIS { antallMånederEtterNedreAldersgrense erMindreEnn MND_36 }
            SÅ {
                // faktum() is inherited from AbstractResourceAccessor
                val beregnet = faktum(
                    name = "slitertilleggBeregnet",
                    value = 378.0,
                    formel = fulltSlitertillegg * justeringsFaktor * trygdetidFaktor
                )

                RETURNER(SlitertilleggFaktum(beregnet))
            }
        }
    }
}
```

### Pros
- ✅ Leverages existing inheritance hierarchy
- ✅ No DSL changes required
- ✅ Access to `root()` for comprehensive tracing
- ✅ `ForklartFaktum` naturally extends `Faktum`
- ✅ Works in any context where `AbstractResourceAccessor` is available
- ✅ No experimental features
- ✅ Already partially implemented in codebase

### Cons
- ⚠️ Only works within components that inherit from `AbstractResourceAccessor`
- ⚠️ Requires `formel` parameter (though could provide overload for non-computed values)
- ⚠️ Less flexible than context-based approaches for customization
- ⚠️ `this` reference in `SÅ` block is the enclosing `Ruleset`, not the `Rule` itself (but can still call `faktum()`)

---

## Comparison Matrix

| Feature | Option 1: Context Parameters | Option 2: Rule Receiver | Option 3: ResourceAccessor |
|---------|------------------------------|-------------------------|----------------------------|
| **No experimental features** | ❌ | ✅ | ✅ |
| **Automatic trace capture** | ✅ | ✅ | ✅ |
| **Works in all scenarios** | ✅ | ✅ | ⚠️ (only in ResourceAccessor) |
| **Clean user code** | ✅ | ✅ | ✅ |
| **Easy to customize** | ✅ | ⚠️ | ⚠️ |
| **Familiar to Kotlin devs** | ⚠️ | ✅ | ✅ |
| **Already in codebase** | ❌ | ❌ | ⚠️ (partial) |
| **Type-safe** | ✅ | ✅ | ✅ |

---

## Recommendation

**Short-term:** Option 3 (AbstractResourceAccessor) - Complete the existing implementation
- Already partially implemented
- No breaking changes
- Works immediately

**Long-term:** Consider Option 1 (Context Parameters) when context receivers become stable
- Most flexible and clean
- Better separation of concerns
- Natural Kotlin idiom

**Alternative:** Option 2 (Rule Receiver) as a stable middle ground
- No experimental features
- Clean and simple
- Easy migration path from Option 3
