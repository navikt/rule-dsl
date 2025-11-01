import no.nav.system.rule.dsl.forklaring.Uttrykk

// CallTracker.kt
object CallTracker {
    private val callStack = ThreadLocal<MutableList<CallInfo>>()

    data class CallInfo(
        val functionName: String,
        val timestamp: Long = System.currentTimeMillis(),
        val depth: Int
    )

    fun getCurrentStack(): List<CallInfo> = callStack.get() ?: emptyList()

    fun getDepth(): Int = callStack.get()?.size ?: 0

    fun push(name: String) {
        val stack = callStack.get() ?: mutableListOf<CallInfo>().also { callStack.set(it) }
        stack.add(CallInfo(name, depth = stack.size))
    }

    fun pop(): CallInfo? {
        val stack = callStack.get() ?: return null
        val info = stack.removeLastOrNull()
        if (stack.isEmpty()) {
            callStack.remove()
        }
        return info
    }

    fun printCallTree() {
        getCurrentStack().forEach { info ->
            println("${"  ".repeat(info.depth)}→ ${info.functionName}")
        }
    }
}

// Hovedfunksjonen
inline fun <reified T : Any> tracked(
    name: String = "",
    noinline block: () -> Uttrykk<T>
): Uttrykk<T> {
    val functionName = name.ifEmpty {
        // Hent faktisk funksjonsnavn fra stack trace
        Thread.currentThread().stackTrace
            .firstOrNull { it.methodName != "tracked" && !it.methodName.contains("$") }
            ?.let { "${it.className.substringAfterLast('.')}.${it.methodName}" }
            ?: "unknown"
    }

    val indent = "  ".repeat(CallTracker.getDepth())
    println("${indent}→ $functionName")

    CallTracker.push(functionName)

    return try {
        val result = block()
        val resultType = result::class.simpleName
        println("${indent}← $functionName returned $resultType")
        result
    } catch (e: Exception) {
        println("${indent}✗ $functionName threw ${e::class.simpleName}")
        throw e
    } finally {
        CallTracker.pop()
    }
}

// Variant for å tracke uten å endre returtype (for void/Unit funksjoner)
inline fun <T> trackedCall(
    name: String = "",
    block: () -> T
): T {
    val functionName = name.ifEmpty {
        Thread.currentThread().stackTrace
            .firstOrNull { it.methodName != "trackedCall" && !it.methodName.contains("$") }
            ?.let { "${it.className.substringAfterLast('.')}.${it.methodName}" }
            ?: "unknown"
    }

    val indent = "  ".repeat(CallTracker.getDepth())
    println("${indent}→ $functionName")

    CallTracker.push(functionName)

    return try {
        block().also {
            println("${indent}← $functionName completed")
        }
    } catch (e: Exception) {
        println("${indent}✗ $functionName threw ${e::class.simpleName}")
        throw e
    } finally {
        CallTracker.pop()
    }
}