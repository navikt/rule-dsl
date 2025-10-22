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
