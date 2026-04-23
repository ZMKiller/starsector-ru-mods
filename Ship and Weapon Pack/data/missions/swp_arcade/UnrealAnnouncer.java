package data.missions.swp_arcade;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.SoundPlayerAPI;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import java.awt.Color;
import java.util.List;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

public class UnrealAnnouncer extends BaseEveryFrameCombatPlugin {

    private static final Color BACKGROUND_COLOR = new Color(0, 0, 0, 255);
    private static final Color HIGH_COLOR = new Color(160, 255, 0, 255);
    private static final Color LOW_COLOR = new Color(255, 0, 0, 255);
    private static final Color MID_COLOR = new Color(255, 255, 0, 255);
    private static final Color WARP_JITTER_COLOR = new Color(0, 255, 255, 255);

    private static boolean comboOn = false;
    private static float comboTimer = 0f;
    private static int deathCount = 0;
    private static final float height = 8f;
    private static final Vector2f position = new Vector2f(Global.getSettings().getScreenWidth() / 2f, 5f);
    private static int spreeKills = 0;
    private static float timerCap = 1f;
    private static final float width = Global.getSettings().getScreenHeight() / 4.5f - 20f;

    public static void addKill(float degree, int type) {
        if (deathCount <= 1) {
            comboTimer = 20f;
            comboOn = true;
        }

        float overkillPrevention = 1f / (1f + deathCount / 20f);
        float timeBonus = (float) Math.pow(degree, 0.75);
        switch (type) {
            case 1:
                comboTimer += 1.5f * timeBonus * overkillPrevention;
                break;
            case 2:
                comboTimer += 1f * timeBonus * overkillPrevention;
                break;
            case 3:
                comboTimer += 2.5f * timeBonus * overkillPrevention;
                break;
            case 4:
                comboTimer += 1f * timeBonus * overkillPrevention;
                break;
            case 99:
                comboTimer += 1.5f * timeBonus * overkillPrevention;
                break;
            default:
                comboTimer += 0.5f * timeBonus * overkillPrevention;
                break;
        }

        switch (type) {
            case 0:
                deathCount += 1;
                break;
            case 1:
                deathCount += 2;
                break;
            case 2:
                deathCount += 2;
                break;
            case 3:
                deathCount += 3;
                break;
            case 4:
                deathCount += 2;
                break;
            case 99:
                deathCount += 4;
                break;
            default:
                deathCount++;
                break;
        }
    }

    public static int getComboMulti() {
        return deathCount;
    }

    private static float getBaseWarpMult(int comboMultiplier, float factor) {
        return 1f / (1f + (comboMultiplier - 1) * factor);
    }

    private static float getWarpMult(int comboMultiplier, float factor, float timer) {
        float baseTimeMult = getBaseWarpMult(comboMultiplier, factor);
        return 1f / (1f + (comboMultiplier - 1) * (factor * Math.max(Math.min(timer / (5f * baseTimeMult), 1f), 0f)));
    }

    private static void glColor(Color color, float alphaMult) {
        GL11.glColor4ub((byte) color.getRed(), (byte) color.getGreen(), (byte) color.getBlue(),
                (byte) (color.getAlpha() * alphaMult));
    }

    private static void resetKills() {
        deathCount = 0;
        spreeKills = 0;
        comboTimer = 0f;
        comboOn = false;
    }

