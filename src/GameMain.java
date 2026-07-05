import javax.swing.*;
import java.awt.*;

public class GameMain {
    public static void main(String[] args) {
        DatabaseManager.initializeDatabase();
        SwingUtilities.invokeLater(() -> {
            createAndShowGUI();
        });
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Chicken Invaders - My Project");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setSize(800, 600);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new CardLayout());
        LoginPanel loginPanel = new LoginPanel();
        MainMenuPanel mainMenuPanel = new MainMenuPanel();
        mainPanel.add(loginPanel, "LoginScreen");
        mainPanel.add(mainMenuPanel, "MainMenuScreen");

        frame.add(mainPanel);
        frame.setVisible(true);
    }
}