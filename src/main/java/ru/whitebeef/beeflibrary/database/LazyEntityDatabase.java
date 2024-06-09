package ru.whitebeef.beeflibrary.database;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;
import ru.whitebeef.beeflibrary.database.abstractions.Database;
import ru.whitebeef.beeflibrary.entites.LazyEntity;
import ru.whitebeef.beeflibrary.entites.LazyEntityData;
import ru.whitebeef.beeflibrary.utils.GsonUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class LazyEntityDatabase extends Database {

    private final Class<? extends LazyEntity> lazyEntityClass;
    private final Class<? extends LazyEntityData> lazyEntityDataClass;

    public LazyEntityDatabase(Plugin plugin, String databasePath, Class<? extends LazyEntity> lazyEntityClass, Class<? extends LazyEntityData> lazyEntityDataClass) {
        super(plugin, databasePath);
        this.lazyEntityClass = lazyEntityClass;
        this.lazyEntityDataClass = lazyEntityDataClass;
    }


    @Nullable
    public synchronized LazyEntity getLazyEntity(Plugin plugin, UUID entityUuid) {
        LazyEntity lazyEntity = null;

        String SQL = "SELECT * FROM LazyEntities WHERE uuid = '" + entityUuid + "' LIMIT 1;";
        Connection connection = getConnection();
        try (PreparedStatement statement = connection.prepareStatement(SQL);
             ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                try {
                    lazyEntity = lazyEntityClass.getDeclaredConstructor(Plugin.class, long.class, UUID.class, lazyEntityDataClass)
                            .newInstance(plugin, rs.getLong("id"), entityUuid, GsonUtils.parseJSON(rs.getString("data"), lazyEntityDataClass));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return lazyEntity;
    }

    public synchronized LazyEntity saveLazyEntity(LazyEntity lazyEntity) {
        String SQL = switch (getDialect()) {
            case SQLITE -> "INSERT INTO LazyEntities (uuid, data) VALUES (?,?) " +
                    "ON CONFLICT(uuid) DO UPDATE SET data = ?";
            case MYSQL -> "INSERT INTO LazyEntities (uuid, data) VALUES (?,?) " +
                    "ON DUPLICATE KEY UPDATE data = ?";
        };
        Connection connection = getConnection();
        try (PreparedStatement statement = connection.prepareStatement(SQL)) {

            statement.setString(1, lazyEntity.getEntityUuid().toString());
            String data = GsonUtils.parseObject(lazyEntity.getData());
            statement.setString(2, data);
            statement.setString(3, data);

            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        return getLazyEntity(Bukkit.getPluginManager().getPlugin(lazyEntity.getPluginName()), lazyEntity.getEntityUuid());
    }

}
