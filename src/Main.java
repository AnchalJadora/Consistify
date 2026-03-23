import dao.DatabaseManager;
import ui.MainFrame;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        DatabaseManager.initializeDatabase();
        SwingUtilities.invokeLater(MainFrame::new);
    }
}



//java -cp ".;../lib/sqlite-jdbc-3.47.1.0.jar" Main