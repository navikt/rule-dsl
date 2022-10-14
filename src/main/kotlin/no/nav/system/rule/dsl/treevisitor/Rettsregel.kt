package no.nav.system.rule.dsl.treevisitor

import no.nav.system.rule.dsl.DslDomainPredicate
import no.nav.system.rule.dsl.Predicate
import no.nav.system.rule.dsl.Rule
import no.nav.system.rule.dsl.rettsregel.Subsumsjon
import kotlin.experimental.ExperimentalTypeInference
import kotlin.reflect.KMutableProperty

@OptIn(ExperimentalTypeInference::class)
open class Rettsregel(
    private val name: String,
    override val sequence: Int
) : Rule(name = name, sequence = sequence) {

    private lateinit var rettsregelTargetProperty: KMutableProperty<Rettsregel>
    private lateinit var rettsregel: Rettsregel
    /**
     * DSL: Domain Predicate entry.
     */
    @DslDomainPredicate
    @OverloadResolutionByLambdaReturnType
    @JvmName("SubsumsjonHVIS")
    fun HVIS(subsumsjonFunction: () -> Subsumsjon)  {
        OG(subsumsjonFunction)
    }

    /**
     * DSL: Domain Predicate entry.
     */
    @DslDomainPredicate
    @OverloadResolutionByLambdaReturnType
    @JvmName("SubsumsjonOG")
    fun OG(subsumsjonFunction: () -> Subsumsjon) {
        predicateList.add(subsumsjonFunction)
    }

    /**
     * DSL: Domain Predicate entry.
     */
    @DslDomainPredicate
    @OverloadResolutionByLambdaReturnType
    @JvmName("RettsregelOG")
    fun OG(subsumsjonFunction: () -> Rettsregel) {
        predicateList.add { Predicate { subsumsjonFunction.invoke().fired } }
    }

//    fun RESULTATx(rr: KMutableProperty<Rettsregel>) {
//        rettsregelTargetProperty = rr
//    }
    fun RESULTAT(rr: Rettsregel) {
        rettsregel = rr
    }

    override fun evaluate() {
        rettsregel.become(this)
        super.evaluate()
    }

    private fun become(rr: Rettsregel) {
//        this.name = rr.name
//        this.sequence = rr.sequence
//        this.comment = rr.comment
//        this.predicateList.clear(); this.predicateList.
//        this.pattern
//        this.patternOffset
//        this.evaluated = false
//        this.fired = false
//        this.actionStatement
//        this.returnValue
//        this.returnRule
    }

    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("$name er evaluert: ${evaluated.svarord()} og kjørt: ${fired.svarord()}\n")
        sb.append("Subsumsjon:\n")
        predicateList.forEach {
            sb.append("\t").append(it).append("\n")
        }
        sb.append("Regeldok: ${prettyDoc()}\n")
        return sb.toString()
    }
}

class TomRettsregel : Rettsregel(name = "Tom rettsregel.", sequence = 0)

fun Boolean.svarord() = if (this) "JA" else "NEI"
