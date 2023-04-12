package ru.whitebeef.beeflibrary;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import ru.whitebeef.beeflibrary.commands.AbstractCommand;
import ru.whitebeef.beeflibrary.commands.SimpleCommand;
import ru.whitebeef.beeflibrary.commands.impl.inventorygui.OpenSubCommand;
import ru.whitebeef.beeflibrary.handlers.PluginHandler;
import ru.whitebeef.beeflibrary.inventory.CustomInventoryGUICommand;
import ru.whitebeef.beeflibrary.inventory.IInventoryGUI;
import ru.whitebeef.beeflibrary.inventory.InventoryGUIHandler;
import ru.whitebeef.beeflibrary.inventory.InventoryGUIManager;
import ru.whitebeef.beeflibrary.inventory.deprecated.OldInventoryGUIHandler;
import ru.whitebeef.beeflibrary.inventory.deprecated.OldInventoryGUIManager;
import ru.whitebeef.beeflibrary.placeholderapi.PAPIUtils;
import ru.whitebeef.beeflibrary.utils.ItemUtils;
import ru.whitebeef.beeflibrary.utils.SoundType;

import java.util.ArrayList;
import java.util.function.Function;

public final class BeefLibrary extends JavaPlugin {

    private static BeefLibrary instance;
    private boolean placeholderAPIHooked = false;

    public static BeefLibrary getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        loadConfig(this);

        tryHookPlaceholderAPI();
        registerListeners(this, new PluginHandler(), new OldInventoryGUIHandler(), new InventoryGUIHandler());

        new OldInventoryGUIManager();
        new InventoryGUIManager();
        registerCustomGUICommands();
        registerCommands();
    }

    private void registerCommands() {
        AbstractCommand.builder("inventorygui", SimpleCommand.class)
                .setPermission("beeflibrary.commands.inventorygui")
                .setMinArgsCount(1)
                .addSubCommand(AbstractCommand.builder("open", OpenSubCommand.class)
                        .setMinArgsCount(2)
                        .build())
                .build().register(this);
    }

    @Override
    public void onDisable() {
        InventoryGUIManager.getInstance().closeAllInventories();
        AbstractCommand.unregisterAllCommands(this);
    }

    public void tryHookPlaceholderAPI() {
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            placeholderAPIHooked = true;
            getLogger().info("PlaceholderAPI hooked!");
        }
    }

    public void unhookPlaceholderAPI() {
        placeholderAPIHooked = false;
        getLogger().info("PlaceholderAPI unhooked!");
    }

    public void registerCommand(AbstractCommand abstractCommand) {
        abstractCommand.register(this);
    }

    public boolean registerPlaceholders(PlaceholderExpansion... expansions) {
        if (isPlaceholderAPIHooked()) {
            for (PlaceholderExpansion expansion : expansions) {
                expansion.register();
            }
        }
        return isPlaceholderAPIHooked();
    }

    public boolean isPlaceholderAPIHooked() {
        return placeholderAPIHooked;
    }

    public static void registerListeners(Plugin plugin, Listener... listeners) {
        for (Listener listener : listeners) {
            Bukkit.getPluginManager().registerEvents(listener, plugin);
        }
    }

    public static void registerPlaceholder(Plugin plugin, String placeholder, Function<CommandSender, Component> function) {
        PAPIUtils.registerPlaceholder(plugin, placeholder, function);
    }

    public static void registerGUI(Plugin plugin, IInventoryGUI.Builder template) {
        InventoryGUIManager.getInstance().registerTemplate(plugin, template);
    }

    public static void registerGUIs(Plugin plugin, String path) {
        InventoryGUIManager.getInstance().loadInventories(plugin, path);
    }

    public static void loadConfig(Plugin plugin) {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
    }

    private void registerCustomGUICommands() {
        CustomInventoryGUICommand.setInstance(new CustomInventoryGUICommand());
        CustomInventoryGUICommand.getInstance().registerCommand("close", ((inventoryGUI, player, s) -> inventoryGUI.close()));
        CustomInventoryGUICommand.getInstance().registerCommand("sound", ((inventoryGUI, player, s) ->
                SoundType.play(player, s.replace("sound ", ""))));
        CustomInventoryGUICommand.getInstance().registerCommand("return", ((inventoryGUI, player, s) -> {
            ArrayList<ItemStack> newItems = ItemUtils.parseItemsMapToArrayList(ItemUtils.getNewItems(inventoryGUI,
                    inventoryGUI.getInventory(player), player.getOpenInventory().getTopInventory()));
            ItemUtils.returnItems(player, newItems);
        }));
        CustomInventoryGUICommand.getInstance().registerCommand("clear", ((inventoryGUI, player, s) ->
                InventoryGUIManager.getInstance().openTemplate(player, inventoryGUI.getNamespace())));
        CustomInventoryGUICommand.getInstance().registerCommand("openinv", ((inventoryGUI, player, s) ->
                InventoryGUIManager.getInstance().openTemplate(player, s.replace("openinv ", ""))));
    }

}
