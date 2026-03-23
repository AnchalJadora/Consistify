package model;

import java.time.LocalDate;

public class HabitLog {
    private int id;
    private int habitId;
    private LocalDate logDate;
    private boolean completed;
    private String notes;

    public HabitLog(int habitId, LocalDate logDate, boolean completed, String notes) {
        this.habitId = habitId;
        this.logDate = logDate;
        this.completed = completed;
        this.notes = notes;
    }

    public HabitLog(int id, int habitId, LocalDate logDate, boolean completed, String notes) {
        this.id = id;
        this.habitId = habitId;
        this.logDate = logDate;
        this.completed = completed;
        this.notes = notes;
    }

    public int getId() { return id; }
    public int getHabitId() { return habitId; }
    public LocalDate getLogDate() { return logDate; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}