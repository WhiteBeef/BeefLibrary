package ru.whitebeef.beeflibrary.handlers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import ru.whitebeef.beeflibrary.annotations.AnnotationPreprocessor;
import ru.whitebeef.beeflibrary.annotations.LoadType;

public final class PluginEnableHandler implements Listener {

    @EventHandler
    public void onPluginLoad(PluginEnableEvent event) {
        AnnotationPreprocessor.getInstance().scanPlugin(event.getPlugin(), LoadType.AFTER_ENABLE);
    }
}
