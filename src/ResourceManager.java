import javax.imageio.ImageIO;
import java.awt.Image;
import java.io.File;
import java.io.IOException;

public class ResourceManager {

    public static Image loadImage(String folderName, String fileName) {
        String path = "resources/images/" + folderName + "/" + fileName;
        try {
            return ImageIO.read(new File(path));
        } catch (IOException e) {
            System.err.println("Error loading image from path: " + path);
            return null;
        }
    }
}