import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;

public class ShooterEnemy extends Enemy {

    public ShooterEnemy(int x, int y, Image image) {
        super(x, y, image);
    }

    @Override
    public void move() {
        // منطق حرکت تیرانداز (مثلاً حرکت افقی یا ثابت)
        // اگر می‌خواهی مثل دشمنان عادی حرکت کند:
        // x += speedX;
    }

    @Override
    public void draw(Graphics2D g2d) {
        if (image != null) {
            g2d.drawImage(image, x, y, 40, 40, null);
        }
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle(x, y, 40, 40);
    }
}