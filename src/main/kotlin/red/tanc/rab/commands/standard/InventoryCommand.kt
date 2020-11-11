package red.tanc.rab.commands.standard

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import red.tanc.rab.commands.Command
import red.tanc.rab.gameElements.Item
import red.tanc.rab.utilities.Constants
import red.tanc.rab.utilities.Converter
import java.awt.Color

class InventoryCommand : Command() {
    override val guildOnly: Boolean = true
    override val adminOnly: Boolean = false

    override fun run(e: MessageReceivedEvent, args: List<String>) {
        val user = e.author

        // TODO: retrieve list from database. this is just for testing
        val items = mutableListOf<Item>()
        items.add(Item("BLOOD Orange", "\uD83C\uDF4A", 100))
        items.add(Item("Fish stEW", "\uD83C\uDF72", 1))
        items.add(Item("A Test", "\uD83E\uDD95", 1000000))

        // The maximum length of an amount
        // This will be used to adjust the indents
        val maxLength = getMaxLength(items)

        // A list of field values of the inventory
        // This will be used to bypass the 1024 character limit
        val values = mutableListOf<String>()

        // Building the inventory list
        var list = ""
        for (item in items) {
            val listTmp = "- `${Converter.addIndent(item.amount.toString(), maxLength)}x` ${item.emoji} ${item.name}\n"

            if (list.length + listTmp.length > 1024
                    && getListLength(values) + list.length <= Constants.MESSAGE_MAX_LENGTH) {
                values.add(list)
                list = ""
            }

            list += listTmp
        }

        if (getListLength(values) + list.length <= Constants.MESSAGE_MAX_LENGTH)
            values.add(list)

        // Create the fancy inventory message
        val eb = EmbedBuilder()
                .setColor(Color.decode(Constants.COLOR))
                .setAuthor("${user.name}${Converter.getApostrophS(user.name)} inventory", null, user.effectiveAvatarUrl)

        for (value in values)
            eb.addField("", value, false)

        e.channel.sendMessage(eb.build()).queue()
    }

    private fun getMaxLength(items: MutableList<Item>): Int {
        var i = 0
        for (item in items) if (item.amount > i) i = item.amount
        return i.toString().length
    }

    private fun getListLength(values: MutableList<String>): Int {
        var i = 0
        for (value in values) i += value.length
        return i
    }
}