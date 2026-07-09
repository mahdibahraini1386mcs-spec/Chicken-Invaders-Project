import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;

public class BossBullet {
    private int x, y;
    private int speedX, speedY;
    private int width = 25;
    private int height = 30;
    private Image image;

    public BossBullet(int x, int y, int speedX, int speedY, Image image) {
        this.x = x;
        this.y = y;
        this.speedX = speedX;
        this.speedY = speedY;
        this.image = image;
    }

    public void move() {
        x += speedX;
        y += speedY;
    }

    public void draw(Graphics2D g2d) {
        if (image != null) {
            g2d.drawImage(image, x, y, width, height, null);
        } else {
            g2d.setColor(java.awt.Color.YELLOW);
            g2d.fillOval(x, y, width, height);
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public int getY() { return y; }
}