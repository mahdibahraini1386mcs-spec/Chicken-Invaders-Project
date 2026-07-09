import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private enum GameState { MENU, PLAYING, PAUSED, GAMEOVER, WIN, HIGH_SCORES, SETTINGS, HOW_TO_PLAY }
    private GameState gameState = GameState.MENU;
    private GameState previousState = GameState.MENU;

    private String[] menuOptions = {"New Game", "High Scores", "Settings", "How to Play", "Exit"};
    private int currentMenuSelection = 0;

    private Timer timer;
    private Plane plane;
    private ArrayList<Enemy> enemies;
    private ArrayList<Bullet> bullets;
    private ArrayList<Egg> eggs;
    private ArrayList<PowerUp> powerUps;
    private ArrayList<Explosion> explosions;
    private ArrayList<BossExplosion> bossExplosions;
    private Cell[][] grid;
    private HashMap<Enemy, Cell> enemyCellMap;
    private BossEnemy boss;
    private ArrayList<BossBullet> bossBullets;

    private boolean leftPressed = false;
    private boolean rightPressed = false;
    private boolean upPressed = false;
    private boolean downPressed = false;
    private boolean spacePressed = false;

    private int gridDirection = 1;
    private double gridSpeedX = 1.2;
    private int gridStepY = 20;
    private long lastEggTime = 0;
    private int eggInterval = 3000;
    private int bossDefeatTimer = 0;
    private Random random = new Random();

    private int currentLevel = 1;
    private int score = 0;

    private Image planeImage, normalEnemyImage, fastEnemyImage, zigzagEnemyImage, shooterEnemyImage, bossImage, boss2Image;
    private Image menuBgImage, gameBgImage, eggImage;
    private Image explosion1Image, explosion2Image;

    public GamePanel() {
        setFocusable(true);
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(800, 600));
        addKeyListener(this);

        explosions = new ArrayList<>();
        bossExplosions = new ArrayList<>();
        loadImages();
        ScoreManager.load();
        timer = new Timer(16, this);
        timer.start();
        SoundManager.playMusic("Chicken Invaders 2 Remastered OST - Main Theme.wav");
    }

    private void loadImages() {
        planeImage = ResourceManager.loadImage("airplan", "1.png");
        normalEnemyImage = ResourceManager.loadImage("chicken", "normal_chicken.png");
        fastEnemyImage = ResourceManager.loadImage("chicken", "fast_chicken.png");
        zigzagEnemyImage = ResourceManager.loadImage("chicken", "zigzag_chicken.png");
        shooterEnemyImage = ResourceManager.loadImage("chicken", "shooter_chicken.png");
        bossImage = ResourceManager.loadImage("chicken", "boss1.png");
        boss2Image = ResourceManager.loadImage("chicken", "boss2.png");

        menuBgImage = ResourceManager.loadImage("background", "background.jpg");
        gameBgImage = ResourceManager.loadImage("background", "background2.jpg");
        eggImage = ResourceManager.loadImage("chicken", "egg.png");

        explosion1Image = ResourceManager.loadImage("airplan", "Explosion.png");
        explosion2Image = ResourceManager.loadImage("airplan", "Explosion2.png");
    }

    private void startGame() {
        score = 0;
        currentLevel = 1;
        bossDefeatTimer = 0;
        plane = new Plane(375, 500, planeImage, ScoreManager.selectedPlane);
        enemies = new ArrayList<>();
        bullets = new ArrayList<>();
        eggs = new ArrayList<>();
        powerUps = new ArrayList<>();
        explosions = new ArrayList<>();
        bossExplosions = new ArrayList<>();
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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        if (gameState == GameState.MENU) {
            if (menuBgImage != null) g2d.drawImage(menuBgImage, 0, 0, getWidth(), getHeight(), null);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 50));
            g2d.drawString("CHICKEN INVADERS", 150, 150);

            g2d.setFont(new Font("Arial", Font.BOLD, 30));
            for (int i = 0; i < menuOptions.length; i++) {
                if (i == currentMenuSelection) {
                    g2d.setColor(Color.YELLOW);
                    g2d.drawString("> " + menuOptions[i] + " <", 280, 280 + (i * 50));
                } else {
                    g2d.setColor(Color.WHITE);
                    g2d.drawString(menuOptions[i], 310, 280 + (i * 50));
                }
            }
        } else if (gameState == GameState.HIGH_SCORES) {
            if (menuBgImage != null) g2d.drawImage(menuBgImage, 0, 0, getWidth(), getHeight(), null);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 40));
            g2d.drawString("HIGH SCORES", 250, 150);
            g2d.setFont(new Font("Arial", Font.PLAIN, 20));
            g2d.drawString("1. Player - 0 pts - Level 1", 280, 250);
            g2d.drawString("Press ESC to return", 300, 500);
        } else if (gameState == GameState.SETTINGS) {
            if (menuBgImage != null) g2d.drawImage(menuBgImage, 0, 0, getWidth(), getHeight(), null);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 40));
            g2d.drawString("SETTINGS", 300, 150);
            g2d.setFont(new Font("Arial", Font.PLAIN, 20));
            g2d.drawString("Music: " + (SoundManager.getMusicStatus() ? "ON" : "OFF") + " (Press M)", 300, 250);
            g2d.drawString("SFX: " + (SoundManager.getSFXStatus() ? "ON" : "OFF") + " (Press O)", 300, 300);
            g2d.drawString("Press ESC to return", 300, 500);
        } else if (gameState == GameState.HOW_TO_PLAY) {
            if (menuBgImage != null) g2d.drawImage(menuBgImage, 0, 0, getWidth(), getHeight(), null);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 40));
            g2d.drawString("HOW TO PLAY", 250, 150);
            g2d.setFont(new Font("Arial", Font.PLAIN, 20));
            g2d.drawString("W/A/S/D or Arrows: Move", 280, 250);
            g2d.drawString("Spacebar: Shoot", 280, 300);
            g2d.drawString("P: Pause/Resume", 280, 350);
            g2d.drawString("ESC: Back to Menu", 280, 400);
            g2d.drawString("Press ESC to return", 300, 500);
        } else if (gameState == GameState.PLAYING) {
            if (gameBgImage != null) g2d.drawImage(gameBgImage, 0, 0, getWidth(), getHeight(), null);
            plane.draw(g2d);
            for (Enemy enemy : enemies) enemy.draw(g2d);
            for (Bullet bullet : bullets) bullet.draw(g2d);
            for (Egg egg : eggs) egg.draw(g2d);
            for (PowerUp pu : powerUps) pu.draw(g2d);
            for (Explosion ex : explosions) ex.draw(g2d);
            for (BossExplosion bex : bossExplosions) bex.draw(g2d);
            if (boss != null) boss.draw(g2d);
            for (BossBullet bb : bossBullets) bb.draw(g2d);
            GameHUD.draw(g2d, score, ScoreManager.coins, plane.getLives(), currentLevel);
        } else if (gameState == GameState.PAUSED) {
            if (gameBgImage != null) g2d.drawImage(gameBgImage, 0, 0, getWidth(), getHeight(), null);
            plane.draw(g2d);
            for (Enemy enemy : enemies) enemy.draw(g2d);
            for (Bullet bullet : bullets) bullet.draw(g2d);
            for (Egg egg : eggs) egg.draw(g2d);
            for (PowerUp pu : powerUps) pu.draw(g2d);
            for (Explosion ex : explosions) ex.draw(g2d);
            for (BossExplosion bex : bossExplosions) bex.draw(g2d);
            if (boss != null) boss.draw(g2d);
            for (BossBullet bb : bossBullets) bb.draw(g2d);
            GameHUD.draw(g2d, score, ScoreManager.coins, plane.getLives(), currentLevel);

            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRect(0, 0, getWidth(), getHeight());

            g2d.setColor(Color.YELLOW);
            g2d.setFont(new Font("Arial", Font.BOLD, 50));
            g2d.drawString("PAUSED", 300, 300);
            g2d.setFont(new Font("Arial", Font.PLAIN, 20));
            g2d.drawString("Press P to Resume", 310, 350);
            g2d.drawString("Press ESC for Main Menu", 280, 390);
        } else if (gameState == GameState.GAMEOVER) {
            g2d.setColor(Color.RED);
            g2d.setFont(new Font("Arial", Font.BOLD, 50));
            g2d.drawString("GAME OVER", 250, 300);
        } else if (gameState == GameState.WIN) {
            g2d.setColor(Color.GREEN);
            g2d.setFont(new Font("Arial", Font.BOLD, 50));
            g2d.drawString("YOU WIN!", 280, 300);
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
            updateBossExplosions();
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
        if (upPressed) plane.moveUp();
        if (downPressed) plane.moveDown(getHeight());

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
        while (iter.hasNext()) {
            if (!iter.next().update()) iter.remove();
        }
    }

    private void updateBossExplosions() {
        Iterator<BossExplosion> iter = bossExplosions.iterator();
        while (iter.hasNext()) {
            if (!iter.next().update()) iter.remove();
        }
    }

    private void updateEnemies() {
        if (enemies.isEmpty()) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastEggTime > eggInterval && !enemies.isEmpty()) {
            Enemy randomEnemy = enemies.get(random.nextInt(enemies.size()));
            eggs.add(new Egg(randomEnemy.getX() + 20, randomEnemy.getY() + 40, eggImage));
            lastEggTime = currentTime;
        }

        for (Enemy enemy : enemies) {
            enemy.move();
            if (enemy.getY() > getHeight()) {
                enemy.setY(-40);
            }
        }

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
        if (boss.canShoot(currentLevel == 8 ? 1000 : 1500)) {
            int bx = boss.getX() + boss.getWidth() / 2, by = boss.getY() + boss.getHeight() - 20;
            if (currentLevel == 4) {
                bossBullets.add(new BossBullet(bx, by, 0, 4, eggImage));
                bossBullets.add(new BossBullet(bx, by, -4, 0, eggImage));
                bossBullets.add(new BossBullet(bx, by, 4, 0, eggImage));
                bossBullets.add(new BossBullet(bx, by, 0, -4, eggImage));
            } else if (currentLevel == 8) {
                for (int i = 0; i < 8; i++) {
                    double angle = Math.toRadians(i * 45);
                    bossBullets.add(new BossBullet(bx, by, (int)(Math.cos(angle)*5), (int)(Math.sin(angle)*5), eggImage));
                }
            }
        }
    }

    private void updateBossBullets() {
        Iterator<BossBullet> iter = bossBullets.iterator();
        while (iter.hasNext()) {
            BossBullet b = iter.next();
            b.move();
            if (b.getY() > getHeight()) iter.remove();
        }
    }

    private void checkCollisions() {
        Iterator<Bullet> bulletIter = bullets.iterator();
        ArrayList<Enemy> newSpawns = new ArrayList<>();
        while (bulletIter.hasNext()) {
            Bullet b = bulletIter.next();
            Rectangle bBounds = b.getBounds();
            boolean bulletRemoved = false;

            if (boss != null && bBounds.intersects(new Rectangle(boss.getX(), boss.getY(), boss.getWidth(), boss.getHeight()))) {
                boss.takeDamage(1 * plane.getDamageMultiplier());
                bulletIter.remove();
                bulletRemoved = true;
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
                        SoundManager.playSound("mixkit-epic-impact-afar-explosion-2782.wav");

                        if (random.nextDouble() < 0.2) powerUps.add(new PowerUp(e.getX(), e.getY(), "AddFire"));

                        if (cell.getCounter() > 0) {
                            Enemy ne = (cell.getEnemyType().equals("Normal")) ? new NormalEnemy(cell.getX(), cell.getY(), normalEnemyImage) :
                                    (cell.getEnemyType().equals("Fast")) ? new FastEnemy(cell.getX(), cell.getY(), fastEnemyImage) :
                                            (cell.getEnemyType().equals("Zigzag")) ? new ZigzagEnemy(cell.getX(), cell.getY(), zigzagEnemyImage) :
                                                    new ShooterEnemy(cell.getX(), cell.getY(), shooterEnemyImage);
                            newSpawns.add(ne);
                            enemyCellMap.put(ne, cell);
                        }
                        break;
                    }
                }
            }
        }
        enemies.addAll(newSpawns);
    }

    private void checkLevelUp() {
        if (boss != null && boss.getHealth() <= 0) {
            bossExplosions.add(new BossExplosion(boss.getX(), boss.getY(), boss.getWidth(), boss.getHeight(), explosion1Image, explosion2Image));
            SoundManager.playSound("mixkit-epic-impact-afar-explosion-2782.wav");
            boss = null;
            bossDefeatTimer = 100;
        }

        if (bossDefeatTimer > 0) {
            bossDefeatTimer--;
            if (bossDefeatTimer == 0) {
                if (currentLevel == 8) gameState = GameState.WIN;
                else { currentLevel++; initLevel5(); }
            }
        } else if (enemies.isEmpty() && boss == null && gameState == GameState.PLAYING) {
            currentLevel++;
            if (currentLevel == 2) initLevel2();
            else if (currentLevel == 3) initLevel3();
            else if (currentLevel == 4) initLevel4();
            else if (currentLevel == 5) initLevel5();
            else if (currentLevel == 6) initLevel6();
            else if (currentLevel == 7) initLevel7();
            else if (currentLevel == 8) initLevel8();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (gameState == GameState.MENU) {
            if (key == KeyEvent.VK_UP) {
                currentMenuSelection--;
                if (currentMenuSelection < 0) currentMenuSelection = menuOptions.length - 1;
            } else if (key == KeyEvent.VK_DOWN) {
                currentMenuSelection++;
                if (currentMenuSelection > menuOptions.length - 1) currentMenuSelection = 0;
            } else if (key == KeyEvent.VK_ENTER) {
                if (currentMenuSelection == 0) startGame();
                else if (currentMenuSelection == 1) gameState = GameState.HIGH_SCORES;
                else if (currentMenuSelection == 2) {
                    previousState = GameState.MENU;
                    gameState = GameState.SETTINGS;
                }
                else if (currentMenuSelection == 3) gameState = GameState.HOW_TO_PLAY;
                else if (currentMenuSelection == 4) System.exit(0);
            }
        }
        else if (gameState == GameState.PLAYING) {
            if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) leftPressed = true;
            if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) rightPressed = true;
            if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) upPressed = true;
            if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) downPressed = true;
            if (key == KeyEvent.VK_SPACE) spacePressed = true;

            if (key == KeyEvent.VK_P) gameState = GameState.PAUSED;
            if (key == KeyEvent.VK_ESCAPE) gameState = GameState.MENU;
            if (key == KeyEvent.VK_M) {
                previousState = GameState.PLAYING;
                gameState = GameState.SETTINGS;
            }
        }
        else if (gameState == GameState.PAUSED) {
            if (key == KeyEvent.VK_P) gameState = GameState.PLAYING;
            if (key == KeyEvent.VK_ESCAPE) gameState = GameState.MENU;
            if (key == KeyEvent.VK_M) {
                previousState = GameState.PAUSED;
                gameState = GameState.SETTINGS;
            }
        }
        else if (gameState == GameState.SETTINGS) {
            if (key == KeyEvent.VK_ESCAPE) gameState = previousState;
            if (key == KeyEvent.VK_M) SoundManager.toggleMusic();
            if (key == KeyEvent.VK_O) SoundManager.toggleSFX();
        }
        else if (gameState == GameState.HIGH_SCORES || gameState == GameState.HOW_TO_PLAY || gameState == GameState.GAMEOVER || gameState == GameState.WIN) {
            if (key == KeyEvent.VK_ESCAPE) gameState = GameState.MENU;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (gameState == GameState.PLAYING) {
            if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) leftPressed = false;
            if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) rightPressed = false;
            if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) upPressed = false;
            if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) downPressed = false;
            if (key == KeyEvent.VK_SPACE) spacePressed = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}