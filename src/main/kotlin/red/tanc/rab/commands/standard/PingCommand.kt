package red.tanc.rab.commands.standard

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import red.tanc.rab.commands.Command
import red.tanc.rab.utilities.Constants
import java.awt.Color
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

class PingCommand : Command() {
    override val guildOnly: Boolean = false
    override val adminOnly: Boolean = false

    override fun run(e: MessageReceivedEvent, args: List<String>) {
        val eb = EmbedBuilder()
                .setColor(Color.decode(Constants.COLOR))
                .setDescription("Measuring ...")

        e.channel.sendMessage(eb.build()).queue { message ->
            val jda = e.jda
            val botPing = e.message.timeCreated.until(message.timeCreated, ChronoUnit.MILLIS)
            val webSocketPing = jda.gatewayPing
            val sm = jda.shardManager
            val avgWebSocketPing = sm?.averageGatewayPing?.roundToInt() ?: 0
            jda.restPing.queue { restPing ->
                val newEmbed = EmbedBuilder()
                        .setColor(Color.decode(Constants.COLOR))
                        .addField(
                                "Bot Ping: $botPing ms",
                                "The time that the bot took to respond to your ping command.",
                                false
                        )
                        .addField(
                                "REST Ping: $restPing ms",
                                "The time that Discord took to respond to an API request.",
                                false
                        )
                        .addField(
                                "WebSocket Ping (Shard: ${jda.shardInfo.shardId}): $webSocketPing ms",
                                "The time that Discord took to respond to the current shard's last heartbeat.",
                                false
                        )
                        .addField(
                                "Avg. WebSocket Ping (All ${sm!!.shardsTotal} shards): $avgWebSocketPing ms",
                                "The average time that Discord took to respond to all shard's last heatbeats.",
                                false
                        )
                        .build()

                message.editMessage(newEmbed).queue()
            }
        }
    }
}