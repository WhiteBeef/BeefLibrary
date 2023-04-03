package ru.whitebeef.beeflibrary.inventory;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class InventoryGUIManager {
    public static InventoryGUIManager instance;

    public static InventoryGUIManager getInstance() {
        return instance;
    }

    private final HashMap<Player, IInventoryGUI> openInventoryGUI = new HashMap<>();
    private final HashMap<String, IInventoryGUI.Builder> inventoryTemplates = new HashMap<>();

    public InventoryGUIManager() {
        instance = this;
    }

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

    @NotNull
    public Collection<IInventoryGUI.Builder> getRegisteredTemplates() {
        return inventoryTemplates.values();
    }

    @NotNull
    public Set<String> getRegisteredTemplateNames() {
        return inventoryTemplates.keySet();
    }

    public void closeInventories(Plugin plugin) {
        for (var entry : openInventoryGUI.entrySet()) {
            if (entry.getValue().getNamespace().startsWith(plugin.getName().toLowerCase())) {
                entry.getKey().closeInventory(InventoryCloseEvent.Reason.PLUGIN);
            }
        }
    }

    public void unregisterTemplates(Plugin plugin) {
        closeInventories(plugin);

        List<String> toRemove = new ArrayList<>();
        for (String namespace : inventoryTemplates.keySet()) {
            if (namespace.startsWith(plugin.getName().toLowerCase())) {
                toRemove.add(namespace);
            }
        }

        for (String namespace : toRemove) {
            inventoryTemplates.remove(namespace);
        }
    }

    public boolean isRegisteredTemplate(Plugin plugin, String namespace) {
        return inventoryTemplates.containsKey(plugin.getName().toLowerCase() + "." + namespace);
    }

    public void openTemplate(Player player, Plugin plugin, String namespace) {
        if (isRegisteredTemplate(plugin, namespace)) {
            inventoryTemplates.get(plugin.getName().toLowerCase() + "." + namespace).build().open(player);
        }
    }

    public boolean isRegisteredTemplate(String namespace) {
        return inventoryTemplates.containsKey(namespace);
    }

    public void openTemplate(Player player, String namespace) {
        if (isRegisteredTemplate(namespace)) {
            inventoryTemplates.get(namespace).build().open(player);
        }
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
