package service;

import dao.HabitDAO;
import model.HabitLog;

import java.time.LocalDate;
import java.util.List;

public class StreakCalculator {

    private final HabitDAO habitDAO = new HabitDAO();

    public int getCurrentStreak(int habitId) {
        List<HabitLog> logs = habitDAO.getLogsForHabit(habitId);
        if (logs.isEmpty()) return 0;

        int streak = 0;
        LocalDate checkDate = LocalDate.now();

        // FIX: If today has no completed log yet, start checking from yesterday.
        // This gives a grace period — the streak doesn't break just because
        // the user hasn't marked today's habit yet.
        boolean todayLogged = logs.stream()
            .anyMatch(l -> l.getLogDate().isEqual(LocalDate.now()) && l.isCompleted());

        if (!todayLogged) {
            checkDate = checkDate.minusDays(1);
        }

        for (HabitLog log : logs) {
            if (log.getLogDate().isEqual(checkDate) && log.isCompleted()) {
                streak++;
                checkDate = checkDate.minusDays(1);
            } else if (log.getLogDate().isBefore(checkDate)) {
                break; // Gap found, streak ends
            }
        }
        return streak;
    }

    public int getLongestStreak(int habitId) {
        List<HabitLog> logs = habitDAO.getLogsForHabit(habitId);
        if (logs.isEmpty()) return 0;
        logs.sort((a, b) -> a.getLogDate().compareTo(b.getLogDate()));

        int longest = 0, current = 0;
        LocalDate prev = null;

        for (HabitLog log : logs) {
            if (!log.isCompleted()) { current = 0; prev = null; continue; }
            if (prev == null || log.getLogDate().isEqual(prev.plusDays(1))) {
                current++;
            } else {
                current = 1;
            }
            prev = log.getLogDate();
            longest = Math.max(longest, current);
        }
        return longest;
    }

    public double getCompletionRate(int habitId, LocalDate start, LocalDate end) {
        List<HabitLog> logs = habitDAO.getLogsForDateRange(habitId, start, end);
        if (logs.isEmpty()) return 0.0;
        long completed = logs.stream().filter(HabitLog::isCompleted).count();
        return (double) completed / logs.size() * 100.0;
    }

    public int[] getWeeklySummary(int habitId) {
        // Returns completions per day for last 7 days (index 0 = 6 days ago, 6 = today)
        int[] summary = new int[7];
        LocalDate today = LocalDate.now();
        List<HabitLog> logs = habitDAO.getLogsForDateRange(habitId, today.minusDays(6), today);
        for (HabitLog log : logs) {
            if (log.isCompleted()) {
                int idx = (int) (today.toEpochDay() - log.getLogDate().toEpochDay());
                if (idx >= 0 && idx < 7) summary[6 - idx] = 1;
            }
        }
        return summary;
    }
}