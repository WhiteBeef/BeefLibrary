package ru.whitebeef.beeflibrary.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class InventoryUtils {
    public static boolean canBeAddedFully(ItemStack[] storageContents, ItemStack[] itemStacks) {
        Inventory tempInv = Bukkit.createInventory(null, storageContents.length);
        tempInv.setStorageContents(storageContents);
        for (ItemStack itemStack : itemStacks) {
            if (itemStack == null) {
                continue;
            }
            if (!tempInv.addItem(itemStack).isEmpty()) {
                return false;
            }
        }
        return true;
    }

}
