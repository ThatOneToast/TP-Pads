package dev.toast.events

import dev.toast.portals.TeleportBlockType
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

@Suppress("unused")
class PlayerPortalBlockTeleportEvent(
    val player: Player,
    val portalType: TeleportBlockType,
    val playerSound: Boolean,
    private var isCancelled: Boolean = false
): Event(), Cancellable {

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

    override fun isCancelled(): Boolean {
        return isCancelled
    }

    override fun setCancelled(cancel: Boolean) {
        this.isCancelled = cancel
    }

}

