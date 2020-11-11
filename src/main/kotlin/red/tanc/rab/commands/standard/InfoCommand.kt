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

        // User mentioned user if available
        if (message.mentionedUsers.size > 0)
            user = message.mentionedUsers[0]

        val meters = Database.getMeters(guild.idLong, user.idLong)

        val eb = EmbedBuilder()
            .setColor(Color.decode(Constants.COLOR))
            .setAuthor(user.name, null, user.effectiveAvatarUrl)
            .addField("Adventure status", Database.getOptStatusText(guild.idLong, user.idLong), true)
            .addField("Current chapter", Converter.getChapter(meters).toString(), true)
            .addField("Kilometers walked", Converter.toKilometer(meters).toString(), true)
            .addField(
                "Commands", """
                    ℹ️ **${Constants.PREFIX}info:**
                    ```
                      - Alias: ${Constants.PREFIX}information
                      - Displays this message
                    ```
                    :arrow_forward: **${Constants.PREFIX}start:**
                    ```
                      - Alias: ${Constants.PREFIX}resume
                      - Starts or resumes your adventure
                    ```
                    ⏸️ **${Constants.PREFIX}stop:**
                    ```
                      - Alias: ${Constants.PREFIX}pause
                      - Pauses your adventure
                    ```  
                    🎒 **${Constants.PREFIX}inventory:**
                    ```
                      - Alias: ${Constants.PREFIX}inv
                      - Displays your inventory
                    ```  
                """.trimIndent(), false
            )

        e.channel.sendMessage(eb.build()).queue()
    }
}