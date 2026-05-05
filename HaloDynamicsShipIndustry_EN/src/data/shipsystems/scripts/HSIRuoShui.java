package data.shipsystems.scripts;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.IntervalUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;

import java.util.*;

public class HSIRuoShui extends BaseShipSystemScript {
    private ShipAPI ship = null;

    protected static final float SYSTEM_RANGE = 2000f;

    protected static final float DAMAGE = 100f;

    protected static final float MANUVER_DEBUFF = 0.8f;

    protected static final float SPEED_DEBUFF = 0.8f;

    protected static final float PHASEKEEP_DEBUFF = 1.33f;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if(ship==null){
            if(stats.getEntity() instanceof ShipAPI){
                ship = (ShipAPI) stats.getEntity();
            }else{
                return;
            }
        }
        HSIRuoShuiDebuffManager.getInstance(ship);
    }


    public static float getSystemRange(ShipAPI ship){
        return ship.getMutableStats().getSystemRangeBonus().computeEffective(SYSTEM_RANGE);
    }




    public static class HSIRuoShuiDebuffManager implements AdvanceableListener{

        private ShipAPI ship;

        private Map<ShipAPI,Float> ShipTimer = new HashMap<>();

        private Map<MissileAPI,Float> MissileTimer = new HashMap<>();

        public HSIRuoShuiDebuffManager(ShipAPI ship){
            this.ship = ship;
        }

        private IntervalUtil checkElapsed = new IntervalUtil(0.05f,0.05f);

        public static HSIRuoShuiDebuffManager getInstance(ShipAPI ship){
            if(ship.hasListenerOfClass(HSIRuoShuiDebuffManager.class)){
                return ship.getListeners(HSIRuoShuiDebuffManager.class).get(0);
            }else{
                HSIRuoShuiDebuffManager manager = new HSIRuoShuiDebuffManager(ship);
                ship.addListener(manager);
                return manager;
            }
        }

        @Override
        public void advance(float amount) {
            if(ship.getSystem() == null) return;
            checkElapsed.advance(amount);
            boolean isActive = ship.getSystem().getEffectLevel()>0;
            if(checkElapsed.intervalElapsed()){
                float level = 1f+2f*ship.getSystem().getEffectLevel();
                for (ShipAPI e : AIUtils.getNearbyEnemies(ship, getSystemRange(ship))){
                    applyShipDebuff(e,level);
                    if(ShipTimer.containsKey(e)){
                        ShipTimer.put(e,ShipTimer.get(e)+checkElapsed.getIntervalDuration()*(level-1f)/2f);
                        if(ShipTimer.get(e)>=1f&&e.isFighter()){
                            applyDamage(e,(level-1f)/2f);
                        }
                    }else{
                        ShipTimer.put(e,checkElapsed.getIntervalDuration()*(level-1f)/2f);
                    }
                }

                for(MissileAPI m:AIUtils.getNearbyEnemyMissiles(ship,getSystemRange(ship))){
                    applyMslDebuff(m,level);
                    if(MissileTimer.containsKey(m)){
                        MissileTimer.put(m,ShipTimer.get(m)+checkElapsed.getIntervalDuration()*(level-1f)/2f);
                        if(MissileTimer.get(m)>=1f){
                            applyDamage(m,(level-1f)/2f);
                        }
                    }else{
                        MissileTimer.put(m,checkElapsed.getIntervalDuration()*(level-1f)/2f);
                    }
                }

                for(DamagingProjectileAPI proj:Global.getCombatEngine().getProjectiles()){
                    if(proj.getOwner() == ship.getOwner()) continue;
                    if(MathUtils.getDistanceSquared(proj.getLocation(),ship.getLocation())>getSystemRange(ship)*getSystemRange(ship)){
                        applyProjDebuff(proj,level);
                    }
                }
                List<ShipAPI> toRemoveShip = new ArrayList<>();
                for (ShipAPI s : ShipTimer.keySet()) {
                    if (MathUtils.getDistanceSquared(ship.getLocation(), s.getLocation()) > (getSystemRange(ship) + s.getCollisionRadius() + ship.getCollisionRadius()) * (getSystemRange(ship) + s.getCollisionRadius() + ship.getCollisionRadius())) {
                        s.getMutableStats().getAcceleration().unmodify("HSI_RuoShui");
                        s.getMutableStats().getDeceleration().unmodify("HSI_RuoShui");
                        s.getMutableStats().getMaxTurnRate().unmodify("HSI_RuoShui");
                        s.getMutableStats().getTurnAcceleration().unmodify("HSI_RuoShui");
                        s.getMutableStats().getMaxSpeed().unmodify("HSI_RuoShui");
                        s.getMutableStats().getPhaseCloakUpkeepCostBonus().unmodify("HSI_RuoShui");
                        toRemoveShip.add(s);
                    }
                }
                for(ShipAPI s:toRemoveShip){
                    float l = ShipTimer.get(s);
                    if(s.isFighter()) applyDamage(s,l);
                    ShipTimer.remove(s);
                }
                List<MissileAPI> toRemoveMsl = new ArrayList<>();
                for (MissileAPI s : MissileTimer.keySet()) {
                    if (MathUtils.getDistanceSquared(ship.getLocation(), s.getLocation()) > (getSystemRange(ship) + s.getCollisionRadius() + ship.getCollisionRadius()) * (getSystemRange(ship) + s.getCollisionRadius() + ship.getCollisionRadius())) {
                        s.getEngineStats().getAcceleration().unmodify("HSI_RuoShui");
                        s.getEngineStats().getDeceleration().unmodify("HSI_RuoShui");
                        s.getEngineStats().getMaxTurnRate().unmodify("HSI_RuoShui");
                        s.getEngineStats().getTurnAcceleration().unmodify("HSI_RuoShui");
                        s.getEngineStats().getMaxSpeed().unmodify("HSI_RuoShui");
                        toRemoveMsl.add(s);
                    }
                }
                for(MissileAPI s:toRemoveMsl){
                    float l = ShipTimer.get(s);
                    applyDamage(s,l);
                    MissileTimer.remove(s);
                }
            }
        }

        protected void applyDamage(CombatEntityAPI target,float level){
            Global.getCombatEngine().applyDamage(target, target.getLocation(), DAMAGE*level, DamageType.OTHER, 0, true, false,
                    ship, false);
        }

        protected void applyShipDebuff(ShipAPI e,float level){
            e.getMutableStats().getAcceleration().modifyMult("HSI_RuoShui",1f-(1f-MANUVER_DEBUFF)*level);
            e.getMutableStats().getDeceleration().modifyMult("HSI_RuoShui",1f-(1f-MANUVER_DEBUFF)*level);
            e.getMutableStats().getMaxTurnRate().modifyMult("HSI_RuoShui",1f-(1f-MANUVER_DEBUFF)*level);
            e.getMutableStats().getTurnAcceleration().modifyMult("HSI_RuoShui",1f-(1f-MANUVER_DEBUFF)*level);
            e.getMutableStats().getMaxSpeed().modifyMult("HSI_RuoShui",1f-(1f-SPEED_DEBUFF)*level);
            e.getMutableStats().getPhaseCloakUpkeepCostBonus().modifyMult("HSI_RuoShui",1f+(PHASEKEEP_DEBUFF-1f)*level);
        }

        protected void applyProjDebuff(DamagingProjectileAPI p, float level){
            if(p.getVelocity().length()>(1f-(1f-SPEED_DEBUFF)*level)*p.getProjectileSpec().getMoveSpeed((p.getSource()!=null)?p.getSource().getMutableStats() : null,p.getWeapon())){
                p.getVelocity().scale((1f-(1f-SPEED_DEBUFF)*level)*p.getProjectileSpec().getMoveSpeed((p.getSource()!=null)?p.getSource().getMutableStats() : null,p.getWeapon())/p.getVelocity().length());
            }
        }

        protected void applyMslDebuff(MissileAPI m, float level){
            m.getEngineStats().getAcceleration().modifyMult("HSI_RuoShui",1f-(1f-MANUVER_DEBUFF)*level);
            m.getEngineStats().getDeceleration().modifyMult("HSI_RuoShui",1f-(1f-MANUVER_DEBUFF)*level);
            m.getEngineStats().getMaxTurnRate().modifyMult("HSI_RuoShui",1f-(1f-MANUVER_DEBUFF)*level);
            m.getEngineStats().getTurnAcceleration().modifyMult("HSI_RuoShui",1f-(1f-MANUVER_DEBUFF)*level);
            m.getEngineStats().getMaxSpeed().modifyMult("HSI_RuoShui",1f-(1f-SPEED_DEBUFF)*level);
        }












    }
}
