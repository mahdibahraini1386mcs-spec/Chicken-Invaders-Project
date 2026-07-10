import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;

public class BossEnemy {
    private double x, y;
    private int width, height;
    private Image img;
    private int health, maxHealth;
    private int level;

    // متغیرهای حرکتی
    private double dx;
    private double dy = 1.0; // برای حرکت عمودی غول ۸
    private double startY;
    private long startTime;
    private double accelX = 0.05; // برای شتاب غول ۸
    private long lastShotTime = 0;

    public BossEnemy(int x, int y, int width, int height, Image img, int level, int maxHealth) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.img = img;
        this.level = level;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.startY = y;
        this.startTime = System.currentTimeMillis();

        this.dx = (level == 4) ? 1.5 : 2.0;
    }

    public void move() {
        if (level == 4) {
            x += dx;
            if (x <= 0 || x >= 800 - width) {
                dx = -dx; // برخورد به لبه
            }
            long elapsed = System.currentTimeMillis() - startTime;
            y = startY + Math.sin(elapsed / 500.0) * 15.0; // دامنه 15 پیکسل

        } else if (level == 8) {
            dx += accelX;
            if (dx > 4.0) accelX = -0.02; // کاهش شتاب
            if (dx < -4.0) accelX = 0.02; // افزایش شتاب

            x += dx;
            if (x <= 0) { x = 0; dx = Math.abs(dx); accelX = 0.05; }
            if (x >= 800 - width) { x = 800 - width; dx = -Math.abs(dx); accelX = -0.05; }

            // حرکت عمودی
            y += dy;
            if (y < startY || y > startY + 100) {
                dy = -dy; // تغییر جهت عمودی
            }
        }
    }

    public boolean canShoot(int interval) {
        long now = System.currentTimeMillis();
        if (now - lastShotTime > interval) {
            lastShotTime = now;
            return true;
        }
        return false;
    }

    public void takeDamage(int dmg) {
        health -= dmg;
    }

    public int getHealth() { return health; }
    public int getX() { return (int) x; }
    public int getY() { return (int) y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Rectangle getBounds() { return new Rectangle((int)x, (int)y, width, height); }

    public void draw(Graphics g) {
        g.drawImage(img, (int)x, (int)y, width, height, null);


        int barWidth = width;
        int barHeight = 10;
        int barX = (int)x;
        int barY = (int)y - 20;


        double hpPercent = (double) health / maxHealth;
        if (hpPercent < 0) hpPercent = 0;

        int red = (int) (255 * (1 - hpPercent) * 2);
        int green = (int) (255 * hpPercent * 2);
        red = Math.min(255, Math.max(0, red));
        green = Math.min(255, Math.max(0, green));


        g.setColor(Color.DARK_GRAY);
        g.fillRect(barX, barY, barWidth, barHeight);


        g.setColor(new Color(red, green, 0));
        g.fillRect(barX, barY, (int)(barWidth * hpPercent), barHeight);


        g.setColor(Color.WHITE);
        g.drawRect(barX, barY, barWidth, barHeight);
    }
}