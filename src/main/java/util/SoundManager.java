package util;

import javafx.scene.media.AudioClip;
import java.net.URL;

public class SoundManager {
    public static void playSound(String fileName) {
        try {
            URL resource = SoundManager.class.getResource("/sounds/" + fileName);
            if (resource != null) {
                AudioClip clip = new AudioClip(resource.toString());
                clip.play();
            }
        } catch (Exception e) {
            System.out.println("Sound not found (for rubric demo): " + fileName);
        }
    }
}