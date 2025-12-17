# Context-Driven Rule DSL Design

## Status: Draft - Team Discussion

## Executive Summary

This document proposes a redesign of the rule DSL that simplifies the forklaring (explanation) system by:
- Replacing the ARC tree with a single **Uttrykk tree**
- Using a **Context object** to explicitly track execution
- Eliminating adapter classes (FaktumNode, TrackablePredicate, TrackableCondition)
- Making formatters type-agnostic

## Problem Statement

The current ARC (AbstractRuleComponent) model conflates two concerns:
1. **Code organization**: Service → Flow → Ruleset → Rule hierarchy
2. **Decision tracking**: What expressions were evaluated and why

This creates complexity:
- Multiple wrapper types to bridge Uttrykk and ARC worlds
- Formatters need type-specific logic (Rule, Branch, Predicate, etc.)
- Tree is built implicitly as side-effect of execution
- Difficult to query or transform the computation tree

## Core Design Principles

1. **One Tree Type**: Everything is `Uttrykk<T>` - no containers, no adapters
2. **Explicit Tracking**: Context object explicitly registers decisions
3. **Imperative User Code**: Users still write normal Kotlin code
4. **Type-Agnostic Formatters**: Walk Uttrykk tree without knowing specific types
5. **Reuse Existing Foundation**: Build on `no.nav.system.ruledsl.core.model.uttrykk`

## Proposed Architecture

### 1. Context Object

The Context manages execution tracing and builds the Uttrykk tree:

```kotlin
interface ExecutionContext {
    /**
     * Track a boolean decision (HVIS, OG condition)
     */
    fun <T> decision(name: String, expression: Uttrykk<Boolean>): Boolean

    /**
     * Track a calculation (FORMEL)
     */
    fun <T : Any> compute(name: String, expression: Uttrykk<T>): T

    /**
     * Enter a named scope (for service/flow/ruleset organization)
     */
    fun enterScope(name: String)

    /**
     * Exit current scope
     */
    fun exitScope()

    /**
     * Get the complete execution tree
     */
    fun getTree(): ExecutionTree
}
```

### 2. Execution Tree

A simple tree of Uttrykk nodes with metadata:

```kotlin
data class ExecutionNode<T : Any>(
    val uttrykk: Uttrykk<T>,
    val scope: String,           // e.g., "VilkårsprøvSlitertilleggRS.VILKÅR-OPPFYLT"
    val parent: ExecutionNode<*>?,
    val children: List<ExecutionNode<*>>,
    val references: List<Reference> = emptyList()
)

class ExecutionTree(
    val root: ExecutionNode<*>
) {
    /**
     * Find node by predicate
     */
    fun find(predicate: (ExecutionNode<*>) -> Boolean): ExecutionNode<*>?

    /**
     * Get all ancestors of a node
     */
    fun ancestors(node: ExecutionNode<*>): List<ExecutionNode<*>>

    /**
     * Get all contributing Uttrykk for a node (transitive dependencies)
     */
    fun dependencies(node: ExecutionNode<*>): List<Uttrykk<*>>
}
```

### 3. DSL Keywords

User-facing DSL that works with Context:

```kotlin
// Inside a computation class/function with context access
class VilkårsprøvSlitertilleggRS(val ctx: ExecutionContext) {

    fun compute(alder: Int, trygdetid: Int): Boolean {
        ctx.enterScope("VilkårsprøvSlitertilleggRS.VILKÅR-OPPFYLT")

        val result = ctx.decision("Vilkår oppfylt") {
            // This creates Uttrykk nodes automatically
            HVIS { alder >= 62 } OG { trygdetid >= 20 }
        }

        ctx.exitScope()
        return result
    }
}
```

## Modeling Sequential Execution

### The Key Insight: Sequential Code Is Just Normal Kotlin

A ruleflow today is often just orchestration:
```kotlin
// Current model - explicit flow
override var ruleflow: () -> T = {
    val a = doA().run(this)
    val b = doB().run(this)
    val c = doC(a, b).run(this)
    c
}
```

**In the context model, this is just normal code:**

