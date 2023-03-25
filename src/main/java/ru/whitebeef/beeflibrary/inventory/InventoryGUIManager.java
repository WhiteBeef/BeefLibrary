package ru.whitebeef.beeflibrary.inventory;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class InventoryGUIManager {
    public static InventoryGUIManager instance;

    public static InventoryGUIManager getInstance() {
        return instance;
    }

    public static void setInstance(InventoryGUIManager instance) {
        InventoryGUIManager.instance = instance;
    }

    private final HashMap<Player, IInventoryGUI> openInventoryGUI = new HashMap<>();
    private final HashMap<String, IInventoryGUI.Builder> inventoryTemplates = new HashMap<>();

    public void registerTemplate(Plugin plugin, @NotNull IInventoryGUI.Builder builder) {
        inventoryTemplates.put(plugin.getName().toLowerCase() + "." + builder.getNamespace(), builder);
    }

    /**
     * @param namespace don't forget put plugin name with point to namespace
     */
    @NotNull
    public IInventoryGUI createGUI(@NotNull String namespace) {
        if (!inventoryTemplates.containsKey(namespace)) {
            throw new IllegalArgumentException("Namespace " + namespace + " is not registered!");
        }
        return inventoryTemplates.get(namespace).build();
    }

    @Nullable
    public IInventoryGUI getOpenedInventory(@NotNull Player player) {
        return openInventoryGUI.get(player);
    }

    /**
     * Using package-private modifier to hide this method from outside eyes
     */
    void addOpenInventory(@NotNull Player player, @NotNull IInventoryGUI inventoryGUI) {
        openInventoryGUI.put(player, inventoryGUI);
    }

    /**
     * Using package-private modifier to hide this method from outside eyes
     */
    void removeOpenInventory(@NotNull Player player) {
        openInventoryGUI.remove(player);
    }

}
