package data.kit;

import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.RippleDistortion;
import org.lwjgl.util.vector.Vector2f;

public class HSIVisualUtils {
    //from tdb
    public static void easyRippleOut(Vector2f location, Vector2f velocity, float size, float intensity, float fadesize, float frameRate) {
        if (intensity == -1f) {
            intensity = size / 3f;
        }
        if (velocity == null) {
            velocity = new Vector2f();
        }
        RippleDistortion ripple = new RippleDistortion(location, velocity);
        ripple.setSize(size);
        ripple.setIntensity(intensity);
        ripple.setFrameRate(frameRate);
        ripple.fadeInSize(fadesize);
        ripple.fadeOutIntensity(fadesize);

        DistortionShader.addDistortion(ripple);
    }
}
