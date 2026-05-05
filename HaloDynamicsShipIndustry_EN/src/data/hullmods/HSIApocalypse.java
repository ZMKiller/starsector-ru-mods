package data.hullmods;

import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.hullmods.HighScatterAmp;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.AIUtils;

import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;

public class HSIApocalypse extends BaseHullMod {

    public void applyEffectsBeforeShipCreation(HullSize hullSize,
											   MutableShipStatsAPI stats, String id) {
        stats.getCombatEngineRepairTimeMult().modifyMult(id, 0.33f);
        stats.getCombatWeaponRepairTimeMult().modifyMult(id, 0.33f);
	}

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        ship.addListener(new HighScatterAmp.HighScatterAmpDamageDealtMod(ship));
        //if(ship.getHullSpec().getBaseHullId().endsWith("_G")){
        //    ship.getMutableStats().getSystemRegenBonus().modifyPercent(id,20f);
        //}
    }

    public void advanceInCombat(ShipAPI ship, float amount) {
        HSITurbulanceShieldListenerV2 shield = HSITurbulanceShieldListenerV2.getInstance(ship);
        ship.getMutableStats().getHullDamageTakenMult().modifyMult("HSI_Apocalypse",(1f-(1f-shield.getShield().getShieldLevel())*0.3f));
        ship.getMutableStats().getArmorDamageTakenMult().modifyMult("HSI_Apocalypse",(1f-(1f-shield.getShield().getShieldLevel())*0.3f));
        ship.getMutableStats().getShieldDamageTakenMult().modifyMult("HSI_Apocalypse",(1f-(1f-shield.getShield().getShieldLevel())*0.3f));
        ship.getMutableStats().getEffectiveArmorBonus().modifyFlat("HSI_Apocalypse",50f*(1f-shield.getShield().getShieldLevel()));

        if(ship.getHullSpec().getBaseHullId().equals("HSI_Apocalyse")&&ship.getShipTarget()!=null&&ship.getSystem().canBeActivated()&&Math.abs(MathUtils.getShortestRotation(ship.getFacing(), Misc.getAngleInDegrees(ship.getLocation(),ship.getShipTarget().getLocation())))<5f&&ship.getWing()!=null&&ship.getWing().getSourceShip()!=null&&!ship.getWing().getSourceShip().isPullBackFighters()){
            ship.useSystem();
        }

        if(ship.getHullSpec().getBaseHullId().endsWith("_G")&&Math.random()>0.9f){
            for(WeaponAPI weapon:ship.getAllWeapons()){
                if(weapon.getSpec().getWeaponId().equals("HSI_Apocalypse_RA")&&weapon.isFiring()&&ship.getSystem()!=null&&ship.getSystem().canBeActivated()){
                    ship.useSystem();
                }
            }
        }

    }
}