```kotlin
class MyFlow(val ctx: ExecutionContext) {
    fun execute(): T {
        ctx.enterScope("MyFlow")

        val a = doA()  // ← Normal function call
        val b = doB()  // ← Normal function call
        val c = doC(a, b)  // ← Normal function call

        ctx.exitScope()
        return c
    }
}
```

### What Gets Tracked?

**Only decisions and computations are tracked as Uttrykk:**

```kotlin
fun doA(): Int {
    ctx.enterScope("doA")

    val result = ctx.compute("Result A") {
        FORMEL { 10 * 5 }  // ← This creates an Uttrykk node
    }

    ctx.exitScope()
    return result
}
```

**The sequential execution itself is NOT an Uttrykk.**

The resulting tree shows:
```
MyFlow (scope marker)
  ├─ doA (scope marker)
  │   └─ Result A = 50 (Uttrykk node)
  ├─ doB (scope marker)
  │   └─ Result B = 100 (Uttrykk node)
  └─ doC (scope marker)
      └─ Result C = 150 (Uttrykk node)
```

The tree structure **implicitly** shows the sequence: A happened before B happened before C.

### Control Flow (if/else)

Normal Kotlin control flow works naturally:

```kotlin
fun execute(): Double {
    ctx.enterScope("BehandleSliterordning")

    // Track the decision
    val vilkår = ctx.decision("Vilkår oppfylt") {
        alder >= 62 OG trygdetid >= 20
    }

    // Normal if/else - no special tracking needed
    val beløp = if (vilkår) {
        beregnSlitertillegg()  // This branch was taken
    } else {
        0.0  // This branch was not taken
    }

    ctx.exitScope()
    return beløp
}
```

**In the explanation:**
- You see "Vilkår oppfylt = true" (the decision)
- You see "Slitertillegg = 2500.0" (the result)
- **The fact that the true branch was taken is implicit** - we computed slitertillegg, so we must have taken that branch

### Loops

Loops also work naturally:

```kotlin
fun processAll(items: List<Item>): List<Result> {
    ctx.enterScope("ProcessAll")

    val results = items.map { item ->
        ctx.enterScope("Process item ${item.id}")

        val result = ctx.compute("Processed ${item.id}") {
            FORMEL { item.value * 2 }
        }

        ctx.exitScope()
        result
    }

    ctx.exitScope()
    return results
}
```

The tree would show each iteration as a separate scope with its own computation nodes.

### Pattern System

The current pattern system can work similarly:

```kotlin
fun processBoperioder(boperioder: List<Boperiode>): Int {
    ctx.enterScope("ProcessBoperioder")

    var total = 0
    boperioder.forEach { periode ->
        ctx.enterScope("Boperiode.${periode.id}")

        val måneder = ctx.compute("Måneder for periode") {
            FORMEL { periode.tom.monthsUntil(periode.fom) }
        }

        total += måneder
        ctx.exitScope()
    }

    ctx.exitScope()
    return total
}
```

### Key Takeaways

1. **Sequential execution = normal Kotlin code** (no DSL needed)
2. **Scopes mark organizational boundaries** (enterScope/exitScope)
3. **Only decisions and computations create Uttrykk nodes**
4. **Control flow (if/else/loops) is implicit** - inferred from what was computed
5. **The tree structure itself shows execution order**

## Scope Management Implementation

### Backend Handling of enterScope/exitScope

The Context implementation can automatically manage scope entry/exit using several strategies:

#### Strategy 1: Automatic Scope Detection via Stack Frames

```kotlin
class ExecutionContextImpl : ExecutionContext {
    private val tree = mutableListOf<ExecutionNode<*>>()
    private val scopeStack = ArrayDeque<String>()

    override fun <T : Any> compute(name: String, expression: Uttrykk<T>): T {
        // Automatically detect scope from call stack
        val stackFrame = Thread.currentThread().stackTrace[2]
        val autoScope = "${stackFrame.className}.${stackFrame.methodName}"

        scopeStack.addLast(autoScope)
        try {
            val node = ExecutionNode(expression, autoScope, getCurrentParent(), emptyList())
            tree.add(node)
            return expression.verdi
        } finally {
            scopeStack.removeLast()
        }
    }
}
```

**Pros:**
- No explicit enterScope/exitScope calls needed
- Automatic from method names

