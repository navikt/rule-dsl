
🎯 The Vision (Reconciled with Reality)

## Architectural Principles

**Separation of Concerns:**
- **Uttrykk (Expression Tree)**: Does all the detail work
  - Mathematical calculations
  - Comparisons and predicates
  - Formula construction
  - Self-explanation with traceability

- **AbstractRuleComponent (ARC)**: High-level architecture
  - Orchestrates ruleflows and rulesets
  - Composes services using branches and decisions
  - Provides execution context via ExecutionTrace.kt
  - Maintains backward compatibility with thousands of existing production rules

**Core Principle:** One expression tree (Uttrykk) as the single source of truth that:
1. Calculates correctly
2. Explains itself with reference traceability (legal sources, URLs, confluence, etc.)
3. Provides structural tree visualization with deduplication
4. Enables perspective generation (starting with technical tester perspective)

## ✅ What We've Achieved

**Strong Foundation**

1. ✅ Expression Tree Structure: Complete with MathOperation, ComparisonOperation, ListOperation, Faktum, Const
2. ✅ Lazy Evaluation & Caching: val verdi by lazy { ... } - this IS the "Memo (cached)" shown in visualization!
3. ✅ Consistent Evaluator Pattern: All operations accept evaluator functions - extensible
4. ✅ Explanation Methods:
   - notasjon(): symbolic form
   - konkret(): concrete values
   - forklar(): recursive explanation
   - faktumSet(): contributing facts
5. ✅ Named Values: Faktum wraps expressions with names
6. ✅ Execution Context: ExecutionTrace.kt provides ARC-based tracking (better than throwable stacktrace anti-pattern)
7. ✅ Faktum hvorfor parameter: Ready to receive execution context from ExecutionTrace

## 🎯 Action Plan - Prioritized Work Items

### Phase 1: Reference Traceability & ID System ✅ COMPLETE!

**Goal:** Enable systematic reference tracking for legal sources, documentation, and other references

**Implemented:**

1. ✅ **Simple Reference data class**
   ```kotlin
   data class Reference(
       val id: String,    // e.g., "FTL-20-18"
       val url: String    // e.g., "https://lovdata.no/..."
   )
   ```
   - Simplified design (no subtypes)
   - Works for all reference types (legal, confluence, docs, etc.)

2. ✅ **References on AbstractRuleComponent**
   ```kotlin
   abstract class AbstractRuleComponent {
       open val references: List<Reference> = emptyList()
       // ...
   }
   ```
   - Subclasses (Ruleset, Rule, etc.) can override to provide references
   - Default empty list (backward compatible)

3. ✅ **References on Faktum** (replaced rvsId)
   ```kotlin
   data class Faktum<T : Any>(
       val navn: String,
       val uttrykk: Uttrykk<T>,
       val references: List<Reference> = emptyList(),  // NEW
       private val hvorfor: List<Uttrykk<*>>? = null
   )
   ```
   - Removed deprecated rvsId parameter
   - Added references list

4. ✅ **Fluent .ref() API (only on Faktum)**
   ```kotlin
   fun <T : Any> Faktum<T>.ref(reference: Reference): Faktum<T>
   fun <T : Any> Faktum<T>.ref(id: String, url: String): Faktum<T>
   ```
   - Forces explicit naming (must create Faktum first)
   - Supports chaining multiple references
   - Clean, immutable API

**Usage Examples:**
```kotlin
// On Ruleset
class VilkårSlitertilleggRS : AbstractRuleset<Boolean>() {
    override val references = listOf(
        Reference("FTL-20-18", "https://lovdata.no/..."),
        Reference("CONF-SLITER", "https://confluence.nav.no/...")
    )
}

// On Faktum (constructor)
val faktum = Faktum(
    navn = "aldersgrense",
    verdi = 67,
    references = listOf(Reference("FTL-20-7", "https://lovdata.no/..."))
)

// On Faktum (fluent)
val faktum = Faktum("aldersgrense", 67)
    .ref("FTL-20-7", "https://lovdata.no/...")
    .ref("RUNDSKRIV-2024", "https://nav.no/...")
```

**Files Created:**
- `/src/main/kotlin/no/nav/system/rule/dsl/reference/Reference.kt`
- `/src/main/kotlin/no/nav/system/rule/dsl/reference/ReferenceExtensions.kt`
- `/src/test/kotlin/no/nav/system/rule/dsl/reference/ReferenceTest.kt` (9 tests, all passing)

**Files Modified:**
- `/src/main/kotlin/no/nav/system/rule/dsl/AbstractRuleComponent.kt` - Added references property
- `/src/main/kotlin/no/nav/system/rule/dsl/rettsregel/Faktum.kt` - Replaced rvsId with references

### Phase 2: Tree Visualization (HIGH PRIORITY)

