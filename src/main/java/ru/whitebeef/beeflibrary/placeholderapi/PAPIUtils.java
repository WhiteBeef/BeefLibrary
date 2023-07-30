package ru.whitebeef.beeflibrary.placeholderapi;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.whitebeef.beeflibrary.BeefLibrary;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PAPIUtils {

    private static final TextReplacementConfig REMOVE_PLACEHOLDERS = TextReplacementConfig.builder().match("%(?!message\\b)[A-z0-9_]+%").replacement("").build();
    private static Map<String, Function<CommandSender, Component>> registeredPlaceholders = new HashMap<>();

    /**
     * Плейсхолдер, который будет зарегистрирован будет иметь формат %pluginname_placeholder%
     *
     * @param plugin      Плагин будет регистрировать плейсхолдер, null - глобальный плейсхолдер
     * @param placeholder Плейсхолдер, без %%
     * @param function    Функцуия, в которую будет пердаваться CommandSender, для которого будет заменяться плейсхолдер, возвращаться должна компонента
     */
    public static void registerPlaceholder(@Nullable Plugin plugin, @NotNull String placeholder, @NotNull Function<CommandSender, Component> function) {
        if (placeholder.isEmpty()) {
            throw new IllegalArgumentException("Placeholder must be not empty!");
        }

        String toRegister;
        if (plugin != null) {
            toRegister = "%" + plugin.getName().toLowerCase() + "_" + placeholder + "%";
        } else {
            toRegister = "%" + placeholder + "%";
        }

        registeredPlaceholders.put(toRegister, function);

    }

    public static void unregisterPlaceholders(Plugin plugin) {
        List<String> toUnregister = registeredPlaceholders.keySet().stream().filter(s -> s.startsWith("%" + plugin.getName().toLowerCase() + "_")).toList();
        toUnregister.forEach(registeredPlaceholders::remove);
    }

    public static void unregisterAllPlaceholders() {
        registeredPlaceholders = new HashMap<>();
    }


    public static boolean isRegisteredPlaceholder(String placeholder) {
        return registeredPlaceholders.containsKey(placeholder);
    }

    public static Function<CommandSender, Component> getRegisteredPlaceHolder(String placeholder) {
        return registeredPlaceholders.getOrDefault(placeholder, commandSender -> PlainTextComponentSerializer.plainText().deserialize(placeholder));
    }

    public static Component setPlaceholders(@Nullable CommandSender commandSender, @NotNull Component component) {
        if (commandSender == null) {
            return component;
        }
        component = replaceCustomPlaceHolders(commandSender, component);
        String line = GsonComponentSerializer.gson().serialize(component);
        if (!(commandSender instanceof Player player)) {
            component = GsonComponentSerializer.gson().deserialize(line)
                    .replaceText(TextReplacementConfig.builder()
                            .match("%player_name%")
                            .replacement(commandSender.name()).build()
                    );
            component = component.replaceText(REMOVE_PLACEHOLDERS);
            return component;
        }

        return BeefLibrary.getInstance().isPlaceholderAPIHooked() ? GsonComponentSerializer.gson().deserialize(PlaceholderAPI.setPlaceholders(player, GsonComponentSerializer.gson().serialize(component))) : component;
    }

    public static String setPlaceholders(CommandSender sender, String text) {
        if (sender == null) {
            return text;
        }
        if (!(sender instanceof Player player)) {
            return text.replaceAll("%player_name%", sender.getName()).replaceAll("%(?!message\\b)[A-z0-9_]+%", "");
        }
        return BeefLibrary.getInstance().isPlaceholderAPIHooked() ? PlaceholderAPI.setPlaceholders(player, text) : text;
    }

    private static Component replaceCustomPlaceHolders(CommandSender commandSender, Component component) {
        String gson = GsonComponentSerializer.gson().serialize(component);

        Set<String> toReplace = new HashSet<>();

        Pattern pattern = Pattern.compile("%[\\w\\-_]+%");

        Matcher matcher = pattern.matcher(gson);
        int index = 0;
        while (matcher.find(index)) {
            String placeholder = gson.substring(matcher.start(), matcher.end());
            if (isRegisteredPlaceholder(placeholder)) {
                toReplace.add(placeholder);
            }
            index = matcher.end();
        }

        for (@RegExp String placeholder : toReplace) {
            component = component
                    .replaceText(TextReplacementConfig.builder()
                            .matchLiteral(placeholder)
                            .replacement(getRegisteredPlaceHolder(placeholder).apply(commandSender))
                            .build());
        }

        return component;
    }

    public static Component replaceBiPlaceholders(Component component, CommandSender sender, CommandSender recipient) {
        component = component
                .replaceText(TextReplacementConfig.builder()
                        .match("%(sender|recipient){1}\\-([\\w\\-\\_]+)%")
                        .replacement((matchResult, builder) -> {
                            String placeholder = "%" + matchResult.group(2) + "%";
                            if (matchResult.group(1).equals("sender")) {
                                if (sender != null) {
                                    return Component.text(PAPIUtils.setPlaceholders(sender, placeholder));
                                } else {
                                    return Component.text(matchResult.group());
                                }
                            } else {
                                if (recipient != null) {
                                    return Component.text(PAPIUtils.setPlaceholders(recipient, placeholder));
                                } else {
                                    return Component.text(matchResult.group());
                                }
                            }
                        })
                        .build());
        return component;
    }

    public static String replaceBiPlaceholders(String text, CommandSender sender, CommandSender recipient) {
        Matcher matcher = Pattern.compile("%(sender|recipient){1}\\-([A-z\\-\\_]+)%").matcher(text);
        int index = 0;
        StringBuilder sb = new StringBuilder();
        while (matcher.find(index)) {
            sb.append(text, index, matcher.start());
            String placeholder = "%" + matcher.group(2) + "%";
            if (!isRegisteredPlaceholder(placeholder)) {
                boolean isSenderPlaceholder = matcher.group(1).equals("sender");
                if (isSenderPlaceholder) {
                    if (sender != null) {
                        sb.append(setPlaceholders(sender, placeholder));
                    } else {
                        sb.append(text, matcher.start(), matcher.end());
                    }
                } else {
                    if (recipient != null) {
                        sb.append(setPlaceholders(recipient, placeholder));
                    } else {
                        sb.append(text, matcher.start(), matcher.end());
                    }
                }
            } else {
                sb.append(text, matcher.start(), matcher.end());
            }
            index = matcher.end();
        }
        sb.append(text, index, text.length());
        return sb.toString();
    }
}
