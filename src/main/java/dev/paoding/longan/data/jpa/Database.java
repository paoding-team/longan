package dev.paoding.longan.data.jpa;

import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {
    public static final String POSTGRESQL = "postgresql";
    public static final String MYSQL = "mysql";
    public static final String ORACLE = "oracle";
    private static String type;
    private static Connection connection;

    public static String getType() {
        return type;
    }

    public static void setType(String type) {
        Database.type = type;
    }

    public static void init(String url, String username, String password) {
        if(type != null){
            return;
        }
        url = url.toLowerCase();
        if (url.contains(Database.POSTGRESQL)) {
            type = Database.POSTGRESQL;
        } else if (url.contains(Database.MYSQL)) {
            type = Database.MYSQL;
        } else if (url.contains(Database.ORACLE)) {
            type = Database.ORACLE;
        }

        try {
            connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void destroy() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static Array createArrayOf(Object object) {
        Class<?> componentType = object.getClass().getComponentType();
        String typeName = null;
        if (String.class.isAssignableFrom(componentType)) {
            typeName = "text";
        } else if (Integer.class.isAssignableFrom(componentType)) {
            typeName = "int4";
        }
        try {
            return connection.createArrayOf(typeName, (Object[]) object);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean isPostgresql() {
        return type.equals(POSTGRESQL);
    }

    public static boolean isMySQL() {
        return type.equals(MYSQL);
    }

    public static boolean isOracle() {
        return type.endsWith(ORACLE);
    }
}
