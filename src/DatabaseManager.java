import java.sql.*;

public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:game_data.db";

    // ===== متد initializeDatabase =====
    public static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(URL)) {
            if (conn != null) {
                Statement stmt = conn.createStatement();

                stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                        "username TEXT PRIMARY KEY, " +
                        "password TEXT, " +
                        "highscore INTEGER DEFAULT 0, " +
                        "active_plane TEXT DEFAULT 'Default')");

                stmt.execute("CREATE TABLE IF NOT EXISTS scores (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "username TEXT, " +
                        "score INTEGER, " +
                        "level INTEGER, " +
                        "date TEXT, " +
                        "musicOn INTEGER, " +
                        "shootSfx INTEGER, " +
                        "hitSfx INTEGER, " +
                        "gameoverSfx INTEGER)");

                stmt.execute("CREATE TABLE IF NOT EXISTS settings (" +
                        "username TEXT PRIMARY KEY, " +
                        "musicOn INTEGER, " +
                        "shootSfx INTEGER, " +
                        "hitSfx INTEGER, " +
                        "gameoverSfx INTEGER)");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ===== متد register =====
    public static boolean register(String username, String password) {
        String insertUser = "INSERT INTO users(username, password, highscore, active_plane) VALUES(?, ?, 0, 'Default')";
        String insertSettings = "INSERT INTO settings(username, musicOn, shootSfx, hitSfx, gameoverSfx) VALUES(?, 1, 1, 1, 1)";

        try (Connection conn = DriverManager.getConnection(URL)) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt1 = conn.prepareStatement(insertUser);
                 PreparedStatement pstmt2 = conn.prepareStatement(insertSettings)) {

                pstmt1.setString(1, username);
                pstmt1.setString(2, password);
                pstmt1.executeUpdate();

                pstmt2.setString(1, username);
                pstmt2.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException ex) {
                conn.rollback();
                System.err.println("Database Registration Error: " + ex.getMessage());
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean login(String username, String password) {
        try (Connection conn = DriverManager.getConnection(URL)) {
            PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM users WHERE username = ? AND password = ?");
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    // ===== متد saveScore =====
    public static void saveScore(String username, int score, int level, UserSettings settings) {
        if (username == null) return;

        // 1. ذخیره رکورد امتیاز در جدول scores
        try (Connection conn = DriverManager.getConnection(URL)) {
            PreparedStatement pstmt = conn.prepareStatement(
                    "INSERT INTO scores(username, score, level, date, musicOn, shootSfx, hitSfx, gameoverSfx) VALUES(?,?,?,?,?,?,?,?)");
            pstmt.setString(1, username);
            pstmt.setInt(2, score);
            pstmt.setInt(3, level);
            pstmt.setString(4, new java.util.Date().toString());
            pstmt.setBoolean(5, settings.musicOn);
            pstmt.setBoolean(6, settings.shootSfx);
            pstmt.setBoolean(7, settings.hitSfx);
            pstmt.setBoolean(8, settings.gameoverSfx);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // 2. به‌روزرسانی highscore در جدول users (فقط در صورت بیشتر بودن)
        String getQuery = "SELECT highscore FROM users WHERE username = ?";
        String updateQuery = "UPDATE users SET highscore = ? WHERE username = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement getStmt = conn.prepareStatement(getQuery)) {
            getStmt.setString(1, username);
            ResultSet rs = getStmt.executeQuery();
            if (rs.next()) {
                int currentHigh = rs.getInt("highscore");
                if (score > currentHigh) {
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                        updateStmt.setInt(1, score);
                        updateStmt.setString(2, username);
                        updateStmt.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static UserSettings getUserSettings(String username) {
        try (Connection conn = DriverManager.getConnection(URL)) {
            PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM settings WHERE username = ?");
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new UserSettings(rs.getBoolean("musicOn"), rs.getBoolean("shootSfx"),
                        rs.getBoolean("hitSfx"), rs.getBoolean("gameoverSfx"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new UserSettings(true, true, true, true);
    }

    public static void updateSettings(String username, UserSettings settings) {
        try (Connection conn = DriverManager.getConnection(URL)) {
            PreparedStatement pstmt = conn.prepareStatement("UPDATE settings SET musicOn=?, shootSfx=?, hitSfx=?, gameoverSfx=? WHERE username=?");
            pstmt.setBoolean(1, settings.musicOn);
            pstmt.setBoolean(2, settings.shootSfx);
            pstmt.setBoolean(3, settings.hitSfx);
            pstmt.setBoolean(4, settings.gameoverSfx);
            pstmt.setString(5, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static java.util.List<ScoreRecord> getTopScores(int limit) {
        java.util.List<ScoreRecord> scores = new java.util.ArrayList<>();
        String query =
                "SELECT s.username, s.score, s.level, s.date FROM scores s " +
                        "INNER JOIN (SELECT username, MAX(score) AS maxScore FROM scores GROUP BY username) m " +
                        "ON s.username = m.username AND s.score = m.maxScore " +
                        "GROUP BY s.username " +
                        "ORDER BY s.score DESC LIMIT " + limit;
        try (Connection conn = DriverManager.getConnection(URL)) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                scores.add(new ScoreRecord(rs.getString("username"), rs.getInt("score"), rs.getInt("level"), rs.getString("date")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return scores;
    }

    // ===== متدهای جدید فروشگاه (با قلب تپنده دیتابیس) =====

    // ۱. گرفتن بالاترین امتیاز مستقیماً از جدول رکوردها
    public static int getUserMaxScore(String username) {
        int max = 0;
        // ⚠️ بسیار مهم: فرض کردیم اسم جدولی که رکوردهای بازی در آن ذخیره می‌شود "scores" است.
        String query = "SELECT MAX(score) AS maxScore FROM scores WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                max = rs.getInt("maxScore");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return max;
    }

    // ۲. گرفتن نام سفینه فعال کاربر
    public static String getActivePlane(String username) {
        String plane = "Default";
        if (username == null) return plane;
        String query = "SELECT active_plane FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                plane = rs.getString("active_plane");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return plane;
    }

    // ۳. لاجیک تجهیز کردن (باز کردن قفل سفینه)
    public static boolean equipPlane(String username, String planeName, int cost) {
        int currentHighScore = getUserMaxScore(username); // خواندن امتیاز واقعی

        if (currentHighScore < cost) return false; // امتیاز هنوز به حد نصاب نرسیده

        String updateQuery = "UPDATE users SET active_plane = ? WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(updateQuery)) {
            pstmt.setString(1, planeName);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ===== کلاس‌های داخلی =====
    public static class UserSettings {
        public boolean musicOn, shootSfx, hitSfx, gameoverSfx;
        public UserSettings(boolean m, boolean s, boolean h, boolean g) {
            musicOn = m;
            shootSfx = s;
            hitSfx = h;
            gameoverSfx = g;
        }
    }

    public static class ScoreRecord {
        public String username, date;
        public int score, level;
        public ScoreRecord(String u, int s, int l, String d) {
            username = u;
            score = s;
            level = l;
            date = d;
        }
    }
}