import java.awt.*;

public class GameHUD {
    public static void draw(Graphics2D g2d, int score, int coins, int lives, int level) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 18));


        g2d.drawString("Score: " + score, 10, 25);

        // نمایش مرحله
        g2d.drawString("Level: " + level, 10, 45);

        // نمایش جان
        g2d.drawString("Lives: " + lives, 10, 65);

        // نمایش سکه‌های جمع‌آوری شده
        g2d.drawString("Coins: " + coins, 700, 25);

        // راهنمای سریع کلیدها
        g2d.setFont(new Font("Arial", Font.PLAIN, 12));
        g2d.drawString("P: Pause | ESC: Menu", 680, 580);
    }
}