# Uttrykk vs Formel - To tilnærminger til regelsporing

Dette dokumentet sammenligner to måter å representere matematiske beregninger på:
1. **Formel** - Dagens implementasjon (string-basert med lazy evaluation)
2. **Uttrykk** - Ny rekursiv AST-struktur (explicit tree structure)

## Konseptuell forskjell

### Formel (dagens løsning)
Formel er en **runtime-konstruert** representasjon som bygges opp ved hjelp av operator overloading:

```kotlin
val G = Formel.variable("G", 110000)
val resultat = 0.25 * G / 12

// Internt lagres:
// - notasjon: "0.25 * G / 12"  (string)
// - innhold: "0.25 * 110000 / 12"  (string)
// - namedVarMap: {"G" -> 110000}
// - subFormelList: Set<Formel>
// - locked: Boolean (om den skal ekspanderes eller ikke)
```

### Uttrykk (rekursiv AST)
Uttrykk er en **eksplisitt trestruktur** som representerer beregningen:

```kotlin
val G = Faktum("G", 110000)
val resultat = Div(
    Mul(Const(0.25), Var(G)),
    Const(12)
)

// Internt lagres:
// Div(
//   venstre = Mul(Const(0.25), Var(Faktum("G", 110000))),
//   høyre = Const(12)
// )
```

## Sammenligning

| Aspekt | Formel | Uttrykk |
|--------|--------|---------|
| **Representasjon** | String-basert notasjon + evaluering | Eksplisitt trestruktur |
| **Oppbygging** | Operator overloading (naturlig syntaks) | Operator overloading (naturlig syntaks) |
| **Type system** | Kompleks type-inferens (shouldBeDouble flag) | Pattern matching på sealed interface |
| **Traversering** | Via `subFormelList` (flat Set) | Via rekursiv pattern matching |
| **Forklaringsgenerering** | Parser `notasjon` og `innhold` strings | Traverser trestruktur direkte |
| **Locking** | `locked` boolean flag | `Navngitt<T>` wrapper type |
| **Debugging** | `toTreeString()`, `toHTML()` metoder | `treVisning()`, visitor pattern |
| **Transformasjoner** | Vanskelig (må manipulere strings) | Enkelt (pattern matching) |
| **Ytelse (oppbygging)** | Bygger strings ved hver operasjon | Bygger objekttre ved hver operasjon |
| **Ytelse (evaluering)** | Lazy evaluation med Crunch parser | Direct recursive evaluation |
| **Minnesforbruk** | Strings + Set<Formel> | Objekttre (hver node = objekt) |
| **Kompleksitet** | Medium (string manipulation) | Lav (standard rekursiv datastruktur) |

## Fordeler og ulemper

### Formel (dagens løsning)

**Fordeler:**
- ✅ Naturlig syntaks med operator overloading
- ✅ Lazy evaluation (beregner kun når nødvendig)
- ✅ Kompakt representasjon (strings i stedet for objekttre)
- ✅ `locked` mekanisme for performance og lesbarhet
- ✅ Eksisterende infrastruktur og tester
- ✅ Crunch library for sikker evaluering

**Ulemper:**
- ❌ Vanskelig å traverse strukturen systematisk
- ❌ String parsing for forklaring (fragile)
- ❌ Kompleks håndtering av type-inferens
- ❌ Transformasjoner må manipulere strings
- ❌ Debugging er avhengig av string representasjon
- ❌ `mergeAndValidateVarMaps` kompleksitet

### Uttrykk (rekursiv AST)

**Fordeler:**
- ✅ Enkel traversering med pattern matching
- ✅ Type-sikkerhet via sealed interface
- ✅ Transformasjoner er trivielle (visitor pattern)
- ✅ Forklaringsgenerering er direkte
- ✅ Standard rekursiv datastruktur (velkjent pattern)
- ✅ Enkel å utvide med nye operasjoner
- ✅ Naturlig for compilator-teknologi

**Ulemper:**
- ❌ Større minnesforbruk (objekttre vs strings)
- ❌ Mer overhead ved oppbygging
- ❌ Ingen lazy evaluation (evaluerer alltid)
- ❌ Må bygges fra scratch (ikke eksisterende infrastruktur)
- ❌ Krever reimplementasjon av eksisterende Formel-logikk

