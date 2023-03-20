package ru.whitebeef.beeflibrary.inventory.deprecated;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import ru.whitebeef.beeflibrary.BeefLibrary;
import ru.whitebeef.beeflibrary.utils.Color;

import java.util.List;

@Deprecated
public class OldInventoryGUIHandler implements Listener {

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        OldInventoryGUIManager oldInventoryGUIManager = OldInventoryGUIManager.getInstance();
        Player player = (Player) event.getPlayer();
        if (!oldInventoryGUIManager.getOpenInventories().containsKey(player.getName())) {
            return;
        }
        oldInventoryGUIManager.close(player, event.getInventory(), event.getReason());
    }


    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        try {
            OldInventoryGUIManager oldInventoryGUIManager = OldInventoryGUIManager.getInstance();
            Player player = (Player) event.getWhoClicked();
            if (!oldInventoryGUIManager.getOpenInventories().containsKey(player.getName())) {
                return;
            }
            if (event.isShiftClick()) {
                event.setCancelled(true);
                return;
            }
            if (event.getClickedInventory() != event.getInventory()) {
                return;
            }
            int slot = event.getSlot();
            OldInventoryGUI oldInventoryGUI = oldInventoryGUIManager.getOpenInventories().get(player.getName());

            if (!oldInventoryGUI.isChangeableSlot(slot) && event.getInventory().equals(event.getClickedInventory())) {
                event.setCancelled(true);
            }
            oldInventoryGUI.onClick(event);


            List<String> commands = oldInventoryGUI.getCommands(player, slot);
            if (commands != null && !commands.isEmpty() && event.getView().getTopInventory().equals(event.getClickedInventory())) {
                for (String command : commands) {
                    if (command == null) {
                        continue;
                    } else if (command.startsWith("close")) {
                        player.closeInventory();
                        continue;
                    } else if (command.equalsIgnoreCase("return")) {
                        oldInventoryGUI.returnItems(event.getView().getTopInventory(), (Player) event.getWhoClicked());
                        continue;
                    } else if (command.startsWith("openInv")) {
                        oldInventoryGUIManager.open(player, command.replace("openInv ", ""));
                        continue;
                    } else if (command.equalsIgnoreCase("clear")) {
                        player.getOpenInventory().getTopInventory().setContents(oldInventoryGUI.getInventory(player).getContents());
                        continue;
                    } else if (command.equalsIgnoreCase("openPrevious")) {
                        OldInventoryGUI previousOldInventoryGUI = oldInventoryGUI.getPreviousInventory();
                        if (previousOldInventoryGUI != null) {
                            oldInventoryGUIManager.open(player, previousOldInventoryGUI);
                        }
                        continue;
                    }

                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), BeefLibrary.getInstance().isPlaceholderAPIHooked() ? PlaceholderAPI.setPlaceholders(player, command) : command);
                }
            }
        } catch (Exception ex) {
            event.getWhoClicked().sendMessage(Color.colorize("&cПроизошла непредвиденная ошибка!\nЕсли вы увидели это сообщение, свяжитесь с администрацией! Код ошибки: #0001"));
            event.setCancelled(true);
            ex.printStackTrace();
        }
    }


    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        try {
            OldInventoryGUIManager oldInventoryGUIManager = OldInventoryGUIManager.getInstance();

            Player player = (Player) event.getWhoClicked();
            if (!oldInventoryGUIManager.getOpenInventories().containsKey(player.getName())) {
                return;
            }
            OldInventoryGUI oldInventoryGui = oldInventoryGUIManager.getOpenInventories().get(player.getName());
            event.getNewItems().forEach((slot, item) -> {
                if (!oldInventoryGui.isChangeableSlot(slot)) {
                    event.setCancelled(true);
                }
            });
            oldInventoryGui.onDrag(event);

        } catch (Exception ex) {
            event.getWhoClicked().sendMessage(Color.colorize("&cПроизошла непредвиденная ошибка!\nЕсли вы увидели это сообщение, свяжитесь с администрацией! Код ошибки: #0002"));
            event.setCancelled(true);
            ex.printStackTrace();
        }
    }
}
