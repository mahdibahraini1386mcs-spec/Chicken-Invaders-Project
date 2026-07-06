import java.awt.*;

public class GameHUD {
    public static void draw(Graphics2D g2d, int score, int highScore, int lives, int level) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("SansSerif", Font.BOLD, 18));

        g2d.drawString("Level: " + level, 20, 30);
        g2d.drawString("Score: " + score, 20, 60);
        g2d.drawString("High Score: " + highScore, 20, 90);
        g2d.drawString("Lives: ", 20, 120);

        g2d.setColor(Color.RED);
        for (int i = 0; i < lives; i++) {
            g2d.fillOval(80 + (i * 30), 105, 20, 20);
        }
    }
}