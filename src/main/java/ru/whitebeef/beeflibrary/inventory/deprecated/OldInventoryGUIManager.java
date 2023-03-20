package ru.whitebeef.beeflibrary.inventory.deprecated;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import ru.whitebeef.beeflibrary.BeefLibrary;
import ru.whitebeef.beeflibrary.chat.MessageFormatter;
import ru.whitebeef.beeflibrary.utils.ItemUtils;

import java.util.ArrayList;
import java.util.HashMap;

@Deprecated
public class OldInventoryGUIManager {

    private static OldInventoryGUIManager instance;

    public static OldInventoryGUIManager getInstance() {
        return instance;
    }

    private final HashMap<String, OldInventoryGUI> loadedInventories = new HashMap<>();
    private final HashMap<String, OldInventoryGUI> openInventories = new HashMap<>();
    private final HashMap<String, OldInventoryGUI> lastClosedInventories = new HashMap<>();

    public OldInventoryGUIManager() {
        instance = this;
    }

    public HashMap<String, OldInventoryGUI> getLoadedInventories() {
        return loadedInventories;
    }

    @Nullable
    public OldInventoryGUI getLoadedInventory(String namespace) {
        return loadedInventories.get(namespace);
    }

    public HashMap<String, OldInventoryGUI> getOpenInventories() {
        return openInventories;
    }

    public OldInventoryGUI getOpenInventory(Player player) {
        return openInventories.get(player.getName());
    }

    public void addInventory(OldInventoryGUI oldInventoryGUI) {
        if (isLoaded(oldInventoryGUI.getNameSpace())) {
            BeefLibrary.getInstance().getLogger().severe("InventoryGUI '" + oldInventoryGUI.getNameSpace() + "' is already loaded! Check your config.yml to correct data! Rewriting this GUI");
        }
        loadedInventories.put(oldInventoryGUI.getNameSpace(), oldInventoryGUI);
    }


    public OldInventoryGUI open(Player player, String name) {
        OldInventoryGUI oldInventoryGUI = loadedInventories.get(name).clone();
        asyncOpenInv(player, oldInventoryGUI);
        return oldInventoryGUI;
    }

    public void open(Player player, OldInventoryGUI oldInventoryGUI) {
        asyncOpenInv(player, oldInventoryGUI);
    }

    private void asyncOpenInv(Player player, OldInventoryGUI oldInventoryGUI) {
        Bukkit.getScheduler().runTaskAsynchronously(BeefLibrary.getInstance(), () -> {
            Inventory openInv = oldInventoryGUI.getInventory(player);
            Bukkit.getScheduler().runTask(BeefLibrary.getInstance(), () -> {
                player.openInventory(openInv);
                openInventories.put(player.getName(), oldInventoryGUI);
                OldInventoryGUI last = lastClosedInventories.get(player.getName());
                if (last != null) {
                    oldInventoryGUI.setPreviousInventory(last);
                }
            });
        });
    }

    public void close(Player player, Inventory afterInventory, InventoryCloseEvent.Reason reason) {
        if (player == null) {
            return;
        }
        OldInventoryGUI oldInventoryGui = openInventories.get(player.getName());
        if (oldInventoryGui == null) {
            return;
        }
        if (reason != InventoryCloseEvent.Reason.PLUGIN) {
            lastClosedInventories.put(player.getName(), oldInventoryGui);
        }
        openInventories.remove(player.getName());

        if (player.isOnline() && reason != InventoryCloseEvent.Reason.OPEN_NEW) {
            player.closeInventory();
        }

        if (!oldInventoryGui.isForceClose()) {
            for (String command : oldInventoryGui.getCommandsOnClose()) {
                if (command.equalsIgnoreCase("return")) {
                    ArrayList<ItemStack> newItems = ItemUtils.parseItemsMapToArrayList(ItemUtils.getNewItems(oldInventoryGui, oldInventoryGui.getInventory(player), afterInventory));
                    ItemUtils.returnItems(player, newItems);
                    continue;
                } else if (command.equalsIgnoreCase("clear")) {
                    player.getOpenInventory().getTopInventory().setContents(oldInventoryGui.getInventory(player).getContents());
                    continue;
                } else if (command.startsWith("openInv")) {
                    open(player, command.replace("openInv ", ""));
                    continue;
                }
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                        MessageFormatter.of(command).replaceRegex("%player%", player.getName()).toString());
            }
        }
    }

    public void clearAllInventories() {
        closeAllInventories();
        loadedInventories.clear();
    }

    public boolean isLoaded(String namespace) {
        return getLoadedInventories().containsKey(namespace);
    }

    public void closeAllInventories() {
        for (String name : new HashMap<>(openInventories).keySet()) {
            Player player = Bukkit.getPlayer(name);
            close(player, player.getOpenInventory().getTopInventory(), InventoryCloseEvent.Reason.DISCONNECT);
        }
    }


}
