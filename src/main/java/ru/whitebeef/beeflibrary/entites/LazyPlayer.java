package ru.whitebeef.beeflibrary.entites;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public abstract class LazyPlayer extends LazyEntity {


    public LazyPlayer(Plugin plugin, long id, Player player, LazyEntityData data) {
        super(plugin, id, player.getUniqueId(), data);
    }

    public LazyPlayer(Plugin plugin, long id, Player player) {
        super(plugin, id, player.getUniqueId());
    }

    public LazyPlayer(Plugin plugin, long id, UUID uuid, LazyEntityData data) {
        super(plugin, id, uuid, data);
    }

    public LazyPlayer(Plugin plugin, long id, UUID uuid) {
        super(plugin, id, uuid);
    }

    @Nullable
    public Player getPlayer() {
        return Bukkit.getPlayer(getEntityUuid());
    }

}
