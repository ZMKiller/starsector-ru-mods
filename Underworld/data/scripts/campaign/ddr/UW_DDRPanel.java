package data.scripts.campaign.ddr;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BaseCustomUIPanelPlugin;
import com.fs.starfarer.api.campaign.CustomVisualDialogDelegate.DialogCallbacks;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import java.io.IOException;
import java.util.List;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class UW_DDRPanel extends BaseCustomUIPanelPlugin {

    protected InteractionDialogAPI dialog;
    protected DialogCallbacks callbacks;
    protected CustomPanelAPI panel;
    protected PositionAPI p;
    protected SpriteAPI bg;
    protected boolean practiceMode;

    public UW_DDRPanel(boolean practiceMode) {
        bg = loadTex("graphics/misc/eventide_bg.jpg");

        this.practiceMode = practiceMode;
    }

    static protected SpriteAPI loadTex(String tex) {
        try {
            Global.getSettings().loadTexture(tex);
            return Global.getSettings().getSprite(tex);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void init(CustomPanelAPI panel, DialogCallbacks callbacks, InteractionDialogAPI dialog) {
        this.panel = panel;
        this.callbacks = callbacks;
        this.dialog = dialog;

        if (practiceMode) {
            /* TODO */
        }
    }

    public CustomPanelAPI getPanel() {
        return panel;
    }

    public PositionAPI getPosition() {
        return p;
    }

    @Override
    public void positionChanged(PositionAPI position) {
        this.p = position;
    }

    @Override
    public void render(float alphaMult) {
    }

    @Override
    public void renderBelow(float alphaMult) {
        if (p == null) {
            return;
        }

        float x = p.getX();
        float y = p.getY();
//        float cx = p.getCenterX();
//        float cy = p.getCenterY();
        float w = p.getWidth();
        float h = p.getHeight();

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_SCISSOR_TEST);

        float s = Global.getSettings().getScreenScaleMult();
        GL11.glScissor((int) (x * s), (int) (y * s), (int) (w * s), (int) (h * s));

        bg.render(x, y);

//        for (QuadParticles p : particles) {
//            p.render(alphaMult, true);
//        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    @Override
    public void advance(float amount) {
        if (p == null) {
            return;
        }

//        Iterator<QuadParticles> iter = particles.iterator();
//        while (iter.hasNext()) {
//            QuadParticles curr = iter.next();
//            curr.advance(amount);
//            if (curr.isDone()) {
//                iter.remove();
//            }
//        }
    }

    @Override
    public void processInput(List<InputEventAPI> events) {
        if (p == null) {
            return;
        }

        for (InputEventAPI event : events) {
            if (event.isConsumed()) {
                continue;
            }
            if ((Global.getSettings().isDevMode() || practiceMode)
                    && event.isKeyDownEvent() && (event.getEventValue() == Keyboard.KEY_ESCAPE)) {
                event.consume();
                callbacks.dismissDialog();
                return;
            }

            if (event.isKeyDownEvent()
                    && (event.getEventValue() == Keyboard.KEY_UP
                    || event.getEventValue() == Keyboard.KEY_NUMPAD8
                    || event.getEventValue() == Keyboard.KEY_W)) {
                event.consume();
                /* TODO */
                continue;
            }
            if (event.isKeyDownEvent()
                    && (event.getEventValue() == Keyboard.KEY_LEFT
                    || event.getEventValue() == Keyboard.KEY_NUMPAD4
                    || event.getEventValue() == Keyboard.KEY_A)) {
                event.consume();
                /* TODO */
                continue;
            }
            if (event.isKeyDownEvent()
                    && (event.getEventValue() == Keyboard.KEY_RIGHT
                    || event.getEventValue() == Keyboard.KEY_NUMPAD6
                    || event.getEventValue() == Keyboard.KEY_D)) {
                event.consume();
                /* TODO */
                continue;
            }
            if (event.isKeyDownEvent()
                    && (event.getEventValue() == Keyboard.KEY_DOWN
                    || event.getEventValue() == Keyboard.KEY_NUMPAD2
                    || event.getEventValue() == Keyboard.KEY_S)) {
                event.consume();
                /* TODO */
            }
        }
    }
}
