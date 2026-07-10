import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:game.db";

    public static class ScoreRecord {
        public String username;
        public int score;
        public int level;
        public String date;
        public ScoreRecord(String u, int s, int l, String d) { username = u; score = s; level = l; date = d; }
    }

    public static class UserSettings {
        public boolean musicOn, shootSfx, hitSfx, gameoverSfx;
        public UserSettings(boolean m, boolean s, boolean h, boolean g) { musicOn = m; shootSfx = s; hitSfx = h; gameoverSfx = g; }
    }

    public static void initDB() {
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {

            String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                    "username TEXT PRIMARY KEY," +
                    "password TEXT NOT NULL," +
                    "high_score INTEGER DEFAULT 0," +
                    "last_level INTEGER DEFAULT 1," +
                    "music_on BOOLEAN DEFAULT 1," +
                    "shoot_sfx BOOLEAN DEFAULT 1," +
                    "hit_sfx BOOLEAN DEFAULT 1," +
                    "gameover_sfx BOOLEAN DEFAULT 1" +
                    ")";
            stmt.execute(createUsersTable);

            String createScoresTable = "CREATE TABLE IF NOT EXISTS scores (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT," +
                    "score INTEGER," +
                    "level INTEGER," +
                    "play_date DATE DEFAULT (date('now','localtime'))," +
                    "FOREIGN KEY(username) REFERENCES users(username)" +
                    ")";
            stmt.execute(createScoresTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean register(String username, String password) {
        String sql = "INSERT INTO users(username, password) VALUES(?, ?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public static boolean login(String username, String password) {
        String sql = "SELECT password FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("password").equals(password);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static UserSettings getUserSettings(String username) {
        String sql = "SELECT music_on, shoot_sfx, hit_sfx, gameover_sfx FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new UserSettings(rs.getBoolean("music_on"), rs.getBoolean("shoot_sfx"),
                        rs.getBoolean("hit_sfx"), rs.getBoolean("gameover_sfx"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return new UserSettings(true, true, true, true);
    }

    public static void updateSettings(String username, UserSettings settings) {
        String sql = "UPDATE users SET music_on=?, shoot_sfx=?, hit_sfx=?, gameover_sfx=? WHERE username=?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setBoolean(1, settings.musicOn);
            pstmt.setBoolean(2, settings.shootSfx);
            pstmt.setBoolean(3, settings.hitSfx);
            pstmt.setBoolean(4, settings.gameoverSfx);
            pstmt.setString(5, username);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static void saveScore(String username, int score, int level) {
        if (username == null) return;
        String insertSql = "INSERT INTO scores(username, score, level) VALUES(?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
            pstmt.setString(1, username);
            pstmt.setInt(2, score);
            pstmt.setInt(3, level);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }

        String updateSql = "UPDATE users SET high_score = ?, last_level = ? WHERE username = ? AND high_score < ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
            pstmt.setInt(1, score);
            pstmt.setInt(2, level);
            pstmt.setString(3, username);
            pstmt.setInt(4, score);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static List<ScoreRecord> getTopScores(int limit) {
        List<ScoreRecord> list = new ArrayList<>();
        String sql = "SELECT username, score, level, play_date FROM scores ORDER BY score DESC LIMIT ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new ScoreRecord(rs.getString("username"), rs.getInt("score"), rs.getInt("level"), rs.getString("play_date")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}