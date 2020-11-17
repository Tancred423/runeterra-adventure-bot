package red.tanc.rab.commands.standard

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import red.tanc.rab.Bot
import red.tanc.rab.commands.Command
import red.tanc.rab.utilities.Constants
import red.tanc.rab.utilities.Database
import java.util.concurrent.TimeUnit


class ChannelCommand : Command() {
    override val guildOnly: Boolean = true
    override val adminOnly: Boolean = true

    override fun run(e: MessageReceivedEvent, args: List<String>) {
        val guild = e.guild
        val channel = e.channel
        val message = e.message
        val user = e.author

        val db = Database(guild.idLong)

        if (message.mentionedChannels.size > 0) {
            // Set up channel
            val tlChannel = message.mentionedChannels[0]
            val currentChannelId = db.getChannelId()

            if (currentChannelId == tlChannel.idLong) {
                // Ask for unset
                channel.sendMessage(
                    """
                    Do you want to unset ${tlChannel.asMention} as travel log channel?
                    The adventures will then be paused on this server until you set up a channel again.
                """.trimIndent()
                ).queue { sentMessage ->
                    sentMessage.addReaction(Constants.ACCEPT).queue()
                    sentMessage.addReaction(Constants.DECLINE).queue()

                    Bot.waiter.waitForEvent(
                        GuildMessageReactionAddEvent::class.java,
                        { e ->
                            (e.user == user
                                    && e.messageIdLong == sentMessage.idLong
                                    && (e.reactionEmote.name == Constants.ACCEPT
                                    || e.reactionEmote.name == Constants.DECLINE))
                        },
                        { e ->
                            if (e.reactionEmote.name == Constants.ACCEPT) {
                                // Accept: Unset channel
                                sentMessage.clearReactions().queue()
                                sentMessage.editMessage("${tlChannel.asMention} was unset and the adventures are paused.")
                                    .queue()
                                db.unsetChannelId()
                            } else {
                                // Decline: Cancel
                                sentMessage.clearReactions().queue()
                                sentMessage.editMessage("Unset cancelled.").queue()
                            }
                        }, 15, TimeUnit.MINUTES
                    ) { timeout(sentMessage) }
                }
            } else {
                // Set
                db.setChannelId(tlChannel.idLong)
                channel.sendMessage(
                    """
                    The travel log channel was set to: ${tlChannel.asMention}
                    The adventures can ${if (db.hasMeters()) "continue." else "start."}
                """.trimIndent()
                ).queue()
            }
        } else {
            // Get channel
            val tlChannel = guild.getTextChannelById(db.getChannelId())
            if (tlChannel != null) channel.sendMessage("The current travel log channel is: ${tlChannel.asMention}")
                .queue()
            else channel.sendMessage("The travel log channel could **not** be found.").queue()
        }
    }

    // Timeout
    private fun timeout(sentMessage: Message) {
        println(sentMessage.contentRaw)
        sentMessage.clearReactions().queue {
            sentMessage.editMessage(
                """
                |~~${sentMessage.contentRaw}~~
                |⚠ Timeout: You did **not** react in time. (15 minutes)
            """.trimMargin()
            ).queue()
        }
    }
}