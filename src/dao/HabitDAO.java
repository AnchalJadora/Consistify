package dao;

import model.Habit;
import model.HabitLog;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HabitDAO {

    // ─── Habit CRUD ───────────────────────────────────────────────

    public int addHabit(Habit habit) {
        String sql = "INSERT INTO habits (name, description, frequency, created_date, is_active) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, habit.getName());
            pstmt.setString(2, habit.getDescription());
            pstmt.setString(3, habit.getFrequency());
            pstmt.setString(4, habit.getCreatedDate().toString());
            pstmt.setInt(5, habit.isActive() ? 1 : 0);
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Error adding habit: " + e.getMessage());
        }
        return -1;
    }

    public List<Habit> getAllActiveHabits() {
        List<Habit> habits = new ArrayList<>();
        String sql = "SELECT * FROM habits WHERE is_active = 1 ORDER BY name";
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                habits.add(mapHabit(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching habits: " + e.getMessage());
        }
        return habits;
    }

    public void deleteHabit(int habitId) {
        String sql = "UPDATE habits SET is_active = 0 WHERE id = ?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, habitId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting habit: " + e.getMessage());
        }
    }

    // ─── Log CRUD ─────────────────────────────────────────────────

    public void logHabit(int habitId, LocalDate date, boolean completed, String notes) {
        String sql = "INSERT INTO habit_logs (habit_id, log_date, completed, notes) VALUES (?, ?, ?, ?) " +
                     "ON CONFLICT(habit_id, log_date) DO UPDATE SET completed = excluded.completed, notes = excluded.notes";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, habitId);
            pstmt.setString(2, date.toString());
            pstmt.setInt(3, completed ? 1 : 0);
            pstmt.setString(4, notes);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error logging habit: " + e.getMessage());
        }
    }

    public List<HabitLog> getLogsForHabit(int habitId) {
        List<HabitLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM habit_logs WHERE habit_id = ? ORDER BY log_date DESC";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, habitId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                logs.add(mapLog(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching logs: " + e.getMessage());
        }
        return logs;
    }

    public List<HabitLog> getLogsForDateRange(int habitId, LocalDate start, LocalDate end) {
        List<HabitLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM habit_logs WHERE habit_id = ? AND log_date BETWEEN ? AND ? ORDER BY log_date";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, habitId);
            pstmt.setString(2, start.toString());
            pstmt.setString(3, end.toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                logs.add(mapLog(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching logs for range: " + e.getMessage());
        }
        return logs;
    }

    public boolean isCompletedToday(int habitId) {
        String sql = "SELECT completed FROM habit_logs WHERE habit_id = ? AND log_date = ?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, habitId);
            pstmt.setString(2, LocalDate.now().toString());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("completed") == 1;
        } catch (SQLException e) {
            System.err.println("Error checking today's log: " + e.getMessage());
        }
        return false;
    }

    // ─── Helpers ──────────────────────────────────────────────────

    private Habit mapHabit(ResultSet rs) throws SQLException {
        return new Habit(
            rs.getInt("id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getString("frequency"),
            LocalDate.parse(rs.getString("created_date")),
            rs.getInt("is_active") == 1
        );
    }

    private HabitLog mapLog(ResultSet rs) throws SQLException {
        return new HabitLog(
            rs.getInt("id"),
            rs.getInt("habit_id"),
            LocalDate.parse(rs.getString("log_date")),
            rs.getInt("completed") == 1,
            rs.getString("notes")
        );
    }
}