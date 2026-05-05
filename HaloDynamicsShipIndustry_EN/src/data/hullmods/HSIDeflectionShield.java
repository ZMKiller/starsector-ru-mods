package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import data.kit.AjimusUtils;
import data.kit.HSIBounds;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.RippleDistortion;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.EllipseUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.Iterator;
import java.util.List;
import java.util.EnumSet;

public class HSIDeflectionShield implements AdvanceableListener {
    private ShipAPI ship;

    private float a=0,b = 0;

    //private IntervalUtil checker = new IntervalUtil(0.05f,0.05f);

    private CombatEngineAPI engine;

    private HSIBounds bound;


    public HSIDeflectionShield(ShipAPI ship){
        this.ship = ship;

        a = 0.8f*ship.getSpriteAPI().getWidth();
        b = 0.9f*ship.getSpriteAPI().getHeight();

        //isFonX = ship.getSpriteAPI().getWidth()>=ship.getSpriteAPI().getHeight();
        //if(a<=b) a = b;
        //c = (float)((a == b)?0:Math.sqrt(a*a-b*b));
        engine = Global.getCombatEngine();
        if(ship.getShield()!=null){
            ship.getShield().setRadius(1f);
        }

        bound = new HSIBounds(ship);
        Vector2f zero = EllipseUtils.getPointOnEllipse(ship.getLocation(),a,b,ship.getFacing()-90f,0);
        bound.addSegment(zero,EllipseUtils.getPointOnEllipse(ship.getLocation(),a,b,ship.getFacing()-90f,20));
        for(int i = 2;i<18;i++){
            float a1 = i*20;
            Vector2f point =  EllipseUtils.getPointOnEllipse(ship.getLocation(),a,b,ship.getFacing()-90f,a1);
            bound.addSegment(point.x,point.y);
        }
        bound.addSegment(zero.x,zero.y);
        Global.getCombatEngine().addLayeredRenderingPlugin(new HSIDeflectionShieldRenderPlugin(ship,this));
    }

    @Override
    public void advance(float amount) {
        if(engine == null) return;
        if(engine.isPaused()) return;
        bound.update(ship.getLocation(),ship.getFacing());
        if(!ship.getShield().isOn()){
            ship.getMutableStats().getTimeMult().unmodify("DeflectionShieldTest");
            return;
        }else{
            ship.getMutableStats().getTimeMult().modifyMult("DeflectionShieldTest",1.01f);
        }
        //checker.advance(amount);
        //if(checker.intervalElapsed()) {
        for (DamagingProjectileAPI proj : engine.getProjectiles()) {
                if (proj.getOwner() != ship.getOwner()) {
                    if(MathUtils.getDistanceSquared(ship.getLocation(),proj.getLocation())<1.21f*Math.max(a,b)*Math.max(a,b)) {
                        if (EllipseUtils.isPointWithinEllipse(proj.getLocation(), ship.getLocation(), a, b, ship.getFacing() - 90f)) {
                            //float absorbLevel = 1f-ship.getFluxLevel();
                            float damageTaken = computeDamageToShield(proj.getDamage(), proj.getSource(), false);
                            float empDamageTaken = computeEMPDamageTaken(proj);
                            if (proj.getProjectileSpec() != null && proj.getProjectileSpec().getOnHitEffect() != null) {
                                proj.getProjectileSpec().getOnHitEffect().onHit(proj, ship, proj.getLocation(), true, new HSIDeflectionShieldDamageResultForOnhitOnly(damageTaken, empDamageTaken), engine);
                            }
                            ship.getFluxTracker().increaseFlux(AjimusUtils.EnsurePositive(damageTaken), true);
                            createHitRipple(new Vector2f(proj.getLocation()), ship.getVelocity(), damageTaken,
                                    proj.getDamage().getType(),
                                    VectorUtils.getFacing(VectorUtils.getDirectionalVector(ship.getLocation(),
                                            new Vector2f(proj.getLocation()))),
                                    ship.getCollisionRadius());
                            Global.getCombatEngine().removeEntity(proj);
                            //float leakDamage = damageTaken*(1f-absorbLevel);
                            //engine.spawnEmpArcPierceShields();
                        }
                    }
                }
        }
        //}
        for (BeamAPI beam : engine.getBeams()) {
            if (beam.getSource().getOwner() != ship.getOwner()) {
                //do some check first
                if(CollisionUtils.getCollides(beam.getFrom(),beam.getTo(),ship.getLocation(),1.1f*Math.max(a,b))){
                    Vector2f collide = getCollisionPoint(beam.getFrom(),beam.getTo());
                    if(collide!=null){
                        beam.getTo().set(collide);
                        float damageTaken = computeDamageToShield(beam.getDamage(),beam.getSource(),true);
                        ship.getFluxTracker().increaseFlux(AjimusUtils.EnsurePositive(damageTaken),false);
                    }
                }
            }
        }
    }

