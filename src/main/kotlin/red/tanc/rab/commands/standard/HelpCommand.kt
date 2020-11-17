package red.tanc.rab.commands.standard

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import red.tanc.rab.commands.Command
import red.tanc.rab.utilities.Constants
import red.tanc.rab.utilities.Database
import java.awt.Color

class HelpCommand : Command() {
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

        eb.addField(
            "Standard Commands", """
            🔎 **${Constants.PREFIX}help**
            ```
              • Alias: ${Constants.PREFIX}h
              • Displays this message
            ```
            🏓 **${Constants.PREFIX}ping**
            ```
              • Alias: ${Constants.PREFIX}pong
              • Displays the bot's latencies
            ```
        """.trimIndent(), false
        )

        if (e.member!!.hasPermission(Permission.ADMINISTRATOR)) {
            eb.addField(
                "Admin Commands", """
                #️⃣ **${Constants.PREFIX}channel**
                ```
                  • Alias: ${Constants.PREFIX}chan 
                  • Displays the current travel log channel
                ```
                #️⃣ **${Constants.PREFIX}channel #channel-name**
                ```
                  • Alias: ${Constants.PREFIX}chan #channel-name
                  • Sets or unsets the mentioned channel for travel logs
                ```
            """.trimIndent(), false
            )
        }

        eb.addField(
            "Adventure Commands", """
                    ℹ️ **${Constants.PREFIX}info**
                    ```
                      • Alias: ${Constants.PREFIX}information
                      • Displays your adventure information
                    ```
                    ℹ️ **${Constants.PREFIX}info @user**
                    ```
                      • Alias: ${Constants.PREFIX}information @user
                      • Displays the mentioned user's adventure information
                    ```
                    :arrow_forward: **${Constants.PREFIX}start:**
                    ```
                      • Alias: ${Constants.PREFIX}resume
                      • Starts or resumes your adventure
                    ```
                    ⏸️ **${Constants.PREFIX}stop:**
                    ```
                      • Alias: ${Constants.PREFIX}pause
                      • Pauses your adventure
                    ```  
                    🎒 **${Constants.PREFIX}inventory:**
                    ```
                      • Alias: ${Constants.PREFIX}inv
                      • Displays your inventory
                    ```  
                """.trimIndent(), false
        )

        e.channel.sendMessage(eb.build()).queue()
    }

    private fun getAdventureStatus(e: MessageReceivedEvent, db: Database): String {
        val user = e.author

        val channelId = db.getChannelId()
        val hasMeters = db.hasMeters()

        var status = db.getOptStatusText(user.idLong)

        if (channelId == 0L && hasMeters) {
            status += " (Paused server wide by admin)"
        } else if (channelId == 0L) {
            status += " (Not yet started from admin)"
        }

        return status
    }
}