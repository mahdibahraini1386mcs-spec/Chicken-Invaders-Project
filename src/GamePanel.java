import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private Timer timer;
    private int spaceshipX = 375;
    private final int spaceshipY = 500;
    private final int spaceshipWidth = 50;
    private final int spaceshipHeight = 30;
    private int spaceshipSpeed = 0;

    public GamePanel() {
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
        timer = new Timer(16, this);
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

        repaint();
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
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_RIGHT) {
            spaceshipSpeed = 0;
        }
    }
}