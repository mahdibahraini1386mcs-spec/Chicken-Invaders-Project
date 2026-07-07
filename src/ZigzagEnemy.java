import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;

public class ZigzagEnemy extends Enemy {

    public ZigzagEnemy(int x, int y, Image image) {
        super(x, y, image);
    }

    @Override
    public void move() {
        // منطق حرکت زیگزاگی
        x += (Math.sin(y * 0.1) * 2); // حرکت به چپ و راست بر اساس Y
        y += 1; // حرکت تدریجی به پایین
    }

    @Override
    public void draw(Graphics2D g2d) {
        if (image != null) {
            g2d.drawImage(image, x, y, 40, 40, null);
        }
    }

    @Override
    public Rectangle getBounds() {
        // بازگرداندن محدوده برخورد برای تشخیص تصادف تیر با دشمن
        return new Rectangle(x, y, 40, 40);
    }
}