import no.nav.system.rule.dsl.Rule

//package no.nav.system.rule.dsl.treevisitor
//
//import no.nav.system.rule.dsl.DslDomainPredicate
//import no.nav.system.rule.dsl.Predicate
//import no.nav.system.rule.dsl.Rule
//import no.nav.system.rule.dsl.rettsregel.Faktum
//import no.nav.system.rule.dsl.rettsregel.Subsumsjon
//import no.nav.system.rule.dsl.rettsregel.UTFALL
//import kotlin.experimental.ExperimentalTypeInference
//import kotlin.reflect.KMutableProperty
//
//@OptIn(ExperimentalTypeInference::class)
//open class Rettsregel(
//    private val name: String,
//    override val sequence: Int
//) : Rule(name = name, sequence = sequence) {
//
//     var utfall: String = ""
//
//    /**
//     * DSL: Domain Predicate entry.
//     */
//    @DslDomainPredicate
//    @OverloadResolutionByLambdaReturnType
//    @JvmName("SubsumsjonHVIS")
//    fun HVIS(subsumsjonFunction: () -> Subsumsjon) {
//        OG(subsumsjonFunction)
//    }
//
//    /**
//     * DSL: Domain Predicate entry.
//     */
//    @DslDomainPredicate
//    @OverloadResolutionByLambdaReturnType
//    @JvmName("SubsumsjonOG")
//    fun OG(subsumsjonFunction: () -> Subsumsjon) {
//        predicateList.add(subsumsjonFunction)
//    }
//
//    // TODO Lag en DSL entry for Faktum også. EVT Vurder om vi kan ha OG( func -> AbstractRuleComponent )
//
//    /**
//     * DSL: Domain Predicate entry.
//     */
//    @DslDomainPredicate
//    @OverloadResolutionByLambdaReturnType
//    @JvmName("RettsregelOG")
//    fun OG(rettsregelFunction: () -> Rettsregel) {
//        predicateList.add {
//            val rettsregel = rettsregelFunction.invoke()
//            this.children.add(rettsregel)
//            Predicate { rettsregel.fired }
//        }
//    }
//    /**
//     * DSL: Domain Predicate entry.
//     */
//    @DslDomainPredicate
//    @OverloadResolutionByLambdaReturnType
//    @JvmName("FaktumOG")
//    fun OG(rettsregelFunction: () -> Faktum<UTFALL>) {
//        predicateList.add {
//            val rettsregel = rettsregelFunction.invoke()
////            this.children.add(rettsregel)
//            Predicate { rettsregel.verdi == UTFALL.OPPFYLT }
//        }
//    }
//    /**
//     * DSL: Domain Predicate entry.
//     */
//    @DslDomainPredicate
//    @OverloadResolutionByLambdaReturnType
//    @JvmName("RettsregelListeOG")
//    fun OG(rettsregelFunction: () -> List<Rettsregel>) {
//        rettsregelFunction.invoke().forEach { rettsregel ->
//            this.children.add(rettsregel)
//            predicateList.add { Predicate { rettsregel.fired } }
//        }
//    }
//
//    override fun toString(): String {
//        val sb = StringBuilder()
//        sb.append("$name er evaluert: ${evaluated.svarord()} og kjørt: ${fired.svarord()}\n")
//        sb.append("Subsumsjon:\n")
//        predicateList.forEach {
//            sb.append("\t").append(it).append("\n")
//        }
//        sb.append("Regeldok: ${prettyDoc()}\n")
//        return sb.toString()
//    }
//}
//
//
fun Boolean.svarord() = if (this) "JA" else "NEI"
