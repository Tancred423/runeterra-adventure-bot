package red.tanc.rab.utilities

import kotlin.math.floor
import kotlin.math.roundToInt

class Converter {
    companion object {
        fun getChapter(meter: Int): Int {
            return floor(meter.toDouble() / Constants.METERS_PER_CHAPTER).toInt()
        }

        fun toKilometer(meter: Int): Double {
            return ((meter.toDouble() / 1000) * 100.0).roundToInt() / 100.0
        }

        fun addIndent(string: String, totalLenght: Int): String {
            val indents = totalLenght - string.length
            return if (indents > 0) " ".repeat(indents) + string
            else string
        }

        fun getApostrophS(name: String): String {
            return if (name.endsWith("s")) "'" else "'s"
        }
    }
}