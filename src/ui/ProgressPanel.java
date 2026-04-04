package ui;

import model.Habit;
import service.HabitService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ProgressPanel extends JPanel {

    private final HabitService habitService = new HabitService();
    private JComboBox<Habit> habitCombo;
    private JPanel chartPanel;
    private JLabel statsLabel;
    private JLabel statsLabel2;
    private JScrollPane chartScrollPane;
    private JScrollPane tableScrollPane;
    private JPanel contentArea;
    private CardLayout cardLayout;
    private int[] chartData = new int[0];
    private String[] chartLabels = new String[0];
    private String selectedHabitName = "";
    private String selectedFrequency = "DAILY";

    private static final String CARD_TABLE = "table";
    private static final String CARD_CHART = "chart";

    public ProgressPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 247, 250));

        // ── NORTH ───────────────────────────────────────────────
        JPanel northPanel = new JPanel(new BorderLayout(5, 5));
        northPanel.setBackground(new Color(245, 247, 250));

        JLabel title = new JLabel("Progress and Analytics", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(40, 40, 40));
        title.setBorder(new EmptyBorder(0, 0, 8, 0));
        northPanel.add(title, BorderLayout.NORTH);

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        topPanel.setBackground(new Color(245, 247, 250));
        topPanel.add(new JLabel("Select Habit: "));

        habitCombo = new JComboBox<>();
        habitCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        habitCombo.setPreferredSize(new Dimension(220, 30));
        loadHabits();
        topPanel.add(habitCombo);

        JButton showBtn = new JButton("Show Progress");
        styleButton(showBtn, new Color(70, 130, 180));
        showBtn.addActionListener(e -> showProgress());
        topPanel.add(showBtn);

        JButton resetBtn = new JButton("Show All Habits");
        styleButton(resetBtn, new Color(100, 100, 100));
        resetBtn.addActionListener(e -> showSummaryTable());
        topPanel.add(resetBtn);

        northPanel.add(topPanel, BorderLayout.SOUTH);
        add(northPanel, BorderLayout.NORTH);

        // ── CENTER ──────────────────────────────────────────────
        JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setBackground(new Color(245, 247, 250));

        JPanel statsPanel = new JPanel(new GridLayout(2, 1, 0, 3));
        statsPanel.setBackground(new Color(245, 247, 250));
        statsLabel = new JLabel(" ", SwingConstants.CENTER);
        statsLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statsLabel.setForeground(new Color(30, 30, 30));
        statsLabel2 = new JLabel(" ", SwingConstants.CENTER);
        statsLabel2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statsLabel2.setForeground(new Color(80, 80, 80));
        statsPanel.add(statsLabel);
        statsPanel.add(statsLabel2);
        center.add(statsPanel, BorderLayout.NORTH);

        // CardLayout — swaps table and chart
        cardLayout = new CardLayout();
        contentArea = new JPanel(cardLayout);
        contentArea.setBackground(new Color(245, 247, 250));

        tableScrollPane = new JScrollPane(buildSummaryTable());
        tableScrollPane.setBorder(BorderFactory.createEmptyBorder());
        contentArea.add(tableScrollPane, CARD_TABLE);

        chartPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawChart(g);
            }
        };
        chartPanel.setBackground(Color.WHITE);
        chartScrollPane = new JScrollPane(chartPanel);
        chartScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        chartScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        contentArea.add(chartScrollPane, CARD_CHART);

        center.add(contentArea, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        cardLayout.show(contentArea, CARD_TABLE);
    }

    // ── SHOW HABIT CHART ─────────────────────────────────────────
    private void showProgress() {
        Habit habit = (Habit) habitCombo.getSelectedItem();
        if (habit == null) return;

        selectedHabitName = habit.getName();
        selectedFrequency  = habit.getFrequency();
        LocalDate startDate = habit.getCreatedDate();
        LocalDate today     = LocalDate.now();

        // Frequency-aware data + labels
        chartData   = habitService.getFullHistory(habit.getId(), startDate, today, selectedFrequency);
        chartLabels = habitService.getFullHistoryLabels(startDate, today, selectedFrequency);

        // Dynamic chart width: 50px per slot
        int chartWidth = Math.max(700, chartData.length * 50 + 80);
        chartPanel.setPreferredSize(new Dimension(chartWidth, 280));
        chartPanel.revalidate();

        // Stats
        int currentStreak = habitService.getCurrentStreak(habit.getId());
        int longestStreak = habitService.getLongestStreak(habit.getId());

        long doneDots = 0;
        for (int d : chartData) if (d == 1) doneDots++;
        long missedDots = chartData.length - doneDots;
        double rate = chartData.length > 0 ? (double) doneDots / chartData.length * 100.0 : 0.0;

        // Label slots as "days" or "weeks" depending on frequency
        boolean isWeekly = "WEEKLY".equalsIgnoreCase(selectedFrequency);
        String unit = isWeekly ? "week(s)" : "day(s)";

        statsLabel.setText(String.format(
            "Current Streak: %d %s   |   Longest Streak: %d %s   |   Completion Rate: %.1f%%",
            currentStreak, unit, longestStreak, unit, rate));
        statsLabel2.setText(String.format(
            "Active Since: %s   |   Total %s: %d   |   Done: %d   |   Missed: %d   [%s]",
            startDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
            isWeekly ? "Weeks" : "Days",
            chartData.length, doneDots, missedDots,
            selectedFrequency));

        cardLayout.show(contentArea, CARD_CHART);
        chartPanel.repaint();
    }

    // ── SHOW SUMMARY TABLE ───────────────────────────────────────
    private void showSummaryTable() {
        statsLabel.setText(" ");
        statsLabel2.setText(" ");
        tableScrollPane.setViewportView(buildSummaryTable());
        cardLayout.show(contentArea, CARD_TABLE);
    }

    // ── BUILD TABLE ──────────────────────────────────────────────
    private JTable buildSummaryTable() {
        String[] columns = {"Habit", "Frequency", "Login Date", "Total Slots", "Done", "Missed", "Completion %"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        List<Habit> habits = habitService.getAllHabits();
        LocalDate today = LocalDate.now();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM yyyy");

        for (Habit habit : habits) {
            LocalDate startDate = habit.getCreatedDate();
            String freq = habit.getFrequency();
            int[] history = habitService.getFullHistory(habit.getId(), startDate, today, freq);
            long doneDays = 0;
            if (history != null) for (int d : history) if (d == 1) doneDays++;
            long missedDays = history.length - doneDays;
            double rate = history.length > 0 ? (double) doneDays / history.length * 100.0 : 0.0;

            model.addRow(new Object[]{
                habit.getName(),
                freq,
                startDate.format(fmt),
                history.length,
                doneDays,
                missedDays,
                String.format("%.1f%%", rate)
            });
        }

        JTable table = new JTable(model);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setRowHeight(38);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(new Color(210, 230, 255));
        table.setSelectionForeground(Color.BLACK);

        // Base renderer
        DefaultTableCellRenderer baseRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean selected, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, value, selected, focus, row, col);
                setForeground(new Color(30, 30, 30));
                if (!selected)
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(242, 246, 252));
                setBorder(new EmptyBorder(0, 14, 0, 14));
                setHorizontalAlignment(col == 0 ? SwingConstants.LEFT : SwingConstants.CENTER);
                return this;
            }
        };

        // Frequency badge renderer
        DefaultTableCellRenderer freqRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean selected, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, value, selected, focus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                setBorder(new EmptyBorder(0, 14, 0, 14));
                if (!selected) {
                    setBackground(row % 2 == 0 ? Color.WHITE : new Color(242, 246, 252));
                    if ("WEEKLY".equals(value)) {
                        setForeground(new Color(100, 60, 180)); // purple for weekly
                    } else {
                        setForeground(new Color(30, 120, 60));  // green for daily
                    }
                } else {
                    setForeground(Color.BLACK);
                }
                return this;
            }
        };

        // Completion % renderer
        DefaultTableCellRenderer rateRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean selected, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, value, selected, focus, row, col);
                setHorizontalAlignment(SwingConstants.CENTER);
                setForeground(new Color(30, 30, 30));
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                setBorder(new EmptyBorder(0, 14, 0, 14));
                if (!selected && value != null) {
                    double pct = Double.parseDouble(value.toString().replace("%", ""));
                    if (pct >= 75)      setBackground(new Color(160, 214, 160));
                    else if (pct >= 40) setBackground(new Color(250, 210, 100));
                    else                setBackground(new Color(240, 150, 150));
                }
                return this;
            }
        };

        table.getColumnModel().getColumn(0).setCellRenderer(baseRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(freqRenderer);
        for (int i = 2; i < 6; i++)
            table.getColumnModel().getColumn(i).setCellRenderer(baseRenderer);
        table.getColumnModel().getColumn(6).setCellRenderer(rateRenderer);

        // Header — custom renderer to beat Nimbus
        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getWidth(), 42));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean selected, boolean focus, int row, int col) {
                JLabel lbl = new JLabel(value == null ? "" : value.toString());
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                lbl.setForeground(Color.WHITE);
                lbl.setBackground(new Color(50, 90, 140));
                lbl.setOpaque(true);
                lbl.setBorder(new EmptyBorder(8, 14, 8, 14));
                lbl.setHorizontalAlignment(col == 0 ? SwingConstants.LEFT : SwingConstants.CENTER);
                return lbl;
            }
        });

        int[] widths = {170, 90, 115, 95, 80, 80, 115};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);

        return table;
    }

    // ── DRAW BAR CHART ───────────────────────────────────────────
    private void drawChart(Graphics g) {
        if (chartData == null || chartData.length == 0) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = chartPanel.getWidth(), h = chartPanel.getHeight();
        int barW = 36, gap = 14, startX = 60, baseY = h - 60;

        // Background
        g2.setColor(new Color(240, 248, 255));
        g2.fillRect(0, 0, w, h);

        // Title + frequency badge
        g2.setColor(Color.DARK_GRAY);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
        boolean isWeekly = "WEEKLY".equalsIgnoreCase(selectedFrequency);
        g2.drawString("Full History - " + selectedHabitName
            + "  [" + (isWeekly ? "Weekly" : "Daily") + "]", 20, 25);

        // Axes
        g2.setColor(Color.GRAY);
        g2.drawLine(55, 40, 55, baseY);
        g2.drawLine(55, baseY, w - 20, baseY);

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        g2.setColor(new Color(46, 139, 87));
        g2.drawString("Done", 5, baseY - 95);
        g2.setColor(new Color(220, 80, 80));
        g2.drawString("Miss", 5, baseY - 15);

        for (int i = 0; i < chartData.length; i++) {
            int x = startX + i * (barW + gap);
            boolean done = chartData[i] == 1;
            int barH = done ? 100 : 20;
            Color barColor = done ? new Color(46, 139, 87) : new Color(220, 80, 80);

            g2.setColor(barColor);
            g2.fillRoundRect(x, baseY - barH, barW, barH, 8, 8);

            // Label inside bar
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
            String lbl = done ? "D" : "M";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(lbl, x + (barW - fm.stringWidth(lbl)) / 2, baseY - barH + 14);

            // Date label rotated 45°
            g2.setColor(Color.DARK_GRAY);
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
            Graphics2D g2c = (Graphics2D) g2.create();
            g2c.translate(x + barW / 2, baseY + 5);
            g2c.rotate(Math.toRadians(45));
            g2c.drawString(chartLabels[i], 0, 0);
            g2c.dispose();
        }
    }

    // ── HELPERS ──────────────────────────────────────────────────
    private void loadHabits() {
        habitCombo.removeAllItems();
        habitService.getAllHabits().forEach(habitCombo::addItem);
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