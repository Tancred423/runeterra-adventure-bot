package red.tanc.rab.utilities

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import red.tanc.rab.Bot
import java.sql.Connection
import java.sql.SQLException

public class Database {
    companion object {
        ////////////////////////////////////////////
        // General
        ////////////////////////////////////////////
        private fun closeQuietly(connection: Connection) {
            try {
                connection.close()
            } catch (e: SQLException) {
                Console.log("Closing DB connection failed")
            }
        }

        ////////////////////////////////////////////
        // Travel log channel
        ////////////////////////////////////////////
        fun getChannelId(guildId: Long): Long {
            var id = 0L
            val connection = Bot.db.connection

            try {
                val select = connection.prepareStatement("""
                    SELECT travel_log_channel_id
                    FROM guilds
                    WHERE guild_id=?
                """.trimIndent())
                select.setLong(1, guildId)

                val resultSet = select.executeQuery()

                if (resultSet.next()) id = resultSet.getLong("travel_log_channel_id")
            } catch (e: SQLException) {
                println(e.printStackTrace())
            } finally {
                closeQuietly(connection)
            }

            return id
        }

        fun setChannelId(guildId: Long, channelId: Long) {
            val currentChannelId = getChannelId(guildId)
            val connection = Bot.db.connection

            try {
                if (currentChannelId == 0L) {
                    // Insert
                    val insert = connection.prepareStatement("""
                        INSERT INTO guilds (guild_id,travel_log_channel_id)
                        VALUES (?,?)
                    """.trimIndent())
                    insert.setLong(1, guildId)
                    insert.setLong(2, channelId)

                    insert.executeUpdate()
                } else {
                    // Update
                    val update = connection.prepareStatement("""
                        UPDATE guilds
                        SET travel_log_channel_id=?
                        WHERE guild_id=?
                    """.trimIndent())
                    update.setLong(1, channelId)
                    update.setLong(2, guildId)

                    update.executeUpdate()
                }
            } catch (e: SQLException) {
                println(e.printStackTrace())
            } finally {
                closeQuietly(connection)
            }
        }

        ////////////////////////////////////////////
        // Meters
        ////////////////////////////////////////////
        fun getMeters(guildId: Long, userId: Long): Int {
            var meters = 0
            val connection = Bot.db.connection

            try {
                val select = connection.prepareStatement("""
                    SELECT meters
                    FROM meters
                    WHERE guild_id=?
                    AND user_id=?
                """.trimIndent())
                select.setLong(1, guildId)
                select.setLong(2, userId)

                val resultSet = select.executeQuery()

                if (resultSet.next()) meters = resultSet.getInt("meters")
            } catch (e: SQLException) {
                println(e.printStackTrace())
            } finally {
                closeQuietly(connection)
            }

            return meters
        }

        fun setMeters(guildId: Long, userId: Long, meters: Int) {
            val currentMeters = getMeters(guildId, userId)
            val connection = Bot.db.connection

            try {
                if (currentMeters == 0) {
                    // Insert
                    val insert = connection.prepareStatement("""
                        INSERT INTO meters (guild_id,user_id,meters)
                        VALUES (?,?,?)
                    """.trimIndent())
                    insert.setLong(1, guildId)
                    insert.setLong(2, userId)
                    insert.setInt(3, meters)

                    insert.executeUpdate()
                } else {
                    // Update
                    val update = connection.prepareStatement("""
                        UPDATE meters
                        SET meters=?
                        WHERE guild_id=?
                        AND user_id=?
                    """.trimIndent())
                    update.setInt(1, currentMeters + meters)
                    update.setLong(2, guildId)
                    update.setLong(3, userId)

                    update.executeUpdate()
                }
            } catch (e: SQLException) {
                println(e.printStackTrace())
            } finally {
                closeQuietly(connection)
            }
        }

        ////////////////////////////////////////////
        // Opt-In / Opt-Out
        // Status: 0 = not begun, 1 = running, 2 = paused
        // todo: make that an enum
        ////////////////////////////////////////////
        fun getOptStatusText(guildId: Long, userId: Long): String {
            return when (getOptStatus(guildId, userId)) {
                1 -> "Running"
                2 -> "Paused"
                else -> "Not started" // 0
            }
        }

        fun getOptStatus(guildId: Long, userId: Long): Int {
            var status = 0
            val connection = Bot.db.connection

            try {
                val select = connection.prepareStatement("""
                    SELECT status
                    FROM opt
                    WHERE guild_id=?
                    AND user_id=?
                """.trimIndent())
                select.setLong(1, guildId)
                select.setLong(2, userId)

                val resultSet = select.executeQuery()

                if (resultSet.next()) status = resultSet.getInt("status")
            } catch (e: SQLException) {
                println(e.printStackTrace())
            } finally {
                closeQuietly(connection)
            }

            return status
        }

        fun optIn(guildId: Long, userId: Long): Int {
            var status = 0
            val connection = Bot.db.connection

            try {
                status = getOptStatus(guildId, userId)
                if (status == 0) {
                    // Insert
                    val insert = connection.prepareStatement("""
                        INSERT INTO opt (guild_id,user_id,status)
                        VALUES (?,?,?)
                    """.trimIndent())
                    insert.setLong(1, guildId)
                    insert.setLong(2, userId)
                    insert.setInt(3, 1) // running

                    insert.executeUpdate()
                } else if (status == 2) {
                    // Update
                    val update = connection.prepareStatement("""
                        UPDATE opt
                        SET status=?
                        WHERE guild_id=?
                        AND user_id=?
                    """.trimIndent())
                    update.setInt(1, 1) // running
                    update.setLong(2, guildId)
                    update.setLong(3, userId)

                    update.executeUpdate()
                }
            } catch (e: SQLException) {
                println(e.printStackTrace())
            } finally {
                closeQuietly(connection)
            }

            return status
        }

        fun optOut(guildId: Long, userId: Long): Int {
            var status = 0
            val connection = Bot.db.connection

            try {
                status = getOptStatus(guildId, userId)
                if (status == 1) {
                    // Update
                    val update = connection.prepareStatement("""
                        UPDATE opt
                        SET status=?
                        WHERE guild_id=?
                        AND user_id=?
                    """.trimIndent())
                    update.setInt(1, 2) // paused
                    update.setLong(2, guildId)
                    update.setLong(3, userId)

                    update.executeUpdate()
                }
            } catch (e: SQLException) {
                println(e.printStackTrace())
            } finally {
                closeQuietly(connection)
            }

            return status
        }
    }

    fun connectToDatabase(): HikariDataSource {
        val path = "${System.getProperty("user.dir")}/resources/database.properties"
        val config = HikariConfig(path)
        return HikariDataSource(config)
    }
}