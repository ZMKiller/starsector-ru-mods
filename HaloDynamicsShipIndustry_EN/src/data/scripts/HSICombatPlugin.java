package data.scripts;

import com.fs.starfarer.api.GameState;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.util.Misc;
import data.hullmods.HSITurbulanceShieldListenerV2;
import data.hullmods.HSITurbulanceShieldListenerV2.HSITurbulanceShieldStats;
import data.kit.HSII18nUtil;
import data.shipsystems.scripts.HSIDeepDive;
import org.dark.shaders.util.ShaderLib;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicUI;

import java.awt.*;
import java.util.List;

import static org.magiclib.util.MagicUI.*;

public class HSICombatPlugin extends BaseEveryFrameCombatPlugin {
    private static final float UIscaling = Global.getSettings().getScreenScaleMult();
    @Override
    public void init(CombatEngineAPI engine) {
        if(engine.getContext()!=null&&engine.getContext().getOtherFleet()!=null&& engine.getContext().getOtherFleet().getCommander()!=null
                &&engine.getContext().getOtherFleet().getCommander().getFaction()!=null
                &&engine.getContext().getOtherFleet().getCommander().getFaction().getId().equals("HSI")){
            forceFullAssault =  engine.getContext().getOtherFleet().getMemoryWithoutUpdate().contains("$HSI_FullAssault");
        }
    }
    private boolean forceFullAssault = false;
    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        drawFullUI();
        if(forceFullAssault){
            CombatFleetManagerAPI cfM = Global.getCombatEngine().getFleetManager(FleetSide.ENEMY);
            //cfM.setAdmiralAI(new HSIAdmiralAIPlugin());
            cfM.setCanForceShipsToEngageWhenBattleClearlyLost(true);

            CombatTaskManagerAPI ctM =  Global.getCombatEngine().getFleetManager(FleetSide.ENEMY).getTaskManager(false);
            ctM.setFullAssault(true);
            ctM.setPreventFullRetreat(true);
        }
    }

    protected static Color ENEMY = new Color(253,98,6);
    protected void drawFullUI() {
        if(Global.getCurrentState().equals(GameState.TITLE)) return;
        if(Global.getCombatEngine()!=null&&Global.getCombatEngine().isUIShowingDialog()) return;
        if(Global.getCombatEngine().getPlayerShip() != null
                && HSITurbulanceShieldListenerV2.hasShield(Global.getCombatEngine().getPlayerShip())) {
            ShipAPI ship = Global.getCombatEngine().getPlayerShip();
            HSITurbulanceShieldListenerV2 s = HSITurbulanceShieldListenerV2.getInstance(ship);
            HSITurbulanceShieldStats shield = s.getShield();
            String up = HSII18nUtil.getHullModString("HSITurbulanceShieldSafe");
            if (shield.getShieldLevel() < 0.4)
                up = HSII18nUtil.getHullModString("HSITurbulanceShieldDanger");
            MagicUI.drawHUDStatusBar(ship, shield.getShieldLevel(), null, null, 0f,
                    HSII18nUtil.getHullModString("HSITurbulanceShield0"), up, false);
            MagicUI.drawInterfaceStatusBar(ship, shield.getShieldLevel(), null, null,
                    shield.getExtra() / (shield.getExtra() + shield.getShieldCap()),
                    HSII18nUtil.getHullModString("HSITurbulanceShield0"),
                    (int) (shield.getCurrent() + shield.getExtra()));
        }

        for(ShipAPI ss:Global.getCombatEngine().getShips()){
            if(ss.isFighter()) continue;
            if(!ss.isAlive()) continue;
            if(ss.isPhased()||ss.getCollisionClass().equals(CollisionClass.NONE)||ss.getCustomData()!=null&&ss.getCustomData().containsKey(HSIDeepDive.DEEP_DIVE)) continue;
            if(HSITurbulanceShieldListenerV2.hasShield(ss)) {
                if (ShaderLib.isOnScreen(ss.getLocation(), 3000f)) {
                    HSITurbulanceShieldListenerV2 ssShield = HSITurbulanceShieldListenerV2.getInstance(ss);
                    HSITurbulanceShieldStats shield = ssShield.getShield();
                    String ssup = HSII18nUtil.getHullModString("HSITurbulanceShieldSafe");
                    if (ssShield.getShield().getShieldLevel() < 0.4)
                        ssup = HSII18nUtil.getHullModString("HSITurbulanceShieldDanger");
                    if (ss.getOwner() == 0) {
                        if (ss.isAlly()) {
                            drawHUDStatusBar(ss,ssShield.getShield().getShieldLevel(), Misc.getHighlightColor(),Misc.getHighlightColor(),shield.getExtra() / (shield.getExtra() + shield.getShieldCap()),HSII18nUtil.getHullModString("HSITurbulanceShield0"),ssup,false);
                        } else {
                            drawHUDStatusBar(ss,ssShield.getShield().getShieldLevel(), null,null,shield.getExtra() / (shield.getExtra() + shield.getShieldCap()),HSII18nUtil.getHullModString("HSITurbulanceShield0"),ssup,false);
                        }
                    } else {
                        drawHUDStatusBar(ss,ssShield.getShield().getShieldLevel(), ENEMY,ENEMY,shield.getExtra() / (shield.getExtra() + shield.getShieldCap()),HSII18nUtil.getHullModString("HSITurbulanceShield0"),ssup,false);
                    }
                }
            }
        }

    }

    public static void drawHUDStatusBar(ShipAPI ship, float fill, Color innerColor, Color borderColor, float secondfill, String bottext, String toptext, boolean offset) {
        Vector2f pos = ship.getLocation();
        CombatEngineAPI engine = Global.getCombatEngine();
        if (engine == null || engine.getViewport() == null) {
            return;
        }

        if (Global.getCombatEngine().getCombatUI() == null || Global.getCombatEngine().getCombatUI().isShowingCommandUI() || !Global.getCombatEngine().isUIShowingHUD()) {
            return;
        }

        Vector2f pos2 = new Vector2f((int) Global.getCombatEngine().getViewport().convertWorldXtoScreenX(pos.getX()), (int) Global.getCombatEngine().getViewport().convertWorldYtoScreenY(pos.getY()));
        if (offset) {
            pos2.translate(0f, 16f);
        }
        addHUDStatusBar(ship, fill, innerColor, borderColor, secondfill, pos2);
        if (TODRAW10 != null) {
            if (bottext != null && !bottext.isEmpty()) {
                TODRAW10.setText(bottext);
                int pixelleft = (int) TODRAW10.getWidth();
                pos2.translate(-pixelleft - 5f, 8f);
                addHUDStatusText(ship, innerColor, pos2);
            }
            if (toptext != null && !toptext.isEmpty()) {
                pos2.translate(0, 8f);
                TODRAW10.setText(toptext);
                addHUDStatusText(ship, innerColor, pos2);
            }
        }
    }

    private static void addHUDStatusBar(ShipAPI ship, float fill, Color innerColor, Color borderColor, float secondfill, Vector2f screenPos) {

        final float boxWidth = 59 * UIscaling;
        final float boxHeight = 5 * UIscaling;

        final Vector2f element = getHUDOffset(ship);
        final Vector2f boxLoc = Vector2f.add(new Vector2f(screenPos.getX(), screenPos.getY()), element, null);
        final Vector2f shadowLoc = Vector2f.add(new Vector2f(screenPos.getX() + 1f, screenPos.getY() - 1f), element, null);
        boxLoc.scale(UIscaling);
        shadowLoc.scale(UIscaling);

        // Used to properly interpolate between colors
        float alpha = 1f;
        if (Global.getCombatEngine().isUIShowingDialog()) {
            return;
        }

        Color innerCol = innerColor == null ? GREENCOLOR : innerColor;
        Color borderCol = borderColor == null ? GREENCOLOR : borderColor;
        if (!ship.isAlive()) {
            innerCol = BLUCOLOR;
            borderCol = BLUCOLOR;
        }
        int pixelHardfill = 0;
        float hardfill = secondfill < 0 ? 0 : secondfill;
        hardfill = hardfill > 1 ? 1 : hardfill;
        if (hardfill >= fill) {
            hardfill = fill;
        }
        pixelHardfill = (int) (boxWidth * hardfill);
        pixelHardfill = pixelHardfill <= 3 ? -pixelHardfill : -3;

        int hfboxWidth = (int) (boxWidth * hardfill);
        int fboxWidth = (int) (boxWidth * fill);

        OpenGLBar(ship, alpha, borderCol, innerCol, fboxWidth, hfboxWidth, boxHeight, boxWidth, pixelHardfill, shadowLoc, boxLoc);

    }

    /**
     * Draw text with the font victor10 where you want on the screen.
     *
     * @param ship      The player ship.
     * @param textColor The color of the text
     * @param screenPos The position on the Screen.
     */
    private static void addHUDStatusText(ShipAPI ship, Color textColor, Vector2f screenPos) {
        Color borderCol = textColor == null ? GREENCOLOR : textColor;
        if (!ship.isAlive()) {
            borderCol = BLUCOLOR;
        }
        float alpha = 1f;
        if (Global.getCombatEngine().isUIShowingDialog()) {
            return;
        }
        Color shadowcolor = new Color(Color.BLACK.getRed() / 255f, Color.BLACK.getGreen() / 255f, Color.BLACK.getBlue() / 255f,
                1f - Global.getCombatEngine().getCombatUI().getCommandUIOpacity());
        Color color = new Color(borderCol.getRed() / 255f, borderCol.getGreen() / 255f, borderCol.getBlue() / 255f,
                alpha * (borderCol.getAlpha() / 255f)
                        * (1f - Global.getCombatEngine().getCombatUI().getCommandUIOpacity()));

        final Vector2f boxLoc = Vector2f.add(new Vector2f(screenPos.getX(), screenPos.getY()),
                getHUDOffset(ship), null);
        final Vector2f shadowLoc = Vector2f.add(new Vector2f(screenPos.getX() + 1f, screenPos.getY() - 1f),
                getHUDOffset(ship), null);
        if (UIscaling != 1) {
            boxLoc.scale(UIscaling);
            shadowLoc.scale(UIscaling);
            TODRAW10.setFontSize(10 * UIscaling);
        }

        // Global.getCombatEngine().getViewport().
        openGL11ForText();
        // TODRAW10.setText(text);
        // TODRAW10.setMaxHeight(26);
        TODRAW10.setBaseColor(shadowcolor);
        TODRAW10.draw(shadowLoc);
        TODRAW10.setBaseColor(color);
        TODRAW10.draw(boxLoc);
        closeGL11ForText();
    }

    private static void OpenGLBar(ShipAPI ship, float alpha, Color borderCol, Color innerCol, int fboxWidth, int hfboxWidth, float boxHeight, float boxWidth, int pixelHardfill, Vector2f shadowLoc, Vector2f boxLoc) {
        final int width = (int) (Display.getWidth() * Display.getPixelScaleFactor());
        final int height = (int) (Display.getHeight() * Display.getPixelScaleFactor());

        // Set OpenGL flags
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glViewport(0, 0, width, height);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glOrtho(0, width, 0, height, -1, 1);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glTranslatef(0.01f, 0.01f, 0);

        if (ship.isAlive()) {
            // Render the drop shadow
            if (fboxWidth != 0) {
                GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
                GL11.glColor4f(Color.BLACK.getRed() / 255f, Color.BLACK.getGreen() / 255f, Color.BLACK.getBlue() / 255f,
                        1f - Global.getCombatEngine().getCombatUI().getCommandUIOpacity());
                GL11.glVertex2f(shadowLoc.x - 1, shadowLoc.y);
                GL11.glVertex2f(shadowLoc.x + fboxWidth, shadowLoc.y);
                GL11.glVertex2f(shadowLoc.x - 1, shadowLoc.y + boxHeight + 1);
                GL11.glVertex2f(shadowLoc.x + fboxWidth, shadowLoc.y + boxHeight + 1);
                GL11.glEnd();
            }
        }

        // Render the drop shadow of border.
        GL11.glBegin(GL11.GL_LINES);
        GL11.glColor4f(Color.BLACK.getRed() / 255f, Color.BLACK.getGreen() / 255f, Color.BLACK.getBlue() / 255f,
                1f - Global.getCombatEngine().getCombatUI().getCommandUIOpacity());
        GL11.glVertex2f(shadowLoc.x + hfboxWidth + pixelHardfill, shadowLoc.y - 1);
        GL11.glVertex2f(shadowLoc.x + 3 + hfboxWidth + pixelHardfill, shadowLoc.y - 1);
        GL11.glVertex2f(shadowLoc.x + hfboxWidth + pixelHardfill, shadowLoc.y + boxHeight);
        GL11.glVertex2f(shadowLoc.x + 3 + hfboxWidth + pixelHardfill, shadowLoc.y + boxHeight);
        GL11.glVertex2f(shadowLoc.x + boxWidth, shadowLoc.y);
        GL11.glVertex2f(shadowLoc.x + boxWidth, shadowLoc.y + boxHeight);

        // Render the border transparency fix
        GL11.glColor4f(Color.BLACK.getRed() / 255f, Color.BLACK.getGreen() / 255f, Color.BLACK.getBlue() / 255f,
                1f - Global.getCombatEngine().getCombatUI().getCommandUIOpacity());
        GL11.glVertex2f(boxLoc.x + hfboxWidth + pixelHardfill, boxLoc.y - 1);
        GL11.glVertex2f(boxLoc.x + 3 + hfboxWidth + pixelHardfill, boxLoc.y - 1);
        GL11.glVertex2f(boxLoc.x + hfboxWidth + pixelHardfill, boxLoc.y + boxHeight);
        GL11.glVertex2f(boxLoc.x + 3 + hfboxWidth + pixelHardfill, boxLoc.y + boxHeight);
        GL11.glVertex2f(boxLoc.x + boxWidth, boxLoc.y);
        GL11.glVertex2f(boxLoc.x + boxWidth, boxLoc.y + boxHeight);

        // Render the border
        GL11.glColor4f(borderCol.getRed() / 255f, borderCol.getGreen() / 255f, borderCol.getBlue() / 255f,
                alpha * (1 - Global.getCombatEngine().getCombatUI().getCommandUIOpacity()));
        GL11.glVertex2f(boxLoc.x + hfboxWidth + pixelHardfill, boxLoc.y - 1);
        GL11.glVertex2f(boxLoc.x + 3 + hfboxWidth + pixelHardfill, boxLoc.y - 1);
        GL11.glVertex2f(boxLoc.x + hfboxWidth + pixelHardfill, boxLoc.y + boxHeight);
        GL11.glVertex2f(boxLoc.x + 3 + hfboxWidth + pixelHardfill, boxLoc.y + boxHeight);
        GL11.glVertex2f(boxLoc.x + boxWidth, boxLoc.y);
        GL11.glVertex2f(boxLoc.x + boxWidth, boxLoc.y + boxHeight);
        GL11.glEnd();

        // Render the fill element
        if (ship.isAlive()) {
            GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
            GL11.glColor4f(innerCol.getRed() / 255f, innerCol.getGreen() / 255f, innerCol.getBlue() / 255f,
                    alpha * (innerCol.getAlpha() / 255f)
                            * (1f - Global.getCombatEngine().getCombatUI().getCommandUIOpacity()));
            GL11.glVertex2f(boxLoc.x, boxLoc.y);
            GL11.glVertex2f(boxLoc.x + fboxWidth, boxLoc.y);
            GL11.glVertex2f(boxLoc.x, boxLoc.y + boxHeight);
            GL11.glVertex2f(boxLoc.x + fboxWidth, boxLoc.y + boxHeight);
            GL11.glEnd();
        }
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        GL11.glPopAttrib();

    }

    /**
     * Get the UI Element Offset for the player on the center. (Depends of the
     * collision radius and the zoom)
     *
     * @param ship The player ship.
     * @return The offset.
     */
    private static Vector2f getHUDOffset(ShipAPI ship) {
        ViewportAPI viewport = Global.getCombatEngine().getViewport();
        float mult = viewport.getViewMult();

        return new Vector2f((int) (-ship.getCollisionRadius() / mult),
                (int) (ship.getCollisionRadius() / mult));
    }
}
