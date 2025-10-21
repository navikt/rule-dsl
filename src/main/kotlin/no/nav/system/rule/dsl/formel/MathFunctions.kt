package no.nav.system.rule.dsl.formel

import redempt.crunch.functional.EvaluationEnvironment

val evalEnv = EvaluationEnvironment().apply {
    addFunction("avrund", 1) { args -> Math.round(args[0]).toDouble() }
    addFunction("avrundMedToDesimal", 1) { args -> Math.round(args[0] * 100.0) / 100.0 }
    /**
     * Legacy funksjon som vi fremdeles må støtte.
     */
    addFunction("avrund2Desimal", 1) { args -> Math.round(args[0] * 100.0) / 100.0 }
    addFunction("afpAvrundBrutto", 1) { args ->
        val årsbeløp = Math.floor((args[0] + 0.005) * 100) / 100
        Math.floor(årsbeløp / 12 + 0.5)
    }
    addFunction("afpAvrundNetto", 2) { args ->
        val årsbeløp = Math.floor((args[0] + 0.005) * 100) / 100
        Math.floor(årsbeløp / 12 * args[1] / 100 + 0.5)
    }
    addFunction("max", 2) { args -> Math.max(args[0], args[1]); }
    addFunction("min", 2) { args -> Math.min(args[0], args[1]); }
}