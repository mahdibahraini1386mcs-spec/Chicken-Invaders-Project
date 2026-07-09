import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;

public class Egg {
    private int x, y;
    private int width = 15;
    private int height = 20;
    private int speed = 4;
    private Image image;

    public Egg(int x, int y, Image image) {
        this.x = x;
        this.y = y;
        this.image = image;
    }

    public void move() {
        y += speed;
    }

    public void draw(Graphics2D g2d) {
        if (image != null) {
            g2d.drawImage(image, x, y, width, height, null);
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public int getY() { return y; }
}