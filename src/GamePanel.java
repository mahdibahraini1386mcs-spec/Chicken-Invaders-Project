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

        addAncestorListener(new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                requestFocusInWindow();
            }
            @Override
            public void ancestorRemoved(AncestorEvent event) {}
            @Override
            public void ancestorMoved(AncestorEvent event) {}
        });

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
                int counter = (row == 0) ? 1 : 2;
                Image img = (row == 0) ? fastEnemyImage : normalEnemyImage;
                Cell cell = new Cell(row, col, x, y, counter, type);
                grid[row][col] = cell;
                Enemy enemy = (row == 0) ? new FastEnemy(x, y, img) : new NormalEnemy(x, y, img);
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
                int counter = (row >= 2) ? 2 : 1;
                Image img = (row == 0) ? zigzagEnemyImage : ((row == 1) ? fastEnemyImage : normalEnemyImage);
                Cell cell = new Cell(row, col, x, y, counter, type);
                grid[row][col] = cell;
                Enemy enemy = (row == 0) ? new ZigzagEnemy(x, y, img) : ((row == 1) ? new FastEnemy(x, y, img) : new NormalEnemy(x, y, img));
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
                String type = (row < 2) ? "Shooter" : "Fast";
                int counter = 3;
                Image img = (row < 2) ? shooterEnemyImage : fastEnemyImage;
                Cell cell = new Cell(row, col, x, y, counter, type);
                grid[row][col] = cell;
                Enemy enemy = (row < 2) ? new ShooterEnemy(x, y, img) : new FastEnemy(x, y, img);
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
                String type = (row < 2) ? "Zigzag" : "Shooter";
                int counter = 4;
                Image img = (row < 2) ? zigzagEnemyImage : shooterEnemyImage;
                Cell cell = new Cell(row, col, x, y, counter, type);
                grid[row][col] = cell;
                Enemy enemy = (row < 2) ? new ZigzagEnemy(x, y, img) : new ShooterEnemy(x, y, img);
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
            g2d.drawString("Total Points: " + ScoreManager.coins, 330, 450);
        } else if (gameState == GameState.STORE) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 30));
            g2d.drawString("STORE - Total Points: " + ScoreManager.coins, 200, 100);
            g2d.setFont(new Font("Arial", Font.PLAIN, 18));
            g2d.drawString("1. Default (0 pts) - Spd: 5, FireRate: 300ms, Lives: 3", 100, 200);
            g2d.drawString("2. Fast (5000 pts) - Spd: 7, FireRate: 250ms, Lives: 3", 100, 250);
            g2d.drawString("3. Heavy (8000 pts) - Spd: 4, FireRate: 200ms, Lives: 5", 100, 300);
            g2d.drawString("4. Sniper (10000 pts) - Spd: 5, FireRate: 150ms, Boss Dmg x2", 100, 350);
            g2d.setColor(Color.YELLOW);
            g2d.drawString("Current Plane: " + (ScoreManager.selectedPlane + 1), 100, 420);
            g2d.drawString("Press 1-4 to Buy/Equip. Press ESC to return.", 100, 470);
        } else if (gameState == GameState.PLAYING) {
            plane.draw(g2d);
            for (Enemy enemy : enemies) enemy.draw(g2d);
            for (Bullet bullet : bullets) bullet.draw(g2d);
            for (Egg egg : eggs) egg.draw(g2d);
            if (boss != null) boss.draw(g2d);
            for (BossBullet bb : bossBullets) bb.draw(g2d);
            GameHUD.draw(g2d, score, ScoreManager.coins, plane.getLives(), currentLevel);
            g2d.setColor(Color.WHITE);
            g2d.drawString("Fire Power: " + plane.getFireCount(), 10, 80);
        } else if (gameState == GameState.GAMEOVER) {
            g2d.setColor(Color.RED);
            g2d.setFont(new Font("Arial", Font.BOLD, 50));
            g2d.drawString("GAME OVER", 250, 300);
            g2d.setFont(new Font("Arial", Font.PLAIN, 20));
            g2d.setColor(Color.WHITE);
            g2d.drawString("Press ESC to return to Menu", 260, 400);
        } else if (gameState == GameState.WIN) {
            g2d.setColor(Color.GREEN);
            g2d.setFont(new Font("Arial", Font.BOLD, 50));
            g2d.drawString("YOU WIN!", 280, 300);
            g2d.setFont(new Font("Arial", Font.PLAIN, 20));
            g2d.setColor(Color.WHITE);
            g2d.drawString("Press ESC to return to Menu", 260, 400);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameState == GameState.PLAYING) {
            updatePlane();
            updateBullets();
            updateEnemies();
            updateEggs();
            updateBoss();
            updateBossBullets();
            checkCollisions();
            checkLevelUp();
            if (plane.getLives() <= 0) {
                gameState = GameState.GAMEOVER;
                ScoreManager.coins += score;
                ScoreManager.save();
            }
        }
        repaint();
    }

    private void updatePlane() {
        if (leftPressed) plane.moveLeft();
        if (rightPressed) plane.moveRight(getWidth());
        if (spacePressed && plane.canShoot()) {
            plane.shoot(bullets);
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
        for (Enemy enemy : enemies) enemy.move();
        boolean hitEdge = false;
        for (Enemy enemy : enemies) {
            if (!(enemy instanceof ZigzagEnemy)) {
                if (enemy.getX() >= getWidth() - 40 && gridDirection == 1) { hitEdge = true; break; }
                if (enemy.getX() <= 0 && gridDirection == -1) { hitEdge = true; break; }
            }
        }
        if (hitEdge) {
            gridDirection *= -1;
            for (Enemy enemy : enemies) enemy.setY(enemy.getY() + gridStepY);
        } else {
            for (Enemy enemy : enemies) {
                if (!(enemy instanceof ZigzagEnemy)) enemy.setX(enemy.getX() + (int)(gridSpeedX * gridDirection));
            }
        }
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastEggTime >= eggInterval) {
            lastEggTime = currentTime;
            Enemy randomEnemy = enemies.get(random.nextInt(enemies.size()));
            eggs.add(new Egg(randomEnemy.getX() + 20, randomEnemy.getY() + 40, null));
            if (randomEnemy instanceof ShooterEnemy) {
                int targetX = plane.getX() + plane.getWidth() / 2;
                int targetY = plane.getY();
                int dx = (targetX > randomEnemy.getX()) ? 3 : -3;
                bossBullets.add(new BossBullet(randomEnemy.getX() + 20, randomEnemy.getY() + 40, dx, 5, null));
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

    private void updateBoss() {
        if (boss == null) return;
        boss.move();
        long attackRate = (currentLevel == 8) ? 1000 : 1500;
        if (boss.canShoot(attackRate)) {
            int bx = boss.getX() + boss.getWidth() / 2, by = boss.getY() + boss.getHeight() - 20;
            if (currentLevel == 4) {
                bossBullets.add(new BossBullet(bx, by, 0, 4, null));
                bossBullets.add(new BossBullet(bx, by, -4, 0, null));
                bossBullets.add(new BossBullet(bx, by, 4, 0, null));
                bossBullets.add(new BossBullet(bx, by, 0, -4, null));
            } else if (currentLevel == 8) {
                bossBullets.add(new BossBullet(bx, by, 0, 5, null));
                bossBullets.add(new BossBullet(bx, by, 5, 5, null));
                bossBullets.add(new BossBullet(bx, by, 5, 0, null));
                bossBullets.add(new BossBullet(bx, by, 5, -5, null));
                bossBullets.add(new BossBullet(bx, by, 0, -5, null));
                bossBullets.add(new BossBullet(bx, by, -5, -5, null));
                bossBullets.add(new BossBullet(bx, by, -5, 0, null));
                bossBullets.add(new BossBullet(bx, by, -5, 5, null));
            }
        }
    }

    private void updateBossBullets() {
        Iterator<BossBullet> iter = bossBullets.iterator();
        while (iter.hasNext()) {
            BossBullet b = iter.next();
            b.move();
            if (b.getY() > getHeight() || b.getY() < 0 || b.getX() < 0 || b.getX() > getWidth()) iter.remove();
        }
    }

    private void checkCollisions() {
        Iterator<Bullet> bulletIter = bullets.iterator();
        ArrayList<Enemy> newSpawns = new ArrayList<>();
        while (bulletIter.hasNext()) {
            Bullet b = bulletIter.next();
            Rectangle bBounds = b.getBounds();
            boolean bulletRemoved = false;

            if (boss != null) {
                if (bBounds.intersects(new Rectangle(boss.getX(), boss.getY(), boss.getWidth(), boss.getHeight()))) {
                    boss.takeDamage(1 * plane.getDamageMultiplier());
                    bulletIter.remove();
                    bulletRemoved = true;
                    SoundManager.playSound("mixkit-epic-impact-afar-explosion-2782.wav");
                }
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
                        if (e instanceof NormalEnemy) score += 10;
                        else if (e instanceof FastEnemy) score += 15;
                        else if (e instanceof ZigzagEnemy) score += 20;
                        else if (e instanceof ShooterEnemy) score += 25;
                        SoundManager.playSound("mixkit-epic-impact-afar-explosion-2782.wav");

                        if (cell.getCounter() > 0) {
                            String type = cell.getEnemyType();
                            Enemy newEnemy = null;
                            if (type.equals("Normal")) newEnemy = new NormalEnemy(cell.getX(), cell.getY(), normalEnemyImage);
                            else if (type.equals("Fast")) newEnemy = new FastEnemy(cell.getX(), cell.getY(), fastEnemyImage);
                            else if (type.equals("Zigzag")) newEnemy = new ZigzagEnemy(cell.getX(), cell.getY(), zigzagEnemyImage);
                            else if (type.equals("Shooter")) newEnemy = new ShooterEnemy(cell.getX(), cell.getY(), shooterEnemyImage);
                            if (newEnemy != null) {
                                newSpawns.add(newEnemy);
                                enemyCellMap.put(newEnemy, cell);
                            }
                        }
                        break;
                    }
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
        Iterator<BossBullet> bbIter = bossBullets.iterator();
        while (bbIter.hasNext()) {
            BossBullet bb = bbIter.next();
            if (bb.getBounds().intersects(pBounds)) {
                plane.loseLife();
                bbIter.remove();
            }
        }
        if (boss != null && new Rectangle(boss.getX(), boss.getY(), boss.getWidth(), boss.getHeight()).intersects(pBounds)) {
            plane.loseLife();
        }
    }

    private void checkLevelUp() {
        if (enemies.isEmpty() && boss == null && gameState == GameState.PLAYING) {
            score += 200;
            currentLevel++;
            if (currentLevel == 2) initLevel2();
            else if (currentLevel == 3) initLevel3();
            else if (currentLevel == 4) initLevel4();
            else if (currentLevel == 5) initLevel5();
            else if (currentLevel == 6) initLevel6();
            else if (currentLevel == 7) initLevel7();
            else if (currentLevel == 8) initLevel8();
        } else if (boss != null && boss.getHealth() <= 0) {
            score += (currentLevel == 4) ? 500 : 1000;
            boss = null;
            if (currentLevel == 8) {
                gameState = GameState.WIN;
                ScoreManager.coins += score;
                ScoreManager.save();
            } else {
                currentLevel++;
                initLevel5();
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (gameState == GameState.MENU) {
            if (key == KeyEvent.VK_ENTER) startGame();
            if (key == KeyEvent.VK_S) gameState = GameState.STORE;
        } else if (gameState == GameState.STORE) {
            if (key == KeyEvent.VK_ESCAPE) gameState = GameState.MENU;
            if (key == KeyEvent.VK_1) buyPlane(0, 0);
            if (key == KeyEvent.VK_2) buyPlane(1, 5000);
            if (key == KeyEvent.VK_3) buyPlane(2, 8000);
            if (key == KeyEvent.VK_4) buyPlane(3, 10000);
        } else if (gameState == GameState.PLAYING) {
            if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) leftPressed = true;
            if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) rightPressed = true;
            if (key == KeyEvent.VK_SPACE) spacePressed = true;
        } else if (gameState == GameState.GAMEOVER || gameState == GameState.WIN) {
            if (key == KeyEvent.VK_ESCAPE) gameState = GameState.MENU;
        }
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

    @Override
    public void keyTyped(KeyEvent e) {}
}