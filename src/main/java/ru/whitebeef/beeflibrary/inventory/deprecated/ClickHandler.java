package ru.whitebeef.beeflibrary.inventory.deprecated;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
@Deprecated
public interface ClickHandler {

    default void onClick(InventoryClickEvent event) {
    }

    default void onDrag(InventoryDragEvent event) {
    }
}
