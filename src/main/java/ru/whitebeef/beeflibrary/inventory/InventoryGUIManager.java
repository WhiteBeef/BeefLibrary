package ru.whitebeef.beeflibrary.inventory;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.whitebeef.beeflibrary.utils.ItemGenerateProperties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;

public class InventoryGUIManager {
    public static InventoryGUIManager instance;

    public static InventoryGUIManager getInstance() {
        return instance;
    }

    private final HashMap<UUID, IInventoryGUI> openInventoryGUI = new HashMap<>();
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
        return openInventoryGUI.get(player.getUniqueId());
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
                Player player = Bukkit.getPlayer(entry.getKey());
                if (player == null) {
                    continue;
                }
                player.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
            }
        }
    }

    public void closeAllInventories() {
        for (var entry : openInventoryGUI.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player == null) {
                continue;
            }
            player.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
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

    public void loadInventory(Plugin plugin, ConfigurationSection section)  {
        IInventoryGUI.Builder builder = IInventoryGUI.builder(section.getName(), section.getInt("size"));

        builder.setName(section.getString("name"));
        for (String slotStr : section.getConfigurationSection("slots").getKeys(false)) {
            ConfigurationSection slotSection = section.getConfigurationSection("slots" + "." + slotStr);
            int slot = Integer.parseInt(slotStr);
            if (slotSection.isString("permission")) {
                String permission = slotSection.getString("permission");

                if (slotSection.isConfigurationSection("hasPermission")) {
                    if (slotSection.isList("hasPermission.commands")) {
                        slotSection.getStringList("hasPermission.commands").forEach(command ->
                                builder.addClickConsumer(slot, (player) -> player.hasPermission(permission), (inventoryGUI, player) -> {
                                    CustomInventoryGUICommand.getInstance().runCommand(inventoryGUI, player, command);
                                }));
                    }
                }

                if (slotSection.isConfigurationSection("noPermission")) {
                    if (slotSection.isList("noPermission.commands")) {
                        slotSection.getStringList("noPermission.commands").forEach(command ->
                                builder.addClickConsumer(slot, (player) -> !player.hasPermission(permission), (inventoryGUI, player) -> {
                                    CustomInventoryGUICommand.getInstance().runCommand(inventoryGUI, player, command);
                                }));
                    }
                }

                builder.setItem(slot, player -> player.hasPermission(permission), ItemGenerateProperties.of(slotSection.getConfigurationSection("hasPermission")));
                builder.setItem(slot, player -> !player.hasPermission(permission), ItemGenerateProperties.of(slotSection.getConfigurationSection("noPermission")));

            } else {
                builder.setItem(slot, ItemGenerateProperties.of(slotSection));
                if (section.isList("slots" + "." + slotStr + ".commands")) {
                    section.getStringList("slots" + "." + slotStr + ".commands").forEach(command ->
                            builder.addClickConsumer(slot, (inventoryGUI, player) -> CustomInventoryGUICommand.getInstance().runCommand(inventoryGUI, player, command)));
                }
            }

        }

        List<BiConsumer<IInventoryGUI, Player>> closeConsumers = new ArrayList<>();

        if (section.isList("commandsOnClose")) {
            section.getStringList("commandsOnClose").forEach(command -> closeConsumers.add((inventoryGUI, player) ->
                    CustomInventoryGUICommand.getInstance().runCommand(inventoryGUI, player, command)));
        }

        builder.addCloseCommands(closeConsumers);

        registerTemplate(plugin, builder);

    }

    public void loadInventories(Plugin plugin, String path) {
        FileConfiguration cfg = plugin.getConfig();
        if (!cfg.isConfigurationSection(path)) {
            return;
        }
        for (String inventoryName : cfg.getConfigurationSection(path).getKeys(false)) {
            try {
                loadInventory(plugin, cfg.getConfigurationSection(path + "." + inventoryName));
            } catch (Exception e) {
                plugin.getLogger().severe("Error while loading InventoryGUI with namespace " + inventoryName);
                e.printStackTrace();
            }
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

    public void addOpenInventory(@NotNull Player player, @NotNull IInventoryGUI inventoryGUI) {
        openInventoryGUI.put(player.getUniqueId(), inventoryGUI);
    }

    public void removeOpenInventory(@NotNull Player player) {
        openInventoryGUI.remove(player.getUniqueId());
    }

}
