package net.aruneko.inventorysorter

import org.bukkit.Material
import org.bukkit.Server
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin

class InventorySorterListeners(private val plugin: Plugin, private val server: Server) : Listener {
    private fun hasInventory(block: Block) : Boolean {
        return when(block.type) {
            Material.CHEST,
            Material.ENDER_CHEST,
            Material.TRAPPED_CHEST,
            Material.SHULKER_BOX,
            Material.DISPENSER,
            Material.DROPPER,
            Material.HOPPER -> true
            else -> false
        }
    }

    private fun restackItems(items: List<ItemStack>) : List<ItemStack> {
        val item = items.first()
        val maxStack = item.maxStackSize
        val totalItems = items.map { it.amount }.sum()
        val maxStackItems = List(totalItems / maxStack){ _ -> ItemStack(item.type, maxStack) }
        val remainderItems = List(1){ _ -> ItemStack(item.type, totalItems % maxStack) }
        return maxStackItems.plus(remainderItems)
    }

    private fun sortInventory(inventory: Inventory) : Array<ItemStack> {
        val allItems = inventory.contents
        val nonNullItems = allItems.filter { item -> item != null }
        val sortedItems = nonNullItems.sortedBy { it.type }
        val groupedItems = sortedItems.groupBy { it.type }.map {
            (_, items) -> when(items.first().maxStackSize) {
                // スタックできないアイテムはそのまま
                1 -> items
                // スタックできるアイテムはまとめる
                else -> restackItems(items)
            }
        }.flatten()
        val nullItems = Array(allItems.size - groupedItems.size){ _ -> ItemStack(Material.AIR) }
        return groupedItems.toTypedArray().plus(nullItems)
    }

    @EventHandler
    fun onPlayerInteractEvent(event: PlayerInteractEvent) {
        val player = event.player
        if (!player.isSneaking or (event.action != Action.RIGHT_CLICK_BLOCK)) {
            return
        }
        if (!hasInventory(event.clickedBlock)) {
            return
        }

        val inventoryBlock = event.clickedBlock.state as InventoryHolder
        val inventory = inventoryBlock.inventory
        val sortedInventory = sortInventory(inventory)
        inventory.contents = sortedInventory
    }
}