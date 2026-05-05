package data.weapons.scripts.proj;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.AsteroidAPI;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI;
import com.fs.starfarer.api.impl.combat.NegativeExplosionVisual;
import com.fs.starfarer.api.impl.combat.RiftCascadeMineExplosion;
import com.fs.starfarer.api.impl.combat.dweller.DwellerShroud;
import com.fs.starfarer.api.impl.combat.dweller.RiftLightningEffect;
import com.fs.starfarer.api.impl.combat.dweller.ShroudedThunderheadHullmod;
import com.fs.starfarer.api.impl.combat.threat.EnergyLashSystemScript;
import com.fs.starfarer.api.impl.combat.threat.RoilingSwarmEffect;
import com.fs.starfarer.api.impl.combat.threat.RoilingSwarmEffect.SwarmMember;
import com.fs.starfarer.api.loading.DamagingExplosionSpec;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.kit.HSIAutoFireTargetPicker;
import data.kit.HSIIds;
import org.lazywizard.lazylib.combat.AIUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.Iterator;

import static com.fs.starfarer.api.impl.combat.dweller.ShroudedThunderheadHullmod.FLUX_PER_DAMAGE;
import static com.fs.starfarer.api.impl.combat.dweller.ShroudedThunderheadHullmod.getFluxCost;

public class HWIMassDriverProjectile implements OnFireEffectPlugin, ProximityExplosionEffect,OnHitEffectPlugin{
    @Override
    public void onHit(DamagingProjectileAPI projectile, CombatEntityAPI target, Vector2f point, boolean shieldHit,
                      ApplyDamageResultAPI damageResult, CombatEngineAPI engine) {
        if(projectile.getSource()!=null&&projectile.getSource().getVariant().hasHullMod("shrouded_thunderhead")&&Math.random()>=0.2f){
                spawnLightning(projectile.getSource(),point);
        }
    }

    protected boolean deductFlux(ShipAPI ship, float fluxCost) {
        if (!ship.getFluxTracker().increaseFlux(fluxCost, false)) {
            return false;
        }
        return true;
    }

