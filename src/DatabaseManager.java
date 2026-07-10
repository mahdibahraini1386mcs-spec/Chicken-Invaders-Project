import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:game.db";


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
}