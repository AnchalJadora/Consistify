package dao;

import model.Habit;
import model.HabitLog;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HabitDAO {

    // save a new habit to database, return its id
    public int addHabit(Habit habit) {

        String sql = "INSERT INTO habits (name, description, frequency, created_date, is_active) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, habit.getName());
            pstmt.setString(2, habit.getDescription());
            pstmt.setString(3, habit.getFrequency());
            pstmt.setString(4, habit.getCreatedDate().toString());
            pstmt.setInt(5, 1);

            int rows = pstmt.executeUpdate();

            if (rows == 0) {
                System.err.println("Habit insert failed");
                return -1;
            }

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            System.err.println("Could not save habit");
            e.printStackTrace();
        }

        return -1;
    }

    // get all active habits from database
    public List<Habit> getAllActiveHabits() {

        List<Habit> habits = new ArrayList<>();

        String sql = "SELECT * FROM habits WHERE is_active = 1 ORDER BY name";

        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                habits.add(makeHabit(rs));
            }

        } catch (SQLException e) {
            System.err.println("Could not fetch habits");
            e.printStackTrace();
        }

        return habits;
    }

    // marks habit as inactive
    public void deleteHabit(int habitId) {

        String sql = "UPDATE habits SET is_active = 0 WHERE id = ?";

        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {

            pstmt.setInt(1, habitId);

            int rows = pstmt.executeUpdate();

            if (rows == 0) {
                System.err.println("Delete failed: no habit found");
            }

        } catch (SQLException e) {
            System.err.println("Could not delete habit");
            e.printStackTrace();
        }
    }

    // save today's completion for a habit
    public void logHabit(int habitId, LocalDate date, boolean completed) {

        String sql = "INSERT INTO habit_logs (habit_id, log_date, completed) VALUES (?, ?, ?) " +
                     "ON CONFLICT(habit_id, log_date) DO UPDATE SET completed = excluded.completed";

        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {

            pstmt.setInt(1, habitId);
            pstmt.setString(2, date.toString());
            pstmt.setInt(3, completed ? 1 : 0);

            int rows = pstmt.executeUpdate();

            if (rows == 0) {
                System.err.println("Log insert/update failed");
            }

        } catch (SQLException e) {
            System.err.println("Could not log habit");
            e.printStackTrace();
        }
    }

    // get all logs for a habit
    public List<HabitLog> getLogsForHabit(int habitId) {

        List<HabitLog> logs = new ArrayList<>();

        String sql = "SELECT * FROM habit_logs WHERE habit_id = ? ORDER BY log_date DESC";

        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {

            pstmt.setInt(1, habitId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(makeLog(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Could not fetch logs");
            e.printStackTrace();
        }

        return logs;
    }

    // get logs between two dates
    public List<HabitLog> getLogsForDateRange(int habitId, LocalDate start, LocalDate end) {

        List<HabitLog> logs = new ArrayList<>();

        String sql = "SELECT * FROM habit_logs WHERE habit_id = ? AND log_date BETWEEN ? AND ?";

        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {

            pstmt.setInt(1, habitId);
            pstmt.setString(2, start.toString());
            pstmt.setString(3, end.toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    logs.add(makeLog(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Could not fetch logs for range");
            e.printStackTrace();
        }

        return logs;
    }

    // check if habit is completed today
    public boolean isCompletedToday(int habitId) {

        String sql = "SELECT completed FROM habit_logs WHERE habit_id = ? AND log_date = ?";

        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {

            pstmt.setInt(1, habitId);
            pstmt.setString(2, LocalDate.now().toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("completed") == 1;
                }
            }

        } catch (SQLException e) {
            System.err.println("Could not check today's log");
            e.printStackTrace();
        }

        return false;
    }

    // convert database row into Habit object
    private Habit makeHabit(ResultSet rs) throws SQLException {

        return new Habit(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("frequency"),
                LocalDate.parse(rs.getString("created_date")),
                rs.getInt("is_active") == 1
        );
    }

    // convert database row into HabitLog object
    private HabitLog makeLog(ResultSet rs) throws SQLException {

        return new HabitLog(
                rs.getInt("id"),
                rs.getInt("habit_id"),
                LocalDate.parse(rs.getString("log_date")),
                rs.getInt("completed") == 1,
                rs.getString("notes")
        );
    }
}