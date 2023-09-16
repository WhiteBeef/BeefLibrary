package ru.whitebeef.beeflibrary.utils;

import de.tr7zw.nbtapi.NBTItem;
import net.kyori.adventure.text.format.TextDecoration;
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
import ru.leonidm.fastnbt.api.FastNBTItem;
import ru.whitebeef.beeflibrary.BeefLibrary;
import ru.whitebeef.beeflibrary.chat.MessageFormatter;
import ru.whitebeef.beeflibrary.placeholderapi.PAPIUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

//TODO: add flags and attributes
public class ItemGenerateProperties implements Cloneable {

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

        if (section.isInt("customModelData")) {
            builder.setCustomModelData(String.valueOf(section.getInt("customModelData")));
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
                enchantments.put(enchantment, MathUtils.getInt(enchantmentSection.getString("level")));
            }

            builder.setEnchantments(enchantments);
        }

        if (section.isConfigurationSection("nbt")) {
            ConfigurationSection nbtSection = section.getConfigurationSection("nbt");
            HashMap<String, Object> nbt = new HashMap<>();
            for (String path : nbtSection.getKeys(false)) {
                nbt.put(path, nbtSection.get(path));
            }
            builder.setNbt(Set.of(p -> nbt));
        }

        return builder.build();
    }

    private Function<Player, Material> material;
    private Function<Player, String> name;
    private Function<Player, List<String>> lore;
    private Function<Player, String> count;
    private Function<Player, String> customModelData;
    private Function<Player, Map<Enchantment, Integer>> enchantments;
    private Function<Player, String> damage;
    private Set<Function<Player, Map<String, Object>>> nbt;

    public ItemGenerateProperties(Function<Player, Material> material, Function<Player, String> name, Function<Player, List<String>> lore, Function<Player, String> count, Function<Player, String> customModelData, Function<Player, Map<Enchantment, Integer>> enchantments, Function<Player, String> damage, Set<Function<Player, Map<String, Object>>> nbt) {
        this.material = Objects.requireNonNullElse(material, (p) -> Material.STONE);
        this.name = Objects.requireNonNullElse(name, (p) -> "");
        this.lore = Objects.requireNonNullElse(lore, (p) -> Collections.emptyList());
        this.count = Objects.requireNonNullElse(count, (p) -> "1");
        this.customModelData = Objects.requireNonNullElse(customModelData, (p) -> "0");
        this.enchantments = Objects.requireNonNullElse(enchantments, (p) -> new HashMap<>());
        this.damage = Objects.requireNonNullElse(damage, (p) -> "0");
        this.nbt = Objects.requireNonNullElse(nbt, new HashSet<>());
    }

    @NotNull
    public ItemStack generate(@Nullable Player player) {
        ItemStack itemStack = new ItemStack(material.apply(player));

        Integer count = MathUtils.getInt(this.count.apply(player));
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

            Integer customModelData = MathUtils.getInt(this.customModelData.apply(player));
            if (customModelData != null) {
                meta.setCustomModelData(customModelData);
            }

            Integer damage = MathUtils.getInt(this.damage.apply(player));
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
        for (var function : this.nbt) {
            Map<String, Object> nbt = function.apply(player);
            if (nbt != null && !nbt.isEmpty()) {
                for (var entry : nbt.entrySet()) {
                    String path = entry.getKey();
                    if (BeefLibrary.getInstance().isFastNBT()) {
                        FastNBTItem item = FastNBTItem.write(itemStack, true);
                        if (entry.getValue() instanceof String str) {
                            Integer integer = MathUtils.getInt(str);
                            if (integer != null) {
                                item.setInt(path, integer);
                            } else {
                                item.setString(path, PAPIUtils.setPlaceholders(player, str));
                            }
                        } else if (entry.getValue() instanceof Integer value) {
                            item.setInt(path, value);
                        } else if (entry.getValue() instanceof Boolean value) {
                            item.setBoolean(path, value);
                        } else if (entry.getValue() instanceof Double value) {
                            item.setDouble(path, value);
                        } else if (entry.getValue() instanceof Byte value) {
                            item.setByte(path, value);
                        } else {
                            item.setString(path, GsonUtils.parseObject(entry.getValue()));
                        }
                    } else if (BeefLibrary.getInstance().isNBTAPI()) {
                        NBTItem item = new NBTItem(itemStack);
                        if (entry.getValue() instanceof String str) {
                            Integer integer = MathUtils.getInt(str);
                            if (integer != null) {
                                item.setInteger(path, integer);
                            } else {
                                item.setString(path, PAPIUtils.setPlaceholders(player, str));
                            }
                        } else if (entry.getValue() instanceof Integer value) {
                            item.setInteger(path, value);
                        } else if (entry.getValue() instanceof Boolean value) {
                            item.setBoolean(path, value);
                        } else if (entry.getValue() instanceof Double value) {
                            item.setDouble(path, value);
                        } else if (entry.getValue() instanceof Byte value) {
                            item.setByte(path, value);
                        } else {
                            item.setString(path, GsonUtils.parseObject(entry.getValue()));
                        }
                    }
                }
            }
        }
        ItemUtils.getItem(player, itemStack);

        return itemStack;
    }

    public ItemGenerateProperties setName(Function<@Nullable Player, String> name) {
        this.name = name;
        return this;
    }

    public ItemGenerateProperties setName(String name) {
        this.name = p -> name;
        return this;
    }

    public ItemGenerateProperties setMaterial(Function<@Nullable Player, Material> material) {
        this.material = material;
        return this;
    }

    public ItemGenerateProperties setMaterial(Material material) {
        this.material = p -> material;
        return this;
    }

    public ItemGenerateProperties setCount(Function<@Nullable Player, String> count) {
        this.count = count;
        return this;
    }

    public ItemGenerateProperties setCount(String count) {
        this.count = p -> count;
        return this;
    }

    public ItemGenerateProperties setCustomModelData(Function<@Nullable Player, String> customModelData) {
        this.customModelData = customModelData;
        return this;
    }

    public ItemGenerateProperties setCustomModelData(String customModelData) {
        this.customModelData = p -> customModelData;
        return this;
    }

    public ItemGenerateProperties setLore(Function<@Nullable Player, List<String>> lore) {
        this.lore = lore;
        return this;
    }

    public ItemGenerateProperties setLore(List<String> lore) {
        this.lore = p -> lore;
        return this;
    }

    public ItemGenerateProperties setDescription(List<String> description) {
        this.lore = p -> description;
        return this;
    }

    public ItemGenerateProperties setDescription(Function<@Nullable Player, List<String>> description) {
        this.lore = description;
        return this;
    }

    public ItemGenerateProperties setEnchantments(Function<@Nullable Player, Map<Enchantment, Integer>> enchantments) {
        this.enchantments = enchantments;
        return this;
    }


    public ItemGenerateProperties setEnchantments(Map<Enchantment, Integer> enchantments) {
        this.enchantments = p -> enchantments;
        return this;
    }

    public ItemGenerateProperties setDamage(Function<@Nullable Player, String> damage) {
        this.damage = damage;
        return this;
    }

    public ItemGenerateProperties setDamage(String damage) {
        this.damage = p -> damage;
        return this;
    }

    public ItemGenerateProperties setNbt(Set<Function<@Nullable Player, Map<String, Object>>> nbt) {
        this.nbt = nbt;
        return this;
    }

    public ItemGenerateProperties addNbt(Function<@Nullable Player, Map<String, Object>> nbt) {
        this.nbt.add(nbt);
        return this;
    }

    public ItemGenerateProperties addNbt(Map<String, Object> nbt) {
        this.nbt.add((p) -> nbt);
        return this;
    }

    public ItemGenerateProperties addNbt(String path, Object obj) {
        this.nbt.add(p -> new HashMap<>() {{
            put(path, obj);
        }});
        return this;
    }


    @Override
    public ItemGenerateProperties clone() {
        try {
            ItemGenerateProperties clone = (ItemGenerateProperties) super.clone();
            clone.setMaterial(material);
            clone.setName(name);
            clone.setLore(lore);
            clone.setCount(count);
            clone.setCustomModelData(customModelData);
            clone.setEnchantments(enchantments);
            clone.setDamage(damage);
            clone.setNbt(nbt);
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
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
        private Set<Function<@Nullable Player, Map<String, Object>>> nbt;

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

        public Builder addNbt(Function<@Nullable Player, Map<String, Object>> nbt) {
            this.nbt.add(nbt);
            return this;
        }

        public Builder addNbt(Map<String, Object> nbt) {
            this.nbt.add((p) -> nbt);
            return this;
        }

        public Builder addNbt(String path, Object obj) {
            this.nbt.add(p -> new HashMap<>() {{
                put(path, obj);
            }});
            return this;
        }

        public Builder setNbt(Set<Function<@Nullable Player, Map<String, Object>>> nbt) {
            this.nbt = nbt;
            return this;
        }


        public ItemGenerateProperties build() {
            return new ItemGenerateProperties(material, name, lore, count, customModelData, enchantments, damage, nbt);
        }
    }
}
