package data.shipsystems.scripts;

import java.awt.Color;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.magiclib.util.MagicTargeting;
import org.magiclib.util.MagicTargeting.targetSeeking;

import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

import data.kit.HSII18nUtil;

public class HSIIFFDisruptor extends BaseShipSystemScript{
    private ShipAPI ship;
    private ShipAPI target;
    private Set<WeaponAPI> effectedWeapons = new HashSet<>();
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        target = pickTarget();
        if(effectLevel>=1&&target!=null){
            for(WeaponAPI weapon:target.getAllWeapons()){
                if(weapon.isDecorative()) return;
                if(target.getWeaponGroupFor(weapon)!=null&&target.getWeaponGroupFor(weapon).getAutofirePlugin(weapon)!=null){
                    ShipAPI weaponT = target.getWeaponGroupFor(weapon).getAutofirePlugin(weapon).getTargetShip();
                    if(weaponT!=null&&weaponT.isFighter()&&weaponT.getOwner()==ship.getOwner()){
                        weapon.setForceNoFireOneFrame(true);
                        weapon.setGlowAmount(effectLevel, new Color(255,100,90,255));
                        effectedWeapons.add(weapon);
                    }
                    /*MissileAPI weaponTM = target.getWeaponGroupFor(weapon).getAutofirePlugin(weapon).getTargetMissile();
                    if(weaponTM!=null&&weaponTM.getOwner()==ship.getOwner()){
                        target.getWeaponGroupFor(weapon).getAutofirePlugin(weapon).forceOff();
                    }*/
                }
            }
        }
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        if(effectedWeapons.isEmpty()) return;
        Iterator<WeaponAPI> w = effectedWeapons.iterator();
        while(w.hasNext()){
            w.next().setGlowAmount(0, Color.WHITE);
        }
        effectedWeapons.clear();
	}

    private ShipAPI pickTarget(){
        if(ship!=null){
            return MagicTargeting.pickShipTarget(ship, targetSeeking.NO_RANDOM, 2000, 360, 0, 3, 8, 15, 40);
        }else{
            return null;
        }
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        if (index == 0) {
            return new StatusData(HSII18nUtil.getShipSystemString("HSIIFFDisruptorHints")+"-"+(int)(effectLevel*100f)+"%", false);
        }
        return null;
    }
    
}
