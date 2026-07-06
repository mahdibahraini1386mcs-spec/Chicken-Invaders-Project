import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class MainMenuPanel extends JPanel {

    private JButton btnNewGame;
    private JButton btnHighScores;
    private JButton btnSettings;
    private JButton btnHowToPlay;
    private JButton btnStore;
    private JButton btnExit;

    public MainMenuPanel() {
        initComponents();
        setupLayout();
    }

    private void initComponents() {
        btnNewGame = createStyledButton("New Game");
        btnHighScores = createStyledButton("High Scores");
        btnSettings = createStyledButton("Settings");
        btnHowToPlay = createStyledButton("How to Play");
        btnStore = createStyledButton("Store (Optional)");
        btnExit = createStyledButton("Exit");
    }

    private void setupLayout() {
        setBackground(new Color(30, 30, 40));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel("CHICKEN INVADERS");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 42));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        add(Box.createVerticalStrut(80));
        add(lblTitle);
        add(Box.createVerticalStrut(60));

        add(btnNewGame);
        add(Box.createVerticalStrut(20));
        add(btnHighScores);
        add(Box.createVerticalStrut(20));
        add(btnSettings);
        add(Box.createVerticalStrut(20));
        add(btnHowToPlay);
        add(Box.createVerticalStrut(20));
        add(btnStore);
        add(Box.createVerticalStrut(20));
        add(btnExit);
        add(Box.createVerticalGlue());
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.PLAIN, 18));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(60, 60, 80));
        button.setFocusPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(250, 45));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    public void setNewGameAction(