import javax.swing.JFrame;

public class GameMain {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Chicken Invaders - Galactic Edition");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        GamePanel gamePanel = new GamePanel();
        frame.add(gamePanel);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}