package ru.whitebeef.beeflibrary.utils;

import net.kyori.adventure.text.format.TextDecoration;
import org.apache.commons.lang3.RandomUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.whitebeef.beeflibrary.chat.MessageFormatter;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

//TODO: add nbt, flags and attributes
public class ItemGenerateProperties {

    public static Builder builder() {
        return new Builder();
    }


    public static ItemGenerateProperties of(ConfigurationSection section) {
        Builder builder = new Builder();

        if (section.isString("material")) {
            builder.setMaterial(Material.valueOf(section.getString("material")));
        }

        if (section.isString("name")) {
            builder.setName(section.getString("name"));
        }

        if (section.isString("count")) {
            builder.setCount(section.getString("count"));
        }

        if (section.isString("customModelData")) {
            builder.setCustomModelData(section.getString("customModelData"));
        }

        if (section.isString("damage")) {
            builder.setDamage(section.getString("damage"));
        }

        if (section.isList("lore")) {
            builder.setLore(section.getStringList("lore"));
        }

        if (section.isConfigurationSection("enchantments")) {
            HashMap<Enchantment, Integer> enchantments = new HashMap<>();

            for (String enchantmentString : section.getConfigurationSection("enchantments").getKeys(false)) {
                ConfigurationSection enchantmentSection = section.getConfigurationSection("enchantments." + enchantmentString);

                Enchantment enchantment = Enchantment.getByKey(NamespacedKey.fromString(enchantmentString));
                enchantments.put(enchantment, getInt(enchantmentSection.getString("level")));
            }

            builder.setEnchantments(enchantments);
        }

        return builder.build();
    }

    private final Function<Player, Material> material;
    private final Function<Player, String> name;
    private final Function<Player, List<String>> lore;
    private final Function<Player, String> count;
    private final Function<Player, String> customModelData;
    private final Function<Player, Map<Enchantment, Integer>> enchantments;
    private final Function<Player, String> damage;

    public ItemGenerateProperties(Function<Player, Material> material, Function<Player, String> name, Function<Player, List<String>> lore, Function<Player, String> count, Function<Player, String> customModelData, Function<Player, Map<Enchantment, Integer>> enchantments, Function<Player, String> damage) {
        this.material = Objects.requireNonNullElse(material, (p) -> Material.STONE);
        this.name = Objects.requireNonNullElse(name, (p) -> "");
        this.lore = Objects.requireNonNullElse(lore, (p) -> Collections.emptyList());
        this.count = Objects.requireNonNullElse(count, (p) -> "1");
        this.customModelData = Objects.requireNonNullElse(customModelData, (p) -> "0");
        this.enchantments = Objects.requireNonNullElse(enchantments, (p) -> new HashMap<>());
        this.damage = Objects.requireNonNullElse(damage, (p) -> "0");
    }

    @NotNull
    public ItemStack generate(@Nullable Player player) {
        ItemStack itemStack = new ItemStack(material.apply(player));

        Integer count = getInt(this.count.apply(player));
        if (count != null) {
            itemStack.setAmount(count);
        }

        if (itemStack.getItemMeta() != null) {
            ItemMeta meta = itemStack.getItemMeta();

            String name = this.name.apply(player);
            if (name != null && !name.isEmpty()) {
                meta.displayName(MessageFormatter.of(name).toComponent(player).decoration(TextDecoration.ITALIC, false));
            }

            List<String> lore = this.lore.apply(player);
            if (lore != null && !lore.isEmpty()) {
                meta.lore(lore.stream().map(line -> MessageFormatter.of(line).toComponent(player).decoration(TextDecoration.ITALIC, false)).collect(Collectors.toList()));
            }

            Integer customModelData = getInt(this.customModelData.apply(player));
            if (customModelData != null) {
                meta.setCustomModelData(customModelData);
            }

            Integer damage = getInt(this.damage.apply(player));
            if (damage != null && meta instanceof Damageable damageable) {
                damageable.setDamage(damage);
            }

            Map<Enchantment, Integer> enchantments = this.enchantments.apply(player);
            if (enchantments != null && !enchantments.isEmpty()) {
                for (var entry : enchantments.entrySet()) {
                    meta.addEnchant(entry.getKey(), entry.getValue(), true);
                }
            }

            itemStack.setItemMeta(meta);
        }

        ItemUtils.getItem(player, itemStack);

        return itemStack;
    }

    private static Integer getInt(String line) {
        try {
            return Integer.parseInt(line);
        } catch (NumberFormatException e) {
            try {
                String[] arr = line.split("\\.\\.");
                return RandomUtils.nextInt(Integer.parseInt(arr[0]), Integer.parseInt(arr[1]) + 1);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
    }

    public static class Builder {

        private Function<@Nullable Player, Material> material;
        private Function<@Nullable Player, String> name;
        private Function<@Nullable Player, List<String>> lore;
        private Function<@Nullable Player, String> count;
        private Function<@Nullable Player, String> customModelData;
        private Function<@Nullable Player, Map<Enchantment, Integer>> enchantments;
        private Function<@Nullable Player, String> damage;

        public Builder setName(Function<@Nullable Player, String> name) {
            this.name = name;
            return this;
        }

        public Builder setName(String name) {
            this.name = p -> name;
            return this;
        }

        public Builder setMaterial(Function<@Nullable Player, Material> material) {
            this.material = material;
            return this;
        }

        public Builder setMaterial(Material material) {
            this.material = p -> material;
            return this;
        }

        public Builder setCount(Function<@Nullable Player, String> count) {
            this.count = count;
            return this;
        }

        public Builder setCount(String count) {
            this.count = p -> count;
            return this;
        }

        public Builder setCustomModelData(Function<@Nullable Player, String> customModelData) {
            this.customModelData = customModelData;
            return this;
        }

        public Builder setCustomModelData(String customModelData) {
            this.customModelData = p -> customModelData;
            return this;
        }

        public Builder setLore(Function<@Nullable Player, List<String>> lore) {
            this.lore = lore;
            return this;
        }

        public Builder setLore(List<String> lore) {
            this.lore = p -> lore;
            return this;
        }

        public Builder setDescription(List<String> description) {
            this.lore = p -> description;
            return this;
        }

        public Builder setDescription(Function<@Nullable Player, List<String>> description) {
            this.lore = description;
            return this;
        }

        public Builder setEnchantments(Function<@Nullable Player, Map<Enchantment, Integer>> enchantments) {
            this.enchantments = enchantments;
            return this;
        }

        public Builder setEnchantments(Map<Enchantment, Integer> enchantments) {
            this.enchantments = p -> enchantments;
            return this;
        }

        public Builder setDamage(Function<@Nullable Player, String> damage) {
            this.damage = damage;
            return this;
        }

        public Builder setDamage(String damage) {
            this.damage = p -> damage;
            return this;
        }

        public ItemGenerateProperties build() {
            return new ItemGenerateProperties(material, name, lore, count, customModelData, enchantments, damage);
        }
    }
}