    public HSIBounds getBound() {
        return bound;
    }

    public Vector2f getCollisionPoint(Vector2f lineStart,
                                      Vector2f lineEnd)
    {
        Vector2f closestIntersection = null;

        bound.update(ship.getLocation(), ship.getFacing());
        for (BoundsAPI.SegmentAPI tmp : bound.getSegments())
        {
            Vector2f intersection
                    = CollisionUtils.getCollisionPoint(lineStart, lineEnd, tmp.getP1(), tmp.getP2());
            if (intersection != null)
            {
                if (closestIntersection == null)
                {
                    closestIntersection = new Vector2f(intersection);
                }
                else if (MathUtils.getDistanceSquared(lineStart, intersection)
                        < MathUtils.getDistanceSquared(lineStart, closestIntersection))
                {
                    closestIntersection.set(intersection);
                }
            }
        }

        return closestIntersection;
    }


    private float computeDamageToShield(DamageAPI damage,ShipAPI source,boolean isBeam){
        float base = damage.getDamage();
        if(isBeam){
            if(damage.getDamage()>0){
                base = damage.getDpsDuration()*damage.getDamage();
            }
        }
        if(base == 0){
            return 0;
        }
        float mult = 1;
        if(source!=null){
            switch (ship.getHullSize()){
                case DEFAULT:
                    break;
                case FIGHTER:
                    mult*=source.getMutableStats().getDamageToFighters().getModifiedValue();
                    break;
                case FRIGATE:
                    mult*=source.getMutableStats().getDamageToFrigates().getModifiedValue();
                    break;
                case DESTROYER:
                    mult*=source.getMutableStats().getDamageToDestroyers().getModifiedValue();
                    break;
                case CRUISER:
                    mult*=source.getMutableStats().getDamageToCruisers().getModifiedValue();
                    break;
                case CAPITAL_SHIP:
                    mult*=source.getMutableStats().getDamageToCapital().getModifiedValue();
                    break;
            }
        }
        switch (damage.getType()){
            case KINETIC:
                mult*=ship.getMutableStats().getKineticShieldDamageTakenMult().getModifiedValue();
                break;
            case HIGH_EXPLOSIVE:
                mult*=ship.getMutableStats().getHighExplosiveShieldDamageTakenMult().getModifiedValue();
                break;
            case FRAGMENTATION:
                mult*=ship.getMutableStats().getFragmentationShieldDamageTakenMult().getModifiedValue();
                break;
            case ENERGY:
                mult*=ship.getMutableStats().getEnergyShieldDamageTakenMult().getModifiedValue();
                break;
            case OTHER:
                break;
        }
        mult*=damage.getType().getShieldMult();
        if(isBeam){
            mult*=ship.getMutableStats().getBeamDamageTakenMult().getModifiedValue();
        }else{
            mult*=ship.getMutableStats().getProjectileDamageTakenMult().getModifiedValue();
        }
        if(ship.getShield()!=null){
            mult*=ship.getShield().getFluxPerPointOfDamage();
        }else{
            mult*=ship.getMutableStats().getShieldDamageTakenMult().getModifiedValue();
        }
        return base*mult;
    }

