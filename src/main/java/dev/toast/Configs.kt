package dev.toast

import dev.toast.configs.WLConfig

enum class Configs(val config: WLConfig) {
    SETTINGS(Pads.getConfigManager().getConfig("Settings")),
    PORTALS(Pads.getConfigManager().getConfig("Portals")),
    PORTAL_BLOCKS(Pads.getConfigManager().getConfig("PortalBlocks")),


    ;
}