**Cons:**
- Stack walking has performance cost
- Scope names are tied to method names (less flexible)
- Doesn't work well with lambdas

#### Strategy 2: DSL with Automatic Cleanup

```kotlin
class ExecutionContextImpl : ExecutionContext {
    inline fun <T> scope(name: String, block: () -> T): T {
        enterScope(name)
        try {
            return block()
        } finally {
            exitScope()
        }
    }
}

// Usage
fun execute(): Double {
    ctx.scope("BehandleSliterordning") {
        val vilkår = VilkårsprøvRS(ctx).check()
        if (vilkår) beregnSlitertillegg() else 0.0
    }
}
```

**Pros:**
- Try/finally ensures cleanup even on exceptions
- Explicit scope names
- Inline function = zero overhead

**Cons:**
- Still requires wrapping code in scope { }
- Nesting can get verbose

#### Strategy 3: Annotation-Based Scope (Compile-Time)

```kotlin
@Scope("BeregnSlitertillegg")
class BeregnSlitertilleggRS(val ctx: ExecutionContext) {
    fun beregn(): Double {
        // Compiler plugin automatically injects:
        // ctx.enterScope("BeregnSlitertillegg")
        // try { ... user code ... }
        // finally { ctx.exitScope() }

        return ctx.compute("Slitertillegg") {
            FORMEL { grunnbeløp * 0.25 }
        }
    }
}
```

**Pros:**
- Clean user code - no explicit scope management
- Compiler ensures correctness
- Works with suspend functions

**Cons:**
- Requires compiler plugin
- Build complexity

#### Strategy 4: Inline Class Wrapper

```kotlin
@JvmInline
value class Scoped<T>(private val value: T) {
    inline fun <R> with(ctx: ExecutionContext, name: String, block: T.() -> R): R {
        ctx.enterScope(name)
        try {
            return value.block()
        } finally {
            ctx.exitScope()
        }
    }
}

// Usage
val rs = Scoped(BeregnSlitertilleggRS(ctx))
val result = rs.with(ctx, "BeregnSlitertillegg") {
    beregn()
}
```

**Pros:**
- Zero runtime overhead (inline value class)
- Explicit but concise
- No compiler plugin needed

**Cons:**
- Unfamiliar pattern
- Every class needs wrapping

#### Recommended Approach: Hybrid

Combine Strategy 2 (DSL) with Strategy 3 (annotations) for opt-in compiler support:

```kotlin
// Simple case - explicit scope
fun execute() = ctx.scope("Execute") {
    val a = doA()
    val b = doB(a)
    doC(b)
}

// Complex case - annotated class (compiler plugin handles scope)
@AutoScope
class VilkårsprøvRS(val ctx: ExecutionContext) {
    fun check(): Boolean {
        // Scope automatically entered/exited by compiler
        return ctx.decision("Vilkår") { /* ... */ }
    }
}
```

### Internal Tree Building

The context maintains a stack-based tree builder:

```kotlin
private class TreeBuilder {
    private val root = ExecutionNode(/* root marker */)
    private val stack = ArrayDeque<ExecutionNode<*>>().apply { add(root) }

    fun enterScope(name: String) {
        val scopeMarker = ExecutionNode.ScopeMarker(name, stack.last())
        stack.last().addChild(scopeMarker)
        stack.addLast(scopeMarker)
    }

    fun exitScope() {
        require(stack.size > 1) { "Cannot exit root scope" }
        stack.removeLast()
    }

    fun <T : Any> addNode(uttrykk: Uttrykk<T>, metadata: Map<String, Any>): ExecutionNode<T> {
        val node = ExecutionNode(uttrykk, stack.last(), metadata)
        stack.last().addChild(node)
        return node
    }

    fun build(): ExecutionTree = ExecutionTree(root)
}
```

## Context Propagation Strategies

### How Components Access ExecutionContext

Once the ExecutionContext exists, computation components need a way to access it. Several strategies are possible, each with different tradeoffs.

#### Strategy 1: Explicit Parameter Passing

Pass the context explicitly as a constructor parameter:

