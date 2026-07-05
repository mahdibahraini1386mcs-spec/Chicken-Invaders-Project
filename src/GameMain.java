import javax.swing.*;
import java.awt.*;

public class GameMain {
    public static void main(String[] args) {

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
        mainPanel.setBackground(Color.BLACK);

        frame.add(mainPanel);
        frame.setVisible(true);
    }
}