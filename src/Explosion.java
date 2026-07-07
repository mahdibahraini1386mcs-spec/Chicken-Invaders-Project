import java.awt.*;

public class Explosion {
    private int x, y;
    private int timer = 15;

    public Explosion(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean update() {
        timer--;
        return timer > 0;
    }

    public void draw(Graphics2D g2d) {
        g2d.setColor(Color.ORANGE);
        g2d.fillOval(x, y, 40, 40);
        g2d.setColor(Color.RED);
        g2d.fillOval(x + 10, y + 10, 20, 20);
    }
}