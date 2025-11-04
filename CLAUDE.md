# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Kotlin-based rule DSL framework designed to isolate functional business rules from technical code. It provides a lightweight framework to create, run, and explain rules in a structured manner, making rules more accessible to non-technical personnel.

## Build System & Commands

**Maven-based project (Java 21, Kotlin 2.1.0)**

```bash
# Compile the project
mvn compile

# Run all tests
mvn test

# Run a specific test class
mvn test -Dtest=ClassName

# Run a specific test method
mvn test -Dtest=ClassName#methodName

# Package (creates JAR)
mvn package

# Install to local Maven repository
mvn install

# Clean build artifacts
mvn clean
```

## Architecture

### Core Component Hierarchy

The framework is built on a tree structure of `AbstractRuleComponent` subclasses:

```
AbstractRuleService (top level)
  └── AbstractRuleflow (flow logic organizer)
      └── Decision (branching decisions)
          └── Branch (conditional flow paths)
      └── AbstractRuleset (groups related rules)
          └── Rule (individual functional decisions)
              └── Predicate (technical boolean expressions)
              └── DomainPredicate (functional expressions)
                  ├── PairDomainPredicate (compares two Verdi)
                  └── ListDomainPredicate (list relationships)
```

**Key classes:**
- `AbstractRuleService` - Entry point, instantiates resources once per service call
- `AbstractRuleflow` - Organizes flow logic using DSL: `forgrening`, `gren`, `betingelse`, `flyt`
- `AbstractRuleset` - Contains a set of rules on a single topic, defines rules using `regel()`
- `Rule` - Individual rule with predicates using DSL: `HVIS`, `OG`, `SÅ`, `ELLERS`, `RETURNER`
- `Verdi` - Interface for values (implemented by `Faktum` and `Formel`)
- `Faktum` - Name-value pair representing a fact used in domain predicates
- `Predicate` - Technical boolean expressions (null checks, technical validations)
- `DomainPredicate` - Functional/domain expressions for business logic

### Resource Management

Each `AbstractRuleComponent` inherits from `AbstractResourceAccessor` which provides a `resourceMap` for storing resources instantiated once per service call (e.g., rates/satser, loggers, global assets). Resources are propagated down the component tree automatically.

Example pattern:
```kotlin
override fun run(): Response {
    putResource(MyResourceClass::class, MyResourceClass())
    return ruleService.invoke()
}

// Access in any child component:
fun AbstractResourceAccessor.myHelper() =
    getResource(MyResourceClass::class).someValue
```

### Tree Structure & Visitor Pattern

All components form a tree accessed via `children`. The tree enables context tracking during execution.

**Key inspection methods:**
- `root()` - Returns top-level component
- `debug()` - Debug string of all descendants
- `xmlDebug()` - XML debug output
- `trace(searchFunction)` - Searches and traces path to component
- `find(searchFunction)` - Searches tree for components

### Pattern System

The `Pattern` class enables writing rules over lists. Each list element gets its own rule instance:

```kotlin
val pattern = myList.createPattern { /* filter */ }

regel("MyRule", pattern) { element ->
    HVIS { element.someProperty }
    SÅ { /* action */ }
}
```

## DSL Syntax (Norwegian)

**All DSL keywords are in Norwegian.** The DSL provides type-safe builders for rules and flow control.

### Ruleflow DSL

```kotlin
forgrening("Decision name") {
    gren {
        betingelse { /* boolean condition */ }
        flyt {
            /* code to execute if condition true */
        }
    }
    gren { /* other branch */ }
}
```

### Rule DSL

```kotlin
regel("RuleName") {
    HVIS { /* technical predicate */ }
    OG { /* functional domain predicate */ }
    SÅ {
        /* action statement if all predicates true */
    }
    ELLERS {
        /* else statement if any predicate false */
    }
}

// With return value
regel("RuleWithReturn") {
    HVIS { condition }
    SÅ {
        RETURNER(value)  // stops further ruleset evaluation
    }
}
```

