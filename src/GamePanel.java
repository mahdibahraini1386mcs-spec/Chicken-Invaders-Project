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
    private double gridSpeedX = 1.2;
    private int gridStepY = 20;
    private long lastEggTime = 0;
    private int eggInterval = 3000;
    private int bossDefeatTimer = 0;
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

        DatabaseManager.initDB();

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
        long time = System.currentTimeMillis();
        boolean blink = (time % 1000) < 500;

        if (gameState == GameState.LOGIN) {
            if (loginBgImage != null) g2d.drawImage(loginBgImage, 0, 0, getWidth(), getHeight(), null);
            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            int panelWidth = 500;
            int panelHeight = 400;
            int px = (getWidth() - panelWidth) / 2;
            int py = (getHeight() - panelHeight) / 2;
            g2d.setColor(new Color(15, 20, 40, 230));
            g2d.fillRoundRect(px, py, panelWidth, panelHeight, 30, 30);
            g2d.setColor(new Color(0, 255, 255, 150));
            g2d.setStroke(new BasicStroke(3));
            g2d.drawRoundRect(px, py, panelWidth, panelHeight, 30, 30);
            g2d.setFont(new Font("Arial", Font.BOLD, 30));
            g2d.setColor(Color.WHITE);
            String title = "PILOT IDENTIFICATION";
            g2d.drawString(title, px + (panelWidth - g2d.getFontMetrics().stringWidth(title)) / 2, py + 50);

            g2d.setFont(new Font("Monospaced", Font.BOLD, 20));
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.drawString("USERNAME:", px + 50, py + 120);
            g2d.setColor(loginSelection == 0 ? new Color(0, 255, 255, 80) : new Color(50, 50, 50, 150));
            g2d.fillRect(px + 180, py + 95, 250, 35);
            g2d.setColor(loginSelection == 0 ? Color.CYAN : Color.GRAY);
            g2d.drawRect(px + 180, py + 95, 250, 35);
            g2d.setColor(Color.WHITE);
            g2d.drawString(usernameInput + (loginSelection == 0 && blink ? "_" : ""), px + 190, py + 120);

            g2d.setColor(Color.LIGHT_GRAY);
            g2d.drawString("PASSWORD:", px + 50, py + 180);
            g2d.setColor(loginSelection == 1 ? new Color(0, 255, 255, 80) : new Color(50, 50, 50, 150));
            g2d.fillRect(px + 180, py + 155, 250, 35);
            g2d.setColor(loginSelection == 1 ? Color.CYAN : Color.GRAY);
            g2d.drawRect(px + 180, py + 155, 250, 35);
            g2d.setColor(Color.WHITE);
            String hiddenPass = "*".repeat(passwordInput.length());
            g2d.drawString(hiddenPass + (loginSelection == 1 && blink ? "_" : ""), px + 190, py + 180);

            int btnY = py + 230;
            g2d.setColor(loginSelection == 2 ? new Color(0, 255, 100, 150) : new Color(50, 100, 50, 150));
            g2d.fillRoundRect(px + 50, btnY, 180, 45, 20, 20);
            g2d.setColor(loginSelection == 2 ? Color.GREEN : Color.DARK_GRAY);
            g2d.drawRoundRect(px + 50, btnY, 180, 45, 20, 20);
            g2d.setColor(Color.WHITE);
            g2d.drawString("LOGIN", px + 105, btnY + 30);

            g2d.setColor(loginSelection == 3 ? new Color(100, 100, 255, 150) : new Color(50, 50, 100, 150));
            g2d.fillRoundRect(px + 250, btnY, 200, 45, 20, 20);
            g2d.setColor(loginSelection == 3 ? new Color(100, 150, 255) : Color.DARK_GRAY);
            g2d.drawRoundRect(px + 250, btnY, 200, 45, 20, 20);
            g2d.setColor(Color.WHITE);
            g2d.drawString("REGISTER", px + 295, btnY + 30);

            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            g2d.setColor(loginMessageColor);
            g2d.drawString(loginMessage, px + (panelWidth - g2d.getFontMetrics().stringWidth(loginMessage)) / 2, py + 340);

        } else if (gameState == GameState.MENU) {
            if (menuBgImage != null) g2d.drawImage(menuBgImage, 0, 0, getWidth(), getHeight(), null);
            g2d.setColor(new Color(0, 0, 0, 80));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            String title = "CHICKEN INVADERS";
            g2d.setFont(new Font("Arial", Font.BOLD | Font.ITALIC, 65));
            int titleWidth = g2d.getFontMetrics().stringWidth(title);
            int titleX = (getWidth() - titleWidth) / 2;
            int titleY = 150 + (int)(Math.sin(time / 300.0) * 15);
            for(int i = 8; i >= 1; i--) {
                g2d.setColor(new Color(255, 0, 0, 30));
                g2d.drawString(title, titleX - i, titleY + i);
                g2d.drawString(title, titleX + i, titleY - i);
            }
            GradientPaint titleGp = new GradientPaint(titleX, titleY - 60, Color.YELLOW, titleX, titleY, new Color(255, 69, 0));
            g2d.setPaint(titleGp);
            g2d.drawString(title, titleX, titleY);
            g2d.setColor(new Color(255, 255, 255, 100));
            Shape oldClip = g2d.getClip();
            g2d.setClip(new Rectangle(titleX, titleY - 65, titleWidth, 35));
            g2d.drawString(title, titleX, titleY);
            g2d.setClip(oldClip);

            if (currentUser != null) {
                g2d.setFont(new Font("Arial", Font.BOLD, 16));
                g2d.setColor(Color.CYAN);
                g2d.drawString("Logged in as: " + currentUser, 20, 30);
            }

            g2d.setFont(new Font("Arial", Font.BOLD, 35));
            int startY = 280;
            for (int i = 0; i < menuOptions.length; i++) {
                String text = menuOptions[i];
                int textWidth = g2d.getFontMetrics().stringWidth(text);
                int textX = (getWidth() - textWidth) / 2;
                int y = startY + (i * 60);
                if (i == currentMenuSelection) {
                    int alpha = 80 + (int)(Math.abs(Math.sin(time / 200.0)) * 80);
                    g2d.setColor(new Color(0, 255, 255, alpha));
                    g2d.fillRoundRect(textX - 50, y - 40, textWidth + 100, 55, 30, 30);
                    g2d.setColor(new Color(0, 255, 255, 200));
                    g2d.setStroke(new BasicStroke(3));
                    g2d.drawRoundRect(textX - 50, y - 40, textWidth + 100, 55, 30, 30);
                    int arrowOffset = 25 + (int)(Math.abs(Math.sin(time / 150.0)) * 15);
                    g2d.setColor(Color.YELLOW);
                    g2d.drawString(">", textX - arrowOffset - 15, y);
                    g2d.drawString("<", textX + textWidth + arrowOffset - 5, y);
                    GradientPaint optionGp = new GradientPaint(textX, y - 35, Color.WHITE, textX, y, Color.CYAN);
                    g2d.setPaint(optionGp);
                    g2d.drawString(text, textX, y);
                } else {
                    g2d.setColor(new Color(0, 0, 0, 200));
                    g2d.drawString(text, textX + 3, y + 3);
                    g2d.setColor(new Color(180, 180, 180, 200));
                    g2d.drawString(text, textX, y);
                }
            }

        } else if (gameState == GameState.HIGH_SCORES) {
            if (menuBgImage != null) g2d.drawImage(menuBgImage, 0, 0, getWidth(), getHeight(), null);
            g2d.setColor(new Color(0, 0, 0, 180));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            int panelWidth = 700, panelHeight = 450;
            int px = (getWidth() - panelWidth) / 2, py = (getHeight() - panelHeight) / 2;
            g2d.setColor(new Color(30, 20, 10, 220));
            g2d.fillRoundRect(px, py, panelWidth, panelHeight, 40, 40);
            g2d.setColor(new Color(255, 215, 0, 200));
            g2d.setStroke(new BasicStroke(4));
            g2d.drawRoundRect(px, py, panelWidth, panelHeight, 40, 40);
            String title = "🏆 HALL OF FAME 🏆";
            g2d.setFont(new Font("Arial", Font.BOLD, 40));
            g2d.setColor(Color.BLACK);
            g2d.drawString(title, (getWidth() - g2d.getFontMetrics().stringWidth(title)) / 2 + 3, py + 63);
            g2d.setColor(new Color(255, 215, 0));
            g2d.drawString(title, (getWidth() - g2d.getFontMetrics().stringWidth(title)) / 2, py + 60);

            g2d.setFont(new Font("Arial", Font.BOLD, 18));
            g2d.setColor(new Color(200, 200, 200));
            g2d.drawString("RANK", px + 40, py + 130);
            g2d.drawString("PILOT", px + 140, py + 130);
            g2d.drawString("SCORE", px + 280, py + 130);
            g2d.drawString("LEVEL", px + 410, py + 130);
            g2d.drawString("DATE", px + 520, py + 130);
            g2d.setColor(new Color(255, 215, 0, 100));
            g2d.drawLine(px + 30, py + 145, px + panelWidth - 30, py + 145);

            g2d.setFont(new Font("Arial", Font.BOLD, 20));
            String[] ranks = {"1ST", "2ND", "3RD", "4TH", "5TH"};
            Color[] colors = {new Color(255, 215, 0), new Color(192, 192, 192), new Color(205, 127, 50), Color.WHITE, Color.GRAY};

            for (int i = 0; i < 5; i++) {
                g2d.setColor(colors[i]);
                g2d.drawString(ranks[i], px + 40, py + 190 + (i * 45));
                if (i < topScores.size()) {
                    DatabaseManager.ScoreRecord sr = topScores.get(i);
                    g2d.drawString(sr.username, px + 140, py + 190 + (i * 45));
                    g2d.drawString(String.valueOf(sr.score), px + 280, py + 190 + (i * 45));
                    g2d.drawString(String.valueOf(sr.level), px + 410, py + 190 + (i * 45));
                    g2d.drawString(sr.date, px + 520, py + 190 + (i * 45));
                } else {
                    g2d.drawString("---", px + 140, py + 190 + (i * 45));
                    g2d.drawString("---", px + 280, py + 190 + (i * 45));
                    g2d.drawString("---", px + 410, py + 190 + (i * 45));
                    g2d.drawString("---", px + 520, py + 190 + (i * 45));
                }
            }

            if (blink) {
                g2d.setColor(Color.YELLOW); g2d.setFont(new Font("Arial", Font.BOLD, 20));
                String escText = "[ Press ESC to return to Menu ]";
                g2d.drawString(escText, (getWidth() - g2d.getFontMetrics().stringWidth(escText)) / 2, py + panelHeight - 30);
            }

        } else if (gameState == GameState.SETTINGS) {
            if (menuBgImage != null) g2d.drawImage(menuBgImage, 0, 0, getWidth(), getHeight(), null);
            g2d.setColor(new Color(0, 0, 0, 170));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            int panelWidth = 600, panelHeight = 500;
            int px = (getWidth() - panelWidth) / 2, py = (getHeight() - panelHeight) / 2;
            g2d.setColor(new Color(20, 10, 40, 220));
            g2d.fillRoundRect(px, py, panelWidth, panelHeight, 50, 50);
            g2d.setColor(new Color(255, 0, 255, 180));
            g2d.setStroke(new BasicStroke(4));
            g2d.drawRoundRect(px, py, panelWidth, panelHeight, 50, 50);
            g2d.setFont(new Font("Arial", Font.BOLD, 40));
            String title = "SYSTEM SETTINGS";
            g2d.setColor(Color.BLACK);
            g2d.drawString(title, (getWidth() - g2d.getFontMetrics().stringWidth(title)) / 2 + 3, py + 63);
            g2d.setColor(Color.MAGENTA);
            g2d.drawString(title, (getWidth() - g2d.getFontMetrics().stringWidth(title)) / 2, py + 60);

            boolean[] toggles = {userSettings.musicOn, userSettings.shootSfx, userSettings.hitSfx, userSettings.gameoverSfx};
            String[] labels = {"🎵  BACKGROUND MUSIC", "🔫  SHOOT SFX", "💥  HIT SFX", "☠️  GAME OVER SFX"};
            String[] keys = {"M", "S", "H", "G"};

            for (int i = 0; i < 4; i++) {
                int rowY = py + 130 + (i * 80);
                g2d.setFont(new Font("Arial", Font.BOLD, 22));
                g2d.setColor(Color.WHITE);
                g2d.drawString(labels[i], px + 40, rowY + 20);
                g2d.setFont(new Font("Arial", Font.PLAIN, 14));
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.drawString("Press '" + keys[i] + "' to toggle", px + 40, rowY + 40);

                int swX = px + panelWidth - 120;
                g2d.setColor(toggles[i] ? new Color(0, 255, 150) : new Color(80, 80, 80));
                g2d.fillRoundRect(swX, rowY, 70, 35, 35, 35);
                g2d.setColor(Color.WHITE);
                g2d.fillOval(toggles[i] ? swX + 37 : swX + 3, rowY + 3, 29, 29);
            }

            if (blink) {
                g2d.setColor(Color.YELLOW);
                g2d.setFont(new Font("Arial", Font.BOLD, 20));
                String escText = "[ Press ESC to return to Menu ]";
                g2d.drawString(escText, (getWidth() - g2d.getFontMetrics().stringWidth(escText)) / 2, py + panelHeight - 20);
            }

        } else if (gameState == GameState.HOW_TO_PLAY) {
            if (menuBgImage != null) g2d.drawImage(menuBgImage, 0, 0, getWidth(), getHeight(), null);
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            int panelWidth = 600, panelHeight = 450;
            int px = (getWidth() - panelWidth) / 2, py = (getHeight() - panelHeight) / 2;
            g2d.setColor(new Color(15, 15, 30, 220));
            g2d.fillRoundRect(px, py, panelWidth, panelHeight, 40, 40);
            g2d.setColor(Color.CYAN);
            g2d.setStroke(new BasicStroke(3));
            g2d.drawRoundRect(px, py, panelWidth, panelHeight, 40, 40);
            String title = "HOW TO PLAY";
            g2d.setFont(new Font("Arial", Font.BOLD, 45));
            g2d.setColor(Color.BLACK);
            g2d.drawString(title, (getWidth() - g2d.getFontMetrics().stringWidth(title)) / 2 + 4, py + 74);
            g2d.setColor(Color.CYAN);
            g2d.drawString(title, (getWidth() - g2d.getFontMetrics().stringWidth(title)) / 2, py + 70);
            g2d.setFont(new Font("Arial", Font.BOLD, 22));
            g2d.setColor(Color.WHITE);
            String[] instructions = {"🚀   MOVE: W / A / S / D  or  Arrows", "🔫   SHOOT: Spacebar", "⏸️   PAUSE / RESUME: P", "🎯   OBJECTIVE: Survive & Destroy!"};
            for (int i = 0; i < instructions.length; i++) g2d.drawString(instructions[i], px + 60, py + 160 + (i * 60));
            if (blink) {
                g2d.setColor(Color.YELLOW); g2d.setFont(new Font("Arial", Font.BOLD, 20));
                String escText = "[ Press ESC to return to Menu ]";
                g2d.drawString(escText, (getWidth() - g2d.getFontMetrics().stringWidth(escText)) / 2, py + panelHeight - 40);
            }

        } else if (gameState == GameState.PLAYING) {
            if (gameBgImage != null) g2d.drawImage(gameBgImage, 0, 0, getWidth(), getHeight(), null);
            GameHUD.draw(g2d, score, ScoreManager.coins, plane.getLives(), currentLevel);
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
            GameHUD.draw(g2d, score, ScoreManager.coins, plane.getLives(), currentLevel);
            plane.draw(g2d);
            for (Enemy enemy : enemies) enemy.draw(g2d);
            for (Bullet bullet : bullets) bullet.draw(g2d);
            for (Egg egg : eggs) egg.draw(g2d);
            for (PowerUp pu : powerUps) pu.draw(g2d);
            for (Explosion ex : explosions) ex.draw(g2d);
            for (BossExplosion bex : bossExplosions) bex.draw(g2d);
            if (boss != null) boss.draw(g2d);
            for (BossBullet bb : bossBullets) bb.draw(g2d);

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
                DatabaseManager.saveScore(currentUser, score, currentLevel);
                SoundManager.playSound("mixkit-retro-arcade-game-over-470.wav");
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
            if (enemy.getY() > getHeight()) enemy.setY(-40);
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
                        score += 10;
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
            score += 500;
            boss = null;
            bossDefeatTimer = 100;
        }
        if (bossDefeatTimer > 0) {
            bossDefeatTimer--;
            if (bossDefeatTimer == 0) {
                if (currentLevel == 8) {
                    gameState = GameState.WIN;
                    DatabaseManager.saveScore(currentUser, score, currentLevel);
                    SoundManager.playMusic("Chicken Invaders 2 Remastered OST - Ending Theme.wav");
                } else { currentLevel++; initLevel5(); }
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

        if (gameState == GameState.LOGIN) {
            if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_TAB) {
                loginSelection = (loginSelection + 1) % 4;
            } else if (key == KeyEvent.VK_UP) {
                loginSelection = (loginSelection - 1 + 4) % 4;
            } else if (key == KeyEvent.VK_BACK_SPACE) {
                if (loginSelection == 0 && usernameInput.length() > 0) {
                    usernameInput = usernameInput.substring(0, usernameInput.length() - 1);
                } else if (loginSelection == 1 && passwordInput.length() > 0) {
                    passwordInput = passwordInput.substring(0, passwordInput.length() - 1);
                }
            } else if (key == KeyEvent.VK_ENTER) {
                if (loginSelection == 0) loginSelection = 1;
                else if (loginSelection == 1) loginSelection = 2;
                else if (loginSelection == 2) {
                    if (usernameInput.isEmpty() || passwordInput.isEmpty()) {
                        loginMessageColor = Color.RED;
                        loginMessage = "ERROR: EMPTY FIELDS!";
                    } else if (DatabaseManager.login(usernameInput, passwordInput)) {
                        currentUser = usernameInput;
                        userSettings = DatabaseManager.getUserSettings(currentUser);
                        gameState = GameState.MENU;
                    } else {
                        loginMessageColor = Color.RED;
                        loginMessage = "ERROR: INCORRECT USERNAME OR PASSWORD!";
                    }
                } else if (loginSelection == 3) {
                    if (usernameInput.isEmpty() || passwordInput.isEmpty()) {
                        loginMessageColor = Color.RED;
                        loginMessage = "ERROR: EMPTY FIELDS!";
                    } else if (DatabaseManager.register(usernameInput, passwordInput)) {
                        loginMessageColor = Color.GREEN;
                        loginMessage = "SUCCESS: REGISTERED! YOU MAY NOW LOGIN.";
                        loginSelection = 2;
                    } else {
                        loginMessageColor = Color.RED;
                        loginMessage = "ERROR: USERNAME ALREADY EXISTS!";
                    }
                }
            }
        } else if (gameState == GameState.MENU) {
            if (key == KeyEvent.VK_UP) {
                currentMenuSelection--;
                if (currentMenuSelection < 0) currentMenuSelection = menuOptions.length - 1;
            } else if (key == KeyEvent.VK_DOWN) {
                currentMenuSelection++;
                if (currentMenuSelection > menuOptions.length - 1) currentMenuSelection = 0;
            } else if (key == KeyEvent.VK_ENTER) {
                if (currentMenuSelection == 0) startGame();
                else if (currentMenuSelection == 1) {
                    topScores = DatabaseManager.getTopScores(5);
                    gameState = GameState.HIGH_SCORES;
                }
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

            if (currentUser != null) {
                DatabaseManager.updateSettings(currentUser, userSettings);
            }

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
                if (loginSelection == 0 && usernameInput.length() < 12) {
                    usernameInput += c;
                } else if (loginSelection == 1 && passwordInput.length() < 12) {
                    passwordInput += c;
                }
            }
        }
    }
}