package ru.whitebeef.beeflibrary.utils;

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

}
