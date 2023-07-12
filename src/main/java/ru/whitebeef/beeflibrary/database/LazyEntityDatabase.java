package ru.whitebeef.beeflibrary.database;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;
import ru.whitebeef.beeflibrary.BeefLibrary;
import ru.whitebeef.beeflibrary.database.abstractions.Database;
import ru.whitebeef.beeflibrary.entites.LazyEntity;
import ru.whitebeef.beeflibrary.utils.GsonUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public class LazyEntityDatabase extends Database {
    private static LazyEntityDatabase instance;

    public static LazyEntityDatabase getInstance() {
        return instance;
    }


    public LazyEntityDatabase() {
        super(BeefLibrary.getInstance());

        instance = this;
    }


    @Nullable
    public LazyEntity getLazyEntity(Plugin plugin, UUID entityUuid) {
        LazyEntity lazyEntity = null;

        String SQL = "SELECT * FROM LazyEntities WHERE uuid = '" + entityUuid + "' LIMIT 1;";
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(SQL)) {
            if (rs.next()) {
                var pair = LazyEntity.getRegisteredTypes(plugin);
                try {
                    lazyEntity = pair.left().getDeclaredConstructor(Plugin.class, UUID.class, pair.right())
                            .newInstance(plugin, entityUuid, GsonUtils.parseJSON(rs.getString("data"), pair.right()));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return lazyEntity;
    }

    public boolean saveLazyEntity(LazyEntity lazyEntity) {
        boolean saved = true;
        String SQL = switch (getDialect()) {
            case SQLITE -> "INSERT INTO LazyPlayers (uuid, data) VALUES (?,?) " +
                    "ON CONFLICT(uuid) DO UPDATE SET data = ?";
            case MYSQL -> "INSERT INTO LazyPlayers (uuid, data) VALUES (?,?) " +
                    "ON DUPLICATE KEY UPDATE data = ?";
        };
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL)) {

            statement.setString(1, lazyEntity.getEntityUuid().toString());
            String data = GsonUtils.parseObject(lazyEntity.getData());
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
