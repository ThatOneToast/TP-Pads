package dev.toast.portals

import dev.toast.Pads
import dev.toast.utils.ChatStructure
import dev.toast.utils.WLPlayerCommand
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class GetTPBlock: WLPlayerCommand(
    name = "tpblock",
    permission = "pads.tpblock",
    cooldown = 5
) {


    private fun createTPBlock(id: String): ItemStack {
        val block = ItemStack(Material.END_PORTAL_FRAME)
        val meta = block.itemMeta ?: return ItemStack(Material.END_PORTAL_FRAME)

        meta.displayName(ChatStructure.GOLD + "TP Block - $id")
        val lore: MutableList<Component> = arrayListOf()

        lore.add(ChatStructure.WHITE + " - The first portal you place is the main control portal. ")
        lore.add(ChatStructure.WHITE + "   - The second portal is the child. ")
        lore.add(ChatStructure.WHITE + "   - If you disable the tp capability on the child, The child will not teleport you back to the main.")

        meta.lore(lore)
        val pdc = meta.persistentDataContainer
        pdc.set(NamespacedKey(Pads.getPlugin(), "tp_block"), PersistentDataType.STRING, id)

        block.itemMeta = meta
        return block
    }



    override fun executeCommand(sender: Player, args: Array<String>): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage(ChatStructure.RED + "Usage: /tpblock <id>")
            return true
        }

        val id = args[0]
        val tpBlock = createTPBlock(id)
        sender.inventory.addItem(tpBlock)
        sender.updateInventory()
        sender.sendMessage(ChatStructure.GREEN + "TP Block added to your inventory.")

        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<String>
    ): List<String> {
        return listOf()
    }
}
