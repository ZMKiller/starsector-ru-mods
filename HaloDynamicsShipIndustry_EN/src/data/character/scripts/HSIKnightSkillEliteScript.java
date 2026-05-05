package data.character.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import com.fs.starfarer.api.combat.listeners.DamageListener;

import java.awt.*;

public class HSIKnightSkillEliteScript implements DamageListener {
    public static float LEVEL = 10f;
    private ShipAPI ship;
    public HSIKnightSkillEliteScript(ShipAPI ship){
        this.ship = ship;
    }

    private void clear(){
        Global.getCombatEngine().getListenerManager().removeListener(this);
    }
    @Override
    public void reportDamageApplied(Object source, CombatEntityAPI target, ApplyDamageResultAPI result) {
        if(ship.getCaptain()==null){
            clear();
            return;
        }
        if(ship.getCaptain().getStats()==null){
            clear();
            return;
        }
        boolean hasEliteKnightSkill = false;
        for(MutableCharacterStatsAPI.SkillLevelAPI skill:ship.getCaptain().getStats().getSkillsCopy()){
            if(skill.getSkill().getId().equals("HSI_Knight")||skill.getSkill().getId().equals("HSI_Knight_SP")){
                if(skill.getLevel()>1){
                    hasEliteKnightSkill = true;
                }
            }
        }
        if(!hasEliteKnightSkill){
            clear();
            return;
        }
        if(target instanceof ShipAPI){
            if(source instanceof DamagingProjectileAPI&&((DamagingProjectileAPI) source).getSource()!=null&&((DamagingProjectileAPI) source).getSource() == ship) {
                ShipAPI t = (ShipAPI) target;
                if (t.getHullLevel() <= LEVEL / 100f && result.getDamageToShields() <= 0 && result.getTotalDamageToArmor() <= 0 && result.getDamageToHull() > 0) {
                    Global.getCombatEngine().applyDamage(t, t.getLocation(), t.getMaxHitpoints() * 20f, DamageType.OTHER, 0, true, false,
                            null, false);
                    Global.getCombatEngine().addNebulaParticle(t.getLocation(), t.getVelocity(), t.getCollisionRadius(), 3f, 0f, 0f, Math.min(t.getCollisionRadius() / 50f, 6f), new Color(100, 125, 255, 255));
                }
            }

            if(source instanceof BeamAPI&&((BeamAPI) source).getSource()!=null&&((BeamAPI) source).getSource()==ship){
                ShipAPI t = (ShipAPI) target;
                if (t.getHullLevel() <= LEVEL / 100f && result.getDamageToShields() <= 0 && result.getTotalDamageToArmor() <= 0 && result.getDamageToHull() > 0) {
                    Global.getCombatEngine().applyDamage(t, t.getLocation(), t.getMaxHitpoints() * 20f, DamageType.OTHER, 0, true, false,
                            null, false);
                    Global.getCombatEngine().addNebulaParticle(t.getLocation(), t.getVelocity(), t.getCollisionRadius(), 3f, 0f, 0f, Math.min(t.getCollisionRadius() / 50f, 6f), new Color(100, 125, 255, 255));
                }
            }

            if(source instanceof ShipAPI&& source == ship){
                ShipAPI t = (ShipAPI) target;
                if (t.getHullLevel() <= LEVEL / 100f && result.getDamageToShields() <= 0 && result.getTotalDamageToArmor() <= 0 && result.getDamageToHull() > 0) {
                    Global.getCombatEngine().applyDamage(t, t.getLocation(), t.getMaxHitpoints() * 20f, DamageType.OTHER, 0, true, false,
                            null, false);
                    Global.getCombatEngine().addNebulaParticle(t.getLocation(), t.getVelocity(), t.getCollisionRadius(), 3f, 0f, 0f, Math.min(t.getCollisionRadius() / 50f, 6f), new Color(100, 125, 255, 255));
                }
            }
        }
    }
}
