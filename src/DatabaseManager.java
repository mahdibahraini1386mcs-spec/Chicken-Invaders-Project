import java.sql.*;

public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:game_data.db";

    public static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(URL)) {
            if (conn != null) {
                Statement stmt = conn.createStatement();
                stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                        "username TEXT PRIMARY KEY, " +
                        "password TEXT)");

                stmt.execute("CREATE TABLE IF NOT EXISTS scores (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "username TEXT, " +
                        "score INTEGER, " +
                        "level INTEGER, " +
                        "date TEXT)");

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

    public static boolean register(String username, String password) {
        try (Connection conn = DriverManager.getConnection(URL)) {
            PreparedStatement pstmt = conn.prepareStatement("INSERT INTO users VALUES(?, ?)");
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();

            PreparedStatement stmt = conn.prepareStatement("INSERT INTO settings VALUES(?, 1, 1, 1, 1)");
            stmt.setString(1, username);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
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

    public static void saveScore(String username, int score, int level, UserSettings settings) {
        try (Connection conn = DriverManager.getConnection(URL)) {
            PreparedStatement pstmt = conn.prepareStatement("INSERT INTO scores(username, score, level, date) VALUES(?,?,?,?)");
            pstmt.setString(1, username);
            pstmt.setInt(2, score);
            pstmt.setInt(3, level);
            pstmt.setString(4, new java.util.Date().toString());
            pstmt.executeUpdate();
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
        try (Connection conn = DriverManager.getConnection(URL)) {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM scores ORDER BY score DESC LIMIT " + limit);
            while (rs.next()) {
                scores.add(new ScoreRecord(rs.getString("username"), rs.getInt("score"), rs.getInt("level"), rs.getString("date")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return scores;
    }

    public static class UserSettings {
        public boolean musicOn, shootSfx, hitSfx, gameoverSfx;
        public UserSettings(boolean m, boolean s, boolean h, boolean g) { musicOn = m; shootSfx = s; hitSfx = h; gameoverSfx = g; }
    }

    public static class ScoreRecord {
        public String username, date;
        public int score, level;
        public ScoreRecord(String u, int s, int l, String d) { username = u; score = s; level = l; date = d; }
    }
}