package no.nav.system.rule.dsl.forklaring

// ========================================================================
// Logiske operatorer
// ========================================================================

/**
 * Logisk OG-operator.
 */
internal data class Og(
    val venstre: Uttrykk<Boolean>,
    val høyre: Uttrykk<Boolean>
) : BinærOperator<Boolean, Boolean, Boolean>() {

    override fun venstre() = venstre
    override fun høyre() = høyre

    override fun operatorNavn() = "Og"
    override fun operatorSymbol() = "OG"

    override fun evaluer(): Boolean = venstre.evaluer() && høyre.evaluer()
}

/**
 * Logisk ELLER-operator.
 */
internal data class Eller(
    val venstre: Uttrykk<Boolean>,
    val høyre: Uttrykk<Boolean>
) : BinærOperator<Boolean, Boolean, Boolean>() {

    override fun venstre() = venstre
    override fun høyre() = høyre

    override fun operatorNavn() = "Eller"
    override fun operatorSymbol() = "ELLER"

    override fun evaluer(): Boolean = venstre.evaluer() || høyre.evaluer()
}

/**
 * Logisk IKKE-operator (negasjon).
 */
internal data class Ikke(
    val uttrykk: Uttrykk<Boolean>
) : UnærOperator<Boolean, Boolean>() {

    override fun uttrykk() = uttrykk

    override fun operatorNavn() = "Ikke"
    override fun operatorSymbol() = "IKKE"

    override fun evaluer(): Boolean = !uttrykk.evaluer()
}