    public void spawnLightning(ShipAPI ship, Vector2f hit) {
        CombatEngineAPI engine = Global.getCombatEngine();

        Vector2f from = ship.getLocation();
        Vector2f point = hit;

        float dist = Misc.getDistance(from, point);


        float mult = ShroudedThunderheadHullmod.getPowerMult(ship.getHullSize());
        float damage = ShroudedThunderheadHullmod.getDamage(ship.getHullSize());
        float emp = ShroudedThunderheadHullmod.getEMPDamage(ship.getHullSize());

        if (FLUX_PER_DAMAGE > 0f) {
            float fluxCost = getFluxCost(ship.getHullSize());
            if (!deductFlux(ship, fluxCost)) {
                return;
            }
        }

        DwellerShroud shroud = DwellerShroud.getShroudFor(ship);
        if (shroud != null) {
            float angle = Misc.getAngleInDegrees(ship.getLocation(), point);
            from = Misc.getUnitVectorAtDegreeAngle(angle + 90f - 180f * (float) Math.random());
            from.scale((0.5f + (float) Math.random() * 0.25f) * shroud.getShroudParams().maxOffset * shroud.getShroudParams().overloadArcOffsetMult);
            Vector2f.add(ship.getLocation(), from, from);
        }


        float arcSpeed = RiftLightningEffect.RIFT_LIGHTNING_SPEED;

        EmpArcEntityAPI.EmpArcParams params = new EmpArcEntityAPI.EmpArcParams();
        params.segmentLengthMult = 8f;
        params.zigZagReductionFactor = 0.15f;
        //params.fadeOutDist = ship.getCollisionRadius() * 0.5f;
        params.fadeOutDist = 50f;
        params.minFadeOutMult = 10f;
//		params.flickerRateMult = 0.7f;
        params.flickerRateMult = 0.3f;
//		params.flickerRateMult = 0.05f;
//		params.glowSizeMult = 3f;
//		params.brightSpotFullFraction = 0.5f;

        params.movementDurOverride = Math.max(0.05f, dist / arcSpeed);

        float arcWidth = 40f + mult * 40f;
        float explosionRadius = 40f + mult * 40f;

        //Color color = weapon.getSpec().getGlowColor();
        Color color = RiftLightningEffect.RIFT_LIGHTNING_COLOR;
        EmpArcEntityAPI arc = (EmpArcEntityAPI)engine.spawnEmpArcVisual(from, ship, point, null,
                arcWidth, // thickness
                color,
                new Color(255,255,255,255),
                params
        );
        arc.setCoreWidthOverride(arcWidth / 2f);

        arc.setRenderGlowAtStart(false);
        arc.setFadedOutAtStart(true);
        arc.setSingleFlickerMode(true);

        float volume = 0.75f + 0.25f * mult;
        float pitch = 1f + 0.25f * (1f - mult);
        Global.getSoundPlayer().playSound("rift_lightning_fire", pitch, volume, from, ship.getVelocity());

        if (shroud != null) {
            DwellerShroud.DwellerShroudParams shroudParams = shroud.getShroudParams();
            params = new EmpArcEntityAPI.EmpArcParams();
            params.segmentLengthMult = 4f;
            params.glowSizeMult = 4f;
            params.flickerRateMult = 0.5f + (float) Math.random() * 0.5f;
            params.flickerRateMult *= 1.5f;

            //Color fringe = shroudParams.overloadArcFringeColor;
            Color fringe = color;
            Color core = Color.white;

            float thickness = shroudParams.overloadArcThickness;

            //Vector2f to = Misc.getPointAtRadius(from, 1f);

            float angle = Misc.getAngleInDegrees(from, ship.getLocation());
            angle = angle + 90f * ((float) Math.random() - 0.5f);
            Vector2f dir = Misc.getUnitVectorAtDegreeAngle(angle);
            dist = shroudParams.maxOffset * shroud.getShroudParams().overloadArcOffsetMult;
            dist = dist * 0.5f + dist * 0.5f * (float) Math.random();
            //dist *= 1.5f;
            dist *= 0.5f;
            dir.scale(dist);
            Vector2f to = Vector2f.add(from, dir, new Vector2f());

            arc = (EmpArcEntityAPI)engine.spawnEmpArcVisual(
                    from, ship, to, ship, thickness, fringe, core, params);

            arc.setCoreWidthOverride(shroudParams.overloadArcCoreThickness);
            arc.setSingleFlickerMode(false);
            //arc.setRenderGlowAtStart(false);
        }



        float explosionDelay = params.movementDurOverride * 0.8f;
        Global.getCombatEngine().addPlugin(new EnergyLashSystemScript.DelayedCombatActionPlugin(explosionDelay, new Runnable() {
            @Override
            public void run() {
                DamagingExplosionSpec spec = new DamagingExplosionSpec(
                        0.1f, // duration
                        explosionRadius, // radius
                        explosionRadius * 0.5f, // coreRadius
                        damage, // maxDamage
                        damage / 2f, // minDamage
                        CollisionClass.PROJECTILE_NO_FF, // collisionClass
                        CollisionClass.GAS_CLOUD, // collisionClassByFighter - using to flag it as from this effect
                        3f, // particleSizeMin
                        3f, // particleSizeRange
                        0.5f, // particleDuration
                        0, // particleCount
                        new Color(255,255,255,0), // particleColor
                        new Color(255,100,100,0)  // explosionColor
                );
                spec.setMinEMPDamage(emp * 0.5f);
                spec.setMaxEMPDamage(emp);

                spec.setDamageType(DamageType.ENERGY);
                spec.setUseDetailedExplosion(false);
                spec.setSoundSetId("rift_lightning_explosion");
                spec.setSoundVolume(0.5f + 0.5f * mult);

                DamagingProjectileAPI explosion = engine.spawnDamagingExplosion(spec, ship, point);

                //explosion.addDamagedAlready(target);
                //color = new Color(255,75,75,255);

                //		float baseSize = 10f;
                //
                //		NEParams p = RiftCascadeMineExplosion.createStandardRiftParams(
                //				color, baseSize);
                //		//p.hitGlowSizeMult = 0.5f;
                //		p.noiseMult = 6f;
                //		p.thickness = 25f;
                //		p.fadeOut = 0.5f;
                //		p.spawnHitGlowAt = 1f;
                //		p.additiveBlend = true;
                //		p.blackColor = Color.white;
                //		p.underglow = null;
                //		p.withNegativeParticles = false;
                //		p.withHitGlow = false;
                //		p.fadeIn = 0f;
                //		//p.numRiftsToSpawn = 1;
                //
                //		RiftCascadeMineExplosion.spawnStandardRift(explosion, p);

                Color color = RiftLightningEffect.RIFT_LIGHTNING_COLOR;
                color = new Color(255,75,75,255);
                NegativeExplosionVisual.NEParams p = RiftCascadeMineExplosion.createStandardRiftParams(
                        color, 14f + 6f * mult);
                p.fadeOut = 0.5f + 0.5f * mult;
                p.hitGlowSizeMult = 0.6f;
                p.thickness = 50f;
                //p.thickness = 25f;


                //		p.hitGlowSizeMult = 0.5f;
                //		p.thickness = 25f;
                //		p.fadeOut = 0.25f;

                RiftCascadeMineExplosion.spawnStandardRift(explosion, p);
            }
        }));

    }

