//Creates tables (habits and habit_logs) when the app starts for the first time


package dao;

//java built in sql tools
import java.sql.Connection;                 //connects to database
import java.sql.DriverManager;              //manages the database
import java.sql.SQLException;               //catch & handles exception 
import java.sql.Statement;                  //to write and execute sql queries

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:consistify.db";
    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
            //no connection or the previous connection was closed, make a new one
        }
        return connection;
    }


    //method that runs once at startup to set up the database tables
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