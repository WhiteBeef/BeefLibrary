package ru.whitebeef.beeflibrary.database;

public class Column {
    private final String name;
    private final String type;

    public Column(String name, String type) {
        this.name = name;
        this.type = type;
    }

    private boolean autoIncrement = false;

    public Column autoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
        return this;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isAutoIncrement() {
        return autoIncrement;
    }
}
