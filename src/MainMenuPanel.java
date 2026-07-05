import javax.swing.*;
import java.awt.*;

public class MainMenuPanel extends JPanel {
    public MainMenuPanel() {
        setLayout(new GridBagLayout());
        setBackground(Color.BLACK);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;


        JLabel titleLabel = new JLabel("CHICKEN INVADERS");
        titleLabel.setForeground(Color.YELLOW);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        gbc.gridy = 0; add(titleLabel, gbc);


        JButton newGameButton = new JButton("New Game");
        JButton scoresButton = new JButton("High Scores");
        JButton settingsButton = new JButton("Settings");
        JButton exitButton = new JButton("Exit");


        gbc.gridy = 1; add(newGameButton, gbc);
        gbc.gridy = 2; add(scoresButton, gbc);
        gbc.gridy = 3; add(settingsButton, gbc);
        gbc.gridy = 4; add(exitButton, gbc);


        exitButton.addActionListener(e -> System.exit(0));
    }
}