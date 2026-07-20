import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Arrays;

public class GamePanel extends JPanel implements ActionListener, KeyListener {

    private enum GameState { LOGIN, MENU, STORE, PLAYING, PAUSED, GAMEOVER, WIN, HIGH_SCORES, SETTINGS, HOW_TO_PLAY }

    // ===== تنظیم صفحه پیش‌فرض روی منوی اصلی =====
    private GameState gameState = GameState.MENU;
    private GameState previousState = GameState.MENU;

    private String usernameInput = "";
    private String passwordInput = "";
    private int loginSelection = 0;
    private String loginMessage = "SYSTEM READY - AWAITING CREDENTIALS";
    private Color loginMessageColor = Color.YELLOW;
    public static String currentUser = null;

    // مختصات ثابت پنل لاگین
    private static final int LOGIN_PANEL_WIDTH = 500;
    private static final int LOGIN_PANEL_HEIGHT = 400;

    private DatabaseManager.UserSettings userSettings = new DatabaseManager.UserSettings(true, true, true, true);
    private List<DatabaseManager.ScoreRecord> topScores = new ArrayList<>();

    private String[] menuOptions = {"New Game", "Store", "High Scores", "Settings", "How to Play", "Exit"};
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
    private Image addFireImg, rapidFireImg, extraLifeImg, shieldImg, freezeBombImg;

    // متغیرهای فروشگاه
    private Image[] storeImages = new Image[4];
    private String[] planeNames = {"Default", "Fast", "Heavy", "Sniper"};
    private int[] planeCosts = {0, 5000, 8000, 10000};
    private int[] planeSpeeds = {5, 7, 4, 5};
    private int[] planeFireRates = {300, 250, 200, 150};
    private int[] planeLives = {3, 3, 5, 3};
    private String[] planeSpecs = {"Standard Issue", "High Speed Engine", "Titanium Armor", "Double Boss Damage"};
    private int storeSelection = 0;
    private String activePlane = "Default";
    private String storeMessage = "";
    private long storeMessageTime = 0;

    // ===== منوی تایید خروج سفارشی =====
    private boolean showExitConfirm = false;
    private Rectangle btnYes = new Rectangle(250, 320, 140, 50);
    private Rectangle btnNo = new Rectangle(410, 320, 140, 50);
    private int hoverButton = -1; // 0=Yes, 1=No

    // ===== متغیرهای پیام خطای سایبرپانکی برای ورود به استور =====
    private boolean showStoreLoginError = false;
    private Rectangle btnOkError = new Rectangle(320, 350, 160, 50);
    private boolean hoverOkError = false;

    // Game Over افکت‌ها
    private ArrayList<GameOverParticle> gameOverParticles = new ArrayList<>();
    private long gameOverStartTime = 0;
    private double displayedScore = 0;

    // Win افکت‌ها
    private ArrayList<ConfettiParticle> confettiParticles = new ArrayList<>();
    private ArrayList<FireworkParticle> fireworkParticles = new ArrayList<>();
    private long winStartTime = 0;
    private long lastFireworkSpawn = 0;
    private double winDisplayedScore = 0;
    private double trophyRotation = 0;

