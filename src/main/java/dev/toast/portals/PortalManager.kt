package dev.toast.portals

import dev.toast.Configs
import dev.toast.Pads
import dev.toast.configs.WLYamlConfig
import dev.toast.events.PlayerPortalBlockTeleportEvent
import dev.toast.saveNewPortal
import dev.toast.utils.CooldownManager
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.*

class PortalManager: Listener {

    init {
        Pads.getPlugin().server.pluginManager.registerEvents(this, Pads.getPlugin())
    }

    private val tpBlocksPlaced: MutableMap<UUID, PortalData> = mutableMapOf()


    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPortalPlace(event: BlockPlaceEvent) {
        val blockItem = event.itemInHand
        val block = event.blockPlaced

        val blockItemPdc = blockItem.itemMeta?.persistentDataContainer ?: return
        if (!blockItemPdc.has(NamespacedKey(Pads.getPlugin(), "tp_block"), PersistentDataType.STRING)) return
        val id = blockItemPdc.get(NamespacedKey(Pads.getPlugin(), "tp_block"), PersistentDataType.STRING)!!


        fun handleFirst(): PortalData {
            val playerData = PortalData()
            playerData.id = id
            tpBlocksPlaced[event.player.uniqueId] = playerData
            return playerData
        }

        val location = block.location
        val playerData: PortalData = tpBlocksPlaced[event.player.uniqueId] ?: handleFirst()



        if (playerData.masterNode == null && playerData.childNode == null) {
            playerData.masterNode = location
            tpBlocksPlaced[event.player.uniqueId] = playerData

        }

        else if (playerData.masterNode != null && playerData.childNode == null) {
            playerData.childNode = location
            tpBlocksPlaced[event.player.uniqueId] = playerData
            saveNewPortal(playerData)
            tpBlocksPlaced.remove(event.player.uniqueId)
            val masterCords = "X: ${playerData.masterNode!!.x}, Y: ${playerData.masterNode!!.y}, Z: ${playerData.masterNode!!.z}"
            val childCords = "X: ${playerData.childNode!!.x}, Y: ${playerData.childNode!!.y}, Z: ${playerData.childNode!!.z}"
            val message = MiniMessage.miniMessage().deserialize(
                "<rainbow>!--------------------${playerData.id}-Portal--------------------!</rainbow> \n" +
                        "<gold>Master:</gold> <gray><click:copy_to_clipboard:'${playerData.masterNode!!.x} ${playerData.masterNode!!.y} ${playerData.masterNode!!.z}'><hover:show_text:'<green>Click to copy!</green>'>{ $masterCords }</hover></click></gray> \n" +
                        "<gold>Child:</gold> <gray><click:copy_to_clipboard:'${playerData.childNode!!.x} ${playerData.childNode!!.y} ${playerData.childNode!!.z}'><hover:show_text:'<green>Click to copy!</green>'>{ $childCords }</hover></click></gray> \n" +
                        "<green>Status:</green> <gray>Enabled</gray>"
            )
            event.player.sendMessage(message)
        }

    }

