import java.awt.*;
import java.util.ArrayList;

public class Plane {
    private int x, y;
    private int width = 50, height = 50;
    private int speed = 5;
    private int lives = 3;
    private int maxLives = 5;
    private long lastShotTime = 0;
    private int fireRate = 300;

    // پاورآپ‌ها
    private boolean shieldActive = false;
    private long shieldEndTime = 0;
    private boolean rapidFireActive = false;
    private long rapidFireEndTime = 0;
    private int fireLevel = 1; // Add Fire (تعداد تیرها)
    private Image img;

    public Plane(int x, int y, Image img, String type) { this.x = x; this.y = y; this.img = img; }

    public void updatePowerUps() {
        long now = System.currentTimeMillis();
        if (shieldActive && now > shieldEndTime) shieldActive = false;
        if (rapidFireActive && now > rapidFireEndTime) rapidFireActive = false;
    }

    public void moveLeft() { if (x > 0) x -= speed; }
    public void moveRight(int limit) { if (x < limit - width) x += speed; }
    public void moveUp() { if (y > 0) y -= speed; }
    public void moveDown(int limit) { if (y < limit - height) y += speed; }

    public boolean canShoot() {
        long now = System.currentTimeMillis();
        int currentFireRate = rapidFireActive ? 150 : fireRate; // ترکیب Rapid Fire
        if (now - lastShotTime >= currentFireRate) {
            lastShotTime = now;
            return true;
        }
        return false;
    }

    public void shoot(ArrayList<Bullet> bullets) {
        if (fireLevel == 1) {
            bullets.add(new Bullet(x + width/2 - 5, y));
        } else if (fireLevel == 2) {
            bullets.add(new Bullet(x + 10, y));
            bullets.add(new Bullet(x + width - 15, y));
        } else {
            bullets.add(new Bullet(x, y));
            bullets.add(new Bullet(x + width/2 - 5, y));
            bullets.add(new Bullet(x + width - 10, y));
        }
    }

    public void takeDamage() {
        if (!shieldActive) lives--;
        else shieldActive = false; // سپر با یک ضربه می‌شکند و از بین می‌رود
    }

    public void addFire() { if (fireLevel < 3) fireLevel++; }
    public void addLife() { if (lives < maxLives) lives++; }
    public void activateShield() { shieldActive = true; shieldEndTime = System.currentTimeMillis() + 10000; }
    public void activateRapidFire() { rapidFireActive = true; rapidFireEndTime = System.currentTimeMillis() + 8000; }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getLives() { return lives; }
    public int getDamageMultiplier() { return 1; }
    public int getFireLevel() { return fireLevel; }

    public String getActivePowerupsText() {
        String text = "";
        if (shieldActive) text += "SHIELD ";
        if (rapidFireActive) text += "RAPID ";
        return text;
    }

    public Rectangle getBounds() { return new Rectangle(x, y, width, height); }

    public void draw(Graphics g) {
        g.drawImage(img, x, y, width, height, null);
        if (shieldActive) {
            g.setColor(new Color(0, 255, 255, 100)); // هاله سپر
            g.fillOval(x - 10, y - 10, width + 20, height + 20);
            g.setColor(Color.CYAN);
            g.drawOval(x - 10, y - 10, width + 20, height + 20);
        }
    }
}