import java.awt.*;

public class Plane {
    private int x, y;
    private int width = 50, height = 50;
    private Image image;

    private int speed;
    private int maxLives;
    private int lives;
    private int fireRate;
    private int damageMultiplier;
    private long lastShotTime = 0;

    public Plane(int startX, int startY, Image image, int type) {
        this.x = startX;
        this.y = startY;
        this.image = image;

        switch(type) {
            case 1: speed = 7; fireRate = 250; maxLives = 3; damageMultiplier = 1; break; // Fast
            case 2: speed = 4; fireRate = 200; maxLives = 5; damageMultiplier = 1; break; // Heavy
            case 3: speed = 5; fireRate = 150; maxLives = 3; damageMultiplier = 2; break; // Sniper (2x Damage to Boss)
            default: speed = 5; fireRate = 300; maxLives = 3; damageMultiplier = 1; break; // Default
        }
        this.lives = maxLives;
    }

    public void moveLeft() {
        x -= speed;
        if (x < 0) x = 0;
    }

    public void moveRight(int panelWidth) {
        x += speed;
        if (x > panelWidth - width) x = panelWidth - width;
    }

    public boolean canShoot() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShotTime >= fireRate) {
            lastShotTime = currentTime;
            return true;
        }
        return false;
    }

    public void draw(Graphics2D g2d) {
        if (image != null) {
            g2d.drawImage(image, x, y, width, height, null);
        } else {
            g2d.setColor(Color.GREEN);
            int[] xPoints = {x, x + width / 2, x + width};
            int[] yPoints = {y + height, y, y + height};
            g2d.fillPolygon(xPoints, yPoints, 3);
        }
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getLives() { return lives; }
    public void loseLife() { lives--; }
    public int getDamageMultiplier() { return damageMultiplier; }
}