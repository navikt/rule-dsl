package no.nav.system.rule.dsl.forklaring

import no.nav.system.rule.dsl.formel.Formel

/**
 * Adapter that allows Uttrykk/Grunnlag to be used where Formel is expected.
 *
 * This enables the integration of the new Uttrykk-based AST system with the existing
 * faktum() function in AbstractRuleComponent, which combines:
 * - HVORFOR (rule flow explanation via Trace.kt)
 * - HVORDAN (calculation explanation)
 *
 * The adapter wraps a Grunnlag and delegates to Uttrykk.forklarDetaljert() for
 * rendering, allowing the Uttrykk's automatic explanation generation to be used
 * in ForklartFaktum objects.
 *
 * Example usage:
 * ```kotlin
 * regel("BeregnSlitertillegg") {
 *     HVIS { true }
 *     SÅ {
 *         RETURNER(
 *             faktum(
 *                 (fulltSlitertillegg * justeringsFaktor * trygdetidFaktor)
 *                     .navngi("slitertillegg")
 *                     .id("SLITERTILLEGG-BEREGNET")
 *             )
 *         )
 *     }
 * }
 * ```
 *
 * This produces output combining rule trace (HVORFOR) and AST explanation (HVORDAN):
 * ```
 * HVORFOR:
 *   BeregnSlitertilleggService
 *     BehandleSliterordningFlyt
 *       BeregnSlitertilleggRS
 *         BeregnSlitertillegg
 *
 * HVORDAN:
 *     SLITERTILLEGG-BEREGNET
 *     slitertillegg = fulltSlitertillegg * justeringsFaktor * trygdetidFaktor
 *     slitertillegg = 2292.0 * 0.33 * 0.5
 *     slitertillegg = 378.15
 *
 *     fulltSlitertillegg = 0.25 * G / 12
 *     ...
 * ```
 *
 * @param T The numeric type (must be Number for compatibility with Formel)
 * @property grunnlag The wrapped Uttrykk/Grunnlag containing the AST
 */
class UttrykkFormelAdapter<T : Number>(
    private val grunnlag: Grunnlag<T>
) : Formel<T>(
    emne = grunnlag.navn,
    prefix = "",
    postfix = "",
    notasjon = grunnlag.notasjon(),  // Symbolic notation from AST
    innhold = grunnlag.konkret(),    // Concrete notation with values
    subFormelList = emptySet(),      // Uttrykk handles its own substructure
    namedVarMap = emptyMap(),        // Not used - Uttrykk tracks variables internally
    locked = true,                   // Treat as atomic unit
    shouldBeDouble = grunnlag.evaluer() is Double
) {

    /**
     * Override name to use Grunnlag's name directly
     */
    override val name: String get() = grunnlag.navn

    /**
     * Override value to evaluate the Uttrykk
     */
    override val value: T get() = grunnlag.evaluer()

    /**
     * Override toString() to use Uttrykk's detailed explanation instead of Formel's tree rendering.
     *
     * This is the key integration point - when ForklartFaktum renders the "HVORDAN" part,
     * it calls toString() on the Formel object. By delegating to forklarDetaljert(),
     * we get the full AST-based explanation with:
     * - RVS-ID (legal reference)
     * - Function name (from tracked())
     * - Symbolic notation
     * - Concrete notation with values
     * - Result
     * - Sub-formulas (navngitte uttrykk)
     */
    override fun toString(): String {
        return grunnlag.forklarDetaljert(
            navn = grunnlag.navn,
            maxDybde = 3,
            inkluderRvsId = true
        )
    }
}
