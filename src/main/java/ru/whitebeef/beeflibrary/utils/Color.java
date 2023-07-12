package ru.whitebeef.beeflibrary.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

public class Color {
    public static final String HEX_PATTERN = "#([0-9A-Fa-f])([0-9A-Fa-f])([0-9A-Fa-f])([0-9A-Fa-f])([0-9A-Fa-f])([0-9A-Fa-f])";

    @NotNull
    public static String applyHex(@NotNull String content) {
        return content.replaceAll(HEX_PATTERN, "&x&$1&$2&$3&$4&$5&$6");
    }

    @NotNull
    public static String colorize(@NotNull String s) {
        s = applyHex(s);
        s = ChatColor.translateAlternateColorCodes('&', s);
        return s;
    }

    @NotNull
    public static Component colorize(@NotNull Component component) {
        component = component.replaceText(TextReplacementConfig.builder()
                .match("#([0-9A-Fa-f]{2})([0-9A-Fa-f]{2})([0-9A-Fa-f]{2})")
                .replacement((matchResult, builder) -> Component.text(matchResult.group())
                        .color(TextColor.color(
                                Integer.parseInt(matchResult.group(1), 16),
                                Integer.parseInt(matchResult.group(2), 16),
                                Integer.parseInt(matchResult.group(3), 16))))
                .build());

        component = MiniMessage.miniMessage().deserialize(MiniMessage.miniMessage().serialize(component));
        return component;
    }
}
