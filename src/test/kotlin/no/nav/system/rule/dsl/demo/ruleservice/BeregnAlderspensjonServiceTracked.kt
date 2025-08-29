package no.nav.system.rule.dsl.demo.ruleservice

import no.nav.system.rule.dsl.demo.domain.*
import no.nav.system.rule.dsl.demo.domain.koder.UtfallType
import no.nav.system.rule.dsl.demo.domain.koder.YtelseEnum
import java.time.LocalDate
import kotlin.math.roundToInt

/**
 * Eksempel på beregnAlderspensjonService med lettvekts Faktum tracking
 * Bruker TrackedFaktum istedenfor original Faktum
 */
fun beregnAlderspensjonServiceTracked(request: Request): Response {
    // Start tracking session
    val sessionId = TrackedFaktum.startTracking()
    println("Starting tracking session: $sessionId")
    
    val person = TrackedFaktum("person", request.person)
    val virkningstidspunkt = TrackedFaktum("virkningstidspunkt", request.virkningstidspunkt)
    
    // Sjekk om person er flyktning
    val angittFlyktning = TrackedFaktum("person.flyktning", person.value.flyktning.value)
    
    val personErFlyktning: TrackedFaktum<UtfallType> = angittFlyktning.hvis(
        ja = {
            val kravlinjeFremsattDatoFom2021 = TrackedFaktum("Søknadstidspunkt fom 2021", true)
            kravlinjeFremsattDatoFom2021.hvis(
                ja = TrackedFaktum("Anvendt flyktning", UtfallType.OPPFYLT),
                nei = TrackedFaktum("Anvendt flyktning", UtfallType.IKKE_OPPFYLT)
            )
        },
        nei = TrackedFaktum("Anvendt flyktning", UtfallType.IKKE_RELEVANT)
    )
    
    // Beregn trygdetid
    val sumBotidMåneder = beregnSumBotidMåneder(person, virkningstidspunkt)
    
    val firefemtedelkrav = TrackedFaktum("firefemtedelskrav", 480L)
    
    val år = personErFlyktning.erLik(UtfallType.OPPFYLT).hvis(
        ja = TrackedFaktum("trygdetid.år", 40),
        nei = TrackedFaktum("trygdetid.år", (sumBotidMåneder.value / 12.0).roundToInt())
    )
    
    val trygdetid = TrackedFaktum("trygdetid", 
        Trygdetid(
            år = år.value,
            faktiskTrygdetidIMåneder = no.nav.system.rule.dsl.rettsregel.Faktum("faktisk", sumBotidMåneder.value),
            firefemtedelskrav = no.nav.system.rule.dsl.rettsregel.Faktum("firefemtedelskrav", firefemtedelkrav.value),
            redusertFremtidigTrygdetid = no.nav.system.rule.dsl.rettsregel.Faktum("redusert", UtfallType.IKKE_RELEVANT)
        )
    )
    
    // Beregn grunnpensjon
    val grunnbeløp = TrackedFaktum("grunnbeløp", 
        GrunnbeløpProvider.getGrunnbeløp(virkningstidspunkt.value)
    )
    
    val erGift = TrackedFaktum("er gift", person.value.erGift)
    val sats = erGift.hvis(
        ja = TrackedFaktum("sats", 0.90),
        nei = TrackedFaktum("sats", 1.00)
    )
    
    val grunnpensjonNetto: TrackedFaktum<Int> = år.erLik(40).hvis(
        ja = TrackedFaktum("grunnpensjon.netto", 
            (grunnbeløp.value * sats.value).roundToInt()
        ),
        nei = {
            val maksTrygdetid = 40.0
            TrackedFaktum("grunnpensjon.netto",
                (grunnbeløp.value * sats.value * år.value / maksTrygdetid).roundToInt()
            )
        }
    )
    
    val grunnpensjon = TrackedFaktum("grunnpensjon",
        Grunnpensjon(
            grunnbeløp = grunnbeløp.value,
            prosentsats = sats.value,
            netto = grunnpensjonNetto.value
        )
    )
    
    // Print execution path
    TrackedFaktum.printPath()
    
    // Eksporter til Neo4j
    val cypherQueries = TrackedFaktum.exportToNeo4j()
    println("\n=== Neo4j Cypher Queries ===")
    cypherQueries.take(3).forEach { println(it) }
    println("... ${cypherQueries.size} queries total")
    
    return Response(
        anvendtTrygdetid = trygdetid.value,
        grunnpensjon = grunnpensjon.value
    )
}

private fun beregnSumBotidMåneder(
    person: TrackedFaktum<Person>,
    virkningstidspunkt: TrackedFaktum<LocalDate>
): TrackedFaktum<Long> {
    // Forenklet beregning for eksempelet
    val dato16år = person.value.fødselsdato.value.plusYears(16)
    val månederFra16 = java.time.temporal.ChronoUnit.MONTHS.between(
        dato16år, 
        virkningstidspunkt.value
    )
    
    // Anta full botid i Norge (forenklet)
    return TrackedFaktum("sum botid måneder", månederFra16.coerceAtMost(480))
}