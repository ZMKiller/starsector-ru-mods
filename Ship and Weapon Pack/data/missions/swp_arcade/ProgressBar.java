package data.missions.swp_arcade;

import com.fs.starfarer.api.Global;
import java.awt.Color;
import java.util.List;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

public class ProgressBar {

    private static final Color BACKGROUND_COLOR = new Color(50, 50, 50, 255);
    private static final Color BORDER_COLOR = new Color(96, 96, 96, 255);
    private static final Color NOTCH1_COLOR = new Color(0, 0, 0, 255);
    private static final Color NOTCH2_COLOR = new Color(191, 191, 191, 255);
    private static final Color PROGRESS_COLOR = new Color(255, 204, 0, 255);

    private static void glColor(Color color, float alphaMult) {
        GL11.glColor4ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue(), (byte) (color.getAlpha() * alphaMult));
    }

    private final float height;
    private final List<Float> notches;
    private final Vector2f position;
    private float progress;
    private final float width;

    public ProgressBar(float progress, List<Float> notches, Vector2f position, float height, float width) {
        this.progress = progress;
        this.notches = notches;
        this.position = new Vector2f(position);
        this.position.y = Global.getSettings().getScreenHeight() - this.position.y;
        this.height = height;
        this.width = width;
    }

    public void setProgress(float progress) {
        this.progress = progress;
    }

    public void render() {
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        final int w = (int) (Display.getWidth() * Display.getPixelScaleFactor()), h = (int) (Display.getHeight() * Display.getPixelScaleFactor());
        GL11.glViewport(0, 0, w, h);

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glOrtho(0, Global.getSettings().getScreenWidth(), 0, Global.getSettings().getScreenHeight(), -1, 1);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glTranslatef(0.01f, 0.01f, 0);

        GL11.glBegin(GL11.GL_QUADS);
        glColor(BACKGROUND_COLOR, 0.5f);
        GL11.glVertex2f(position.x - width * 0.5f, position.y + height * 0.5f);
        GL11.glVertex2f(position.x + width * 0.5f, position.y + height * 0.5f);
        GL11.glVertex2f(position.x + width * 0.5f, position.y - height * 0.5f);
        GL11.glVertex2f(position.x - width * 0.5f, position.y - height * 0.5f);
        glColor(PROGRESS_COLOR, 0.75f);
        GL11.glVertex2f(position.x - width * 0.5f, position.y + height * 0.5f - height * (1f - progress));
        GL11.glVertex2f(position.x + width * 0.5f, position.y + height * 0.5f - height * (1f - progress));
        GL11.glVertex2f(position.x + width * 0.5f, position.y - height * 0.5f);
        GL11.glVertex2f(position.x - width * 0.5f, position.y - height * 0.5f);
        GL11.glEnd();
        GL11.glBegin(GL11.GL_LINE_LOOP);
        GL11.glLineWidth(4f);
        glColor(BORDER_COLOR, 1f);
        GL11.glVertex2f(position.x - width * 0.5f, position.y + height * 0.5f);
        GL11.glVertex2f(position.x + width * 0.5f, position.y + height * 0.5f);
        GL11.glVertex2f(position.x + width * 0.5f, position.y - height * 0.5f);
        GL11.glVertex2f(position.x - width * 0.5f, position.y - height * 0.5f);
        GL11.glEnd();
        GL11.glBegin(GL11.GL_LINES);
        for (Float notch : notches) {
            if (progress >= notch) {
                glColor(NOTCH1_COLOR, 0.75f);
            } else {
                glColor(NOTCH2_COLOR, 0.75f);
            }
            GL11.glVertex2f(position.x - (float) Math.floor(width * 0.25f), position.y + height * 0.5f - height * (1f - notch));
            GL11.glVertex2f(position.x + (float) Math.ceil(width * 0.25f), position.y + height * 0.5f - height * (1f - notch));
        }
        GL11.glEnd();

        GL11.glDisable(GL11.GL_BLEND);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }
}
