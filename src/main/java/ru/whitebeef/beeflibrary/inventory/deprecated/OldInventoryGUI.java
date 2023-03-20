package ru.whitebeef.beeflibrary.inventory.deprecated;


import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;
import ru.whitebeef.beeflibrary.chat.MessageFormatter;
import ru.whitebeef.beeflibrary.inventory.ClickHandler;
import ru.whitebeef.beeflibrary.utils.Color;
import ru.whitebeef.beeflibrary.utils.ItemUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Deprecated
public class OldInventoryGUI implements ClickHandler, Cloneable {

    private final Inventory inventory;
    private Map<Integer, List<String>> commands = new HashMap<>();
    private Map<Integer, String> kitSlots = new HashMap<>();
    private List<String> commandsOnClose = new ArrayList<>();
    private Set<Integer> closableSlots = new HashSet<>();
    private Map<Integer, ItemStack> slots = new HashMap<>();
    private Map<Integer, Pair<String, Pair<Pair<ItemStack, List<String>>, Pair<ItemStack, List<String>>>>> permissionSlots = new HashMap<>();
    private String name;
    private boolean forceClose;
    private final String nameSpace;
    private final int size;
    private OldInventoryGUI previousInventory = null;

    public OldInventoryGUI(String nameSpace, int size) {
        this.nameSpace = nameSpace;
        this.size = size;
        this.name = nameSpace;
        this.inventory = Bukkit.createInventory(null, size, name);
        setChangeable(false);
    }

    protected OldInventoryGUI(OldInventoryGUI oldInventoryGUI) {
        this.nameSpace = oldInventoryGUI.nameSpace;
        this.size = oldInventoryGUI.size;
        this.commands = oldInventoryGUI.commands;
        this.commandsOnClose = oldInventoryGUI.commandsOnClose;
        this.closableSlots = oldInventoryGUI.closableSlots;
        this.name = oldInventoryGUI.name;
        this.forceClose = oldInventoryGUI.forceClose;
        this.slots = oldInventoryGUI.slots;
        this.permissionSlots = oldInventoryGUI.permissionSlots;
        this.kitSlots = oldInventoryGUI.kitSlots;
        inventory = Bukkit.createInventory(null, size, name);
        inventory.setStorageContents(oldInventoryGUI.inventory.getStorageContents());
    }


    @Override
    public void onClick(InventoryClickEvent event) {
        ClickHandler.super.onClick(event);
    }


    public void setChangeable(boolean changeable) {
        if (!changeable) {
            for (int i = 0; i < inventory.getSize(); i++) {
                closableSlots.add(i);
            }
        } else {
            closableSlots.clear();
        }
    }

    public boolean isChangeableSlot(int slot) {
        return !closableSlots.contains(slot);
    }

    public void setChangeableSlot(int slot, boolean changeable) {
        if (changeable) {
            openSlot(slot);
        } else {
            closeSlot(slot);
        }
    }

    public void openSlot(int slot) {
        closableSlots.remove(slot);
    }

    public void closeSlot(int slot) {
        closableSlots.add(slot);
    }


    public void setSlots(Map<Integer, ItemStack> slots) {
        this.slots = slots;
    }


    public void setItem(ItemStack item, int slot, String[] commands) {
        setItem(item, slot, Arrays.asList(commands));
    }

    public void setItem(ItemStack item, int slot, List<String> commands) {
        setItem(item, slot);
        this.commands.put(slot, commands);
    }

    public void setItem(ItemStack item, int slot) {
        slots.put(slot, item);
    }

    public void setPermissionItem(int slot, String permission, ItemStack hasPermission, ItemStack notHasPermission, List<String> hasPermissionCommands, List<String> notHasPermissionCommands) {
        permissionSlots.put(slot, Pair.of(permission, Pair.of(Pair.of(hasPermission, hasPermissionCommands), Pair.of(notHasPermission, notHasPermissionCommands))));
    }

