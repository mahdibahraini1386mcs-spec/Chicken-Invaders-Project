import java.awt.*;

public abstract class Enemy {
    protected int x;
    protected int y;
    protected int width = 40;
    protected int height = 40;
    protected boolean isAlive = true;

    public Enemy(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public abstract void move();

    public abstract void draw(Graphics2D g2d);

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void setAlive(boolean alive) {
        this.isAlive = alive;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }
}