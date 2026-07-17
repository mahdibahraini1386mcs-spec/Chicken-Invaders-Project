import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import java.util.ArrayList;

public class Plane {
    private int x, y;
    private int width = 50, height = 50;
    private Image img;

    // ویژگی‌های داینامیک
    private String type;
    private int speed;
    private int fireInterval; // بر حسب میلی‌ثانیه
    private int maxLives;
    private int lives;
    private int damageMultiplier = 1;

    private int fireLevel = 1;
    private long lastShotTime = 0;

    // زمان‌سنج‌های پاورآپ
    private long rapidFireEndTime = 0;
    private long shieldEndTime = 0;

    public Plane(int x, int y, Image img, String type) {
        this.x = x;
        this.y = y;
        this.img = img;
        this.type = type;

        // اعمال تنظیمات بر اساس نوع هواپیما طبق داکیومنت پروژه
        switch (type) {
            case "Fast":
                this.speed = 7; this.fireInterval = 250; this.maxLives = 3; this.damageMultiplier = 1; break;
            case "Heavy":
                this.speed = 4; this.fireInterval = 200; this.maxLives = 5; this.damageMultiplier = 1; break;
            case "Sniper":
                this.speed = 5; this.fireInterval = 150; this.maxLives = 3; this.damageMultiplier = 2; break; // دمیج دو برابر
            default: // Default
                this.speed = 5; this.fireInterval = 300; this.maxLives = 3; this.damageMultiplier = 1; break;
        }
        this.lives = this.maxLives;
    }

    public void moveLeft() { x -= speed; if (x < 0) x = 0; }
    public void moveRight(int screenWidth) { x += speed; if (x > screenWidth - width) x = screenWidth - width; }
    public void moveUp() { y -= speed; if (y < 0) y = 0; }
    public void moveDown(int screenHeight) { y += speed; if (y > screenHeight - height) y = screenHeight - height; }

    public boolean canShoot() {
        long now = System.currentTimeMillis();
        int currentInterval = isRapidFireActive() ? (fireInterval / 2) : fireInterval;
        if (now - lastShotTime > currentInterval) {
            lastShotTime = now;
            return true;
        }
        return false;
    }

    public void shoot(ArrayList<Bullet> bullets) {
        int bx = x + width / 2 - 5;
        int by = y;
        bullets.add(new Bullet(bx, by));
        if (fireLevel >= 2) { bullets.add(new Bullet(bx - 15, by + 10)); bullets.add(new Bullet(bx + 15, by + 10)); }
        if (fireLevel >= 3) { bullets.add(new Bullet(bx - 25, by + 20)); bullets.add(new Bullet(bx + 25, by + 20)); }
        if (fireLevel >= 4) { bullets.add(new Bullet(bx - 35, by + 30)); bullets.add(new Bullet(bx + 35, by + 30)); }
    }

    public void takeDamage() { if (!isShieldActive()) lives--; }
    public void addLife() { if (lives < maxLives) lives++; }
    public void addFire() { if (fireLevel < 4) fireLevel++; }
    public void activateRapidFire() { rapidFireEndTime = System.currentTimeMillis() + 8000; }
    public void activateShield() { shieldEndTime = System.currentTimeMillis() + 10000; }

    public void updatePowerUps() {}

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getLives() { return lives; }
    public int getFireLevel() { return fireLevel; }
    public int getDamageMultiplier() { return damageMultiplier; } // برای غول‌ها
    public Rectangle getBounds() { return new Rectangle(x, y, width, height); }

    public boolean isShieldActive() { return System.currentTimeMillis() < shieldEndTime; }
    public boolean isRapidFireActive() { return System.currentTimeMillis() < rapidFireEndTime; }

    public String getActivePowerupsText() {
        String s = "";
        if (isRapidFireActive()) s += "[RAPID] ";
        if (isShieldActive()) s += "[SHIELD] ";
        return s;
    }

    public void draw(Graphics g) {
        g.drawImage(img, x, y, width, height, null);
        if (isShieldActive()) {
            g.setColor(new java.awt.Color(0, 255, 255, 100));
            g.fillOval(x - 10, y - 10, width + 20, height + 20);
            g.setColor(java.awt.Color.CYAN);
            g.drawOval(x - 10, y - 10, width + 20, height + 20);
        }
    }
}