### Domain Predicate Operators (in `rettsregel/Operators.kt`)

Custom infix operators for functional expressions that create `DomainPredicate` instances:

**Pair operators** (compare two values):
- `erLik` - equals
- `erUlik` - not equals
- `erMindreEnn` - less than
- `erStørreEnn` - greater than
- `erMindreEllerLik` - less than or equal
- `erStørreEllerLik` - greater than or equal
- `erFør` / `erEtter` - date comparisons
- `erFørEllerLik` / `erEtterEllerLik` - date comparisons with equality

**List operators** (compare value with list):
- `erBlant` - value is in list
- `erIkkeBlant` - value is not in list

Example: `trygdetid erMindreEnn 40` creates a `PairDomainPredicate`

## Formel System (Mathematical Expressions)

Located in `src/main/kotlin/no/nav/system/rule/dsl/formel/`, this subsystem provides mathematical formula support with:
- Operators: `+`, `-`, `*`, `/` (in `Operators.kt`)
- Functions: mathematical functions (in `Functions.kt` and `MathFunctions.kt`)
- Rendering: formula rendering capability (in `Render.kt`)
- DSL builder: `FormelDsl.kt` provides DSL for formula construction

`Formel` objects represent computed numeric values that can track their calculation structure.

## Integrasjon av Uttrykk-forklaring i Rule DSL

The framework supports two approaches for generating calculation explanations:

1. **Formel-systemet** - Direct calculation with manual explanation tracking
2. **Uttrykk-systemet** - AST-based automatic explanation generation (located in `forklaring/`)

The `faktum()` function in `AbstractRuleComponent` provides integration that combines:
- **HVORFOR** - Rule flow tracing via `Trace.kt` (which rules fired and in what order)
- **HVORDAN** - Calculation explanation (how the value was computed)

### Using Uttrykk with faktum()

Uttrykk-based calculations can be integrated into Rule DSL using `faktum(grunnlag: Grunnlag<T>)`:

```kotlin
class BeregnSlitertilleggRS(
    val virkningstidspunkt: YearMonth,
    val person: Person
) : AbstractDemoRuleset<ForklartFaktum<Double>>() {

    // Use Grunnlag (Uttrykk) for calculations
    private lateinit var fulltSlitertillegg: Grunnlag<Double>
    private lateinit var justeringsFaktor: Grunnlag<Double>
    private lateinit var trygdetidFaktor: Grunnlag<Double>

    override fun create() {
        regel("SLITERTILLEGG-BEREGNING-UAVKORTET") {
            HVIS { true }
            SÅ {
                fulltSlitertillegg = (Const(0.25) * Const(grunnbeløp) / Const(12.0))
                    .navngi("fulltSlitertillegg")        // Name for explanation
                    .id("SLITERTILLEGG-UAVKORTET")       // RVS-ID for legal reference
            }
        }

        regel("SLITERTILLEGG-JUSTERING") {
            HVIS { månederEtter < 36 }
            SÅ {
                justeringsFaktor = ((Const(36) - Const(månederEtter)) / Const(36.0))
                    .navngi("justeringsFaktor")
                    .id("JUSTERING-UTTAKSTIDSPUNKT")
            }
            ELLERS {
                justeringsFaktor = Const(1.0).navngi("justeringsFaktor")
            }
        }

        regel("SLITERTILLEGG-TRYGDETID") {
            HVIS { true }
            SÅ {
                trygdetidFaktor = (Const(person.trygdetid) / Const(480.0))
                    .navngi("trygdetidFaktor")
                    .id("AVKORTING-TRYGDETID")
            }
        }

        regel("SLITERTILLEGG-BEREGNET") {
            HVIS { true }
            SÅ {
                val slitertillegg = (fulltSlitertillegg * justeringsFaktor * trygdetidFaktor)
                    .navngi("slitertillegg")
                    .id("SLITERTILLEGG-BEREGNET")

                // faktum() combines HVORFOR (rule trace) + HVORDAN (AST explanation)
                RETURNER(faktum(slitertillegg))
            }
        }
    }
}
```

