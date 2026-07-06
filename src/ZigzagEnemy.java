import java.awt.*;

public class ZigzagEnemy extends Enemy {
    private Image image;
    private double angle = 0; // برای کنترل نوسان

    public ZigzagEnemy(int x, int y, Image image) {
        super(x, y);
        this.image = image;
    }

    @Override
    public void move() {
        x += 1;
        angle += 0.1;
        y += (int) (Math.sin(angle) * 3);
    }

    @Override
    public void draw(Graphics2D g2d) {
        if (image != null) {
            g2d.drawImage(image, x, y, width, height, null);
        } else {
            g2d.setColor(Color.MAGENTA);
            g2d.fillOval(x, y, width, height);
        }
    }
}