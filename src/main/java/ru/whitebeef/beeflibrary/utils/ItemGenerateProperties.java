package ru.whitebeef.beeflibrary.utils;

import org.apache.commons.lang3.RandomUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class ItemGenerateProperties {

    public static Builder builder() {
        return new Builder();
    }

    private final Function<Player, Material> material;
    private final Function<Player, String> name;
    private final Function<Player, List<String>> lore;
    private final Function<Player, String> count;
    private final Function<Player, String> customModelData;

    public ItemGenerateProperties(Function<Player, Material> material, Function<Player, String> name, Function<Player, List<String>> lore, Function<Player, String> count, Function<Player, String> customModelData, Function<Player, String> nbt) {
        this.material = Objects.requireNonNullElse(material, (p) -> Material.STONE);
        this.name = Objects.requireNonNullElse(name, (p) -> "");
        this.lore = Objects.requireNonNullElse(lore, (p) -> Collections.emptyList());
        this.count = Objects.requireNonNullElse(count, (p) -> "1");
        this.customModelData = Objects.requireNonNullElse(customModelData, (p) -> "0");
    }

    @NotNull
    public ItemStack generate(@Nullable Player player) {
        ItemStack itemStack = new ItemStack(material.apply(player));

        Integer count = getInt(this.count.apply(player));
        if (count != null) {
            itemStack.setAmount(count);
        }

        if (itemStack.hasItemMeta()) {
            ItemMeta meta = itemStack.getItemMeta();

            String name = this.name.apply(player);
            if (name != null && !name.isEmpty()) {
                meta.setDisplayName(name);
            }

            List<String> lore = this.lore.apply(player);
            if (lore != null && !lore.isEmpty()) {
                meta.setLore(lore);
            }


            Integer customModelData = getInt(this.customModelData.apply(player));
            if (customModelData != null) {
                meta.setCustomModelData(customModelData);
            }

            itemStack.setItemMeta(meta);
        }

        return itemStack;
    }

    private Integer getInt(String line) {
        try {
            return Integer.parseInt(line);
        } catch (NumberFormatException e) {
            try {
                String[] arr = line.split("\\.\\.");
                return RandomUtils.nextInt(Integer.parseInt(arr[0]), Integer.parseInt(arr[1]));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
    }

    public static class Builder {

        private Function<Player, Material> material;
        private Function<Player, String> name;
        private Function<Player, List<String>> lore;
        private Function<Player, String> count;
        private Function<Player, String> customModelData;
        private Function<Player, String> nbt;

        public Builder setName(Function<Player, String> name) {
            this.name = name;
            return this;
        }

        public Builder setName(String name) {
            this.name = p -> name;
            return this;
        }

        public Builder setMaterial(Function<Player, Material> material) {
            this.material = material;
            return this;
        }

        public Builder setMaterial(Material material) {
            this.material = p -> material;
            return this;
        }

        public Builder setCount(Function<Player, String> count) {
            this.count = count;
            return this;
        }

        public Builder setCount(String count) {
            this.count = p -> count;
            return this;
        }

        public Builder setCustomModelData(Function<Player, String> customModelData) {
            this.customModelData = customModelData;
            return this;
        }

        public Builder setCustomModelData(String customModelData) {
            this.customModelData = p -> customModelData;
            return this;
        }

        public Builder setLore(Function<Player, List<String>> lore) {
            this.lore = lore;
            return this;
        }

        public Builder setDescription(List<String> description) {
            this.lore = p -> description;
            return this;
        }

        public Builder setNbt(Function<Player, String> nbt) {
            this.nbt = nbt;
            return this;
        }

        public Builder setNbt(String nbt) {
            this.nbt = p -> nbt;
            return this;
        }

        public ItemGenerateProperties build() {
            return new ItemGenerateProperties(material, name, lore, count, customModelData, nbt);
        }
    }
}
