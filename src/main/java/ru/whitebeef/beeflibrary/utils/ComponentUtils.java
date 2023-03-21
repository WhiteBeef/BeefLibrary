package ru.whitebeef.beeflibrary.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.command.CommandSender;
import ru.whitebeef.beeflibrary.chat.MessageFormatter;

public class ComponentUtils {
    public static Component replaceTellPlaceholders(Component component, CommandSender sender, CommandSender recipient) {
        component = component
                .replaceText(TextReplacementConfig.builder()
                        .match("%(sender|recipient){1}\\-([A-z\\-\\_]+)%")
                        .replacement((matchResult, builder) -> {
                            MessageFormatter formatter = MessageFormatter.of("%" + matchResult.group(2) + "%");
                            return matchResult.group(1).equals("sender") ? formatter.toComponent(sender) : formatter.toComponent(recipient);
                        })
                        .build());
        return component;
    }

}
