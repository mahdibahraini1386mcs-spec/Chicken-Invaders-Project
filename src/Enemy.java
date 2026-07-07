import java.awt.*;

public abstract class Enemy {
    protected int x, y;
    protected Image image; // اضافه کردن متغیر تصویر

    // اصلاح سازنده برای پذیرش ۳ آرگومان
    public Enemy(int x, int y, Image image) {
        this.x = x;
        this.y = y;
        this.image = image;
    }

    public abstract void move();
    public abstract void draw(Graphics2D g2d);
    public abstract Rectangle getBounds();

    // گترها برای دسترسی از کلاس‌های دیگر
    public int getX() { return x; }
    public int getY() { return y; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
}