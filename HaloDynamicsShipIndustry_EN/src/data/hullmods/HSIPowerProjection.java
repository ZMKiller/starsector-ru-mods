package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.FullName;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.characters.SkillSpecAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.lazywizard.lazylib.combat.AIUtils;

public class HSIPowerProjection extends BaseHullMod {
    protected static final float FIGHTER_WING_RANGE_BONUS = 100f;

    //protected static final float SKILL_PROB = 20f;


    @Override
    public void applyEffectsBeforeShipCreation(ShipAPI.HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getFighterWingRange().modifyPercent(id,FIGHTER_WING_RANGE_BONUS);
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if(Math.random()>(1f-0.15f*amount*60f*((ship.isAlive())?1f:0.5f))){
            for(ShipAPI ally: AIUtils.getAlliesOnMap(ship)){
                if(ally.isFighter()) continue;
                if(ship.isAlive()) {
                    ally.getMutableStats().getFighterWingRange().modifyPercent("HSIPowerProjectionAllyBonus", FIGHTER_WING_RANGE_BONUS);
                }else{
                    ally.getMutableStats().getFighterWingRange().unmodify("HSIPowerProjectionAllyBonus");
                }
            }
        }
    }

    @Override
    public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) {
        if(!fighter.getCustomData().containsKey(id)){
            if(ship.getCaptain()!=null&&!ship.getCaptain().getStats().getSkillsCopy().isEmpty()){
                if(fighter.getCaptain()==null||fighter.getCaptain().isDefault()){
                    PersonAPI person = Global.getFactory().createPerson();
                    WeightedRandomPicker<String> picker = new WeightedRandomPicker<>();
                    picker.addAll(Global.getSettings().getFactionSpec("HSI").getAllPortraits(FullName.Gender.ANY));
                    person.setPortraitSprite(picker.pick());
                    fighter.setCaptain(person);
                }
                if(fighter.getCaptain()!=null){
                    PersonAPI person = fighter.getCaptain();
                    int level = 0;
                    for(MutableCharacterStatsAPI.SkillLevelAPI skill:ship.getCaptain().getStats().getSkillsCopy()){
                        if(skill.getSkill().isCombatOfficerSkill()){
                            person.getStats().setSkillLevel(skill.getSkill().getId(),skill.getLevel());
                            level++;
                        }
                    }
                    person.getStats().setLevel(level);
                    Global.getLogger(this.getClass()).info("FighterCaptain:"+(fighter.getCaptain()!=null&&fighter.getCaptain().isDefault())+"||"+((fighter.getCaptain()!=null)?fighter.getCaptain().getStats().getSkillsCopy().size():0));
                }
            }
            fighter.setCustomData(id,true);
        }
    }

    @Override
    public String getDescriptionParam(int index, ShipAPI.HullSize hullSize) {
        if(index == 0) return (int)FIGHTER_WING_RANGE_BONUS+"%";
        //if(index == 1) return (int)SKILL_PROB+"%";
        return null;
    }
}
