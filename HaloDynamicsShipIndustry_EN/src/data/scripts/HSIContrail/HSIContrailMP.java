package data.scripts.HSIContrail;

import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.util.FaderUtil;

public class HSIContrailMP {
    private Vector2f location;
    private Vector2f velocity;
    private FaderUtil fader;
    private Color color;

    public HSIContrailMP(Vector2f location,Vector2f velocity,float dur,Color color){
        this.location = new Vector2f(location);
        this.velocity = new Vector2f(velocity);
        fader = new FaderUtil(0.2f, dur/8f,dur);
        fader.fadeIn();
        fader.setBounceDown(true);
        this.color = color;
    }

    public void advance(float amount){
        fader.advance(amount);
        location = Vector2f.add(location, getVelForAdvance(amount), location);
    }

    protected Vector2f getVelForAdvance(float amount){
        return (Vector2f)(new Vector2f(velocity).scale(amount/velocity.length()));
    }

    public boolean isExpired(){
        return fader.isFadedOut();
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setFader(FaderUtil fader) {
        this.fader = fader;
    }

    public void setLocation(Vector2f location) {
        this.location = location;
    }

    public void setVelocity(Vector2f velocity) {
        this.velocity = velocity;
    }

    public Color getColor() {
        return color;
    }

    public FaderUtil getFader() {
        return fader;
    }

    public Vector2f getLocation() {
        return location;
    }

    public Vector2f getVelocity() {
        return velocity;
    }

    public float getOpacity(){
        return fader.getBrightness();
    }
}
