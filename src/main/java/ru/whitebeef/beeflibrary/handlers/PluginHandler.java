package ru.whitebeef.beeflibrary.handlers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import ru.whitebeef.beeflibrary.chat.MessageType;
import ru.whitebeef.beeflibrary.commands.AbstractCommand;
import ru.whitebeef.beeflibrary.inventory.InventoryGUIManager;
import ru.whitebeef.beeflibrary.utils.SoundType;

public class PluginHandler implements Listener {

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        MessageType.registerTypesSection(event.getPlugin(), "messages");
        SoundType.registerTypesSection(event.getPlugin(), "sounds");
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        InventoryGUIManager.getInstance().unregisterTemplates(event.getPlugin());
        SoundType.unregisterTypesSection(event.getPlugin());
        AbstractCommand.unregisterAllCommands(event.getPlugin());
        MessageType.unregisterTypesSection(event.getPlugin());
    }
}
