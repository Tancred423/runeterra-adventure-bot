package red.tanc.rab.commands

import net.dv8tion.jda.api.events.message.MessageReceivedEvent

abstract class Command {
    abstract val guildOnly: Boolean
    abstract val adminOnly: Boolean

    abstract fun run(e: MessageReceivedEvent, args: List<String>)
}