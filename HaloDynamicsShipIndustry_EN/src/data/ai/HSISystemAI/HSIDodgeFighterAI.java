package data.ai.HSISystemAI;

import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.subsystems.drones.PIDController;
import org.magiclib.util.MagicTargeting;

public class HSIDodgeFighterAI implements ShipSystemAIScript {
    private ShipAPI ship;
    private CombatEngineAPI engine;
    private ShipSystemAPI system;

    private IntervalUtil threatChecker = new IntervalUtil(0.05f, 0.1f);

    private Vector2f weightedDefensiveDir = new Vector2f();

    private PIDController controller = new PIDController(2f, 2f, 6f, 0.5f);

    private int r = 0;


    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        // this.flags = flags;
        this.engine = engine;
        this.system = ship.getPhaseCloak();
        r = (Math.random()>0.5f)?-1:1;
    }

    // private float sinceLast = 0f;

    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        boolean pulledBack = false;
        if(ship.isFighter()&&ship.getWing()!=null&&ship.getWing().getSourceShip()!=null){
            ShipAPI source = ship.getWing().getSourceShip();
            pulledBack = source.isPullBackFighters();
        }
        if(!pulledBack){
            ShipAPI t = MagicTargeting.pickTarget(ship, MagicTargeting.targetSeeking.IGNORE_SOURCE,300,360,10,1,1,1,1,true);
            if(t!=null){
                ship.setShipTarget(t);
            }
        }
        //normal movement
        weightedDefensiveDir = new Vector2f();
        float potential = 0;
        for (DamagingProjectileAPI proj : engine.getProjectiles()) {
            if (proj.getOwner() == ship.getOwner())
                continue;
            float dist = MathUtils.getDistanceSquared(proj.getLocation(), ship.getLocation());
            if ( dist>90000)
                continue;
            if(dist<2.5f*ship.getCollisionRadius()*ship.getCollisionRadius()) {
                potential += ((proj.getDamageType().equals(DamageType.FRAGMENTATION)) ? 0.25f : 1f)
                        * proj.getDamage().getDamage();
            }
            Vector2f dir = VectorUtils.getDirectionalVector(ship.getLocation(),proj.getLocation());
            VectorUtils.rotate(dir,70f+20f*(float)Math.random()*r,dir);
            dir.scale(proj.getDamageAmount()/10f);
            Vector2f.add(weightedDefensiveDir,dir,weightedDefensiveDir);
        }

        for(BeamAPI beam:engine.getBeams()){
            if(beam.getSource().getOwner()==ship.getOwner()) continue;
            Vector2f nearest = MathUtils.getNearestPointOnLine(ship.getLocation(),beam.getFrom(),beam.getTo());
            if(MathUtils.getDistance(ship.getLocation(),nearest)<ship.getCollisionRadius()*1.2f){
                potential+=beam.getDamage().getDamage()/2f;
            }
            Vector2f dir = VectorUtils.getDirectionalVector(nearest,ship.getLocation());
            dir.scale(beam.getDamage().getDamage()/20f);
            Vector2f.add(weightedDefensiveDir,dir,weightedDefensiveDir);
        }


        //system burst movement
        if (system.getCooldownRemaining() > 0)
            return;
        if (system.isOutOfAmmo())
            return;
        if (system.isActive())
            return;
        boolean shouldDo = false;
        threatChecker.advance(amount);
        if (threatChecker.intervalElapsed()) {
            float lim = 25f;
            shouldDo = potential>=lim;
        }
        if (shouldDo) {
            ship.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.SYSTEM_TARGET_COORDS,1f,Vector2f.add(ship.getLocation(),weightedDefensiveDir,null));
            ship.giveCommand(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK, ship.getMouseTarget(), 0);
        }
    }

}
