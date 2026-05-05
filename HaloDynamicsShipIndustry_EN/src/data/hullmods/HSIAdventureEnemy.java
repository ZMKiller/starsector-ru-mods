package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.mission.FleetSide;

public class HSIAdventureEnemy extends BaseHullMod {

    protected CombatEngineAPI engine = Global.getCombatEngine();

    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        int level = 0;
        for(String tag:stats.getVariant().getTags()){
            if(tag.startsWith("ADVLEVEL_")){
                level = Integer.parseInt(tag.substring(9));

            }
        }
        if(level>0){
            float multPositive = level/20f;
            stats.getHullBonus().modifyMult(id,1f+2f+multPositive);
            stats.getArmorBonus().modifyMult(id,1f+multPositive);
            stats.getFluxCapacity().modifyMult(id,1f+multPositive);
            stats.getDamageToFrigates().modifyMult(id,1f+multPositive);
            stats.getDamageToDestroyers().modifyMult(id,1f+multPositive);
            stats.getDamageToCruisers().modifyMult(id,1f+multPositive);
            stats.getDamageToCapital().modifyMult(id,1f+multPositive);

            stats.getDamageToFighters().modifyMult(id,1f+multPositive);
            stats.getDamageToMissiles().modifyMult(id,1f+multPositive);
        }
    }
    private static final String KEY= "HSISA_ADV";

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if(Math.random()>0.05f*(amount*60f)) return;
        int level = 0;
        for(String tag:ship.getVariant().getTags()){
            if(tag.startsWith("ADVLEVEL_")){
                level = Integer.parseInt(tag.substring(9));
            }
        }
        float actualSize = 1;
        for(FleetMemberAPI member:engine.getFleetManager(FleetSide.PLAYER).getDeployedCopy()){
            float base = member.getDeploymentPointsCost();
            float mult = 1f;
            if(!member.getCaptain().isDefault()){
                float mult1 = 1f;
                for(MutableCharacterStatsAPI.SkillLevelAPI skill:member.getCaptain().getStats().getSkillsCopy()){
                    if(!skill.getSkill().isCombatOfficerSkill()) continue;
                    if(skill.getLevel()<2){
                        mult1+=0.05f;
                    }else{
                        mult1+=0.12f;
                    }
                }
                mult*=mult1;
            }
            if(member.getFleetCommander()!=null){
                float mult2 = 1f;
                for(MutableCharacterStatsAPI.SkillLevelAPI skill:member.getCaptain().getStats().getSkillsCopy()){
                    if(!skill.getSkill().isAdmiralSkill()) continue;
                    mult2+=0.02f;
                }
                mult*=mult2;
            }
            float mult3 = 1f+member.getVariant().getSMods().size()*0.05f;
            mult*=mult3;
            actualSize+=base*mult;
        }

        float base = 20f+level*5f;
        float actualBuffPositive = Math.max(1f,1f+((actualSize/base-1f)*level/15f));
        float actualBuffNegative = Math.min(1f,-1f*(1f-base/actualSize)*level/15f+1f);

        MutableShipStatsAPI stats = ship.getMutableStats();
        stats.getHullDamageTakenMult().modifyMult(KEY,actualBuffNegative);
        stats.getShieldDamageTakenMult().modifyMult(KEY,actualBuffNegative);
        stats.getArmorDamageTakenMult().modifyMult(KEY,actualBuffNegative);

        stats.getBallisticWeaponDamageMult().modifyMult(KEY,actualBuffPositive);
        stats.getEnergyWeaponDamageMult().modifyMult(KEY,actualBuffPositive);
        stats.getMissileWeaponDamageMult().modifyMult(KEY,actualBuffPositive);
    }
}
