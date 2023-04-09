package ru.whitebeef.beeflibrary.inventory;

import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.whitebeef.beeflibrary.inventory.impl.InventoryGUI;
import ru.whitebeef.beeflibrary.utils.ItemGenerateProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public interface IInventoryGUI extends ClickableInventory {

    static Builder builder(String namespace, int size) {
        return new InventoryGUI.Builder(namespace, size);
    }

    @NotNull String getNamespace();

    @NotNull String getName();

    @NotNull Component getName(Player player);

    int getSize();

    void setItem(int slot, @Nullable ItemGenerateProperties item);

    boolean testPutPredicate(@NotNull Player player, @NotNull ItemStack item, int slot);

    @Nullable ItemStack getItem(int slot);

    @Nullable ItemStack getItem(int slot, Player player);

    void open(@NotNull Player player);

    @NotNull Set<@NotNull Player> getOpenedPlayers();

    void close(Player player);

    void onClose(InventoryCloseEvent event);

    void close();

    @NotNull Set<Integer> getClosedSlots();

    boolean isSlotClosed(int slot);

    void closeSlot(int slot);

    void openSlot(int slot);

    void setConsumersOnClose(@NotNull List<@NotNull BiConsumer<@NotNull IInventoryGUI, @NotNull Player>> commands);

    @NotNull Inventory getInventory(Player player);

    abstract class Builder {

        protected Set<Integer> closedSlots = new HashSet<>();
        protected String namespace;
        protected int size;
        protected String name;
        protected Map<Integer, List<Pair<BiPredicate<Player, ItemStack>, BiConsumer<Player, ItemStack>>>> putPredicates = new HashMap<>();
        protected Map<Integer, List<Pair<@NotNull Predicate<@NotNull Player>, @NotNull BiConsumer<IInventoryGUI, Player>>>> clickConsumers = new HashMap<>();
        protected List<BiConsumer<IInventoryGUI, Player>> consumersOnClose = new ArrayList<>();
        protected Map<Integer, List<Pair<Predicate<Player>, ItemGenerateProperties>>> items = new HashMap<>();


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
            IntStream.range(0, size).forEach(closedSlots::add);
        }

        public String getNamespace() {
            return namespace;
        }

        public Builder setItem(int slot, @NotNull ItemGenerateProperties item) {
            if (slot < 0 || slot >= size) {
                throw new IllegalArgumentException("Slot " + slot + " is not in range [0.." + (size - 1) + "]");
            }
            items.computeIfAbsent(slot, k -> new ArrayList<>()).add(Pair.of(player -> true, item));
            return this;
        }

        public Builder setItem(int slot, Predicate<Player> predicate, @NotNull ItemGenerateProperties item) {
            if (slot < 0 || slot >= size) {
                throw new IllegalArgumentException("Slot " + slot + " is not in range [0.." + (size - 1) + "]");
            }
            items.computeIfAbsent(slot, k -> new ArrayList<>()).add(Pair.of(predicate, item));
            return this;
        }

        public Builder setName(@NotNull String name) {
            this.name = name;
            return this;
        }

        public Builder addPutPredicate(int slot, @NotNull BiPredicate<@NotNull Player, @Nullable ItemStack> predicate) {
            this.putPredicates.computeIfAbsent(slot, k -> new ArrayList<>()).add(Pair.of(predicate, (player, itemStack) -> {
            }));
            return this;
        }

        public Builder addPutPredicate(int slot, @NotNull BiPredicate<@NotNull Player, @Nullable ItemStack> predicate,
                                       @NotNull BiConsumer<@NotNull Player, @Nullable ItemStack> elseConsumer) {
            this.putPredicates.computeIfAbsent(slot, k -> new ArrayList<>()).add(Pair.of(predicate, elseConsumer));
            return this;
        }

        public Builder addClickPredicate(int slot, @NotNull Predicate<@NotNull Player> predicate) {
            this.clickConsumers.computeIfAbsent(slot, k -> new ArrayList<>()).add(Pair.of(predicate, ((inventoryGUI, player) -> {
            })));
            return this;
        }

        public Builder addClickPredicate(int slot, @NotNull Predicate<@NotNull Player> predicate,
                                         @NotNull BiConsumer<@NotNull IInventoryGUI, @NotNull Player> consumer) {
            this.clickConsumers.computeIfAbsent(slot, k -> new ArrayList<>()).add(Pair.of(predicate, consumer));
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

        public Builder addCloseCommands(List<BiConsumer<IInventoryGUI, Player>> consumers) {
            consumersOnClose.addAll(consumers);
            return this;
        }

        public Builder addClickConsumer(int slot, BiConsumer<IInventoryGUI, Player> consumer) {
            clickConsumers.computeIfAbsent(slot, k -> new ArrayList<>()).add(Pair.of(player -> true, consumer));
            return this;
        }

        public Builder addClickConsumer(int slot, Predicate<Player> predicate, BiConsumer<IInventoryGUI, Player> consumer) {
            clickConsumers.computeIfAbsent(slot, k -> new ArrayList<>()).add(Pair.of(predicate, consumer));
            return this;
        }

        public void register(Plugin plugin) {
            InventoryGUIManager.getInstance().registerTemplate(plugin, this);
        }

        public abstract IInventoryGUI build();
    }
}
