package red.tanc.rab.utilities

import java.time.OffsetDateTime
import java.time.ZoneId

class Console {
    companion object {
        fun log(value: String) {
            val nowString = OffsetDateTime.now(ZoneId.of("+01:00")).toString().replace("T", " ").substring(0, 16)
            println("[${nowString}] $value")
        }
    }
}