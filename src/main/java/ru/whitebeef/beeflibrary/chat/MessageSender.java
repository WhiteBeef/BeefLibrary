package ru.whitebeef.beeflibrary.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import redis.clients.jedis.JedisPubSub;
import ru.whitebeef.beeflibrary.BeefLibrary;
import ru.whitebeef.beeflibrary.placeholderapi.PAPIUtils;
import ru.whitebeef.beeflibrary.utils.Color;
import ru.whitebeef.beeflibrary.utils.JedisUtils;

import java.util.UUID;

public class MessageSender {

    private static final String CHANNEL_NAME = "multiServerMessage";
    private static MessageSender instance;

    public static MessageSender getInstance() {
        return instance;
    }


    public static void sendMessage(CommandSender sender, Component component) {
        sender.sendMessage(component);
    }

    public static void sendMessage(CommandSender sender, String message) {
        sendMessage(sender, MessageFormatter.of(message).toComponent(sender));
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

    public static void sendActionBar(Player player, String message) {
        sendActionBar(player, MessageFormatter.of(message).toComponent(player));
    }

    public static void sendActionBar(Player player, Component str) {
        player.sendActionBar(str);
    }

    public static void sendMultiServerMessage(UUID playerUuid, String message) {
        JedisUtils.jedisSend(BeefLibrary.getInstance(), CHANNEL_NAME, playerUuid.toString() + ";" + message);

    }

    public static void sendMultiServerMessage(UUID playerUuid, Component message) {
        JedisUtils.jedisSend(BeefLibrary.getInstance(), CHANNEL_NAME, playerUuid.toString() + ";" + message);

    }

    public static void sendMultiServerMessage(String name, String message) {
        JedisUtils.jedisSend(BeefLibrary.getInstance(), CHANNEL_NAME, name + ";" + message);
    }

    public static void sendMultiServerMessage(String name, Component message) {
        JedisUtils.jedisSend(BeefLibrary.getInstance(), CHANNEL_NAME, name + ";" + GsonComponentSerializer.gson().serialize(message));
    }

    public MessageSender() {
        instance = this;


        JedisUtils.subscribe(BeefLibrary.getInstance(), CHANNEL_NAME, new JedisPubSub() {
            @Override
            public void onMessage(String channel, String message) {

                if (message.startsWith(BeefLibrary.getServerUuid().toString())) {
                    return;
                }
                String[] splitMessage = message.split(";", 3);
                if (splitMessage.length < 3) {
                    throw new IllegalStateException("Cannot transform redis message to 'serverName;sender;jsonMessage': " + message);
                }
                if (!Bukkit.getServer().getName().equals(splitMessage[0])) {
                    if (Bukkit.getPlayer(splitMessage[1]) == null) {
                        return;
                    }
                    sendMessage(splitMessage[1], GsonComponentSerializer.gson().deserialize(splitMessage[2]));
                }

            }
        });
    }


    public void sendMultiServerMessage(CommandSender sender, String message) {
        sendMessage(sender, LegacyComponentSerializer.legacySection().deserialize(message));
    }

    public void sendMessage(String recipient, Component message) {

        Player recipientPlayer = Bukkit.getPlayer(recipient);
        if (recipientPlayer == null) {
            return;
        }

        MessageSender.sendMessage(recipientPlayer, Color.colorize(PAPIUtils.setPlaceholders(recipientPlayer, PAPIUtils.replaceBiPlaceholders(message, null, recipientPlayer))));
    }

}