    public static enum MassDriverType {
        SHOCK, ARC, EXPLODE, SHROUDED;
    }

    protected static final float FLUX_DISCOUNT = 0.5f;

    @Override
    public void onFire(DamagingProjectileAPI projectile, WeaponAPI weapon, CombatEngineAPI engine) {
        CombatEntityAPI target = HSIAutoFireTargetPicker.PickAutoFireTarget(weapon);
        MassDriverType shot = MassDriverType.SHOCK;
        if (target == null) {
            target = AIUtils.getNearestEnemy(projectile);
            if(target==null){
                return;
            }
        } 
        if (target instanceof ShipAPI targetShip) {
            if (targetShip.getShield() != null && targetShip.getShield().isOn()&&targetShip.getShield().isWithinArc(weapon.getLocation())) {
                if(targetShip.getFluxLevel()>=0.9f){
                    shot = MassDriverType.EXPLODE;
                }else{
                    if(targetShip.getAIFlags()!=null){
                        if(targetShip.getAIFlags().hasFlag(ShipwideAIFlags.AIFlags.DO_NOT_USE_SHIELDS)){
                            shot = MassDriverType.EXPLODE;
                        }
                        if(targetShip.getAIFlags().hasFlag(ShipwideAIFlags.AIFlags.KEEP_SHIELDS_ON)){
                            shot = MassDriverType.SHOCK;
                        }
                        float angle = Misc.getAngleInDegrees(targetShip.getLocation(),weapon.getLocation());
                        if(targetShip.getAverageArmorInSlice(Misc.normalizeAngle(angle-30f),Misc.normalizeAngle(angle+30f))<=200){
                            shot = MassDriverType.SHOCK;
                        }
                    }
                }
            }
            /*else if (targetShip.isFighter() || targetShip.isDrone()) {
                shot = MassDriverType.ARC;
            } */
            else {
                shot = MassDriverType.EXPLODE;
            }
        } else if (target instanceof MissileAPI) {
            shot = MassDriverType.ARC;
        }
        ShipAPI source = weapon.getShip();
        boolean isFragShot = false;
        RoilingSwarmEffect sourceSwarm = RoilingSwarmEffect.getSwarmFor(projectile.getSource());
        if (sourceSwarm != null) {
            weapon.setPD(false);
            SwarmMember fragment = pickOuterFragmentWithinRange(150f,sourceSwarm);
            if (fragment != null) {
                shot = MassDriverType.SHOCK;
                sourceSwarm.removeMember(fragment);
                engine.spawnEmpArcVisual(fragment.loc,source,projectile.getLocation(),projectile,6f,new Color(255,55,255,125),new Color(255,255,255,225));
            }
        }

        DamagingProjectileAPI proj = projectile;

        if(source!=null&&projectile.getWeapon().getSize().equals(WeaponAPI.WeaponSize.LARGE)){
            if(source.getVariant().hasHullMod("shrouded_mantle")||source.getVariant().hasHullMod("shrouded_thunderhead")||source.getVariant().hasHullMod("shrouded_lens")) {
                shot = MassDriverType.SHROUDED;
                weapon.setPD(false);
            }
        }

        switch (shot) {
            case ARC -> {
                proj = (DamagingProjectileAPI) engine.spawnProjectile(weapon.getShip(), weapon,
                        weapon.getSpec().getWeaponId() + "_Arc",
                        projectile.getLocation(), projectile.getFacing(), null);
                if (source != null) {
                    source.getFluxTracker().decreaseFlux(weapon.getFluxCostToFire() * FLUX_DISCOUNT);
                }
                engine.removeEntity(projectile);
            }
            case SHOCK -> {
                proj = (DamagingProjectileAPI) engine.spawnProjectile(weapon.getShip(), weapon,
                        weapon.getSpec().getWeaponId() + "_Shock",
                        projectile.getLocation(), projectile.getFacing(), null);
                engine.removeEntity(projectile);
            }
            case SHROUDED -> {
                proj = (DamagingProjectileAPI) engine.spawnProjectile(weapon.getShip(), weapon,
                        weapon.getSpec().getWeaponId() + "_Shrouded",
                        projectile.getLocation(), projectile.getFacing(), null);
                engine.removeEntity(projectile);
            }
            default -> {
            }
        }
        if(isFragShot){
            proj.getDamage().getModifier().modifyMult("HWI_MassDrive_FragEnhance",1.33f);
        }




        if (source.getVariant().hasHullMod(HSIIds.HullMod.HWI_UPD)) {
            if (source.getCustomData().containsKey("MassProjectileLastShot" + weapon.getSlot().getId())) {
                MassDriverType lastShot = (MassDriverType) source.getCustomData()
                        .get("MassProjectileLastShot" + weapon.getSlot().getId());
                if (lastShot != shot) {
                    proj.getDamage().getModifier().modifyMult("HSI_WeaponIndUpgrade", 1.5f);
                }
            }
            source.setCustomData("MassProjectileLastShot" + weapon.getSlot().getId(), shot);
        }
    }

