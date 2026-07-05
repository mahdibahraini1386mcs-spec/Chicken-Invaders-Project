import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;

public class DatabaseManager {

    private static final String URL = "jdbc:sqlite:game.db";

    public static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {

            String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                    "username TEXT PRIMARY KEY," +
                    "password TEXT NOT NULL," +
                    "high_score INTEGER DEFAULT 0," +
                    "last_level INTEGER DEFAULT 1," +
                    "sound_music BOOLEAN DEFAULT 1," +
                    "sound_shot BOOLEAN DEFAULT 1," +
                    "sound_crash BOOLEAN DEFAULT 1," +
                    "sound_gameover BOOLEAN DEFAULT 1" +
                    ");";
            stmt.execute(createUsersTable);

           
            String createHistoryTable = "CREATE TABLE IF NOT EXISTS game_history (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "username TEXT," +
                    "score INTEGER," +
                    "level INTEGER," +
                    "play_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                    "FOREIGN KEY(username) REFERENCES users(username)" +
                    ");";
            stmt.execute(createHistoryTable);

            System.out.println("Database tables checked/created successfully.");

        } catch (SQLException e) {
            System.out.println("Database connection error: " + e.getMessage());
        }
    }
}