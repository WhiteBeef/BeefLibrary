package ru.whitebeef.beeflibrary.inventory;

import me.clip.placeholderapi.PlaceholderAPI;
import org.apache.logging.log4j.util.TriConsumer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.whitebeef.beeflibrary.BeefLibrary;
import ru.whitebeef.beeflibrary.utils.ScheduleUtils;

import java.util.HashMap;
import java.util.Map;

public class CustomInventoryGUICommand {

    private static CustomInventoryGUICommand instance;

    public static CustomInventoryGUICommand getInstance() {
        return instance;
    }

    public static void setInstance(CustomInventoryGUICommand instance) {
        CustomInventoryGUICommand.instance = instance;
    }

    private final Map<String, TriConsumer<IInventoryGUI, Player, String>> customCommands = new HashMap<>();


    public boolean registerCommand(String command, TriConsumer<IInventoryGUI, Player, String> consumer) {
        if (isCustomCommand(command)) {
            return false;
        }
        customCommands.put(command, consumer);
        return true;
    }

    public boolean isCustomCommand(String command) {
        return customCommands.containsKey(command);
    }

    public void runCommand(IInventoryGUI inventoryGUI, Player player, String command) {
        ScheduleUtils.runTask(() -> customCommands.getOrDefault(command.toLowerCase(),
                (i, p, s) -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), BeefLibrary.getInstance().isPlaceholderAPIHooked() ? PlaceholderAPI.setPlaceholders(player, s) : s))
                .accept(inventoryGUI, player, command));
    }
}
