package data.ai.HSISystemAI;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.thoughtworks.xstream.mapper.Mapper;
import data.hullmods.HSITurbulanceShieldListenerV2;
import data.kit.HSII18nUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

import java.util.ArrayList;
import java.util.List;

public class HSIFortressShieldAI implements ShipSystemAIScript {

    private ShipAPI ship;
    private CombatEngineAPI engine;
    private ShipSystemAPI system;

    private IntervalUtil threatChecker = new IntervalUtil(0.1f, 0.2f);

    private int skipShipCheck = 5;

    private Vector2f weightedDefensiveDir = new Vector2f();

    private boolean shouldDo = false;

    private List<WeaponAPI> threats = new ArrayList<>();

    //private PIDController controller = new PIDController(2f, 2f, 6f, 0.5f);

    private int r = 0;

    private float keep = 0;

    private HSITurbulanceShieldListenerV2 shield = null;

    private boolean isSystem = false;


    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        // this.flags = flags;
        this.engine = engine;
        isSystem = ship.getSystem()!=null&&ship.getSystem().getSpecAPI().getId().equals("HSI_FortifiedShield");
        if(isSystem){
            this.system = system;
        }else {
            this.system = ship.getPhaseCloak();
        }
        r = (Math.random() > 0.5f) ? -1 : 1;
    }

    private float armor = 0;

    @Override
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        //Global.getLogger(this.getClass()).info(ship.getLocation()+"||"+missileDangerDir+"||"+collisionDangerDir);
        if (HSITurbulanceShieldListenerV2.hasShield(ship) && shield == null) {
            shield = HSITurbulanceShieldListenerV2.getInstance(ship);
        }
        if (shield == null) return;
        armor = shield.getStats().getShieldArmorValue().computeEffective(0);
        threatChecker.advance(amount);
        //Global.getCombatEngine().headInDirectionWithoutTurning(ship, VectorUtils.getFacing(weightedDefensiveDir), ship.getMaxSpeed());
        keep = Math.max(0, keep - amount);
        shouldDo = false;
        if (!threatChecker.intervalElapsed()){

        }else {
            weightedDefensiveDir = new Vector2f();
            if (collisionDangerDir != null) {
                Vector2f.add(weightedDefensiveDir, collisionDangerDir, weightedDefensiveDir);
            }
            float potential = 0;
            for (DamagingProjectileAPI proj : engine.getProjectiles()) {
                if (proj.getOwner() == ship.getOwner())
                    continue;
                float dist = MathUtils.getDistanceSquared(proj.getLocation(), ship.getLocation());
                if (dist > 1000000)
                    continue;
                if (dist < 360000||((proj instanceof MissileAPI)&&((MissileAPI) proj).getSpec().getBehaviorJSON()!=null&&((MissileAPI) proj).getSpec().getBehaviorJSON().has("payloadWeaponId"))) {
                    float damage = ((proj.getDamageType().equals(DamageType.FRAGMENTATION)) ? 0.25f : 1f)
                            * proj.getDamage().getDamage();
                    damage*=(damage/(damage+armor));
                    potential+=damage;
                }
                Vector2f dir = VectorUtils.getDirectionalVector(ship.getLocation(), proj.getLocation());
                VectorUtils.rotate(dir, 70f + 20f * (float) Math.random() * r, dir);
                dir.scale(proj.getDamageAmount() / 10f);
                Vector2f.add(weightedDefensiveDir, dir, weightedDefensiveDir);
            }

            for (BeamAPI beam : engine.getBeams()) {
                if (beam.getSource().getOwner() == ship.getOwner()) continue;
                Vector2f nearest = MathUtils.getNearestPointOnLine(ship.getLocation(), beam.getFrom(), beam.getTo());
                if (MathUtils.getDistance(ship.getLocation(), nearest) < ship.getCollisionRadius() * 1.2f) {
                    float damage = ((beam.getDamage().getType().equals(DamageType.FRAGMENTATION)) ? 0.25f : 1f)
                            * beam.getDamage().getDamage()*0.5f;
                    damage*=(damage/(damage+armor));
                    potential+=damage;
                }

                Vector2f dir = VectorUtils.getDirectionalVector(nearest, ship.getLocation());
                dir.scale(beam.getDamage().getDamage() / 10f);
                Vector2f.add(weightedDefensiveDir, dir, weightedDefensiveDir);
            }
            if (skipShipCheck <= 0) {
                skipShipCheck = 5;
                threats.clear();
                for (ShipAPI s : AIUtils.getNearbyEnemies(ship, 3000f)) {
                    for (WeaponAPI weapon : s.getAllWeapons()) {
                        if (weapon.isDisabled()) continue;
                        if (weapon.isDecorative()) continue;
                        if (weapon.getType().equals(WeaponAPI.WeaponType.MISSILE)) continue;
                        if (weapon.getSpec().getAIHints().contains(WeaponAPI.AIHints.PD_ONLY)) continue;
                        threats.add(weapon);
                    }
                }
            } else {
                skipShipCheck--;
            }
            for(WeaponAPI weapon:threats){
                if(Misc.getDistance(ship.getLocation(),weapon.getLocation())<=weapon.getRange()+ship.getCollisionRadius()&&Math.abs(weapon.distanceFromArc(ship.getLocation()))<=10f) {
                    if (weapon.getCooldownRemaining() <= 0.01f) {
                        float damage = weapon.getDamage().getDamage();
                        if(weapon.getDamage().isDps()) damage*=0.5f;
                        damage *= (weapon.getDamage().getDamage() / (weapon.getDamage().getDamage() + armor));
                        potential += damage;
                    } else if (weapon.isFiring()) {
                        float damage = weapon.getDamage().getDamage();
                        if(weapon.getDamage().isDps()) damage*=0.5f;
                        damage *= (weapon.getDamage().getDamage() / (weapon.getDamage().getDamage() + armor));
                        potential += damage;
                    }
                }
            }

            float lim = shield.getShield().getShieldCap()* 0.15f * shield.getShield().getShieldLevel();

            if (shield.getShield().getShieldLevel() < 0.5f) {
                lim *= 0.6f;
            }
            shouldDo = potential >= lim ;
            if(shouldDo) keep = 0.2f;
            //Global.getLogger(this.getClass()).info("ShouldDo1:"+shouldDo+"||Potential:"+potential+"||Lim:"+lim);
        }

        shouldDo = shouldDo|| keep > 0;
        //Global.getLogger(this.getClass()).info("ShouldDo2:"+shouldDo+"||Keep:"+keep);
        shouldDo = ship.getHardFluxLevel() < 0.9f && shouldDo;
        shouldDo = shouldDo && shield.getShield().getShieldLevel() >= 0.05f;
        /*String content = potential+"/"+lim;
        Global.getCombatEngine().maintainStatusForPlayerShip("HSIFortifiedShield",
                "graphics/icons/hullsys/fortress_shield.png", HSII18nUtil.getShipSystemString("HSIFortifiedShieldName"), content,
                false);*/

        if (shouldDo) {
            if (!system.isOn()) {
                if(isSystem){
                    ship.useSystem();
                }
                else{
                    ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, ship.getMouseTarget(), 0);
                }
                keep = 2.5f;
            }
        } else {
            if (system.isActive()) {
                if(isSystem){
                    ship.useSystem();
                }
                else{
                    ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, ship.getMouseTarget(), 0);
                }
            }
        }
        //Global.getLogger(this.getClass()).info(shouldDo+"|"+rprScale/totalScale+"|"+keep);
    }
}