**Goal:** Create structural tree visualization with deduplication

**Tasks:**
1. **Build UttrykksTreePrinter**
   - Traverse expression tree depth-first
   - Generate indented tree structure with box-drawing characters
   - Show operation types (Mul, Add, ComparisonOperation, etc.)
   - Display Faktum names and Const values

2. **Implement deduplication tracking**
   - Track seen Faktum instances during traversal
   - First occurrence: show full subtree
   - Subsequent occurrences: show reference like "[6] FULL_TRYGDETID (3 forekomster)"
   - Maintain reference number map

**Deliverable:**
```
Grunnlag(slitertillegg) = 2291.67
│  └─ Mul
│  │  ├─ Mul
│  │  │  ├─ Grunnlag(fulltSlitertillegg) = 25000.0
│  │  │  └─ [1] trygdetidFaktor = 0.9
│  │  └─ [2] justeringsFaktor = 1.02
[1] trygdetidFaktor (2 forekomster)
[2] justeringsFaktor (1 forekomst)
```

### Phase 3: Siloed Architecture - Execution, Tracing, and Perspectives ✅ COMPLETE!

**Goal:** Clean separation between execution, tracing, and viewing with perspective functions

**Architectural Vision - The Siloed Approach:**

```
┌─────────────┐     ┌────────────────┐     ┌────────────────┐
│  Execution  │ ──> │    Tracing     │ ──> │  Perspectives  │
│    (ARC)    │     │(ExecutionTrace)│     │   (Viewing)    │
└─────────────┘     └────────────────┘     └────────────────┘
  Run rules          Track everything      Render for audience
```

**Implemented:**

1. ✅ **ExecutionTrace (Tracing Layer)**
   - Already exists and captures complete execution path
   - Single instance in resourceMap (thread-safe)
   - `fullPath()` returns complete ARC component tree
   - `pathForHvorfor()` returns filtered decision path
   - No changes needed - already perfect!

2. ✅ **Four Built-in Perspective Functions**
   ```kotlin
   fun ExecutionTrace.toFullString(): String
   fun ExecutionTrace.toFunctionalString(): String
   fun ExecutionTrace.toUttrykksTree(faktum: Faktum<*>): String
   fun ExecutionTrace.toFaktumExplanation(faktum: Faktum<*>): String
   ```

3. ✅ **Perspective Implementation Details:**

   **FullPerspective** → `toFullString()`
   - Shows complete execution trace
   - All components with values
   - Use case: Complete audit trail

   **FunctionalPerspective** → `toFunctionalString()`
   - Uses existing `pathForHvorfor()` filtering
   - Shows only decision nodes (rules, branches, predicates)
   - Use case: Business analysts

   **UttrykksTreePerspective** → `toUttrykksTree(faktum)`
   - Integrates Phase 2's UttrykksTreePrinter!
   - Shows formula structure with deduplication
   - Use case: Technical testers

   **FaktumPerspective** → `toFaktumExplanation(faktum)`
   - Uses existing `forklar()` method
   - Combines WHAT/HOW/WHY
   - Use case: Explaining specific results

4. ✅ **Extension Function Pattern**
   - All perspectives are extension functions on ExecutionTrace
   - Client repos can add custom perspectives:
   ```kotlin
   fun ExecutionTrace.toJSON(): JsonObject { ... }
   fun ExecutionTrace.toHTML(): String { ... }
   ```

5. ✅ **Faktum.hvorfor Integration**
   - Already wired via `AbstractRuleComponent.sporing()` methods
   - Captures `pathForHvorfor()` automatically
   - No changes needed - already works!

**Files Created:**
- `/src/main/kotlin/no/nav/system/rule/dsl/perspectives/Perspectives.kt`
- `/src/test/kotlin/no/nav/system/rule/dsl/perspectives/PerspectivesTest.kt` (7 passing tests)

**Usage Examples:**
```kotlin
// In a rule service
override fun run(): Response {
    val trace = ExecutionTrace()
    putResource(ExecutionTrace::class, trace)

    val result = ruleService.invoke()

    // Different perspectives of same execution:
    println(trace.toFullString())          // Complete trace
    println(trace.toFunctionalString())    // Decisions only
    println(trace.toUttrykksTree(faktum))  // Formula tree
    println(trace.toFaktumExplanation(faktum))  // Bottom-up explanation

    return result
}
```

**Note on forklar():**
- Keeping `forklar()` for now (not deprecated)
- `toFaktumExplanation()` uses `forklar()` internally
- May deprecate in future if perspectives prove superior
- Allows gradual migration

### Phase 4: Structured Data Output for GUI Integration (MEDIUM PRIORITY)

**Goal:** Enable GUI rendering via structured data output (complement to string perspectives)

