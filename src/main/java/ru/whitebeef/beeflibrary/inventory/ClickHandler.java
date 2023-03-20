package ru.whitebeef.beeflibrary.inventory;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;

public interface ClickHandler {

    default void onClick(InventoryClickEvent event) {
    }

    default void onDrag(InventoryDragEvent event) {
    }
}
