package ru.whitebeef.beeflibrary.placeholderapi;

import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.whitebeef.beeflibrary.BeefLibrary;

public class PAPIUtils {
    private static final TextReplacementConfig REMOVE_PLACEHOLDERS = TextReplacementConfig.builder().match("%(?!message\\b)[A-z0-9_]+%").replacement("").build();
    private static final TextReplacementConfig PLAYER_NAME_COMPONENT_PLACEHOLDERS = TextReplacementConfig.builder().match("%player-name-component%").replacement("%player_name%").build();

    public static Component setPlaceholders(@Nullable CommandSender sender, @NotNull Component component) {
        String line = GsonComponentSerializer.gson().serialize(component);
        if (sender == null) {
            return component;
        }
        if (!(sender instanceof Player player)) {
            component = GsonComponentSerializer.gson().deserialize(line)
                    .replaceText(PLAYER_NAME_COMPONENT_PLACEHOLDERS)
                    .replaceText(TextReplacementConfig.builder()
                            .match("%player_name%")
                            .replacement(sender.name()).build()
                    );
            component = component.replaceText(REMOVE_PLACEHOLDERS);
            return component;
        }
        return GsonComponentSerializer.gson().deserialize(BeefLibrary.getInstance().isPlaceholderAPIHooked() ? PlaceholderAPI.setPlaceholders(player, line) : line)
                .replaceText(TextReplacementConfig.builder()
                        .match("%player-name-component%")
                        .replacement(player.name()
                                .hoverEvent(LegacyComponentSerializer.legacySection().deserialize("Нажмите, чтобы отправить личное сообщение").asHoverEvent())
                                .clickEvent(ClickEvent.suggestCommand("/tell " + player.getName() + " "))).build()
                );
    }

    public static String setPlaceholders(CommandSender sender, String text) {
        if (!(sender instanceof Player player)) {
            return text.replaceAll("%player-name-component%", "%player_name%").replaceAll("%player_name%", sender.getName()).replaceAll("%(?!message\\b)[A-z0-9_]+%", "");
        }
        return BeefLibrary.getInstance().isPlaceholderAPIHooked() ? PlaceholderAPI.setPlaceholders(player, text) : text;
    }
}
