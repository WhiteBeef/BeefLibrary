package ru.whitebeef.beeflibrary.handlers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import ru.whitebeef.beeflibrary.annotations.InitAnnotationProcessor;
import ru.whitebeef.beeflibrary.chat.MessageType;
import ru.whitebeef.beeflibrary.commands.AbstractCommand;

public class PluginHandler implements Listener {

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        InitAnnotationProcessor.scanElements(event.getPlugin());
        MessageType.registerTypesSection(event.getPlugin(), "messages");
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        MessageType.unregisterTypesSection(event.getPlugin());
        AbstractCommand.unregisterAllCommands(event.getPlugin());
    }
}