```kotlin
class BeregnSlitertilleggRS(
    val ctx: ExecutionContext,
    val grunnbeløp: Int
) {
    fun beregn(): Double {
        return ctx.compute("Slitertillegg") {
            FORMEL { grunnbeløp * 0.25 }
        }
    }
}

// Usage
val result = BeregnSlitertilleggRS(ctx, 120000).beregn()
```

**Pros:**
- Simple and explicit - no magic
- Easy to understand and debug
- Works with current Kotlin versions
- Easy to test (just pass mock context)
- IDE support is perfect
- Clear data flow

**Cons:**
- Verbose - every class needs ctx parameter
- Context must be threaded through all calls
- Mixing business data (grunnbeløp) with infrastructure (ctx)

#### Strategy 2: Parent.run() Pattern (Current ARC Model)

Use an abstract base class that provides a `.run(parent)` method. The parent propagates context to children:

```kotlin
// Base class provides context propagation
abstract class RuleComponent {
    private var contextImpl: ExecutionContext? = null
    protected val ctx: ExecutionContext
        get() = contextImpl ?: throw IllegalStateException("Not initialized")

    fun <T : Any> run(parent: RuleComponent): T {
        // Inherit context from parent
        this.contextImpl = parent.contextImpl
        return execute()
    }

    protected abstract fun execute(): T
}

// User code
class BeregnSlitertilleggRS(val grunnbeløp: Int) : RuleComponent() {
    override fun execute(): Double {
        return ctx.compute("Slitertillegg") {
            FORMEL { grunnbeløp * 0.25 }
        }
    }
}

// Usage
val result = BeregnSlitertilleggRS(120000).run(this)
```

**Pros:**
- Clean constructor - only business data (grunnbeløp)
- Context propagates automatically down the tree
- Familiar pattern from current ARC model
- Components can be instantiated without context (useful for exploration)
- Natural parent-child relationship
- Context is available via `ctx` property without explicit passing

**Cons:**
- Less explicit - context comes from "somewhere"
- Requires inheritance from base class
- Context access fails if component not initialized via run()
- Harder to test in isolation (need to set up parent relationship)
- More complex initialization protocol

**Comparison with Current ARC:**
This is essentially how the current ARC model works:
- `AbstractRuleComponent` is the base class
- `.run(parent)` sets up parent-child relationship and propagates resourceMap
- The ExecutionContext would work the same way, just propagating context instead of resourceMap

**Migration Path:**
If adopting this pattern:
- ExecutionContext could be added to existing AbstractRuleComponent
- Gradual migration: add ctx alongside existing ARC infrastructure
- Eventually remove ARC tree if context-driven model proves superior

#### Strategy 3: Context Parameters (Kotlin 2.1+)

Use Kotlin's upcoming context parameters feature (detailed in next section).

**Pros:**
- Clean constructors (no ctx parameter)
- Type-safe implicit propagation
- No inheritance required

**Cons:**
- Requires Kotlin 2.1+
- New language feature with unknown edge cases
- See detailed evaluation below

### Recommendation

**For prototype and initial implementation:**
Use **Strategy 1 (Explicit Parameters)** or **Strategy 2 (Parent.run() Pattern)**.

**Strategy 2 (Parent.run())** is recommended if:
- You want consistency with current ARC model
- You value clean constructors (only business data)
- You're comfortable with base class inheritance
- Migration path from current codebase is important

**Strategy 1 (Explicit Parameters)** is recommended if:
- You value explicitness and debuggability
- You want to avoid inheritance-based patterns
- You're starting fresh without migration constraints
- Testing in isolation is a priority

**Strategy 3 (Context Parameters)** should be evaluated after Kotlin 2.1 is stable, as described in the next section.

## Context Parameters (Kotlin 2.1+)

### Evaluation of Context Parameters Feature

Kotlin 2.1 introduces **context parameters** that allow implicit parameter passing through the call stack:

```kotlin
context(ExecutionContext)
class BeregnSlitertilleggRS(val grunnbeløp: Int) {
    fun beregn(): Double {
        // ctx is implicitly available via context parameter
        return compute("Slitertillegg") {
            FORMEL { grunnbeløp * 0.25 }
        }
    }
}

context(ExecutionContext)
fun compute(name: String, block: () -> Uttrykk<T>): T {
    // 'this@ExecutionContext' is available implicitly
    // ...
}
```

