package ru.whitebeef.beeflibrary.commands;

import org.bukkit.command.CommandSender;
import ru.whitebeef.beeflibrary.chat.MessageFormatter;
import ru.whitebeef.beeflibrary.chat.MessageType;

import java.util.function.BiConsumer;

public enum StandardConsumers {

    NO_PERMISSION((sender, args) -> {
        sender.sendMessage(MessageFormatter.of(MessageType.of("beeflibrary.no_permissions")).toComponent(sender));
    }),

    ONLY_FOR_PLAYERS((sender, args) -> {
        sender.sendMessage(MessageFormatter.of(MessageType.of("beeflibrary.only_for_players")).toComponent(sender));
    }),

    NO_ARGS((sender, args) -> {
        sender.sendMessage(MessageFormatter.of(MessageType.of("beeflibrary.no_args")).toComponent(sender));
    });

    private final BiConsumer<CommandSender, String[]> biConsumer;


    StandardConsumers(BiConsumer<CommandSender, String[]> biConsumer) {
        this.biConsumer = biConsumer;
    }

    public BiConsumer<CommandSender, String[]> getConsumer() {
        return biConsumer;
    }
}