**Rationale:**
- Phase 3 perspectives produce human-readable strings
- GUIs need structured data (JSON, data classes) for interactive rendering
- Enable drill-down, expand/collapse, filtering in GUI

**Tasks:**

1. **Design data class hierarchy for trace output**
   ```kotlin
   data class ExecutionTraceData(
       val rootComponent: ComponentNode,
       val allFaktum: List<FaktumNode>,
       val executionPath: List<ExecutionStep>
   )

   sealed class TraceNode {
       data class ComponentNode(type, name, children, result)
       data class FaktumNode(name, value, formula, dependencies)
       data class PredicateNode(expression, result, operands)
   }
   ```

2. **Implement structured data perspectives**
   - `ExecutionTrace.toStructuredData(): ExecutionTraceData`
   - `ExecutionTrace.toJSON(): JsonObject` (using kotlinx.serialization)
   - Convert trace tree to serializable data structures
   - Preserve all information (not just strings)

3. **Design GUI-friendly presentation models**
   - Separate from internal trace structure
   - Include metadata for rendering (node types, expandability, etc.)
   - Support filtering (show only failures, show only calculations, etc.)

4. **Documentation and examples**
   - Show how main app GUI can consume structured data
   - Example: Expandable tree view in GUI
   - Example: Filtering/searching through trace
   - Example: Linking Faktum to contributing rules

**Deliverable:**
- Structured data output from ExecutionTrace
- JSON serialization support
- Documentation for GUI integration
- Example: Main app can render interactive trace view

### Phase 5: Convenience Methods (LOW PRIORITY)

**Goal:** Ergonomic improvements to API

**Tasks:**
1. **Fluent .navngi() method**
   - Extension: Uttrykk<T>.navngi(navn: String): Faktum<T>
   - Wraps expression in Faktum with given name
   - Enables chaining: calculation.navngi("result").ref(...)
   - Note: Previous attempts encountered issues - investigate and resolve

**Deliverable:** Can write formulas fluently with inline naming

## 📊 Updated Progress Assessment

| Component                     | Status | Notes                                              |
|-------------------------------|--------|----------------------------------------------------|
| Expression Tree Core          | ✅ 100% | Complete and stable                                |
| Evaluator Pattern             | ✅ 100% | Complete and extensible                            |
| Tree Visualization            | ✅ 100% | UttrykksTreePrinter with deduplication             |
| Reference System              | ✅ 100% | Simple Reference class + fluent API                |
| ExecutionTrace (Tracing)      | ✅ 100% | Already captures everything needed                 |
| Perspective Functions         | ✅ 100% | 4 built-in perspectives + extension pattern        |
| Faktum-ExecutionTrace Wiring  | ✅ 100% | Already wired via sporing() methods                |
| Structured Data Output        | ❌ 0%   | Needs design and implementation (Phase 4)          |
| Fluent API (.navngi)          | ❌ 0%   | Low priority convenience feature (Phase 5)         |

## 💡 Key Insights

1. **Siloed Architecture is the Way Forward**
   - Clean separation: Execution (ARC) → Tracing (ExecutionTrace) → Perspectives (viewing functions)
   - Single ExecutionTrace captures everything; perspectives filter and format
   - No subclasses, just one trace with multiple viewing functions

2. **ExecutionTrace.kt is the right foundation**
   - Already avoiding the throwable stacktrace anti-pattern
   - Lives in resourceMap for thread-safety
   - Needs enhancement to capture Uttrykk-graph alongside ARC components

3. **Complementary architectures - Uttrykk and ARC**
   - Uttrykk: Detail work (calculations, formulas, predicates)
   - ARC: High-level orchestration (ruleflows, rulesets, decisions)
   - Both captured in single ExecutionTrace

4. **Backward compatibility is non-negotiable**
   - Thousands of production rules depend on current patterns
   - Enhancements must be additive, not breaking

5. **Perspective functions over embedded formatting**
   - Move formatting OUT of Uttrykk classes
   - Uttrykk stays focused on calculation
   - Perspectives (toFullString, toFunctionalString, etc.) handle rendering
   - Extension functions enable custom perspectives in client repos

6. **Tree visualization is the "wow" feature** ✅
   - **DONE!** UttrykksTreePrinter with deduplication complete
   - Visually demonstrates the power of the expression tree approach
   - Will be integrated into UttrykksTreePerspective

## 🤔 Fundamental Design Tension: Faktum-Centric vs. Service-Centric Explanation

**Two Valid Needs:**

### Faktum-Centric View (Bottom-Up)
- **Question:** "Why does THIS specific Faktum have this value?"
- **Navigation:** Start with result → trace backwards to find contributing factors
- **Use case:** "Why is slitertillegg = 2291.67?"
- **Answer needed:**
  - Formula: how it was calculated (WHAT/HOW)
  - Dependencies: which input facts were used
  - Context: which rules/branches created it (WHY)

