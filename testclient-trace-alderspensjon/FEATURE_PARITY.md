# Feature Parity: core-trace vs core

This document compares the features of the new `core-trace` module against the original `core` module.

## Available Features ✅

| Feature | core | core-trace | Notes |
|---------|------|------------|-------|
| `regel()` with `HVIS`/`OG` | ✅ | ✅ | Identical DSL |
| `SÅ` for side-effects | ✅ | ✅ | Returns Unit |
| `RETURNER` for values | ✅ | ✅ | Must return `Faktum<T>` |
| Technical predicates (guard) | ✅ | ✅ | Short-circuits on false |
| Domain predicates (`erLik`, `erMindreEnn`, etc.) | ✅ | ✅ | Traced for explanation |
| List predicates (`erBlant`, `erIkkeBlant`) | ✅ | ✅ | |
| `Faktum` for named values | ✅ | ✅ | Same concept, different package |
| Math expressions (`+`, `-`, `*`, `/`) | ✅ | ✅ | Traced in formula output |
| Resource system | ✅ | ✅ | Via `ResourceAccessor` interface |
| Extension functions for resources | ✅ | ✅ | Works on `ResourceAccessor` |
| Execution tracing | ✅ | ✅ | Different approach (Trace context) |
| Nested rule calls | ✅ | ✅ | Via function calls with context parameter |
| `SPOR` for explicit tracing | ❌ | ✅ | New feature for SÅ blocks |

## Missing Features ❌

### 1. Pattern System
**Impact: HIGH**

The old system had `createPattern` for list-based rule evaluation:
```kotlin
val norskeBoperioder = boperiodeListe.createPattern { it.land == LandEnum.NOR }

regel("BoPeriode", norskeBoperioder) { boperiode ->
    HVIS { boperiode.fom < dato16år }
    SÅ { ... }
}
```

Each list element created its own rule instance with individual tracing.

**Workaround:** Manual iteration with regular code, but loses per-item tracing.

### 2. ELLERS (Else branch)
**Impact: MEDIUM**

```kotlin
regel("Example") {
    HVIS { condition }
    SÅ { ... }
    ELLERS { ... }  // Not available
}
```

**Workaround:** Create a second rule with inverted condition.

### 3. kommentar() - Rule Documentation
**Impact: LOW**

```kotlin
regel("Example") {
    HVIS { ... }
    kommentar("Legal reference: Ftrl § 3-2")
}
```

**Workaround:** Use code comments. No trace output for documentation.

### 5. sporing() Helper Function
**Impact: LOW**

The old `sporing("name", value)` convenience function:
```kotlin
RETURNER(sporing("Anvendt flyktning", UtfallType.OPPFYLT))
```

**Workaround:** Use `Faktum("name", value)` directly.

### 6. AbstractRuleService / AbstractRuleflow / AbstractRuleset Classes
**Impact: MEDIUM**

Class-based organization with inheritance:
```kotlin
class MyRuleset : AbstractRuleset<Response>() {
    override fun create() { ... }
}
```

**core-trace approach:** Function-based with context parameters:
```kotlin
context(trace: Trace)
fun myRuleset(): Faktum<Response> = traced { ... }
```

### 7. forgrening/gren/betingelse/flyt (Decision branching DSL)
**Impact: MEDIUM**

```kotlin
forgrening("Sivilstand?") {
    gren {
        betingelse("Gift") { person.erGift }
        flyt { ... }
    }
    gren {
        betingelse("Ugift") { !person.erGift }
        flyt { ... }
    }
}
```

**Workaround:** Use regular Kotlin `if`/`when` statements, but loses semantic tracing of decisions.

## Resolved Issues ✅

### Domain Predicate Eager Evaluation (FIXED)
Previously, domain predicates after a null-guard would cause NPE at build time:
```kotlin
HVIS { obj != null }                    // Guard - OK
OG { obj!!.field erLik value }          // Was: NPE at build time
```

**Fix:** Domain predicates are now wrapped in `DomainPredicate` which defers evaluation
until the predicate's value is accessed. Guards short-circuit before lazy predicates are evaluated.

If a guard short-circuits, the trace output shows `- (not evaluated)` for subsequent predicates.

### Pattern System (FIXED)
Now supported via overloaded `regel`:
```kotlin
val boperioder = listOf(Boperiode(...), Boperiode(...))

regel("process boperiode", boperioder) { boperiode ->
    HVIS { boperiode.land == LandEnum.NOR }
    SÅ { totalMonths += boperiode.months }
}
```

Each element gets its own traced rule (e.g., "process boperiode.1", "process boperiode.2").

### Rule Introspection (FIXED)
Rules now return `RuleResult` which implements `Expression<Boolean>`:
```kotlin
val r1 = regel("AngittFlyktning") { ... }
val r2 = regel("HarUnntak") { ... }

regel("Decision") {
    HVIS { r1 }  // Traces as "regel 'AngittFlyktning' har truffet"
    ...
}
```

For pattern rules, use `minstEnHarTruffet()` or `ingenHarTruffet()`:
```kotlin
val overgangsregler = regel("Overgang", items) { item -> ... }

regel("Decision") {
    HVIS { overgangsregler.minstEnHarTruffet() }  // At least one fired
    HVIS { overgangsregler.ingenHarTruffet() }    // None fired
    ...
}
```

## Architecture Differences

| Aspect | core | core-trace |
|--------|------|------------|
| Entry point | Class extending `AbstractRuleService` | Function with `Trace` context |
| Rule organization | Class extending `AbstractRuleset` | Function returning `Faktum<T>` |
| Flow logic | Class extending `AbstractRuleflow` | Regular Kotlin functions |
| Resource propagation | Automatic via component tree | Manual via `Trace.putResource` |
| Trace output | Via `debug()`, `xmlDebug()` methods | Via `Trace.debugTree()` |
| Context mechanism | Implicit via parent-child relationships | Explicit via Kotlin context parameters |

## Recommendations

1. **Add ELLERS support** - Common requirement, relatively simple to add
2. **Consider forgrening DSL** - Nice for semantic decision tree tracing
