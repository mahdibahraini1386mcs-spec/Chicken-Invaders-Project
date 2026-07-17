import java.awt.Image;

public class ZigzagEnemy extends Enemy {
    private int zigzagCounter = 0;

    public ZigzagEnemy(int x, int y, Image img) {
        super(x, y, img);
    }

    @Override
    public void move() {
        super.move();
    }

    @Override
    public void moveToTarget(int tx, int ty) {
        zigzagCounter++;
        int zigzagOffset = (zigzagCounter / 10 % 2 == 0) ? 3 : -3;

        if (x < tx) x += spawnSpeed;
        if (x > tx) x -= spawnSpeed;
        x += zigzagOffset;

        if (y < ty) y += spawnSpeed;
        if (y > ty) y -= spawnSpeed;

        if (Math.abs(x - tx) < 6 && Math.abs(y - ty) < 6) isSpawning = false;
    }
}