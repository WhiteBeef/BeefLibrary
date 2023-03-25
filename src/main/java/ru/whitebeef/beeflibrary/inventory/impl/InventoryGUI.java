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
import ru.whitebeef.beeflibrary.inventory.CustomInventoryGUICommand;
import ru.whitebeef.beeflibrary.inventory.IInventoryGUI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public class InventoryGUI implements IInventoryGUI {

    private final Inventory inventory;
    private final String namespace;
    private final int size;
    private Component name;
    private final Map<Integer, @NotNull BiPredicate<@NotNull Player, @Nullable ItemStack>> predicates;
    private final Map<Integer, List<String>> commands;
    private List<String> commandsOnClose;

    private Set<Integer> closedSlots;

    @Override
    public void onClick(InventoryClickEvent event) {
        //TODO
    }

    @Override
    public void onDrag(InventoryDragEvent event) {
        //TODO
    }

    @Override
    public Builder builder(String namespace, int size) {
        return new Builder(namespace, size);
    }

    public InventoryGUI(@NotNull String namespace, int size, @NotNull Component name, @NotNull Map<Integer,
            @NotNull BiPredicate<@NotNull Player, @Nullable ItemStack>> predicates,
                        @NotNull Map<Integer, List<@NotNull String>> commands, @NotNull ItemStack[] @NotNull items,
                        @NotNull Set<@NotNull Integer> closedSlots, @NotNull List<@NotNull String> commandsOnClose) {
        this.namespace = namespace;
        this.size = size;
        this.name = name;
        this.predicates = predicates;
        this.commands = commands;
        this.closedSlots = closedSlots;
        this.commandsOnClose = commandsOnClose;
        inventory = Bukkit.createInventory(null, size, name);
    }

    @Override
    @NotNull
    public String getNamespace() {
        return namespace;
    }

    @Override
    @NotNull
    public Component getName() {
        return name;
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
        return predicates.getOrDefault(slot, (a, b) -> true).test(player, item);
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
        player.openInventory(inventory);
    }

    @Override
    @NotNull
    public Set<@NotNull Player> getOpenedPlayers() {
        return inventory.getViewers().stream().map(humanEntity -> (Player) humanEntity).collect(Collectors.toSet());
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

    }

    @Override
    public void rename(@NotNull Component newName) {

    }

    private static class Builder extends IInventoryGUI.Builder {

        public Builder(String namespace, int size) {
            super(namespace, size);
        }

        @Override
        public IInventoryGUI build() {
            return new InventoryGUI(namespace, size, name, predicates, commands, items, closedSlots, commandsOnClose);
        }
    }
}
