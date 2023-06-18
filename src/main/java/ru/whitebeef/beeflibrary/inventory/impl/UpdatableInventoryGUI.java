package ru.whitebeef.beeflibrary.inventory.impl;

import it.unimi.dsi.fastutil.Pair;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.whitebeef.beeflibrary.inventory.IInventoryGUI;
import ru.whitebeef.beeflibrary.inventory.IUpdatableInventoryGUI;
import ru.whitebeef.beeflibrary.utils.ItemGenerateProperties;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class UpdatableInventoryGUI extends InventoryGUI implements IUpdatableInventoryGUI {

    public static InventoryGUI.Builder builder(String namespace, int size) {
        return new InventoryGUI.Builder(namespace, size);
    }

    public UpdatableInventoryGUI(@NotNull String namespace, int size, @NotNull String name, @NotNull Map<Integer, List<Pair<@NotNull BiPredicate<@NotNull Player, @Nullable ItemStack>, @NotNull BiConsumer<@NotNull Player, @Nullable ItemStack>>>> putPredicates, @NotNull Map<Integer, List<Pair<@NotNull Predicate<@NotNull Player>, @NotNull BiConsumer<@NotNull IInventoryGUI, @NotNull Player>>>> clickConsumers, @NotNull Map<Integer, List<Pair<@NotNull Predicate<@NotNull Player>, @NotNull ItemGenerateProperties>>> items, @NotNull Set<@NotNull Integer> closedSlots, @NotNull List<@NotNull BiConsumer<@NotNull IInventoryGUI, @NotNull Player>> consumersOnClose) {
        super(namespace, size, name, putPredicates, clickConsumers, items, closedSlots, consumersOnClose);
    }

    @Override
    public void update() {
        for (Player player : getOpenedPlayers()) {
            open(player);
        }
    }

    @Override
    public void setItem(int slot, @Nullable ItemGenerateProperties item) {
        super.setItem(slot, item);

        for (Player player : getOpenedPlayers()) {
            player.getOpenInventory().getTopInventory().setItem(slot, item == null ? null : item.generate(player));
        }
    }

    public static class Builder extends IInventoryGUI.Builder {

        public Builder(@NotNull String namespace, int size) {
            super(namespace, size);
        }

        @Override
        public UpdatableInventoryGUI build() {
            return new UpdatableInventoryGUI(namespace, size, name, putPredicates, clickConsumers, items, closedSlots, consumersOnClose);
        }
    }

}
