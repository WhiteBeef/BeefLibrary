package ru.whitebeef.beeflibrary.commands;

import org.bukkit.command.CommandSender;
import ru.whitebeef.beeflibrary.chat.MessageSender;

import java.util.function.BiConsumer;

public enum StandardConsumers {

    NO_PERMISSION((sender, args) -> {
        MessageSender.sendMessageType(sender, "beeflibrary.no_permissions");
    }),

    ONLY_FOR_PLAYERS((sender, args) -> {
        MessageSender.sendMessageType(sender, "beeflibrary.only_for_players");
    }),

    COOLDOWN((sender, args) -> {
        MessageSender.sendMessageType(sender, "beeflibrary.cooldown");
    }),

    NO_ARGS((sender, args) -> {
        MessageSender.sendMessageType(sender, "beeflibrary.no_args");
    });

    private final BiConsumer<CommandSender, String[]> biConsumer;


    StandardConsumers(BiConsumer<CommandSender, String[]> biConsumer) {
        this.biConsumer = biConsumer;
    }

    public BiConsumer<CommandSender, String[]> getConsumer() {
        return biConsumer;
    }
}
