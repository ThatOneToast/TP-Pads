package dev.toast.portals

import org.bukkit.Location

data class PortalData(
    var id: String? = null,
    var masterNode: Location? = null,
    var childNode: Location? = null
)
