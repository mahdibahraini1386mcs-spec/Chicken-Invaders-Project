import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private Timer timer;
    private Plane plane;
    private ArrayList<Enemy> enemies;
    private ArrayList<Bullet> bullets;
    private ArrayList<Egg> eggs;
    private Cell[][] grid;
    private HashMap<Enemy, Cell> enemyCellMap;

    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean spacePressed = false;

    private int gridDirection = 1;
    private final int GRID_STEP_Y = 20;
    private long lastEggTime = 0;
    private Random random = new Random();

    private int currentLevel = 1;
    private int score = 0;

    private Image planeImage;
    private Image normalEnemyImage;
    private Image fastEnemyImage;
    private Image zigzagEnemyImage;

    public GamePanel() {
        setFocusable(true);
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(800, 600));
        addKeyListener(this);

        loadImages();

        plane = new Plane(375, 500, planeImage);
        enemies = new ArrayList<>();
        bullets = new ArrayList<>();
        eggs = new ArrayList<>();
        enemyCellMap = new HashMap<>();
        grid = new Cell[5][8];

        initLevel1();

        timer = new Timer(16, this);
        timer.start();
    }

    private void loadImages() {
        planeImage = ResourceManager.loadImage("airplan", "plane.png");
        normalEnemyImage = ResourceManager.loadImage("chicken", "chicken.png");
        fastEnemyImage = ResourceManager.loadImage("chicken", "fast_chicken.png");
        zigzagEnemyImage = ResourceManager.loadImage("chicken", "zigzag_chicken.png");
    }

    private void initLevel1() {
        int startX = 80;
        int startY = 50;
        int hGap = 70;
        int vGap = 50;

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 8; col++) {
                int x = startX + col * hGap;
                int y = startY + row * vGap;
                Cell cell = new Cell(row, col, x, y, 2, "Normal");
                grid[row][col] = cell;

                NormalEnemy enemy = new NormalEnemy(x, y, normalEnemyImage);
                enemies.add(enemy);
                enemyCellMap.put(enemy, cell);
            }
        }
    }

    private void initLevel2() {
        int startX = 80;
        int startY = 50;
        int hGap = 70;
        int vGap = 50;

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 8; col++) {
                int x = startX + col * hGap;
                int y = startY + row * vGap;

                if (row == 0) {
                    Cell cell = new Cell(row, col, x, y, 1, "Fast");
                    grid[row][col] = cell;
                    FastEnemy enemy = new FastEnemy(x, y, fastEnemyImage);
                    enemies.add(enemy);
                    enemyCellMap.put(enemy, cell);
                } else {
                    Cell cell = new Cell(row, col, x, y, 2, "Normal");
                    grid[row][col] = cell;
                    NormalEnemy enemy = new NormalEnemy(x, y, normalEnemyImage);
                    enemies.add(enemy);
                    enemyCellMap.put(enemy, cell);
                }
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        plane.draw(g2d);

        for (Enemy enemy : enemies) {
            enemy.draw(g2d);
        }
        for (Bullet bullet : bullets) {
            bullet.draw(g2d);
        }
        for (Egg egg : eggs) {
            egg.draw(g2d);
        }

        GameHUD.draw(g2d, score, plane.getLives(), currentLevel);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        updatePlane();
        updateBullets();
        updateEnemies();
        updateEggs();
        checkCollisions();
        checkLevelUp();
        repaint();
    }

    private void updatePlane() {
        if (leftPressed) plane.moveLeft();
        if (rightPressed) plane.moveRight(getWidth());
        if (spacePressed && plane.canShoot()) {
            bullets.add(new Bullet(plane.getX() + plane.getWidth() / 2 - 2, plane.getY()));
            SoundManager.playSound("mixkit-short-laser-gun-shot-1670.wav");
        }
    }

    private void updateBullets() {
        Iterator<Bullet> iter = bullets.iterator();
        while (iter.hasNext()) {
            Bullet b = iter.next();
            b.move();
            if (b.getY() < 0) iter.remove();
        }
    }

    private void updateEnemies() {
        if (enemies.isEmpty()) return;

        // فراخوانی متد move برای هر دشمن (منطق پلی‌مورفیک)
        for (Enemy enemy : enemies) {
            enemy.move();
        }

        // منطق برخورد به لبه‌ها (فقط برای دشمنانی که حرکت افقی شبکه دارند)
        boolean hitEdge = false;
        for (Enemy enemy : enemies) {
            if (!(enemy instanceof ZigzagEnemy)) {
                if (enemy.getX() >= getWidth() - 40 || enemy.getX() <= 0) {
                    hitEdge = true;
                    break;
                }
            }
        }

        if (hitEdge) {
            gridDirection *= -1;
            for (Enemy enemy : enemies) {
                enemy.setY(enemy.getY() + GRID_STEP_Y);
            }
        }
    }

    private void updateEggs() {
        Iterator<Egg> iter = eggs.iterator();
        while (iter.hasNext()) {
            Egg egg = iter.next();
            egg.move();
            if (egg.getY() > getHeight()) iter.remove();
        }
    }

    private void checkCollisions() {
        Iterator<Bullet> bulletIter = bullets.iterator();
        ArrayList<Enemy> newSpawns = new ArrayList<>();

        while (bulletIter.hasNext()) {
            Bullet b = bulletIter.next();
            Rectangle bBounds = b.getBounds();

            Iterator<Enemy> enemyIter = enemies.iterator();
            while (enemyIter.hasNext()) {
                Enemy e = enemyIter.next();
                if (bBounds.intersects(e.getBounds())) {
                    Cell cell = enemyCellMap.get(e);
                    cell.decreaseCounter();

                    enemyIter.remove();
                    enemyCellMap.remove(e);
                    bulletIter.remove();
                    score += 10;

                    SoundManager.playSound("mixkit-epic-impact-afar-explosion-2782.wav");

                    if (cell.getCounter() > 0) {
                        String type = cell.getEnemyType();
                        Enemy newEnemy = null;

                        if (type.equals("Normal")) newEnemy = new NormalEnemy(cell.getX(), cell.getY(), normalEnemyImage);
                        else if (type.equals("Fast")) newEnemy = new FastEnemy(cell.getX(), cell.getY(), fastEnemyImage);
                        else if (type.equals("Zigzag")) newEnemy = new ZigzagEnemy(cell.getX(), cell.getY(), zigzagEnemyImage);

                        if (newEnemy != null) {
                            newSpawns.add(newEnemy);
                            enemyCellMap.put(newEnemy, cell);
                        }
                    }
                    break;
                }
            }
        }

        enemies.addAll(newSpawns);

        Rectangle pBounds = new Rectangle(plane.getX(), plane.getY(), plane.getWidth(), plane.getHeight());
        Iterator<Egg> eggIter = eggs.iterator();
        while (eggIter.hasNext()) {
            Egg egg = eggIter.next();
            if (egg.getBounds().intersects(pBounds)) {
                plane.loseLife();
                eggIter.remove();
            }
        }
    }

    private void checkLevelUp() {
        if (enemies.isEmpty()) {
            currentLevel++;
            if (currentLevel == 2) initLevel2();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) leftPressed = true;
        if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) rightPressed = true;
        if (key == KeyEvent.VK_SPACE) spacePressed = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) leftPressed = false;
        if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) rightPressed = false;
        if (key == KeyEvent.VK_SPACE) spacePressed = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}