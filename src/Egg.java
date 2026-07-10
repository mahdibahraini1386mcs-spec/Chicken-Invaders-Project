import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;

public class Egg {
    private int x, y, dx, dy;
    private int width = 15, height = 20;
    private Image img;

    public Egg(int x, int y, int dx, int dy, Image img) {
        this.x = x;
        this.y = y;
        this.dx = dx;
        this.dy = dy;
        this.img = img;
    }

    public void move() {
        x += dx;
        y += dy;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public Rectangle getBounds() { return new Rectangle(x, y, width, height); }
    public void draw(Graphics g) { g.drawImage(img, x, y, width, height, null); }
}