package no.nav.system.rule.dsl.demo.ruleservice

import no.nav.system.rule.dsl.demo.domain.*
import org.junit.jupiter.api.Test
import java.time.LocalDate

class FaktumTrackerTest {
    
    @Test
    fun `test faktum tracking med Neo4j eksport`() {
        // Arrange
        val person = Person(
            fødselsdato = no.nav.system.rule.dsl.rettsregel.Faktum("fødselsdato", LocalDate.of(1955, 1, 1)),
            erGift = false,
            flyktning = no.nav.system.rule.dsl.rettsregel.Faktum("flyktning", false),
            forsteVirkningsdatoGrunnlagListe = mutableListOf(),
            trygdetidK19 = Trygdetid(
                år = 40,
                faktiskTrygdetidIMåneder = no.nav.system.rule.dsl.rettsregel.Faktum("faktisk", 480L),
                firefemtedelskrav = no.nav.system.rule.dsl.rettsregel.Faktum("firefemtedelskrav", 480L),
                redusertFremtidigTrygdetid = no.nav.system.rule.dsl.rettsregel.Faktum("redusert", 
                    no.nav.system.rule.dsl.demo.domain.koder.UtfallType.IKKE_RELEVANT)
            ),
            trygdetidK20 = Trygdetid(
                år = 40,
                faktiskTrygdetidIMåneder = no.nav.system.rule.dsl.rettsregel.Faktum("faktisk", 480L),
                firefemtedelskrav = no.nav.system.rule.dsl.rettsregel.Faktum("firefemtedelskrav", 480L),
                redusertFremtidigTrygdetid = no.nav.system.rule.dsl.rettsregel.Faktum("redusert", 
                    no.nav.system.rule.dsl.demo.domain.koder.UtfallType.IKKE_RELEVANT)
            ),
            inngangOgEksportgrunnlag = null
        )
        
        val request = Request(
            virkningstidspunkt = LocalDate.of(2023, 1, 1),
            person = person
        )
        
        // Act
        val response = beregnAlderspensjonServiceTracked(request)
        
        // Assert
        assert(response.grunnpensjon != null)
        println("\n=== Response ===")
        println("Trygdetid: ${response.anvendtTrygdetid?.år} år")
        println("Grunnpensjon: ${response.grunnpensjon?.netto} kr")
        
        // Vis execution path
        val executionPath = TrackedFaktum.getExecutionPath()
        println("\n=== Execution Summary ===")
        println("Session: ${executionPath.sessionId}")
        println("Nodes created: ${executionPath.nodes.size}")
        println("Edges created: ${executionPath.edges.size}")
        
        // Vis Neo4j JSON for import
        val neo4jJson = FaktumTracker.toNeo4jJson()
        println("\n=== Neo4j JSON (første 500 tegn) ===")
        println(neo4jJson.take(500))
        
        // Generer Cypher queries
        val queries = TrackedFaktum.exportToNeo4j()
        
        println("\n=== Sample Cypher Queries for Neo4j ===")
        println("-- Opprett session node:")
        println(queries.first())
        
        println("\n-- Opprett faktum nodes (første 2):")
        queries.drop(1).take(2).forEach { println(it) }
        
        println("\n-- For å importere til Neo4j:")
        println("1. Kjør disse queries i Neo4j Browser eller cypher-shell")
        println("2. Åpne Bloom og søk på: Session {id: '${executionPath.sessionId}'}")
        println("3. Ekspander for å se execution path")
        
        // Lag en fil med alle queries for enkel import
        val allQueries = queries.joinToString("\n\n")
        println("\n-- Alle queries kan kjøres med:")
        println("cat execution_path.cypher | cypher-shell -u neo4j -p password")
        
        // Clean up
        TrackedFaktum.clearTracking()
    }
    
    @Test
    fun `test branch detection i hvis-statements`() {
        TrackedFaktum.startTracking()
        
        val erGift = TrackedFaktum("er gift", true)
        
        val sats = erGift.hvis(
            ja = TrackedFaktum("sats gift", 0.90),
            nei = TrackedFaktum("sats ugift", 1.00)
        )
        
        println("\n=== Branch Detection Test ===")
        TrackedFaktum.printPath()
        
        val path = TrackedFaktum.getExecutionPath()
        
        // Sjekk at vi tok "ja" grenen (sats gift skulle være opprettet)
        val satsNode = path.nodes.find { it.faktumNavn == "sats gift" }
        assert(satsNode != null) { "Should have created 'sats gift' node" }
        assert(satsNode?.faktumVerdi == 0.90) { "Sats should be 0.90" }
        
        TrackedFaktum.clearTracking()
    }
    
    @Test
    fun `test integration med PSI graf i Neo4j`() {
        val sessionId = TrackedFaktum.startTracking()
        
        // Simuler en enkel beregning
        val grunnbeløp = TrackedFaktum("grunnbeløp", 130000)
        val sats = TrackedFaktum("sats", 1.0)
        val resultat = TrackedFaktum("resultat", (grunnbeløp.value * sats.value).toInt())
        
        val queries = TrackedFaktum.exportToNeo4j()
        
        println("\n=== Integration with PSI Graph ===")
        println("Denne runtime execution kan kobles til din eksisterende PSI graf:")
        println("""
            // Etter import av runtime execution:
            MATCH (runtime:Faktum:Runtime {sessionId: '$sessionId'})
            MATCH (psi:Function {name: runtime.function})
            CREATE (runtime)-[:EXECUTED_IN]->(psi)
            
            // Vis kombinert graf i Bloom:
            MATCH path = (s:Session {id: '$sessionId'})-[*]-(f:Faktum:Runtime)
            MATCH (f)-[:EXECUTED_IN]->(func:Function)
            MATCH (func)-[:CALLS|CREATES|USES*..3]-(related)
            RETURN path, func, related
        """.trimIndent())
        
        TrackedFaktum.clearTracking()
    }
}