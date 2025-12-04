# TrackerResource Implementation Plan

## Overview
Replace the current post-execution explanation system with a real-time TrackerResource architecture using the ResourceMap pattern.

## Goals
- **Real-time tracking**: Capture execution data during rule evaluation via hooks, not post-execution tree traversal
- **Zero overhead**: No tracker registered = no tracking cost
- **User control**: Users instantiate and register their own TrackerResource implementation
- **Preserve API**: Keep `forklar()` signature identical
- **Complete replacement**: Delete all `explanation/*` files (no backward compatibility needed - prototype phase)

## Architecture

### 1. TrackerResource Base Class
**File**: `src/main/kotlin/no/nav/system/rule/dsl/tracker/TrackerResource.kt`

**Generic Design**: `TrackerResource<R>` where `R` is the native return type (e.g., `String`, `ExplanationModel`, `JsonObject`)

**Accumulator/Collector Pattern**:
- **Accumulation Phase** (during execution): Hooks accumulate data into internal mutable state
- **Transformation Phase** (when requested): Transform accumulated data → result of type `R`

Abstract base class extending `AbstractResource` with:

- **Hook methods** (open with empty defaults - ACCUMULATION):
  - `onFaktumCreated(faktum, parent)` - Called when Faktum created via sporing()
  - `onRuleEvaluationStart(rule)` - Called before rule predicates evaluated
  - `onRuleEvaluationEnd(rule, fired)` - Called after rule completes
  - `onPredicateEvaluated(predicate, rule, result)` - Called after each predicate
  - `onRulesetEnter(component)` - Called when ruleset starts
  - `onRulesetExit(component)` - Called when ruleset completes

- **Transformation methods** (abstract - must be implemented - TRANSFORMATION):
  - `explainFaktum(faktum, filter): R` - Generate explanation in native type R
  - `explainFaktumAsString(faktum, filter): String` - Generate String explanation (required for forklar() API)
  - `getAllFaktum(): List<FaktumNode<*>>` - Return all tracked Faktum nodes
  - `traverseTree(root): R` - Generate tree representation in native type R

**Key Design Principle**: Every tracker must provide both:
1. **Native format** (`explainFaktum` returns `R`) - structured data for programmatic use
2. **String format** (`explainFaktumAsString` returns `String`) - human-readable for forklar() API

### 2. Built-in Implementations

#### NoOpTracker (Default Fallback)
**File**: `src/main/kotlin/no/nav/system/rule/dsl/tracker/NoOpTracker.kt`

Default tracker used when no tracker is registered:
```kotlin
class NoOpTracker : TrackerResource<String>() {
    override fun explainFaktum(faktum: Faktum<*>, filter: Filter): String {
        return "No tracker implementation found. Register a tracker via putResource(TrackerResource::class, ...)"
    }

    override fun explainFaktumAsString(faktum: Faktum<*>, filter: Filter): String {
        return explainFaktum(faktum, filter)
    }

    override fun getAllFaktum(): List<FaktumNode<*>> = emptyList()
    override fun traverseTree(root: AbstractRuleComponent): String = "No tracker registered"
}
```

**Registration**: Automatically used when no custom tracker registered (no exceptions thrown)

#### IndentedTextTracker
**File**: `src/main/kotlin/no/nav/system/rule/dsl/tracker/IndentedTextTracker.kt`

**Type**: `TrackerResource<String>` - Native format is already String

Simple text output for logging/debugging:
- **Accumulation**: Tracks relationships in mutable maps during execution
- **Transformation**: Builds formatted string from accumulated data
- Output format: HVA/HVORDAN/HVORFOR sections with indentation

**Data structures** (accumulator state):
```kotlin
private val faktumNodes = mutableListOf<FaktumNode<*>>()
private val faktumToRuleContext = mutableMapOf<Faktum<*>, MutableList<RuleContext>>()
private val ruleToPredicates = mutableMapOf<Rule<*>, MutableList<PredicateEvaluation>>()
private var currentRule: Rule<*>? = null
```