    private CombatEngineAPI engine;

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        float playerTimeMult = getWarpMult(deathCount, 0.125f, comboTimer);
        float playerShotTimeMult = getWarpMult(deathCount, 0.1f, comboTimer);
        float bossTimeMult = getWarpMult(deathCount, 0.075f, comboTimer);
        if (!engine.isPaused()) {
            if (!comboOn && deathCount > 0) {
                comboOn = true;
                comboTimer = 20f;
            }

            if (comboOn) {
                timerCap = Math.max(timerCap, comboTimer);
                comboTimer -= amount / playerTimeMult;
                engine.getPlayerShip().setCurrentCR(1f);
                if (deathCount > 1) {
                    engine.getPlayerShip().setJitterUnder(this, WARP_JITTER_COLOR, 0.2f / playerTimeMult, Math.round(
                            5f / playerTimeMult),
                            0.5f / playerTimeMult,
                            3f / playerTimeMult);
                    engine.getTimeMult().modifyMult("wombo_combo", playerTimeMult);
                    engine.getPlayerShip().getMutableStats().getTimeMult().modifyMult("wombo_combo", 1f / playerTimeMult);
                    engine.getPlayerShip().getMutableStats().getProjectileSpeedMult().modifyMult("wombo_combo", 1f
                            / playerShotTimeMult);
                    engine.getPlayerShip().getMutableStats().getMissileAccelerationBonus().modifyMult("wombo_combo",
                            1f
                            / playerShotTimeMult);
                    engine.getPlayerShip().getMutableStats().getMissileMaxSpeedBonus().modifyMult("wombo_combo", 1f
                            / playerShotTimeMult);
                    engine.getPlayerShip().getMutableStats().getMissileMaxTurnRateBonus().modifyMult("wombo_combo", 1f
                            / playerShotTimeMult);
                    engine.getPlayerShip().getMutableStats().getMissileTurnAccelerationBonus().modifyMult("wombo_combo",
                            1f
                            / playerShotTimeMult);
                    for (ShipAPI ship : engine.getShips()) {
                        if (ship != engine.getPlayerShip() && ship.isAlive()) {
                            if (MissionPlugin.BOSS_SHIPS.containsKey(ship.getHullSpec().getHullId())) {
                                ship.getMutableStats().getTimeMult().modifyMult("wombo_combo", 1f / bossTimeMult);
                                ship.getMutableStats().getProjectileSpeedMult().modifyMult("wombo_combo", 1f
                                        / bossTimeMult);
                                ship.getMutableStats().getMissileAccelerationBonus().modifyMult("wombo_combo", 1f
                                        / bossTimeMult);
                                ship.getMutableStats().getMissileMaxSpeedBonus().modifyMult("wombo_combo", 1f
                                        / bossTimeMult);
                                ship.getMutableStats().getMissileMaxTurnRateBonus().modifyMult("wombo_combo", 1f
                                        / bossTimeMult);
                                ship.getMutableStats().getMissileTurnAccelerationBonus().modifyMult("wombo_combo", 1f
                                        / bossTimeMult);
                            }
                        }
                    }
                }
            }

            if (comboOn && deathCount > spreeKills) {
                playSound(deathCount);
                spreeKills = deathCount;
            }

            if (comboOn && comboTimer <= 0f) {
                resetKills();
                timerCap = 1f;
                engine.getPlayerShip().setCurrentCR(1f);
                engine.getTimeMult().unmodify("wombo_combo");
                engine.getPlayerShip().getMutableStats().getTimeMult().unmodify("wombo_combo");
                for (ShipAPI ship : engine.getShips()) {
                    if (ship != engine.getPlayerShip() && ship.isAlive()) {
                        if (MissionPlugin.BOSS_SHIPS.containsKey(ship.getHullSpec().getHullId())) {
                            ship.getMutableStats().getTimeMult().unmodify("wombo_combo");
                        }
                    }
                }
            }
        }

        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        final int w = (int) (Display.getWidth() * Display.getPixelScaleFactor()), h = (int) (Display.getHeight()
                * Display.getPixelScaleFactor());
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
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glTranslatef(0.01f, 0.01f, 0);

