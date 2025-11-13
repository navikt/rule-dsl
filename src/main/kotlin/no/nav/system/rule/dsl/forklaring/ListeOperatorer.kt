package no.nav.system.rule.dsl.forklaring

// ========================================================================
// Liste-operatorer
// ========================================================================

/**
 * Sammenligning: Verdi er blant liste (in).
 */
internal data class ErBlant<T : Any>(
    val verdi: Uttrykk<T>,
    val liste: Uttrykk<List<T>>
) : Uttrykk<Boolean> {

    // Map verdi/liste til venstre/høyre for å kunne bruke felles logikk
    private val venstre: Uttrykk<T> get() = verdi
    private val høyre: Uttrykk<List<T>> get() = liste

    override fun evaluer(): Boolean = verdi.evaluer() in liste.evaluer()

    override fun notasjon(): String = "${verdi.notasjon()} ER BLANT ${liste.notasjon()}"

    override fun konkret(): String = "${verdi.konkret()} ER BLANT ${liste.konkret()}"

    override fun grunnlagListe(): List<Grunnlag<out Any>> =
        verdi.grunnlagListe() + liste.grunnlagListe()

    override fun dybde(): Int = 1 + maxOf(verdi.dybde(), liste.dybde())

    override fun strukturellHash(): String = "ErBlant:${verdi.strukturellHash()}:${liste.strukturellHash()}"
}

/**
 * Sammenligning: Verdi er ikke blant liste (not in).
 */
internal data class ErIkkeBlant<T : Any>(
    val verdi: Uttrykk<T>,
    val liste: Uttrykk<List<T>>
) : Uttrykk<Boolean> {

    // Map verdi/liste til venstre/høyre for å kunne bruke felles logikk
    private val venstre: Uttrykk<T> get() = verdi
    private val høyre: Uttrykk<List<T>> get() = liste

    override fun evaluer(): Boolean = verdi.evaluer() !in liste.evaluer()

    override fun notasjon(): String = "${verdi.notasjon()} ER IKKE BLANT ${liste.notasjon()}"

    override fun konkret(): String = "${verdi.konkret()} ER IKKE BLANT ${liste.konkret()}"

    override fun grunnlagListe(): List<Grunnlag<out Any>> =
        verdi.grunnlagListe() + liste.grunnlagListe()

    override fun dybde(): Int = 1 + maxOf(verdi.dybde(), liste.dybde())

    override fun strukturellHash(): String = "ErIkkeBlant:${verdi.strukturellHash()}:${liste.strukturellHash()}"
}
