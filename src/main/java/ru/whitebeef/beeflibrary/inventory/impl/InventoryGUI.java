package ru.whitebeef.beeflibrary.inventory.impl;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.whitebeef.beeflibrary.chat.MessageFormatter;
import ru.whitebeef.beeflibrary.inventory.CustomInventoryGUICommand;
import ru.whitebeef.beeflibrary.inventory.IInventoryGUI;
import ru.whitebeef.beeflibrary.inventory.InventoryGUIManager;
import ru.whitebeef.beeflibrary.utils.ItemUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;

public class InventoryGUI implements IInventoryGUI {
    public static Builder builder(String namespace, int size) {
        return new Builder(namespace, size);
    }

    private Set<Player> openedPlayers = new HashSet<>();
    private final Inventory inventory;
    private final String namespace;
    private final int size;
    private final String name;
    private final Map<Integer, List<@NotNull BiPredicate<@NotNull Player, @Nullable ItemStack>>> predicates;
    private final Map<Integer, List<String>> commands;
    private List<String> commandsOnClose;
    private Set<Integer> closedSlots;

    public InventoryGUI(@NotNull String namespace, int size, @NotNull String name, @NotNull Map<Integer,
            List<@NotNull BiPredicate<@NotNull Player, @Nullable ItemStack>>> predicates,
                        @NotNull Map<Integer, List<@NotNull String>> commands, @NotNull ItemStack[] items,
                        @NotNull Set<@NotNull Integer> closedSlots, @NotNull List<@NotNull String> commandsOnClose) {
        this.namespace = namespace;
        this.size = size;
        this.name = name;
        this.predicates = predicates;
        this.commands = commands;
        this.closedSlots = closedSlots;
        this.commandsOnClose = commandsOnClose;
        inventory = Bukkit.createInventory(null, size, name);
        inventory.setStorageContents(items);
    }

    @Override
    public void onClick(InventoryClickEvent event) {
        if (event.getClickedInventory() != event.getInventory()) {
            return;
        }
        if (event.isShiftClick()) {
            event.setCancelled(true);
        }
        int slot = event.getSlot();

        if (isSlotClosed(slot)) {
            event.setCancelled(true);
        }

        Player player = (Player) event.getWhoClicked();
        CustomInventoryGUICommand customCommandManager = CustomInventoryGUICommand.getInstance();
        for (String command : commands.getOrDefault(slot, Collections.emptyList())) {
            customCommandManager.runCommand(this, player, command);
        }
    }

    @Override
    public void onDrag(InventoryDragEvent event) {
        event.getNewItems().forEach((slot, item) -> {
            if (isSlotClosed(slot)) {
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
    public @NotNull Component getName(Player player) {
        return MessageFormatter.of(name).toComponent(player);
    }

    @Override
    public int getSize() {
        return size;
    }


    @Override
    public void setItem(int slot, @Nullable ItemStack item) {
        inventory.setItem(slot, item);
        getOpenedPlayers().forEach(Player::updateInventory);
    }

    @Override
    public boolean testPredicate(@NotNull Player player, @NotNull ItemStack item, int slot) {
        for(BiPredicate<Player, ItemStack> predicate : predicates.getOrDefault(slot, new ArrayList<>())){
            if(!predicate.test(player,item)){
                return false;
            }
        }
        return true;
    }

    @Override
    @Nullable
    public ItemStack getItem(int slot) {
        return inventory.getItem(slot);
    }

    @Override
    public HashMap<Integer, ItemStack> addItem(@Nullable ItemStack itemStack) {
        //TODO сделать проверку на предикаты и добавлять только если всё проходит и слот открыт
        return inventory.addItem(itemStack);
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
    public void setStorageContents(ItemStack @NotNull [] items) {
        inventory.setStorageContents(items);
        getOpenedPlayers().forEach(Player::updateInventory);
    }

    @Override
    public List<String> getCommands(int slot) {
        return commands.getOrDefault(slot, new ArrayList<>());
    }

    @Override
    @Nullable
    public ItemStack @NotNull [] getStorageContents() {
        return inventory.getStorageContents();
    }

    @Override
    @NotNull
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public void close(Player player) {
        player.closeInventory(InventoryCloseEvent.Reason.PLUGIN);
        InventoryGUIManager.getInstance().removeOpenInventory(player);
        openedPlayers.remove(player);
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        commandsOnClose.forEach(command -> CustomInventoryGUICommand.getInstance().runCommand(this, (Player) event.getPlayer(), command));
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
    public @NotNull List<@NotNull String> getCommandsOnClose() {
        return commandsOnClose;
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
    public void setCommandsOnClose(@NotNull List<@NotNull String> commands) {
        this.commandsOnClose = commands;
    }

    @Override
    public void rename(@NotNull Component newName) {

    }

    @Override
    public Inventory getInventory(Player player) {
        Inventory inv = Bukkit.createInventory(null, size, MessageFormatter.of(name).toComponent(player));
        for (int i = 0; i < this.inventory.getSize(); i++) {
            if (player != null) {
                inv.setItem(i, ItemUtils.getItemStack(player, inventory.getItem(i)));
            }
        }
        return inv;
    }

    public static class Builder extends IInventoryGUI.Builder {

        public Builder(String namespace, int size) {
            super(namespace, size);
        }

        @Override
        public IInventoryGUI build() {
            return new InventoryGUI(namespace, size, name, predicates, commands, items, closedSlots, commandsOnClose);
        }
    }
}
