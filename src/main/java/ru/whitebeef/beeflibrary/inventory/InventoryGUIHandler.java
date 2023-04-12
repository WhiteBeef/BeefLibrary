package ru.whitebeef.beeflibrary.inventory;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import ru.whitebeef.beeflibrary.utils.InventoryUtils;

public class InventoryGUIHandler implements Listener {

    @EventHandler
    public void onClickInventory(InventoryClickEvent event) {
        InventoryGUIManager manager = InventoryGUIManager.getInstance();
        IInventoryGUI inventoryGUI = manager.getOpenedInventory((Player) event.getWhoClicked());
        if (inventoryGUI == null) {
            return;
        }
        if (event.getClickedInventory() == null) {
            return;
        }
        if (event.getClickedInventory() == event.getView().getTopInventory()) {
            inventoryGUI.onClick(event);
            return;
        }

        if (event.isShiftClick()) {
            event.setCancelled(true);
            for (int slot = 0; slot < inventoryGUI.getSize(); slot++) {
                if (!inventoryGUI.isSlotClosed(slot)) {
                    event.getCurrentItem().setAmount(event.getCurrentItem().getAmount()
                            - InventoryUtils.addAsMaxAsPossible(event.getInventory(), slot, event.getCurrentItem(), 64));
                }
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        InventoryGUIManager manager = InventoryGUIManager.getInstance();
        IInventoryGUI inventoryGUI = manager.getOpenedInventory((Player) event.getWhoClicked());
        if (inventoryGUI == null) {
            return;
        }

        inventoryGUI.onDrag(event);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getReason() == InventoryCloseEvent.Reason.PLUGIN) {
            return;
        }
        InventoryGUIManager manager = InventoryGUIManager.getInstance();
        IInventoryGUI inventoryGUI = manager.getOpenedInventory((Player) event.getPlayer());
        if (inventoryGUI == null) {
            return;
        }
        manager.removeOpenInventory((Player) event.getPlayer());

        inventoryGUI.onClose(event);
    }
}
