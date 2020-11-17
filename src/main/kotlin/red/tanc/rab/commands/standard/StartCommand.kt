package red.tanc.rab.commands.standard

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import red.tanc.rab.commands.Command
import red.tanc.rab.utilities.Database

class StartCommand : Command() {
    override val guildOnly: Boolean = true
    override val adminOnly: Boolean = false

    override fun run(e: MessageReceivedEvent, args: List<String>) {
        val guild = e.guild
        val user = e.author
        val channel = e.channel

        val db = Database(guild.idLong)

        when (db.optIn(user.idLong)) {
            0 -> channel.sendMessage("${user.asMention}, your adventure has begun!").queue()
            1 -> channel.sendMessage("${user.asMention}, your adventure is already running!").queue()
            2 -> channel.sendMessage("${user.asMention}, your adventure will continue!").queue()
        }
    }
}