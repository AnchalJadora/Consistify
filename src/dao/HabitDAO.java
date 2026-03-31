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
        try {
            PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, habit.getName());
            pstmt.setString(2, habit.getDescription());
            pstmt.setString(3, habit.getFrequency());
            pstmt.setString(4, habit.getCreatedDate().toString());
            pstmt.setInt(5, 1);
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Could not save habit: " + e.getMessage());
        }
        return -1;
    }

    // get all active habits from database
    public List<Habit> getAllActiveHabits() {
        List<Habit> habits = new ArrayList<>();
        String sql = "SELECT * FROM habits WHERE is_active = 1 ORDER BY name";
        try {
            Statement stmt = DatabaseManager.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                habits.add(makeHabit(rs));
            }
        } catch (SQLException e) {
            System.err.println("Could not fetch habits: " + e.getMessage());
        }
        return habits;
    }

    // marks habit as inactive
    public void deleteHabit(int habitId) {
        String sql = "UPDATE habits SET is_active = 0 WHERE id = ?";
        try {
            PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql);
            pstmt.setInt(1, habitId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Could not delete habit: " + e.getMessage());
        }
    }

   
    // save today's completion for a habit
    public void logHabit(int habitId, LocalDate date, boolean completed) {
        String sql = "INSERT INTO habit_logs (habit_id, log_date, completed) VALUES (?, ?, ?) " +
                     "ON CONFLICT(habit_id, log_date) DO UPDATE SET completed = excluded.completed";
        //if a log for this habit on this date already exists, just update it instead of creating a duplicate.
         
        try {
            PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql);
            pstmt.setInt(1, habitId);
            pstmt.setString(2, date.toString());
            pstmt.setInt(3, completed ? 1 : 0);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Could not log habit: " + e.getMessage());
        }
    }

    //get all logs for a habit
    public List<HabitLog> getLogsForHabit(int habitId) {
        List<HabitLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM habit_logs WHERE habit_id = ? ORDER BY log_date DESC";
        try {
            PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql);
            pstmt.setInt(1, habitId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                logs.add(makeLog(rs));
            }
        } catch (SQLException e) {
            System.err.println("Could not fetch logs: " + e.getMessage());
        }
        return logs;
    }

    // Get logs between two dates (used for 30 day completion rate)
    public List<HabitLog> getLogsForDateRange(int habitId, LocalDate start, LocalDate end) {
        List<HabitLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM habit_logs WHERE habit_id = ? AND log_date BETWEEN ? AND ?";
        try {
            PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql);
            pstmt.setInt(1, habitId);
            pstmt.setString(2, start.toString());
            pstmt.setString(3, end.toString());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                logs.add(makeLog(rs));
            }
        } catch (SQLException e) {
            System.err.println("Could not fetch logs for range: " + e.getMessage());
        }
        return logs;
    }

    // Check if habit is already done today
    public boolean isCompletedToday(int habitId) {
        String sql = "SELECT completed FROM habit_logs WHERE habit_id = ? AND log_date = ?";
        try {
            PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql);
            pstmt.setInt(1, habitId);
            pstmt.setString(2, LocalDate.now().toString());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("completed") == 1;
            }
        } catch (SQLException e) {
            System.err.println("Could not check today's log: " + e.getMessage());
        }
        return false;
    }

/*They convert raw database data into proper Java objects 
that the rest of the app can understand. */


    // convert a database row into a Habit object
    private Habit makeHabit(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        String frequency = rs.getString("frequency");
        LocalDate createdDate = LocalDate.parse(rs.getString("created_date"));
        boolean isActive = rs.getInt("is_active") == 1;
        return new Habit(id, name, description, frequency, createdDate, isActive);
    }

    // convert a database row into a HabitLog object
    private HabitLog makeLog(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int habitId = rs.getInt("habit_id");
        LocalDate date = LocalDate.parse(rs.getString("log_date"));
        boolean completed = rs.getInt("completed") == 1;
        return new HabitLog(id, habitId, date, completed, null);
    }
}