import javax.sound.sampled.*;
import java.io.File;

public class SoundManager {
    private static boolean sfxEnabled = true;
    private static boolean musicEnabled = true;
    private static Clip musicClip;

    public static void playSound(String fileName) {
        if (!sfxEnabled) return;
        try {
            File soundFile = new File("sounds/" + fileName);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (Exception e) {
            System.err.println("Error playing sound: " + fileName);
            e.printStackTrace();
        }
    }

    public static void playMusic(String fileName) {
        if (musicClip != null && musicClip.isRunning()) musicClip.stop();
        if (!musicEnabled) return;
        try {
            File soundFile = new File("sounds/" + fileName);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
            musicClip = AudioSystem.getClip();
            musicClip.open(audioIn);
            musicClip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {
            System.err.println("Error playing music: " + fileName);
            e.printStackTrace();
        }
    }

    public static void toggleSFX() {
        sfxEnabled = !sfxEnabled;
    }

    public static void toggleMusic() {
        musicEnabled = !musicEnabled;
        if (!musicEnabled && musicClip != null) {
            musicClip.stop();
        } else if (musicEnabled && musicClip != null) {
            musicClip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    public static boolean getSFXStatus() {
        return sfxEnabled;
    }

    public static boolean getMusicStatus() {
        return musicEnabled;
    }
}