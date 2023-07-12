package ru.whitebeef.beeflibrary.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ru.whitebeef.beeflibrary.placeholderapi.PAPIUtils;
import ru.whitebeef.beeflibrary.utils.Color;

public class MessageFormatter {


    public static MessageFormatter of(String text) {
        return new MessageFormatter(text);
    }

    private MessageFormatter(@NotNull String message) {
        this.message = message;
    }

    private String message;

    public MessageFormatter replaceRegex(@NotNull String regex, @NotNull String replacement) {
        this.message = this.message.replaceAll(regex, replacement);
        return this;
    }

    public MessageFormatter applyPlaceholders(CommandSender player) {
        this.message = PAPIUtils.setPlaceholders(player, message);
        return this;
    }

    public Component toComponent(CommandSender sender) {
        if (sender == null) {
            return LegacyComponentSerializer.legacySection().deserialize(Color.colorize(message));
        }
        message = Color.colorize(message);
        return PAPIUtils.setPlaceholders(sender, LegacyComponentSerializer.legacySection().deserialize(message));
    }

    public Component toComponent() {
        return PAPIUtils.setPlaceholders(null, LegacyComponentSerializer.legacySection().deserialize(Color.colorize(message)));
    }

    @Override
    public String toString() {
        return Color.colorize(message);
    }
}
