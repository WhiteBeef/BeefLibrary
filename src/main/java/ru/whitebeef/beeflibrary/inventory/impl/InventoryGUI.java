package ru.whitebeef.beeflibrary.inventory.impl;

import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.whitebeef.beeflibrary.BeefLibrary;
import ru.whitebeef.beeflibrary.chat.MessageFormatter;
import ru.whitebeef.beeflibrary.inventory.IInventoryGUI;
import ru.whitebeef.beeflibrary.inventory.InventoryGUIManager;
import ru.whitebeef.beeflibrary.utils.InventoryUtils;
import ru.whitebeef.beeflibrary.utils.ItemGenerateProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public class InventoryGUI implements IInventoryGUI {
    public static Builder builder(String namespace, int size) {
        return new Builder(namespace, size);
    }

    private final Set<Player> openedPlayers = new HashSet<>();
    private final String namespace;
    private final int size;
    private final String name;
    private final Map<Integer, List<Pair<@NotNull BiPredicate<@NotNull Player, @Nullable ItemStack>, @NotNull BiConsumer<@NotNull Player, @Nullable ItemStack>>>> putPredicates;
    private final Map<Integer, List<Pair<@NotNull Predicate<@NotNull Player>, @NotNull BiConsumer<IInventoryGUI, Player>>>> clickConsumers;
    private List<BiConsumer<IInventoryGUI, Player>> consumersOnClose;
    private final Set<Integer> closedSlots;
    private Map<Integer, List<Pair<Predicate<Player>, ItemGenerateProperties>>> items;

    public InventoryGUI(@NotNull String namespace, int size, @NotNull String name,
                        @NotNull Map<Integer, List<Pair<@NotNull BiPredicate<@NotNull Player, @Nullable ItemStack>, @NotNull BiConsumer<@NotNull Player, @Nullable ItemStack>>>> putPredicates,
                        @NotNull Map<Integer, List<Pair<@NotNull Predicate<@NotNull Player>, @NotNull BiConsumer<@NotNull IInventoryGUI, @NotNull Player>>>> clickConsumers,
                        @NotNull Map<Integer, List<Pair<@NotNull Predicate<@NotNull Player>, @NotNull ItemGenerateProperties>>> items,
                        @NotNull Set<@NotNull Integer> closedSlots, @NotNull List<@NotNull BiConsumer<@NotNull IInventoryGUI, @NotNull Player>> consumersOnClose) {
        this.namespace = namespace;
        this.size = size;
        this.name = name;
        this.putPredicates = putPredicates;
        this.clickConsumers = clickConsumers;
        this.closedSlots = closedSlots;
        this.consumersOnClose = consumersOnClose;
        this.items = items;
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        if (event.getClickedInventory() != event.getInventory()) {
            return;
        }

        Inventory inventory = event.getInventory();
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        if (isSlotClosed(slot)) {
            event.setCancelled(true);
        } else {
            ItemStack item = event.getCurrentItem();
            //
            if (event.isShiftClick()) {
                //TODO
                if (event.getCurrentItem() == null) {
                    return;
                }

                if (!testPutPredicate(player, null, slot)) {
                    event.setCancelled(true);
                }

                //event.setCancelled(true);
                //

            } else if (item != null && event.isLeftClick() || event.isRightClick()) {
                if (event.getCursor() == null) {
                    return;
                }
                if (event.getSlot() != 1) {
                    return;
                }
                ItemStack cursor = event.getCursor();


                if (testPutPredicate(player, item, slot)) {
                    if (!cursor.isSimilar(item)) {
                        return;
                    }
                    event.setCancelled(true);

                    if (event.getClick().isLeftClick()) {
                        cursor.setAmount(cursor.getAmount()
                                - InventoryUtils.addAsMaxAsPossible(event.getInventory(), slot, cursor, 64));
                    } else {
                        cursor.setAmount(cursor.getAmount()
                                - InventoryUtils.addAsMaxAsPossible(event.getInventory(), slot, cursor, 1));
                    }
                }
            } else if (event.getClick().isKeyboardClick() || event.getClick() == ClickType.SWAP_OFFHAND) {

                Bukkit.getScheduler().runTask(BeefLibrary.getInstance(), () -> {
                    if (event.getSlot() != 1) {
                        return;
                    }
                    int button = event.getHotbarButton();
                    ItemStack firstItem;
                    if (button == -1) {
                        player.getInventory().getItemInOffHand();
                        firstItem = player.getInventory().getItemInOffHand();
                    } else {
                        firstItem = player.getInventory().getItem(button) == null ? new ItemStack(Material.AIR) : player.getInventory().getItem(button);
                    }

                    if (testPutPredicate(player, firstItem, slot)) {
                        return;
                    }

                    event.setCancelled(false);

                    ItemStack temp = inventory.getItem(slot);

                    if (button == -1) {
                        player.getInventory().setItemInOffHand(temp);
                    } else {
                        player.getInventory().setItem(button, temp);
                    }
                    inventory.setItem(slot, firstItem);
                });
            }
        }

        for (var consumer : clickConsumers.getOrDefault(slot, Collections.emptyList())) {
            if (!consumer.left().test(player)) {
                continue;
            }
            consumer.right().accept(this, player);
        }
    }

    @Override
    public void onDrag(InventoryDragEvent event) {
        event.getNewItems().forEach((slot, item) -> {
            if (event.isCancelled()) {
                return;
            }
            if (isSlotClosed(slot)) {
                event.setCancelled(true);
            }
            if (!testPutPredicate((Player) event.getWhoClicked(), item, slot)) {
                event.setCancelled(true);
            }
        });
    }


    @Override
    @NotNull
    public String getNamespace() {
        return namespace;
    }

    @Override
    @NotNull
    public String getName() {
        return name;
    }

    @Override
    @NotNull
    public Component getName(Player player) {
        return MessageFormatter.of(name).toComponent(player);
    }

    @Override
    public int getSize() {
        return size;
    }

    @Override
    public void setItem(int slot, @Nullable ItemGenerateProperties item) {
        items.computeIfAbsent(slot, k -> new ArrayList<>()).add(Pair.of(player -> true, item));
    }

    @Override
    public boolean testPutPredicate(@NotNull Player player, @Nullable ItemStack item, int slot) {
        for (var pairPredicate : putPredicates.getOrDefault(slot, new ArrayList<>())) {
            if (!pairPredicate.left().test(player, item)) {
                return false;
            }
        }
        return true;
    }

    @Override
    @Nullable
    public ItemStack getItem(int slot) {
        if (!items.containsKey(slot)) {
            return null;
        }
        return items.get(slot).stream().findAny().get().right().generate(null);
    }

    @Override
    @Nullable
    public ItemStack getItem(int slot, @Nullable Player player) {
        ItemGenerateProperties property = null;
        for (var pair : items.getOrDefault(slot, Collections.emptyList())) {
            if (pair == null) {
                continue;
            }
            if (pair.left().test(player)) {
                property = pair.right();
                break;
            }
        }
        return property == null ? null : property.generate(player);
    }

    @Override
    public void open(@NotNull Player player) {
        player.openInventory(getInventory(player));
        openedPlayers.add(player);
        InventoryGUIManager.getInstance().addOpenInventory(player, this);
    }

    @Override
    @NotNull
    public Set<@NotNull Player> getOpenedPlayers() {
        return openedPlayers;
    }

    @Override
    public void close(Player player) {
        player.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
        InventoryGUIManager.getInstance().removeOpenInventory(player);
        openedPlayers.remove(player);
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        consumersOnClose.forEach(consumer -> consumer.accept(this, (Player) event.getPlayer()));
    }

    @Override
    public void close() {
        getOpenedPlayers().forEach(this::close);
    }

    @Override
    public @NotNull Set<Integer> getClosedSlots() {
        return closedSlots;
    }

    @Override
    public boolean isSlotClosed(int slot) {
        return closedSlots.contains(slot);
    }

    @Override
    public void closeSlot(int slot) {
        closedSlots.add(slot);
    }

    @Override
    public void openSlot(int slot) {
        closedSlots.remove(slot);
    }

    @Override
    public void setConsumersOnClose(@NotNull List<@NotNull BiConsumer<@NotNull IInventoryGUI, @NotNull Player>> commands) {
        this.consumersOnClose = commands;
    }

    @Override
    @NotNull
    public Inventory getInventory(Player player) {
        Inventory inv = Bukkit.createInventory(null, size, MessageFormatter.of(name).toComponent(player));
        for (int i = 0; i < this.size; i++) {
            inv.setItem(i, getItem(i, player));
        }
        return inv;
    }

    public static class Builder extends IInventoryGUI.Builder {

        public Builder(String namespace, int size) {
            super(namespace, size);
        }

        @Override
        public IInventoryGUI build() {
            return new InventoryGUI(namespace, size, name, putPredicates, clickConsumers, items, closedSlots, consumersOnClose);
        }

    }
}
