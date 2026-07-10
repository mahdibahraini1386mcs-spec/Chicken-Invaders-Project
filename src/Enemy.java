import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;

public class Enemy {
    protected int x, y;
    protected int width = 50, height = 50;
    protected Image img;
    private boolean spawning = false;

    public Enemy(int x, int y, Image img) {
        this.x = x;
        this.y = y;
        this.img = img;
    }

    public void setSpawning(boolean sp) {
        this.spawning = sp;
    }

    public boolean isSpawning() {
        return spawning;
    }

    public void moveToTarget(int tx, int ty) {
        double speed = (this.getClass().getSimpleName().equals("FastEnemy")) ? 8.0 : 4.0;
        double dx = tx - x;
        double dy = ty - y;
        double dist = Math.sqrt(dx * dx + dy * dy);

        if (dist <= speed) {
            x = tx;
            y = ty;
            spawning = false;
        } else {
            x += (int) ((dx / dist) * speed);
            y += (int) ((dy / dist) * speed);

            if (this.getClass().getSimpleName().equals("ZigzagEnemy")) {
                x += (int) (Math.sin(y / 15.0) * 15);
            }
        }
    }

    public void move() {
    }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Rectangle getBounds() { return new Rectangle(x, y, width, height); }
    public void draw(Graphics g) { g.drawImage(img, x, y, width, height, null); }
}