**Implementation**:
```kotlin
class IndentedTextTracker : TrackerResource<String>() {
    override fun explainFaktum(faktum: Faktum<*>, filter: Filter): String {
        return buildString { /* transform accumulated data → String */ }
    }

    override fun explainFaktumAsString(faktum: Faktum<*>, filter: Filter): String {
        return explainFaktum(faktum, filter)  // Same - already String!
    }
}
```

#### SectionTracker
**File**: `src/main/kotlin/no/nav/system/rule/dsl/tracker/SectionTracker.kt`

**Type**: `TrackerResource<ExplanationModel>` - Native format is structured data

Structured data model for GUI/JSON consumers:
- **Accumulation**: Same tracking strategy as IndentedTextTracker
- **Transformation**: Builds `ExplanationModel` from accumulated data
- Includes `ForklaringTypeEnum` (HVA, HVORDAN, HVORFOR)
- Supports metadata for each section

**Data classes**:
```kotlin
data class ExplanationModel(val sections: List<Section>)
data class Section(
    val type: ForklaringTypeEnum,
    val lines: List<String>,
    val children: List<Section> = emptyList(),
    val metadata: Map<String, Any> = emptyMap()
)
enum class ForklaringTypeEnum { HVA, HVORDAN, HVORFOR }
```

**Implementation**:
```kotlin
class SectionTracker : TrackerResource<ExplanationModel>() {
    override fun explainFaktum(faktum: Faktum<*>, filter: Filter): ExplanationModel {
        return ExplanationModel(sections = /* transform accumulated data → model */)
    }

    override fun explainFaktumAsString(faktum: Faktum<*>, filter: Filter): String {
        return explainFaktum(faktum, filter).toHvaHvordanHvorfor()  // Convert model → String
    }
}
```

### 3. Public API
**File**: `src/main/kotlin/no/nav/system/rule/dsl/tracker/TrackerExtensions.kt`

Extension functions for user-facing API:

```kotlin
// Main API - explain single Faktum (always returns String)
fun <T : Any> Faktum<T>.forklar(filter: Filter = Filters.FUNCTIONAL): String {
    val tracker = wrapperNode?.resourceMap?.get(TrackerResource::class) as? TrackerResource<*>
        ?: NoOpTracker()  // Use default instead of throwing exception
    return tracker.explainFaktumAsString(this, filter)
}

// Utility APIs
fun AbstractRuleComponent.collectFaktum(): List<FaktumNode<*>> {
    val tracker = resourceMap[TrackerResource::class] as? TrackerResource<*>
        ?: NoOpTracker()
    return tracker.getAllFaktum()
}

fun AbstractRuleComponent.traverseHva(): String {
    val tracker = resourceMap[TrackerResource::class] as? TrackerResource<*>
        ?: NoOpTracker()
    return tracker.traverseTree(this).toString()
}

// Builder pattern API (for compatibility)
fun AbstractRuleComponent.explain(): ExplanationBuilder
fun ExplanationModel.toIndentedText(): String
enum class Direction { UP, DOWN }
class ExplanationBuilder { ... }
```

**Key API Design**:
- `forklar()` always returns `String` (uses `explainFaktumAsString()`)
- Uses `NoOpTracker()` as fallback instead of throwing exceptions
- Users who need native type `R` access tracker directly from resourceMap

### 4. Filter System
**File**: `src/main/kotlin/no/nav/system/rule/dsl/tracker/Filter.kt` (moved from explanation/)

```kotlin
fun interface Filter {
    fun includes(component: AbstractRuleComponent): Boolean
}

object Filters {
    val ALL = Filter { true }
    val FUNCTIONAL = Filter { /* rules, branches, predicates, faktum only */ }
}
```

## Implementation Steps

