import java.awt.*;

public class FastEnemy extends Enemy {
    private Image image;

    public FastEnemy(int x, int y, Image image) {
        super(x, y);
        this.image = image;
    }

    @Override
    public void move() {
    }

    @Override
    public void draw(Graphics2D g2d) {
        if (image != null) {
            g2d.drawImage(image, x, y, width, height, null);
        } else {
            g2d.setColor(Color.ORANGE);
            g2d.fillOval(x, y, width, height);
        }
    }
}