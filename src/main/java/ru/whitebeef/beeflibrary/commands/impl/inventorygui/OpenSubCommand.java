package ru.whitebeef.beeflibrary.commands.impl.inventorygui;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import ru.whitebeef.beeflibrary.chat.MessageSender;
import ru.whitebeef.beeflibrary.chat.MessageType;
import ru.whitebeef.beeflibrary.commands.AbstractCommand;
import ru.whitebeef.beeflibrary.commands.Alias;
import ru.whitebeef.beeflibrary.commands.SubCommand;
import ru.whitebeef.beeflibrary.inventory.InventoryGUIManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class OpenSubCommand extends SubCommand {

    public OpenSubCommand(String name, String permission, String description, String usageMessage, boolean onlyForPlayers, BiConsumer<CommandSender, String[]> onCommand, BiFunction<CommandSender, String[], List<String>> onTabComplete, Map<String, AbstractCommand> subCommands, List<Alias> aliases, int minArgsCount) {
        super(name, permission, description, usageMessage, onlyForPlayers, onCommand, onTabComplete, subCommands, aliases, minArgsCount);
    }

    @Override
    protected void onCommand(CommandSender sender, String[] args) {
        Player player = Bukkit.getPlayer(args[0]);
        if (player == null) {
            MessageSender.sendMessage(sender, MessageType.of("player_not_found").replaceAll("%player%", args[0]));
            return;
        }

        if (!InventoryGUIManager.getInstance().isRegisteredTemplate(args[1])) {
            MessageSender.sendMessage(sender, MessageType.of("inventorygui_not_found").replaceAll("%namespace%", args[1]));
            return;
        }

        InventoryGUIManager.getInstance().openTemplate(player, args[1]);
    }

    @Override
    protected List<String> onTabComplete(CommandSender sender, String[] args) {
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        }
        if (args.length == 2) {
            return new ArrayList<>(InventoryGUIManager.getInstance().getRegisteredTemplateNames());
        }
        return super.onTabComplete(sender, args);
    }
}
