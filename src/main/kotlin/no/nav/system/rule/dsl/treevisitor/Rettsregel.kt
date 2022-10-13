package no.nav.system.rule.dsl.treevisitor

import no.nav.system.rule.dsl.DslDomainPredicate
import no.nav.system.rule.dsl.Predicate
import no.nav.system.rule.dsl.Rule
import no.nav.system.rule.dsl.rettsregel.Subsumsjon
import kotlin.experimental.ExperimentalTypeInference
import kotlin.reflect.KMutableProperty

open class Rettsregel(
    private val name: String,
    override val sequence: Int
) : Rule(name = name, sequence = sequence) {

    private lateinit var rettsregelTargetProperty: KMutableProperty<Rettsregel>

    /**
     * DSL: Domain Predicate entry.
     */
    @DslDomainPredicate
    @OptIn(ExperimentalTypeInference::class)
    @OverloadResolutionByLambdaReturnType
    @JvmName("SubsumsjonHVIS")
    fun HVIS(subsumsjonFunction: () -> Subsumsjon)  {
        OG(subsumsjonFunction)
    }

    /**
     * DSL: Domain Predicate entry.
     */
    @DslDomainPredicate
    @OptIn(ExperimentalTypeInference::class)
    @OverloadResolutionByLambdaReturnType
    @JvmName("SubsumsjonOG")
    fun OG(subsumsjonFunction: () -> Subsumsjon) {
        subsumsjonFunction.invoke().also {
            children.add(it)
            predicateList.add(it)
        }
    }

    /**
     * DSL: Domain Predicate entry.
     */
    @DslDomainPredicate
    @OptIn(ExperimentalTypeInference::class)
    @OverloadResolutionByLambdaReturnType
    @JvmName("RettsregelOG")
    fun OG(subsumsjonFunction: () -> Rettsregel) {
        subsumsjonFunction.invoke().also {
            children.add(it)
            predicateList.add(Predicate { it.fired })
        }
    }

    fun RESULTAT(rr: KMutableProperty<Rettsregel>) {
        rettsregelTargetProperty = rr
    }

    override fun evaluate() {
        rettsregelTargetProperty.setter.call(this)
        super.evaluate()
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
