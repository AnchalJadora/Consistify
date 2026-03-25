package ui;

import model.Habit;
import service.HabitService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class TrackingPanel extends JPanel {

    private final HabitService habitService = new HabitService();
    private JPanel habitsContainer;

    public TrackingPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Track Today's Habits", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        habitsContainer = new JPanel();
        habitsContainer.setLayout(new BoxLayout(habitsContainer, BoxLayout.Y_AXIS));
        JScrollPane scroll = new JScrollPane(habitsContainer);
        scroll.setBorder(BorderFactory.createTitledBorder("Today's Checklist"));
        add(scroll, BorderLayout.CENTER);

        JButton refreshBtn = new JButton(" Refresh List");
        styleButton(refreshBtn, new Color(70, 130, 180));
        refreshBtn.addActionListener(e -> loadTrackingCards());
        JPanel south = new JPanel();
        south.add(refreshBtn);
        add(south, BorderLayout.SOUTH);

        loadTrackingCards();
    }

    private void loadTrackingCards() {
        habitsContainer.removeAll();
        List<Habit> habits = habitService.getAllHabits();

        if (habits.isEmpty()) {
            JLabel empty = new JLabel("No habits yet. Add some from the 'My Habits' tab! ", SwingConstants.CENTER);
            empty.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            habitsContainer.add(empty);
        }

        for (Habit habit : habits) {
            habitsContainer.add(createHabitCard(habit));
            habitsContainer.add(Box.createVerticalStrut(8));
        }

        habitsContainer.revalidate();
        habitsContainer.repaint();
    }

    private JPanel createHabitCard(Habit habit) {
        boolean doneToday = habitService.isCompletedToday(habit.getId());
        int streak = habitService.getCurrentStreak(habit.getId());

        JPanel card = new JPanel(new BorderLayout(10, 5));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(doneToday ? new Color(46, 139, 87) : new Color(180, 180, 180), 2),
                new EmptyBorder(10, 12, 10, 12)));
        card.setBackground(doneToday ? new Color(240, 255, 240) : Color.WHITE);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JLabel nameLabel = new JLabel(habit.getName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));

        JLabel streakLabel = new JLabel(" Streak: " + streak + " day(s)");
        streakLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        streakLabel.setForeground(new Color(200, 80, 20));

        JPanel left = new JPanel(new GridLayout(2, 1));
        left.setOpaque(false);
        left.add(nameLabel);
        left.add(streakLabel);

        JButton markBtn = new JButton(doneToday ? " Done" : "Mark Done");
        markBtn.setEnabled(!doneToday);
        styleButton(markBtn, doneToday ? Color.GRAY : new Color(46, 139, 87));
        markBtn.addActionListener(e -> {
            habitService.markHabit(habit.getId(), true, "");
            showCelebration(habit.getName());
            loadTrackingCards();
        });

        card.add(left, BorderLayout.CENTER);
        card.add(markBtn, BorderLayout.EAST);
        return card;
    }

    private void showCelebration(String habitName) {
        JOptionPane.showMessageDialog(this,
                " Great job! You completed:\n\"" + habitName + "\"\nKeep up the streak!",
                "Habit Completed!", JOptionPane.INFORMATION_MESSAGE);
    }

    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBorderPainted(false);
        btn.setOpaque(true);
    }
}