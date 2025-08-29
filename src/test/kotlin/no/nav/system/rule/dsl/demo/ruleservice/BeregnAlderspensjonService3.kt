package no.nav.system.rule.dsl.demo.ruleservice

import no.nav.system.rule.dsl.AbstractRuleComponent
import no.nav.system.rule.dsl.demo.domain.*
import no.nav.system.rule.dsl.demo.domain.koder.LandEnum
import no.nav.system.rule.dsl.demo.domain.koder.UnntakEnum
import no.nav.system.rule.dsl.demo.domain.koder.UnntakEnum.*
import no.nav.system.rule.dsl.demo.domain.koder.UtfallType
import no.nav.system.rule.dsl.demo.domain.koder.YtelseEnum
import no.nav.system.rule.dsl.demo.helper.localDate
import no.nav.system.rule.dsl.enums.PairComparator
import no.nav.system.rule.dsl.enums.RuleComponentType
import no.nav.system.rule.dsl.inspections.debug
import no.nav.system.rule.dsl.rettsregel.*
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

/**
 * Wrapper-klasse som bygger regelgraf ved å koble sammen Faktum og Subsumtion objekter
 */
class RegelGrafNode<T : Any>(
    val navn: String,
    val faktum: Faktum<T>,
    vararg dependencies: AbstractRuleComponent
) : AbstractRuleComponent() {
    init {
        // Koble avhengigheter til denne noden
        dependencies.forEach { dep ->
            this.children.add(dep)
        }
    }
    
    override fun name() = navn
    override fun type() = faktum.type()
    override fun fired() = true
    override fun toString() = "$navn"
    
    fun verdi(): T = faktum.value
}

/**
 * Hjelpefunksjoner for å bygge regelgraf med funksjoner
 */
fun <T : Any> faktumNode(navn: String, verdi: T, vararg deps: AbstractRuleComponent): RegelGrafNode<T> {
    return RegelGrafNode(navn, Faktum(navn, verdi), *deps)
}

fun <T : Any> beregnetFaktum(navn: String, beregning: () -> Pair<T, Array<AbstractRuleComponent>>): RegelGrafNode<T> {
    val (verdi, deps) = beregning()
    return RegelGrafNode(navn, Faktum(navn, verdi), *deps)
}

/**
 * Logiske operatorer som bygger subsumsjoner
 */
fun eller(vararg komponenter: AbstractSubsumtion): AbstractSubsumtion {
    val resultat = komponenter.any { it.fired }
    return object : AbstractSubsumtion(
        comparator = PairComparator.ULIK, // Bruker ULIK som placeholder, siden ELLER ikke finnes
        function = { resultat }
    ) {
        init {
            this.children.addAll(komponenter)
        }
        
        override fun type() = RuleComponentType.PAR_SUBSUMSJON
        override fun toString() = "${if (fired) "JA" else "NEI"} (${komponenter.joinToString(" eller ")})"
    }
}

fun og(vararg komponenter: AbstractSubsumtion): AbstractSubsumtion {
    val resultat = komponenter.all { it.fired }
    return object : AbstractSubsumtion(
        comparator = PairComparator.LIK, // Bruker LIK som placeholder, siden OG ikke finnes
        function = { resultat }
    ) {
        init {
            this.children.addAll(komponenter)
        }
        
        override fun type() = RuleComponentType.PAR_SUBSUMSJON
        override fun toString() = "${if (fired) "JA" else "NEI"} (${komponenter.joinToString(" og ")})"
    }
}

/**
 * Service implementasjon med regelgraf
 */
class BeregnAlderspensjonService3(
    private val request: Request,
) : AbstractDemoRuleService<Response>() {
    
    override val ruleService: () -> Response = {
        // Basis faktum
        val person = faktumNode("person", request.person)
        val virkningstidspunkt = faktumNode("virkningstidspunkt", request.virkningstidspunkt)
        val fødselsdato = faktumNode("fødselsdato", request.person.fødselsdato.value, person)
        
        // Beregn botid i Norge med sporing
        val sumBotidNorge = beregnetFaktum("sum botid Norge måneder") {
            val dato16år = localDate(fødselsdato.verdi().year + 16, fødselsdato.verdi().monthValue, fødselsdato.verdi().dayOfMonth)
            val boperioder = request.person.boperioder.filter { it.land == LandEnum.NOR }
            
            val måneder = boperioder.sumOf { boperiode ->
                if (boperiode.fom < dato16år) {
                    ChronoUnit.MONTHS.between(dato16år, boperiode.tom)
                } else {
                    ChronoUnit.MONTHS.between(boperiode.fom, boperiode.tom)
                }
            }
            
            Pair(måneder, arrayOf(person))
        }
        
        // Faktum for grenser
        val firefemtedelskrav = faktumNode("firefemtedelskrav", 480L)
        val dato1991 = faktumNode("dato 1991", localDate(1991, 1, 1))
        
        // Subsumsjoner med eksisterende operatorer
        val virkningstidspunktEtter1991 = virkningstidspunkt.faktum erEtterEllerLik dato1991.faktum
        val botidUnder480 = sumBotidNorge.faktum erMindreEnn firefemtedelskrav.faktum
        
        // Kombinert subsumsjon
        val skalHaRedusertFremtidigTrygdetid = og(virkningstidspunktEtter1991, botidUnder480)
        
        // Beregn trygdetid med avhengigheter
        val trygdetid = beregnetFaktum("trygdetid") {
            val redusertFremtidig = if (skalHaRedusertFremtidigTrygdetid.fired) {
                Faktum("redusert fremtidig trygdetid", UtfallType.OPPFYLT)
            } else {
                Faktum("ikke redusert fremtidig trygdetid", UtfallType.IKKE_OPPFYLT)
            }
            
            val trygdetidObj = Trygdetid(
                faktiskTrygdetidIMåneder = sumBotidNorge.faktum,
                firefemtedelskrav = firefemtedelskrav.faktum,
                redusertFremtidigTrygdetid = redusertFremtidig,
                år = (sumBotidNorge.verdi() / 12.0).roundToInt()
            )
            
            Pair(trygdetidObj, arrayOf(sumBotidNorge, firefemtedelskrav, skalHaRedusertFremtidigTrygdetid))
        }
        
        // Sats basert på sivilstatus
        val erGift = faktumNode("er gift", request.person.erGift, person)
        val sats = beregnetFaktum("sats") {
            val satsverdi = if (erGift.verdi()) 0.90 else 1.00
            Pair(satsverdi, arrayOf(erGift))
        }
        
        // Grunnbeløp
        val grunnbeløp = faktumNode("grunnbeløp", grunnbeløpByDate(virkningstidspunkt.verdi()))
        
        // Beregn grunnpensjon
        val grunnpensjon = beregnetFaktum("grunnpensjon") {
            val år = trygdetid.verdi().år
            val grunnpensjonObj = Grunnpensjon(
                grunnbeløp = grunnbeløp.verdi(),
                prosentsats = sats.verdi(),
                netto = if (år == 40) {
                    (grunnbeløp.verdi() * sats.verdi()).roundToInt()
                } else {
                    (grunnbeløp.verdi() * sats.verdi() * år / 40.0).roundToInt()
                }
            )
            Pair(grunnpensjonObj, arrayOf(grunnbeløp, sats, trygdetid))
        }
        
        // Debug regelgrafen
        println("\n=== REGELGRAF ===")
        println(grunnpensjon.debug())
        println("================\n")
        
        Response(
            anvendtTrygdetid = trygdetid.verdi(),
            grunnpensjon = grunnpensjon.verdi()
        )
    }
}