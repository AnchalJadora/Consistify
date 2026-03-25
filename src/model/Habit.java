package model;

import java.time.LocalDate;

public class Habit {
    private int id;
    private String name;
    private String description;
    private String frequency;              
    private LocalDate createdDate;
    private boolean isActive;

    //Loading an EXISTING habit
    public Habit(int id, String name, String description, String frequency, LocalDate createdDate, boolean isActive) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.frequency = frequency;
        this.createdDate = createdDate;
        this.isActive = isActive;
    }
    //Creating a NEW habit
    public Habit(String name, String description, String frequency) {
        this.name = name;
        this.description = description;
        this.frequency = frequency;
        this.createdDate = LocalDate.now();
        this.isActive = true;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
    public LocalDate getCreatedDate() { return createdDate; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    @Override
    public String toString() { return name; }
}