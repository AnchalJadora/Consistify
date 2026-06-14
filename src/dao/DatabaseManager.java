// responsible for connecting to database + creating tables

package dao;

import java.io.File;                        //to build correct file path for database
import java.sql.Connection;                 //connection to database
import java.sql.DriverManager;              //create connection to database
import java.sql.SQLException;               //handles database errors
import java.sql.Statement;                  //to execute the queries

public class DatabaseManager {

    //path to avoid multiple database file issue
    private static final String DB_PATH =
            System.getProperty("user.dir") + File.separator + "consistify.db";

    private static final String DB_URL = "jdbc:sqlite:" + DB_PATH;
    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL);

                Statement stmt = connection.createStatement();
                stmt.execute("PRAGMA foreign_keys = ON;");
                stmt.close();

                System.out.println("DB Connected: " + DB_URL);
            }
        } catch (SQLException e) {
            System.err.println("DB connection failed");
            e.printStackTrace();
            throw e;
        }

        return connection;
    }

    // creates database tables only if they do not exist
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

            System.out.println("Database initialized successfully");
        } catch (SQLException e) {
            System.err.println("Error initializing database");
            e.printStackTrace();
        }
    }

    // closes database connection
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                connection = null;
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection");
            e.printStackTrace();
        }
    }

    // prints database file location for debugging
    public static void printDBPath() {
        System.out.println("Database path: " + DB_PATH);
    }
}