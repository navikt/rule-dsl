package concept


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


data class Bruker(val name: String, val age: Int, val trygdetid: Int, val limitOptions: Int?)

fun main() {
    val sliterROOT = TraceNode("sliterordning")
    val sliterRootContext = TraceContext(sliterROOT)

    with(sliterRootContext) {
        val tillegg = sliterordning(Bruker("Nola", 65, 24, null))
        println("tillegg: $tillegg")
    }
    sliterROOT.print()
}

context(tc: TraceContext)
fun sliterordning(bruker: Bruker): Int = traced("sliterordning") {
    val innvilget = vilkår(bruker)

    emptyFuncton(bruker)

    if (innvilget)
        beregnSlitertillegg(bruker)
    else
        0
}

context(tc: TraceContext)
fun emptyFuncton(bruker: Bruker) = traced<Unit>("emptyFuncton") {
    // Emptyness
}

context(tc: TraceContext)
fun vilkår(bruker: Bruker): Boolean = traced<Boolean>("vilkår") {
    return bruker.age > 62
}

context(tc: TraceContext)
fun beregnSlitertillegg(bruker: Bruker): Int = traced<Int>("beregnSlitertillegg") {
    return 4000 * bruker.trygdetid / 40
}