### Output Structure

The `ForklartFaktum` returned by `faktum()` contains:

```kotlin
data class ForklartFaktum<T>(
    val name: String,              // "slitertillegg"
    val value: T,                  // 378.15
    val hvorfor: String,           // Rule flow trace
    val hvordan: Formel<T>         // Calculation explanation (AST-based)
)
```

**HVORFOR** (from `Trace.kt`):
```
BeregnSlitertilleggService
  BehandleSliterordningFlyt
    BeregnSlitertilleggRS
      SLITERTILLEGG-BEREGNING-UAVKORTET
      SLITERTILLEGG-JUSTERING
      SLITERTILLEGG-TRYGDETID
      SLITERTILLEGG-BEREGNET
```

**HVORDAN** (from `Uttrykk.forklarDetaljert()`):
```
SLITERTILLEGG-BEREGNET
slitertillegg = fulltSlitertillegg * justeringsFaktor * trygdetidFaktor
slitertillegg = 2292.0 * 0.33 * 0.5
slitertillegg = 378.15

fulltSlitertillegg = 0.25 * G / 12
fulltSlitertillegg = 0.25 * 110000 / 12
fulltSlitertillegg = 2292.0

justeringsFaktor = (36 - månederEtter) / 36
justeringsFaktor = (36 - 24) / 36
justeringsFaktor = 0.33

trygdetidFaktor = trygdetid / 480
trygdetidFaktor = 240 / 480
trygdetidFaktor = 0.5
```

### Implementation Details

The integration uses an adapter pattern (`UttrykkFormelAdapter`) to make `Grunnlag<T>` compatible with the `Formel<T>` interface:

```kotlin
fun <T : Number> faktum(grunnlag: Grunnlag<T>): ForklartFaktum<T> {
    return faktum(UttrykkFormelAdapter(grunnlag))
}
```

The adapter delegates to `Uttrykk.forklarDetaljert()` for automatic AST-based explanation generation, combining:
- RVS-ID (legal reference from `.id()`)
- Variable names (from `.navngi()`)
- Symbolic notation (AST structure)
- Concrete notation (with actual values)
- Sub-formulas (nested named expressions)

### When to Use

**Use Uttrykk + faktum()** when:
- Calculation explanation is required for end users or legal documentation
- Complex formulas need automatic breakdown and tracing
- You want AST-based explanation generation without manual tracking

**Use direct calculation** when:
- Simple value computation without explanation needs
- Performance is critical (Uttrykk has AST overhead)
- The calculation is self-explanatory or purely technical

See `BeregnSlitertilleggRSUttrykk.kt` and `BeregnSlitertilleggRSUttrykkTest.kt` in `src/test/kotlin/no/nav/pensjon/sliterordning/regelsett/` for a complete working example.

### Regelflyt-forklaring for ikke-numeriske verdier

For regelsett som returnerer ikke-numeriske verdier (enum, boolean, etc.), er `medForklaring()` tilgjengelig fra `AbstractRuleset`:

```kotlin
class PersonenErFlyktningRS(
    private val person: Person,
    // ... parameters
) : AbstractRuleset<Faktum<UtfallType>>() {

    override fun create() {
        regel("AngittFlyktning_HarFlyktningFlaggetSatt") {
            HVIS { person.flyktning }
            // ...
        }
        regel("AnvendtFlyktning_oppfylt") {
            HVIS { "AngittFlyktning".minstEnHarTruffet() }
            SÅ {
                RETURNER(Faktum("Anvendt flyktning", OPPFYLT))
            }
        }
    }
}

// I test:
val rs = PersonenErFlyktningRS(person, ...)
rs.test()
val forklartResultat = rs.medForklaring()  // Fra AbstractRuleset

println(forklartResultat.forklaring())
```

