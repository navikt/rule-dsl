package no.nav.system.rule.dsl.demo.ruleservice

import no.nav.system.rule.dsl.rettsregel.Faktum
import java.time.Instant
import java.util.UUID

/**
 * Lettvekts execution path tracker for Faktum
 * Sporer den faktiske stien gjennom regelverket runtime
 */
object FaktumTracker {
    
    private val executionContext = ThreadLocal.withInitial { ExecutionContext() }
    
    data class ExecutionContext(
        val sessionId: String = UUID.randomUUID().toString(),
        val startTime: Instant = Instant.now(),
        val nodes: MutableList<ExecutionNode> = mutableListOf(),
        val edges: MutableList<ExecutionEdge> = mutableListOf()
    )
    
    data class ExecutionNode(
        val id: String = UUID.randomUUID().toString(),
        val faktumNavn: String,
        val faktumVerdi: Any?,
        val faktumType: String,
        val function: String,
        val lineNumber: Int,
        val timestamp: Instant = Instant.now(),
        val branch: String? = null // "ja"/"nei" for hvis-grener
    )
    
    data class ExecutionEdge(
        val fromId: String,
        val toId: String,
        val relationship: String, // "CREATES", "USES", "EVALUATES"
        val condition: String? = null
    )
    
    /**
     * Start ny sporingsøkt
     */
    fun startSession(): String {
        val context = ExecutionContext()
        executionContext.set(context)
        return context.sessionId
    }
    
    /**
     * Spor opprettelse av nytt Faktum
     */
    fun track(faktum: Faktum<*>, sourceLocation: StackTraceElement? = null): String {
        val location = sourceLocation ?: findRelevantStackElement()
        
        val node = ExecutionNode(
            faktumNavn = faktum.name,
            faktumVerdi = faktum.value,
            faktumType = faktum.value?.javaClass?.simpleName ?: "null",
            function = location?.methodName ?: "unknown",
            lineNumber = location?.lineNumber ?: -1,
            branch = detectBranch()
        )
        
        val context = executionContext.get()
        val nodeIndex = context.nodes.size
        context.nodes.add(node)
        
        // Koble til forrige node hvis det finnes
        if (nodeIndex > 0) {
            val previousNode = context.nodes[nodeIndex - 1]
            context.edges.add(
                ExecutionEdge(
                    fromId = previousNode.id,
                    toId = node.id,
                    relationship = inferRelationship(previousNode, node),
                    condition = node.branch
                )
            )
        }
        
        return node.id
    }
    
    /**
     * Marker at et Faktum brukes som input
     */
    fun trackUsage(faktum: Faktum<*>, byFunction: String) {
        val context = executionContext.get()
        val lastNode = context.nodes.lastOrNull() ?: return
        
        // Finn node som representerer dette faktum
        val faktumNode = context.nodes.findLast { 
            it.faktumNavn == faktum.name && it.faktumVerdi == faktum.value 
        }
        
        if (faktumNode != null && faktumNode.id != lastNode.id) {
            context.edges.add(
                ExecutionEdge(
                    fromId = faktumNode.id,
                    toId = lastNode.id,
                    relationship = "USED_BY",
                    condition = null
                )
            )
        }
    }
    
    /**
     * Hent execution path for gjeldende sesjon
     */
    fun getExecutionPath(): ExecutionContext = executionContext.get()
    
    /**
     * Generer Cypher queries for Neo4j import
     */
    fun toCypherQueries(sessionId: String? = null): List<String> {
        val context = sessionId?.let { id ->
            // Hent fra cache hvis implementert
            executionContext.get()
        } ?: executionContext.get()
        
        val queries = mutableListOf<String>()
        
        // Opprett session node
        queries.add("""
            MERGE (s:Session {id: '${context.sessionId}'})
            SET s.startTime = datetime('${context.startTime}')
        """.trimIndent())
        
        // Opprett faktum nodes
        context.nodes.forEach { node ->
            queries.add("""
                CREATE (f:Faktum:Runtime {
                    id: '${node.id}',
                    navn: '${node.faktumNavn}',
                    verdi: '${node.faktumVerdi}',
                    type: '${node.faktumType}',
                    function: '${node.function}',
                    lineNumber: ${node.lineNumber},
                    timestamp: datetime('${node.timestamp}'),
                    branch: ${node.branch?.let { "'$it'" } ?: "null"},
                    sessionId: '${context.sessionId}'
                })
            """.trimIndent())
        }
        
        // Opprett edges
        context.edges.forEach { edge ->
            queries.add("""
                MATCH (from:Faktum {id: '${edge.fromId}', sessionId: '${context.sessionId}'})
                MATCH (to:Faktum {id: '${edge.toId}', sessionId: '${context.sessionId}'})
                CREATE (from)-[:${edge.relationship} {
                    ${edge.condition?.let { "condition: '$it'" } ?: ""}
                }]->(to)
            """.trimIndent())
        }
        
        // Koble til PSI-graf hvis ønskelig
        queries.add("""
            // Koble runtime faktum til statiske funksjoner fra PSI
            MATCH (f:Faktum:Runtime {sessionId: '${context.sessionId}'})
            MATCH (func:Function {name: f.function})
            CREATE (f)-[:EXECUTED_IN]->(func)
        """.trimIndent())
        
        return queries
    }
    
