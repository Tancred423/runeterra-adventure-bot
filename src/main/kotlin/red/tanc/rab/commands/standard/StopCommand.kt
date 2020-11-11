package red.tanc.rab.commands.standard

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import red.tanc.rab.commands.Command
import red.tanc.rab.utilities.Database

class StopCommand : Command() {
    override val guildOnly: Boolean = true
    override val adminOnly: Boolean = false

    override fun run(e: MessageReceivedEvent, args: List<String>) {
        val guild = e.guild
        val user = e.author
        val channel = e.channel

        when (Database.optOut(guild.idLong, user.idLong)) {
            0 -> channel.sendMessage("${user.asMention}, your adventure has not begun yet, thus you cannot pause it!").queue()
            1 -> channel.sendMessage("${user.asMention}, your adventure is now paused!").queue()
            2 -> channel.sendMessage("${user.asMention}, your adventure was already paused!").queue()
        }
    }
}