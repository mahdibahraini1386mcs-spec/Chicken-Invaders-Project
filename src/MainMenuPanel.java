import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainMenuPanel extends JPanel {

    private JPanel mainPanel;
    private GamePanel gamePanel;

    public MainMenuPanel(JPanel mainPanel, GamePanel gamePanel) {
        this.mainPanel = mainPanel;
        this.gamePanel = gamePanel;

        JButton startButton = new JButton("Start Game");
        JButton exitButton = new JButton("Exit");

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                CardLayout cl = (CardLayout) mainPanel.getLayout();
                cl.show(mainPanel, "GameScreen");
            }
        });

        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        this.add(startButton);
        this.add(exitButton);
    }
}