## Brukseksempler

### Enkel beregning

**Formel:**
```kotlin
val G = Formel.variable("G", 110000)
val resultat = FormelBuilder.create<Double>()
    .name("beregning")
    .expression(0.25 * G / 12)
    .build()

println(resultat.forklarKompakt())
// Output:
// beregning = 0.25 * G / 12
// beregning = 0.25 * 110000 / 12
// beregning = 2291.67
```

**Uttrykk:**
```kotlin
val G = Faktum("G", 110000)
val resultat = 0.25 * Var(G) / 12

println(resultat.forklarKompakt("beregning"))
// Output:
// beregning = 0.25 * G / 12
// beregning = 0.25 * 110000 / 12
// beregning = 2291.67
```

### Navngitte subforklaringer

**Formel:**
```kotlin
val subFormel = FormelBuilder.create<Double>()
    .name("sub")
    .expression(a + b)
    .locked()  // Atomisk enhet
    .build()

val hovedformel = subFormel * c
```

**Uttrykk:**
```kotlin
val subUttrykk = (Var(a) + Var(b)).navngi("sub")
val hoveduttrykk = subUttrykk * Var(c)
```

### Transformasjoner

**Formel:**
```kotlin
// Vanskelig! Må manipulere strings eller rebuilde
val transformert = formel.copy(
    notasjon = formel.notasjon.replace("x", "100"),
    // ... kompleks logikk
)
```

**Uttrykk:**
```kotlin
// Enkelt! Pattern matching
val transformert = uttrykk.erstatt("x") { Const(100) }

// Forenkling
val forenklet = uttrykk.forenkel()

// Custom transformasjon med visitor
val alleKonstanter = uttrykk.visit { expr ->
    when (expr) {
        is Const -> listOf(expr.verdi)
        else -> emptyList()
    }
}
```

## Når bruke hva?

### Bruk Formel når:
- ✓ Du trenger lazy evaluation
- ✓ Minnesforbruk er kritisk
- ✓ Du jobber med eksisterende kodebase
- ✓ Du trenger Crunch library for sikkerhet
- ✓ Performance ved oppbygging er viktig

### Bruk Uttrykk når:
- ✓ Du trenger mange transformasjoner
- ✓ Traversering av strukturen er viktig
- ✓ Du bygger nye verktøy (compilator, analyzer)
- ✓ Type-sikkerhet er kritisk
- ✓ Du trenger interaktive forklaringer
- ✓ Debugging av strukturen er viktig

## Hybridtilnærming?

Det er mulig å kombinere begge:

```kotlin
// Formel med eksplisitt AST-representasjon
class Formel<T : Number> {
    val notasjon: String        // For display
    val innhold: String         // For evaluation
    val ast: Uttrykk<T>?        // Optional AST for traversering

    // Bruk AST for transformasjoner og forklaring
    // Bruk strings for evaluering og display
}
```

**Fordeler med hybrid:**
- Kompatibel med eksisterende kode
- AST tilgjengelig når nødvendig
- Kan bygges opp gradvis

**Ulemper med hybrid:**
- Kompleksitet (to representasjoner å maintaine)
- Minnesforbruk (begge representasjoner)
- Synkronisering mellom AST og strings

## Konklusjon

**Formel** er godt egnet for dagens bruk:
- Fungerer bra for eksisterende regelsporing
- Kompakt og effektiv
- Etablert infrastruktur

**Uttrykk** er bedre for fremtidig utvikling:
- Enklere å jobbe med programmatisk
- Mer fleksibel for nye features
- Standard datastruktur fra compilator-teori

**Anbefaling:**
1. Behold Formel for eksisterende produksjonskode
2. Bruk Uttrykk for:
   - Nye verktøy og analyzers
   - Interaktive forklaringer
   - Pedagogiske eksempler
   - Eksperimentering med nye konsepter
3. Vurder gradvis migrering eller hybrid tilnærming

## Se også

- `Formel.kt` - Dagens implementasjon
- `Uttrykk.kt` - Ny rekursiv struktur
- `FormelForklaring.kt` - Forklaring for Formel
- `UttrykkForklaring.kt` - Forklaring for Uttrykk
- `UttrykkDemo.kt` - Eksempler med Uttrykk
