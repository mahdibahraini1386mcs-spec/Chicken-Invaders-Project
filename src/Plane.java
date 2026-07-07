import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.util.ArrayList;

public class Plane {
    private int x, y;
    private int width = 50;
    private int height = 50;
    private Image image;
    private int speed = 5;
    private int lives = 3;
    private int fireLevel = 1;
    private int damageMultiplier = 1;
    private int planeType;
    private long lastShootTime = 0;
    private int shootDelay = 200;

    public Plane(int x, int y, Image image, int planeType) {
        this.x = x;
        this.y = y;
        this.image = image;
        this.planeType = planeType;

        if (planeType == 2) {
            this.damageMultiplier = 2;
            this.speed = 7;
        }
    }

    public void moveLeft() {
        x -= speed;
        if (x < 0) x = 0;
    }

    public void moveRight(int screenWidth) {
        x += speed;
        if (x > screenWidth - width) x = screenWidth - width;
    }

    public void moveUp() {
        y -= speed;
        if (y < 0) y = 0;
    }

    public void moveDown(int screenHeight) {
        y += speed;
        if (y > screenHeight - height) y = screenHeight - height;
    }

    public boolean canShoot() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShootTime >= shootDelay) {
            lastShootTime = currentTime;
            return true;
        }
        return false;
    }

    public void shoot(ArrayList<Bullet> bullets) {
        if (fireLevel == 1) {
            bullets.add(new Bullet(x + width / 2 - 2, y));
        } else if (fireLevel == 2) {
            bullets.add(new Bullet(x + 10, y));
            bullets.add(new Bullet(x + width - 10, y));
        } else {
            bullets.add(new Bullet(x, y));
            bullets.add(new Bullet(x + width / 2 - 2, y));
            bullets.add(new Bullet(x + width, y));
        }
    }

    public void draw(Graphics2D g2d) {
        if (image != null) {
            g2d.drawImage(image, x, y, width, height, null);
        }
    }

    public void addFire() {
        if (fireLevel < 3) {
            fireLevel++;
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getLives() { return lives; }
    public int getDamageMultiplier() { return damageMultiplier; }
    public void setLives(int lives) { this.lives = lives; }
    public void takeDamage() { this.lives--; }
}