### Phase 1: Infrastructure
1. Create `tracker/` directory
2. Move `Filter.kt` from `explanation/` to `tracker/`
3. Create `TrackerResource.kt` base class with generic type `TrackerResource<R>`
4. Create `NoOpTracker.kt` - default fallback tracker
5. Create `IndentedTextTracker.kt` implementation (`TrackerResource<String>`)
6. Create `SectionTracker.kt` implementation (`TrackerResource<ExplanationModel>`)
7. Create `TrackerExtensions.kt` with public API (uses NoOpTracker as fallback)

### Phase 2: Framework Hooks
Add tracker notifications to framework:

1. **AbstractRuleComponent.sporing()**
   - After adding FaktumNode to tree
   - Call: `tracker?.onFaktumCreated(faktum, this)`

2. **Rule.evaluate()**
   - Before predicate loop: `tracker?.onRuleEvaluationStart(this)`
   - After each predicate: `tracker?.onPredicateEvaluated(predicate, this, result)`
   - After action/else: `tracker?.onRuleEvaluationEnd(this, fired)`

3. **AbstractRuleset.internalRun()**
   - After create(): `tracker?.onRulesetEnter(this)`
   - Before both returns: `tracker?.onRulesetExit(this)`

Tracker lookup pattern:
```kotlin
(resourceMap[TrackerResource::class] as? TrackerResource)?.onHookMethod(...)
```

### Phase 3: Testing & Cleanup
1. Compile and verify hooks work without tracker (no exceptions)
2. Update test base classes to register tracker:
   - `AbstractDemoRuleService`: `putResource(TrackerResource::class, IndentedTextTracker())`
   - `AbstractDemoRuleset`: `putResource(TrackerResource::class, IndentedTextTracker())`
3. Run all tests - verify passing
4. **Delete old explanation system**:
   - `explanation/ExplanationBuilder.kt`
   - `explanation/ExplanationModel.kt` (moved to SectionTracker)
   - `explanation/Renderers.kt`
   - `explanation/` directory
   - `test/.../explanation/ExplanationBuilderTest.kt`

### Phase 4: Documentation Updates
1. Update CLAUDE.md with TrackerResource pattern
2. Add examples of registering custom trackers
3. Document hook lifecycle

## Usage Examples

### Basic Usage (Service)
```kotlin
class MyService : AbstractRuleService<Response>() {
    override fun run(): Response {
        // Register tracker for this service execution
        putResource(TrackerResource::class, IndentedTextTracker())
        return super.run()
    }
}
```

### Basic Usage (Standalone Ruleset)
```kotlin
class MyRuleset : AbstractRuleset<Unit>() {
    override fun test(): Unit {
        putResource(TrackerResource::class, IndentedTextTracker())
        return internalRun()
    }
}
```

### Explaining Results
```kotlin
val result = service.run()
val faktum = result.someFaktum

// Simple text explanation (always works, uses explainFaktumAsString)
val explanation: String = faktum.forklar()
println(explanation)

// Collect all faktum
val allFaktum = service.collectFaktum()

// Tree traversal
val tree = service.traverseHva()
```

### Accessing Native Format (Structured Data)
```kotlin
// Register SectionTracker
val service = MyService().apply {
    putResource(TrackerResource::class, SectionTracker())
}
val result = service.run()

// Get tracker and access native ExplanationModel
val tracker = service.resourceMap[TrackerResource::class] as SectionTracker
val model: ExplanationModel = tracker.explainFaktum(result.faktum, Filters.FUNCTIONAL)

// Use model for GUI, JSON serialization, etc.
val json = model.toJSON()
```

### Custom Tracker
```kotlin
class JSONTracker : TrackerResource<JsonObject>() {
    // Accumulator: collect events during execution
    private val events = mutableListOf<Event>()

    override fun onFaktumCreated(faktum: Faktum<*>, parent: AbstractRuleComponent) {
        events.add(Event("faktum_created", faktum.navn, faktum.verdi))
    }

    // Transformation: return native JsonObject
    override fun explainFaktum(faktum: Faktum<*>, filter: Filter): JsonObject {
        return JsonObject(
            "faktum" to faktum.navn,
            "events" to events.filter { it.name == faktum.navn }
        )
    }

    // String variant: convert JsonObject → String
    override fun explainFaktumAsString(faktum: Faktum<*>, filter: Filter): String {
        return explainFaktum(faktum, filter).toString()
    }

    override fun getAllFaktum(): List<FaktumNode<*>> = emptyList()
    override fun traverseTree(root: AbstractRuleComponent): JsonObject {
        return JsonObject("tree" to events)
    }
}
```

