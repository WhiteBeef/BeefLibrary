package ru.whitebeef.beeflibrary.database;

import org.jetbrains.annotations.NotNull;
import ru.whitebeef.beeflibrary.database.abstractions.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Table {

    private final List<Column> columns = new ArrayList<>();
    private final String name;

    public String getName() {
        return name;
    }

    public Table(String name) {
        this.name = name;
    }

    public void setup(Database database) {
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ").append(name).append(" (");
        if (columns.size() > 0) {
            Column lastColumn = columns.get(columns.size() - 1);
            for (Column column : columns) {
                sql.append(column.getName()).append(" ").append(column.getType());
                if (column.isAutoIncrement()) {
                    sql.append(" ").append(switch (database.getDialect()) {
                        case MYSQL -> "AUTO_INCREMENT";
                        case SQLITE -> "AUTOINCREMENT";
                    });
                }
                if (column != lastColumn) {
                    sql.append(",");
                }
            }
            sql.append(")").append(";");
        }
        try (Connection con = database.getConnection();
             PreparedStatement products = con.prepareStatement(sql.toString())) {

            products.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Table addColumn(@NotNull Column column) {
        columns.add(column);
        return this;
    }

    public Table addColumn(@NotNull String name, String type) {
        columns.add(new Column(name, type));
        return this;
    }

}
