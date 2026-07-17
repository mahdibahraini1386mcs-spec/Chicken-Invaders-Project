import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;

public class Enemy {
    protected int x, y;
    protected int width = 40, height = 40;
    protected Image img;
    protected boolean isSpawning = false;
    protected int spawnSpeed = 2; // سرعت پایه هنگام جایگزینی/اسپان - FastEnemy این را دو برابر می‌کند

    public Enemy(int x, int y, Image img) {
        this.x = x;
        this.y = y;
        this.img = img;
    }

    public void move() {
    }

    public void draw(Graphics g) {
        g.drawImage(img, x, y, width, height, null);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public boolean isSpawning() { return isSpawning; }
    public void setSpawning(boolean b) { this.isSpawning = b; }

    public void moveToTarget(int tx, int ty) {
        if (x < tx) x += spawnSpeed;
        if (x > tx) x -= spawnSpeed;
        if (y < ty) y += spawnSpeed;
        if (y > ty) y -= spawnSpeed;
        if (Math.abs(x - tx) < spawnSpeed + 3 && Math.abs(y - ty) < spawnSpeed + 3) isSpawning = false;
    }
}