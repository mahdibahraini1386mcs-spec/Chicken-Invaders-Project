import java.io.*;

public class ScoreManager {
    private static final String FILE_PATH = "resources/highscore.txt";

    public static int loadHighScore() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            return Integer.parseInt(reader.readLine());
        } catch (Exception e) {
            return 0;
        }
    }

    public static void saveHighScore(int score) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            writer.write(String.valueOf(score));
        } catch (IOException e) {
        }
    }
}