    private float computeEMPDamageTaken(DamagingProjectileAPI proj){
        float base = proj.getEmpAmount();
        float mult = ship.getMutableStats().getEmpDamageTakenMult().getModifiedValue();
        return base*mult;
    }

    private void createHitRipple(Vector2f location, Vector2f velocity, float damage, DamageType type, float direction,
                                 float shieldRadius) {
        float dmg = damage;
        if (type == DamageType.FRAGMENTATION) {
            dmg *= 0.25f;
        }
        if (type == DamageType.HIGH_EXPLOSIVE) {
            dmg *= 0.5f;
        }
        if (type == DamageType.KINETIC) {
            dmg *= 2f;
        }

        if (dmg < 75f) {
            return;
        }

        float fadeTime = (float) Math.pow(dmg, 0.25) * 0.1f;
        float size = (float) Math.pow(dmg, 0.3333333) * 8f;

        float ratio = Math.min(size / shieldRadius, 1f);
        float arc = 90f - ratio * 14.54136f; // Don't question the magic number

        float start1 = direction - arc;
        if (start1 < 0f) {
            start1 += 360f;
        }
        float end1 = direction + arc;
        if (end1 >= 360f) {
            end1 -= 360f;
        }

        float start2 = direction + arc;
        if (start2 < 0f) {
            start2 += 360f;
        }
        float end2 = direction - arc;
        if (end2 >= 360f) {
            end2 -= 360f;
        }

        RippleDistortion ripple = new RippleDistortion(location, velocity);
        ripple.setSize(size);
        ripple.setIntensity(size * 0.2f);
        ripple.setFrameRate(60f / fadeTime);
        ripple.fadeInSize(fadeTime * 1.2f);
        ripple.fadeOutIntensity(fadeTime);
        ripple.setSize(size * 0.2f);
        ripple.setArc(start1, end1);
        DistortionShader.addDistortion(ripple);

        ripple = new RippleDistortion(location, velocity);
        ripple.setSize(size);
        ripple.setIntensity(size * 0.05f);
        ripple.setFrameRate(60f / fadeTime);
        ripple.fadeInSize(fadeTime * 1.2f);
        ripple.fadeOutIntensity(fadeTime);
        ripple.setSize(size * 0.2f);
        ripple.setArc(start2, end2);
        DistortionShader.addDistortion(ripple);
    }



    //private static boolean isProjectileWithInShield(DamagingProjectileAPI projectile,ShipAPI ship,Vector2f F1,Vector2f F2,float a){
        //return (Misc.getDistance(projectile.getLocation(),F1)+Misc.getDistance(projectile.getLocation(),F2))<=2*a;
    //}

    //private static boolean isLocationWithInShield(Vector2f location,ShipAPI ship,Vector2f F1,Vector2f F2,float a){
        //return (Misc.getDistance(location,F1)+Misc.getDistance(location,F2))<=2*a;
    //}
    public static class HSIDeflectionShieldRenderPlugin implements CombatLayeredRenderingPlugin{

        private ShipAPI ship;
        private HSIDeflectionShield deflectionShield;

        private float elapsed = 0;

        private float alpha = 0;

        public HSIDeflectionShieldRenderPlugin(ShipAPI ship,HSIDeflectionShield deflectionShield){
            this.ship = ship;
            this.deflectionShield =deflectionShield;
        }


        @Override
        public void init(CombatEntityAPI anchor) {

        }

        @Override
        public void cleanup() {

        }

        @Override
        public boolean isExpired() {
            return !ship.isAlive();
        }

