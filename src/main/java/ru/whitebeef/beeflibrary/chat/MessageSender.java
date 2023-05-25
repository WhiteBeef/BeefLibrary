package ru.whitebeef.beeflibrary.chat;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class MessageSender {

    public static void sendMessage(CommandSender sender, Component component) {
        sender.sendMessage(component);
    }

    public static void sendMessage(CommandSender sender, String str) {
        sendMessage(sender, MessageFormatter.of(str).toComponent(sender));
    }

    public static void sendMessageType(CommandSender sender, String messageType) {
        sendMessage(sender, MessageType.of(messageType));
    }

    public static void sendMessageType(CommandSender sender, Plugin plugin, String messageType) {
        sendMessage(sender, MessageType.of(plugin, messageType));
    }

    public static void sendActionBarMessageType(Player player, Plugin plugin, String messageType) {
        sendActionBar(player, MessageType.of(plugin, messageType));
    }

    public static void sendActionBarMessageType(Player player, String messageType) {
        sendActionBar(player, MessageType.of(messageType));
    }

    public static void sendActionBar(Player player, String str) {
        sendActionBar(player, MessageFormatter.of(str).toComponent(player));
    }

    public static void sendActionBar(Player player, Component str) {
        player.sendActionBar(str);
    }
}
