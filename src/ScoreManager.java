import java.io.*;

public class ScoreManager {
    private static final String FILE_PATH = "resources/savegame.txt";

    public static int coins = 0;
    public static int selectedPlane = 0; // 0:Default, 1:Fast, 2:Heavy, 3:Sniper

    public static void load() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            coins = Integer.parseInt(reader.readLine());
            selectedPlane = Integer.parseInt(reader.readLine());
        } catch (Exception e) {
            coins = 0;
            selectedPlane = 0;
        }
    }

    public static void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH))) {
            writer.write(coins + "\n");
            writer.write(selectedPlane + "\n");
        } catch (IOException e) {}
    }
}