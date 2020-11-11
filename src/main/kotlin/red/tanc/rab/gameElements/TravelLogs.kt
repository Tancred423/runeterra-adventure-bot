package red.tanc.rab.gameElements

class TravelLogs {
    companion object {
        fun getNameOfChapter(chapter: Int): String {
            return when (chapter) {
                else -> "Arrival in Ionia"
            }
        }

        fun getStory(chapter: Int): String {
            return when (chapter) {
                else -> "Story here soon™"
            }
        }
    }
}