        @Override
        public void advance(float amount) {
            if(ship.getShield()!=null&&ship.getShield().isOn()){
                elapsed+=amount;
                if(elapsed<0.5f){
                    alpha = elapsed/0.5f;
                }else{
                    alpha = 1;
                }
            }else{
                elapsed = 0;
            }
            alpha*=ship.getCombinedAlphaMult();
            //deflectionShield.advance(amount);
        }

        @Override
        public EnumSet<CombatEngineLayers> getActiveLayers() {
            return EnumSet.of(CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER);
        }

        @Override
        public float getRenderRadius() {
            return 10000000f;
        }

        @Override
        public void render(CombatEngineLayers Layer, ViewportAPI viewport) {
            if(ship.getShield().isOn()&&Layer == CombatEngineLayers.ABOVE_SHIPS_AND_MISSILES_LAYER) {
                BoundsAPI bound = deflectionShield.getBound();
                Color inner = new Color(175, 175, 225, 75);
                Color ring = ship.getShield().getRingColor();
                //Global.getLogger(this.getClass()).info("Alpha:"+alpha);
                GL11.glPushMatrix();
                GL11.glDisable(GL11.GL_TEXTURE_2D);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
                //GL11.glBegin(GL11.GL_TRIANGLE_FAN);
                GL11.glLineWidth(4f);
                GL11.glBegin(GL11.GL_LINE_STRIP);
                //Global.getLogger(this.getClass()).info("Begin Frame."+ship.getLocation());
                Misc.setColor(inner, AjimusUtils.EnsurePositive(alpha));
                //GL11.glVertex2f(ship.getLocation().getX(),ship.getLocation().getY());
                List<BoundsAPI.SegmentAPI> segments = bound.getSegments();
                Iterator<BoundsAPI.SegmentAPI> iterator = segments.iterator();
                while (iterator.hasNext()) {
                    BoundsAPI.SegmentAPI seg = iterator.next();
                    Misc.setColor(inner, AjimusUtils.EnsurePositive(alpha));
                    GL11.glVertex2f(seg.getP1().getX(), seg.getP1().getY());
                    //Global.getLogger(this.getClass()).info("Vec:"+seg.getP1());
                    if (!iterator.hasNext()) {
                        Misc.setColor(inner, AjimusUtils.EnsurePositive(alpha));
                        GL11.glVertex2f(seg.getP2().getX(), seg.getP2().getY());
                        //Global.getLogger(this.getClass()).info("Vec:"+seg.getP2());
                    }
                }
                //Global.getLogger(this.getClass()).info("End Frame.");
                GL11.glEnd();
                GL11.glPopMatrix();
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glEnable(GL11.GL_BLEND);
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            }
        }
    }

    public static class HSIDeflectionShieldDamageResultForOnhitOnly implements ApplyDamageResultAPI{
        private float s,e;

        public HSIDeflectionShieldDamageResultForOnhitOnly(float shieldDamage,float empDamage){
            this.s  = shieldDamage;
            this.e = empDamage;
        }

        @Override
        public float getDamageToHull() {
            return 0;
        }

        @Override
        public float getTotalDamageToArmor() {
            return 0;
        }

        @Override
        public float getDamageToPrimaryArmorCell() {
            return 0;
        }

        @Override
        public float getDamageToShields() {
            return s;
        }

        @Override
        public void setDamageToHull(float v) {

        }

        @Override
        public void setTotalDamageToArmor(float v) {

        }

        @Override
        public void setDamageToPrimaryArmorCell(float v) {

        }

        @Override
        public void setDamageToShields(float v) {

        }

        @Override
        public float getEmpDamage() {
            return e;
        }

        @Override
        public void setEmpDamage(float v) {

        }

        @Override
        public DamageType getType() {
            return null;
        }

        @Override
        public void setType(DamageType damageType) {

        }

        @Override
        public float getOverMaxDamageToShields() {
            return 0;
        }

        @Override
        public void setOverMaxDamageToShields(float v) {

        }

        @Override
        public boolean isDps() {
            return false;
        }

        @Override
        public void setDps(boolean b) {

        }
    }
}
