package dev.toast.events

import dev.toast.portals.PortalSettings
import dev.toast.portals.TeleportBlockType
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class PlayerPortalBlockPlace (
    val player: Player,
    val portalType: TeleportBlockType,
    val portal: Block,
    val portalID: String,
    val portalSettings: PortalSettings
    ): Event() {

        override fun getHandlers(): HandlerList {
            return HANDLER_LIST
        }

        companion object {
            @JvmStatic
            private val HANDLER_LIST = HandlerList()

            @JvmStatic
            fun getHandlerList(): HandlerList {
                return HANDLER_LIST
            }

        }
}

