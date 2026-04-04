import dao.DatabaseManager;
import ui.MainFrame;
 
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
 
public class Main {
    public static void main(String[] args) {
        // FIX: Apply LookAndFeel BEFORE any UI components are created
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {}
 
        DatabaseManager.initializeDatabase();
        SwingUtilities.invokeLater(MainFrame::new);
    }
}
 
//Compile (from inside src/ folder):
//javac -encoding UTF-8 -cp "../lib/sqlite-jdbc-3.47.1.0.jar" -sourcepath . Main.java
 
//Run (from inside src/ folder):
//java -cp ".;../lib/sqlite-jdbc-3.47.1.0.jar" Main
 