## Migration Notes

### No Backward Compatibility
This is a complete replacement. No migration path needed since we're in prototype phase.

### Key Changes
1. **Registration**: Tracker registered via `putResource()` in `run()` method
2. **Key**: Always use `TrackerResource::class` as the key, not the concrete implementation class
3. **API**: `forklar()` works the same, but implementation is completely different
4. **toString()**: Rule and TrackablePredicate now include type prefix ("regel:", "predikat:")

### Breaking Changes
None - this is prototype code with no external users.

## Testing Strategy

### Unit Tests
- `TrackerExtensionsTest.kt` - Test public API
- Verify all hooks called correctly
- Test with and without tracker registered

### Integration Tests
- All existing tests should pass with IndentedTextTracker
- Verify SectionTracker builds correct models
- Test builder pattern API compatibility

### Expected Results
- All tests pass (187-188 tests)
- Zero overhead when no tracker registered
- Clean, readable explanations from `forklar()`

## Success Criteria

**v2 Design (Updated):**
- [ ] TrackerResource<R> generic base class created
- [ ] NoOpTracker default fallback created
- [ ] explainFaktum(faktum, filter): R method (native format)
- [ ] explainFaktumAsString(faktum, filter): String method (String format)
- [ ] IndentedTextTracker extends TrackerResource<String>
- [ ] SectionTracker extends TrackerResource<ExplanationModel>
- [ ] Framework hooks added (sporing, evaluate, internalRun)
- [ ] Public API preserved (forklar signature identical, returns String)
- [ ] forklar() uses NoOpTracker as fallback (no exceptions)
- [ ] Old explanation system deleted
- [ ] All tests passing (99%+ pass rate)
- [ ] Graceful degradation when no tracker registered

**v1 Implementation (Completed):**
- ✅ TrackerResource base class created (non-generic)
- ✅ IndentedTextTracker implementation complete
- ✅ SectionTracker implementation complete
- ✅ Framework hooks added
- ✅ Old explanation system deleted
- ✅ 187/188 tests passing (99.5%)

## Timeline

Estimated: 2-3 hours
- Phase 1 (Infrastructure): 45 min
- Phase 2 (Hooks): 30 min
- Phase 3 (Testing & Cleanup): 45 min
- Phase 4 (Documentation): 30 min

## Status

**🔄 DESIGN UPDATE REQUIRED** - 2025-11-26

Initial implementation complete (187/188 tests passing), but needs refactoring to match updated design:

### Current Implementation (v1)
- ❌ Non-generic: `TrackerResource` (no type parameter)
- ❌ Returns String only: `explainFaktum(faktum, filter): String`
- ❌ Throws exceptions when no tracker registered

### Target Design (v2 - This Document)
- ✅ Generic: `TrackerResource<R>` where R is native return type
- ✅ Dual methods:
  - `explainFaktum(faktum, filter): R` - native format
  - `explainFaktumAsString(faktum, filter): String` - String format
- ✅ Accumulator/Collector pattern clearly separated
- ✅ NoOpTracker as default fallback (no exceptions)

### Next Steps
1. Refactor `TrackerResource` to be generic `TrackerResource<R>`
2. Add `explainFaktumAsString()` method to base class
3. Create `NoOpTracker` as default fallback
4. Update `IndentedTextTracker` to extend `TrackerResource<String>`
5. Update `SectionTracker` to extend `TrackerResource<ExplanationModel>`
6. Update `forklar()` to use `NoOpTracker()` as fallback
7. Verify all tests still pass
