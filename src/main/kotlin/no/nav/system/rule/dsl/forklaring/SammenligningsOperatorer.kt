package no.nav.system.rule.dsl.forklaring

// ========================================================================
// Sammenligningsoperatorer
// ========================================================================

/**
 * Sammenligning: Lik (==).
 */
internal data class Lik<T : Comparable<T>>(
    val venstre: Uttrykk<T>,
    val høyre: Uttrykk<T>
) : SammenligningOperator<T>() {

    override fun venstre() = venstre
    override fun høyre() = høyre

    override fun operatorNavn() = "Lik"
    override fun operatorSymbol() = "="

    override fun evaluer(): Boolean = venstre.evaluer() == høyre.evaluer()
}

/**
 * Sammenligning: Ulik (!=).
 */
internal data class Ulik<T : Comparable<T>>(
    val venstre: Uttrykk<T>,
    val høyre: Uttrykk<T>
) : SammenligningOperator<T>() {

    override fun venstre() = venstre
    override fun høyre() = høyre

    override fun operatorNavn() = "Ulik"
    override fun operatorSymbol() = "≠"

    override fun evaluer(): Boolean = venstre.evaluer() != høyre.evaluer()
}

/**
 * Sammenligning: Større enn (>).
 */
internal data class StørreEnn<T : Comparable<T>>(
    val venstre: Uttrykk<T>,
    val høyre: Uttrykk<T>
) : SammenligningOperator<T>() {

    override fun venstre() = venstre
    override fun høyre() = høyre

    override fun operatorNavn() = "StørreEnn"
    override fun operatorSymbol() = ">"

    override fun evaluer(): Boolean = venstre.evaluer() > høyre.evaluer()
}

/**
 * Sammenligning: Mindre enn (<).
 */
internal data class MindreEnn<T : Comparable<T>>(
    val venstre: Uttrykk<T>,
    val høyre: Uttrykk<T>
) : SammenligningOperator<T>() {

    override fun venstre() = venstre
    override fun høyre() = høyre

    override fun operatorNavn() = "MindreEnn"
    override fun operatorSymbol() = "<"

    override fun evaluer(): Boolean = venstre.evaluer() < høyre.evaluer()
}

/**
 * Sammenligning: Større eller lik (>=).
 */
internal data class StørreEllerLik<T : Comparable<T>>(
    val venstre: Uttrykk<T>,
    val høyre: Uttrykk<T>
) : SammenligningOperator<T>() {

    override fun venstre() = venstre
    override fun høyre() = høyre

    override fun operatorNavn() = "StørreEllerLik"
    override fun operatorSymbol() = "≥"

    override fun evaluer(): Boolean = venstre.evaluer() >= høyre.evaluer()
}

/**
 * Sammenligning: Mindre eller lik (<=).
 */
internal data class MindreEllerLik<T : Comparable<T>>(
    val venstre: Uttrykk<T>,
    val høyre: Uttrykk<T>
) : SammenligningOperator<T>() {

    override fun venstre() = venstre
    override fun høyre() = høyre

    override fun operatorNavn() = "MindreEllerLik"
    override fun operatorSymbol() = "≤"

    override fun evaluer(): Boolean = venstre.evaluer() <= høyre.evaluer()
}
