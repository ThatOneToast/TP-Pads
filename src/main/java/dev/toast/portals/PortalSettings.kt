package dev.toast.portals

import java.util.*

data class PortalSettings(
    val isEnabled: Boolean = true,
    val sound: Boolean = true,
    val effect: Boolean = true,
    val teleportDelay: Int = 2,
    val teleportEntities: Boolean = true,
    val teleportItems: Boolean = true,
    val childTeleportation: Boolean = true,
    val uuid: UUID = UUID.randomUUID()
) {
    fun serialize(): Map<String, Any> {
        return mapOf(
            "Enabled" to isEnabled,
            "Teleport-Sound" to sound,
            "Teleport-Effect" to effect,
            "Teleport-Delay" to teleportDelay,
            "Teleport-Entities" to teleportEntities,
            "Teleport-Items" to teleportItems,
            "Child-Teleportation" to childTeleportation,
            "UUID" to uuid.toString()
        )
    }
}
