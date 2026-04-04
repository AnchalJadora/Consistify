package service;

import dao.HabitDAO;
import model.Habit;
import model.HabitLog;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
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

    /**
     * For DAILY habits: returns one slot per day (1 = done, 0 = missed).
     * For WEEKLY habits: returns one slot per week (1 = done that week, 0 = missed).
     * Labels are set via the companion method getFullHistoryLabels().
     */
    public int[] getFullHistory(int habitId, LocalDate startDate, LocalDate endDate, String frequency) {
        List<HabitLog> logs = habitDAO.getLogsForDateRange(habitId, startDate, endDate);

        if ("WEEKLY".equalsIgnoreCase(frequency)) {
            // Build week slots from start of the week containing startDate → endDate
            LocalDate weekStart = startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate weekEnd   = endDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            int totalWeeks = (int) ChronoUnit.WEEKS.between(weekStart, weekEnd) + 1;

            int[] result = new int[totalWeeks];

            for (HabitLog log : logs) {
                if (log.isCompleted()) {
                    // Which week slot does this log fall into?
                    LocalDate logWeekStart = log.getLogDate()
                            .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                    int idx = (int) ChronoUnit.WEEKS.between(weekStart, logWeekStart);
                    if (idx >= 0 && idx < totalWeeks) {
                        result[idx] = 1; // mark the whole week as done
                    }
                }
            }
            return result;

        } else {
            // DAILY: one slot per day
            int totalDays = (int) (endDate.toEpochDay() - startDate.toEpochDay()) + 1;
            int[] result = new int[totalDays];
            for (HabitLog log : logs) {
                if (log.isCompleted()) {
                    int idx = (int) (log.getLogDate().toEpochDay() - startDate.toEpochDay());
                    if (idx >= 0 && idx < totalDays) result[idx] = 1;
                }
            }
            return result;
        }
    }

    /**
     * Returns the labels array that matches getFullHistory() output.
     * DAILY  → "MMM d" per day
     * WEEKLY → "MMM d" of each Monday (week start)
     */
    public String[] getFullHistoryLabels(LocalDate startDate, LocalDate endDate, String frequency) {
        java.time.format.DateTimeFormatter fmt =
                java.time.format.DateTimeFormatter.ofPattern("MMM d");

        if ("WEEKLY".equalsIgnoreCase(frequency)) {
            LocalDate weekStart = startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate weekEnd   = endDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            int totalWeeks = (int) ChronoUnit.WEEKS.between(weekStart, weekEnd) + 1;
            String[] labels = new String[totalWeeks];
            for (int i = 0; i < totalWeeks; i++) {
                labels[i] = "W: " + weekStart.plusWeeks(i).format(fmt);
            }
            return labels;
        } else {
            int totalDays = (int) (endDate.toEpochDay() - startDate.toEpochDay()) + 1;
            String[] labels = new String[totalDays];
            for (int i = 0; i < totalDays; i++) {
                labels[i] = startDate.plusDays(i).format(fmt);
            }
            return labels;
        }
    }

    /**
     * Completion rate aware of frequency:
     * DAILY  → completedDays / totalDays
     * WEEKLY → completedWeeks / totalWeeks
     */
    public double getCompletionRateSmart(int habitId, LocalDate startDate, LocalDate endDate, String frequency) {
        int[] history = getFullHistory(habitId, startDate, endDate, frequency);
        if (history.length == 0) return 0.0;
        long done = 0;
        for (int d : history) if (d == 1) done++;
        return (double) done / history.length * 100.0;
    }

    /** Convenience: total slots (days or weeks) for stats display */
    public int getTotalSlots(LocalDate startDate, LocalDate endDate, String frequency) {
      
        if ("WEEKLY".equalsIgnoreCase(frequency)) {
            LocalDate weekStart = startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate weekEnd   = endDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            return (int) ChronoUnit.WEEKS.between(weekStart, weekEnd) + 1;
        } else {
            return (int) (endDate.toEpochDay() - startDate.toEpochDay()) + 1;
        }
    }
}