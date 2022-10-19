package no.nav.system.rule.dsl.demo.domain.koder

import no.nav.system.rule.dsl.enums.SuperEnum
import no.nav.system.rule.dsl.rettsregel.Faktum

enum class UnntakEnum : SuperEnum {
    FLYKT_ALDER, FLYKT_BARNEP, FLYKT_GJENLEV, FLYKT_UFOREP;

    override fun faktum(): Faktum<*> {
        return when (this) {
            FLYKT_ALDER -> Faktum("Flyktning Alderspensjon", this)
            FLYKT_BARNEP -> Faktum("Flyktning Barnepensjon", this)
            FLYKT_GJENLEV -> Faktum("Flyktning Gjenlevendepensjon", this)
            FLYKT_UFOREP -> Faktum("Flyktning Uførepensjon", this)
        }
    }

    override fun navn() = this.name

}