### Service-Centric View (Top-Down)
- **Question:** "What did the entire service execution do?"
- **Navigation:** Start with service → drill down through decisions
- **Use case:** "Show me all decisions made during this pension calculation"
- **Answer needed:**
  - Which ruleflows executed
  - Which branches were taken
  - Which rules fired
  - Complete execution sequence

### Resolution: Single Trace, Multiple Perspectives

**The Siloed Architecture Resolves This!**

```
                    ExecutionTrace
                    (captures both)
                         |
        ┌────────────────┼────────────────┐
        │                │                │
  toFullString()  toFunctionalString()  toFaktumExplanation()
  (everything)    (decisions only)      (bottom-up from Faktum)
```

**How It Works:**

1. **Single ExecutionTrace captures EVERYTHING**
   - All ARC components (ruleflows, decisions, rules, branches)
   - All Uttrykk nodes (formulas, predicates, calculations)
   - Complete top-down execution tree
   - Each Faktum creation captures snapshot → Faktum.hvorfor

2. **Perspective functions provide different views:**

   **FullPerspective** → `trace.toFullString()`
   - Top-down: complete service execution
   - Shows every component, every calculation
   - Use case: Complete audit trail

   **FunctionalPerspective** → `trace.toFunctionalString()`
   - Top-down: filtered for decisions
   - Shows ruleflows, decisions, rules (not technical details)
   - Use case: Business documentation

   **FaktumPerspective** → `trace.toFaktumExplanation(faktum)`
   - Bottom-up: start with Faktum, search upward
   - Shows formula (from Uttrykk) + context (from hvorfor)
   - Combines WHAT/HOW (calculation) with WHY (execution context)
   - Use case: "Explain this specific result"

   **UttrykksTreePerspective** → `trace.toUttrykksTree()`
   - Shows formula structure with deduplication
   - Focus on calculation mechanics
   - Use case: Technical verification

**Implementation:**
- ExecutionTrace: Capture phase (no filtering, no decisions)
- Perspective functions: Presentation phase (filter/format for audience)
- Faktum.hvorfor: Bridge between Faktum and trace (enables bottom-up view)

**Conclusion:** Not either/or, but **one trace with multiple lenses**. Same execution data, different perspectives based on what question you're asking.

## 🚀 Current Status & Next Steps

### ✅ Phase 1 Complete! - Reference Traceability

**Delivered:**
- Simple Reference data class (id, url)
- References on both AbstractRuleComponent and Faktum
- Fluent `.ref()` API for chaining references
- 9 passing tests

### ✅ Phase 2 Complete! - Tree Visualization

**Delivered:**
- UttrykksTreePrinter with box-drawing characters
- Deduplication tracking with reference numbers
- Extension function `printTree()` for easy usage
- 7 passing tests

### ✅ Phase 3 Complete! - Siloed Architecture & Perspectives

**Delivered:**
- Clean architectural separation: Execution → Tracing → Perspectives
- ExecutionTrace already captures complete execution path (no changes needed!)
- Four built-in perspective functions:
  - `toFullString()` - complete audit trail
  - `toFunctionalString()` - business decisions only
  - `toUttrykksTree(faktum)` - formula visualization (integrates Phase 2!)
  - `toFaktumExplanation(faktum)` - bottom-up explanation
- Extension function pattern enables custom perspectives in client repos
- 7 passing tests
- Faktum.hvorfor already wired (no changes needed!)

**Usage:**
```kotlin
override fun run(): Response {
    val trace = ExecutionTrace()
    putResource(ExecutionTrace::class, trace)

    val result = ruleService.invoke()

    // Different perspectives of same execution:
    println(trace.toFullString())          // Complete trace
    println(trace.toFunctionalString())    // Decisions only
    println(trace.toUttrykksTree(faktum))  // Formula tree with dedup
    println(trace.toFaktumExplanation(faktum))  // WHAT/HOW/WHY

    return result
}
```

### 🎯 What's Next?

**Core vision achieved!** Phases 1-3 deliver the fundamental architecture:
- ✅ Expression trees calculate correctly
- ✅ References provide legal traceability
- ✅ Tree visualization shows structure with deduplication
- ✅ Multiple perspectives from single execution trace
- ✅ Extensible via client-defined perspectives

**Remaining phases are optional enhancements:**

**Phase 4: Structured Data Output** (Medium Priority)
- JSON/data class output for GUI integration
- Enable interactive UI (drill-down, expand/collapse, filtering)

**Phase 5: Convenience Methods** (Low Priority)
- Fluent `.navngi()` API for inline naming

The foundation is solid and production-ready! 🎉