### Usage Example

```kotlin
class SliterordningService(val request: Request) {
    fun run(): SliterordningResponse {
        val ctx = ExecutionContext.create()

        with(ctx) {  // ← Establishes context
            scope("SliterordningService") {
                val vilkår = VilkårsprøvRS().check()
                val beløp = BeregnSlitertilleggRS(request.grunnbeløp).beregn()
                SliterordningResponse.Innvilget(beløp, ctx.getTree())
            }
        }
    }
}
```

### Advantages

1. **No explicit ctx parameter passing**
   ```kotlin
   // Before
   class MyRuleset(val ctx: ExecutionContext, val data: Data)

   // After
   context(ExecutionContext)
   class MyRuleset(val data: Data)
   ```

2. **Cleaner call sites**
   ```kotlin
   // Before
   VilkårsprøvRS(ctx, alder, trygdetid).check()

   // After (with context established)
   VilkårsprøvRS(alder, trygdetid).check()
   ```

3. **Type-safe implicit parameters**
   - Compiler enforces context availability
   - No magic ThreadLocal or global state
   - Works with suspend functions

4. **Composability**
   ```kotlin
   context(ExecutionContext, Logger, TransactionManager)
   class MyComplexRuleset {
       // All three contexts available
   }
   ```

### Disadvantages

1. **Language feature adoption risk**
   - Context parameters are new (Kotlin 2.1+)
   - May have edge cases or limitations
   - Team needs to learn new syntax

2. **IDE support maturity**
   - IntelliJ IDEA support may be incomplete initially
   - Autocomplete, refactoring tools need updating

3. **Debugging complexity**
   - Implicit parameters harder to trace
   - Stack traces don't show context flow clearly

4. **Migration challenge**
   - Existing codebase can't use context parameters
   - Gradual migration path unclear

5. **Testability consideration**
   ```kotlin
   // Explicit parameter - easy to mock
   class MyRuleset(val ctx: ExecutionContext)
   MyRuleset(mockContext)

   // Context parameter - requires establishing context
   context(ExecutionContext)
   class MyRuleset
   with(mockContext) { MyRuleset() }  // More ceremony
   ```

### Recommendation

**Start with explicit parameters, evaluate context parameters later:**

1. **Phase 1 (Prototype)**: Use explicit `ctx: ExecutionContext` parameter
   - Proven pattern
   - Clear, debuggable
   - Works with current Kotlin version

2. **Phase 2 (After Kotlin 2.1 stable)**: Experiment with context parameters
   - Build sample ruleset using context parameters
   - Evaluate IDE support, debugging experience
   - Measure team feedback

3. **Phase 3 (Decision Point)**: Migrate if advantages are clear
   - If context parameters work well, provide migration guide
   - Keep explicit parameter style as supported alternative

### Hybrid Approach

Support both styles:

```kotlin
// Explicit style (always works)
class VilkårsprøvRS(val ctx: ExecutionContext) {
    fun check(): Boolean { /* ... */ }
}

// Context parameter style (opt-in)
context(ExecutionContext)
class VilkårsprøvRSWithContext {
    fun check(): Boolean { /* ... */ }
}

// Framework supports both
fun runService(service: Service) {
    val ctx = ExecutionContext.create()
    service.run(ctx)  // Explicit
    // OR
    with(ctx) { service.run() }  // Context parameter
}
```

## Code Organization

### Classes vs Functions

**Both are supported.** The key is that computation units:
- Accept a Context (or have access to shared context)
- Are nameable (for scoping)
- Have parameters (input data)
- Return typed values
- Can call other computation units

**Class-based organization** (similar to current testclient-sliterordningen):

```kotlin
// "Ruleset" is just a class that does computation
class BeregnSlitertilleggRS(
    val ctx: ExecutionContext,
    val grunnbeløp: Int
) {
    fun beregn(): Double {
        ctx.enterScope("BeregnSlitertilleggRS.BEREGN")

        val result = ctx.compute("Slitertillegg") {
            FORMEL { grunnbeløp * 0.25 }
        }

        ctx.exitScope()
        return result
    }
}

// "Service" is just a class that orchestrates
class SliterordningService(val request: Request) {
    private val ctx = ExecutionContext.create()

    fun run(): SliterordningResponse {
        ctx.enterScope("SliterordningService")

        val vilkår = VilkårsprøvSlitertilleggRS(ctx).compute(
            request.alder,
            request.trygdetid
        )

        val beløp = if (vilkår) {
            BeregnSlitertilleggRS(ctx, request.grunnbeløp).beregn()
        } else {
            0.0
        }

        ctx.exitScope()
        return SliterordningResponse.Innvilget(beløp, ctx.getTree())
    }
}
```

