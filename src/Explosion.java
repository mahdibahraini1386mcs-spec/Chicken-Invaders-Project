import java.awt.Graphics;
import java.awt.Image;

public class Explosion {
    private int x, y;
    private int timer = 15; // افکت چند فریمی
    private Image img;

    public Explosion(int x, int y, Image img) {
        this.x = x; this.y = y; this.img = img;
    }

    public boolean update() {
        timer--;
        return timer > 0;
    }

    public void draw(Graphics g) {
        if (img != null) {
            // محو شدن تدریجی (Shrink)
            g.drawImage(img, x + (15 - timer), y + (15 - timer), timer * 3, timer * 3, null);
        }
    }
}