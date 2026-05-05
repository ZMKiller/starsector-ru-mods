package data.hullmods;

import java.util.Random;

import org.lazywizard.lazylib.VectorUtils;
import java.awt.Color;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier;
import com.fs.starfarer.api.impl.combat.MoteAIScript;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.TimeoutTracker;

import data.kit.HSII18nUtil;

public class HSIElegySystemListener implements DamageTakenModifier, AdvanceableListener {
    private ShipAPI target;
    private ShipAPI source;
    private TimeoutTracker<ShipAPI> maintain = new TimeoutTracker<>();
    private FaderUtil brightness = new FaderUtil(0f, 1.2f);
    private float elapsed = 90f * (float) Math.random();
    public Color TEXT_COLOR = new Color(255, 55, 55, 255);

    public HSIElegySystemListener(ShipAPI target, ShipAPI source) {
        this.target = target;
        this.source = source;
        target.setCustomData("HSIElegySystemLocked", new Object());
    }

    @Override
    public void advance(float amount) {
        maintain.advance(amount);
        if (maintain.getItems().isEmpty()) {
            brightness.fadeOut();
        } else {
            brightness.fadeIn();
        }
        brightness.advance(amount);
        if (target.isHulk() || !target.isAlive()) {
            int num = HSIElegySystem.NUM_AT_KILL.get(target.getHullSize());
            CombatEngineAPI engine = Global.getCombatEngine();
            for (int i = 0; i < num; i++) {
                Vector2f loc = Misc.getPointWithinRadiusUniform(target.getLocation(), target.getCollisionRadius(),
                        target.getCollisionRadius() * 2f, new Random());
                float dir = VectorUtils.getAngle(target.getLocation(), loc);
                float arc = 50;
                dir += arc * (float) Math.random() - arc / 2f;

                String weaponId = HSIElegySystemScript.getWeapon();
                MissileAPI mote = (MissileAPI) engine.spawnProjectile(source, null,
                        weaponId,
                        loc, dir, null);
                mote.setWeaponSpec(weaponId);
                mote.setMissileAI(new MoteAIScript(mote));
                mote.getActiveLayers().remove(CombatEngineLayers.FF_INDICATORS_LAYER);
                mote.setEmpResistance(10000);
                HSIElegySystemScript.getSharedData(source).motes.add(mote);
            }
            target.getCustomData().remove("HSIElegySystemLocked");
            target.removeListener(this);
        }
        if (maintain.getItems().isEmpty() && brightness.getBrightness() == 0) {
            target.getCustomData().remove("HSIElegySystemLocked");
            target.removeListener(this);
        }
        if (target != null && source.getSystem().isChargeup()&&source.getSystem().getEffectLevel()<=0.05f) {
            if (target.getFluxTracker().showFloaty() ||
                    source == Global.getCombatEngine().getPlayerShip() ||
                    target == Global.getCombatEngine().getPlayerShip()) {
                target.getFluxTracker().showOverloadFloatyIfNeeded(
                        HSII18nUtil.getShipSystemString("HSIElegyFloaty"), TEXT_COLOR, 4f, true);
            }
        }
        if (target != null && source.getSystem().getEffectLevel() >=1) {
            if (target == Global.getCombatEngine().getPlayerShip()) {
                float damMult = HSIElegySystem.DAMAGE_BUFF_MULT;
                Global.getCombatEngine().maintainStatusForPlayerShip(target,
                        source.getSystem().getSpecAPI().getIconSpriteName(),
                        source.getSystem().getDisplayName(),
                        HSII18nUtil.getShipSystemString("HSIElegyStatusData2") + "+"
                                + (int) ((damMult - 1f) * 100f)
                                + HSII18nUtil.getShipSystemString("HSIElegyStatusData1"),
                        false);
            }
        }
        elapsed += amount;
    }

    public String modifyDamageTaken(Object param, CombatEntityAPI target,
            DamageAPI damage, Vector2f point, boolean shieldHit) {
        float Cdamage = damage.getDamage();
        if(Cdamage == 0){
            Cdamage =1;
        }
        damage.getModifier().modifyMult("HSI_ElegySystem",
                ((Cdamage + HSIElegySystem.DAMAGE_BUFF_FLAT) / Cdamage));
        damage.getModifier().modifyMult("HSI_ElegySystemActive",
                1f + source.getSystem().getEffectLevel() * (HSIElegySystem.DAMAGE_BUFF_MULT - 1f));
        return "HSI_ElegySystem";
    }

    public void maintain(ShipAPI s) {
        if (maintain.contains(s)) {
            maintain.set(s, 0.2f);
        } else {
            maintain.add(s, 0.2f);
        }
    }

    public FaderUtil getBrightness() {
        return brightness;
    }

    public float getElapsed() {
        return elapsed;
    }

    public ShipAPI getTarget() {
        return target;
    }

    public static HSIElegySystemListener getInstance(ShipAPI source,ShipAPI target){
        if(target.getCustomData().containsKey("HSIElegySystemLocked")){
            return target.getListeners(HSIElegySystemListener.class).get(0);
        }else{
            HSIElegySystemListener l = new HSIElegySystemListener(target, source);
            target.addListener(l);
            return l;
        }
    }

}