    /**
     * Eksporter til Neo4j-vennlig format
     */
    fun toNeo4jJson(): String {
        val context = executionContext.get()
        
        return """
        {
            "session": {
                "id": "${context.sessionId}",
                "startTime": "${context.startTime}",
                "nodeCount": ${context.nodes.size},
                "edgeCount": ${context.edges.size}
            },
            "nodes": [
                ${context.nodes.joinToString(",\n") { node ->
                    """
                    {
                        "id": "${node.id}",
                        "labels": ["Faktum", "Runtime"],
                        "properties": {
                            "navn": "${node.faktumNavn}",
                            "verdi": "${node.faktumVerdi}",
                            "type": "${node.faktumType}",
                            "function": "${node.function}",
                            "lineNumber": ${node.lineNumber},
                            "timestamp": "${node.timestamp}",
                            "branch": ${node.branch?.let { "\"$it\"" } ?: "null"}
                        }
                    }
                    """.trimIndent()
                }}
            ],
            "relationships": [
                ${context.edges.joinToString(",\n") { edge ->
                    """
                    {
                        "id": "${UUID.randomUUID()}",
                        "type": "${edge.relationship}",
                        "startNode": "${edge.fromId}",
                        "endNode": "${edge.toId}",
                        "properties": {
                            ${edge.condition?.let { "\"condition\": \"$it\"" } ?: ""}
                        }
                    }
                    """.trimIndent()
                }}
            ]
        }
        """.trimIndent()
    }
    
    /**
     * Vis execution path som tekst (for debugging)
     */
    fun printPath() {
        val context = executionContext.get()
        println("\n=== Execution Path for session ${context.sessionId} ===")
        
        context.nodes.forEachIndexed { index, node ->
            val indent = "  ".repeat(index)
            val branch = node.branch?.let { " [$it]" } ?: ""
            println("$indent${index + 1}. ${node.faktumNavn} = ${node.faktumVerdi}$branch")
            println("$indent   (${node.function}:${node.lineNumber})")
        }
        
        println("\n=== Relationships ===")
        context.edges.forEach { edge ->
            val fromNode = context.nodes.find { it.id == edge.fromId }
            val toNode = context.nodes.find { it.id == edge.toId }
            println("${fromNode?.faktumNavn} --[${edge.relationship}]--> ${toNode?.faktumNavn}")
        }
    }
    
    // Hjelpemetoder
    
    private fun findRelevantStackElement(): StackTraceElement? {
        return Thread.currentThread().stackTrace.firstOrNull { element ->
            element.className.contains("ruleservice") && 
            !element.className.contains("FaktumTracker") &&
            !element.methodName.startsWith("get")
        }
    }
    
    private fun detectBranch(): String? {
        val stack = Thread.currentThread().stackTrace
        
        // Se etter hvis-kall i stacken
        val hvisIndex = stack.indexOfFirst { it.methodName == "hvis" }
        if (hvisIndex > 0) {
            // Sjekk om vi er i ja eller nei lambda
            val callerAfterHvis = stack.getOrNull(hvisIndex - 1)
            return when {
                callerAfterHvis?.methodName?.contains("ja") == true -> "ja"
                callerAfterHvis?.methodName?.contains("nei") == true -> "nei"
                else -> null
            }
        }
        
        return null
    }
    
    private fun inferRelationship(from: ExecutionNode, to: ExecutionNode): String {
        return when {
            from.function == to.function -> "CREATES"
            from.faktumNavn in to.faktumNavn -> "DERIVES"
            else -> "FLOWS_TO"
        }
    }
    
    /**
     * Clear current session
     */
    fun clear() {
        executionContext.remove()
    }
}

/**
 * Extension function for å gjøre sporing transparent
 */
fun <T : Any> Faktum<T>.tracked(): Faktum<T> {
    FaktumTracker.track(this)
    return this
}