    @Suppress("UNCHECKED_CAST")
    @EventHandler(priority = EventPriority.MONITOR)
    private fun onPortalTeleport(event: PlayerMoveEvent) {
        val blockLocal = event.player.location
        val newBlockLocal = Location(blockLocal.world, blockLocal.x, (blockLocal.y - 0.75), blockLocal.z)
        val block = newBlockLocal.block
        if (block.type != Material.END_PORTAL_FRAME) return
        val portalBlocks = Configs.PORTAL_BLOCKS.config as WLYamlConfig

        if (portalBlocks.getProperty(block.location.serialize().toString().replace(".", ":")) == null) return

        val property: MutableMap<String, Any> =
            portalBlocks.getProperty(block.location.serialize().toString().replace(".", ":")) as MutableMap<String, Any>
        val masterNode: Location = Location.deserialize(property["masterNode"] as Map<String, Any>)
        val childNode: Location = Location.deserialize(property["childNode"] as Map<String, Any>)
        val portalID = property["id"] as String

        var isChild = false
        var isMaster = false

        if (block.location == masterNode) isMaster = true
        if (block.location == childNode) isChild = true

        val settingsConfig = Configs.SETTINGS.config as WLYamlConfig
        val portalSettingsMap: Map<String, Any> = settingsConfig.getProperty(portalID) as Map<String, Any>

        val settings = PortalSettings(
            isEnabled = portalSettingsMap["Enabled"] as Boolean,
            sound = portalSettingsMap["Teleport-Sound"] as Boolean,
            effect = portalSettingsMap["Teleport-Effect"] as Boolean,
            teleportDelay = portalSettingsMap["Teleport-Delay"] as Int,
            teleportEntities = portalSettingsMap["Teleport-Entities"] as Boolean,
            teleportItems = portalSettingsMap["Teleport-Items"] as Boolean,
            childTeleportation = portalSettingsMap["Child-Teleportation"] as Boolean
        )

        if (settings.isEnabled) {
            if (CooldownManager.isOnCooldown(settings.uuid)) return
            if (isChild) {
                Pads.getPlugin().server.pluginManager.callEvent(
                    PlayerPortalBlockTeleportEvent(
                        event.player,
                        TeleportBlockType.CHILD,
                        settings.sound
                    )
                )
                val playerPrev = event.player.location
                val location = Location(masterNode.world, masterNode.x, (masterNode.y + 1.25), masterNode.z)
                event.player.teleport(location)
                val playerNow = event.player.location

                val message = MiniMessage.miniMessage().deserialize(
                    "<rainbow>!--------------------${portalID}-Portal--------------------!</rainbow> \n" +
                        "<green>Teleported to master portal!</green>" +
                        "<gold>From:</gold> <gray>{ X: ${playerPrev.x}, Y: ${playerPrev.y}, Z: ${playerPrev.z} }</gray> \n" +
                        "<gold>To:</gold> <gray>{ X: ${playerNow.x}, Y: ${playerNow.y}, Z: ${playerNow.z} }</gray> \n"
                )
                event.player.sendMessage(message)
            }


            if (isMaster) {
                Pads.getPlugin().server.pluginManager.callEvent(
                    PlayerPortalBlockTeleportEvent(
                        event.player,
                        TeleportBlockType.MASTER,
                        settings.sound
                    )
                )

                val playerPrev = event.player.location
                val location = Location(childNode.world, childNode.x, (childNode.y + 1.25), childNode.z)
                event.player.teleport(location)
                val playerNow = event.player.location

                val message = MiniMessage.miniMessage().deserialize(
                    "<rainbow>!--------------------${portalID}-Portal--------------------!</rainbow>" +
                        "<green>Teleported to child portal!</green>" +
                        "<gold>From:</gold> <gray>{ X: ${playerPrev.x}, Y: ${playerPrev.y}, Z: ${playerPrev.z} }</gray>" +
                        "<gold>To:</gold> <gray>{ X: ${playerNow.x}, Y: ${playerNow.y}, Z: ${playerNow.z} }</gray>"
                )
                event.player.sendMessage(message)

            }
            CooldownManager.applyCooldown(settings.uuid, settings.teleportDelay)
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun handleTeleportEffects(event: PlayerPortalBlockTeleportEvent) {
        if (event.portalType != TeleportBlockType.MASTER) return

        fun playSound() {
            val sound: Sound = Sound.sound(org.bukkit.Sound.ENTITY_PLAYER_TELEPORT, Sound.Source.PLAYER, 1.0f, 1.0f)
            event.player.playSound(sound)
            event.player.addPotionEffect(PotionEffect(PotionEffectType.CONFUSION, 3 * 20, 2, true, false ))

        }

        if (event.playerSound) {
            playSound()
        }

    }

}
