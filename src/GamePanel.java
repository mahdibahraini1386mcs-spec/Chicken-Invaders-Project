import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private enum GameState { LOGIN, MENU, PLAYING, PAUSED, GAMEOVER, WIN, HIGH_SCORES, SETTINGS, HOW_TO_PLAY }
    private GameState gameState = GameState.LOGIN;
    private GameState previousState = GameState.MENU;

    private String usernameInput = "";
    private String passwordInput = "";
    private int loginSelection = 0;
    private String loginMessage = "SYSTEM READY - AWAITING CREDENTIALS";
    private Color loginMessageColor = Color.YELLOW;
    public static String currentUser = null;

    private DatabaseManager.UserSettings userSettings = new DatabaseManager.UserSettings(true, true, true, true);
    private List<DatabaseManager.ScoreRecord> topScores = new ArrayList<>();

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
    private double gridSpeedX = 1.0;
    private int gridStepY = 20;
    private long lastEggTime = 0;
    private int eggInterval = 3000;
    private int bossDefeatTimer = 0;
    private long freezeEndTime = 0;
    private Random random = new Random();

    private int currentLevel = 1;
    private int score = 0;

    private Image planeImage, normalEnemyImage, fastEnemyImage, zigzagEnemyImage, shooterEnemyImage, bossImage, boss2Image;
    private Image loginBgImage, menuBgImage, gameBgImage, eggImage;
    private Image explosion1Image, explosion2Image;

    public GamePanel() {
        setFocusable(true);
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(800, 600));
        addKeyListener(this);

        DatabaseManager.initializeDatabase();

        explosions = new ArrayList<>();
        bossExplosions = new ArrayList<>();
        loadImages();
        ScoreManager.load();
        timer = new Timer(16, this);
        timer.start();
        SoundManager.playMusic(SoundManager.MAIN_THEME);
    }

    private void loadImages() {
        planeImage = ResourceManager.loadImage("airplan", "1.png");
        normalEnemyImage = ResourceManager.loadImage("chicken", "normal_chicken.png");
        fastEnemyImage = ResourceManager.loadImage("chicken", "fast_chicken.png");
        zigzagEnemyImage = ResourceManager.loadImage("chicken", "zigzag_chicken.png");
        shooterEnemyImage = ResourceManager.loadImage("chicken", "shooter_chicken.png");
        bossImage = ResourceManager.loadImage("chicken", "boss1.png");
        boss2Image = ResourceManager.loadImage("chicken", "boss2.png");
        loginBgImage = ResourceManager.loadImage("background", "morgh.png");
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
        freezeEndTime = 0;
        plane = new Plane(375, 500, planeImage, String.valueOf(ScoreManager.selectedPlane));
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

    private Enemy createEnemy(String type, int x, int y) {
        switch (type) {
            case "Fast": return new FastEnemy(x, y, fastEnemyImage);
            case "Zigzag": return new ZigzagEnemy(x, y, zigzagEnemyImage);
            case "Shooter": return new ShooterEnemy(x, y, shooterEnemyImage);
            default: return new NormalEnemy(x, y, normalEnemyImage);
        }
    }

    private int getEnemyCounter(String type, int level) {
        if (level <= 3) {
            switch(type) {
                case "Fast": return 1;
                case "Normal": case "Zigzag": case "Shooter": return 2;
            }
        } else {
            switch(type) {
                case "Fast": return 2;
                case "Normal": case "Zigzag": case "Shooter": return 3;
            }
        }
        return 1;
    }

    private void spawnGrid(String[] rowTypes) {
        gridDirection = 1;
        int startX = 80, startY = 50, hGap = 70, vGap = 50;
        for (int row = 0; row < 5; row++) {
            String type = rowTypes[row];
            for (int col = 0; col < 8; col++) {
                int x = startX + col * hGap, y = startY + row * vGap;
                Cell cell = new Cell(row, col, x, y, getEnemyCounter(type, currentLevel), type);
                grid[row][col] = cell;
                Enemy enemy = createEnemy(type, x, y);
                enemies.add(enemy);
                enemyCellMap.put(enemy, cell);
            }
        }
    }

    private void initLevel1() { gridSpeedX = 1.0; gridStepY = 20; eggInterval = 3000; spawnGrid(new String[]{"Normal", "Normal", "Normal", "Normal", "Normal"}); }
    private void initLevel2() { gridSpeedX = 1.5; gridStepY = 20; eggInterval = 2000; spawnGrid(new String[]{"Fast", "Normal", "Normal", "Normal", "Normal"}); }
    private void initLevel3() { gridSpeedX = 2.0; gridStepY = 25; eggInterval = 1500; spawnGrid(new String[]{"Zigzag", "Fast", "Normal", "Normal", "Normal"}); }
    private void initLevel4() { enemies.clear(); eggs.clear(); boss = new BossEnemy(325, 50, 150, 150, bossImage, 4, 50); }
    private void initLevel5() { gridSpeedX = 2.5; gridStepY = 25; eggInterval = 1000; spawnGrid(new String[]{"Shooter", "Shooter", "Fast", "Fast", "Normal"}); }
    private void initLevel6() { gridSpeedX = 3.0; gridStepY = 30; eggInterval = 800; spawnGrid(new String[]{"Zigzag", "Zigzag", "Shooter", "Shooter", "Normal"}); }
    private void initLevel7() { gridSpeedX = 3.5; gridStepY = 30; eggInterval = 700; spawnGrid(new String[]{"Zigzag", "Shooter", "Fast", "Normal", "Normal"}); }
    private void initLevel8() { enemies.clear(); eggs.clear(); boss = new BossEnemy(300, 50, 200, 200, boss2Image, 8, 100); }

    private void drawCustomHUD(Graphics2D g2d) {
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        g2d.setColor(Color.WHITE);
        g2d.drawString("PILOT: " + (currentUser != null ? currentUser : "GUEST"), 10, 25);
        g2d.drawString("SCORE: " + score, 10, 45);
        g2d.drawString("LEVEL: " + currentLevel, 10, 65);

        g2d.setColor(Color.RED);
        g2d.drawString("LIVES: " + "♥ ".repeat(plane.getLives()), 10, 85);

        g2d.setColor(Color.YELLOW);
        g2d.drawString("FIRE LVL: " + plane.getFireLevel(), getWidth() - 120, 25);

        String powerups = plane.getActivePowerupsText();
        if (System.currentTimeMillis() < freezeEndTime) powerups += "FREEZE ";

        if (!powerups.isEmpty()) {
            g2d.setColor(Color.CYAN);
            g2d.drawString("ACTIVE: " + powerups, getWidth() - 250, 50);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        long time = System.currentTimeMillis();
        boolean blink = (time % 1000) < 500;

        if (gameState == GameState.LOGIN) {
            if (loginBgImage != null) g2d.drawImage(loginBgImage, 0, 0, getWidth(), getHeight(), null);
            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            int panelWidth = 500, panelHeight = 400, px = (getWidth() - panelWidth) / 2, py = (getHeight() - panelHeight) / 2;
            g2d.setColor(new Color(15, 20, 40, 230)); g2d.fillRoundRect(px, py, panelWidth, panelHeight, 30, 30);
            g2d.setColor(new Color(0, 255, 255, 150)); g2d.setStroke(new BasicStroke(3)); g2d.drawRoundRect(px, py, panelWidth, panelHeight, 30, 30);
            g2d.setFont(new Font("Arial", Font.BOLD, 30)); g2d.setColor(Color.WHITE);
            g2d.drawString("PILOT IDENTIFICATION", px + 80, py + 50);
            g2d.setFont(new Font("Monospaced", Font.BOLD, 20)); g2d.setColor(Color.LIGHT_GRAY);
            g2d.drawString("USERNAME:", px + 50, py + 120);
            g2d.setColor(loginSelection == 0 ? new Color(0, 255, 255, 80) : new Color(50, 50, 50, 150)); g2d.fillRect(px + 180, py + 95, 250, 35);
            g2d.setColor(loginSelection == 0 ? Color.CYAN : Color.GRAY); g2d.drawRect(px + 180, py + 95, 250, 35);
            g2d.setColor(Color.WHITE); g2d.drawString(usernameInput + (loginSelection == 0 && blink ? "_" : ""), px + 190, py + 120);
            g2d.setColor(Color.LIGHT_GRAY); g2d.drawString("PASSWORD:", px + 50, py + 180);
            g2d.setColor(loginSelection == 1 ? new Color(0, 255, 255, 80) : new Color(50, 50, 50, 150)); g2d.fillRect(px + 180, py + 155, 250, 35);
            g2d.setColor(loginSelection == 1 ? Color.CYAN : Color.GRAY); g2d.drawRect(px + 180, py + 155, 250, 35);
            g2d.setColor(Color.WHITE); g2d.drawString("*".repeat(passwordInput.length()) + (loginSelection == 1 && blink ? "_" : ""), px + 190, py + 180);
            int btnY = py + 230;
            g2d.setColor(loginSelection == 2 ? new Color(0, 255, 100, 150) : new Color(50, 100, 50, 150)); g2d.fillRoundRect(px + 50, btnY, 180, 45, 20, 20);
            g2d.setColor(loginSelection == 2 ? Color.GREEN : Color.DARK_GRAY); g2d.drawRoundRect(px + 50, btnY, 180, 45, 20, 20);
            g2d.setColor(Color.WHITE); g2d.drawString("LOGIN", px + 105, btnY + 30);
            g2d.setColor(loginSelection == 3 ? new Color(100, 100, 255, 150) : new Color(50, 50, 100, 150)); g2d.fillRoundRect(px + 250, btnY, 200, 45, 20, 20);
            g2d.setColor(loginSelection == 3 ? new Color(100, 150, 255) : Color.DARK_GRAY); g2d.drawRoundRect(px + 250, btnY, 200, 45, 20, 20);
            g2d.setColor(Color.WHITE); g2d.drawString("REGISTER", px + 295, btnY + 30);
            g2d.setFont(new Font("Arial", Font.BOLD, 16)); g2d.setColor(loginMessageColor);
            g2d.drawString(loginMessage, px + (panelWidth - g2d.getFontMetrics().stringWidth(loginMessage)) / 2, py + 340);
        } else if (gameState == GameState.MENU) {
            if (menuBgImage != null) g2d.drawImage(menuBgImage, 0, 0, getWidth(), getHeight(), null);
            g2d.setColor(new Color(0, 0, 0, 80)); g2d.fillRect(0, 0, getWidth(), getHeight());
            String title = "CHICKEN INVADERS";
            g2d.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 65));
            int titleX = (getWidth() - g2d.getFontMetrics().stringWidth(title)) / 2, titleY = 150;
            g2d.setColor(Color.YELLOW); g2d.drawString(title, titleX, titleY);
            if (currentUser != null) { g2d.setFont(new Font("Arial", Font.BOLD, 16)); g2d.setColor(Color.CYAN); g2d.drawString("Logged in as: " + currentUser, 20, 30); }
            g2d.setFont(new Font("Arial", Font.BOLD, 35));
            int startY = 280;
            for (int i = 0; i < menuOptions.length; i++) {
                int textX = (getWidth() - g2d.getFontMetrics().stringWidth(menuOptions[i])) / 2, y = startY + (i * 60);
                if (i == currentMenuSelection) {
                    g2d.setColor(Color.CYAN); g2d.drawString(menuOptions[i], textX, y);
                } else {
                    g2d.setColor(Color.LIGHT_GRAY); g2d.drawString(menuOptions[i], textX, y);
                }
            }
        } else if (gameState == GameState.HIGH_SCORES) {
            if (menuBgImage != null) g2d.drawImage(menuBgImage, 0, 0, getWidth(), getHeight(), null);
            g2d.setColor(new Color(0, 0, 0, 180)); g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.setFont(new Font("Arial", Font.BOLD, 40)); g2d.setColor(Color.YELLOW); g2d.drawString("🏆 HALL OF FAME 🏆", 200, 100);
            g2d.setFont(new Font("Arial", Font.BOLD, 20));
            for (int i = 0; i < topScores.size(); i++) {
                DatabaseManager.ScoreRecord sr = topScores.get(i);
                g2d.drawString((i+1) + ". " + sr.username + " - " + sr.score + " (Lvl " + sr.level + ")", 250, 180 + (i * 45));
            }
            g2d.drawString("[ ESC to Menu ]", 320, 450);
        } else if (gameState == GameState.SETTINGS) {
            if (menuBgImage != null) g2d.drawImage(menuBgImage, 0, 0, getWidth(), getHeight(), null);
            g2d.setColor(new Color(0, 0, 0, 180)); g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.setFont(new Font("Arial", Font.BOLD, 40)); g2d.setColor(Color.MAGENTA); g2d.drawString("SETTINGS", 300, 100);
            g2d.setFont(new Font("Arial", Font.BOLD, 22)); g2d.setColor(Color.WHITE);
            g2d.drawString("🎵 MUSIC (M): " + (userSettings.musicOn ? "ON" : "OFF"), 250, 200);
            g2d.drawString("🔫 SHOOT SFX (S): " + (userSettings.shootSfx ? "ON" : "OFF"), 250, 250);
            g2d.drawString("💥 HIT SFX (H): " + (userSettings.hitSfx ? "ON" : "OFF"), 250, 300);
            g2d.drawString("☠️ GAMEOVER SFX (G): " + (userSettings.gameoverSfx ? "ON" : "OFF"), 250, 350);
            g2d.drawString("[ ESC to Return ]", 320, 450);
        } else if (gameState == GameState.HOW_TO_PLAY) {
            if (menuBgImage != null) g2d.drawImage(menuBgImage, 0, 0, getWidth(), getHeight(), null);
            g2d.setColor(new Color(0, 0, 0, 180)); g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.setFont(new Font("Arial", Font.BOLD, 40)); g2d.setColor(Color.CYAN); g2d.drawString("HOW TO PLAY", 250, 100);
            g2d.setFont(new Font("Arial", Font.BOLD, 22)); g2d.setColor(Color.WHITE);
            g2d.drawString("🚀 MOVE: W/A/S/D or Arrows", 250, 200);
            g2d.drawString("🔫 SHOOT: Spacebar", 250, 250);
            g2d.drawString("⏸️ PAUSE: P", 250, 300);
            g2d.drawString("[ ESC to Menu ]", 320, 450);
        } else if (gameState == GameState.PLAYING) {
            if (gameBgImage != null) g2d.drawImage(gameBgImage, 0, 0, getWidth(), getHeight(), null);
            drawCustomHUD(g2d);
            plane.draw(g2d);
            for (Enemy enemy : enemies) enemy.draw(g2d);
            for (Bullet bullet : bullets) bullet.draw(g2d);
            for (Egg egg : eggs) egg.draw(g2d);
            for (PowerUp pu : powerUps) pu.draw(g2d);
            for (Explosion ex : explosions) ex.draw(g2d);
            for (BossExplosion bex : bossExplosions) bex.draw(g2d);
            if (boss != null) boss.draw(g2d);
            for (BossBullet bb : bossBullets) bb.draw(g2d);
        } else if (gameState == GameState.PAUSED) {
            if (gameBgImage != null) g2d.drawImage(gameBgImage, 0, 0, getWidth(), getHeight(), null);
            drawCustomHUD(g2d);
            plane.draw(g2d);
            for (Enemy enemy : enemies) enemy.draw(g2d);
            g2d.setColor(new Color(0, 0, 0, 150)); g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.setColor(Color.YELLOW); g2d.setFont(new Font("Arial", Font.BOLD, 50)); g2d.drawString("PAUSED", 300, 300);
        } else if (gameState == GameState.GAMEOVER) {
            g2d.setColor(Color.RED); g2d.setFont(new Font("Arial", Font.BOLD, 50)); g2d.drawString("GAME OVER", 250, 300);
            g2d.setFont(new Font("Arial", Font.BOLD, 20)); g2d.drawString("[ ESC to Menu ]", 330, 350);
        } else if (gameState == GameState.WIN) {
            g2d.setColor(Color.GREEN); g2d.setFont(new Font("Arial", Font.BOLD, 50)); g2d.drawString("شما برنده شدید!", 250, 300);
            g2d.setFont(new Font("Arial", Font.BOLD, 20)); g2d.drawString("[ ESC to Menu ]", 330, 350);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameState == GameState.PLAYING) {
            plane.updatePowerUps();
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
                DatabaseManager.saveScore(currentUser, score, currentLevel, userSettings);
                if (userSettings.gameoverSfx) {
                    SoundManager.stopMusic();
                    SoundManager.playSound(SoundManager.GAME_OVER);
                }
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
            if (userSettings.shootSfx) SoundManager.playSound(SoundManager.LASER);
        }
    }

    private void updateBullets() {
        Iterator<Bullet> iter = bullets.iterator();
        while (iter.hasNext()) {
            Bullet b = iter.next(); b.move();
            if (b.getY() < 0) iter.remove();
        }
    }

    private void updatePowerUps() {
        Iterator<PowerUp> iter = powerUps.iterator();
        while (iter.hasNext()) {
            PowerUp p = iter.next();
            p.move();
            if (p.getBounds().intersects(plane.getBounds())) {
                if (p.getType().equals("AddFire")) plane.addFire();
                else if (p.getType().equals("RapidFire")) plane.activateRapidFire();
                else if (p.getType().equals("ExtraLife")) plane.addLife();
                else if (p.getType().equals("Shield")) plane.activateShield();
                else if (p.getType().equals("FreezeBomb")) freezeEndTime = System.currentTimeMillis() + 3000;
                iter.remove();
            } else if (p.getY() > getHeight()) iter.remove();
        }
    }

    private void updateExplosions() {
        Iterator<Explosion> iter = explosions.iterator();
        while (iter.hasNext()) { if (!iter.next().update()) iter.remove(); }
    }

    private void updateBossExplosions() {
        Iterator<BossExplosion> iter = bossExplosions.iterator();
        while (iter.hasNext()) { if (!iter.next().update()) iter.remove(); }
    }

    private void updateEnemies() {
        if (enemies.isEmpty()) return;
        if (System.currentTimeMillis() < freezeEndTime) return;

        boolean hitEdge = false;
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 8; c++) {
                Cell cell = grid[r][c];
                if (cell != null && cell.getCounter() > 0) {
                    if (cell.getX() >= getWidth() - 40 && gridDirection == 1) hitEdge = true;
                    if (cell.getX() <= 0 && gridDirection == -1) hitEdge = true;
                }
            }
        }
        if (hitEdge) {
            gridDirection *= -1;
            for (int r = 0; r < 5; r++) {
                for (int c = 0; c < 8; c++) if (grid[r][c] != null) grid[r][c].setY(grid[r][c].getY() + gridStepY);
            }
        } else {
            for (int r = 0; r < 5; r++) {
                for (int c = 0; c < 8; c++) if (grid[r][c] != null) grid[r][c].setX(grid[r][c].getX() + (int)(gridSpeedX * gridDirection));
            }
        }

        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 8; c++) {
                if (grid[r][c] != null && grid[r][c].getY() > getHeight() + 50) {
                    grid[r][c].setY(-50);
                }
            }
        }

        for (Enemy enemy : enemies) {
            Cell cell = enemyCellMap.get(enemy);
            if (cell != null) {
                if (enemy.isSpawning()) enemy.moveToTarget(cell.getX(), cell.getY());
                else {
                    enemy.setX(cell.getX());
                    enemy.setY(cell.getY());
                }
            }
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastEggTime > eggInterval && !enemies.isEmpty()) {
            Enemy randomEnemy = enemies.get(random.nextInt(enemies.size()));
            if (!randomEnemy.isSpawning()) {
                eggs.add(new Egg(randomEnemy.getX() + 20, randomEnemy.getY() + 40, 0, 4, eggImage));
                if (randomEnemy.getClass().getSimpleName().equals("ShooterEnemy")) {
                    int dx = plane.getX() - randomEnemy.getX(), dy = plane.getY() - randomEnemy.getY();
                    double dist = Math.sqrt(dx*dx + dy*dy);
                    if(dist > 0) eggs.add(new Egg(randomEnemy.getX() + 20, randomEnemy.getY() + 40, (int)((dx/dist)*5), (int)((dy/dist)*5), eggImage));
                }
            }
            lastEggTime = currentTime;
        }
    }

    private void updateEggs() {
        if (System.currentTimeMillis() < freezeEndTime) return;
        Iterator<Egg> iter = eggs.iterator();
        while (iter.hasNext()) {
            Egg egg = iter.next(); egg.move();
            if (egg.getY() > getHeight() || egg.getX() < 0 || egg.getX() > getWidth()) iter.remove();
        }
    }

    private void updateBoss() {
        if (boss == null) return;
        boss.move();
        if (boss.canShoot(currentLevel == 8 ? 1000 : 1500)) {
            int bx = boss.getX() + boss.getWidth() / 2, by = boss.getY() + boss.getHeight() - 20;
            if (currentLevel == 4) {
                bossBullets.add(new BossBullet(bx, by, 0, 4, eggImage)); bossBullets.add(new BossBullet(bx, by, -4, 0, eggImage));
                bossBullets.add(new BossBullet(bx, by, 4, 0, eggImage)); bossBullets.add(new BossBullet(bx, by, 0, -4, eggImage));
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
            BossBullet b = iter.next(); b.move();
            if (b.getY() > getHeight() || b.getX() < 0 || b.getX() > getWidth()) iter.remove();
        }
    }

    private void checkCollisions() {
        Iterator<Bullet> bulletIter = bullets.iterator();
        ArrayList<Enemy> newSpawns = new ArrayList<>();
        while (bulletIter.hasNext()) {
            Bullet b = bulletIter.next();
            Rectangle bBounds = b.getBounds();
            boolean bulletRemoved = false;

            if (boss != null && bBounds.intersects(boss.getBounds())) {
                boss.takeDamage(1 * plane.getDamageMultiplier());
                bulletIter.remove(); bulletRemoved = true;
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

                        int pts = 10;
                        if (e instanceof FastEnemy) pts = 15;
                        else if (e instanceof ZigzagEnemy) pts = 20;
                        else if (e instanceof ShooterEnemy) pts = 25;
                        score += pts;

                        explosions.add(new Explosion(e.getX(), e.getY(), explosion1Image));
                        if (userSettings.hitSfx) SoundManager.playSound(SoundManager.EXPLOSION);

                        if (random.nextDouble() < 0.20) {
                            String[] types = {"AddFire", "RapidFire", "ExtraLife", "Shield", "FreezeBomb"};
                            powerUps.add(new PowerUp(e.getX(), e.getY(), types[random.nextInt(types.length)]));
                        }

                        if (cell.getCounter() > 0) {
                            Enemy ne = createEnemy(cell.getEnemyType(), (random.nextBoolean()) ? -50 : getWidth() + 50, -50);
                            ne.setSpawning(true);
                            newSpawns.add(ne);
                            enemyCellMap.put(ne, cell);
                        }
                        break;
                    }
                }
            }
        }
        enemies.addAll(newSpawns);

        Iterator<Enemy> kamikazeIter = enemies.iterator();
        while (kamikazeIter.hasNext()) {
            Enemy e = kamikazeIter.next();
            if (plane.getBounds().intersects(e.getBounds())) {
                plane.takeDamage();
                explosions.add(new Explosion(plane.getX(), plane.getY(), explosion1Image));
                explosions.add(new Explosion(e.getX(), e.getY(), explosion1Image));
                if (userSettings.hitSfx) SoundManager.playSound(SoundManager.EXPLOSION);

                Cell cell = enemyCellMap.get(e);
                if(cell != null) cell.decreaseCounter();
                kamikazeIter.remove();
                enemyCellMap.remove(e);
            }
        }

        Iterator<Egg> eggIter = eggs.iterator();
        while (eggIter.hasNext()) {
            Egg egg = eggIter.next();
            if (plane.getBounds().intersects(egg.getBounds())) {
                plane.takeDamage();
                explosions.add(new Explosion(plane.getX(), plane.getY(), explosion1Image));
                eggIter.remove();
                if (userSettings.hitSfx) SoundManager.playSound(SoundManager.EXPLOSION);
            }
        }

        Iterator<BossBullet> bbIter = bossBullets.iterator();
        while (bbIter.hasNext()) {
            BossBullet bb = bbIter.next();
            if (plane.getBounds().intersects(bb.getBounds())) {
                plane.takeDamage();
                explosions.add(new Explosion(plane.getX(), plane.getY(), explosion1Image));
                bbIter.remove();
                if (userSettings.hitSfx) SoundManager.playSound(SoundManager.EXPLOSION);
            }
        }
    }

    private void checkLevelUp() {
        if (boss != null && boss.getHealth() <= 0) {
            bossExplosions.add(new BossExplosion(boss.getX(), boss.getY(), boss.getWidth(), boss.getHeight(), explosion1Image, explosion2Image));
            if (userSettings.hitSfx) SoundManager.playSound(SoundManager.EXPLOSION);
            score += (currentLevel == 4) ? 500 : 1000;
            boss = null;
            bossDefeatTimer = 100;
        }
        if (bossDefeatTimer > 0) {
            bossDefeatTimer--;
            if (bossDefeatTimer == 0) {
                if (currentLevel == 8) {
                    gameState = GameState.WIN;
                    DatabaseManager.saveScore(currentUser, score, currentLevel, userSettings);
                    SoundManager.stopMusic();
                    SoundManager.playMusic(SoundManager.ENDING_THEME);
                } else { score += 200; currentLevel++; initLevel5(); }
            }
        } else if (enemies.isEmpty() && boss == null && gameState == GameState.PLAYING) {
            score += 200;
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

        if (gameState == GameState.LOGIN) {
            if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_TAB) loginSelection = (loginSelection + 1) % 4;
            else if (key == KeyEvent.VK_UP) loginSelection = (loginSelection - 1 + 4) % 4;
            else if (key == KeyEvent.VK_BACK_SPACE) {
                if (loginSelection == 0 && usernameInput.length() > 0) usernameInput = usernameInput.substring(0, usernameInput.length() - 1);
                else if (loginSelection == 1 && passwordInput.length() > 0) passwordInput = passwordInput.substring(0, passwordInput.length() - 1);
            } else if (key == KeyEvent.VK_ENTER) {
                if (loginSelection == 0) loginSelection = 1;
                else if (loginSelection == 1) loginSelection = 2;
                else if (loginSelection == 2) {
                    if (usernameInput.isEmpty() || passwordInput.isEmpty()) { loginMessageColor = Color.RED; loginMessage = "ERROR: EMPTY FIELDS!"; }
                    else if (DatabaseManager.login(usernameInput, passwordInput)) { currentUser = usernameInput; userSettings = DatabaseManager.getUserSettings(currentUser); gameState = GameState.MENU; }
                    else { loginMessageColor = Color.RED; loginMessage = "ERROR: INCORRECT USERNAME OR PASSWORD!"; }
                } else if (loginSelection == 3) {
                    if (usernameInput.isEmpty() || passwordInput.isEmpty()) { loginMessageColor = Color.RED; loginMessage = "ERROR: EMPTY FIELDS!"; }
                    else if (DatabaseManager.register(usernameInput, passwordInput)) { loginMessageColor = Color.GREEN; loginMessage = "SUCCESS: REGISTERED! YOU MAY NOW LOGIN."; loginSelection = 2; }
                    else { loginMessageColor = Color.RED; loginMessage = "ERROR: USERNAME ALREADY EXISTS!"; }
                }
            }
        } else if (gameState == GameState.MENU) {
            if (key == KeyEvent.VK_UP) { currentMenuSelection--; if (currentMenuSelection < 0) currentMenuSelection = menuOptions.length - 1; }
            else if (key == KeyEvent.VK_DOWN) { currentMenuSelection++; if (currentMenuSelection > menuOptions.length - 1) currentMenuSelection = 0; }
            else if (key == KeyEvent.VK_ENTER) {
                if (currentMenuSelection == 0) startGame();
                else if (currentMenuSelection == 1) { topScores = DatabaseManager.getTopScores(5); gameState = GameState.HIGH_SCORES; }
                else if (currentMenuSelection == 2) { previousState = GameState.MENU; gameState = GameState.SETTINGS; }
                else if (currentMenuSelection == 3) gameState = GameState.HOW_TO_PLAY;
                else if (currentMenuSelection == 4) System.exit(0);
            }
        } else if (gameState == GameState.PLAYING) {
            if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) leftPressed = true;
            if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) rightPressed = true;
            if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) upPressed = true;
            if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) downPressed = true;
            if (key == KeyEvent.VK_SPACE) spacePressed = true;
            if (key == KeyEvent.VK_P) gameState = GameState.PAUSED;
            if (key == KeyEvent.VK_ESCAPE) gameState = GameState.MENU;
            if (key == KeyEvent.VK_M) { previousState = GameState.PLAYING; gameState = GameState.SETTINGS; }
        } else if (gameState == GameState.PAUSED) {
            if (key == KeyEvent.VK_P) gameState = GameState.PLAYING;
            if (key == KeyEvent.VK_ESCAPE) gameState = GameState.MENU;
            if (key == KeyEvent.VK_M) { previousState = GameState.PAUSED; gameState = GameState.SETTINGS; }
        } else if (gameState == GameState.SETTINGS) {
            if (key == KeyEvent.VK_ESCAPE) gameState = previousState;
            if (key == KeyEvent.VK_M) userSettings.musicOn = !userSettings.musicOn;
            if (key == KeyEvent.VK_S) userSettings.shootSfx = !userSettings.shootSfx;
            if (key == KeyEvent.VK_H) userSettings.hitSfx = !userSettings.hitSfx;
            if (key == KeyEvent.VK_G) userSettings.gameoverSfx = !userSettings.gameoverSfx;
            if (currentUser != null) DatabaseManager.updateSettings(currentUser, userSettings);
        } else if (gameState == GameState.HIGH_SCORES || gameState == GameState.HOW_TO_PLAY || gameState == GameState.GAMEOVER || gameState == GameState.WIN) {
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
    public void keyTyped(KeyEvent e) {
        if (gameState == GameState.LOGIN) {
            char c = e.getKeyChar();
            if (Character.isLetterOrDigit(c)) {
                if (loginSelection == 0 && usernameInput.length() < 12) usernameInput += c;
                else if (loginSelection == 1 && passwordInput.length() < 12) passwordInput += c;
            }
        }
    }
}