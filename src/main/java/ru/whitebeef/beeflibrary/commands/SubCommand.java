package ru.whitebeef.beeflibrary.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import ru.whitebeef.beeflibrary.BeefLibrary;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public abstract class SubCommand extends AbstractCommand {

    public SubCommand(String name, String permission, String description, String usageMessage, boolean onlyForPlayers,
                      BiConsumer<CommandSender, String[]> onCommand,
                      BiFunction<CommandSender, String[], List<String>> onTabComplete,
                      Map<String, AbstractCommand> subCommands,
                      List<Alias> aliases, int minArgsCount) {
        super(name, permission, description, usageMessage, onlyForPlayers, onCommand, onTabComplete, subCommands, aliases, minArgsCount);
    }

    @Override
    public void register(Plugin plugin) {
        BeefLibrary.getInstance().getLogger().severe("SubCommands can't be registered! You try to register " + getClass().getName());
    }

    @Override
    public String toString() {
        return getName();
    }
}
