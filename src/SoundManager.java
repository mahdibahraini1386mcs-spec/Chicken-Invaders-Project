import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class SoundManager {


    public static final String MAIN_THEME = "sounds/sound-effects/Chicken Invaders 2 Remastered OST - Main Theme.wav";
    public static final String ENDING_THEME = "sounds/sound-effects/Chicken Invaders 2 Remastered OST - Ending Theme.wav";
    public static final String EXPLOSION = "sounds/sound-effects/mixkit-epic-impact-afar-explosion-2782.wav";
    public static final String GAME_OVER = "sounds/sound-effects/mixkit-retro-arcade-game-over-470.wav";
    public static final String LASER = "sounds/sound-effects/mixkit-short-laser-gun-shot-1670.wav";

    private static Clip backgroundMusic;


    public static void playSound(String filePath) {
        try {
            File soundFile = new File(filePath);
            if (soundFile.exists()) {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundFile);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                clip.start();
            } else {
                System.out.println("Error: Sound file not found at " + filePath);
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }


    public static void playMusic(String filePath) {
        try {
            if (backgroundMusic != null && backgroundMusic.isRunning()) {
                backgroundMusic.stop();
            }
            File musicFile = new File(filePath);
            if (musicFile.exists()) {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(musicFile);
                backgroundMusic = AudioSystem.getClip();
                backgroundMusic.open(audioIn);
                backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
            } else {
                System.out.println("Error: Music file not found at " + filePath);
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }


    public static void stopMusic() {
        if (backgroundMusic != null && backgroundMusic.isRunning()) {
            backgroundMusic.stop();
        }
    }
}