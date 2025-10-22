package no.nav.system.rule.dsl.formel

import kotlin.reflect.KFunction1


fun Formel<*>.toTreeString(level: Int, maxLevel: Int): String {
    val s = StringBuilder()
    s.append(" ".repeat(level * 2)).append("Formelnavn: ").append(name).append("  level: ")
        .append(level).append("  resultat: ").append(resultat()).append("  locked: ").append(locked)
        .append("  ant.subFormler: ").append(subFormelList.size).append("  hash: ").append(this.hashCode())
        .append("\n")
    s.append(" ".repeat(level * 2)).append("    notasjon:\t\t").append(notasjon).append("\n")
    s.append(" ".repeat(level * 2)).append("    innhold: \t\t").append(innhold)
        .append(" = ${resultat()}").append("\n")
    s.append(" ".repeat(level * 2)).append("    namedVarMap:  \t").append(namedVarMap.toString())
        .append("\n")
    s.append(" ".repeat(level * 2)).append("    subFormelList:\t")
        .append(subFormelList.map { it.emne }).append("\n")
    if (level < maxLevel) {
        subFormelList.forEach {
            s.append(it.toTreeString(level + 1, maxLevel))
        }
    }
    return s.toString()
}

fun Formel<*>.toHTML(): String = toTreeHTML(0, Int.MAX_VALUE)
fun Formel<*>.toHTML(maxLevel: Int): String = toTreeHTML(0, maxLevel)

/**
 * Log formelen med en funksjon tilpasset domenet.
 */
fun <T : Number> Formel<T>.logUsing(kFunction1: KFunction1<String?, Unit>): Formel<T> {
    this.toHTML(0).split("\n").forEach { kFunction1.invoke(it) }
    return this
}

private fun Formel<*>.toTreeHTML(level: Int, maxLevel: Int): String {
    val sb = StringBuilder()

    sb.append(" ".repeat(level * 2)).append("<formel navn='").append(name).append("'")
    sb.append(" level='").append(level).append("'").append(" resultat='").append(resultat()).append("'")
        .append(" locked='").append(locked).append("'").append(" antSubFormler='").append(subFormelList.size)
        .append("'>\n")
    sb.append(" ".repeat(level * 2 + 2)).append("<fl>").append(emne).append(" = ").append(notasjon)
        .append("</fl>\n")
    sb.append(" ".repeat(level * 2 + 2)).append("<fl>").append(emne).append(" = ").append(innhold)
        .append("</fl>\n")
    sb.append(" ".repeat(level * 2 + 2)).append("<fl>").append(emne).append(" = ").append(resultat())
        .append("</fl>\n")

    if (level < maxLevel) {
        subFormelList.forEach {
            sb.append(it.toTreeHTML(level + 1, maxLevel))
        }
    }
    sb.append(" ".repeat(level * 2)).append("</formel>\n")
    return sb.toString()
}