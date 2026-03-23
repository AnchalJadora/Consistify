package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:consistify.db";
    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
        }
        return connection;
    }

    public static void initializeDatabase() {
        String createHabitsTable = """
            CREATE TABLE IF NOT EXISTS habits (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                description TEXT,
                frequency TEXT NOT NULL DEFAULT 'DAILY',
                created_date TEXT NOT NULL,
                is_active INTEGER NOT NULL DEFAULT 1
            );
        """;

        String createLogsTable = """
            CREATE TABLE IF NOT EXISTS habit_logs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                habit_id INTEGER NOT NULL,
                log_date TEXT NOT NULL,
                completed INTEGER NOT NULL DEFAULT 0,
                notes TEXT,
                FOREIGN KEY (habit_id) REFERENCES habits(id),
                UNIQUE(habit_id, log_date)
            );
        """;

        try (Statement stmt = getConnection().createStatement()) {
            stmt.execute(createHabitsTable);
            stmt.execute(createLogsTable);
            System.out.println("Database initialized successfully.");
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}