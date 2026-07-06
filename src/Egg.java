import java.awt.*;

public class Egg {
    private int x;
    private int y;
    private int speed = 4;
    private int width = 15;
    private int height = 20;
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
        } else {
            g2d.setColor(Color.WHITE);
            g2d.fillOval(x, y, width, height);
        }
    }

    public int getY() {
        return y;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
}