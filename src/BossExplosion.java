import java.awt.Graphics2D;
import java.awt.Image;

public class BossExplosion {
    private int x, y, width, height;
    private Image img1, img2;
    private int life = 100;
    private int frameCount = 0;

    public BossExplosion(int x, int y, int width, int height, Image img1, Image img2) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.img1 = img1;
        this.img2 = img2;
    }

    public boolean update() {
        life--;
        frameCount++;
        return life > 0;
    }

    public void draw(Graphics2D g2d) {
        if (frameCount % 20 < 10) {
            if (img1 != null) g2d.drawImage(img1, x, y, width, height, null);
        } else {
            if (img2 != null) g2d.drawImage(img2, x, y, width, height, null);
        }
    }
}