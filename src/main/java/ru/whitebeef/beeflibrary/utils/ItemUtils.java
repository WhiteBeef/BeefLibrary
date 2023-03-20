package ru.whitebeef.beeflibrary.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;
import ru.whitebeef.beeflibrary.BeefLibrary;
import ru.whitebeef.beeflibrary.chat.MessageFormatter;
import ru.whitebeef.beeflibrary.inventory.deprecated.OldInventoryGUI;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class ItemUtils {

    /* Выкинет вещь, если инвентарь полный */
    public static void addItem(Player player, ItemStack item) {
        player.getInventory().addItem(item).values()
                .forEach(itemStack -> Bukkit.getScheduler().scheduleSyncDelayedTask(BeefLibrary.getInstance(),
                        () -> player.getWorld().dropItem(player.getLocation(), itemStack), 1L));
    }

    public static void returnItems(Player player, ArrayList<ItemStack> items) {
        items.forEach(item -> {
            addItem(player, item.clone());
            item.setAmount(0);
        });
    }

    @NotNull
    public static ItemStack parseItemStack(ConfigurationSection section) {
        ItemStack itemStack = new ItemStack(Material.getMaterial(section.getString("material").toUpperCase()));

        ItemMeta meta = itemStack.getItemMeta();

        if (section.isString("name")) {
            meta.displayName(MessageFormatter.of(section.getString("name")).toComponent().decoration(TextDecoration.ITALIC, false));
        }

        if (section.isList("lore")) {
            List<Component> lore = new ArrayList<>();
            section.getStringList("lore").forEach(str -> lore.add(MessageFormatter.of(str).toComponent()));
            meta.lore(lore);
        }

        if (section.isInt("customModelData")) {
            meta.setCustomModelData(section.getInt("customModelData"));
        }

        if (section.isString("skullOwner")) {
            if (meta instanceof SkullMeta skullMeta) {
                skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(section.getString("skullOwner")));
            }
        }

        itemStack.setItemMeta(meta);
        return itemStack;
    }

    public static ArrayList<ItemStack> parseItemsMapToArrayList(HashMap<ItemStack, Integer> mapStart) {
        HashMap<ItemStack, Integer> map = new HashMap<>(mapStart);
        ArrayList<ItemStack> retArray = new ArrayList<>();
        if (map.size() == 0) {
            return retArray;
        }
        for (ItemStack item : map.keySet()) {
            while (map.getOrDefault(item, 0) > 0) {
                int amount = map.getOrDefault(item, 0);
                int addAmount;
                if (amount > 64) {
                    map.put(item, map.getOrDefault(item, 0) - 64);
                    addAmount = 64;
                } else {
                    addAmount = map.getOrDefault(item, 0);
                    map.put(item, 0);
                }
                ItemStack clonedItem = item.clone();
                clonedItem.setAmount(addAmount);
                retArray.add(clonedItem);
            }
        }
        return retArray;
    }

    public static HashMap<ItemStack, Integer> getNewItems(OldInventoryGUI gui, Inventory beforeInventory, Inventory afterInventory) {
        HashMap<ItemStack, Integer> map = new HashMap<>();
        if (afterInventory == null) {
            return map;
        }
        ItemStack[] afterStorageContents = afterInventory.getStorageContents();
        ItemStack[] beforeStorageContents = beforeInventory.getStorageContents();
        for (int i = 0; i < beforeStorageContents.length; i++) {
            if (!gui.isChangeableSlot(i)) {
                continue;
            }
            if (beforeStorageContents[i] == null) {
                if (afterStorageContents[i] != null) {
                    map.put(afterStorageContents[i], map.getOrDefault(afterStorageContents[i], 0) + afterStorageContents[i].getAmount());
                }
                continue;
            } else if (afterStorageContents[i] == null) {
                map.put(beforeStorageContents[i], map.getOrDefault(beforeStorageContents[i], 0) - beforeStorageContents[i].getAmount());
                continue;
            }
            if (!beforeStorageContents[i].equals(afterStorageContents[i])) {
                map.put(beforeStorageContents[i], map.getOrDefault(beforeStorageContents[i], 0) - beforeStorageContents[i].getAmount());
                map.put(afterStorageContents[i], map.getOrDefault(afterStorageContents[i], 0) + afterStorageContents[i].getAmount());
                continue;
            }
            if (beforeStorageContents[i].getAmount() != afterStorageContents[i].getAmount()) {
                map.put(afterStorageContents[i], map.getOrDefault(afterStorageContents[i], 0) + afterStorageContents[i].getAmount() - beforeStorageContents[i].getAmount());
            }
        }
        return map;
    }

    @Nullable
    public static String serializeItemStacks(@NotNull ItemStack... itemStacks) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeInt(itemStacks.length);

            for (ItemStack itemStack : itemStacks) {
                dataOutput.writeObject(itemStack);
            }

            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
    public static String serializeString(@NotNull String... strings) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeInt(strings.length);

            for (String str : strings) {
                dataOutput.writeObject(str);
            }

            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @NotNull
    public static List<ItemStack> deserializeItemStack(@NotNull String string) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(string));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            List<ItemStack> itemStacks = new ArrayList<>();

            int size = dataInput.readInt();
            for (int i = 0; i < size; i++) {
                itemStacks.add((ItemStack) dataInput.readObject());
            }

            dataInput.close();
            return itemStacks;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Nullable
    public static List<String> deserializeString(@NotNull String string) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(string));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            List<String> strings = new ArrayList<>();
            int size = dataInput.readInt();
            for (int i = 0; i < size; i++) {
                strings.add((String) dataInput.readObject());
            }

            dataInput.close();
            return strings;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
