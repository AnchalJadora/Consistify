package ui;

import dao.DatabaseManager;

import javax.swing.*;       //swing components like JFrame JButton
import java.awt.*;          //font and color

public class MainFrame extends JFrame {

    public MainFrame() {                        //constructor
        setTitle("Consistify - Habit Tracker");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        

        // Apply theme
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {}

        
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 14));

        tabs.addTab("My Habits", new HabitPanel());
        tabs.addTab("Track Today", new TrackingPanel());
        tabs.addTab("Progress", new ProgressPanel());

        add(tabs);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                DatabaseManager.closeConnection();
            }
        });

        setVisible(true);
    }
}