Output:
```
HVA
    Anvendt flyktning = OPPFYLT

HVORFOR (Regelflyt-sporing):
    regelsett: PersonenErFlyktningRS
      regel: JA AngittFlyktning_HarFlyktningFlaggetSatt
      regel: JA AnvendtFlyktning_oppfylt
```

### Comparing Approaches: Uttrykk vs Formel

**Uttrykk-approach** (recommended for complex calculations):
- **HVORFOR**: Rule flow explanation
- **HVORDAN**: Automatic AST-based calculation breakdown
- Use `faktum(grunnlag: Grunnlag<T>)` with Uttrykk expressions
- Best for: Complex formulas requiring detailed explanation

**Formel-approach** (traditional):
- **HVORFOR**: Rule flow explanation only
- No automatic calculation breakdown
- Use `medForklaring()` with regular Double/Int values
- Best for: Simple calculations or non-numeric values

Example comparison in `BeregnSlitertilleggRSFormelTest.kt` shows:

**Formel output (HVORFOR only):**
```
regelsett: BeregnSlitertilleggRSFormel
  regel: JA SLITERTILLEGG-BEREGNING-UAVKORTET
  regel: JA SLITERTILLEGG-JUSTERING-UTTAKSTIDSPUNKT
  regel: JA SLITERTILLEGG-AVKORTING-TRYGDETID
  regel: JA SLITERTILEGG-BEREGNET
```

**Uttrykk output (HVORFOR + HVORDAN):**
```
HVORFOR:
  regelsett: BeregnSlitertilleggRSUttrykk
    regel: JA SLITERTILEGG-BEREGNET

HVORDAN:
  SLITERTILLEGG-BEREGNET
  slitertillegg = fulltSlitertillegg * justeringsFaktor * trygdetidFaktor
  slitertillegg = 2291.67 * 1.0 * 0.5
  slitertillegg = 1145.83

  fulltSlitertillegg = 0.25 * G / 12
  fulltSlitertillegg = 0.25 * 110000 / 12
  fulltSlitertillegg = 2291.67
  ...
```

See `BeregnSlitertilleggRSFormel.kt` vs `BeregnSlitertilleggRSUttrykk.kt` for implementation comparison.

## Testing

Test structure is in `src/test/kotlin/`:

**Demo examples:** `no.nav.system.rule.dsl.demo/`
- `domain/` - Example domain models
- `ruleservice/` - Example rule services (see `AbstractDemoRuleService` for resource pattern)
- `ruleset/` - Example rulesets
- `ruleflow/` - Example ruleflows
- `inspection/` - Examples of using inspection/debug features

**Real-world example:** `no.nav.pensjon.sliterordning/`
- Production-like example of pension calculation rules

Use `test()` method on rulesets/ruleflows for standalone testing without a parent component.

## Code Conventions

1. **Component inheritance**: Always inherit from appropriate abstract base (`AbstractRuleService`, `AbstractRuleflow`, `AbstractRuleset`)
2. **Resource pattern**: Use `putResource`/`getResource` for shared state, never global variables
3. **Tree building**: Child components are added automatically via `run(parent)` or explicitly via `parent.children.add()`
4. **Evaluation order**: Rulesets execute rules in sequence number order (100-series increments)
5. **Pattern offset**: Rules created from patterns get sequential naming with `.1`, `.2`, etc.
6. **Norwegian DSL**: All DSL keywords (`regel`, `forgrening`, `HVIS`, `OG`, `SÅ`, etc.) must remain in Norwegian
7. **Faktum usage**: Use `Faktum` for functional business values that need names for explanation/tracing

## Package Structure

- `no.nav.system.rule.dsl` - Core framework classes
  - `enums/` - Enumerations (RuleComponentType, Comparator, etc.)
  - `error/` - Custom exceptions
  - `formel/` - Mathematical formula system
  - `inspections/` - Debug and inspection utilities
  - `pattern/` - Pattern system for list-based rules
  - `resource/` - Resource management
  - `rettsregel/` - Legal rule components (Verdi, Faktum, DomainPredicate, Operators)
