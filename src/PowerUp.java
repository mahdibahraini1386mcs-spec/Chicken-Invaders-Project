import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;

public class PowerUp {
    private int x, y;
    private int width = 30, height = 30;
    private String type;
    private Image img;

    public PowerUp(int x, int y, String type, Image img) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.img = img;
    }

    public void move() {
        y += 2; // اصلاح شد: طبق بخش ۴.۶ صورت پروژه سرعت سقوط باید ۲ پیکسل/فریم باشد (قبلاً ۳ بود)
    }

    public void draw(Graphics g) {
        if (img != null) {
            g.drawImage(img, x, y, width, height, null);
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public String getType() {
        return type;
    }

    public int getY() {
        return y;
    }
}