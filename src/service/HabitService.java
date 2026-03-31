//this file does not do any real work itself
//it just receives requests from the UI and passes them to either HabitDAO or StreakCalculator to handle

package service;

import dao.HabitDAO;
import model.Habit;
import model.HabitLog;

import java.time.LocalDate;
import java.util.List;

public class HabitService {

    private final HabitDAO habitDAO = new HabitDAO();
    private final StreakCalculator streakCalculator = new StreakCalculator();

    public Habit createHabit(String name, String description, String frequency) {
        Habit habit = new Habit(name, description, frequency);
        int id = habitDAO.addHabit(habit);
        habit.setId(id);
        return habit;
    }

    public List<Habit> getAllHabits() {
        return habitDAO.getAllActiveHabits();
    }

    public void deleteHabit(int habitId) {
        habitDAO.deleteHabit(habitId);
    }

    public void markHabit(int habitId, boolean completed, String notes) {
        habitDAO.logHabit(habitId, LocalDate.now(), completed);
    }

    public boolean isCompletedToday(int habitId) {
        return habitDAO.isCompletedToday(habitId);
    }

    public int getCurrentStreak(int habitId) {
        return streakCalculator.getCurrentStreak(habitId);
    }

    public int getLongestStreak(int habitId) {
        return streakCalculator.getLongestStreak(habitId);
    }

    public double getCompletionRate(int habitId, LocalDate start, LocalDate end) {
        return streakCalculator.getCompletionRate(habitId, start, end);
    }

    public int[] getWeeklySummary(int habitId) {
        return streakCalculator.getWeeklySummary(habitId);
    }

    public List<HabitLog> getLogs(int habitId) {
        return habitDAO.getLogsForHabit(habitId);
    }
}