**Why classes work well:**
- Encapsulate parameters (grunnbeløp, etc.)
- Can have helper methods
- Easy to test in isolation
- Mirror current structure (Service/Flow/Ruleset/Rule)

## Uttrykk Foundation (Reusable)

The existing `no.nav.system.ruledsl.core.model.uttrykk` package is largely reusable:

**Keep as-is:**
- `Uttrykk<T>` interface
- `Faktum<T>` (but no more FaktumNode wrapper)
- Operators: `erLik`, `erStørreEnn`, etc. (domain predicates)
- Math: `+`, `-`, `*`, `/`
- Formula system: `Formel`, `avrund2desimal`, etc.
- Boolean: `Sammenligning`, `MengdeRelasjon`

**Remove:**
- FaktumNode (no longer needed)
- All ARC classes (AbstractRuleComponent, Rule, Ruleset, etc.)
- TrackablePredicate, TrackableCondition (context handles tracking)

**Enhance:**
- Add `Uttrykk.dependencies(): List<Uttrykk<*>>` to get contributing expressions
- Ensure all Uttrykk implementations properly track their dependencies

## Formatter Interface

Formatters become dramatically simpler:

```kotlin
interface ExecutionFormatter<T> {
    fun format(node: ExecutionNode<*>, tree: ExecutionTree): T
}

// Example: IndentedTextFormatter
class IndentedTextFormatter : ExecutionFormatter<String> {
    override fun format(node: ExecutionNode<*>, tree: ExecutionTree): String {
        return buildString {
            appendLine("${node.uttrykk.navn} = ${node.uttrykk.verdi}")

            // Get contributing expressions
            tree.dependencies(node).forEach { dependency ->
                val depNode = tree.find { it.uttrykk == dependency }
                if (depNode != null) {
                    val explanation = format(depNode, tree)
                    // indent and append
                    explanation.lines().forEach { line ->
                        appendLine("  $line")
                    }
                }
            }
        }
    }
}
```

**No type checking, no filterIsInstance, no special cases.**

## Migration Strategy

### Phase 1: Prototype in Branch
- Implement ExecutionContext
- Implement ExecutionTree
- Create simple test cases
- Verify formatters are simpler

### Phase 2: Rewrite testclient-sliterordningen
- Convert to context-driven model
- Compare complexity
- Verify all tests pass

### Phase 3: Evaluate
- Is it simpler?
- Is it more testable?
- Can we migrate gradually?
- Decision point: proceed or abandon

### Phase 4: (If proceeding) Gradual Migration
- Keep ARC model for backward compatibility
- New code uses context model
- Provide migration guide

## Open Questions

1. **Performance**: Does explicit context tracking add overhead?
2. **Pattern System**: How does `createPattern()` work with context?
3. **Resource Management**: Where do resources (satser, etc.) live?
4. **Testing**: How to inject/mock context?
5. **Backwards Compatibility**: Can both models coexist?
6. **RETURNER semantics**: How to handle early returns with context?
7. **Error Handling**: How to capture failed predicates?

## Success Criteria

The new model succeeds if:
1. ✅ Formatters have <50 lines of code (vs 160 today)
2. ✅ No type checking (`is`, `when`, `filterIsInstance`) in formatters
3. ✅ Tree query API is intuitive
4. ✅ User code organization is similar to today
5. ✅ All testclient-sliterordningen tests pass
6. ✅ Explanation quality is equal or better

## Next Steps

1. **Team Review**: Discuss this design
2. **Refinement**: Address concerns and open questions
3. **Spike**: 2-day prototype to validate approach
4. **Decision**: Continue or refine ARC model?

---

*Document created: 2025-12-09*
*Contributors: [Team to add names]*
