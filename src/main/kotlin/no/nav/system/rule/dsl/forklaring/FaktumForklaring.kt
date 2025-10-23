package no.nav.system.rule.dsl.forklaring

import no.nav.system.rule.dsl.rettsregel.DomainPredicate
import no.nav.system.rule.dsl.rettsregel.Faktum
import no.nav.system.rule.dsl.rettsregel.ListDomainPredicate
import no.nav.system.rule.dsl.rettsregel.PairDomainPredicate

/**
 * Extension functions for å generere forklaringer fra Faktum og Subsumsjoner.
 *
 * ## Eksempel
 * ```kotlin
 * val antallMåneder = Faktum("antallMånederEtterNedreAldersgrense", 24)
 * val grense = Faktum("MND_36", 36)
 *
 * val subsumtion = antallMåneder erMindreEnn grense
 *
 * // Generer forklaring
 * val forklaring = subsumtion.forklar()
 *
 * // Output:
 * // FORDI
 * //     antallMånederEtterNedreAldersgrense er mindre enn MND_36
 * //     24 er mindre enn 36
 * //
 * //     FORDI
 * //         antallMånederEtterNedreAldersgrense = 24
 * //         MND_36 = 36
 * ```
 */

/**
 * Genererer "HVA" forklaring for et Faktum - viser navn og verdi.
 */
fun <T : Any> Faktum<T>.forklarHva(nivå: Int = 0): HvaForklaring {
    return HvaForklaring(
        navn = this.name,
        symbolskUttrykk = this.value.toString(),
        konkretUttrykk = this.value.toString(),
        resultat = this.value,
        nivå = nivå
    )
}

/**
 * Genererer en enkel tekstforklaring av et faktum.
 */
fun <T : Any> Faktum<T>.forklar(): String {
    return if (this.anonymous) {
        "'${this.value}'"
    } else {
        "'${this.name}' (${this.value})"
    }
}

/**
 * Genererer "FORDI" forklaring for et domainPredicate.
 * Viser hvorfor en betingelse er oppfylt eller ikke.
 */
fun DomainPredicate.forklar(): HvorforForklaring {
    return when (this) {
        is PairDomainPredicate -> this.forklar()
        is ListDomainPredicate -> this.forklar()
        else -> HvorforForklaring(emptyList())
    }
}

/**
 * Genererer forklaring for et pair domain predicate (sammenligning av to verdier).
 *
 * ## Eksempel output:
 * ```
 * FORDI
 *     antallMånederEtterNedreAldersgrense er mindre enn MND_36
 *     24 er mindre enn 36
 *
 *     FORDI
 *         antallMånederEtterNedreAldersgrense = 24
 *         MND_36 = 36
 * ```
 */
fun PairDomainPredicate.forklar(nivå: Int = 0): HvorforForklaring {
    val komparatorText = if (this.fired) this.comparator.text else this.comparator.negated()

    // Parse toString() output to extract better formatted values
    val fullText = this.toString()
    val svarord = if (fullText.startsWith("JA ")) "JA " else if (fullText.startsWith("NEI ")) "NEI " else ""
    val cleanText = fullText.removePrefix(svarord).trim()

    // Try to extract value names and concrete values
    // Format is typically: 'name1' (value1) <comparator> 'name2' (value2)
    val parts = cleanText.split(komparatorText)
    val verdi1Str = if (parts.isNotEmpty()) parts[0].trim() else cleanText
    val verdi2Str = if (parts.size > 1) parts[1].trim() else ""

    val hovedSubsumtion = SubsumsjonForklaring(
        beskrivelse = cleanText,
        verdi1 = verdi1Str,
        komparator = komparatorText,
        verdi2 = verdi2Str,
        oppfylt = this.fired
    )

    // TODO: I fremtiden kan vi utvide dette til å inkludere underliggende faktum
    // hvis vi lagrer referanser til Verdi-objektene i PairDomainPredicate

    return HvorforForklaring(
        subsumsjoner = listOf(hovedSubsumtion),
        underliggende = emptyList(),
        nivå = nivå
    )
}

/**
 * Genererer forklaring for et list domain predicate (sammenligning med liste).
 *
 * ## Eksempel output:
 * ```
 * FORDI
 *     sivilstand er blant [GIFT, SAMBOER]
 *     GIFT er blant [GIFT, SAMBOER]
 *
 *     FORDI
 *         sivilstand = GIFT
 * ```
 */
fun ListDomainPredicate.forklar(nivå: Int = 0): HvorforForklaring {
    val komparatorText = if (this.fired) this.comparator.text else this.comparator.negated()

    // Parse toString() output for å hente informasjon
    val fullText = this.toString()
    val faktumStr = fullText
        .substringAfter("NEI ")
        .substringAfter("JA ")
        .substringBefore(" er ")
        .substringBefore(komparatorText)
        .trim()

    val listeStr = fullText
        .substringAfter(komparatorText)
        .trim()

    val hovedSubsumtion = SubsumsjonForklaring(
        beskrivelse = fullText,
        verdi1 = faktumStr,
        komparator = komparatorText,
        verdi2 = listeStr,
        oppfylt = this.fired
    )

    return HvorforForklaring(
        subsumsjoner = listOf(hovedSubsumtion),
        underliggende = emptyList(),
        nivå = nivå
    )
}

/**
 * Kombinerer flere domain predicates til en komplett "FORDI" forklaring.
 *
 * ## Eksempel
 * ```kotlin
 * val betingelser = listOf(
 *     alder erStørreEnn 62,
 *     trygdetid erMindreEnn 40
 * )
 *
 * val forklaring = betingelser.forklarFordi()
 * // Output:
 * // FORDI
 * //     alder er større enn 62
 * //     70 er større enn 62
 * //
 * //     trygdetid er mindre enn 40
 * //     35 er mindre enn 40
 * ```
 */
fun List<DomainPredicate>.forklarFordi(nivå: Int = 0): HvorforForklaring {
    val alleSubsumsjonForklaringer = this.flatMap { betingelse ->
        when (betingelse) {
            is PairDomainPredicate -> betingelse.forklar(nivå).subsumsjoner
            is ListDomainPredicate -> betingelse.forklar(nivå).subsumsjoner
            else -> emptyList()
        }
    }

    return HvorforForklaring(
        subsumsjoner = alleSubsumsjonForklaringer,
        underliggende = emptyList(),
        nivå = nivå
    )
}

/**
 * Genererer en kompakt oversikt over alle domain predicates.
 */
fun List<DomainPredicate>.forklarKompakt(): String {
    return this.joinToString(separator = "\n") { it.toString() }
}
