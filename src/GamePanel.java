import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.List;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private Timer timer;
    private int spaceshipX = 375;
    private final int spaceshipY = 500;
    private final int spaceshipWidth = 50;
    private final int spaceshipHeight = 30;
    private int spaceshipSpeed = 0;
    private List<Bullet> bullets;
    private List<Chicken> chickens;

    public GamePanel() {
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        bullets = new ArrayList<>();
        chickens = new ArrayList<>();
        initChickens();
        timer = new Timer(16, this);
    }

    private void initChickens() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 8; col++) {
                chickens.add(new Chicken(100 + (col * 80), 50 + (row * 60)));
            }
        }
    }

    public void startGame() {
        timer.start();
        this.requestFocusInWindow();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(Color.GREEN);
        int[] xPoints = {spaceshipX, spaceshipX + spaceshipWidth / 2, spaceshipX + spaceshipWidth};
        int[] yPoints = {spaceshipY + spaceshipHeight, spaceshipY, spaceshipY + spaceshipHeight};
        g2d.fillPolygon(xPoints, yPoints, 3);

        for (Bullet bullet : bullets) {
            bullet.draw(g2d);
        }

        for (Chicken chicken : chickens) {
            if (chicken.isAlive()) {
                chicken.draw(g2d);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        spaceshipX += spaceshipSpeed;

        if (spaceshipX < 0) {
            spaceshipX = 0;
        }
        if (spaceshipX > 800 - spaceshipWidth) {
            spaceshipX = 800 - spaceshipWidth;
        }

        for (int i = 0; i < bullets.size(); i++) {
            Bullet b = bullets.get(i);
            b.move();
            if (b.getY() < 0) {
                bullets.remove(i);
                i--;
            }
        }

        checkCollisions();

        repaint();
    }

    private void checkCollisions() {
        for (int i = 0; i < bullets.size(); i++) {
            Bullet b = bullets.get(i);
            Rectangle bulletBounds = b.getBounds();

            for (Chicken c : chickens) {
                if (c.isAlive() && bulletBounds.intersects(c.getBounds())) {
                    c.setAlive(false);
                    bullets.remove(i);
                    i--;
                    break;
                }
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_LEFT) {
            spaceshipSpeed = -7;
        }
        if (key == KeyEvent.VK_RIGHT) {
            spaceshipSpeed = 7;
        }
        if (key == KeyEvent.VK_SPACE) {
            bullets.add(new Bullet(spaceshipX + spaceshipWidth / 2 - 2, spaceshipY));
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT) {
            spaceshipSpeed = 0;
        }
    }
}