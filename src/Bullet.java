import java.awt.*;

public class Bullet {
    private int x;
    private int y;
    private final int speed = 10;
    private final int width = 4;
    private final int height = 15;

    public Bullet(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void move() {
        y -= speed;
    }

    public void draw(Graphics2D g2d) {
        g2d.setColor(Color.RED);
        g2d.fillRect(x, y, width, height);
    }

    public int getY() {
        return y;
    }
}