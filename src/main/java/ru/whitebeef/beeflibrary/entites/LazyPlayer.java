package ru.whitebeef.beeflibrary.entites;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public abstract class LazyPlayer extends LazyEntity {


    public LazyPlayer(Plugin plugin, Player player, LazyEntityData data) {
        super(plugin, player.getUniqueId(), data);
    }

    public LazyPlayer(Plugin plugin, Player player) {
        super(plugin, player.getUniqueId());
    }

    public LazyPlayer(Plugin plugin, UUID uuid, LazyEntityData data) {
        super(plugin, uuid, data);
    }

    public LazyPlayer(Plugin plugin, UUID uuid) {
        super(plugin, uuid);
    }

    @Nullable
    public Player getPlayer() {
        return Bukkit.getPlayer(getEntityUuid());
    }

}
