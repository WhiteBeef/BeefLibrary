package ru.whitebeef.beeflibrary.inventory;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;

public interface InventoryGUI extends ClickableInventory, RenameableInventory {

    Builder builder();

    @NotNull String getNamespace();

    @NotNull Component getName();

    int getSize();

    void setName();

    void setItem(int slot, @Nullable ItemStack item);

    @NotNull Map<Integer, @NotNull BiPredicate<@NotNull Player, @Nullable ItemStack>> getPredicateSlots();

    @Nullable ItemStack getItem(int slot);

    boolean addItem(@Nullable ItemStack itemStack);

    void open(@NotNull Player player);

    @NotNull Set<@NotNull Player> getOpenedPlayers();

    void setStorageContents(@Nullable ItemStack @NotNull [] items);

    List<String> getCommands(int slot);

    @Nullable ItemStack @NotNull [] getStorageContents();

    @NotNull Inventory getInventory();

    void close();

    abstract class Builder {
        public abstract Builder setSize(int size);

        public abstract Builder setName(@NotNull Component name);

        public abstract Builder setPredicate(int slot, @NotNull BiPredicate<@NotNull Player, @Nullable ItemStack> predicate);

        public abstract Builder setStorageContents(@Nullable ItemStack @NotNull [] items);

        public abstract Builder setCommand(int slot, String command);

        public abstract InventoryGUI build();
    }
}
