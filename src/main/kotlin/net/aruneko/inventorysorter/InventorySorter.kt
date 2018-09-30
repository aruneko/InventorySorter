package net.aruneko.inventorysorter

import org.bukkit.plugin.java.JavaPlugin

class InventorySorter : JavaPlugin() {
    override fun onEnable() {
        server.pluginManager.registerEvents(InventorySorterListeners(this, server), this)
    }

    override fun onDisable() {}
}