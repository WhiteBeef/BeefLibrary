package ru.whitebeef.beeflibrary;

import com.rylinaux.plugman.PlugMan;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import ru.whitebeef.beeflibrary.chat.MessageType;
import ru.whitebeef.beeflibrary.commands.AbstractCommand;
import ru.whitebeef.beeflibrary.commands.SimpleCommand;
import ru.whitebeef.beeflibrary.commands.impl.inventorygui.OpenSubCommand;
import ru.whitebeef.beeflibrary.handlers.PlayerJoinQuitHandler;
import ru.whitebeef.beeflibrary.handlers.PluginHandler;
import ru.whitebeef.beeflibrary.inventory.CustomInventoryGUICommand;
import ru.whitebeef.beeflibrary.inventory.IInventoryGUI;
import ru.whitebeef.beeflibrary.inventory.InventoryGUIHandler;
import ru.whitebeef.beeflibrary.inventory.InventoryGUIManager;
import ru.whitebeef.beeflibrary.inventory.deprecated.OldInventoryGUIHandler;
import ru.whitebeef.beeflibrary.inventory.deprecated.OldInventoryGUIManager;
import ru.whitebeef.beeflibrary.inventory.impl.UpdatableInventoryGUI;
import ru.whitebeef.beeflibrary.placeholderapi.PAPIUtils;
import ru.whitebeef.beeflibrary.utils.ItemUtils;
import ru.whitebeef.beeflibrary.utils.JedisUtils;
import ru.whitebeef.beeflibrary.utils.ScheduleUtils;
import ru.whitebeef.beeflibrary.utils.SoundType;

import java.util.ArrayList;
import java.util.function.Function;

public final class BeefLibrary extends JavaPlugin {

    private static BeefLibrary instance;
    private boolean placeholderAPIHooked = false;
    private boolean isFolia = false;
    private boolean debug = false;

    public static BeefLibrary getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        BeefLibrary.instance = this;
        tryLoadWithFolia();

        loadConfig(this);

        tryHookPlaceholderAPI();

        registerListeners(this, new OldInventoryGUIHandler(), new InventoryGUIHandler(), new PluginHandler(), new PlayerJoinQuitHandler());
        PAPIUtils.unregisterAllPlaceholders();

        MessageType.registerTypesSection(this, "messages");

        new OldInventoryGUIManager();
        new InventoryGUIManager();
        registerCustomGUICommands();
        registerCommands();

        new JedisUtils();

        ScheduleUtils.runTaskLater(this, () -> {
            if (Bukkit.getPluginManager().isPluginEnabled("PlugManX")) {
                for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                    if (!plugin.getDescription().getDepend().contains("BeefLibrary") &&
                            !plugin.getDescription().getSoftDepend().contains("BeefLibrary")) {
                        continue;
                    }
                    PlugMan.getInstance().getPluginUtil().reload(plugin);
                }
            }
        }, 10L);

        debug = getConfig().getBoolean("debug");

    }

    /**
     * Tries to load class from Folia.
     * If it succeeds {@link BeefLibrary#isFolia}, field will be set to true.
     */
    private void tryLoadWithFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            isFolia = true;
            getLogger().info("Loaded with Folia!");
        } catch (Exception ignored) {
        }
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
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (!plugin.getDescription().getDepend().contains("BeefLibrary")) {
                continue;
            }
            Bukkit.getPluginManager().disablePlugin(plugin);
        }

        InventoryGUIManager.getInstance().closeAllInventories();
        AbstractCommand.unregisterAllCommands(this);
        PAPIUtils.unregisterAllPlaceholders();
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
                if (expansion != null) {
                    expansion.register();
                }
            }
        }
        return isPlaceholderAPIHooked();
    }

    public boolean unregisterPlaceholders(PlaceholderExpansion... expansions) {
        if (isPlaceholderAPIHooked()) {
            for (PlaceholderExpansion expansion : expansions) {
                if (expansion != null) {
                    expansion.unregister();
                }
            }
        }
        return isPlaceholderAPIHooked();
    }

    public boolean isPlaceholderAPIHooked() {
        return placeholderAPIHooked;
    }

    /**
     * Returns true if plugin is running on Folia (or Folia-based fork).
     * Otherwise, returns false, which means that plugin is running on Paper (or Paper-based fork).
     *
     * @return true if plugin is running on Folia (or Folia-based fork), otherwise false.
     */
    public boolean isFolia() {
        return isFolia;
    }

    public static void registerListeners(Plugin plugin, Listener... listeners) {
        for (Listener listener : listeners) {
            Bukkit.getPluginManager().registerEvents(listener, plugin);
        }
    }

    public static void registerPlaceholder(Plugin plugin, String
            placeholder, Function<CommandSender, Component> function) {
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

    public boolean isDebug() {
        return debug;
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
        CustomInventoryGUICommand.getInstance().registerCommand("update", ((inventoryGUI, player, s) -> {
            if (inventoryGUI instanceof UpdatableInventoryGUI updatableInventoryGUI) {
                updatableInventoryGUI.update();
            }
        }
        ));
    }
}
