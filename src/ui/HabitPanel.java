package ui;

import model.Habit;
import service.HabitService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class HabitPanel extends JPanel {

    private final HabitService habitService = new HabitService();
    private DefaultListModel<Habit> listModel;
    private JList<Habit> habitList;

    public HabitPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Title
        JLabel title = new JLabel("Your Habits", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        // List
        listModel = new DefaultListModel<>();
        habitList = new JList<>(listModel);
        habitList.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        habitList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        habitList.setCellRenderer(new HabitCellRenderer());
        loadHabits();

        JScrollPane scrollPane = new JScrollPane(habitList);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Active Habits"));
        add(scrollPane, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));

        JButton addBtn = new JButton("Add Habit");
        JButton deleteBtn = new JButton("Delete");
        JButton refreshBtn = new JButton("Refresh");

        styleButton(addBtn, new Color(46, 139, 87));
        styleButton(deleteBtn, new Color(178, 34, 34));
        styleButton(refreshBtn, new Color(70, 130, 180));

        addBtn.addActionListener(e -> showAddHabitDialog());
        deleteBtn.addActionListener(e -> deleteSelectedHabit());
        refreshBtn.addActionListener(e -> loadHabits());

        btnPanel.add(addBtn);
        btnPanel.add(deleteBtn);
        btnPanel.add(refreshBtn);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void loadHabits() {
        listModel.clear();
        List<Habit> habits = habitService.getAllHabits();
        habits.forEach(listModel::addElement);
    }

    private void showAddHabitDialog() {
        JTextField nameField = new JTextField();
        JTextField descField = new JTextField();
        String[] freqOptions = {"DAILY", "WEEKLY"};
        JComboBox<String> freqBox = new JComboBox<>(freqOptions);

        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        panel.add(new JLabel("Habit Name:"));   panel.add(nameField);
        panel.add(new JLabel("Description:"));  panel.add(descField);
        panel.add(new JLabel("Frequency:"));    panel.add(freqBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add New Habit",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Habit name cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            habitService.createHabit(name, descField.getText().trim(), (String) freqBox.getSelectedItem());
            loadHabits();
            JOptionPane.showMessageDialog(this, "Habit \"" + name + "\" added! ", "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deleteSelectedHabit() {
        Habit selected = habitList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this, "Please select a habit to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete \"" + selected.getName() + "\"? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            habitService.deleteHabit(selected.getId());
            loadHabits();
        }
    }

    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBorderPainted(false);
        btn.setOpaque(true);
    }

    // Custom renderer
    static class HabitCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean hasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, hasFocus);
            if (value instanceof Habit h) {
                setText( h.getName() + "  [" + h.getFrequency() + "]"
                        + (h.getDescription().isEmpty() ? "" : " — " + h.getDescription()));
            }
            return this;
        }
    }
}