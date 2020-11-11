package red.tanc.rab.commands.standard

import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import red.tanc.rab.commands.Command
import red.tanc.rab.utilities.Database

class ChannelCommand : Command() {
    override val guildOnly: Boolean = true
    override val adminOnly: Boolean = true

    override fun run(e: MessageReceivedEvent, args: List<String>) {
        val guild = e.guild
        val channel = e.channel
        val message = e.message

        if (message.mentionedChannels.size > 0) {
            // Set up channel
            val tlChannel = message.mentionedChannels[0]
            Database.setChannelId(guild.idLong, tlChannel.idLong)
            channel.sendMessage("The travel log channel was set to: ${tlChannel.asMention}").queue()
        } else {
            // Get channel
            val tlChannel = guild.getTextChannelById(Database.getChannelId(guild.idLong))

            if (tlChannel != null) channel.sendMessage("The current channel is: ${tlChannel.asMention}").queue()
            else channel.sendMessage("The travel log channel couldn't be found.").queue()
        }
    }
}