        if (deathCount > 1) {
            GL11.glBegin(GL11.GL_QUADS);
            glColor(BACKGROUND_COLOR, 1f);
            GL11.glVertex2f(position.x - width * 0.5f + 1f, position.y + height * 0.5f - 1f);
            GL11.glVertex2f(position.x + width * 0.5f - width * (1f - comboTimer / timerCap) + 1f, position.y + height
                    * 0.5f - 1f);
            GL11.glVertex2f(position.x + width * 0.5f - width * (1f - comboTimer / timerCap) + 1f, position.y - height
                    * 0.5f - 1f);
            GL11.glVertex2f(position.x - width * 0.5f + 1f, position.y - height * 0.5f - 1f);
            if (comboTimer <= 5f) {
                glColor(LOW_COLOR, 1f);
            } else if (comboTimer <= 10f) {
                glColor(MID_COLOR, 1f);
            } else {
                glColor(HIGH_COLOR, 1f);
            }
            GL11.glVertex2f(position.x - width * 0.5f, position.y + height * 0.5f);
            GL11.glVertex2f(position.x + width * 0.5f - width * (1f - comboTimer / timerCap), position.y + height * 0.5f);
            GL11.glVertex2f(position.x + width * 0.5f - width * (1f - comboTimer / timerCap), position.y - height * 0.5f);
            GL11.glVertex2f(position.x - width * 0.5f, position.y - height * 0.5f);
            GL11.glEnd();
        }

        GL11.glDisable(GL11.GL_BLEND);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glPopMatrix();
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glPopMatrix();
        GL11.glPopAttrib();
    }

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
        deathCount = 0;
        spreeKills = 0;
        comboTimer = 0f;
        timerCap = 1f;
        comboOn = false;
        engine.getPlayerShip().setCurrentCR(1f);
    }

    private void playSound(int recentKills) {
        SoundPlayerAPI sp = Global.getSoundPlayer();
        float pitch = 1.0f;
        float vol = 1.0f;

        if (recentKills == 2) {
            sp.playUISound("swp_arcade_two_kills", pitch, vol);
        } else if (recentKills == 3) {
            sp.playUISound("swp_arcade_three_kills", pitch, vol);
        } else if (recentKills == 4) {
            sp.playUISound("swp_arcade_four_kills", pitch, vol);
        } else if (recentKills == 5) {
            sp.playUISound("swp_arcade_five_kills", pitch, vol);
        } else if (recentKills == 6) {
            sp.playUISound("swp_arcade_six_kills", pitch, vol);
        } else if (recentKills == 7) {
            sp.playUISound("swp_arcade_seven_kills", pitch, vol);
        } else if (recentKills == 8) {
            sp.playUISound("swp_arcade_eight_kills", pitch, vol);
        } else if (recentKills == 9) {
            sp.playUISound("swp_arcade_nine_kills", pitch, vol);
        } else if (recentKills == 10) {
            sp.playUISound("swp_arcade_ten_kills", pitch, vol);
        } else if (recentKills == 11) {
            sp.playUISound("swp_arcade_eleven_kills", pitch, vol);
        } else if (recentKills == 12) {
            sp.playUISound("swp_arcade_twelve_kills", pitch, vol);
        } else if (recentKills == 13) {
            sp.playUISound("swp_arcade_thirteen_kills", pitch, vol);
        } else if (recentKills == 14) {
            sp.playUISound("swp_arcade_fourteen_kills", pitch, vol);
        } else if (recentKills == 15) {
            sp.playUISound("swp_arcade_fifteen_kills", pitch, vol);
        } else if (recentKills == 16) {
            sp.playUISound("swp_arcade_sixteen_kills", pitch, vol);
        } else if (recentKills == 17) {
            sp.playUISound("swp_arcade_seventeen_kills", pitch, vol);
        } else if (recentKills == 18) {
            sp.playUISound("swp_arcade_eighteen_kills", pitch, vol);
        } else if (recentKills == 19) {
            sp.playUISound("swp_arcade_ninteen_kills", pitch, vol);
        } else if (recentKills == 20) {
            sp.playUISound("swp_arcade_twenty_kills", pitch, vol);
        } else if (recentKills == 21) {
            sp.playUISound("swp_arcade_twentyone_kills", pitch, vol);
        } else if (recentKills == 22) {
            sp.playUISound("swp_arcade_twentytwo_kills", pitch, vol);
        } else if (recentKills >= 23) {
            sp.playUISound("swp_arcade_wtf_kills", pitch, vol);
        }
    }
}
