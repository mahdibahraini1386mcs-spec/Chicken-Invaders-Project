import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;

public class BossEnemy {
    private int x, y, width, height;
    private double realX, realY;
    private double dx = 1.5;
    private double dy = 0.5;
    private double startY;
    private Image image;
    private int health;
    private int maxHealth;
    private long lastShootTime;

    public BossEnemy(int x, int y, int width, int height, Image image) {
        this.x = x;
        this.y = y;
        this.realX = x;
        this.realY = y;
        this.width = width;
        this.height = height;
        this.image = image;
        this.startY = y;
        this.lastShootTime = System.currentTimeMillis();
    }

    public void setHealth(int maxHealth) {
        this.maxHealth = maxHealth;
        this.health = maxHealth;
    }

    public int getHealth() {
        return health;
    }

    public void takeDamage(int damage) {
        health -= damage;
        if (health < 0) health = 0;
    }

    public boolean canShoot(long attackRate) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShootTime >= attackRate) {
            lastShootTime = currentTime;
            return true;
        }
        return false;
    }

    public void move() {
        realX += dx;
        if (realX <= 0 || realX >= 800 - width) {
            dx = -dx;
        }

        realY += dy;
        if (realY > startY + 20 || realY < startY - 20) {
            dy = -dy;
        }

        x = (int) realX;
        y = (int) realY;
    }

    public void draw(Graphics2D g2d) {
        if (image != null) {
            g2d.drawImage(image, x, y, width, height, null);
        }

        if (maxHealth > 0) {
            int barWidth = width;
            int barHeight = 8;
            int barX = x;
            int barY = y - 15;

            g2d.setColor(Color.RED);
            g2d.fillRect(barX, barY, barWidth, barHeight);

            g2d.setColor(Color.GREEN);
            int currentBarWidth = (int) ((double) health / maxHealth * barWidth);
            g2d.fillRect(barX, barY, currentBarWidth, barHeight);

            g2d.setColor(Color.WHITE);
            g2d.drawRect(barX, barY, barWidth, barHeight);
        }
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}