package ru.whitebeef.beeflibrary.utils;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

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

    public static int addAsMaxAsPossible(@NotNull Inventory inventory,
                                         int slot, @NotNull ItemStack toAdd, int limit) {
        ItemStack base = inventory.getItem(slot);
        if (base != null) {
            if (!base.isSimilar(toAdd)) {
                return 0;
            }

            int itemStackAmount = base.getAmount();
            if (itemStackAmount >= 64) {
                return 0;
            }

            int canMaxAddAmount = 64 - itemStackAmount;
            int amountToAdd = Math.min(limit, Math.min(toAdd.getAmount(), canMaxAddAmount));
            base.setAmount(Math.min(64, base.getAmount() + amountToAdd));
            return amountToAdd;
        } else {
            int maxAmount = Math.min(limit, Math.min(toAdd.getAmount(), toAdd.getMaxStackSize()));

            ItemStack outItem = toAdd.clone();
            outItem.setAmount(maxAmount);
            inventory.setItem(slot, outItem);

            return maxAmount;
        }
    }

}
