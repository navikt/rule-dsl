# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Kotlin-based rule DSL framework developed by NAV (Norwegian Labour and Welfare Administration) that provides a lightweight framework for creating, running, and explaining business rules. The framework separates functional rules from technical code to make rules more accessible to non-technical personnel.

## Build Commands

```bash
# Clean and compile
mvn clean compile

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=VilkårsprøvingSlitertilleggRSTest

# Run a single test method
mvn test -Dtest=VilkårsprøvingSlitertilleggRSTest#testMethodName

# Package as JAR
mvn package

# Install to local repository
mvn install

# Clean, compile and run tests
mvn clean test
```

## Architecture

### Core Components

The framework is built around a tree structure of rule components inheriting from `AbstractRuleComponent`:

- **AbstractRuleService** (src/main/kotlin/no/nav/system/rule/dsl/AbstractRuleService.kt) - Service entry point
- **AbstractRuleflow** (src/main/kotlin/no/nav/system/rule/dsl/AbstractRuleflow.kt) - Flow control logic with branches and decisions
- **AbstractRuleset** (src/main/kotlin/no/nav/system/rule/dsl/AbstractRuleset.kt) - Groups related rules
- **Rule** (src/main/kotlin/no/nav/system/rule/dsl/Rule.kt) - Individual business rule
- **Faktum** (src/main/kotlin/no/nav/system/rule/dsl/rettsregel/Faktum.kt) - Name-value pairs for subsumption
- **Subsumtion** (src/main/kotlin/no/nav/system/rule/dsl/rettsregel/Subsumtion.kt) - Functional expressions for rule conditions

### Package Structure

```
src/main/kotlin/no/nav/system/rule/dsl/
├── Core abstractions (AbstractRuleComponent, AbstractRuleset, etc.)
├── enums/ - Comparators and rule types
├── error/ - Custom exceptions
├── inspections/ - Debug and tracing utilities
├── pattern/ - Pattern matching for list operations
├── resource/ - Resource management
└── rettsregel/ - Legal rule specific implementations

src/test/kotlin/no/nav/system/rule/dsl/demo/
├── domain/ - Domain models for pension calculations
├── ruleflow/ - Example rule flows
├── ruleservice/ - Service implementations
└── ruleset/ - Rule set implementations and tests
```

### DSL Syntax

The framework uses Norwegian DSL keywords for rule definition:

- `regel()` - Define a rule
- `HVIS` - If condition (technical predicate)
- `OG` - And condition (functional subsumption)
- `SÅ` - Then action
- `ELLERS` - Else action
- `RETURNER()` - Return value
- `forgrening()` - Branching logic in flows
- `gren` - Branch
- `betingelse` - Condition
- `flyt` - Flow

### Working with Rules

When implementing or modifying rules:

1. Rules extend `AbstractRuleset<T>` where T is the return type
2. Override the `create()` method to define rules
3. Use `Faktum` objects to wrap values with descriptions
4. Use operators like `erLik`, `erMindreEnn`, `erStørreEllerLik` for comparisons
5. Rules can access previous rule results via `harTruffet()` method

### Testing Approach

- Test classes follow the pattern `*Test.kt` (e.g., `VilkårsprøvingSlitertilleggRSTest`)
- Tests use JUnit 5 Jupiter
- Test data is typically created using domain objects from `src/test/kotlin/no/nav/system/rule/dsl/demo/domain/`

### Key Inspection Methods

For debugging and analysis:
- `root()` - Get top-level component
- `debug()` - Debug string of all descendants
- `xmlDebug()` - XML representation
- `trace()` - Find path to specific component
- `find()` - Search component tree