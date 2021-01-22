package red.tanc.rab.listeners

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import red.tanc.rab.Bot
import red.tanc.rab.commands.Command
import red.tanc.rab.commands.standard.*
import red.tanc.rab.gameElements.TravelLogs
import red.tanc.rab.utilities.Console
import red.tanc.rab.utilities.Constants
import red.tanc.rab.utilities.Converter
import red.tanc.rab.utilities.Database
import java.awt.Color
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.concurrent.ThreadLocalRandom

class EventListener : ListenerAdapter() {
    override fun onReady(e: ReadyEvent) {
        val jda = e.jda;
        val selfUser = jda.selfUser
        println(
                """
            ========================================================
            Account: ${selfUser.name}#${selfUser.discriminator} (ID: ${selfUser.id})
            Shard: ${jda.shardInfo}
            Guilds: ${jda.shardManager!!.guilds.size} (Unavailable: ${jda.unavailableGuilds.size})
            Text Channels: ${jda.shardManager!!.textChannels.size}
            Users: ${jda.shardManager!!.users.size}
            Prefix: ${Constants.PREFIX}
            BOT READY!
            ========================================================
        """.trimIndent()
        )

        jda.presence.setPresence(
                OnlineStatus.ONLINE,
                Activity.playing("RAB | ${Constants.PREFIX}help")
        )
    }

    override fun onMessageReceived(e: MessageReceivedEvent) {
        val user = e.author

        if (user.isBot) return

        // Commands
        processCommand(e)

        // Walking (Meters)
        if (e.isFromGuild) {
            val db = Database(e.guild.idLong)

            if (db.getOptStatus(user.idLong) == 1 && db.getChannelId() != 0L)
                processMeters(e)
        }
    }

    private fun processCommand(e: MessageReceivedEvent) {
        val prefix = Constants.PREFIX
        val message = e.message
        var content = message.contentRaw

        if (content.startsWith(prefix)) {
            content = content.replace(prefix, "").trim()
            val contentSplit = content.split(" ")
            val invoke = contentSplit[0]
            val args = contentSplit.drop(1)

            val success = when (invoke.toLowerCase()) {
                // Standard Commands
                "help", "h" -> run(HelpCommand(), e, args)
                "ping", "pong" -> run(PingCommand(), e, args)

                // Admin Commands
                "channel", "chan" -> run(ChannelCommand(), e, args)

                // Commands
                "info", "information" -> run(InfoCommand(), e, args)
                "start", "resume" -> run(StartCommand(), e, args)
                "stop", "pause" -> run(StopCommand(), e, args)
                "inventory", "inv" -> run(InventoryCommand(), e, args)
                else -> return
            }

            if (success) Console.log("[Success] Guild: ${if (e.isFromGuild) e.guild.name else "DM"} - ${e.author.asTag}: ${message.contentDisplay}")
            else Console.log("[Failed] Guild: ${if (e.isFromGuild) e.guild.name else "DM"} - ${e.author.asTag}: ${message.contentDisplay}")
        }
    }

    private fun run(cmd: Command, e: MessageReceivedEvent, args: List<String>): Boolean {
        return if ((!cmd.guildOnly || e.isFromGuild) // Guild only commands only in guilds
                && (!cmd.adminOnly || (e.isFromGuild && e.member!!.isOwner))
        ) { // Admin only commands only from admins
            cmd.run(e, args)
            true
        } else false
    }

    private fun processMeters(e: MessageReceivedEvent) {
        val guild = e.guild
        val user = e.author

        val db = Database(guild.idLong)

        var offCooldown = true
        var userCooldowns = Bot.guildCooldowns[guild.idLong] // returns Map<userId, instant> from current guild
        if (userCooldowns == null) userCooldowns = mutableMapOf()

        if (userCooldowns.contains(user.idLong)) {
            // Check for cooldown
            val now = Instant.now()
            val lastMessage = userCooldowns[user.idLong]
            val diffMillis = ChronoUnit.MILLIS.between(lastMessage, now)
            if (diffMillis < Constants.WALK_COOLDOWN) offCooldown = false
        }

        if (offCooldown) {
            val oldMeters = db.getMeters(user.idLong)
            val oldChapter = Converter.getChapter(oldMeters)

            val randomMeters = ThreadLocalRandom.current().nextInt(15, 26)
            db.setMeters(user.idLong, randomMeters)

            val newMeters = db.getMeters(user.idLong)
            val newChapter = Converter.getChapter(newMeters)

            // Update cooldown
            userCooldowns[user.idLong] = Instant.now()
            Bot.guildCooldowns[guild.idLong] = userCooldowns

            // Check for reached chapter
            if (newChapter > oldChapter) {
                // New chapter reached
                Console.log("${user.name} walked $randomMeters meters and reached chapter $newChapter. Total: $newMeters meters")

                val mb = MessageBuilder()
                        .setContent(user.asMention)

                val eb = EmbedBuilder()
                        .setColor(Color.decode(Constants.COLOR))
                        .setAuthor(
                                "${user.name}${Converter.getApostrophS(user.name)} adventure",
                                null,
                                user.effectiveAvatarUrl
                        )
                        .setTitle("Travel Log $newChapter - ${TravelLogs.getNameOfChapter(newChapter)}")
                        .setDescription("||${TravelLogs.getStory(newChapter)}||")
                        .addField("Commands for this travel log", "-anythingIdkJustATestAnyway", false)

                val tlChannelId = db.getChannelId()
                val tlChannel = guild.getTextChannelById(tlChannelId)
                tlChannel?.sendMessage(mb.setEmbed(eb.build()).build())?.queue()
            } else Console.log("${user.name} walked $randomMeters meters. Total: $newMeters meters")
        }
    }
}