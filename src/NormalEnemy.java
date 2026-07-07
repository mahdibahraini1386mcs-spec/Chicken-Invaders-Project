import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;

public class NormalEnemy extends Enemy {

    public NormalEnemy(int x, int y, Image image) {
        super(x, y, image);
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