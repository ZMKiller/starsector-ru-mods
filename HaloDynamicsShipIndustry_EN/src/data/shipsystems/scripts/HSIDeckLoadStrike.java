package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.ModSpecAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import data.kit.HSII18nUtil;
import org.lazywizard.lazylib.combat.AIUtils;

import javax.management.ObjectName;
import java.util.ArrayList;
import java.util.List;

public class HSIDeckLoadStrike extends BaseShipSystemScript {
    private boolean once = true;

    private ShipAPI ship = null;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if(ship == null) {
            if (stats.getEntity() instanceof ShipAPI) {
                ship = (ShipAPI) stats.getEntity();
            }else{
                return;
            }
        }
        //int owner = ship.getOwner();
        if(!once) return;
        List<ShipAPI> allies = AIUtils.getAlliesOnMap(ship);
        allies.add(ship);
        CombatFleetManagerAPI manager = Global.getCombatEngine().getFleetManager(ship.getOwner());
        manager.setSuppressDeploymentMessages(true);
        for(ShipAPI s: allies){
            if(s.isFighter()) continue;
            if(s.getHullSpec().getFighterBays()<=0) continue;
            if(!s.getHullSpec().getBaseHullId().startsWith("HSI_")) continue;
            if(ship.getOwner()==0&&s.isAlly()&&!ship.isAlly()) continue;
            if(ship.getOwner()==0&&!s.isAlly()&&ship.isAlly()) continue;
            if(ship.hasListenerOfClass(HSIDLSTimer.class)) continue;

            List<HullModEffect> mods = new ArrayList<>();
            for(String mod:ship.getVariant().getHullMods()){
                if(Global.getSettings().getHullModSpec(mod)!=null&&Global.getSettings().getHullModSpec(mod).getEffect()!=null){
                    mods.add(Global.getSettings().getHullModSpec(mod).getEffect());
                }
            }
            for(FighterWingAPI wing:s.getAllWings()){
                if(wing.getSource()!=null){
                    FighterWingSpecAPI spec = wing.getSpec();
                    if(!spec.getId().endsWith("_wing")){
                        continue;
                    }
                    float facing = wing.getSource().getWeaponSlot().computeMidArcAngle(s);

                    ShipAPI f = manager.spawnShipOrWing(spec.getId(),wing.getSource().getWeaponSlot().computePosition(s),facing);
                    if(!f.isFighter()){
                        Global.getCombatEngine().removeEntity(f);
                    }
                    if(f.isFighter()&&f.getWing()!=null){
                        for(ShipAPI fighter:f.getWing().getWingMembers()){
                            fighter.setAlly(s.isAlly());
                            fighter.setOwner(s.getOwner());
                            fighter.setFacing(facing);
                            fighter.turnOnTravelDrive(2f);
                            for(HullModEffect effects:mods){
                                effects.applyEffectsToFighterSpawnedByShip(ship,fighter,"HSIDLS_SystemTrigger");
                            }
                        }
                    }
                }
            }
            s.addListener(new HSIDLSTimer(s));
        }
        manager.setSuppressDeploymentMessages(false);
        once = false;
    }


    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        once = true;
    }

    @Override
    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
        if (system.isOutOfAmmo()) return null;
        if (system.getState() != ShipSystemAPI.SystemState.IDLE) return null;
        List<ShipAPI> allies = AIUtils.getAlliesOnMap(ship);
        allies.add(ship);
        int total = 0;
        int available = 0;
        for(ShipAPI s: allies){
            if(s.isFighter()) continue;
            if(ship.getOwner()==0&&s.isAlly()&&!ship.isAlly()) continue;
            if(ship.getOwner()==0&&!s.isAlly()&&ship.isAlly()) continue;
            if(s.isStationModule()) continue;
            total++;
            available++;
            if(s.hasListenerOfClass(HSIDLSTimer.class)){
                available--;
            }
        }
        return available+"/"+total+" "+ HSII18nUtil.getShipSystemString("HSIUsable");
    }

    public static class HSIDLSTimer implements AdvanceableListener{

        private float time = 120;

        private ShipAPI ship;

        public HSIDLSTimer(ShipAPI ship){
            this.ship = ship;
        }
        @Override
        public void advance(float amount) {
            time-=amount;
            if(time<=0){
                ship.removeListener(this);
            }
        }
    }
}
