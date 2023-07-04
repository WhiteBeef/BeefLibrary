package ru.whitebeef.beeflibrary.database;

import java.sql.JDBCType;

public record Column(String name, JDBCType type, String additional) {

}
