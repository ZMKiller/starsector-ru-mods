package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.combat.MoteAIScript;
import com.fs.starfarer.api.impl.combat.MoteControlScript.MoteData;
import com.fs.starfarer.api.impl.combat.MoteControlScript.SharedMoteAIData;
import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicTargeting;
import org.magiclib.util.MagicTargeting.targetSeeking;

import java.awt.*;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HSIElegySystemScript extends BaseCombatLayeredRenderingPlugin {
    private ShipAPI ship;
    private final SpriteAPI Lock = Global.getSettings().getSprite("graphics/hud/holo_target.png");
    // private IntervalUtil checkTarget = new IntervalUtil(0.5f, 0.8f);
    private ShipAPI subTarget;
    private ShipAPI primaryTarget;
    // private List<ShipAPI> possibleTargets = new ArrayList<>();
    protected static float MAX_ATTRACTOR_RANGE = 2000f;
    public static float MAX_DIST_FROM_SOURCE_TO_ENGAGE_AS_PD = 2000f;
    public static float MAX_DIST_FROM_ATTRACTOR_TO_ENGAGE_AS_PD = 1000f;

    public static int MAX_MOTES = 15;
    // public static int MAX_MOTES_HF = 50;

    public static float ANTI_FIGHTER_DAMAGE = 400;
    // public static float ANTI_FIGHTER_DAMAGE_HF = 1000;

    public static float ANTI_SHIP_DAMAGE = 100;
    public static float ANTI_SHIP_EMP = 500;

    // public static float ATTRACTOR_DURATION_LOCK = 20f;
    // public static float ATTRACTOR_DURATION = 10f;

    public static Map<String, MoteData> MOTE_DATA = new HashMap<String, MoteData>();
    public static String KEY = "HSI_ElegySystem_ref";
    public static String HSI_LITTLE_STAR = "HWI_Z";
    // public static String MOTELAUNCHER_HF = "motelauncher_hf";
    static {
        MoteData normal = new MoteData();
        normal.jitterColor = new Color(100, 165, 255, 175);
        normal.empColor = new Color(175, 175, 255, 255);
        normal.maxMotes = MAX_MOTES;
        normal.antiFighterDamage = ANTI_FIGHTER_DAMAGE;
        normal.impactSound = "mote_attractor_impact_normal";
        normal.loopSound = "mote_attractor_loop";

        MOTE_DATA.put(HSI_LITTLE_STAR, normal);
    }

    protected IntervalUtil launchInterval = new IntervalUtil(1f, 1.2f);
    protected IntervalUtil attractorParticleInterval = new IntervalUtil(0.05f, 0.1f);
    protected WeightedRandomPicker<WeaponSlotAPI> launchSlots = new WeightedRandomPicker<WeaponSlotAPI>();
    // protected WeaponSlotAPI attractor = null;

    // protected int empCount = 0;
    protected boolean findNewTargetOnUse = true;

    public HSIElegySystemScript(ShipAPI ship) {
        this.ship = ship;
        this.layer = CombatEngineLayers.ABOVE_PARTICLES_LOWER;
        ship.setCustomData(KEY, this);
    }

    @Override
    public void advance(float amount) {
        if (Global.getCombatEngine().isPaused()) {
            return;
        }
        if (ship.isHulk()) {
            return;
        }
        if (primaryTarget != null && primaryTarget.isAlive() && !primaryTarget.isHulk()) {
            HSIElegySystemListener.getInstance(ship, primaryTarget).maintain(ship);
        } else {
            primaryTarget = repickPrimaryTarget();
        }
        if (subTarget == null || !subTarget.isAlive() || subTarget.isHulk()
                || subTarget.getOwner() == ship.getOwner() || subTarget == primaryTarget) {
            subTarget = repickSubTarget();
        } else {
            HSIElegySystemListener.getInstance(ship, subTarget).maintain(ship);
        }
        SharedMoteAIData data = getSharedData(ship);
        data.elapsed += amount;
        if (ship.getSystem() != null && ship.getSystem().isActive()) {
            if (subTarget != null && (subTarget.getHullLevel() <= 0.25f||subTarget.getHitpoints()<=2000f)) {
                data.attractorLock = subTarget;
                data.attractorTarget = subTarget.getLocation();
                data.attractorRemaining = 3;
            }
            if (primaryTarget != null && (primaryTarget.getHullLevel() <= 0.25f||primaryTarget.getHitpoints()<=2000f)) {
                data.attractorLock = primaryTarget;
                data.attractorTarget = primaryTarget.getLocation();
                data.attractorRemaining = 3;
            }
        } else {
            data.attractorLock = null;
            data.attractorTarget = null;
        }
        CombatEngineAPI engine = Global.getCombatEngine();

        launchInterval.advance(amount);
        if (launchInterval.intervalElapsed()) {
            Iterator<MissileAPI> iter = data.motes.iterator();
            while (iter.hasNext()) {
                if (!engine.isMissileAlive(iter.next())) {
                    iter.remove();
                }
            }

            if (ship.isHulk()) {
                for (MissileAPI mote : data.motes) {
                    mote.flameOut();
                }
                data.motes.clear();
                return;
            }

            int maxMotes = getMaxMotes(ship);
            if (data.motes.size() < maxMotes && // false &&
                    !ship.getFluxTracker().isOverloadedOrVenting()) {
                findSlots(ship);

                WeaponSlotAPI slot = launchSlots.pick();

                Vector2f loc = slot.computePosition(ship);
                float dir = slot.computeMidArcAngle(ship);
                float arc = slot.getArc();
                dir += arc * (float) Math.random() - arc / 2f;

                String weaponId = getWeapon();
                MissileAPI mote = (MissileAPI) engine.spawnProjectile(ship, null,
                        weaponId,
                        loc, dir, null);
                mote.setWeaponSpec(weaponId);
                mote.setMissileAI(new MoteAIScript(mote));
                mote.getActiveLayers().remove(CombatEngineLayers.FF_INDICATORS_LAYER);
                mote.setEmpResistance(10000);
                data.motes.add(mote);

                engine.spawnMuzzleFlashOrSmoke(ship, slot, mote.getWeaponSpec(), 0, dir);

                Global.getSoundPlayer().playSound("mote_attractor_launch_mote", 1f, 0.25f, loc, new Vector2f());
            }

            subTarget = null;
            primaryTarget = null;
        }
    }

    @Override
    public void render(CombatEngineLayers layer, ViewportAPI viewport) {
        if (layer == this.layer) {
            SpriteAPI sprite = Global.getSettings().getSprite("graphics/fx/HSI_ContrailParticle.png");
            {
                ShipAPI s = primaryTarget;
                if (s != null && s.hasListenerOfClass(HSIElegySystemListener.class)) {
                    HSIElegySystemListener l = s.getListeners(HSIElegySystemListener.class).get(0);
                    float alpha = 0.5f * l.getBrightness().getBrightness()
                            + 0.49f * ship.getSystem().getEffectLevel();
                    float elapsed = l.getElapsed();
                    ShipAPI from = ship;
                    ShipAPI to = s;
                    GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
                    GL11.glPushMatrix();
                    GL11.glEnable(GL11.GL_TEXTURE_2D);
                    sprite.bindTexture();
                    GL11.glEnable(GL11.GL_BLEND);
                    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                    GL11.glBegin(GL11.GL_QUAD_STRIP);
                    float textLength = sprite.getWidth();
                    float textProgress = elapsed * 1.5f;
                    Vector2f mp0 = from.getLocation();
                    Vector2f mp1 = to.getLocation();
                    float linkAngle = VectorUtils.getAngle(mp0, mp1);
                    // float width = WIDTH*midpoints.get(i).getOpacity()*0.5f;
                    float width = 10f;
                    float length = MathUtils.getDistance(mp0, mp1) / 10f;
                    for (int i = 0; i < 11; i++) {
                        Vector2f center = new Vector2f(0.1f * i * mp0.x + 0.1f * (10 - i) * mp1.x,
                                0.1f * i * mp0.y + 0.1f * (10 - i) * mp1.y);
                        Vector2f v0 = MathUtils.getPointOnCircumference(center, width, linkAngle + 90f);
                        Vector2f v1 = MathUtils.getPointOnCircumference(center, width, linkAngle - 90f);
                        GL11.glColor4ub((byte) HSIElegySystem.LINK.getRed(), (byte) HSIElegySystem.LINK.getGreen(),
                                (byte) HSIElegySystem.LINK.getBlue(),
                                (byte) (HSIElegySystem.LINK.getAlpha()
                                        * alpha * Math.sin(0.314f * i)));
                        GL11.glTexCoord2f(textProgress, 0f);
                        GL11.glVertex2f(v0.x, v0.y);
                        GL11.glTexCoord2f(textProgress, 1f);
                        GL11.glVertex2f(v1.x, v1.y);
                        textProgress += (length / (10f * textLength));
                    }
                    GL11.glEnd();
                    GL11.glPopMatrix();
                    GL11.glPopAttrib();
                    Lock.setSize(s.getHullSize().ordinal() * 16f, s.getHullSize().ordinal() * 16f);
                    Lock.setAngle(s.getFacing() + l.getElapsed() * 0.5f);
                    Lock.setAlphaMult(alpha);
                    Lock.renderAtCenter(s.getLocation().x, s.getLocation().y);
                }
            }
            {
                ShipAPI s = subTarget;
                if (s != null && s.hasListenerOfClass(HSIElegySystemListener.class)) {
                    HSIElegySystemListener l = s.getListeners(HSIElegySystemListener.class).get(0);
                    float alpha = 0.5f * l.getBrightness().getBrightness()
                            + 0.49f * ship.getSystem().getEffectLevel();
                    float elapsed = l.getElapsed();
                    ShipAPI from = ship;
                    ShipAPI to = s;
                    GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
                    GL11.glPushMatrix();
                    GL11.glEnable(GL11.GL_TEXTURE_2D);
                    sprite.bindTexture();
                    GL11.glEnable(GL11.GL_BLEND);
                    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                    GL11.glBegin(GL11.GL_QUAD_STRIP);
                    float textLength = sprite.getWidth();
                    float textProgress = elapsed * 1.5f;
                    Vector2f mp0 = from.getLocation();
                    Vector2f mp1 = to.getLocation();
                    float linkAngle = VectorUtils.getAngle(mp0, mp1);
                    // float width = WIDTH*midpoints.get(i).getOpacity()*0.5f;
                    float width = 10f;
                    float length = MathUtils.getDistance(mp0, mp1) / 10f;
                    for (int i = 0; i < 11; i++) {
                        Vector2f center = new Vector2f(0.1f * i * mp0.x + 0.1f * (10 - i) * mp1.x,
                                0.1f * i * mp0.y + 0.1f * (10 - i) * mp1.y);
                        Vector2f v0 = MathUtils.getPointOnCircumference(center, width, linkAngle + 90f);
                        Vector2f v1 = MathUtils.getPointOnCircumference(center, width, linkAngle - 90f);
                        GL11.glColor4ub((byte) HSIElegySystem.LINK.getRed(), (byte) HSIElegySystem.LINK.getGreen(),
                                (byte) HSIElegySystem.LINK.getBlue(),
                                (byte) (HSIElegySystem.LINK.getAlpha()
                                        * alpha));
                        GL11.glTexCoord2f(textProgress, 0f);
                        GL11.glVertex2f(v0.x, v0.y);
                        GL11.glTexCoord2f(textProgress, 1f);
                        GL11.glVertex2f(v1.x, v1.y);
                        textProgress += (length / (10f * textLength));
                    }
                    GL11.glEnd();
                    GL11.glPopMatrix();
                    GL11.glPopAttrib();
                    Lock.setSize(s.getHullSize().ordinal() * 16f, s.getHullSize().ordinal() * 16f);
                    Lock.setAngle(s.getFacing() + l.getElapsed() * 0.5f);
                    Lock.setAlphaMult(alpha);
                    Lock.renderAtCenter(s.getLocation().x, s.getLocation().y);
                }
            }
        }
    }

    @Override
    public EnumSet<CombatEngineLayers> getActiveLayers() {
        return EnumSet.of(layer);
    }

    @Override
    public float getRenderRadius() {
        return 100000f;
    }

    public ShipAPI repickPrimaryTarget() {
        if (ship.getShipTarget() != null) {
            return ship.getShipTarget();
        } else {
            return MagicTargeting.pickShipTarget(ship, targetSeeking.LOCAL_RANDOM, (int) HSIElegySystem.RANGE, 360, 0,
                    15, 25, 45, 100);
        }
    }

    public ShipAPI repickSubTarget() {
        ShipAPI nT = null;
        float level = 2;
        for (ShipAPI s : AIUtils.getNearbyEnemies(ship, HSIElegySystem.RANGE)) {
            if (s == primaryTarget)
                continue;
            if (s.isFighter())
                continue;
            if (s.getHullLevel() < level) {
                nT = s;
                level = s.getHullLevel();
            }
        }

        return nT;
    }

    protected void findSlots(ShipAPI ship) {
        for (WeaponSlotAPI slot : ship.getHullSpec().getAllWeaponSlotsCopy()) {
            if (slot.isSystemSlot()) {
                launchSlots.add(slot);
            }
        }
    }

    public static String getWeapon() {
        return HSI_LITTLE_STAR;
    }

    public static float getAntiFighterDamage(ShipAPI ship) {
        return MOTE_DATA.get(getWeapon()).antiFighterDamage;
    }

    public static String getImpactSoundId(ShipAPI ship) {
        return MOTE_DATA.get(getWeapon()).impactSound;
    }

    public static Color getJitterColor(ShipAPI ship) {
        return MOTE_DATA.get(getWeapon()).jitterColor;
    }

    public static Color getEMPColor(ShipAPI ship) {
        return MOTE_DATA.get(getWeapon()).empColor;
    }

    public static int getMaxMotes(ShipAPI ship) {
        return MOTE_DATA.get(getWeapon()).maxMotes;
    }

    public static String getLoopSound(ShipAPI ship) {
        return MOTE_DATA.get(getWeapon()).loopSound;
    }

    public static SharedMoteAIData getSharedData(ShipAPI source) {
        String key = source + "_mote_AI_shared";
        SharedMoteAIData data = (SharedMoteAIData) Global.getCombatEngine().getCustomData().get(key);
        if (data == null) {
            data = new SharedMoteAIData();
            Global.getCombatEngine().getCustomData().put(key, data);
        }
        return data;
    }

    public boolean hasTarget() {
        return subTarget != null || primaryTarget != null;
    }

    public static HSIElegySystemScript getInstance(ShipAPI ship) {
        if (ship.getCustomData().containsKey(HSIElegySystemScript.KEY)) {
            return (HSIElegySystemScript) ship.getCustomData().get(HSIElegySystemScript.KEY);
        } else {
            HSIElegySystemScript script = new HSIElegySystemScript(ship);
            Global.getCombatEngine().addLayeredRenderingPlugin(script);
            return script;
        }
    }

    @Override
    public boolean isExpired() {
        return ship.isHulk()||ship==null||Global.getCombatEngine()==null||!Global.getCombatEngine().isEntityInPlay(ship);
    }

}
