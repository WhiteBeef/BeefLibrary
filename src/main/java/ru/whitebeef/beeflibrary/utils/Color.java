package ru.whitebeef.beeflibrary.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.ChatColor;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class Color {
    public static final String HEX_PATTERN = "#([0-9A-Fa-f])([0-9A-Fa-f])([0-9A-Fa-f])([0-9A-Fa-f])([0-9A-Fa-f])([0-9A-Fa-f])";
    @RegExp
    public static final String BUKKIT_HEX_PATTERN = "&x((&[A-Fa-f0-9]){6})";

    private static final Map<String, Component> colorAliases = new HashMap<>() {{
        put("&0", Component.text("<black>"));
        put("&1", Component.text("<dark_blue>"));
        put("&2", Component.text("<dark_green>"));
        put("&3", Component.text("<dark_aqua>"));
        put("&4", Component.text("<dark_red>"));
        put("&5", Component.text("<dark_purple>"));
        put("&6", Component.text("<gold>"));
        put("&7", Component.text("<gray>"));
        put("&8", Component.text("<dark_gray>"));
        put("&9", Component.text("<blue>"));
        put("&a", Component.text("<green>"));
        put("&b", Component.text("<aqua>"));
        put("&c", Component.text("<red>"));
        put("&d", Component.text("<light_purple>"));
        put("&e", Component.text("<yellow>"));
        put("&f", Component.text("<white>"));
        put("&k", Component.text("<obfuscated>"));
        put("&l", Component.text("<bold>"));
        put("&m", Component.text("<strikethrough>"));
        put("&n", Component.text("<underlined>"));
        put("&o", Component.text("<italic>"));
        put("&r", Component.text("<reset>"));
    }};

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
        component = toMiniMessageFormat(component);
        component = MiniMessage.miniMessage().deserialize(
                MiniMessage.miniMessage().serialize(component).replace("\\<", "<").replace("\\>", ">"));
        return component;
    }

    private static Component toMiniMessageFormat(Component component) {
        component = component.replaceText(TextReplacementConfig.builder()
                .match("#[0-9A-Fa-f]{6}")
                .replacement((matchResult, builder) -> Component.text("<color:" + matchResult.group() + ">"))
                .build());
        component = component.replaceText(TextReplacementConfig.builder()
                .match(BUKKIT_HEX_PATTERN)
                .replacement((matchResult, builder) -> Component.text("<color:#" + matchResult.group(1).replaceAll("&", "") + ">"))
                .build());
        component = component.replaceText(TextReplacementConfig.builder()
                .match("&[0-9A-Fa-fklmnor]{1}")
                .replacement((matchResult, builder) -> colorAliases.get(matchResult.group())).build());


        return component;
    }
}
