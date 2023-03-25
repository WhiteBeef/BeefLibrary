package ru.whitebeef.beeflibrary.inventory;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.IntStream;

public interface IInventoryGUI extends ClickableInventory, RenameableInventory {

    Builder builder(String namespace, int size);

    @NotNull String getNamespace();

    @NotNull Component getName();

    int getSize();

    void setItem(int slot, @Nullable ItemStack item);

    boolean testPredicate(@NotNull Player player, @NotNull ItemStack item, int slot);

    @Nullable ItemStack getItem(int slot);

    HashMap<Integer, ItemStack> addItem(@Nullable ItemStack itemStack);

    void open(@NotNull Player player);

    @NotNull Set<@NotNull Player> getOpenedPlayers();

    void setStorageContents(@Nullable ItemStack @NotNull [] items);

    List<String> getCommands(int slot);

    @Nullable ItemStack @NotNull [] getStorageContents();

    void close(Player player);

    void onClose(InventoryCloseEvent event);

    void close();

    @NotNull Set<Integer> getClosedSlots();

    boolean isSlotClosed(int slot);

    @NotNull List<@NotNull String> getCommandsOnClose();

    void closeSlot(int slot);

    void openSlot(int slot);

    void setCommandsOnClose(@NotNull List<@NotNull String> commands);

    @NotNull Inventory getInventory();

    abstract class Builder {

        protected Set<Integer> closedSlots = new HashSet<>();
        protected List<String> commandsOnClose = new ArrayList<>();
        protected String namespace;
        protected int size;
        protected Component name;
        protected Map<Integer, @NotNull BiPredicate<@NotNull Player, @Nullable ItemStack>> predicates;
        protected Map<Integer, List<String>> commands;
        protected ItemStack[] items;


        public Builder(@NotNull String namespace, int size) {
            this.namespace = namespace;
            if (size < 0) {
                throw new IllegalArgumentException("Size can't be negative! (" + size + ")");
            }
            if (size % 9 != 0) {
                size += 9 - size % 9;
            }
            if (size >= 54) {
                size = 54;
            }
            this.size = size;
        }

        public String getNamespace() {
            return namespace;
        }

        public Builder setItem(int slot, @NotNull ItemStack item) {
            if (slot < 0 || slot >= size) {
                throw new IllegalArgumentException("Slot " + slot + " is not in range [0.." + (size - 1) + "]");
            }
            items[slot] = item;
            return this;
        }

        public Builder setName(@NotNull Component name) {
            this.name = name;
            return this;
        }

        public Builder setPredicate(int slot, @NotNull BiPredicate<@NotNull Player, @Nullable ItemStack> predicate) {
            this.predicates.put(slot, predicate);
            return this;
        }

        public Builder setStorageContents(@Nullable ItemStack @NotNull [] items) {
            if (this.size != items.length) {
                throw new IllegalArgumentException("Size of items array (" + items.length + ") not equals InventoryGUI size(" + this.size + ") ");
            }
            this.items = items;
            return this;
        }

        public Builder setCommand(int slot, @NotNull String command) {
            commands.computeIfAbsent(slot, k -> new ArrayList<>()).add(command);
            return this;
        }

        public Builder closeSlots(int... slots) {
            for (int slot : slots) {
                closedSlots.add(slot);
            }
            return this;
        }

        public Builder openSlots(int... slots) {
            for (int slot : slots) {
                closedSlots.remove(slot);
            }
            return this;
        }

        public Builder closeAllSlots() {
            IntStream.range(0, size - 1).forEach(closedSlots::add);
            return this;
        }

        public Builder openAllSlots() {
            closedSlots.clear();
            return this;
        }

        public Builder addCloseCommands(String... command) {
            commandsOnClose.addAll(List.of(command));
            return this;
        }

        public void register(Plugin plugin) {
            InventoryGUIManager.getInstance().registerTemplate(plugin, this);
        }

        public abstract IInventoryGUI build();
    }
}
