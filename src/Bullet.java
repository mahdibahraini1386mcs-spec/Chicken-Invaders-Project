import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class Bullet {
    private int x, y;
    private int speed = 10;
    private int width = 4;
    private int height = 15;

    public Bullet(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void move() {
        y -= speed;
    }

    public void draw(Graphics2D g2d) {
        g2d.setColor(Color.YELLOW);
        g2d.fillRect(x, y, width, height);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public int getY() { return y; }
}