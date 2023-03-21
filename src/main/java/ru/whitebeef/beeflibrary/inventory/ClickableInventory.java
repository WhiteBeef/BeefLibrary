package ru.whitebeef.beeflibrary.inventory;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public interface ClickableInventory {

    void onClick(int slot, InventoryClickEvent event);

    void onDrag(int slot, InventoryDragEvent event);

}
