import java.awt.*;

public class PowerUp {
    private int x, y;
    private String type; // "AddFire", "Rapid", "Life", "Shield", "Freeze"
    private int width = 30, height = 30;
    private int speed = 2;

    public PowerUp(int x, int y, String type) {
        this.x = x;
        this.y = y;
        this.type = type;
    }

    public void move() { y += speed; }

    public void draw(Graphics2D g2d) {
        g2d.setColor(Color.YELLOW);
        g2d.fillRect(x, y, width, height);
        g2d.setColor(Color.BLACK);
        g2d.drawString(type.substring(0, 1), x + 10, y + 20);
    }

    public Rectangle getBounds() { return new Rectangle(x, y, width, height); }
    public String getType() { return type; }
    public int getY() { return y; }
}