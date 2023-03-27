package ru.whitebeef.beeflibrary.commands;

import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public final class SimpleCommand extends AbstractCommand {

    public static SimpleCommand newInstance(String name, String permission) {
        return new SimpleCommand(name, permission, "", "", false, null, null, new HashMap<>(), Collections.emptyList(), 0);
    }

    public static SimpleCommand newInstance(String name) {
        return newInstance(name, "");
    }

    public SimpleCommand(String name, String permission, String description, String usageMessage, boolean onlyForPlayers,
                         BiConsumer<CommandSender, String[]> onCommand,
                         BiFunction<CommandSender, String[], List<String>> onTabComplete,
                         Map<String, AbstractCommand> subCommands,
                         List<Alias> aliases, int minArgsCount) {
        super(name, permission, description, usageMessage, onlyForPlayers, onCommand, onTabComplete, subCommands, aliases, minArgsCount);
    }

}
