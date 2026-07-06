import java.awt.*;

public class Chicken {
    private int x;
    private int y;
    private final int width = 40;
    private final int height = 40;
    private boolean isAlive = true;
    private int speed = 1;

    public Chicken(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void move() {
        y += speed;
    }

    public void draw(Graphics2D g2d) {
        g2d.setColor(Color.YELLOW);
        g2d.fillOval(x, y, width, height);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void setAlive(boolean alive) {
        this.isAlive = alive;
    }

    public int getY() {
        return y;
    }
}