    @Nullable
    public ItemStack getItem(Player player, int slot) {
        if (permissionSlots.containsKey(slot)) {
            return player.hasPermission(permissionSlots.get(slot).left()) ? permissionSlots.get(slot).right().left().left() : permissionSlots.get(slot).right().right().left();
        }
        return slots.get(slot);
    }

    public void setName(String name) {
        this.name = Color.colorize(name);
    }

    public void removeItem(ItemStack item) {
        inventory.remove(item);
    }

    public void removeItem(int slot) {
        inventory.setItem(slot, null);
    }

    public boolean containsItem(ItemStack item) {
        return inventory.contains(item);
    }

    public String getName() {
        return name;
    }

    public boolean isForceClose() {
        return forceClose;
    }

    public String getNameSpace() {
        return nameSpace;
    }

    public int getSize() {
        return size;
    }

    public void setForceClose(boolean forceClose) {
        this.forceClose = forceClose;
    }

    /**
     * @return cloned Inventory
     */
    public Inventory getInventory(Player player) {
        Inventory inv = Bukkit.createInventory(null, size, name);
        for (int i = 0; i < this.inventory.getSize(); i++) {
            if (getItem(player, i) != null) {
                ItemStack itemStack = getItem(player, i).clone();
                if (itemStack.hasItemMeta()) {
                    ItemMeta meta = itemStack.getItemMeta();
                    if (meta.hasLore()) {
                        ArrayList<Component> lore = new ArrayList<>();
                        if (player != null) {
                            meta.getLore().forEach(str -> lore.add(MessageFormatter.of(str).toComponent(player).decoration(TextDecoration.ITALIC, false)));
                        }
                        meta.lore(lore);
                    }
                    if (meta.hasDisplayName()) {
                        if (player != null) {
                            meta.displayName(MessageFormatter.of(meta.getDisplayName()).toComponent(player).decoration(TextDecoration.ITALIC, false));
                        }
                    }

                    itemStack.setItemMeta(meta);
                }
                inv.setItem(i, itemStack);
            }
        }
        return inv;
    }

    public void setCommandsOnClose(List<String> commandsOnClose) {
        this.commandsOnClose = commandsOnClose;
    }

    public List<String> getCommands(Player player, int slot) {
        if (permissionSlots.containsKey(slot)) {
            return player.hasPermission(permissionSlots.get(slot).left()) ?
                    permissionSlots.get(slot).right().left().right() : permissionSlots.get(slot).right().right().right();
        }
        return commands.getOrDefault(slot, new ArrayList<>());
    }

    public void setCommand(int slot, List<String> commands) {
        this.commands.put(slot, commands);
    }

    private void setCommands(Map<Integer, List<String>> commands) {
        this.commands = new HashMap<>(commands);
    }

    private void setClosableSlots(Set<Integer> slots) {
        this.closableSlots = new HashSet<>(slots);
    }

    public List<String> getCommandsOnClose() {
        return commandsOnClose;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OldInventoryGUI that = (OldInventoryGUI) o;
        return forceClose == that.forceClose && size == that.size && Objects.equals(commands, that.commands) && Objects.equals(commandsOnClose, that.commandsOnClose) && Objects.equals(closableSlots, that.closableSlots) && Objects.equals(inventory, that.inventory) && Objects.equals(name, that.name) && Objects.equals(nameSpace, that.nameSpace) && Objects.equals(slots, that.slots);
    }

    @Override
    public int hashCode() {
        return Objects.hash(commands, commandsOnClose, closableSlots, inventory, name, forceClose, nameSpace, size, kitSlots);
    }

    @Override
    public OldInventoryGUI clone() {
        return new OldInventoryGUI(this);
    }

    public void returnItems(Inventory inventory, Player player) {
        ArrayList<ItemStack> newItems = ItemUtils.parseItemsMapToArrayList(ItemUtils.getNewItems(this, this.getInventory(player), inventory));
        ItemUtils.returnItems(player, newItems);
    }

    @Nullable
    public OldInventoryGUI getPreviousInventory() {
        return previousInventory;
    }

    public void setPreviousInventory(@Nullable OldInventoryGUI previousInventory) {
        this.previousInventory = previousInventory;
    }


}
