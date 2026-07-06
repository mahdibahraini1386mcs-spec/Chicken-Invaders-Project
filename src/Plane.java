import java.awt.*;

public class Plane {
    private int x;
    private int y;
    private int speed = 5;
    private int lives = 3;
    private int width = 50;
    private int height = 30;
    private long lastShotTime = 0;
    private int firePower = 1;

    public Plane(int startX, int startY) {
        this.x = startX;
        this.y = startY;
    }

    public void moveLeft() {
        x -= speed;
        if (x < 0) {
            x = 0;
        }
    }

    public void moveRight(int panelWidth) {
        x += speed;
        if (x > panelWidth - width) {
            x = panelWidth - width;
        }
    }

    public boolean canShoot() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShotTime >= 300) {
            lastShotTime = currentTime;
            return true;
        }
        return false;
    }

    public void draw(Graphics2D g2d) {
        g2d.setColor(Color.GREEN);
        int[] xPoints = {x, x + width / 2, x + width};
        int[] yPoints = {y + height, y, y + height};
        g2d.fillPolygon(xPoints, yPoints, 3);
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getLives() { return lives; }
    public void loseLife() { lives--; }
    public int getFirePower() { return firePower; }
    public void addFirePower() { firePower++; }
}