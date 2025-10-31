package no.nav.system.rule.dsl.demo.forklaring

import no.nav.system.rule.dsl.demo.domain.koder.UtfallType
import no.nav.system.rule.dsl.demo.domain.koder.UtfallType.*
import no.nav.system.rule.dsl.forklaring.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TabellTest {

    @Test
    fun `tabell med enkel matching regel`() {
        val betingelse = Const(true)
        val resultat = tabell<String> {
            regel {
                når { betingelse }
                resultat { Const("MATCH") }
            }
        }

        assertEquals("MATCH", resultat.evaluer())
    }

    @Test
    fun `tabell matcher første regel i rekkefølge`() {
        val betingelse1 = Const(true)
        val betingelse2 = Const(true)

        val resultat = tabell<String> {
            regel {
                når { betingelse1 }
                resultat { Const("FØRSTE") }
            }
            regel {
                når { betingelse2 }
                resultat { Const("ANDRE") }
            }
        }

        assertEquals("FØRSTE", resultat.evaluer())
    }

    @Test
    fun `tabell hopper over ikke-matchende regler`() {
        val betingelse1 = Const(false)
        val betingelse2 = Const(true)
        val betingelse3 = Const(true)

        val resultat = tabell<String> {
            regel {
                når { betingelse1 }
                resultat { Const("FØRSTE") }
            }
            regel {
                når { betingelse2 }
                resultat { Const("ANDRE") }
            }
            regel {
                når { betingelse3 }
                resultat { Const("TREDJE") }
            }
        }

        assertEquals("ANDRE", resultat.evaluer())
    }

    @Test
    fun `tabell bruker ellers-klausul når ingen regler matcher`() {
        val betingelse1 = Const(false)
        val betingelse2 = Const(false)

        val resultat = tabell<String> {
            regel {
                når { betingelse1 }
                resultat { Const("FØRSTE") }
            }
            regel {
                når { betingelse2 }
                resultat { Const("ANDRE") }
            }
            ellers { Const("ELLERS") }
        }

        assertEquals("ELLERS", resultat.evaluer())
    }

    @Test
    fun `tabell kaster exception hvis ingen regler matcher og ingen ellers-klausul`() {
        val betingelse1 = Const(false)
        val betingelse2 = Const(false)

        val resultat = tabell<String>("MinTabell") {
            regel {
                når { betingelse1 }
                resultat { Const("FØRSTE") }
            }
            regel {
                når { betingelse2 }
                resultat { Const("ANDRE") }
            }
        }

        val exception = assertThrows(IllegalStateException::class.java) {
            resultat.evaluer()
        }
        assertTrue(exception.message!!.contains("MinTabell"))
    }

    @Test
    fun `tabell med komplekse boolean uttrykk`() {
        val a = Const(true)
        val b = Const(false)
        val c = Const(true)

        val resultat = tabell<String> {
            regel {
                når { a og b }  // false
                resultat { Const("A OG B") }
            }
            regel {
                når { a og c }  // true
                resultat { Const("A OG C") }
            }
            regel {
                når { b eller c }  // true
                resultat { Const("B ELLER C") }
            }
        }

        assertEquals("A OG C", resultat.evaluer())
    }

    @Test
    fun `tabell med sammenlignende betingelser`() {
        val alder = Grunnlag("alder", Const(67))

        val resultat = tabell<String> {
            regel {
                når { alder erMindreEnn 62 }
                resultat { Const("FOR UNG") }
            }
            regel {
                når { alder erMindreEnn 67 }
                resultat { Const("TIDLIG") }
            }
            regel {
                når { alder erStørreEllerLik 67 }
                resultat { Const("NORMAL") }
            }
        }

        assertEquals("NORMAL", resultat.evaluer())
    }

    @Test
    fun `tabell notasjon viser alle regler`() {
        val a = Grunnlag("a", Const(true))
        val b = Grunnlag("b", Const(false))

        val tabell = tabell<String>("TestTabell") {
            regel {
                når { a }
                resultat { Const("A") }
            }
            regel {
                når { b }
                resultat { Const("B") }
            }
            ellers { Const("ELLERS") }
        }

        val notasjon = tabell.notasjon()

        assertTrue(notasjon.contains("TABELL TestTabell:"))
        assertTrue(notasjon.contains("1. NÅR"))
        assertTrue(notasjon.contains("2. NÅR"))
        assertTrue(notasjon.contains("ELLERS"))
    }

    @Test
    fun `tabell konkret viser kun matchende regel`() {
        val a = Grunnlag("a", Const(false))
        val b = Grunnlag("b", Const(true))

        val tabell = tabell<String>("TestTabell") {
            regel {
                når { a }
                resultat { Const("A") }
            }
            regel {
                når { b }
                resultat { Const("B") }
            }
            ellers { Const("ELLERS") }
        }

        val konkret = tabell.konkret()

        assertTrue(konkret.contains("TABELL TestTabell:"))
        assertTrue(konkret.contains("2. NÅR"))  // Regel 2 matchet
        assertTrue(konkret.contains("→ B"))
        assertFalse(konkret.contains("1. NÅR"))  // Regel 1 vises ikke
        assertFalse(konkret.contains("ELLERS"))  // Ellers vises ikke
    }

    @Test
    fun `tabell konkret viser ellers når ingen regler matcher`() {
        val a = Grunnlag("a", Const(false))
        val b = Grunnlag("b", Const(false))

        val tabell = tabell<String>("TestTabell") {
            regel {
                når { a }
                resultat { Const("A") }
            }
            regel {
                når { b }
                resultat { Const("B") }
            }
            ellers { Const("ELLERS") }
        }

        val konkret = tabell.konkret()

        assertTrue(konkret.contains("TABELL TestTabell:"))
        assertTrue(konkret.contains("ELLERS → ELLERS"))
        assertFalse(konkret.contains("1. NÅR"))
        assertFalse(konkret.contains("2. NÅR"))
    }

    @Test
    fun `tabell samler grunnlag KUN fra matchende regel (lazy)`() {
        val a = Grunnlag("a", Const(true))
        val b = Grunnlag("b", Const(false))
        val c = Grunnlag("c", Const(true))

        val tabell = tabell<String> {
            regel {
                når { a }
                resultat { Const("A") }
            }
            regel {
                når { b og c }
                resultat { Const("B OG C") }
            }
        }

        val grunnlagListe = tabell.grunnlagListe()

        // Lazy evaluering: kun regel 1 matchet, så kun 'a' skal samles
        assertEquals(1, grunnlagListe.size)
        assertTrue(grunnlagListe.any { it.navn == "a" })
        assertFalse(grunnlagListe.any { it.navn == "b" })
        assertFalse(grunnlagListe.any { it.navn == "c" })
    }

    @Test
    fun `tabell beregner korrekt dybde`() {
        val a = Grunnlag("a", Const(true))
        val b = Grunnlag("b", Add(Const(1), Const(2)))  // Dybde 2

        val tabell = tabell<String> {
            regel {
                når { a }
                resultat { Const("A") }
            }
            regel {
                når { Const(false) }
                resultat { Const("ALDRI") }
            }
        }

        // Dybde er 1 (tabell) + 1 (max regel dybde)
        assertEquals(2, tabell.dybde())
    }

    @Test
    fun `tabell med navngi og id`() {
        val angittFlyktning = Grunnlag("angittFlyktning", Const(true))

        val resultat = tabell<UtfallType> {
            regel {
                når { ikke(angittFlyktning) }
                resultat { Const(IKKE_RELEVANT) }
            }
            regel {
                når { angittFlyktning }
                resultat { Const(OPPFYLT) }
            }
        }.navngi("flyktningVurdering")
            .id("FlyktningVurdering")

        assertEquals(OPPFYLT, resultat.evaluer())
        assertEquals("flyktningVurdering", resultat.navn)
        assertEquals("FlyktningVurdering", resultat.rvsId)
    }

    @Test
    fun `tabell med feilUttrykk i ellers`() {
        val betingelse = Const(false)

        val tabell = tabell<String> {
            regel {
                når { betingelse }
                resultat { Const("OK") }
            }
            ellers { feilUttrykk("Ugyldig tilstand") }
        }

        val exception = assertThrows(IllegalStateException::class.java) {
            tabell.evaluer()
        }
        assertEquals("Ugyldig tilstand", exception.message)
    }

    @Test
    fun `realistisk eksempel - flyktningvurdering`() {
        val angittFlyktning = Grunnlag("angittFlyktning", Const(true))
        val kravlinjeFremsattDatoFom2021 = Grunnlag("kravlinjeFremsattDatoFom2021", Const(true))
        val overgangsRegler = Grunnlag("overgangsRegler", Const(false))

        val resultat = tabell<UtfallType>("flyktningVurdering") {
            regel {
                når { ikke(angittFlyktning) }
                resultat { Const(IKKE_RELEVANT) }
            }
            regel {
                når { angittFlyktning og ikke(kravlinjeFremsattDatoFom2021) }
                resultat { Const(OPPFYLT) }
            }
            regel {
                når { angittFlyktning og kravlinjeFremsattDatoFom2021 og ikke(overgangsRegler) }
                resultat { Const(IKKE_OPPFYLT) }
            }
            regel {
                når { angittFlyktning og kravlinjeFremsattDatoFom2021 og overgangsRegler }
                resultat { Const(OPPFYLT) }
            }
            ellers { feilUttrykk("Ugyldig tilstand i flyktningvurdering") }
        }.navngi("erFlyktning")
            .id("ErFlyktning")

        assertEquals(IKKE_OPPFYLT, resultat.evaluer())
    }

    @Test
    fun `tabell uten navn`() {
        val tabell = tabell<String> {
            regel {
                når { Const(true) }
                resultat { Const("OK") }
            }
        }

        val notasjon = tabell.notasjon()
        assertTrue(notasjon.startsWith("TABELL:"))
        assertFalse(notasjon.contains("TABELL :"))  // Ingen ekstra mellomrom
    }

    @Test
    fun `regel builder krever både når og resultat`() {
        assertThrows(IllegalStateException::class.java) {
            tabell<String> {
                regel {
                    // Mangler når
                    resultat { Const("OK") }
                }
            }.evaluer()
        }

        assertThrows(IllegalStateException::class.java) {
            tabell<String> {
                regel {
                    når { Const(true) }
                    // Mangler resultat
                }
            }.evaluer()
        }
    }
}
