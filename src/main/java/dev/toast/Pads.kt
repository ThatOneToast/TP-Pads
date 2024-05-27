package dev.toast

import dev.toast.configs.ConfigManager
import dev.toast.configs.WLYamlConfig
import dev.toast.portals.GetTPBlock
import dev.toast.portals.PortalData
import dev.toast.portals.PortalManager
import dev.toast.portals.PortalSettings
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import java.nio.ByteBuffer
import java.security.MessageDigest
import java.util.*

fun combineUUIDs(
    uuid1: UUID,
    uuid2: UUID,
): UUID {
    val byteBuffer = ByteBuffer.allocate(32)
    byteBuffer.putLong(uuid1.mostSignificantBits)
    byteBuffer.putLong(uuid1.leastSignificantBits)
    byteBuffer.putLong(uuid2.mostSignificantBits)
    byteBuffer.putLong(uuid2.leastSignificantBits)
    val combinedBytes = byteBuffer.array()

    val md = MessageDigest.getInstance("SHA-1")
    val sha1Bytes = md.digest(combinedBytes)

    val msb = ByteBuffer.wrap(sha1Bytes, 0, 8).long
    val lsb = ByteBuffer.wrap(sha1Bytes, 8, 8).long

    return UUID(msb, lsb)
}

class Pads : JavaPlugin() {
    

    override fun onEnable() {
        // Plugin startup logic
        this.dataFolder.mkdirs()
        instance = this
        configManager = ConfigManager()

        val settings = WLYamlConfig(
            "Settings",
            Pads.getPlugin().dataFolder.absolutePath,
            true
        )

        val portals = WLYamlConfig(
            "Portals",
            Pads.getPlugin().dataFolder.absolutePath,
            true
        )

        val portalBlocks = WLYamlConfig(
            "PortalBlocks",
            getPlugin().dataFolder.absolutePath,
            true
        )

        settings.save()
        portals.save()
        portalBlocks.save()
        GetTPBlock()
        PortalManager()


    }



    override fun onDisable() {
        // Plugin shutdown logic
    }

    companion object {
        private lateinit var instance: Plugin
        private lateinit var configManager: ConfigManager

        @JvmStatic
        fun getPlugin(): Plugin {
            if (::instance.isInitialized) {
                return instance
            } else {
                throw IllegalStateException("Plugin not initialized")
            }
        }

        @JvmStatic
        fun getConfigManager(): ConfigManager {
            if(::configManager.isInitialized) {
                return configManager
            } else {
                throw IllegalStateException("ConfigManager not initialized")
            }
        }
    }
}


fun saveNewPortal(data: PortalData) {
    try {
        val portals = Configs.PORTALS.config as WLYamlConfig
        val nodes: MutableMap<String, Map<String, Any>> = mutableMapOf()
        nodes["Master"] = data.masterNode!!.serialize()
        nodes["Child"] = data.childNode!!.serialize()

        portals.setProperty(data.id!!, mapOf(Pair("Location", nodes)))
        portals.save()
    } catch (_: ConfigManager.ConfigurationNotFoundException) {
        val portals = WLYamlConfig(
            "Portals",
            Pads.getPlugin().dataFolder.absolutePath,
            true
        )
        val nodePair: Map<Any, Any> = mutableMapOf(data.masterNode!!.serialize() to data.childNode!!.serialize())
        portals.setProperty(data.id!!, mapOf(Pair("Location", nodePair)))
        portals.save()

    }

    try {
        val portalBlocks = Configs.PORTAL_BLOCKS.config as WLYamlConfig

        val masterNode: MutableMap<String, Any> = mutableMapOf()
        masterNode["masterNode"] = data.masterNode!!.serialize()
        masterNode["childNode"] = data.childNode!!.serialize()
        masterNode["id"] = data.id!!

        val childNode: MutableMap<String, Any> = mutableMapOf()
        childNode["masterNode"] = data.childNode!!.serialize()
        childNode["childNode"] = data.masterNode!!.serialize()
        childNode["id"] = data.id!!

        portalBlocks.setProperty(data.masterNode!!.serialize().toString().replace(".", ":"), masterNode)
        portalBlocks.setProperty(data.childNode!!.serialize().toString().replace(".", ":"), childNode)
        portalBlocks.save()
    } catch (_: Exception) {}

    try {
        val settings = Configs.SETTINGS.config as WLYamlConfig
        val pairedNodes = PortalSettings().serialize()
        settings.setProperty(data.id!!, pairedNodes)
        settings.save()

    } catch (_: Exception) {}
}
