package no.nav.system.rule.dsl.treevisitor

class Conclusion(
    /**
     * True if rule has been evaluated.
     */
    var evaluated: Boolean = false,
    /**
     * True if rule has been fired.
     */
    val fired: Boolean = false,
    /**
     * Name of the rule
     */
    var ruleName: String = "Forespørsel treffer ingen relevant regel.",
    /**
     * Rule documentation
     */
    var documentation: String = "",
    /**
     * Reasons why the rule fired or not.
     */
    val reasons: List<String> = listOf()
) {
    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("$ruleName er evaluert: ${evaluated.svarord()} og kjørt: ${fired.svarord()}\n")
        sb.append("Predikat:\n")
        reasons.forEach {
            sb.append("\t").append(it.trim()).append("\n")
        }
        sb.append("Regeldok: $documentation\n")
        return sb.toString()
    }
}

fun Boolean.svarord() = if (this) "JA" else "NEI"
