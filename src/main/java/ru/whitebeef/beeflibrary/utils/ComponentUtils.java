package ru.whitebeef.beeflibrary.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Location;

public class ComponentUtils {

    public static boolean containsText(Component component, String text) {
        if (!(component instanceof TextComponent textComponent)) {
            return false;
        }

        boolean contains = textComponent.content().contains(text);
        if (contains) {
            return true;
        }
        for (Component child : component.children()) {
            if (containsText(child, text)) {
                contains = true;
                break;
            }
        }
        return contains;
    }

}