    public GamePanel() {
        setFocusable(true);
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(800, 600));
        addKeyListener(this);
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e.getX(), e.getY());
            }
        });

        DatabaseManager.initializeDatabase();

        explosions = new ArrayList<>();
        bossExplosions = new ArrayList<>();
        loadImages();
        ScoreManager.load();
        timer = new Timer(16, this);
        timer.start();
        if (userSettings.musicOn) SoundManager.playMusic(SoundManager.MAIN_THEME);

        // شنونده‌های موس برای منوی خروج و ارور استور
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (showExitConfirm) {
                    if (btnYes.contains(e.getPoint())) {
                        showExitConfirm = false;
                        DatabaseManager.saveScore(currentUser, score, currentLevel, userSettings);
                        gameState = GameState.MENU;
                    } else if (btnNo.contains(e.getPoint())) {
                        showExitConfirm = false;
                    }
                }

                // 🔴 این بخش جدید برای کلیک روی دکمه OK ارور استور اضافه شود:
                if (showStoreLoginError && gameState == GameState.MENU) {
                    if (btnOkError.contains(e.getPoint())) {
                        showStoreLoginError = false; // بستن پنجره ارور
                    }
                }
            }
        });

        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (showExitConfirm) {
                    if (btnYes.contains(e.getPoint())) hoverButton = 0;
                    else if (btnNo.contains(e.getPoint())) hoverButton = 1;
                    else hoverButton = -1;
                }

                // 🔴 این بخش جدید برای افکت Hover دکمه OK اضافه شود:
                if (showStoreLoginError && gameState == GameState.MENU) {
                    hoverOkError = btnOkError.contains(e.getPoint());
                }
            }
        });
    }

    private void loadImages() {
        storeImages[0] = ResourceManager.loadImage("airplan", "1.png");
        storeImages[1] = ResourceManager.loadImage("airplan", "2.png");
        storeImages[2] = ResourceManager.loadImage("airplan", "3.png");
        storeImages[3] = ResourceManager.loadImage("airplan", "4.png");
        planeImage = storeImages[0];

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
        addFireImg = ResourceManager.loadImage("powerup1", "add_shot.png");
        rapidFireImg = ResourceManager.loadImage("powerup1", "fast_shot.png");
        extraLifeImg = ResourceManager.loadImage("powerup1", "heal.png");
        shieldImg = ResourceManager.loadImage("powerup1", "sheild.png");
        freezeBombImg = ResourceManager.loadImage("powerup1", "freeze.png");
    }

    private void startGame() {
        score = 0;
        currentLevel = 1;
        bossDefeatTimer = 0;
        freezeEndTime = 0;

        int planeIndex = Arrays.asList(planeNames).indexOf(activePlane);
        if (planeIndex < 0) planeIndex = 0;
        planeImage = storeImages[planeIndex];

        plane = new Plane(375, 500, planeImage, activePlane);
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
    private void initLevel3() { gridSpeedX = 2.0; gridStepY = 25; eggInterval = 1500; spawnGrid(new String[]{"Zigzag", "Zigzag", "Normal", "Normal", "Normal"}); }
    private void initLevel4() { enemies.clear(); eggs.clear(); boss = new BossEnemy(325, 50, 150, 150, bossImage, 4, 50); }
    private void initLevel5() { gridSpeedX = 2.5; gridStepY = 25; eggInterval = 1000; spawnGrid(new String[]{"Shooter", "Shooter", "Fast", "Fast", "Normal"}); }
    private void initLevel6() { gridSpeedX = 3.0; gridStepY = 30; eggInterval = 800; spawnGrid(new String[]{"Zigzag", "Zigzag", "Shooter", "Shooter", "Normal"}); }
    private void initLevel7() { gridSpeedX = 3.5; gridStepY = 30; eggInterval = 700; spawnGrid(new String[]{"Zigzag", "Shooter", "Fast", "Normal", "Normal"}); }
    private void initLevel8() { enemies.clear(); eggs.clear(); boss = new BossEnemy(300, 50, 200, 200, boss2Image, 8, 100); }

    private void drawCustomHUD(Graphics2D g2d) {
        long time = System.currentTimeMillis();

        int leftX = 15, leftY = 15;

        g2d.setFont(new Font("Consolas", Font.BOLD, 18));

        g2d.setColor(new Color(150, 200, 255));
        g2d.drawString("PILOT:", leftX, leftY + 15);
        g2d.setColor(Color.CYAN);
        g2d.drawString(currentUser != null ? currentUser.toUpperCase() : "GUEST", leftX + 70, leftY + 15);

        g2d.setColor(new Color(150, 200, 255));
        g2d.drawString("SCORE:", leftX, leftY + 40);
        g2d.setColor(Color.YELLOW);
        g2d.drawString(String.format("%,d", score), leftX + 70, leftY + 40);

        g2d.setColor(new Color(150, 200, 255));
        g2d.drawString("LEVEL:", leftX, leftY + 65);
        g2d.setColor(new Color(255, 100, 255));
        g2d.drawString(String.valueOf(currentLevel), leftX + 70, leftY + 65);

        g2d.setColor(new Color(150, 200, 255));
        g2d.drawString("HULL:", leftX, leftY + 90);
        for(int i = 0; i < plane.getLives(); i++) {
            g2d.setColor(new Color(255, 0, 50));
            g2d.fillOval(leftX + 70 + (i * 22), leftY + 75, 16, 16);
            g2d.setColor(new Color(255, 255, 255, 200));
            g2d.fillOval(leftX + 73 + (i * 22), leftY + 78, 5, 5);
        }

        int rightX = getWidth() - 260;
        int rightY = 0;

        g2d.setFont(new Font("Consolas", Font.BOLD, 16));
        g2d.setColor(new Color(255, 200, 100));
        g2d.drawString("WEAPON LVL:", rightX + 15, rightY + 30);

        int fLvl = plane.getFireLevel();
        for(int i = 0; i < 5; i++) {
            if(i < fLvl) g2d.setColor(new Color(255, 100, 0));
            else g2d.setColor(new Color(40, 20, 10));

            g2d.fillRect(rightX + 130 + (i * 25), rightY + 16, 20, 16);
            g2d.setColor(new Color(255, 200, 0, 80));
            g2d.drawRect(rightX + 130 + (i * 25), rightY + 16, 20, 16);
        }

        g2d.setColor(new Color(150, 255, 150));
        g2d.drawString("SYSTEMS   :", rightX + 15, rightY + 70);

        String activeText = plane.getActivePowerupsText();
        boolean hasFreeze = System.currentTimeMillis() < freezeEndTime;

        int iconX = rightX + 130;
        int iconY = rightY + 50;

        if (activeText.isEmpty() && !hasFreeze) {
            g2d.setColor(Color.GRAY);
            g2d.drawString("STANDBY", iconX, rightY + 70);
        } else {
            if (activeText.contains("RAPID")) {
                g2d.setColor(new Color(255, 255, 255, 30)); g2d.fillRoundRect(iconX - 2, iconY - 2, 34, 34, 8, 8);
                if (rapidFireImg != null) g2d.drawImage(rapidFireImg, iconX, iconY, 30, 30, null);
                iconX += 45;
            }
            if (activeText.contains("SHIELD")) {
                g2d.setColor(new Color(255, 255, 255, 30)); g2d.fillRoundRect(iconX - 2, iconY - 2, 34, 34, 8, 8);
                if (shieldImg != null) g2d.drawImage(shieldImg, iconX, iconY, 30, 30, null);
                iconX += 45;
            }
            if (hasFreeze) {
                g2d.setColor(new Color(255, 255, 255, 30)); g2d.fillRoundRect(iconX - 2, iconY - 2, 34, 34, 8, 8);
                if (freezeBombImg != null) g2d.drawImage(freezeBombImg, iconX, iconY, 30, 30, null);
                iconX += 45;
            }
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
            int panelWidth = LOGIN_PANEL_WIDTH, panelHeight = LOGIN_PANEL_HEIGHT, px = (getWidth() - panelWidth) / 2, py = (getHeight() - panelHeight) / 2;
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
            g2d.setColor(new Color(5, 5, 20, 150)); g2d.fillRect(0, 0, getWidth(), getHeight());
            if (currentUser != null) { g2d.setFont(new Font("Consolas", Font.BOLD, 16)); g2d.setColor(new Color(0, 255, 255, 220)); g2d.drawString("⚡ PILOT ACTIVE: [" + currentUser.toUpperCase() + "] ⚡", 20, 30); }
            double pulse = (Math.sin(time / 150.0) + 1) / 2.0; double floatY = Math.sin(time / 400.0) * 12;
            String title = "CHICKEN INVADERS";
            g2d.setFont(new Font("Impact", Font.ITALIC, 75));
            FontMetrics fmTitle = g2d.getFontMetrics();
            int titleX = (getWidth() - fmTitle.stringWidth(title)) / 2;
            int titleY = 140 + (int)floatY;
            g2d.setColor(new Color(255, 80, 0, 100)); g2d.drawString(title, titleX + 4, titleY + 4);
            g2d.setColor(new Color(255, 0, 0, 150)); g2d.drawString(title, titleX - 2, titleY - 2);
            GradientPaint titleGrad = new GradientPaint(titleX, titleY - 70, Color.YELLOW, titleX, titleY, new Color(255, 140, 0));
            g2d.setPaint(titleGrad);
            g2d.drawString(title, titleX, titleY);

            int startY = 220;
            int gap = 60;
            int btnWidth = 360;
            int btnHeight = 55;

            for (int i = 0; i < menuOptions.length; i++) {
                int bx = (getWidth() - btnWidth) / 2, by = startY + (i * gap);
                boolean isSelected = (i == currentMenuSelection);
                String icon = "";
                switch (menuOptions[i]) {
                    case "New Game": icon = "🚀 "; break;
                    case "Store": icon = "🛒 "; break;
                    case "High Scores": icon = "🏆 "; break;
                    case "Settings": icon = "⚙️ "; break;
                    case "How to Play": icon = "📖 "; break;
                    case "Exit": icon = "❌ "; break;
                }
                String displayText = icon + menuOptions[i];
                if (isSelected) {
                    g2d.setColor(new Color(0, 255, 255, 40 + (int)(pulse * 60)));
                    g2d.fillRoundRect(bx - 8, by - 5, btnWidth + 16, btnHeight + 10, 30, 30);
                    GradientPaint btnGrad = new GradientPaint(bx, by, new Color(0, 150, 255, 200), bx, by + btnHeight, new Color(0, 50, 150, 200));
                    g2d.setPaint(btnGrad);
                    g2d.fillRoundRect(bx, by, btnWidth, btnHeight, 25, 25);
                    g2d.setColor(Color.CYAN); g2d.setStroke(new BasicStroke(3.0f)); g2d.drawRoundRect(bx, by, btnWidth, btnHeight, 25, 25);
                    g2d.setFont(new Font("Arial", Font.BOLD, 30));
                } else {
                    g2d.setColor(new Color(30, 30, 50, 150)); g2d.fillRoundRect(bx + 15, by + 5, btnWidth - 30, btnHeight - 10, 20, 20);
                    g2d.setColor(new Color(100, 100, 150, 100)); g2d.setStroke(new BasicStroke(1.5f)); g2d.drawRoundRect(bx + 15, by + 5, btnWidth - 30, btnHeight - 10, 20, 20);
                    g2d.setFont(new Font("Arial", Font.BOLD, 22));
                }
                FontMetrics fm = g2d.getFontMetrics();
                int textX = bx + (btnWidth - fm.stringWidth(displayText)) / 2;
                int textY = by + ((btnHeight - fm.getHeight()) / 2) + fm.getAscent();
                g2d.setColor(Color.BLACK); g2d.drawString(displayText, textX + 2, textY + 2);
                if (isSelected) {
                    g2d.setColor(Color.CYAN);
                    int arrowOffset = (int)(pulse * 10); g2d.drawString("▶", textX - 35 - arrowOffset, textY - 2);
                    g2d.drawString("◀", textX + fm.stringWidth(displayText) + 15 + arrowOffset, textY - 2);
                    g2d.setColor(Color.WHITE);
                } else { g2d.setColor(new Color(180, 180, 200)); }
                g2d.drawString(displayText, textX, textY);
            }

            // رسم پیام خطای هولوگرامی قرمز برای استور
            if (showStoreLoginError) {
                // پس‌زمینه تاریک
                g2d.setColor(new Color(0, 0, 0, 210));
                g2d.fillRect(0, 0, getWidth(), getHeight());

                // پنل هشدار سایبرپانکی
                int pX = 200, pY = 200, pW = 400, pH = 230;
                g2d.setColor(new Color(40, 10, 15, 240));
                g2d.fillRoundRect(pX, pY, pW, pH, 30, 30);
                g2d.setColor(new Color(255, 50, 50));
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRoundRect(pX, pY, pW, pH, 30, 30);

                // متن هشدار
                g2d.setFont(new Font("Impact", Font.ITALIC, 45));
                g2d.setColor(new Color(255, 70, 70));
                g2d.drawString("ACCESS DENIED", pX + 65, pY + 65);

                g2d.setFont(new Font("Consolas", Font.BOLD, 17));
                g2d.setColor(Color.LIGHT_GRAY);
                g2d.drawString("Pilot identification required!", pX + 50, pY + 110);
                g2d.drawString("Please LOGIN before entering Store.", pX + 35, pY + 140);

                // دکمه تایید (با افکت Hover موس)
                g2d.setColor(hoverOkError ? new Color(255, 80, 80) : new Color(150, 40, 40));
                g2d.fillRoundRect(btnOkError.x, btnOkError.y, btnOkError.width, btnOkError.height, 20, 20);
                g2d.setColor(hoverOkError ? Color.WHITE : Color.GRAY);
                g2d.drawRoundRect(btnOkError.x, btnOkError.y, btnOkError.width, btnOkError.height, 20, 20);

                g2d.setFont(new Font("Arial", Font.BOLD, 20));
                g2d.setColor(Color.WHITE);
                g2d.drawString("OK, PILOT", btnOkError.x + 32, btnOkError.y + 33);
            }
        } else if (gameState == GameState.STORE) {
            if (menuBgImage != null) g2d.drawImage(menuBgImage, 0, 0, getWidth(), getHeight(), null);
            g2d.setColor(new Color(0, 5, 20, 220)); g2d.fillRect(0, 0, getWidth(), getHeight());

            g2d.setColor(new Color(0, 255, 255, 20));
            int gridOffset = (int)((time / 20) % 40);
            for (int i = 0; i < getWidth(); i += 40) g2d.drawLine(i, 0, i, getHeight());
            for (int i = gridOffset; i < getHeight(); i += 40) g2d.drawLine(0, i, getWidth(), i);

            g2d.setFont(new Font("Impact", Font.ITALIC, 50));
            g2d.setColor(Color.CYAN); g2d.drawString("HANGAR & UPGRADES", 220, 70);

            // ===== خواندن زنده و واقعی امتیاز از دیتابیس اصلی =====
            int displayScore = 0;
            if (currentUser != null) {
                // این خط باعث می‌شود امتیاز فروشگاه دقیقاً با منوی High Scores یکی شود!
                displayScore = DatabaseManager.getUserMaxScore(currentUser);
                activePlane = DatabaseManager.getActivePlane(currentUser);
            }

            // نمایش بالاترین امتیاز در گوشه صفحه
            g2d.setFont(new Font("Consolas", Font.BOLD, 22));
            g2d.setColor(Color.YELLOW);
            g2d.drawString("HIGH SCORE: " + displayScore, 550, 40);

            int pX = 150, pY = 120, pW = 500, pH = 350;
            g2d.setColor(new Color(10, 30, 50, 180)); g2d.fillRoundRect(pX, pY, pW, pH, 30, 30);
            g2d.setColor(new Color(0, 255, 255, 100)); g2d.setStroke(new BasicStroke(3));
            g2d.drawRoundRect(pX, pY, pW, pH, 30, 30);

            int floatY = (int)(Math.sin(time / 200.0) * 15);
            g2d.setColor(new Color(0, 255, 255, 30));
            g2d.fillOval(pX + 175, pY + 160, 150, 40);
            if (storeImages[storeSelection] != null) {
                g2d.drawImage(storeImages[storeSelection], pX + 175, pY + 20 + floatY, 150, 150, null);
            }

            boolean blinkNav = (time % 800) < 400;
            g2d.setColor(blinkNav ? Color.CYAN : new Color(0, 100, 100));
            g2d.setFont(new Font("Arial", Font.BOLD, 50));
            g2d.drawString("◀", pX + 20, pY + 130);
            g2d.drawString("▶", pX + pW - 60, pY + 130);

            g2d.setFont(new Font("Consolas", Font.BOLD, 16));
            g2d.setColor(Color.WHITE);
            g2d.drawString("MODEL: " + planeNames[storeSelection].toUpperCase(), pX + 30, pY + 220);
            g2d.setColor(new Color(255, 100, 100));
            g2d.drawString("SPECIAL: " + planeSpecs[storeSelection], pX + 30, pY + 245);

            String[] statNames = {"SPEED", "FIRE RATE", "ARMOR"};
            int[] statValues = {planeSpeeds[storeSelection]*10, 300 - planeFireRates[storeSelection], planeLives[storeSelection]*20};
            Color[] statColors = {Color.CYAN, Color.ORANGE, Color.GREEN};

            for(int i=0; i<3; i++) {
                g2d.setColor(Color.LIGHT_GRAY); g2d.drawString(statNames[i], pX + 30, pY + 280 + (i*25));
                g2d.setColor(new Color(50, 50, 50)); g2d.fillRect(pX + 150, pY + 268 + (i*25), 200, 12);
                int barWidth = Math.min(200, statValues[i] * 2);
                g2d.setColor(statColors[i]); g2d.fillRect(pX + 150, pY + 268 + (i*25), barWidth, 12);
                g2d.setColor(Color.WHITE); g2d.drawRect(pX + 150, pY + 268 + (i*25), 200, 12);
            }

            String btnText;
            boolean isOwned = planeNames[storeSelection].equals(activePlane);

            if (isOwned) {
                btnText = ">>> ACTIVE SHIP <<<";
                g2d.setColor(Color.GREEN);
            }
            else if (displayScore >= planeCosts[storeSelection]) {
                btnText = "PRESS [ENTER] TO EQUIP (UNLOCKED!)";
                g2d.setColor(Color.YELLOW);
            }
            else {
                btnText = "UNLOCK (NEED " + planeCosts[storeSelection] + " PTS)";
                g2d.setColor(Color.RED);
            }

            g2d.setFont(new Font("Impact", Font.PLAIN, 26));
            int btnX = pX + (pW - g2d.getFontMetrics().stringWidth(btnText)) / 2;
            g2d.drawString(btnText, btnX, pY + pH + 45);

            if (System.currentTimeMillis() - storeMessageTime < 2000) {
                g2d.setFont(new Font("Consolas", Font.BOLD, 20));
                g2d.setColor(Color.WHITE);
                int msgX = (getWidth() - g2d.getFontMetrics().stringWidth(storeMessage)) / 2;
                g2d.drawString(storeMessage, msgX, pY + pH + 80);
            }

            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            g2d.setColor(Color.GRAY);
            g2d.drawString("PRESS [ESC] TO RETURN TO MENU", 270, getHeight() - 20);
        } else if (gameState == GameState.HIGH_SCORES) {
            if (menuBgImage != null) g2d.drawImage(menuBgImage, 0, 0, getWidth(), getHeight(), null);
            g2d.setColor(new Color(0, 5, 15, 200));
            g2d.fillRect(0, 0, getWidth(), getHeight());

            int pW = 680, pH = 520, pX = (getWidth() - pW) / 2, pY = (getHeight() - pH) / 2;

            g2d.setClip(new java.awt.geom.RoundRectangle2D.Float(pX, pY, pW, pH, 30, 30));
            g2d.setColor(new Color(10, 20, 40, 230));
            g2d.fillRect(pX, pY, pW, pH);
            g2d.setColor(new Color(0, 255, 255, 15));
            int gridOffset2 = (int)((time / 30) % 30);
            for (int i = pX; i < pX + pW; i += 30) g2d.drawLine(i, pY, i, pY + pH);
            for (int i = pY + gridOffset2; i < pY + pH; i += 30) g2d.drawLine(pX, i, pX + pW, i);

            int scanY = pY + (int)((time / 5) % pH);
            GradientPaint scanPaint = new GradientPaint(pX, scanY - 20, new Color(0, 255, 255, 0), pX, scanY, new Color(0, 255, 255, 100));
            g2d.setPaint(scanPaint);
            g2d.fillRect(pX, scanY - 20, pW, 20);
            g2d.setColor(new Color(0, 255, 255, 200));
            g2d.drawLine(pX, scanY, pX + pW, scanY);
            g2d.setClip(null);

            g2d.setColor(new Color(0, 255, 255, 100));
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(pX, pY, pW, pH, 30, 30);
            g2d.setColor(Color.CYAN);
            g2d.setStroke(new BasicStroke(4));
            int cLen = 25;
            g2d.drawLine(pX + 15, pY, pX + 15 + cLen, pY); g2d.drawLine(pX, pY + 15, pX, pY + 15 + cLen);
            g2d.drawLine(pX + pW - 15, pY, pX + pW - 15 - cLen, pY); g2d.drawLine(pX + pW, pY + 15, pX + pW, pY + 15 + cLen);
            g2d.drawLine(pX + 15, pY + pH, pX + 15 + cLen, pY + pH); g2d.drawLine(pX, pY + pH - 15, pX, pY + pH - 15 - cLen);
            g2d.drawLine(pX + pW - 15, pY + pH, pX + pW - 15 - cLen, pY + pH); g2d.drawLine(pX + pW, pY + pH - 15, pX + pW, pY + pH - 15 - cLen);

            g2d.setFont(new Font("Impact", Font.ITALIC, 45));
            String title = "HALL OF FAME";
            int titleX = pX + (pW - g2d.getFontMetrics().stringWidth(title)) / 2;
            g2d.setColor(new Color(0, 100, 255, 150));
            g2d.drawString(title, titleX + 3, pY + 63);
            g2d.setColor(Color.CYAN);
            g2d.drawString(title, titleX, pY + 60);

            g2d.setFont(new Font("Consolas", Font.BOLD, 22));
            int startY2 = pY + 130;

            for (int i = 0; i < topScores.size(); i++) {
                DatabaseManager.ScoreRecord sr = topScores.get(i);
                int rowY = startY2 + (i * 55);

                g2d.setColor(new Color(0, 50, 100, 50));
                g2d.fillRoundRect(pX + 30, rowY - 35, pW - 60, 45, 10, 10);

                Color rankColor; String rankStr; Color scoreColor = Color.CYAN;
                if (i == 0) { rankColor = new Color(255, 215, 0); rankStr = "[ 1ST ]"; scoreColor = Color.YELLOW; }
                else if (i == 1) { rankColor = new Color(192, 192, 192); rankStr = "[ 2ND ]"; }
                else if (i == 2) { rankColor = new Color(205, 127, 50); rankStr = "[ 3RD ]"; }
                else { rankColor = Color.CYAN; rankStr = "[ #" + (i + 1) + " ]"; }

                g2d.setColor(rankColor);
                g2d.drawString(rankStr, pX + 30, rowY);

                g2d.setColor(Color.WHITE);
                g2d.drawString(sr.username.toUpperCase(), pX + 120, rowY);

                g2d.setColor(new Color(0, 255, 255, 80));
                g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[]{5, 5}, 0));
                g2d.drawLine(pX + 250, rowY - 6, pX + 320, rowY - 6);

                g2d.setColor(scoreColor);
                g2d.drawString(String.format("%,d", sr.score), pX + 335, rowY);

                g2d.setColor(new Color(255, 50, 100));
                g2d.drawString("LVL " + sr.level, pX + 450, rowY);

                g2d.setFont(new Font("Consolas", Font.PLAIN, 14));
                g2d.setColor(new Color(180, 180, 200));
                String shortDate = sr.date.length() > 16 ? sr.date.substring(0, 16) : sr.date;
                g2d.drawString(shortDate, pX + 520, rowY - 15);
                g2d.setFont(new Font("Consolas", Font.BOLD, 22));
            }

            g2d.setFont(new Font("Arial", Font.BOLD, 18));
            boolean blinkFast = (time % 600) < 300;
            g2d.setColor(blinkFast ? Color.WHITE : new Color(100, 100, 100));
            String retText = ">>> PRESS [ ESC ] TO RETURN TO HANGAR <<<";
            g2d.drawString(retText, pX + (pW - g2d.getFontMetrics().stringWidth(retText)) / 2, pY + pH - 25);
        } else if (gameState == GameState.SETTINGS) {
            if (menuBgImage != null) g2d.drawImage(menuBgImage, 0, 0, getWidth(), getHeight(), null);
            g2d.setColor(new Color(15, 0, 25, 200)); g2d.fillRect(0, 0, getWidth(), getHeight());
            int scanY2 = (int) ((time / 3) % getHeight());
            g2d.setColor(new Color(255, 0, 255, 25)); g2d.fillRect(0, scanY2, getWidth(), 15);
            g2d.setColor(new Color(255, 0, 255, 80)); g2d.drawLine(0, scanY2 + 15, getWidth(), scanY2 + 15);
            int pW = 600, pH = 450, pX2 = (getWidth() - pW) / 2, pY2 = (getHeight() - pH) / 2;
            g2d.setColor(new Color(40, 10, 50, 180)); g2d.fillRoundRect(pX2, pY2, pW, pH, 30, 30);
            g2d.setColor(new Color(255, 0, 255, 150)); g2d.setStroke(new BasicStroke(2.5f)); g2d.drawRoundRect(pX2, pY2, pW, pH, 30, 30);
            double glowPulse = (Math.sin(time / 150.0) + 1) / 2.0;
            g2d.setFont(new Font("Impact", Font.ITALIC, 45));
            String title2 = "AUDIO SYSTEM PREFERENCES";
            int titleX2 = pX2 + (pW - g2d.getFontMetrics().stringWidth(title2)) / 2;
            g2d.setColor(new Color(255, 0, 255, 50 + (int)(glowPulse * 100))); g2d.drawString(title2, titleX2 + 4, pY2 + 64);
            g2d.setColor(Color.MAGENTA); g2d.drawString(title2, titleX2, pY2 + 60);
            String[] labels = {"🎵 MAIN MUSIC THEME", "🔫 LASER WEAPON SFX", "💥 HULL IMPACT SFX", "☠️ CRITICAL FAILURE SFX"};
            boolean[] states = {userSettings.musicOn, userSettings.shootSfx, userSettings.hitSfx, userSettings.gameoverSfx};
            String[] keys = {"[ M ]", "[ S ]", "[ H ]", "[ G ]"};
            int startY3 = pY2 + 130;
            for (int i = 0; i < 4; i++) {
                int rowY = startY3 + (i * 65);
                g2d.setColor(new Color(255, 255, 255, 15)); g2d.fillRoundRect(pX2 + 30, rowY - 30, pW - 60, 50, 15, 15);
                g2d.setFont(new Font("Consolas", Font.BOLD, 20)); g2d.setColor(Color.WHITE); g2d.drawString(labels[i], pX2 + 50, rowY + 2);
                g2d.setColor(new Color(0, 255, 255)); g2d.drawString(keys[i], pX2 + 340, rowY + 2);
                int swX = pX2 + 430, swY = rowY - 20, swW = 70, swH = 30;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (states[i]) {
                    g2d.setColor(new Color(0, 255, 150, 60)); g2d.fillRoundRect(swX - 4, swY - 4, swW + 8, swH + 8, swH, swH);
                    g2d.setColor(new Color(0, 200, 120)); g2d.fillRoundRect(swX, swY, swW, swH, swH, swH);
                    g2d.setColor(Color.WHITE); g2d.fillOval(swX + swW - swH + 2, swY + 2, swH - 4, swH - 4);
                    g2d.setFont(new Font("Arial", Font.BOLD, 13)); g2d.setColor(Color.BLACK); g2d.drawString("ON", swX + 10, swY + 20);
                } else {
                    g2d.setColor(new Color(50, 20, 30, 200)); g2d.fillRoundRect(swX, swY, swW, swH, swH, swH);
                    g2d.setColor(new Color(150, 150, 150)); g2d.fillOval(swX + 2, swY + 2, swH - 4, swH - 4);
                    g2d.setFont(new Font("Arial", Font.BOLD, 13)); g2d.setColor(new Color(255, 100, 100)); g2d.drawString("OFF", swX + 35, swY + 20);
                }
            }
            boolean blinkText = (time % 1000) < 500;
            g2d.setFont(new Font("Consolas", Font.BOLD, 18)); g2d.setColor(blinkText ? Color.MAGENTA : new Color(150, 50, 150));
            String escText = ">>> SYSTEM OVERRIDE: PRESS [ ESC ] TO SAVE AND RETURN <<<";
            g2d.drawString(escText, pX2 + (pW - g2d.getFontMetrics().stringWidth(escText)) / 2, pY2 + pH - 25);
        } else if (gameState == GameState.HOW_TO_PLAY) {
            if (menuBgImage != null) g2d.drawImage(menuBgImage, 0, 0, getWidth(), getHeight(), null);
            g2d.setColor(new Color(0, 5, 15, 200)); g2d.fillRect(0, 0, getWidth(), getHeight());
            int pW = 680, pH = 520, pX3 = (getWidth() - pW) / 2, pY3 = (getHeight() - pH) / 2;
            g2d.setClip(new java.awt.geom.RoundRectangle2D.Float(pX3, pY3, pW, pH, 30, 30));
            g2d.setColor(new Color(10, 20, 40, 230)); g2d.fillRect(pX3, pY3, pW, pH);
            g2d.setColor(new Color(0, 255, 255, 15)); int gridOffset3 = (int)((time / 30) % 30);
            for (int i = pX3; i < pX3 + pW; i += 30) g2d.drawLine(i, pY3, i, pY3 + pH);
            for (int i = pY3 + gridOffset3; i < pY3 + pH; i += 30) g2d.drawLine(pX3, i, pX3 + pW, i);
            int scanY3 = pY3 + (int)((time / 5) % pH);
            GradientPaint scanPaint3 = new GradientPaint(pX3, scanY3 - 20, new Color(0, 255, 255, 0), pX3, scanY3, new Color(0, 255, 255, 100));
            g2d.setPaint(scanPaint3); g2d.fillRect(pX3, scanY3 - 20, pW, 20);
            g2d.setColor(new Color(0, 255, 255, 200)); g2d.drawLine(pX3, scanY3, pX3 + pW, scanY3);
            g2d.setClip(null);
            g2d.setColor(new Color(0, 255, 255, 100)); g2d.setStroke(new BasicStroke(2)); g2d.drawRoundRect(pX3, pY3, pW, pH, 30, 30);
            g2d.setColor(Color.CYAN); g2d.setStroke(new BasicStroke(4));
            int cLen2 = 25;
            g2d.drawLine(pX3 + 15, pY3, pX3 + 15 + cLen2, pY3); g2d.drawLine(pX3, pY3 + 15, pX3, pY3 + 15 + cLen2);
            g2d.drawLine(pX3 + pW - 15, pY3, pX3 + pW - 15 - cLen2, pY3); g2d.drawLine(pX3 + pW, pY3 + 15, pX3 + pW, pY3 + 15 + cLen2);
            g2d.drawLine(pX3 + 15, pY3 + pH, pX3 + 15 + cLen2, pY3 + pH); g2d.drawLine(pX3, pY3 + pH - 15, pX3, pY3 + pH - 15 - cLen2);
            g2d.drawLine(pX3 + pW - 15, pY3 + pH, pX3 + pW - 15 - cLen2, pY3 + pH); g2d.drawLine(pX3 + pW, pY3 + pH - 15, pX3 + pW, pY3 + pH - 15 - cLen2);
            g2d.setFont(new Font("Impact", Font.ITALIC, 45));
            String title3 = "TACTICAL COMMANDS";
            int titleX3 = pX3 + (pW - g2d.getFontMetrics().stringWidth(title3)) / 2;
            g2d.setColor(new Color(0, 100, 255, 150)); g2d.drawString(title3, titleX3 + 3, pY3 + 63);
            g2d.setColor(Color.CYAN); g2d.drawString(title3, titleX3, pY3 + 60);
            g2d.setFont(new Font("Consolas", Font.BOLD, 22));
            String[][] commands = {{"[ W ]", "[ ▲ ]", "THRUST UP"}, {"[ S ]", "[ ▼ ]", "THRUST DOWN"}, {"[ A ]", "[ ◀ ]", "STRAFE LEFT"}, {"[ D ]", "[ ▶ ]", "STRAFE RIGHT"}, {"[   SPACEBAR   ]", "", "FIRE WEAPON"}, {"[ P ]", "", "TACTICAL PAUSE"}};
            int startY4 = pY3 + 130;
            for (int i = 0; i < commands.length; i++) {
                int rowY = startY4 + (i * 55);
                g2d.setColor(new Color(0, 50, 100, 50)); g2d.fillRoundRect(pX3 + 30, rowY - 35, pW - 60, 45, 10, 10);
                g2d.setColor(Color.YELLOW); g2d.drawString(commands[i][0], pX3 + 60, rowY);
                if (!commands[i][1].isEmpty()) {
                    g2d.setColor(new Color(150, 150, 150)); g2d.drawString("OR", pX3 + 150, rowY);
                    g2d.setColor(Color.YELLOW); g2d.drawString(commands[i][1], pX3 + 200, rowY);
                }
                g2d.setColor(new Color(0, 255, 255, 80)); g2d.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[]{5, 5}, 0));
                g2d.drawLine(pX3 + 320, rowY - 6, pX3 + 430, rowY - 6);
                g2d.setColor(Color.CYAN); g2d.drawString(commands[i][2], pX3 + 450, rowY);
            }
            g2d.setFont(new Font("Arial", Font.BOLD, 18));
            boolean blinkFast2 = (time % 600) < 300;
            g2d.setColor(blinkFast2 ? Color.WHITE : new Color(100, 100, 100));
            String retText2 = ">>> PRESS [ ESC ] TO RETURN TO HANGAR <<<";
            g2d.drawString(retText2, pX3 + (pW - g2d.getFontMetrics().stringWidth(retText2)) / 2, pY3 + pH - 25);
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
            long elapsed = time - gameOverStartTime;

            GradientPaint bgGrad = new GradientPaint(0, 0, new Color(40, 0, 0), 0, getHeight(), Color.BLACK);
            g2d.setPaint(bgGrad);
            g2d.fillRect(0, 0, getWidth(), getHeight());

            g2d.setColor(new Color(255, 0, 0, 15));
            for (int i = 0; i < 15; i++) {
                int ly = (int) ((time / 4 + i * 137) % getHeight());
                g2d.drawLine(0, ly, getWidth(), ly);
            }

            int shakeX = 0, shakeY = 0;
            if (elapsed < 600) {
                double shakePower = 1.0 - (elapsed / 600.0);
                shakeX = (int) ((random.nextDouble() - 0.5) * 20 * shakePower);
                shakeY = (int) ((random.nextDouble() - 0.5) * 20 * shakePower);
            }
            g2d.translate(shakeX, shakeY);

            for (GameOverParticle p : gameOverParticles) {
                float alpha = (float) Math.max(0, Math.min(1, p.life));
                Color c = p.color;
                g2d.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (alpha * 255)));
                int s = (int) (p.size * (0.5 + alpha));
                g2d.fillOval((int) p.x - s / 2, (int) p.y - s / 2, s, s);
            }

            g2d.setColor(new Color(255, 0, 0, 10));
            int scanOffset2 = (int) ((time / 20) % 4);
            for (int y = scanOffset2; y < getHeight(); y += 4) {
                g2d.drawLine(0, y, getWidth(), y);
            }

            double titlePulse = (Math.sin(time / 200.0) + 1) / 2.0;
            float titleScale = elapsed < 500 ? (float) (0.5 + 0.5 * Math.min(1, elapsed / 500.0)) : 1.0f;

            g2d.setFont(new Font("Impact", Font.ITALIC, (int) (80 * titleScale)));
            String goTitle = "GAME OVER";
            FontMetrics fmGo = g2d.getFontMetrics();
            int goX = (getWidth() - fmGo.stringWidth(goTitle)) / 2;
            int goY = 220;

            int glitchJitter = (random.nextInt(100) < 8) ? random.nextInt(10) - 5 : 0;

            g2d.setColor(new Color(0, 255, 255, 180));
            g2d.drawString(goTitle, goX - 4 + glitchJitter, goY);
            g2d.setColor(new Color(255, 0, 60, 180));
            g2d.drawString(goTitle, goX + 4 + glitchJitter, goY);
            g2d.setColor(new Color(255, 60, 0, (int) (200 + titlePulse * 55)));
            g2d.drawString(goTitle, goX, goY);

            g2d.setFont(new Font("Consolas", Font.BOLD, 20));
            g2d.setColor(new Color(255, 200, 200));
            String subTitle = "> > >  MISSION FAILED  < < <";
            int subX = (getWidth() - g2d.getFontMetrics().stringWidth(subTitle)) / 2;
            g2d.drawString(subTitle, subX, goY + 35);

            int panelW = 400, panelH = 160;
            int panelX = (getWidth() - panelW) / 2;
            int panelY = goY + 70;

            g2d.setColor(new Color(20, 0, 0, 220));
            g2d.fillRoundRect(panelX, panelY, panelW, panelH, 25, 25);
            g2d.setColor(new Color(255, 60, 60, 150 + (int) (titlePulse * 100)));
            g2d.setStroke(new BasicStroke(2.5f));
            g2d.drawRoundRect(panelX, panelY, panelW, panelH, 25, 25);

            g2d.setFont(new Font("Consolas", Font.BOLD, 18));
            g2d.setColor(new Color(255, 150, 150));
            g2d.drawString("FINAL SCORE", panelX + 30, panelY + 40);

            g2d.setFont(new Font("Consolas", Font.BOLD, 34));
            g2d.setColor(Color.YELLOW);
            g2d.drawString(String.format("%,d", (int) displayedScore), panelX + 30, panelY + 80);

            g2d.setFont(new Font("Consolas", Font.BOLD, 16));
            g2d.setColor(new Color(200, 200, 255));
            g2d.drawString("LEVEL REACHED: " + currentLevel, panelX + 30, panelY + 115);

            g2d.setColor(new Color(255, 255, 255, 60));
            g2d.drawLine(panelX + 20, panelY + 125, panelX + panelW - 20, panelY + 125);

            boolean promptBlink = (time % 800) < 450;
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            g2d.setColor(promptBlink ? Color.WHITE : new Color(120, 120, 120));
            String prompt = "PRESS [ ESC ] TO RETURN TO MENU";
            int promptX = (getWidth() - g2d.getFontMetrics().stringWidth(prompt)) / 2;
            g2d.drawString(prompt, promptX, panelY + panelH + 35);

            g2d.translate(-shakeX, -shakeY);
        } else if (gameState == GameState.WIN) {
            long elapsed = time - winStartTime;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            GradientPaint bgGrad2 = new GradientPaint(0, 0, new Color(10, 5, 35), 0, getHeight(), new Color(0, 0, 10));
            g2d.setPaint(bgGrad2);
            g2d.fillRect(0, 0, getWidth(), getHeight());

            for (int i = 0; i < 80; i++) {
                int sx = (i * 137) % getWidth();
                int sy = (i * 977) % getHeight();
                double twinkle = (Math.sin(time / 300.0 + i) + 1) / 2.0;
                g2d.setColor(new Color(255, 255, 255, (int) (60 + twinkle * 150)));
                int ss = 1 + (i % 3);
                g2d.fillOval(sx, sy, ss, ss);
            }

            int cx = getWidth() / 2, cy = 180;
            java.awt.geom.AffineTransform oldT = g2d.getTransform();
            g2d.translate(cx, cy);
            g2d.rotate(time / 4000.0);
            for (int i = 0; i < 12; i++) {
                g2d.setColor(new Color(255, 230, 120, 18));
                g2d.rotate(Math.toRadians(30));
                g2d.fillRect(-4, 0, 8, 260);
            }
            g2d.setTransform(oldT);

            for (ConfettiParticle p : confettiParticles) p.draw(g2d);

            for (FireworkParticle p : fireworkParticles) {
                float alpha = (float) Math.max(0, Math.min(1, p.life));
                Color c = p.color;
                g2d.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (alpha * 255)));
                int s = (int) (p.size * (0.6 + alpha));
                g2d.fillOval((int) p.x - s / 2, (int) p.y - s / 2, s, s);
                g2d.setColor(new Color(255, 255, 255, (int) (alpha * 80)));
                g2d.fillOval((int) p.x - s / 4, (int) p.y - s / 4, Math.max(1, s / 2), Math.max(1, s / 2));
            }

            int trophyX = getWidth() / 2, trophyY = 130;
            double haloPulse = (Math.sin(time / 200.0) + 1) / 2.0;
            g2d.setColor(new Color(255, 215, 0, (int) (40 + haloPulse * 60)));
            int haloSize = 90 + (int) (haloPulse * 15);
            g2d.fillOval(trophyX - haloSize / 2, trophyY - haloSize / 2, haloSize, haloSize);

            java.awt.geom.AffineTransform trophyT = g2d.getTransform();
            g2d.translate(trophyX, trophyY);
            double wobble = Math.sin(trophyRotation) * 0.25;
            g2d.rotate(wobble);
            GradientPaint trophyGrad = new GradientPaint(-25, -30, new Color(255, 240, 150), 25, 30, new Color(200, 150, 0));
            g2d.setPaint(trophyGrad);
            int[] cupX = {-22, 22, 14, -14};
            int[] cupY = {-25, -25, 10, 10};
            g2d.fillPolygon(cupX, cupY, 4);
            g2d.fillRect(-6, 10, 12, 14);
            g2d.fillRect(-18, 24, 36, 8);
            g2d.setStroke(new BasicStroke(4));
            g2d.setColor(new Color(230, 190, 60));
            g2d.drawArc(-40, -22, 20, 24, 90, 180);
            g2d.drawArc(20, -22, 20, 24, -90, 180);
            g2d.setColor(new Color(255, 255, 255, 150));
            g2d.fillOval(-14, -18, 8, 14);
            g2d.setTransform(trophyT);

            String winTitle = "VICTORY!";
            double entrance = Math.min(1.0, elapsed / 500.0);
            double bounce = 1.0 + Math.sin(entrance * Math.PI) * (1 - entrance) * 0.4;
            int baseSize = (int) (70 * (entrance) * bounce + 5);
            if (baseSize < 10) baseSize = 10;
            g2d.setFont(new Font("Impact", Font.ITALIC, baseSize));
            FontMetrics fmWin = g2d.getFontMetrics();
            int totalW = fmWin.stringWidth(winTitle);
            int startX2 = (getWidth() - totalW) / 2;
            int winY2 = 250;
            int cursorX = startX2;
            for (int i = 0; i < winTitle.length(); i++) {
                char ch = winTitle.charAt(i);
                float hue = (float) (((time / 15.0) + i * 25) % 360) / 360f;
                Color rainbow = Color.getHSBColor(hue, 0.85f, 1.0f);
                g2d.setColor(new Color(0, 0, 0, 120));
                g2d.drawString(String.valueOf(ch), cursorX + 3, winY2 + 3);
                g2d.setColor(rainbow);
                g2d.drawString(String.valueOf(ch), cursorX, winY2);
                cursorX += g2d.getFontMetrics().charWidth(ch);
            }

            g2d.setFont(new Font("Consolas", Font.BOLD, 20));
            g2d.setColor(new Color(200, 255, 230));
            String subTitle2 = "*** ALL SECTORS CLEARED ***";
            int subX2 = (getWidth() - g2d.getFontMetrics().stringWidth(subTitle2)) / 2;
            g2d.drawString(subTitle2, subX2, winY2 + 35);

            int panelW2 = 420, panelH2 = 170;
            int panelX2 = (getWidth() - panelW2) / 2;
            int panelY2 = winY2 + 65;

            g2d.setColor(new Color(20, 15, 0, 220));
            g2d.fillRoundRect(panelX2, panelY2, panelW2, panelH2, 25, 25);
            double borderPulse = (Math.sin(time / 180.0) + 1) / 2.0;
            g2d.setColor(new Color(255, 215, 0, 150 + (int) (borderPulse * 100)));
            g2d.setStroke(new BasicStroke(3f));
            g2d.drawRoundRect(panelX2, panelY2, panelW2, panelH2, 25, 25);

            g2d.setFont(new Font("Consolas", Font.BOLD, 18));
            g2d.setColor(new Color(255, 235, 180));
            g2d.drawString("FINAL SCORE", panelX2 + 30, panelY2 + 40);

            g2d.setFont(new Font("Consolas", Font.BOLD, 36));
            g2d.setColor(Color.YELLOW);
            g2d.drawString(String.format("%,d", (int) winDisplayedScore), panelX2 + 30, panelY2 + 82);

            g2d.setFont(new Font("Consolas", Font.BOLD, 16));
            g2d.setColor(new Color(200, 255, 220));
            g2d.drawString("PILOT: " + (currentUser != null ? currentUser.toUpperCase() : "GUEST"), panelX2 + 30, panelY2 + 112);
            g2d.drawString("ALL 8 SECTORS CLEARED", panelX2 + 30, panelY2 + 135);

            g2d.setColor(new Color(255, 255, 255, 60));
            g2d.drawLine(panelX2 + 20, panelY2 + 145, panelX2 + panelW2 - 20, panelY2 + 145);

            boolean promptBlink2 = (time % 800) < 450;
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            g2d.setColor(promptBlink2 ? Color.WHITE : new Color(150, 150, 100));
            String prompt2 = "PRESS [ ESC ] TO RETURN TO MENU";
            int promptX2 = (getWidth() - g2d.getFontMetrics().stringWidth(prompt2)) / 2;
            g2d.drawString(prompt2, promptX2, panelY2 + panelH2 + 35);
        }

        // ===== منوی تایید خروج خفن =====
        if (showExitConfirm) {
            g2d.setColor(new Color(0, 0, 0, 200));
            g2d.fillRect(0, 0, getWidth(), getHeight());

            int pX = 200, pY = 200, pW = 400, pH = 220;
            g2d.setColor(new Color(15, 25, 45, 240));
            g2d.fillRoundRect(pX, pY, pW, pH, 30, 30);
            g2d.setColor(Color.CYAN);
            g2d.setStroke(new BasicStroke(3));
            g2d.drawRoundRect(pX, pY, pW, pH, 30, 30);

            g2d.setFont(new Font("Impact", Font.ITALIC, 38));
            g2d.setColor(new Color(255, 70, 70));
            g2d.drawString("ABORT MISSION?", pX + 80, pY + 55);

            g2d.setFont(new Font("Consolas", Font.BOLD, 15));
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.drawString("Current progress will be lost.", pX + 70, pY + 95);

            g2d.setColor(hoverButton == 0 ? new Color(255, 80, 80) : new Color(150, 40, 40));
            g2d.fillRoundRect(btnYes.x, btnYes.y, btnYes.width, btnYes.height, 20, 20);
            g2d.setColor(hoverButton == 0 ? Color.WHITE : Color.GRAY);
            g2d.drawRoundRect(btnYes.x, btnYes.y, btnYes.width, btnYes.height, 20, 20);
            g2d.setFont(new Font("Arial", Font.BOLD, 18));
            g2d.setColor(Color.WHITE);
            g2d.drawString("YES, EXIT", btnYes.x + 25, btnYes.y + 32);

            g2d.setColor(hoverButton == 1 ? new Color(50, 255, 100) : new Color(30, 150, 50));
            g2d.fillRoundRect(btnNo.x, btnNo.y, btnNo.width, btnNo.height, 20, 20);
            g2d.setColor(hoverButton == 1 ? Color.WHITE : Color.GRAY);
            g2d.drawRoundRect(btnNo.x, btnNo.y, btnNo.width, btnNo.height, 20, 20);
            g2d.setColor(Color.WHITE);
            g2d.drawString("NO, STAY", btnNo.x + 28, btnNo.y + 32);
        }
    }

    // ===== actionPerformed با توقف زمان هنگام نمایش منوی خروج =====
    @Override
    public void actionPerformed(ActionEvent e) {
        if (showExitConfirm) {
            repaint();
            return;
        }

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
                gameOverStartTime = System.currentTimeMillis();
                displayedScore = 0;
                gameOverParticles.clear();
                for (int i = 0; i < 60; i++) {
                    gameOverParticles.add(new GameOverParticle(
                            plane.getX() + plane.getWidth() / 2.0,
                            plane.getY() + plane.getHeight() / 2.0));
                }
                DatabaseManager.saveScore(currentUser, score, currentLevel, userSettings);
                if (userSettings.gameoverSfx) {
                    SoundManager.stopMusic();
                    SoundManager.playSound(SoundManager.GAME_OVER);
                }
            }
        }
        if (gameState == GameState.GAMEOVER) {
            updateGameOverScreen();
        }
        if (gameState == GameState.WIN) {
            updateWinScreen();
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
            }
            lastEggTime = currentTime;
        }

        for (Enemy enemy : enemies) {
            if (enemy instanceof ShooterEnemy) {
                ShooterEnemy se = (ShooterEnemy) enemy;
                if (se.canShootBullet()) {
                    int dirX = (plane.getX() < se.getX()) ? -5 : 5;
                    eggs.add(new Egg(se.getX() + 15, se.getY() + 20, dirX, 0, eggImage));
                }
            }
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
        if (System.currentTimeMillis() < freezeEndTime) return;
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
        if (System.currentTimeMillis() < freezeEndTime) return;
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
                            String selectedType = types[random.nextInt(types.length)];
                            Image powerUpImg = null;

                            switch(selectedType) {
                                case "AddFire": powerUpImg = addFireImg; break;
                                case "RapidFire": powerUpImg = rapidFireImg; break;
                                case "ExtraLife": powerUpImg = extraLifeImg; break;
                                case "Shield": powerUpImg = shieldImg; break;
                                case "FreezeBomb": powerUpImg = freezeBombImg; break;
                            }
                            powerUps.add(new PowerUp(e.getX(), e.getY(), selectedType, powerUpImg));
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
                    winStartTime = System.currentTimeMillis();
                    lastFireworkSpawn = 0;
                    winDisplayedScore = 0;
                    confettiParticles.clear();
                    fireworkParticles.clear();
                    for (int i = 0; i < 120; i++) confettiParticles.add(new ConfettiParticle(true));
                    DatabaseManager.saveScore(currentUser, score, currentLevel, userSettings);
                    SoundManager.stopMusic();
                    if (userSettings.musicOn) SoundManager.playMusic(SoundManager.ENDING_THEME);
                    if (userSettings.gameoverSfx) SoundManager.playSound(SoundManager.WIN_SOUND);
                } else {
                    currentLevel++; initLevel5();
                }
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

    private void updateGameOverScreen() {
        Iterator<GameOverParticle> it = gameOverParticles.iterator();
        while (it.hasNext()) {
            GameOverParticle p = it.next();
            p.update();
            if (!p.isAlive()) it.remove();
        }
        if (displayedScore < score) {
            displayedScore += Math.max(1, (score - displayedScore) * 0.08);
            if (displayedScore > score) displayedScore = score;
        }
    }

    private class GameOverParticle {
        double x, y, dx, dy;
        Color color;
        int size;
        double life = 1.0;

        GameOverParticle(double x, double y) {
            this.x = x;
            this.y = y;
            double angle = random.nextDouble() * Math.PI * 2;
            double speed = 2 + random.nextDouble() * 8;
            dx = Math.cos(angle) * speed;
            dy = Math.sin(angle) * speed;
            size = 2 + random.nextInt(5);
            Color[] palette = {Color.ORANGE, Color.RED, Color.YELLOW, new Color(255, 100, 0)};
            color = palette[random.nextInt(palette.length)];
        }

        void update() {
            x += dx;
            y += dy;
            dx *= 0.98;
            dy *= 0.98;
            life -= 0.012;
        }

        boolean isAlive() { return life > 0; }
    }

    private void updateWinScreen() {
        trophyRotation += 0.015;

        for (ConfettiParticle p : confettiParticles) p.update(getWidth(), getHeight());

        long now = System.currentTimeMillis();
        if (now - lastFireworkSpawn > 550) {
            lastFireworkSpawn = now;
            int fx = 80 + random.nextInt(getWidth() - 160);
            int fy = 60 + random.nextInt(220);
            Color[] palette = {
                    new Color(255, 60, 60), new Color(255, 215, 0), new Color(0, 220, 255),
                    new Color(0, 255, 140), new Color(255, 0, 220), new Color(255, 255, 255)
            };
            Color burstColor = palette[random.nextInt(palette.length)];
            int count = 40 + random.nextInt(20);
            for (int i = 0; i < count; i++) {
                fireworkParticles.add(new FireworkParticle(fx, fy, burstColor));
            }
        }

        Iterator<FireworkParticle> it = fireworkParticles.iterator();
        while (it.hasNext()) {
            FireworkParticle p = it.next();
            p.update();
            if (!p.isAlive()) it.remove();
        }

        if (winDisplayedScore < score) {
            winDisplayedScore += Math.max(1, (score - winDisplayedScore) * 0.08);
            if (winDisplayedScore > score) winDisplayedScore = score;
        }
    }

    private class ConfettiParticle {
        double x, y, dx, dy, rotation, rotSpeed;
        int size;
        Color color;

        ConfettiParticle(boolean randomStartY) {
            reset(randomStartY);
        }

        void reset(boolean randomStartY) {
            x = random.nextDouble() * 800;
            y = randomStartY ? random.nextDouble() * -600 : -20;
            dx = (random.nextDouble() - 0.5) * 2.0;
            dy = 2 + random.nextDouble() * 3;
            rotation = random.nextDouble() * Math.PI * 2;
            rotSpeed = (random.nextDouble() - 0.5) * 0.3;
            size = 6 + random.nextInt(7);
            Color[] palette = {
                    new Color(255, 80, 80), new Color(255, 215, 0), new Color(80, 220, 255),
                    new Color(120, 255, 120), new Color(255, 120, 255), new Color(255, 255, 255)
            };
            color = palette[random.nextInt(palette.length)];
        }

        void update(int width, int height) {
            x += dx;
            y += dy;
            rotation += rotSpeed;
            if (y > height + 20) reset(false);
        }

        void draw(Graphics2D g2d) {
            java.awt.geom.AffineTransform old = g2d.getTransform();
            g2d.translate(x, y);
            g2d.rotate(rotation);
            g2d.setColor(color);
            g2d.fillRect(-size / 2, -size / 4, size, size / 2);
            g2d.setTransform(old);
        }
    }

    private class FireworkParticle {
        double x, y, dx, dy;
        Color color;
        double life = 1.0;
        int size;

        FireworkParticle(double x, double y, Color baseColor) {
            this.x = x;
            this.y = y;
            double angle = random.nextDouble() * Math.PI * 2;
            double speed = 1.5 + random.nextDouble() * 6;
            dx = Math.cos(angle) * speed;
            dy = Math.sin(angle) * speed;
            size = 2 + random.nextInt(4);
            color = baseColor;
        }

        void update() {
            x += dx;
            y += dy;
            dy += 0.05;
            dx *= 0.985;
            dy *= 0.985;
            life -= 0.018;
        }

        boolean isAlive() { return life > 0; }
    }

    private void attemptLogin() {
        if (usernameInput.isEmpty() || passwordInput.isEmpty()) {
            loginMessageColor = Color.RED;
            loginMessage = "ERROR: EMPTY FIELDS!";
        } else if (DatabaseManager.login(usernameInput, passwordInput)) {
            currentUser = usernameInput;
            userSettings = DatabaseManager.getUserSettings(currentUser);
            gameState = GameState.MENU;
            if (userSettings.musicOn) SoundManager.playMusic(SoundManager.MAIN_THEME);
            else SoundManager.stopMusic();
        } else {
            loginMessageColor = Color.RED;
            loginMessage = "ERROR: INCORRECT USERNAME OR PASSWORD!";
        }
    }

    private void attemptRegister() {
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

    private void handleMouseClick(int mx, int my) {
        if (gameState != GameState.LOGIN) return;

        int panelWidth = LOGIN_PANEL_WIDTH, panelHeight = LOGIN_PANEL_HEIGHT;
        int px = (getWidth() - panelWidth) / 2, py = (getHeight() - panelHeight) / 2;

        Rectangle usernameField = new Rectangle(px + 180, py + 95, 250, 35);
        Rectangle passwordField = new Rectangle(px + 180, py + 155, 250, 35);
        int btnY = py + 230;
        Rectangle loginButton = new Rectangle(px + 50, btnY, 180, 45);
        Rectangle registerButton = new Rectangle(px + 250, btnY, 200, 45);

        if (usernameField.contains(mx, my)) {
            loginSelection = 0;
        } else if (passwordField.contains(mx, my)) {
            loginSelection = 1;
        } else if (loginButton.contains(mx, my)) {
            loginSelection = 2;
            attemptLogin();
        } else if (registerButton.contains(mx, my)) {
            loginSelection = 3;
            attemptRegister();
        }
        repaint();
    }

    // ===== keyPressed با امتیاز واقعی از دیتابیس و قفل کیبورد در ارور استور =====
    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (gameState == GameState.LOGIN) {
            if (key == KeyEvent.VK_ESCAPE) {
                gameState = GameState.MENU;
            } else if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_TAB) {
                loginSelection = (loginSelection + 1) % 4;
            } else if (key == KeyEvent.VK_UP) {
                loginSelection = (loginSelection - 1 + 4) % 4;
            } else if (key == KeyEvent.VK_BACK_SPACE) {
                if (loginSelection == 0 && usernameInput.length() > 0) usernameInput = usernameInput.substring(0, usernameInput.length() - 1);
                else if (loginSelection == 1 && passwordInput.length() > 0) passwordInput = passwordInput.substring(0, passwordInput.length() - 1);
            } else if (key == KeyEvent.VK_ENTER) {
                if (loginSelection == 0) loginSelection = 1;
                else if (loginSelection == 1) loginSelection = 2;
                else if (loginSelection == 2) {
                    if (usernameInput.isEmpty() || passwordInput.isEmpty()) {
                        loginMessageColor = Color.RED; loginMessage = "ERROR: EMPTY FIELDS!";
                    } else if (DatabaseManager.login(usernameInput, passwordInput)) {
                        currentUser = usernameInput;
                        userSettings = DatabaseManager.getUserSettings(currentUser);
                        startGame();
                        if (userSettings.musicOn) SoundManager.playMusic(SoundManager.MAIN_THEME);
                        else SoundManager.stopMusic();
                    } else {
                        loginMessageColor = Color.RED; loginMessage = "ERROR: INCORRECT CREDENTIALS!";
                    }
                } else if (loginSelection == 3) {
                    if (usernameInput.isEmpty() || passwordInput.isEmpty()) {
                        loginMessageColor = Color.RED; loginMessage = "ERROR: EMPTY FIELDS!";
                    } else if (DatabaseManager.register(usernameInput, passwordInput)) {
                        // 🚀 ثبت‌نام موفق = لاگین اتوماتیک و پرواز مستقیم به مرحله اول
                        currentUser = usernameInput;
                        userSettings = DatabaseManager.getUserSettings(currentUser);
                        startGame();
                        if (userSettings.musicOn) SoundManager.playMusic(SoundManager.MAIN_THEME);
                        else SoundManager.stopMusic();
                    } else {
                        loginMessageColor = Color.RED; loginMessage = "ERROR: USER EXISTS!";
                    }
                }
            }
        }
        else if (gameState == GameState.MENU) {
            // اگر خطای استور باز است، کاربر نتواند با کیبورد در منو جابجا شود
            if (showStoreLoginError) {
                if (key == KeyEvent.VK_ENTER || key == KeyEvent.VK_ESCAPE) {
                    showStoreLoginError = false; // با کیبورد هم بتواند ببندد
                }
                return; // این دستور باعث قفل شدن بقیه کدهای این بخش می‌شود
            }

            // کدهای حرکت در منو (VK_UP, VK_DOWN)
            if (key == KeyEvent.VK_UP) { currentMenuSelection--; if (currentMenuSelection < 0) currentMenuSelection = menuOptions.length - 1; }
            else if (key == KeyEvent.VK_DOWN) { currentMenuSelection++; if (currentMenuSelection > menuOptions.length - 1) currentMenuSelection = 0; }
            else if (key == KeyEvent.VK_ENTER) {
                if (currentMenuSelection == 0) {
                    gameState = GameState.LOGIN;
                    loginMessage = "SYSTEM READY - AWAITING CREDENTIALS";
                    loginMessageColor = Color.YELLOW;
                    usernameInput = ""; passwordInput = ""; loginSelection = 0;
                }
                else if (currentMenuSelection == 1) { // گزینه Store
                    if (currentUser != null) {
                        // ورود مجاز به استور
                        Object[] info = DatabaseManager.getStoreInfo(currentUser);
                        if (info != null && info.length >= 2) {
                            int playerHighScore = (int) info[0];
                            activePlane = (String) info[1];
                        }
                        storeSelection = 0;
                        for (int i = 0; i < planeNames.length; i++) {
                            if (planeNames[i].equals(activePlane)) {
                                storeSelection = i;
                                break;
                            }
                        }
                        storeMessage = "";
                        gameState = GameState.STORE;
                    } else {
                        // 🔴 نمایش ارور خفن گرافیکی برای مهمان!
                        showStoreLoginError = true;
                    }
                }
                else if (currentMenuSelection == 2) { topScores = DatabaseManager.getTopScores(5); gameState = GameState.HIGH_SCORES; }
                else if (currentMenuSelection == 3) { previousState = GameState.MENU; gameState = GameState.SETTINGS; }
                else if (currentMenuSelection == 4) gameState = GameState.HOW_TO_PLAY;
                else if (currentMenuSelection == 5) System.exit(0);
            }
        }
        else if (gameState == GameState.STORE) {
            if (key == KeyEvent.VK_ESCAPE) {
                gameState = GameState.MENU;
                storeMessage = "";
            }
            else if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) {
                storeSelection--;
                if (storeSelection < 0) storeSelection = planeNames.length - 1;
                storeMessage = "";
            }
            else if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) {
                storeSelection++;
                if (storeSelection > planeNames.length - 1) storeSelection = 0;
                storeMessage = "";
            }
            else if (key == KeyEvent.VK_ENTER) {
                String selectedName = planeNames[storeSelection];
                int cost = planeCosts[storeSelection];

                int currentHighScore = DatabaseManager.getUserMaxScore(currentUser);

                if (selectedName.equals(activePlane)) {
                    storeMessage = "ALREADY EQUIPPED!";
                    storeMessageTime = System.currentTimeMillis();
                } else if (currentHighScore >= cost) {
                    if (DatabaseManager.equipPlane(currentUser, selectedName, cost)) {
                        activePlane = selectedName;
                        storeMessage = "SHIP UNLOCKED & EQUIPPED!";
                        storeMessageTime = System.currentTimeMillis();
                        if (userSettings.gameoverSfx) SoundManager.playSound(SoundManager.WIN_SOUND);
                    }
                } else {
                    storeMessage = "NEED HIGHER SCORE!";
                    storeMessageTime = System.currentTimeMillis();
                }
            }
        }
        else if (gameState == GameState.PLAYING || gameState == GameState.PAUSED) {
            if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) leftPressed = true;
            if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) rightPressed = true;
            if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) upPressed = true;
            if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) downPressed = true;
            if (key == KeyEvent.VK_SPACE) spacePressed = true;

            if (key == KeyEvent.VK_P) {
                if (gameState == GameState.PLAYING) {
                    gameState = GameState.PAUSED;
                } else if (gameState == GameState.PAUSED) {
                    gameState = GameState.PLAYING;
                }
            }

            if (key == KeyEvent.VK_M) {
                previousState = gameState;
                gameState = GameState.SETTINGS;
            }

            if (key == KeyEvent.VK_ESCAPE) {
                showExitConfirm = true;
            }
        }
        else if (gameState == GameState.SETTINGS) {
            if (key == KeyEvent.VK_ESCAPE) gameState = previousState;
            if (key == KeyEvent.VK_M) {
                userSettings.musicOn = !userSettings.musicOn;
                if (userSettings.musicOn) SoundManager.playMusic(SoundManager.MAIN_THEME);
                else SoundManager.stopMusic();
            }
            if (key == KeyEvent.VK_S) userSettings.shootSfx = !userSettings.shootSfx;
            if (key == KeyEvent.VK_H) userSettings.hitSfx = !userSettings.hitSfx;
            if (key == KeyEvent.VK_G) userSettings.gameoverSfx = !userSettings.gameoverSfx;
            if (currentUser != null) DatabaseManager.updateSettings(currentUser, userSettings);
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