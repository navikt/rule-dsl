package no.nav.system.rule.dsl.rettsregel.forklartfaktum

/**
 * Resultat med regelflyt-forklaring for ikke-numeriske verdier.
 *
 * Dette brukes for regelsett som returnerer ikke-numeriske verdier (enum, boolean, etc.)
 * der man primært trenger regelflyt-sporing (HVORFOR) uten beregningsforklaring (HVORDAN).
 *
 * For numeriske beregninger med AST-basert forklaring, bruk [ForklartFaktum] i stedet.
 *
 * @param T typen til resultatet (kan være Any)
 * @property name navnet på resultatet
 * @property value verdien til resultatet
 * @property hvorfor regelflyt-sporingen som forklarer hvordan resultatet ble nådd
 *
 * @see ForklartFaktum for numeriske verdier med HVORFOR + HVORDAN forklaring
 */
data class ForklartResultat<T : Any>(
    val name: String,
    val value: T,
    val hvorfor: String
) {
    /**
     * Formatterer forklaringen for visning.
     *
     * Output struktur:
     * ```
     * HVA
     *     <name> = <value>
     *
     * HVORFOR (Regelflyt-sporing):
     *     <regelflyt sporing med innrykk>
     * ```
     */
    fun forklaring(): String = buildString {
        appendLine("HVA")
        appendLine("    $name = $value")
        appendLine()
        appendLine("HVORFOR (Regelflyt-sporing):")
        appendLine(hvorfor.prependIndent("    "))
    }

    override fun toString(): String = forklaring()
}
