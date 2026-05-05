package data.weapons.scripts.proj;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.loading.WeaponGroupSpec;
import com.fs.starfarer.api.loading.WeaponGroupType;

import data.ai.HSIWingManAI;
import org.json.JSONObject;

public class HWIWingmanEffect implements ProximityExplosionEffect {
    @Override
    public void onExplosion(DamagingProjectileAPI explosion, DamagingProjectileAPI originalProjectile) {
        CombatEngineAPI engine = Global.getCombatEngine();
        if(originalProjectile instanceof MissileAPI) {
            JSONObject json = ((MissileAPI) originalProjectile).getSpec().getBehaviorJSON();

            String hullId = json.optString("droneId", null);
            String weaponId = json.optString("weaponId", null);
            boolean isDefaultAI = json.optBoolean("defaultAI",false);

            if(hullId != null) {
                ShipHullSpecAPI spec = Global.getSettings().getHullSpec(hullId);
                ShipVariantAPI v = Global.getSettings().createEmptyVariant(hullId+"_Drone", spec);
                if(weaponId!=null) {
                    v.addWeapon("WS 000", weaponId);
                    WeaponGroupSpec g = new WeaponGroupSpec(WeaponGroupType.LINKED);
                    g.addSlot("WS 000");
                    g.setAutofireOnByDefault(true);
                    v.addWeaponGroup(g);
                }
                ShipAPI w = engine.createFXDrone(v);
                w.setLayer(CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER);
                w.setOwner(originalProjectile.getOwner());
                if (originalProjectile.getSource().isAlly()) {
                    w.setAlly(true);
                }
                w.setDrone(true);
                w.getAIFlags().setFlag(AIFlags.DRONE_MOTHERSHIP, 100000f, originalProjectile.getSource());
                w.getMutableStats().getEnergyWeaponDamageMult()
                        .applyMods(originalProjectile.getSource().getMutableStats().getMissileWeaponDamageMult());
                w.getMutableStats().getMissileWeaponDamageMult()
                        .applyMods(originalProjectile.getSource().getMutableStats().getMissileWeaponDamageMult());
                w.getMutableStats().getBallisticWeaponDamageMult()
                        .applyMods(originalProjectile.getSource().getMutableStats().getMissileWeaponDamageMult());
                w.setCollisionClass(CollisionClass.FIGHTER);
                w.setFacing(originalProjectile.getFacing());
                Global.getCombatEngine().addEntity(w);
                w.getLocation().set(explosion.getLocation());
                if(!isDefaultAI) {
                    w.setShipAI(new HSIWingManAI(w, originalProjectile.getSource()));
                }else{
                    w.setHullSize(ShipAPI.HullSize.FRIGATE);
                    ShipAIConfig config = new ShipAIConfig();
                    config.alwaysStrafeOffensively = true;
                    w.setShipAI(Global.getSettings().createDefaultShipAI(w,config));
                    w.setHullSize(ShipAPI.HullSize.FIGHTER);
                }
            }
        }
    }
}
