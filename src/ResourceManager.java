import javax.imageio.ImageIO;
import java.awt.Image;
import java.io.File;
import java.io.IOException;

public class ResourceManager {

    public static Image loadImage(String folderName, String fileName) {
        String path1 = "resources/images/" + folderName + "/" + fileName;
        String path2 = "src/resources/images/" + folderName + "/" + fileName;
        String path3 = "images/" + folderName + "/" + fileName;

        File file1 = new File(path1);
        File file2 = new File(path2);
        File file3 = new File(path3);

        try {
            if (file1.exists()) {
                return ImageIO.read(file1);
            } else if (file2.exists()) {
                return ImageIO.read(file2);
            } else if (file3.exists()) {
                return ImageIO.read(file3);
            } else {
                System.err.println("Cannot find file! Looked in absolute path: " + file1.getAbsolutePath());
                return null;
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return null;
        }
    }
}