package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.loading.FighterWingSpecAPI;
import data.kit.HSII18nUtil;
import org.lazywizard.lazylib.combat.AIUtils;

import java.util.List;

public class HSIUplikeFireControl extends BaseShipSystemScript {


    private ShipAPI ship = null;

    protected static final float RANGE_BONUS = 30f;

    protected static final float DAMAGE_BONUS = 25f;

    public static final String KEY = "HSI_UplinkControl";

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
        List<ShipAPI> allies = AIUtils.getAlliesOnMap(ship);
        allies.add(ship);
        for(ShipAPI s: allies){
            if(s.isFighter()) continue;
            if(ship.getOwner()==0&&s.isAlly()&&!ship.isAlly()) continue;
            if(ship.getOwner()==0&&!s.isAlly()&&ship.isAlly()) continue;
            if(s.getCustomData()!=null&&s.getCustomData().containsKey(KEY)){
                if(s.getCustomData().get(KEY) instanceof ShipAPI){
                    if(!((ShipAPI)s.getCustomData().get(KEY)).equals(ship)){
                        continue;
                    }
                }
            }
            MutableShipStatsAPI Sstats = s.getMutableStats();
            Sstats.getDynamic().getMod(Stats.ELECTRONIC_WARFARE_PENALTY_MOD).modifyMult(id, 0f);
            Sstats.getBallisticWeaponRangeBonus().modifyPercent(id+ship.getName(),RANGE_BONUS*effectLevel);
            Sstats.getEnergyWeaponRangeBonus().modifyPercent(id+ship.getName(),RANGE_BONUS*effectLevel);
            Sstats.getBallisticWeaponDamageMult().modifyPercent(id+ship.getName(),DAMAGE_BONUS*effectLevel);
            Sstats.getEnergyWeaponDamageMult().modifyPercent(id+ship.getName(),DAMAGE_BONUS*effectLevel);
            float accuracy = Sstats.getAutofireAimAccuracy().getModifiedValue();
            float flat = 1f-accuracy;
            Sstats.getAutofireAimAccuracy().modifyFlat(id,flat);
            if(s == Global.getCombatEngine().getPlayerShip()){
                addStatus();
            }
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        if(ship == null) {
            if (stats.getEntity() instanceof ShipAPI) {
                ship = (ShipAPI) stats.getEntity();
            }else{
                return;
            }
        }
        //int owner = ship.getOwner();
        List<ShipAPI> allies = AIUtils.getAlliesOnMap(ship);
        allies.add(ship);
        for(ShipAPI s: allies){
            if(s.isFighter()) continue;
            if(ship.getOwner()==0&&s.isAlly()&&!ship.isAlly()) continue;
            if(ship.getOwner()==0&&!s.isAlly()&&ship.isAlly()) continue;
            MutableShipStatsAPI Sstats = s.getMutableStats();
            Sstats.getDynamic().getMod(Stats.ELECTRONIC_WARFARE_PENALTY_MOD).unmodify(id);
            Sstats.getBallisticWeaponRangeBonus().unmodify(id+ship.getName());
            Sstats.getEnergyWeaponRangeBonus().unmodify(id+ship.getName());
            Sstats.getBallisticWeaponDamageMult().unmodify(id+ship.getName());
            Sstats.getEnergyWeaponDamageMult().unmodify(id+ship.getName());
            Sstats.getAutofireAimAccuracy().unmodify(id);
            if(s.getCustomData()!=null&&s.getCustomData().containsKey(KEY)){
                if(s.getCustomData().get(KEY) instanceof ShipAPI){
                    if(((ShipAPI)s.getCustomData().get(KEY)).equals(ship)){
                        s.setCustomData(KEY,null);
                    }
                }
            }
            if(s == Global.getCombatEngine().getPlayerShip()){
                addStatus();
            }
        }
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
            if(s.getCustomData()!=null&&s.getCustomData().containsKey(KEY)){
                if(s.getCustomData().get(KEY) instanceof ShipAPI){
                    if(!((ShipAPI)s.getCustomData().get(KEY)).equals(ship)){
                        available--;
                    }
                }
            }
        }
        return available+"/"+total+" "+ HSII18nUtil.getShipSystemString("HSIUsable");
    }

    protected void addStatus() {
        String content = HSII18nUtil.getShipSystemString("HSIUplinkFireControlContent0");
        Global.getCombatEngine().maintainStatusForPlayerShip("HSIUplinkFireControl0",
                "graphics/icons/hullsys/ammo_feeder.png", HSII18nUtil.getShipSystemString("HSIUplinkFireControlTitle"), content,
                false);
        content = HSII18nUtil.getShipSystemString("HSIUplinkFireControlContent1")+(int)RANGE_BONUS+"%"+"/"+(int)DAMAGE_BONUS+"%";
        Global.getCombatEngine().maintainStatusForPlayerShip("HSIUplinkFireControl1",
                "graphics/icons/hullsys/ammo_feeder.png", HSII18nUtil.getShipSystemString("HSIUplinkFireControlTitle"), content,
                false);
        content = HSII18nUtil.getShipSystemString("HSIUplinkFireControlContent2");
        Global.getCombatEngine().maintainStatusForPlayerShip("HSIUplinkFireControl2",
                "graphics/icons/hullsys/ammo_feeder.png", HSII18nUtil.getShipSystemString("HSIUplinkFireControlTitle"), content,
                false);
    }
}
