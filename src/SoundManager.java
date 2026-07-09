import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class SoundManager {
    private static boolean musicEnabled = true;
    private static boolean sfxEnabled = true;
    private static Clip backgroundMusic;

    private static File getAudioFile(String fileName) {
        // گشتن به دنبال پوشه در مسیرهای احتمالی
        File dir = new File("sounds/sound-effects");
        if (!dir.exists()) dir = new File("src/sounds/sound-effects");
        if (!dir.exists()) dir = new File("sound-effects");

        if (dir.exists()) {
            File file = new File(dir, fileName);
            if (file.exists()) {
                return file; // فایل با موفقیت پیدا شد
            } else {
                // چاپ پیام‌های خطایابی در کنسول
                System.out.println("⚠️ فایل با این اسم دقیق پیدا نشد: " + fileName);
                System.out.println("📂 اما من پوشه صداها را پیدا کردم! لیست دقیق فایل‌های داخلش این‌هاست:");
                String[] files = dir.list();
                if (files != null) {
                    for (String f : files) {
                        System.out.println("   -> " + f);
                    }
                }
                System.out.println("--------------------------------------------------");
            }
        } else {
            System.out.println("⚠️ کلاً پوشه صداها (sounds/sound-effects) پیدا نشد!");
            System.out.println("مسیر فعلی که جاوا در حال جستجو است: " + System.getProperty("user.dir"));
        }
        return null;
    }

    public static void playSound(String fileName) {
        if (!sfxEnabled) return;
        try {
            File soundFile = getAudioFile(fileName);
            if (soundFile != null) {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                clip.start();
            }
        } catch (Exception e) {
            System.out.println("❌ ارور در پخش افکت (احتمالاً فرمت فایل واقعاً WAV نیست): " + fileName);
            e.printStackTrace();
        }
    }

    public static void playMusic(String fileName) {
        if (!musicEnabled) return;
        try {
            if (backgroundMusic != null && backgroundMusic.isRunning()) {
                backgroundMusic.stop();
            }
            File musicFile = getAudioFile(fileName);
            if (musicFile != null) {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(musicFile);
                backgroundMusic = AudioSystem.getClip();
                backgroundMusic.open(audioIn);
                backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
            }
        } catch (Exception e) {
            System.out.println("❌ ارور در پخش آهنگ (احتمالاً فرمت فایل واقعاً WAV نیست): " + fileName);
            e.printStackTrace();
        }
    }

    public static void toggleMusic() {
        musicEnabled = !musicEnabled;
        if (!musicEnabled && backgroundMusic != null) {
            backgroundMusic.stop();
        } else if (musicEnabled && backgroundMusic != null) {
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public static void toggleSFX() {
        sfxEnabled = !sfxEnabled;
    }

    public static boolean getMusicStatus() { return musicEnabled; }
    public static boolean getSFXStatus() { return sfxEnabled; }
}