package data.hullmods;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier;
import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.TimeoutTracker;
import data.hullmods.ShieldMod.HSIShieldModEffect;
import data.kit.HSII18nUtil;
import data.scripts.HSIRenderer.HSICombatRendererV2;
import data.scripts.HSIRenderer.HSIShieldHitData;
import org.apache.log4j.Logger;
import org.dark.shaders.distortion.DistortionShader;
import org.dark.shaders.distortion.RippleDistortion;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class HSITurbulanceShieldListenerV2 implements DamageTakenModifier, AdvanceableListener {
    private ShipAPI ship;
    private HSITurbulanceShieldMutableStats stats;
    private HSITurbulanceShieldStats shield;
    private HSIShieldRenderData renderData;
    public static final String KEY = "HSITurbulanceShieldAbsorb";

    private static Logger LOG = Global.getLogger(HSITurbulanceShieldListenerV2.class);
    // private HSICombatRendererV2 renderer;
    // private HSICombatRenderer renderer;

    private boolean SHIELD_CONTROL = false;
    private final Color DMG_TEXT = new Color(15, 15, 245, 225);

    public enum ParamType {
        ARC, SHIP, PROJ, BEAM;
    }

    public static HSITurbulanceShieldListenerV2 getInstance(ShipAPI ship) {
        if (HSITurbulanceShieldListenerV2.hasShield(ship)) {
            return ship.getListeners(HSITurbulanceShieldListenerV2.class).get(0);
        } else {
            HSITurbulanceShieldListenerV2 l = new HSITurbulanceShieldListenerV2(ship);
            ship.addListener(l);
            return l;
        }
    }

    public static boolean hasShield(ShipAPI ship) {
        return ship.hasListenerOfClass(HSITurbulanceShieldListenerV2.class);
    }

    private final List<HSIShieldModEffect> shieldEffects;

    public HSITurbulanceShieldListenerV2(ShipAPI ship) {
        this.ship = ship;
        shieldEffects = getshieldModEffects(ship);
        init(ship);
    }

    private TimeoutTracker<BeamAPI> effected = new TimeoutTracker<>();

    public void init(ShipAPI ship) {
        stats = new HSITurbulanceShieldMutableStats();
        shield = new HSITurbulanceShieldStats();
        if (ship.getVariant().hasHullMod(HullMods.SAFETYOVERRIDES)) {
            stats.getShieldRecoveryRate().modifyMult(HullMods.SAFETYOVERRIDES, 0.5f);
        }
        HSICombatRendererV2.getInstance();
        saveArmor();
        renderData = new HSIShieldRenderData();
        for (HSIShieldModEffect e : shieldEffects) {
            e.applyShieldModificationsAfterShipCreation(this);
        }
        shield.update();
        //shield.setCurrent(shield.getShieldCap());
        // renderer = HSICombatRenderer.getInstance(ship);
        // renderer =

        SHIELD_CONTROL = ship.getVariant().hasHullMod("HSI_EliteForge")&&ship.getShield()!=null;
        shield.setEngageArmorProcess(true);
        getStats().getShieldArmorValue().modifyFlat("HSI_BASE",Math.min(getShield().getShieldCap()*HSIHaloV2.ARMOR_FRAC,HSIHaloV2.SHIELD_ARMOR_MAX));
        if(ship.getShield()!=null&&SHIELD_CONTROL){
            ship.getShield().setArc(0f);
            ship.getShield().setRadius(Math.max(1f,ship.getCollisionRadius()-ship.getHullSize().ordinal()*10f));
            ship.getShield().setSkipRendering(true);
            ship.getShield().setInnerColor(new Color(0,0,0,0));
            ship.getShield().setRingColor(new Color(0,0,0,0));
        }
    }

    private static boolean DO_LOG = false;

    private void log(String info){
        if(DO_LOG) LOG.info(info);
    }

    private TimeoutTracker<BeamAPI> beamController = new TimeoutTracker<>();

    public String modifyDamageTaken(Object param, CombatEntityAPI target,
            DamageAPI damage, Vector2f point, boolean shieldHit) {
        log("Start damage record.");
        log("Current Damage:"+damage.getDamage());
        damage.getModifier().unmodify(KEY);
        if ((shield.getCurrent() + shield.getExtra()) <= 0) {
            log("Shield not available.");
            log("Damage record end.");
            log("-------------------------------------------");
            return null;
        }
        if(SHIELD_CONTROL){
            if(ship.getShield() != null&& !ship.getShield().isOn()&&!(ship.getFluxTracker().isOverloadedOrVenting())){
                log("Shield not available.");
                log("Damage record end.");
                log("-------------------------------------------");
                return null;
            }
        }else{
            if(shieldHit){
                log("Hit on vanlia shield.");
                log("Damage record end.");
                log("-------------------------------------------");
                return null;
            }
        }
        /*if(param instanceof  DamagingProjectileAPI){
            DamagingProjectileAPI explosion = (DamagingProjectileAPI) param;
            if(explosion.getSpawnType().equals(ProjectileSpawnType.OTHER)&&explosion.getProjectileSpec()==null){
                damage.getModifier().modifyMult("HSI_CloseCombat",0.25f);
                log("Explosion type:OTHER.");
            }
        }*/
        if(param == null){
            damage.getModifier().modifyMult("HSI_CloseCombat",0.25f);
            log("Param:null.");
        }
        //if (ship.getFluxTracker().isOverloadedOrVenting()&&isShieldBlockWhileVenting()) return null;
        ParamType type = getType(param,damage);
        log("Damage type:"+type.name());
        float strength = getPierceStrength(damage);
        float damageTakenBase = preProcessDamage(type, damage, point,param);
        if(shield.engageArmorProcess){
            damageTakenBase = engageArmorProcess(damageTakenBase,point,(type.equals(ParamType.BEAM)),strength);
        }
        if (damageTakenBase <= 0.1) {
            damage.getModifier().modifyMult(KEY, 0.0001f);
            log("Damage processed to 0");
            log("Damage record end.");
            log("-------------------------------------------");
            return KEY;
        }
        boolean isSoft = damage.isSoftFlux();
        float damageTaken = processDamage(damageTakenBase, type, damage, point);
        if (damageTaken > 0) {
            {
                float t = damageTakenBase / 300f;
                if (t < 1.3f)
                    t = 1.3f;
                if (t > 3f)
                    t = 3f;
                // t += 0.5f;
                float radius = (float) Math.pow(damageTaken, 0.3333333) * 5f;
                if (radius < 10f)
                    radius = 10f;
                if (type == ParamType.BEAM) {
                    BeamAPI beam = (BeamAPI)param;
                    if(beam!=null&&!beamController.contains(beam)&&beam.getBrightness()>0){
                        t = 1f;
                        radius = Math.min(15f,(float) Math.pow(damageTaken, 0.5) * 5f);
                        beamController.add(beam,1f);
                    }
                }
                // renderer.requestHitData(new HSIShieldHitData(ship, point, Color.WHITE,
                // radius, t));
                // renderer.requestShieldRender(new HSIShieldHitData(ship, point, Color.WHITE,
                // radius, t), t);
                if(!getRenderData().getBlockRender()) {
                    renderData.getHitData().add(new HSIShieldHitData(ship, point, Color.WHITE, radius, t), t);
                }
                if (type == ParamType.PROJ) {
                    createHitRipple(point, ship.getVelocity(), damageTakenBase,
                            damage.getType(),
                            VectorUtils.getFacing(VectorUtils.getDirectionalVector(ship.getLocation(),
                                    point)),
                            ship.getCollisionRadius());
                }
            }
            boolean isHitExtra = shield.getExtra() >= damageTaken;
            float damageLeft = shield.takeDamage(damageTaken);
            if (damageTaken - damageLeft >= 0.01f) {
                Global.getCombatEngine().addFloatingDamageText(point, damageTaken - damageLeft, DMG_TEXT, ship, target);
            }
            if (!isHitExtra&&!isSoft
                    //&&shield.checkBlockRegen(type == ParamType.BEAM ? damageTakenBase * 5 : damageTakenBase)
            ) {
                shield.setShieldRegenBlocked(true);
            }
            float computed = damageLeft/damage.getDamage();
            if(shield.getExtra()+shield.getCurrent()<=shield.getShieldCap()*0.05f){
                computed = (damage.getDamage()-(damageTaken-damageLeft))/damage.getDamage();
            }
            damage.getModifier().modifyMult(KEY, Math.max(computed,0.0001f));
            if(type == ParamType.BEAM){
                if(effected.contains((BeamAPI) param)){
                    effected.set((BeamAPI)param,1.1f);
                }else{
                    effected.add((BeamAPI)param,1.1f);
                }
            }
            log("Damage record end.");
            log("-------------------------------------------");
            return KEY;
        }
        log("Damage record end.");
        log("-------------------------------------------");
        return null;
    }

    private float lastFrameHitpoint = 0;


    public void advance(float amount) {
        if(!effected.getItems().isEmpty()){
            for(BeamAPI b:effected.getItems()){
                if(effected.getRemaining(b)<1f){
                    b.getDamage().getModifier().unmodify(KEY);
                }
            }
        }
        effected.advance(1);
        beamController.advance(amount);
        if (ship.getFluxTracker().isOverloadedOrVenting()) {
            stats.getShieldRecoveryRate().modifyMult("HSIOverload", (1f - HSIHaloV2.VENTING_REG_LOSS));
        } else {
            stats.getShieldRecoveryRate().unmodify("HSIOverload");
        }
        if(SHIELD_CONTROL) {
            SHIELD_CONTROL = ship.getVariant().hasHullMod("HSI_EliteForge") && ship.getShield() != null;
        }
        if(ship.getCurrentCR()<=0.0001f){
            shield.setExtra(0);
            shield.setCurrent(0);
            stats.getShieldRegen().modifyMult("NO_CR",0f);
        }

        if (Global.getCombatEngine().isPaused())
            return;
        if (ship.getTravelDrive() != null && ship.getTravelDrive().isActive()) {

        } else {
            renderData.advance(amount);
        }
        renderData.setBlockRender(SHIELD_CONTROL && ship.getShield() != null && !ship.getShield().isOn());

        if (checkSurvival())
            return;
        if (shield.checkRegen(amount)) {
            shield.setShieldRegenBlocked(false);
            regen(amount);
        }
        regen(amount * shield.getShipStatsShieldRegenSync());
        for (HSIShieldModEffect effect : shieldEffects) {
            effect.processEveryFrameShieldEffects(ship, amount);
        }
        if (shield.getShieldLevel() >= 0.5) {
            if(ship.getFluxTracker().isOverloadedOrVenting()) {
                if(!isShieldBlockWhileVenting()){
                    immuneOverload();
                }
            }
        }
        boolean shouldRetain = shield.getShieldLevel() >= 0.05f && (!SHIELD_CONTROL || (ship.getShield() != null && (ship.getFluxTracker().isOverloadedOrVenting() || ship.getShield().isOn())));
        if(shouldRetain) {
            if (ship.getFluxTracker().isOverloadedOrVenting()) {
                if (!isShieldBlockWhileVenting()) {
                    retainArmor();
                    if (ship.getHitpoints() < lastFrameHitpoint) {
                        ship.setHitpoints(lastFrameHitpoint);
                    }
                    lastFrameHitpoint = ship.getHitpoints();
                }
            }else{
                retainArmor();
                if (ship.getHitpoints() < lastFrameHitpoint) {
                    ship.setHitpoints(lastFrameHitpoint);
                }
                lastFrameHitpoint = ship.getHitpoints();
            }
        }
        saveArmor();
        lastFrameHitpoint = ship.getHitpoints();

        lastFrameSystemActive = ship.getSystem()!=null&&ship.getSystem().isOn();
        lastFramePhaseCloackActive = ship.getPhaseCloak()!=null&&ship.getPhaseCloak().isOn();

        immuneEMPEffects(shield.getShieldLevel());

        if(shield.getCurrent()+shield.getExtra()>1000f+ship.getHullSize().ordinal()*1000f){
            if(ship.getShipAI()!=null){
                ship.getShipAI().getConfig().alwaysStrafeOffensively = true;
                ShipwideAIFlags flags = ship.getAIFlags();
                if (flags == null) return;
                if(ship.getVariant().hasHullMod(HullMods.SAFETYOVERRIDES)||ship.getVariant().hasHullMod("HSI_Stalker")) {
                    flags.setFlag(ShipwideAIFlags.AIFlags.DO_NOT_BACK_OFF, 0.5f);
                    flags.setFlag(ShipwideAIFlags.AIFlags.DO_NOT_AVOID_BORDER,1f);
                }
            }
        }else{
            if(ship.getShipAI()!=null){
                ship.getShipAI().getConfig().alwaysStrafeOffensively = false;
            }
        }
        if(SHIELD_CONTROL&&ship.getShield()!=null){
            if(shield.getShieldLevel()<=0){
                if(ship.getShield()!=null){
                    ship.getShield().toggleOff();
                }
            }
            if(ship.getShipAI()!=null){
                if(ship.getHullLevel()>0.5f){
                    if(shield.getShieldLevel()<=0.2f){
                        ship.getShield().toggleOff();
                    }
                }
            }
            getStats().getShieldEffciency().applyMods(ship.getMutableStats().getShieldAbsorptionMult());
            if(ship.getShield().isOn()){
                ship.getShield().forceFacing(Misc.normalizeAngle(ship.getFacing()+180f));
                if(ship == Global.getCombatEngine().getPlayerShip()){
                    Global.getCombatEngine().maintainStatusForPlayerShip("HSIShieldControl","graphics/icons/hullsys/fortress_shield.png", HSII18nUtil.getHullModString("HSIShieldControl"),
                            HSII18nUtil.getHullModString("HSIShieldControlOn"),false);
                }
            }
            //getStats().getShieldEffciency().applyMods(ship.getMutableStats().getShieldDamageTakenMult());
        }

        if(!(ship.getSystem()!=null&&ship.getSystem().isActive())&&ship.getAI()!=null) {
            if (ship.getFluxTracker().getFluxLevel() > 0.8f && ship.getHullLevel() > 0.8f && isShieldBlockWhileVenting() && shield.getCurrent() + shield.getExtra() > 4000f) {
                ship.giveCommand(ShipCommand.VENT_FLUX, null, 0);
            }
        }
    }

    protected ParamType getType(Object param,DamageAPI damage) {
        if (param instanceof DamagingProjectileAPI) {
            return ParamType.PROJ;
        }
        if (param instanceof BeamAPI) {
            return ParamType.BEAM;
        }
        if (param instanceof EmpArcEntityAPI) {
            return ParamType.ARC;
        }
        return ParamType.SHIP;
    }

    protected float getPierceStrength(DamageAPI damage) {
        float s = damage.getDamage();
        if(damage.getStats()!=null){
            s = damage.getStats().getHitStrengthBonus().computeEffective(damage.getDamage());
        }
        return s;
    }

    // for *base* amount
    protected float preProcessDamage(ParamType type, DamageAPI damage, Vector2f point,Object param) {
        ShipAPI source = null;
        log("Begin PreProcess.");
        float factor = HSIHaloV2.DEFAULT_DAMAGE_TAKEN;
        boolean isBeam = false;
        switch (type) {
            case ARC:
                factor *= 0.5f;
                break;
            case BEAM:
                //factor *= damage.getDpsDuration();
                source = ((BeamAPI)param).getSource();
                isBeam = true;
                break;
            case PROJ:
                factor *= 1;
                source = ((DamagingProjectileAPI)param).getSource();
                break;
            case SHIP:
                factor *= 0.8f;
                break;
            default:
                break;
        }
        if(damage.isDps()) isBeam = true;
        log("Is Beam:"+isBeam);
        float damageAmount = computeDamageToShield(damage,source,isBeam);
        log("Damage to Shield:"+damageAmount);
        for (HSIShieldModEffect e : shieldEffects) {
            damageAmount = e.processShieldEffectBeforeShieldProcess(damageAmount, getType(param,damage), damage, point,ship);
            log("Damage processed:"+damageAmount+" by "+e.getClass().getName());
        }
        factor *= getShield().getShipStatsDamageTakenSync();
        log("Factor:"+factor+" with statsDamageTakenSync"+getShield().getShipStatsDamageTakenSync());
        factor *= stats.getShieldEffciency().getModifiedValue();
        log("Factor:"+factor+" with statsShieldEffciency"+stats.getShieldEffciency().getModifiedValue());
        damageAmount = damageAmount * factor;
        log("Damage:"+damageAmount+" after preProcess.");
        log("End PreProcess.");
        return damageAmount;
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
                mult*=ship.getMutableStats().getKineticDamageTakenMult().getModifiedValue();
                break;
            case HIGH_EXPLOSIVE:
                mult*=ship.getMutableStats().getHighExplosiveDamageTakenMult().getModifiedValue();
                break;
            case FRAGMENTATION:
                mult*=ship.getMutableStats().getFragmentationDamageTakenMult().getModifiedValue();
                mult*=0.25f;
                break;
            case ENERGY:
                mult*=ship.getMutableStats().getEnergyDamageTakenMult().getModifiedValue();
                break;
            case OTHER:
                break;
        }
        if(isBeam){
            mult*=ship.getMutableStats().getBeamDamageTakenMult().getModifiedValue();
        }else{
            mult*=ship.getMutableStats().getProjectileDamageTakenMult().getModifiedValue();
        }
        return base*mult;
    }

    protected float processDamage(float base, ParamType param, DamageAPI damage, Vector2f point) {
        log("Begin process.");
        float damageAmount = base;
        for (HSIShieldModEffect e : shieldEffects) {
            log("Damage processed:"+damageAmount+" by "+e.getClass().getName());
            damageAmount = e.processShieldEffect(damageAmount, param, damage, point,ship);
        }
        log("End process.");
        return damageAmount;
    }

    protected List<HSIShieldModEffect> getshieldModEffects(ShipAPI ship) {
        List<HSIShieldModEffect> effects = new ArrayList<HSIShieldModEffect>();
        for (String hullmod : ship.getVariant().getHullMods()) {
            if (Global.getSettings().getHullModSpec(hullmod) != null
                    && Global.getSettings().getHullModSpec(hullmod).getEffect() instanceof HSIShieldModEffect) {
                effects.add((HSIShieldModEffect) Global.getSettings().getHullModSpec(hullmod).getEffect());
            }
        }
        return effects;
    }

    protected float engageArmorProcess(float damage, Vector2f point,boolean isBeam,float pierceStrength) {
        float d = damage;
        log("Begin Armor process.");
        log("Damage:"+d);
        float piercePower = (isBeam ? pierceStrength * 0.5f : pierceStrength);
        log("PierceStrength:"+piercePower);
        float armorValue = 0;
        armorValue = stats.getShieldArmorValue().computeEffective(armorValue);
        log("ArmorValue:"+armorValue);
        float aFactor =  piercePower / (piercePower + armorValue);
        aFactor = MathUtils.clamp(aFactor,1f-ship.getMutableStats().getMaxArmorDamageReduction().getModifiedValue(),1f);
        log("ArmorFactor:"+aFactor);
        d = d*aFactor;
        log("Damage after armor process:"+d);
        log("End Armor process.");
        return d;
    }

    protected boolean checkSurvival() {
        return !ship.isAlive();
    }

    protected void regen(float amount) {
        float toRegen = amount * shield.getBaseShieldRegen() ;
        shield.regenShield(toRegen);
    }

    private float[][] armorGridCopy = null;

    public void retainArmor() {
        if (armorGridCopy == null) {
            saveArmor();
        } else {
            ArmorGridAPI a = ship.getArmorGrid();
            
            if (a == null)
                return;
            float[][] cag = a.getGrid();
            if (cag.length == 0)
                return;
            for (int i = 0; i < cag.length; i++) {
                for (int j = 0; j < cag[i].length; j++) {
                    a.setArmorValue(i, j, Math.max(armorGridCopy[i][j], cag[i][j]));
                }
            }
        }
    }

    public void saveArmor() {
        ArmorGridAPI a = ship.getArmorGrid();
        if (a == null)
            return;
        float[][] cag = a.getGrid();
        if (cag.length == 0)
            return;
        armorGridCopy = new float[cag.length][cag[0].length];
        for (int i = 0; i < cag.length; i++) {
            for (int j = 0; j < cag[i].length; j++) {
                armorGridCopy[i][j] = cag[i][j];
            }
        }
    }

    private boolean lastFramePhaseCloackActive = false;
    private boolean lastFrameSystemActive = true;

    public void immuneOverload() {
        if (ship.getFluxTracker().isOverloaded() && ship.getShield() == null) {
            ship.getFluxTracker().stopOverload();
            if(ship.getPhaseCloak()!=null&&lastFramePhaseCloackActive){
                if(ship.getPhaseCloak().isCoolingDown()){
                    ship.getPhaseCloak().setCooldownRemaining(0f);
                }
                if(ship.getPhaseCloak().getMaxAmmo()>0&&ship.getPhaseCloak().getAmmo()<ship.getPhaseCloak().getMaxAmmo()){
                    ship.getPhaseCloak().setAmmo(ship.getPhaseCloak().getAmmo()+1);
                }
            }
            if(ship.getSystem()!=null&&lastFrameSystemActive){
                if(ship.getSystem().isCoolingDown()){
                    ship.getSystem().setCooldownRemaining(0f);
                }
                if(ship.getSystem().getMaxAmmo()>0&&ship.getSystem().getAmmo()<ship.getSystem().getMaxAmmo()){
                    ship.getSystem().setAmmo(ship.getSystem().getAmmo()+1);
                }
            }
        }
    }

    /*public void immuneEMPEffects(boolean immune) {
        if (immune) {
            ship.getMutableStats().getEmpDamageTakenMult().modifyMult(KEY, 0);
        } else {
            ship.getMutableStats().getEmpDamageTakenMult().unmodify(KEY);
        }
    }*/

    public void immuneEMPEffects(float level){
        float EMP_REDUCTION = 0f;
        if(level<0.25f){
            EMP_REDUCTION = (0.25f-level)*4f;
        }
        ship.getMutableStats().getEmpDamageTakenMult().modifyMult(KEY, EMP_REDUCTION);
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

    public boolean isShieldBlockWhileVenting(){
        return false;
        //return stats.getShieldBlockWhenVenting().getModifiedValue()>=1;
    }

    public static String getKey() {
        return KEY;
    }

    public HSITurbulanceShieldStats getShield() {
        return shield;
    }

    public ShipAPI getShip() {
        return ship;
    }

    public HSITurbulanceShieldMutableStats getStats() {
        return stats;
    }

    public HSIShieldRenderData getRenderData() {
        return renderData;
    }

    public class HSITurbulanceShieldMutableStats {
        private MutableStat shieldCap;
        private MutableStat shieldRegen;
        private MutableStat shieldRecoveryRate;
        private MutableStat shieldRegenCooldown;
        private MutableStat shieldRegenBlock;
        private MutableStat shieldEffciency;
        private MutableStat shieldRecoveryCost;

        private MutableStat shieldBufferCap;

        private MutableStat shieldBlockWhenVenting;

        private StatBonus shieldArmorValue;

        public HSITurbulanceShieldMutableStats() {
            HullSize hullsize = ship.getHullSize();
            shieldCap = new MutableStat(HSIHaloV2.SHIELDCAP.get(hullsize));
            shieldRegen = new MutableStat(HSIHaloV2.REGMAXSPEED.get(hullsize));
            shieldRecoveryRate = new MutableStat(1);
            shieldRegenCooldown = new MutableStat(HSIHaloV2.REGCD.get(hullsize));
            shieldRegenBlock = new MutableStat(HSIHaloV2.LOW_REG_STOP_LIMIT);
            shieldEffciency = new MutableStat(HSIHaloV2.DEFAULT_DAMAGE_TAKEN);
            shieldRecoveryCost = new MutableStat(HSIHaloV2.REGEN_FLUX_RATE);
            shieldBufferCap = new MutableStat(HSIHaloV2.BUFFER_CAP);
            shieldBlockWhenVenting = new MutableStat(2);
            shieldArmorValue = new StatBonus();
        }

        public MutableStat getShieldCap() {
            return shieldCap;
        }

        public MutableStat getShieldRecoveryRate() {
            return shieldRecoveryRate;
        }

        public MutableStat getShieldRegen() {
            return shieldRegen;
        }

        public MutableStat getShieldRegenCooldown() {
            return shieldRegenCooldown;
        }

        public MutableStat getShieldRegenBlock() {
            return shieldRegenBlock;
        }

        public MutableStat getShieldEffciency() {
            return shieldEffciency;
        }

        public MutableStat getShieldRecoveryCost() {
            return shieldRecoveryCost;
        }

        public MutableStat getShieldBufferCap() {
            return shieldBufferCap;
        }

        public MutableStat getShieldBlockWhenVenting() {
            return shieldBlockWhenVenting;
        }

        public StatBonus getShieldArmorValue() {
            return shieldArmorValue;
        }
    }

    public class HSITurbulanceShieldStats {
        private float current;
        private float extra;
        private FaderUtil regenCooldownTimer;
        private boolean engageArmorProcess = false;

        private float buffer = 0;

        public HSITurbulanceShieldStats() {
            update();
        }

        public void update(){
            current = getShieldCap();
            extra = 0;
            regenCooldownTimer = new FaderUtil(0, stats.getShieldRegenCooldown().getModifiedValue());
        }

        public float getCurrent() {
            return current;
        }

        public float getExtra() {
            return extra;
        }

        public float getShieldCap() {
            return stats.getShieldCap().getModifiedValue() * ship.getMaxFlux();
        }

        public float getBufferCap(){
            return stats.getShieldBufferCap().getModifiedValue()*getShieldCap();
        }

        public float getBaseShieldRegen() {
            return stats.getShieldRegen().getModifiedValue()
                    * ship.getMutableStats().getFluxDissipation().getModifiedValue();
        }

        public float getExtraShieldCap() {

            return getShieldCap() * 0.5f;
        }

        public float getShieldLevel() {
            return getCurrent() / (Math.max(1f,getShieldCap()));
        }

        // return leftamount
        public float takeDamage(float amount) {
            float damage = amount;
            if (extra >= damage) {
                extra -= damage;
                log("Extra:"+damage);
                damage = 0;
            } else {
                damage -= extra;
                log("Extra:"+extra);
                extra = 0;
            }
            if (damage > 0) {
                if (current >= damage) {
                    current -= damage;
                    log("Main:"+damage);
                    damage = 0;
                } else {
                    damage -= current;
                    log("Main:"+current);
                    current = 0;
                }
            }
            return damage;
        }

        public void regenShield(float amount) {
            float acutalRegen = amount*stats.getShieldRecoveryRate().getModifiedValue();
            current += acutalRegen;
            if (current > getShieldCap()) {
                acutalRegen -= (current - getShieldCap());
                current = getShieldCap();
            }
            buffer-=acutalRegen*stats.getShieldBufferCap().getModifiedValue();
            if (acutalRegen >= 0.1f)
                generateFlux(acutalRegen);
        }

        protected void generateFlux(float amount) {
            ship.getFluxTracker().increaseFlux(amount * stats.getShieldRecoveryCost().getModifiedValue(), false);
        }

        public void addExtraShield(float amount) {
            extra += amount;
            if (extra >= getExtraShieldCap()) {
                extra = getExtraShieldCap();
            }
        }

        public float getShieldRegenBlock() {
            return 0;
            //return Math.min(stats.getShieldRegenBlock().getModifiedValue() * getShieldCap(),HSIHaloV2.SHIELD_BLOCK_MAX);
        }

        public void setShieldRegenBlocked(boolean shouldBlock) {
            if (shouldBlock) {
                regenCooldownTimer.setBrightness(1f);
                regenCooldownTimer.fadeOut();
            } else {
                regenCooldownTimer.setBrightness(0f);
                regenCooldownTimer.forceOut();
            }
        }

        public boolean isShieldRegenBlocked() {
            return regenCooldownTimer.isFadingOut();
        }

        public FaderUtil getRegenCooldownTimer() {
            return regenCooldownTimer;
        }

        public float getShieldRegenTime() {
            return regenCooldownTimer.getDurationOut();
        }

        public void setShieldRegenTime(float newTime) {
            regenCooldownTimer.setDuration(newTime, newTime);
        }

        public boolean checkBlockRegen(float damage) {
            if(damage <= getShieldRegenBlock()){
                if(buffer<getBufferCap()){
                    buffer+=damage;
                }
                if(buffer>getBufferCap()) buffer = getBufferCap();
                return buffer>=getBufferCap();
            }else{
                return true;
            }
        }

        public boolean checkRegen(float amount) {
            if (regenCooldownTimer.isFadingOut()) {
                regenCooldownTimer.advance(amount);
                if (!ship.areSignificantEnemiesInRange()) {
                    regenCooldownTimer.advance(0.5f * amount);
                }
            }
            return regenCooldownTimer.isFadedOut();
        }

        public boolean isEngageArmorProcess() {
            return engageArmorProcess;
        }

        public void setEngageArmorProcess(boolean engageArmorProcess) {
            this.engageArmorProcess = engageArmorProcess;
        }

        public float getShipStatsDamageTakenSync() {
            float factor = ship.getMutableStats().getHullDamageTakenMult().getModifiedValue()+ship.getMutableStats().getArmorDamageTakenMult().getModifiedValue()+ship.getMutableStats().getShieldDamageTakenMult().getModifiedValue();
            factor = factor/3f;

            if(SHIELD_CONTROL){
                factor = ship.getMutableStats().getShieldDamageTakenMult().getModifiedValue();
            }
            //factor = 1f-factor;
            return factor;
        }

        public float getShipStatsShieldRegenSync() {
            //Global.getLogger(this.getClass()).info("HardDis :" + factor);
            return Math.max(0f,
                    ship.getMutableStats().getHardFluxDissipationFraction().getModifiedValue()*0.4f);
        }

        public void setCurrent(float current) {
            this.current = current;
        }

        public void setExtra(float extra) {
            this.extra = extra;
        }

    }

    public class HSIShieldRenderData {
        private TimeoutTracker<HSIShieldHitData> hitData = new TimeoutTracker<HSIShieldHitData>();

        private boolean reverseSpread = false;

        private IntervalUtil reverseTimer;

        private float elapsed = 0f;

        private boolean blockRender = false;

        public HSIShieldRenderData() {

        }

        public void advance(float amount) {
            if(!reverseSpread) {
                elapsed+=amount;
            }else{
                elapsed-=24.5f*amount;
                if(reverseTimer!=null){
                    reverseTimer.advance(amount);
                    if(reverseTimer.intervalElapsed()){
                        reverseTimer = null;
                        reverseSpread = false;
                    }
                }
            }
            if(elapsed>=14f){
                elapsed-=14f;
            }
            if(elapsed<0f){
                elapsed+=14f;
            }
            hitData.advance(amount);
        }

        public float getSpreadLevel() {
            return elapsed/14f;
        }

        public TimeoutTracker<HSIShieldHitData> getHitData() {
            return hitData;
        }

        public void setReverseSpread(boolean reverseSpread) {
            this.reverseSpread = reverseSpread;
        }

        public void setReverseSpreadForTime(boolean reverseSpread,float time){
            this.reverseSpread = reverseSpread;
            reverseTimer = new IntervalUtil(time,time);
        }

        public float getElapsed() {
            return elapsed;
        }

        public IntervalUtil getReverseTimer() {
            return reverseTimer;
        }

        public void setBlockRender(boolean blockRender) {
            this.blockRender = blockRender;
        }

        public boolean getBlockRender(){
            return this.blockRender;
        }
    }
}
