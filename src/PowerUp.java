import java.awt.*;

public class PowerUp {
    private int x, y;
    private String type;
    private int speed = 2; // خواسته استاد: ۲ پیکسل/فریم

    public PowerUp(int x, int y, String type) {
        this.x = x; this.y = y; this.type = type;
    }

    public void move() { y += speed; }
    public String getType() { return type; }
    public Rectangle getBounds() { return new Rectangle(x, y, 30, 30); }
    public int getY() { return y; }

    public void draw(Graphics g) {
        if (type.equals("AddFire")) g.setColor(Color.RED);
        else if (type.equals("RapidFire")) g.setColor(Color.ORANGE);
        else if (type.equals("ExtraLife")) g.setColor(Color.GREEN);
        else if (type.equals("Shield")) g.setColor(Color.CYAN);
        else if (type.equals("FreezeBomb")) g.setColor(Color.BLUE);

        g.fillOval(x, y, 30, 30);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 10));
        g.drawString(type.substring(0, 3).toUpperCase(), x + 5, y + 20);
    }
}