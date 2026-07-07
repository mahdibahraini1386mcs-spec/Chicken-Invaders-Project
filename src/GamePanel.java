import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private enum GameState { MENU, STORE, PLAYING, GAMEOVER, WIN }
    private GameState gameState = GameState.MENU;

    private Timer timer;
    private Plane plane;
    private ArrayList<Enemy> enemies;
    private ArrayList<Bullet> bullets;
    private ArrayList<Egg> eggs;
    private ArrayList<PowerUp> powerUps;
    private ArrayList<Explosion> explosions;
    private Cell[][] grid;
    private HashMap<Enemy, Cell> enemyCellMap;
    private BossEnemy boss;
    private ArrayList<BossBullet> bossBullets;

    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean spacePressed = false;

    private int gridDirection = 1;
    private double gridSpeedX = 1.2;
    private int gridStepY = 20;
    private long lastEggTime = 0;
    private int eggInterval = 3000;
    private Random random = new Random();

    private int currentLevel = 1;
    private int score = 0;

    private Image planeImage, normalEnemyImage, fastEnemyImage, zigzagEnemyImage, shooterEnemyImage, bossImage, boss2Image;

    public GamePanel() {
        setFocusable(true);
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(800, 600));
        addKeyListener(this);

        explosions = new ArrayList<>();
        loadImages();
        ScoreManager.load();
        timer = new Timer(16, this);
        timer.start();
    }

    private void loadImages() {
        planeImage = ResourceManager.loadImage("airplan", "1.png");
        normalEnemyImage = ResourceManager.loadImage("chicken", "normal_chicken.png");
        fastEnemyImage = ResourceManager.loadImage("chicken", "fast_chicken.png");
        zigzagEnemyImage = ResourceManager.loadImage("chicken", "zigzag_chicken.png");
        shooterEnemyImage = ResourceManager.loadImage("chicken", "shooter_chicken.png");
        bossImage = ResourceManager.loadImage("chicken", "boss1.png");
        boss2Image = ResourceManager.loadImage("chicken", "boss2.png");
    }

    private void startGame() {
        score = 0;
        currentLevel = 1;
        plane = new Plane(375, 500, planeImage, ScoreManager.selectedPlane);
        enemies = new ArrayList<>();
        bullets = new ArrayList<>();
        eggs = new ArrayList<>();
        powerUps = new ArrayList<>();
        explosions = new ArrayList<>();
        bossBullets = new ArrayList<>();
        enemyCellMap = new HashMap<>();
        grid = new Cell[5][8];
        boss = null;
        initLevel1();
        gameState = GameState.PLAYING;
    }

    private void initLevel1() {
        gridSpeedX = 1.2; gridStepY = 20; eggInterval = 3000;
        int startX = 80, startY = 50, hGap = 70, vGap = 50;
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 8; col++) {
                int x = startX + col * hGap, y = startY + row * vGap;
                Cell cell = new Cell(row, col, x, y, 2, "Normal");
                grid[row][col] = cell;
                NormalEnemy enemy = new NormalEnemy(x, y, normalEnemyImage);
                enemies.add(enemy);
                enemyCellMap.put(enemy, cell);
            }
        }
    }

    private void initLevel2() {
        gridSpeedX = 1.5; gridStepY = 20; eggInterval = 2000;
        int startX = 80, startY = 50, hGap = 70, vGap = 50;
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 8; col++) {
                int x = startX + col * hGap, y = startY + row * vGap;
                String type = (row == 0) ? "Fast" : "Normal";
                Cell cell = new Cell(row, col, x, y, (row == 0 ? 1 : 2), type);
                grid[row][col] = cell;
                Enemy enemy = (row == 0) ? new FastEnemy(x, y, fastEnemyImage) : new NormalEnemy(x, y, normalEnemyImage);
                enemies.add(enemy);
                enemyCellMap.put(enemy, cell);
            }
        }
    }

    private void initLevel3() {
        gridSpeedX = 2.0; gridStepY = 25; eggInterval = 1500;
        int startX = 80, startY = 50, hGap = 70, vGap = 50;
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 8; col++) {
                int x = startX + col * hGap, y = startY + row * vGap;
                String type = (row == 0) ? "Zigzag" : ((row == 1) ? "Fast" : "Normal");
                Cell cell = new Cell(row, col, x, y, (row >= 2 ? 2 : 1), type);
                grid[row][col] = cell;
                Enemy enemy = (row == 0) ? new ZigzagEnemy(x, y, zigzagEnemyImage) : ((row == 1) ? new FastEnemy(x, y, fastEnemyImage) : new NormalEnemy(x, y, normalEnemyImage));
                enemies.add(enemy);
                enemyCellMap.put(enemy, cell);
            }
        }
    }

    private void initLevel4() {
        enemies.clear(); eggs.clear();
        boss = new BossEnemy(325, 50, 150, 150, bossImage);
        boss.setHealth(50);
    }

    private void initLevel5() {
        gridSpeedX = 2.5; gridStepY = 25; eggInterval = 1000;
        int startX = 80, startY = 50, hGap = 70, vGap = 50;
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 8; col++) {
                int x = startX + col * hGap, y = startY + row * vGap;
                Cell cell = new Cell(row, col, x, y, 3, (row < 2 ? "Shooter" : "Fast"));
                grid[row][col] = cell;
                Enemy enemy = (row < 2) ? new ShooterEnemy(x, y, shooterEnemyImage) : new FastEnemy(x, y, fastEnemyImage);
                enemies.add(enemy);
                enemyCellMap.put(enemy, cell);
            }
        }
    }

    private void initLevel6() {
        gridSpeedX = 3.0; gridStepY = 30; eggInterval = 800;
        int startX = 80, startY = 50, hGap = 70, vGap = 50;
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 8; col++) {
                int x = startX + col * hGap, y = startY + row * vGap;
                Cell cell = new Cell(row, col, x, y, 4, (row < 2 ? "Zigzag" : "Shooter"));
                grid[row][col] = cell;
                Enemy enemy = (row < 2) ? new ZigzagEnemy(x, y, zigzagEnemyImage) : new ShooterEnemy(x, y, shooterEnemyImage);
                enemies.add(enemy);
                enemyCellMap.put(enemy, cell);
            }
        }
    }

    private void initLevel7() {
        gridSpeedX = 3.5; gridStepY = 30; eggInterval = 700;
        int startX = 80, startY = 50, hGap = 70, vGap = 50;
        for (int row = 0; row < 5; row++) {
            for (int col = 0; col < 8; col++) {
                int x = startX + col * hGap, y = startY + row * vGap;
                String type = "Normal";
                Image img = normalEnemyImage;
                if (row == 0) { type = "Zigzag"; img = zigzagEnemyImage; }
                else if (row == 1) { type = "Shooter"; img = shooterEnemyImage; }
                else if (row == 2) { type = "Fast"; img = fastEnemyImage; }
                Cell cell = new Cell(row, col, x, y, 4, type);
                grid[row][col] = cell;
                Enemy enemy;
                if (row == 0) enemy = new ZigzagEnemy(x, y, img);
                else if (row == 1) enemy = new ShooterEnemy(x, y, img);
                else if (row == 2) enemy = new FastEnemy(x, y, img);
                else enemy = new NormalEnemy(x, y, img);
                enemies.add(enemy);
                enemyCellMap.put(enemy, cell);
            }
        }
    }

    private void initLevel8() {
        enemies.clear(); eggs.clear();
        boss = new BossEnemy(300, 50, 200, 200, boss2Image);
        boss.setHealth(100);
    }

    private void buyPlane(int type, int price) {
        if (ScoreManager.coins >= price) {
            ScoreManager.coins -= price;
            ScoreManager.selectedPlane = type;
            ScoreManager.save();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        if (gameState == GameState.MENU) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 40));
            g2d.drawString("CHICKEN INVADERS", 200, 200);
            g2d.setFont(new Font("Arial", Font.PLAIN, 20));
            g2d.drawString("Press ENTER to Play", 300, 300);
            g2d.drawString("Press S to open STORE", 290, 350);
        } else if (gameState == GameState.PLAYING) {
            plane.draw(g2d);
            for (Enemy enemy : enemies) enemy.draw(g2d);
            for (Bullet bullet : bullets) bullet.draw(g2d);
            for (Egg egg : eggs) egg.draw(g2d);
            for (PowerUp pu : powerUps) pu.draw(g2d);
            for (Explosion ex : explosions) ex.draw(g2d);
            if (boss != null) boss.draw(g2d);
            for (BossBullet bb : bossBullets) bb.draw(g2d);
            GameHUD.draw(g2d, score, ScoreManager.coins, plane.getLives(), currentLevel);
            g2d.drawString("Fire Power: " + plane.getFireCount(), 10, 80);
        } else if (gameState == GameState.GAMEOVER) {
            g2d.setColor(Color.RED);
            g2d.drawString("GAME OVER", 350, 300);
        } else if (gameState == GameState.WIN) {
            g2d.setColor(Color.GREEN);
            g2d.drawString("YOU WIN!", 350, 300);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameState == GameState.PLAYING) {
            updatePlane();
            updateBullets();
            updateEnemies();
            updateEggs();
            updatePowerUps();
            updateExplosions();
            updateBoss();
            updateBossBullets();
            checkCollisions();
            checkLevelUp();
            if (plane.getLives() <= 0) { gameState = GameState.GAMEOVER; ScoreManager.coins += score; ScoreManager.save(); }
        }
        repaint();
    }

    private void updatePlane() {
        if (leftPressed) plane.moveLeft();
        if (rightPressed) plane.moveRight(getWidth());
        if (spacePressed && plane.canShoot()) { plane.shoot(bullets); SoundManager.playSound("mixkit-short-laser-gun-shot-1670.wav"); }
    }

    private void updateBullets() {
        Iterator<Bullet> iter = bullets.iterator();
        while (iter.hasNext()) { Bullet b = iter.next(); b.move(); if (b.getY() < 0) iter.remove(); }
    }

    private void updatePowerUps() {
        Iterator<PowerUp> iter = powerUps.iterator();
        while (iter.hasNext()) {
            PowerUp p = iter.next();
            p.move();
            if (p.getBounds().intersects(new Rectangle(plane.getX(), plane.getY(), plane.getWidth(), plane.getHeight()))) {
                if (p.getType().equals("AddFire")) plane.addFire();
                iter.remove();
            } else if (p.getY() > getHeight()) iter.remove();
        }
    }

    private void updateExplosions() {
        Iterator<Explosion> iter = explosions.iterator();
        while (iter.hasNext()) { if (!iter.next().update()) iter.remove(); }
    }

    private void updateEnemies() {
        if (enemies.isEmpty()) return;
        for (Enemy enemy : enemies) enemy.move();
        boolean hitEdge = false;
        for (Enemy enemy : enemies) {
            if (!(enemy instanceof ZigzagEnemy)) {
                if (enemy.getX() >= getWidth() - 40 && gridDirection == 1) { hitEdge = true; break; }
                if (enemy.getX() <= 0 && gridDirection == -1) { hitEdge = true; break; }
            }
        }
        if (hitEdge) { gridDirection *= -1; for (Enemy enemy : enemies) enemy.setY(enemy.getY() + gridStepY); }
        else { for (Enemy enemy : enemies) { if (!(enemy instanceof ZigzagEnemy)) enemy.setX(enemy.getX() + (int)(gridSpeedX * gridDirection)); } }
    }

    private void updateEggs() {
        Iterator<Egg> iter = eggs.iterator();
        while (iter.hasNext()) { Egg egg = iter.next(); egg.move(); if (egg.getY() > getHeight()) iter.remove(); }
    }

    private void updateBoss() {
        if (boss == null) return;
        boss.move();
        if (boss.canShoot(currentLevel == 8 ? 1000 : 1500)) {
            int bx = boss.getX() + boss.getWidth() / 2, by = boss.getY() + boss.getHeight() - 20;
            if (currentLevel == 4) {
                bossBullets.add(new BossBullet(bx, by, 0, 4, null));
                bossBullets.add(new BossBullet(bx, by, -4, 0, null));
                bossBullets.add(new BossBullet(bx, by, 4, 0, null));
                bossBullets.add(new BossBullet(bx, by, 0, -4, null));
            } else if (currentLevel == 8) {
                for (int i = 0; i < 8; i++) {
                    double angle = Math.toRadians(i * 45);
                    bossBullets.add(new BossBullet(bx, by, (int)(Math.cos(angle)*5), (int)(Math.sin(angle)*5), null));
                }
            }
        }
    }

    private void updateBossBullets() {
        Iterator<BossBullet> iter = bossBullets.iterator();
        while (iter.hasNext()) { BossBullet b = iter.next(); b.move(); if (b.getY() > getHeight()) iter.remove(); }
    }

    private void checkCollisions() {
        Iterator<Bullet> bulletIter = bullets.iterator();
        ArrayList<Enemy> newSpawns = new ArrayList<>();
        while (bulletIter.hasNext()) {
            Bullet b = bulletIter.next();
            Rectangle bBounds = b.getBounds();
            boolean bulletRemoved = false;
            if (boss != null && bBounds.intersects(new Rectangle(boss.getX(), boss.getY(), boss.getWidth(), boss.getHeight()))) {
                boss.takeDamage(1 * plane.getDamageMultiplier()); bulletIter.remove(); bulletRemoved = true;
            }
            if (!bulletRemoved) {
                Iterator<Enemy> enemyIter = enemies.iterator();
                while (enemyIter.hasNext()) {
                    Enemy e = enemyIter.next();
                    if (bBounds.intersects(e.getBounds())) {
                        Cell cell = enemyCellMap.get(e);
                        cell.decreaseCounter();
                        enemyIter.remove();
                        enemyCellMap.remove(e);
                        bulletIter.remove();
                        explosions.add(new Explosion(e.getX(), e.getY()));
                        if (random.nextDouble() < 0.2) powerUps.add(new PowerUp(e.getX(), e.getY(), "AddFire"));
                        if (cell.getCounter() > 0) {
                            Enemy ne = (cell.getEnemyType().equals("Normal")) ? new NormalEnemy(cell.getX(), cell.getY(), normalEnemyImage) :
                                    (cell.getEnemyType().equals("Fast")) ? new FastEnemy(cell.getX(), cell.getY(), fastEnemyImage) :
                                            (cell.getEnemyType().equals("Zigzag")) ? new ZigzagEnemy(cell.getX(), cell.getY(), zigzagEnemyImage) :
                                                    new ShooterEnemy(cell.getX(), cell.getY(), shooterEnemyImage);
                            newSpawns.add(ne); enemyCellMap.put(ne, cell);
                        }
                        break;
                    }
                }
            }
        }
        enemies.addAll(newSpawns);
    }

    private void checkLevelUp() {
        if (enemies.isEmpty() && boss == null && gameState == GameState.PLAYING) {
            currentLevel++;
            if (currentLevel == 2) initLevel2(); else if (currentLevel == 3) initLevel3(); else if (currentLevel == 4) initLevel4();
            else if (currentLevel == 5) initLevel5(); else if (currentLevel == 6) initLevel6(); else if (currentLevel == 7) initLevel7();
            else if (currentLevel == 8) initLevel8();
        } else if (boss != null && boss.getHealth() <= 0) {
            boss = null;
            if (currentLevel == 8) gameState = GameState.WIN; else { currentLevel++; initLevel5(); }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (gameState == GameState.MENU) { if (key == KeyEvent.VK_ENTER) startGame(); if (key == KeyEvent.VK_S) gameState = GameState.STORE; }
        else if (gameState == GameState.PLAYING) {
            if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) leftPressed = true;
            if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) rightPressed = true;
            if (key == KeyEvent.VK_SPACE) spacePressed = true;
        } else if (gameState == GameState.GAMEOVER || gameState == GameState.WIN) { if (key == KeyEvent.VK_ESCAPE) gameState = GameState.MENU; }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (gameState == GameState.PLAYING) {
            int key = e.getKeyCode();
            if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) leftPressed = false;
            if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) rightPressed = false;
            if (key == KeyEvent.VK_SPACE) spacePressed = false;
        }
    }
    @Override public void keyTyped(KeyEvent e) {}
}