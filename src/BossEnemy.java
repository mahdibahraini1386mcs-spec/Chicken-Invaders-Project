import java.awt.Graphics2D;
import java.awt.Image;

public class BossEnemy {
    private int x;
    private int y;
    private int width;
    private int height;
    private int speedX;
    private int speedY;
    private int health;
    private Image image;

    private long lastShotTime;
    private long shotCooldown = 1500;

    public BossEnemy(int x, int y, int width, int height, Image image) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.image = image;
        this.speedX = 2;
        this.speedY = 0;
        this.health = 50;
        this.lastShotTime = System.currentTimeMillis();
    }

    public void draw(Graphics2D g) {
        if (image != null) {
            g.drawImage(image, x, y, width, height, null);
        }
    }

    public void move() {
        x += speedX;
        y += speedY;

        if (x <= 0 || x + width >= 800) {
            speedX = -speedX;
        }
    }

    public boolean canShoot() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShotTime >= shotCooldown) {
            lastShotTime = currentTime;
            return true;
        }
        return false;
    }

    public void takeDamage(int amount) {
        health -= amount;
        if (health < 0) {
            health = 0;
        }
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public Image getImage() {
        return image;
    }
}