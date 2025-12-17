package concept

import no.nav.system.ruledsl.core.trace.User

class TraceNode(
    val name: String,
    val children: MutableList<TraceNode> = mutableListOf()
) {
    fun print(indent: String = "") {
        println("$indent$name")
        children.forEach { it.print("$indent  ") }
    }
}

class TraceContext(
    val current: TraceNode
) {
    fun enter(name: String): TraceContext {
        val child = TraceNode(name)
        current.children += child
        return TraceContext(child)
    }
}

context(tc: TraceContext)
inline fun <T> traced(
    name: String,
    block: context(TraceContext) () -> T
): T {
    val next = tc.enter(name)
    with(next) { // Intellij warning: with is unused. Am I missing something?
        return block()
    }
}

fun main() {
    val sliterROOT = TraceNode("sliterordning")
    val sliterRootContext = TraceContext(sliterROOT)

    with(sliterRootContext) {
        val tillegg = sliterordning(User("Nola", 65, 24, null))
        println("tillegg: $tillegg")
    }
    sliterROOT.print()
}

context(tc: TraceContext)
fun sliterordning(user: User): Int = traced("sliterordning") {
    val innvilget = vilkår(user)

    emptyFuncton(user)

    if (innvilget)
        beregnSlitertillegg(user)
    else
        0
}

context(tc: TraceContext)
fun emptyFuncton(user: User) = traced<Unit>("emptyFuncton") {
    // Emptyness
}

context(tc: TraceContext)
fun vilkår(user: User): Boolean = traced<Boolean>("vilkår") {
    return user.age > 62
}

context(tc: TraceContext)
fun beregnSlitertillegg(user: User): Int = traced<Int>("beregnSlitertillegg") {
    return 4000 * user.trygdetid / 40
}
