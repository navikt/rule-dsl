package no.nav.system.rule.dsl.forklaring

object CallTracker {
    private val callStack = ThreadLocal<MutableList<CallInfo>>()

    data class CallInfo(
        val functionName: String,
        val depth: Int,
        var returnType: String? = null
    )

    fun getCurrentStack(): List<CallInfo> = callStack.get() ?: emptyList()

    fun getDepth(): Int = callStack.get()?.size ?: 0

    fun push(name: String): Int {
        val stack = callStack.get() ?: mutableListOf<CallInfo>().also { callStack.set(it) }
        val depth = stack.size
        stack.add(CallInfo(name, depth = depth))
        return depth
    }

    // FJERN pop() funksjonen helt!

    fun setReturnType(depth: Int, returnType: String) {
        callStack.get()?.getOrNull(depth)?.returnType = returnType
    }

    fun printTrace() {
        val stack = getCurrentStack()
        if (stack.isEmpty()) {
            println("\n=== CALL TRACE (empty) ===\n")
            return
        }

        println("\n=== CALL TRACE ===")
        stack.forEach { info ->
            val indent = "  ".repeat(info.depth)
            val typeInfo = info.returnType ?: "void"
            println("$indent→ ${info.functionName} → $typeInfo")
        }
        println("==================\n")
    }

    fun clear() {
        callStack.remove()
    }
}

fun classifyType(result: Uttrykk<*>): String {
    return when (result) {
        is Grunnlag<*> -> "Grunnlag"
        is Const<*> -> "Const"
        else -> "Uttrykk"
    }
}

inline fun <reified R : Uttrykk<*>> tracked(
    name: String = "",
    noinline block: () -> R
): R {
    val functionName = name.ifEmpty {
        Throwable().stackTrace
            .dropWhile { it.methodName == "tracked" }
            .firstOrNull { frame ->
                frame.methodName != "invoke" &&
                        frame.methodName != "invokeWithArguments" &&
                        !frame.className.startsWith("java.lang") &&
                        !frame.className.contains("MethodHandle") &&
                        frame.className.startsWith("no.nav.system")
            }
            ?.let { frame ->
                "${frame.className.substringAfterLast('.').substringBefore('$')}.${frame.methodName}"
            }
            ?: "unknown"
    }

    val myDepth = CallTracker.push(functionName)

    return try {
        val result = block()
        // Sett returntype på riktig depth-nivå
        CallTracker.setReturnType(myDepth, classifyType(result))

        // Sett funksjonsnavn på resultatet
        when (result) {
            is Grunnlag<*> -> result.funksjon = functionName
            is Const<*> -> result.funksjon = functionName
            else -> {
                throw IllegalStateException("Kallsporing: uventet resultat og kan ikke sette funksjonsnavn")
            }
        }

        result
    } catch (e: Exception) {
        CallTracker.setReturnType(myDepth, "Exception: ${e::class.simpleName}")
        throw e
    }
}