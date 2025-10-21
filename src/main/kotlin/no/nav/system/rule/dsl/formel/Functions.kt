package no.nav.system.rule.dsl.formel


// avrund explicitly returns Int
fun avrund(formel: Formel<Double>): Formel<Int> =
    applySyntax1arg(formel, false) { "avrund( $it )" }

// avrund2Desimal explicitly returns Double
fun avrund2Desimal(formel: Formel<Double>): Formel<Double> =
    applySyntax1arg(formel, true) { "avrundMedToDesimal( $it )" }

// afpAvrundBrutto explicitly returns Int
fun afpAvrundBrutto(formel: Formel<Double>): Formel<Int> =
    applySyntax1arg(formel, false) { "afpAvrundBrutto( $it )" }

// afpAvrundNetto explicitly returns Int
fun afpAvrundNetto(formelA: Formel<Double>, formelB: Formel<Int>): Formel<Int> =
    applySyntax2arg(formelA, formelB, false) { a, b -> "afpAvrundNetto( $a, $b )" }

/**
 * MAX
 */
private val maxFunction: (String, String) -> String = { a, b -> "max( $a, $b )" }

// Both operands are Int, result should be Int
@JvmName("maxIntInt")
fun max(formelA: Formel<Int>, formelB: Formel<Int>): Formel<Int> =
    applySyntax2arg(formelA, formelB, false, maxFunction)

// Both operands are Double, result should be Double
@JvmName("maxDblDbl")
fun max(formelA: Formel<Double>, formelB: Formel<Double>): Formel<Double> =
    applySyntax2arg(formelA, formelB, true, maxFunction)

// Both operands are Int, result should be Int
fun max(maks: Int, formel: Formel<Int>): Formel<Int> =
    applySyntax2arg(Formel.constant(maks), formel, false, maxFunction)

// At least one operand is Double, result should be Double
fun max(maks: Double, formel: Formel<Double>): Formel<Double> =
    applySyntax2arg(Formel.constant(maks), formel, true, maxFunction)

/**
 * MIN
 */
private val minFunction: (String, String) -> String = { a, b -> "min( $a, $b )" }

// Both operands are Int, result should be Int
@JvmName("minIntInt")
fun min(formelA: Formel<Int>, formelB: Formel<Int>): Formel<Int> =
    applySyntax2arg(formelA, formelB, false, minFunction)

// Both operands are Double, result should be Double
@JvmName("minDblDbl")
fun min(formelA: Formel<Double>, formelB: Formel<Double>): Formel<Double> =
    applySyntax2arg(formelA, formelB, true, minFunction)

// Both operands are Int, result should be Int
fun min(maks: Int, formel: Formel<Int>): Formel<Int> =
    applySyntax2arg(Formel.constant(maks), formel, false, minFunction)

// At least one operand is Double, result should be Double
fun min(maks: Double, formel: Formel<Double>): Formel<Double> =
    applySyntax2arg(Formel.constant(maks), formel, true, minFunction)

private fun <T : Number> applySyntax1arg(formel: Formel<*>, shouldBeDouble: Boolean, syntax: (String) -> String): Formel<T> {
    return Formel(
        emne = formel.emne,
        prefix = formel.prefix,
        postfix = formel.postfix,
        notasjon = syntax.invoke(formel.notasjon),
        innhold = syntax.invoke(formel.innhold),
        subFormelList = formel.subFormelList,
        namedVarMap = formel.namedVarMap,
        locked = formel.locked,
        shouldBeDouble = shouldBeDouble
    )
}

private fun <T : Number> applySyntax2arg(formelA: Formel<*>, formelB: Formel<*>, shouldBeDouble: Boolean, syntax: (String, String) -> String): Formel<T> {
    return Formel(
        emne = formelA.emne,
        prefix = formelA.prefix,
        postfix = formelA.postfix,
        notasjon = syntax.invoke(formelA.notasjon, formelB.notasjon),
        innhold = syntax.invoke(formelA.innhold, formelB.innhold),
        subFormelList = formelA.subFormelList + formelB.subFormelList,
        namedVarMap = formelA.mergeAndValidateVarMaps(formelA, formelB),
        locked = false, // Functions typically create unlocked results
        shouldBeDouble = shouldBeDouble
    )
}