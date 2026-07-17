import javax.imageio.ImageIO;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class ResourceManager {

    public static Image loadImage(String folderName, String fileName) {
        String baseRelativePath = folderName + "/" + fileName;

        try {
            // ۱. روش استاندارد (برای زمانی که از پروژه خروجی JAR می‌گیرید)
            // این روش داخل فایل‌های فشرده کلاس‌پث را جستجو می‌کند
            String[] resourcePaths = {
                    "/images/" + baseRelativePath,
                    "/resources/images/" + baseRelativePath,
                    "/" + baseRelativePath
            };

            for (String resPath : resourcePaths) {
                URL imgURL = ResourceManager.class.getResource(resPath);
                if (imgURL != null) {
                    return ImageIO.read(imgURL);
                }
            }

            // ۲. روش کلاسیک فایل سیستم (برای سازگاری با تنظیمات فعلی IDE شما)
            String[] filePaths = {
                    "resources/images/" + baseRelativePath,
                    "src/resources/images/" + baseRelativePath,
                    "images/" + baseRelativePath
            };

            for (String path : filePaths) {
                File file = new File(path);
                if (file.exists()) {
                    return ImageIO.read(file);
                }
            }

            // اگر به هیچ روشی پیدا نشد
            System.err.println("Error: Cannot find image file anywhere! Looked for: " + fileName);
            return null;

        } catch (IOException e) {
            System.err.println("Error reading file (" + fileName + "): " + e.getMessage());
            return null;
        }
    }
}