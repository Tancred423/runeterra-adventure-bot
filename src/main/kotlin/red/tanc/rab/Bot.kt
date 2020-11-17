package red.tanc.rab

import com.jagrosh.jdautilities.commons.waiter.EventWaiter
import com.zaxxer.hikari.HikariDataSource
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.entities.Activity
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder
import net.dv8tion.jda.api.utils.ChunkingFilter
import net.dv8tion.jda.api.utils.MemberCachePolicy
import red.tanc.rab.listeners.EventListener
import red.tanc.rab.utilities.Database
import java.io.FileInputStream
import java.time.Instant
import java.util.*

object Bot {
    private val config = Properties()
    val db: HikariDataSource = Database.connectToDatabase()
    var guildCooldowns: MutableMap<Long, MutableMap<Long, Instant>> = mutableMapOf()
    val waiter = EventWaiter()

    @JvmStatic
    fun main(args: Array<String>) {
        config.load(FileInputStream("${System.getProperty("user.dir")}/resources/bot.properties"))

        DefaultShardManagerBuilder.create(config.getProperty("botToken"), EnumSet.allOf(GatewayIntent::class.java))
                .setChunkingFilter(ChunkingFilter.ALL)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .setActivity(Activity.playing("loading..."))
                .setAutoReconnect(true)
                .addEventListeners(EventListener(), waiter)
                .build()
    }
}