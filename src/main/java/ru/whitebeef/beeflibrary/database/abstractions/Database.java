package ru.whitebeef.beeflibrary.database.abstractions;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;
import ru.whitebeef.beeflibrary.database.Dialect;
import ru.whitebeef.beeflibrary.database.Table;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public abstract class Database {

    // PluginName, Database
    private static final Map<String, Database> databases = new HashMap<>();

    public static Database getDatabase(Plugin plugin, String name) {
        return databases.get(plugin.getName() + ":" + name);
    }

    public static Database getDatabase(String pluginName, String name) {
        return databases.get(pluginName + ":" + name);
    }

    public static List<Database> getDatabases(Plugin plugin) {
        return new HashSet<>(databases.entrySet()).stream()
                .filter(entry -> entry.getKey().startsWith(plugin.getName() + ":"))
                .map(Map.Entry::getValue).toList();
    }

    private Connection connection = null;

    private final Map<String, Table> tables = new HashMap<>();
    private final String host;
    private final String database;
    private final String username;
    private final String password;
    private final Integer port;
    private final String SQL;
    private final Dialect dialect;

    public Database(Plugin plugin, String databasePath) {
        ConfigurationSection databaseSection = plugin.getConfig().getConfigurationSection(databasePath);

        dialect = Dialect.valueOf(databaseSection.getString("sql-dialect").toUpperCase());
        host = databaseSection.getString("host");
        database = databaseSection.getString("database");
        username = databaseSection.getString("username");
        password = databaseSection.getString("password");
        port = databaseSection.getInt("port");

        switch (dialect) {
            case MYSQL -> {
                try {
                    Class.forName("com.mysql.cj.jdbc.Driver");
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
                SQL = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useUnicode=true&autoReconnect=true&passwordCharacterEncoding=utf8&characterEncoding=utf8&useSSL=false&useTimezone=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
            }
            case SQLITE -> {
                File dataFolder = new File(plugin.getDataFolder(), database + ".db");
                if (!dataFolder.exists()) {
                    try {
                        if (!dataFolder.createNewFile()) {
                            Bukkit.getLogger().info("Could not create a database file!");
                        }
                    } catch (IOException e) {
                        Bukkit.getLogger().info("File write error: " + database + ".db");
                    }
                }
                SQL = "jdbc:sqlite:" + dataFolder;
            }
            default -> {
                File dataFolder = new File(plugin.getDataFolder(), database + ".db");
                if (!dataFolder.exists()) {
                    try {
                        if (!dataFolder.createNewFile()) {
                            Bukkit.getLogger().info("Could not create a database file!");
                        }
                    } catch (IOException e) {
                        Bukkit.getLogger().info("File write error: " + database + ".db");
                    }
                }
                SQL = "jdbc:sqlite:" + dataFolder;
            }
        }
        databases.put(plugin.getName() + ":" + database, this);
    }


    public synchronized void setup() {
        connection = getConnection();
        for (Table table : tables.values()) {
            table.setup(this);
        }
    }

    public synchronized void close() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException ignored) {
        }
    }

    /**
     * Returns connection to the database.
     * If connection isn't established yet this method will create one.
     *
     * @param forceReconnect if true will close existing connection to establish a new one
     * @return a {@link Connection}
     */
    public synchronized final Connection getConnection(boolean forceReconnect) {
        try {
            // Close existing connection if
            if (forceReconnect && connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(SQL, username, password);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    /**
     * Returns connection to the database.
     * If connection isn't established yet this method will create one.
     * This method won't close the existing connections.
     *
     * @return a {@link Connection}
     */
    public synchronized final Connection getConnection() {
        return getConnection(false);
    }

    public final Database addTable(Table table) {
        tables.put(table.getName(), table);
        return this;
    }

    @Nullable
    public final Table getTable(String name) {
        return tables.get(name);
    }

    public Dialect getDialect() {
        return dialect;
    }

    public String getDatabaseName() {
        return database;
    }


    @Override
    public String toString() {
        return "Database{" +
                "tables=" + tables +
                ", host='" + host + '\'' +
                ", database='" + database + '\'' +
                ", port=" + port +
                ", dialect=" + dialect +
                '}';
    }
}
