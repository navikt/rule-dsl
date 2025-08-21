# Formel API Migration Guide

The Formel package has been significantly improved in Phase 2 with better type safety, immutability, and cleaner APIs. This guide helps you migrate from the old API to the new one.

## 🚀 Key Improvements

- **Immutable by default**: Formulas cannot be accidentally modified after creation
- **Type safety**: Better handling of Int vs Double operations  
- **Clear factory methods**: Replace confusing constructors
- **Improved builder pattern**: More intuitive API with `FormelBuilder`
- **Better documentation**: Comprehensive docs on locking behavior

## 📋 Migration Steps

### 1. Creating Variables

**Old way:**
```kotlin
val grunnbeløp = Formel("grunnbeløp", 118620)    // Named variable
val constant = Formel(42)                        // Anonymous constant
```

**New way:**
```kotlin
val grunnbeløp = Formel.variable("grunnbeløp", 118620)  // Clear intent
val constant = Formel.constant(42)                      // Clear intent

// Or even shorter with DSL:
import no.nav.system.rule.dsl.formel.*
val grunnbeløp = variable("grunnbeløp", 118620)
val constant = constant(42)
```

### 2. Builder Pattern

**Old way:**
```kotlin
val formula = kmath<Int>()
    .emne("bruttobeløp") 
    .prefix("TP")
    .formel(someExpression)
    .unlock()
    .build()
```

**New way:**
```kotlin
val formula = FormelBuilder.create<Int>()
    .name("bruttobeløp")           // Clearer than "emne" 
    .prefix("TP")
    .expression(someExpression)    // Clearer than "formel"
    .unlocked()                    // Clearer than "unlock"
    .build()

// Or with DSL:
val formula = formula<Int>("bruttobeløp") {
    prefix("TP") 
    expression(someExpression)
    unlocked()
}
```

### 3. Mutating Operations

**Old way (dangerous):**
```kotlin
var formula = Formel("value", 100)
formula.emne = "newName"  // Mutates existing formula!
```

**New way (safe):**
```kotlin
val formula = Formel.variable("value", 100)
val renamed = formula.named("newName")  // Creates new formula
// OR
val renamed = formula.copy(emne = "newName")
```

### 4. Understanding Locking

Locking behavior is now clearly documented:

```kotlin
// Unlocked formulas (default for simple operations)  
val a = Formel.variable("a", 10)
val b = Formel.variable("b", 20)
val sum = a + b  // Notation: "a + b" (variables expanded)

// Locked formulas (default for builder-created formulas)
val complexCalc = FormelBuilder.create<Int>()
    .name("complex")
    .expression(someVeryComplexFormula)
    .build()  // locked=true by default

val total = complexCalc + 100  // Notation: "complex + 100" (not expanded)
```

### 5. Operator Usage

**Old way:**
```kotlin
// Relied on constructors in operator definitions
```

**New way:**
```kotlin
// All operators now use clear factory methods internally
val result = grunnbeløp * 0.45 + tillegg
// Type-safe: automatically handles Int vs Double operations
```

## 🔄 Deprecated API Compatibility

The old API still works but shows deprecation warnings with automatic migration suggestions:

```kotlin
@Deprecated("Use Formel.variable(name, value) instead", ReplaceWith("Formel.variable(emne, num)"))
constructor(emne: String, num: T)

@Deprecated("Use FormelBuilder.create<T>() instead", ReplaceWith("FormelBuilder.create<T>()")) 
fun kmath(): Builder<T>
```

Your IDE will suggest automatic migrations.

## ✅ Benefits of Migration

1. **Type Safety**: Fewer runtime errors with better Int/Double handling
2. **Immutability**: No accidental mutations, thread-safe
3. **Clarity**: Clear intent with `.variable()`, `.constant()`, `.expression()`  
4. **Performance**: Better validation and error handling
5. **Maintainability**: Better documentation and examples

## 🎯 Quick Reference

| Old | New | Notes |
|-----|-----|-------|
| `Formel("name", 42)` | `Formel.variable("name", 42)` | Named variable |
| `Formel(42)` | `Formel.constant(42)` | Anonymous constant |
| `kmath<Int>()` | `FormelBuilder.create<Int>()` | Builder pattern |
| `.emne("name")` | `.name("name")` | Clearer naming |
| `.formel(expr)` | `.expression(expr)` | Clearer naming |  
| `.unlock()` | `.unlocked()` | Clearer naming |
| `formula.emne = "new"` | `formula.named("new")` | Immutable operations |

## 📞 Need Help?

If you encounter issues during migration:
1. Check the deprecation warnings - they include automatic replacement suggestions
2. Review the comprehensive class documentation  
3. Look at the test files for examples of the new patterns
4. The old API continues to work during the transition period