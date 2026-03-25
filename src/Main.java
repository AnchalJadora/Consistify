import dao.DatabaseManager;
import ui.MainFrame;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        DatabaseManager.initializeDatabase();           //Calls the database setup method
        SwingUtilities.invokeLater(MainFrame::new);     //opens the main window of the app
    }
}





//Compile
//javac -encoding UTF-8 -cp "../lib/sqlite-jdbc-3.47.1.0.jar" -sourcepath . Main.java
//Step 2 
//java -cp ".;../lib/sqlite-jdbc-3.47.1.0.jar" Main