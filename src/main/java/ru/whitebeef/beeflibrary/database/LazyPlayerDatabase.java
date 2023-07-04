package ru.whitebeef.beeflibrary.database;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;
import ru.whitebeef.beeflibrary.BeefLibrary;
import ru.whitebeef.beeflibrary.database.abstractions.Database;
import ru.whitebeef.beeflibrary.entites.LazyPlayer;
import ru.whitebeef.beeflibrary.utils.GsonUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class LazyPlayerDatabase extends Database {
    private static LazyPlayerDatabase instance;

    public static LazyPlayerDatabase getInstance() {
        return instance;
    }


    public LazyPlayerDatabase() {
        super(BeefLibrary.getInstance());

        instance = this;
    }


    @Nullable
    public LazyPlayer getLazyPlayer(Plugin plugin, Player player) {
        LazyPlayer lazyPlayer = null;

        String SQL = "SELECT * FROM LazyPlayers WHERE uuid = '" + player.getUniqueId() + "' LIMIT 1;";
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(SQL)) {
            if (rs.next()) {
                var pair = LazyPlayer.getRegisteredTypes(plugin);
                try {
                    lazyPlayer = pair.left().getDeclaredConstructor(Plugin.class, Player.class, pair.right())
                            .newInstance(plugin, player, GsonUtils.parseJSON(rs.getString("data"), pair.right()));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return lazyPlayer;
    }

    public boolean saveRewardPlayer(LazyPlayer lazyPlayer) {
        boolean saved = true;
        String SQL = switch (getDialect()) {
            case SQLITE -> "INSERT INTO LazyPlayers (uuid, data) VALUES (?,?) " +
                    "ON CONFLICT(uuid) DO UPDATE SET data = ?";
            case MYSQL -> "INSERT INTO LazyPlayers (uuid, data) VALUES (?,?) " +
                    "ON DUPLICATE KEY UPDATE data = ?";
        };
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL)) {

            statement.setString(1, lazyPlayer.getPlayerUuid().toString());
            String data = GsonUtils.parseObject(lazyPlayer.getData());
            statement.setString(2, data);
            statement.setString(3, data);

            statement.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
            saved = false;
        }
        return saved;
    }

}
