package red.tanc.rab.commands.standard

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import red.tanc.rab.commands.Command
import red.tanc.rab.utilities.Constants
import red.tanc.rab.utilities.Converter
import red.tanc.rab.utilities.Database
import java.awt.Color

class InfoCommand : Command() {
    override val guildOnly: Boolean = true
    override val adminOnly: Boolean = false

    override fun run(e: MessageReceivedEvent, args: List<String>) {
        val guild = e.guild
        var user = e.author
        val message = e.message

        val db = Database(guild.idLong)

        // User mentioned user if available
        if (message.mentionedUsers.size > 0)
            user = message.mentionedUsers[0]

        val meters = db.getMeters(user.idLong)

        val eb = EmbedBuilder()
            .setColor(Color.decode(Constants.COLOR))
            .setAuthor(user.name, null, user.effectiveAvatarUrl)
            .addField("Adventure status", getAdventureStatus(e, db), true)
            .addField("Current chapter", Converter.getChapter(meters).toString(), true)
            .addField("Kilometers walked", Converter.toKilometer(meters).toString(), true)

        e.channel.sendMessage(eb.build()).queue()
    }

    private fun getAdventureStatus(e: MessageReceivedEvent, db: Database): String {
        val user = e.author

        val channelId = db.getChannelId()
        val hasMeters = db.hasMeters()

        var status = db.getOptStatusText(user.idLong)

        if (channelId == 0L && hasMeters) {
            status += "\n(Paused server wide by admin)"
        } else if (channelId == 0L) {
            status += "\n(Not yet started by admin)"
        }

        return status
    }
}