import java.awt.*;
import java.util.ArrayList;

public class Plane {
    private int x, y, width = 50, height = 50;
    private int speed = 5;
    private int lives = 3;
    private int fireCount = 1;
    private long lastShotTime = 0;
    private long fireRate = 300;
    private Image image;

    public Plane(int x, int y, Image image, int type) {
        this.x = x;
        this.y = y;
        this.image = image;
        if (type == 1) { speed = 7; fireRate = 250; }
        else if (type == 2) { speed = 4; fireRate = 200; lives = 5; }
        else if (type == 3) { speed = 5; fireRate = 150; }
    }

    public void moveLeft() { if (x > 0) x -= speed; }
    public void moveRight(int panelWidth) { if (x < panelWidth - width) x += speed; }

    public boolean canShoot() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShotTime >= fireRate) {
            lastShotTime = currentTime;
            return true;
        }
        return false;
    }

    public void shoot(ArrayList<Bullet> bullets) {
        int startX = x + width / 2;
        for (int i = 0; i < fireCount; i++) {
            int offset = (i - (fireCount - 1) / 2) * 20;
            bullets.add(new Bullet(startX + offset - 2, y));
        }
    }

    public void addFire() {
        if (fireCount < 5) fireCount++;
    }

    public void loseLife() { lives--; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getLives() { return lives; }
    public int getFireCount() { return fireCount; }
    public int getDamageMultiplier() { return (ScoreManager.selectedPlane == 3) ? 2 : 1; }

    public void draw(Graphics2D g2d) {
        if (image != null) g2d.drawImage(image, x, y, width, height, null);
    }
}