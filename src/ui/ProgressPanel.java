package ui;

import model.Habit;
import service.HabitService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class ProgressPanel extends JPanel {

    private final HabitService habitService = new HabitService();
    private JComboBox<Habit> habitCombo;
    private JPanel chartPanel;
    private JLabel statsLabel;

    public ProgressPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel title = new JLabel("Progress & Analytics", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        // Top: Habit selector
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        topPanel.add(new JLabel("Select Habit: "));
        habitCombo = new JComboBox<>();
        habitCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        habitCombo.setPreferredSize(new Dimension(220, 30));
        loadHabits();
        topPanel.add(habitCombo);

        JButton showBtn = new JButton(" Show Progress");
        styleButton(showBtn, new Color(70, 130, 180));
        showBtn.addActionListener(e -> showProgress());
        topPanel.add(showBtn);

        add(topPanel, BorderLayout.NORTH);

        // Center: chart + stats
        JPanel center = new JPanel(new BorderLayout(10, 10));

        statsLabel = new JLabel(" ", SwingConstants.CENTER);
        statsLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        center.add(statsLabel, BorderLayout.NORTH);

        chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawChart(g);
            }
        };
        chartPanel.setPreferredSize(new Dimension(700, 300));
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        center.add(new JScrollPane(chartPanel), BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);
    }

    private int[] weeklyData = new int[7];
    private String selectedHabitName = "";

    private void showProgress() {
        Habit habit = (Habit) habitCombo.getSelectedItem();
        if (habit == null) return;

        selectedHabitName = habit.getName();
        weeklyData = habitService.getWeeklySummary(habit.getId());

        int currentStreak = habitService.getCurrentStreak(habit.getId());
        int longestStreak = habitService.getLongestStreak(habit.getId());
        double rate = habitService.getCompletionRate(habit.getId(),
                LocalDate.now().minusDays(29), LocalDate.now());

        statsLabel.setText(String.format(
            " Current Streak: %d days   |    Longest Streak: %d days   |   30-Day Rate: %.1f%%",
            currentStreak, longestStreak, rate));

        chartPanel.repaint();
    }

    private void drawChart(Graphics g) {
        if (weeklyData == null) return;
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = chartPanel.getWidth(), h = chartPanel.getHeight();
        int barW = 60, gap = 20, startX = 60, baseY = h - 60;

        g2.setColor(new Color(240, 248, 255));
        g2.fillRect(0, 0, w, h);

        // Title
        g2.setColor(Color.DARK_GRAY);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g2.drawString("Last 7 Days – " + selectedHabitName, 20, 25);

        // Y-axis label
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        g2.drawString("Done", 5, baseY - 55);
        g2.drawLine(55, 40, 55, baseY);
        g2.drawLine(55, baseY, w - 20, baseY);

        String[] days = {"6d ago", "5d ago", "4d ago", "3d ago", "2d ago", "Yesterday", "Today"};

        for (int i = 0; i < 7; i++) {
            int x = startX + i * (barW + gap);
            int barH = weeklyData[i] == 1 ? 100 : 5;
            Color barColor = weeklyData[i] == 1 ? new Color(46, 139, 87) : new Color(200, 200, 200);

            g2.setColor(barColor);
            g2.fillRoundRect(x, baseY - barH, barW, barH, 10, 10);

            g2.setColor(Color.DARK_GRAY);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            g2.drawString(weeklyData[i] == 1 ? "✓" : "✗", x + barW / 2 - 5, baseY - barH - 5);

            g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            g2.drawString(days[i], x + 2, baseY + 15);
        }
    }

    private void loadHabits() {
        habitCombo.removeAllItems();
        List<Habit> habits = habitService.getAllHabits();
        habits.forEach(habitCombo::addItem);
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