import java.awt.*;

public class BossEnemy extends Enemy {
    private Image image;
    private int health;
    private int maxHealth;
    private long lastShootTime;
    private int moveDirection = 1;
    private int speed = 2;

    public BossEnemy(int x, int y, Image image, int maxHealth) {
        super(x, y);
        this.image = image;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.width = 150;
        this.height = 150;
        this.lastShootTime = System.currentTimeMillis();
    }

    @Override
    public void move() {
        x += speed * moveDirection;
        if (x <= 0 || x >= 800 - width) {
            moveDirection *= -1;
        }
    }

    @Override
    public void draw(Graphics2D g2d) {
        if (image != null) {
            g2d.drawImage(image, x, y, width, height, null);
        } else {
            g2d.setColor(Color.RED);
            g2d.fillRect(x, y, width, height);
        }

        g2d.setColor(Color.RED);
        g2d.fillRect(x, y - 15, width, 8);
        g2d.setColor(Color.GREEN);
        int hpWidth = (int) (((double) health / maxHealth) * width);
        g2d.fillRect(x, y - 15, hpWidth, 8);
    }

    public boolean canShoot() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShootTime >= 1500) {
            lastShootTime = currentTime;
            return true;
        }
        return false;
    }

    public void takeDamage(int damage) {
        health -= damage;
    }

    public int getHealth() {
        return health;
    }
}