package ru.whitebeef.beeflibrary.chat;

import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;

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

}
