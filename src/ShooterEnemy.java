import java.awt.Image;

public class ShooterEnemy extends Enemy {
    private long lastShotTime = 0;
    private int shotInterval = 2000; // هر ۲ ثانیه یک‌بار احتمال شلیک مستقل بررسی می‌شود

    public ShooterEnemy(int x, int y, Image img) {
        super(x, y, img);
        this.lastShotTime = System.currentTimeMillis(); //  جلوگیری از شلیک رگباری در لحظه اسپان
    }

    @Override
    public void move() {
        super.move();
    }

    public boolean canShootBullet() {
        long now = System.currentTimeMillis();
        if (!isSpawning && now - lastShotTime >= shotInterval) {
            lastShotTime = now;
            return true;
        }
        return false;
    }
}