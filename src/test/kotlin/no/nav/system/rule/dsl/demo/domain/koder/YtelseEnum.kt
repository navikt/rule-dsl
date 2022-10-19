package no.nav.system.rule.dsl.demo.domain.koder

import no.nav.system.rule.dsl.enums.SuperEnum
import no.nav.system.rule.dsl.rettsregel.Faktum

enum class YtelseEnum : SuperEnum {
    AP, UT, AFP, GJR, GJP, UT_GJR;

    override fun navn(): String = this.name

    override fun faktum(): Faktum<YtelseEnum> {
        return when (this) {
            AP -> Faktum("Alderspensjon", this)
            UT -> Faktum("Uføretrygd", this)
            AFP -> Faktum("Avtalefestetpensjon", this)
            GJR -> Faktum("Gjenlevenderett", this)
            GJP -> Faktum("Gjenlevendepensjon", this)
            UT_GJR -> Faktum("Gjenlevendetillegg", this)
        }
    }
}


