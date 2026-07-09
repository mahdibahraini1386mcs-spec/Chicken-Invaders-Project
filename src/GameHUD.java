import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class GameHUD {

    public static void draw(Graphics2D g2d, int score, int coins, int lives, int level) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        long time = System.currentTimeMillis();

        // ==========================================
        // ۱. پنل سمت چپ (بدون کادر، متن معلق با سایه)
        // ==========================================
        int leftX = 15;
        int leftY = 15;

        // رسم SCORE
        g2d.setFont(new Font("Monospaced", Font.BOLD, 15));
        g2d.setColor(Color.BLACK); // سایه
        g2d.drawString("SCORE:", leftX + 13, leftY + 26);
        g2d.setColor(new Color(200, 220, 255)); // رنگ اصلی
        g2d.drawString("SCORE:", leftX + 12, leftY + 25);

        g2d.setFont(new Font("Monospaced", Font.BOLD, 18));
        g2d.setColor(Color.BLACK);
        g2d.drawString(String.format("%06d", score), leftX + 76, leftY + 27);
        g2d.setColor(Color.CYAN);
        g2d.drawString(String.format("%06d", score), leftX + 75, leftY + 26);

        // رسم LEVEL
        g2d.setFont(new Font("Monospaced", Font.BOLD, 15));
        g2d.setColor(Color.BLACK);
        g2d.drawString("LEVEL:", leftX + 13, leftY + 54);
        g2d.setColor(new Color(200, 220, 255));
        g2d.drawString("LEVEL:", leftX + 12, leftY + 53);

        g2d.setFont(new Font("Monospaced", Font.BOLD, 18));
        g2d.setColor(Color.BLACK);
        g2d.drawString(String.valueOf(level), leftX + 76, leftY + 55);
        g2d.setColor(Color.MAGENTA);
        g2d.drawString(String.valueOf(level), leftX + 75, leftY + 54);

        // رسم LIVES
        g2d.setFont(new Font("Monospaced", Font.BOLD, 15));
        g2d.setColor(Color.BLACK);
        g2d.drawString("LIVES:", leftX + 13, leftY + 82);
        g2d.setColor(new Color(200, 220, 255));
        g2d.drawString("LIVES:", leftX + 12, leftY + 81);

        // رسم آیکون سفینه‌ها
        for (int i = 0; i < lives; i++) {
            drawMiniShip(g2d, leftX + 80 + (i * 24), leftY + 69);
        }

        // ==========================================
        // ۲. پنل سمت راست (سکه‌ها) - بدون تغییر
        // ==========================================
        int rightW = 120;
        int rightH = 45;
        int rightX = 800 - rightW - 15;
        int rightY = 15;

        g2d.setColor(new Color(35, 25, 10, 200));
        g2d.fill(new RoundRectangle2D.Float(rightX, rightY, rightW, rightH, 20, 20));

        int alphaGold = 100 + (int)(Math.abs(Math.sin(time / 250.0)) * 155);
        g2d.setColor(new Color(255, 215, 0, alphaGold));
        g2d.setStroke(new BasicStroke(2));
        g2d.draw(new RoundRectangle2D.Float(rightX, rightY, rightW, rightH, 20, 20));

        int coinBounce = (int)(Math.sin(time / 150.0) * 3);

        g2d.setColor(new Color(255, 215, 0));
        g2d.fillOval(rightX + 10, rightY + 11 + coinBounce, 22, 22);
        g2d.setColor(new Color(184, 134, 11));
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(rightX + 10, rightY + 11 + coinBounce, 22, 22);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("$", rightX + 16, rightY + 27 + coinBounce);

        g2d.setFont(new Font("Monospaced", Font.BOLD, 20));
        g2d.setColor(Color.WHITE);
        g2d.drawString(String.format("%04d", coins), rightX + 45, rightY + 29);
    }

    private static void drawMiniShip(Graphics2D g2d, int x, int y) {
        // سایه پشت سفینه برای خوانایی بهتر
        g2d.setColor(Color.BLACK);
        int[] shadowX = {x + 9, x + 17, x + 13, x + 5, x + 1};
        int[] shadowY = {y + 1, y + 13, y + 10, y + 10, y + 13};
        g2d.fillPolygon(shadowX, shadowY, 5);

        // رسم خود سفینه
        int[] xPoints = {x + 8, x + 16, x + 12, x + 4, x};
        int[] yPoints = {y, y + 12, y + 9, y + 9, y + 12};

        g2d.setColor(Color.CYAN);
        g2d.fillPolygon(xPoints, yPoints, 5);

        g2d.setColor(Color.RED);
        g2d.fillOval(x + 5, y + 10, 6, 6);

        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1));
        g2d.drawPolygon(xPoints, yPoints, 5);
    }
}