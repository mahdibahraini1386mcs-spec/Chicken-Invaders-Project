import java.awt.*;
import java.util.ArrayList;

public class Plane {
    private int x, y;
    private int width = 50, height = 50;
    private int speed = 5; // خواسته استاد: ۵ پیکسل/فریم
    private int lives = 3; // جان پایه
    private int maxLives = 5; // حداکثر جان
    private long lastShotTime = 0;
    private int fireRate = 300; // خواسته استاد: ۳۰۰ میلی‌ثانیه
    private boolean shieldActive = false;
    private Image img;

    public Plane(int x, int y, Image img, String type) {
        this.x = x; this.y = y; this.img = img;
    }

    public void moveLeft() { if (x > 0) x -= speed; }
    public void moveRight(int limit) { if (x < limit - width) x += speed; }
    public void moveUp() { if (y > 0) y -= speed; }
    public void moveDown(int limit) { if (y < limit - height) y += speed; }

    public boolean canShoot() {
        long now = System.currentTimeMillis();
        if (now - lastShotTime >= fireRate) {
            lastShotTime = now;
            return true;
        }
        return false;
    }

    public void shoot(ArrayList<Bullet> bullets) {
        bullets.add(new Bullet(x + width/2 - 5, y));
    }

    public void takeDamage() {
        if (!shieldActive) {
            lives--;
        } else {
            shieldActive = false;
        }
    }

    public void addLife() { if (lives < maxLives) lives++; }
    public void activateShield() { this.shieldActive = true; }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getLives() { return lives; }
    public int getDamageMultiplier() { return 1; }
    public void draw(Graphics g) { g.drawImage(img, x, y, width, height, null); }
    public Rectangle getBounds() { return new Rectangle(x, y, width, height); }
}