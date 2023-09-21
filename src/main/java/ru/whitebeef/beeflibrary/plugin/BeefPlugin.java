package ru.whitebeef.beeflibrary.plugin;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import ru.whitebeef.beeflibrary.BeefLibrary;
import ru.whitebeef.beeflibrary.annotations.AnnotationPreprocessor;
import ru.whitebeef.beeflibrary.chat.MessageSender;
import ru.whitebeef.beeflibrary.chat.MessageType;
import ru.whitebeef.beeflibrary.commands.AbstractCommand;
import ru.whitebeef.beeflibrary.commands.SimpleCommand;
import ru.whitebeef.beeflibrary.database.abstractions.Database;
import ru.whitebeef.beeflibrary.inventory.InventoryGUIManager;
import ru.whitebeef.beeflibrary.placeholderapi.PAPIUtils;
import ru.whitebeef.beeflibrary.utils.JedisUtils;
import ru.whitebeef.beeflibrary.utils.SoundType;

public abstract class BeefPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        super.onEnable();

        reload();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        disable();
    }

    private void disable() {
        BeefLibrary.getInstance().unregisterPlaceholders(this);
        InventoryGUIManager.getInstance().unregisterTemplates(this);
        SoundType.unregisterTypesSection(this);
        AbstractCommand.unregisterAllCommands(this);
        PAPIUtils.unregisterPlaceholders(this);
        MessageType.unregisterTypesSection(this);
        HandlerList.unregisterAll(this);
        if (JedisUtils.isJedisEnabled()) {
            JedisUtils.unSubscribe(this);
        }
        for (Database database : Database.getDatabases(this)) {
            database.close();
        }
    }

    public void reload() {
        if (getResource("config.yml") != null) {
            saveDefaultConfig();
        }
        reloadConfig();
        onDisable();

        MessageType.registerTypesSection(this, "messages");
        SoundType.registerTypesSection(this, "sounds");
        AnnotationPreprocessor.getInstance().scanPlugin(this);

        AbstractCommand.builder(getName(), SimpleCommand.class)
                .addSubCommand(AbstractCommand.builder("reload", SimpleCommand.class)
                        .setOnCommand((sender, strings) -> {
                            reload();
                            MessageSender.sendMessageType(sender, BeefLibrary.getInstance(), "success");
                        })
                        .build())
                .setPermission(getName() + ".command")
                .build().register(this);

    }

}