    public static final Color FRINGE_COLOR = new Color(0, 255, 0, 255);
    public static final Color CORE_COLOR = new Color(255, 255, 255, 255);
    // public static final float WIDTH = 12f;// 电弧宽度
    private static final DamageType TYPE = DamageType.ENERGY;// 电弧伤害类型

    @Override
    public void onExplosion(DamagingProjectileAPI explosion, DamagingProjectileAPI originalProjectile) {
        Vector2f loc = explosion.getLocation();
        float RANGE = switch (originalProjectile.getWeapon().getSize()) {
            case LARGE -> 40f;
            case MEDIUM -> 25f;
            case SMALL -> 12.5f;
        };
        Iterator<Object> c = Global.getCombatEngine().getAllObjectGrid().getCheckIterator(loc, RANGE * 8f,
                RANGE * 8f);
        while (c.hasNext()) {
            Object o = c.next();
            if (o instanceof CombatEntityAPI entity) {

                if (entity.getOwner() != explosion.getOwner()
                        && (((o instanceof MissileAPI) && (Misc.getDistance(entity.getLocation(), loc)) < RANGE * 1.1f
                                + entity.getCollisionRadius())
                                || (o instanceof ShipAPI)
                                        && (Misc.getDistance(entity.getLocation(), loc)) < 2.2f * RANGE
                                                + entity.getCollisionRadius())) {
                    if (entity instanceof DamagingProjectileAPI && !(entity instanceof MissileAPI))
                        continue;
                    if (entity instanceof AsteroidAPI)
                        continue;
                    Global.getCombatEngine().spawnEmpArcVisual(loc, explosion, entity.getLocation(), entity, RANGE / 2f,
                            FRINGE_COLOR, CORE_COLOR);
                }
            }
        }
    }


    protected SwarmMember pickOuterFragmentWithinRange(float range, RoilingSwarmEffect sourceSwarm) {
        SwarmMember best = null;
        float maxDist = -Float.MAX_VALUE;
        WeightedRandomPicker<SwarmMember> picker = sourceSwarm.getPicker(true, true);
        while (!picker.isEmpty()) {
            SwarmMember p = picker.pickAndRemove();
            float dist = Misc.getDistance(p.loc, sourceSwarm.getAttachedTo().getLocation());
            if (sourceSwarm.getParams().generateOffsetAroundAttachedEntityOval) {
                //dist -= sourceSwarm.attachedTo.getCollisionRadius() * 0.75f;
                dist -= Misc.getTargetingRadius(p.loc, sourceSwarm.getAttachedTo(), false) + sourceSwarm.getParams().maxOffset - range * 0.5f;
            }
            if (dist > maxDist && dist < range) {
                best = p;
                maxDist = dist;
            }
        }
        return best;
    }
}
