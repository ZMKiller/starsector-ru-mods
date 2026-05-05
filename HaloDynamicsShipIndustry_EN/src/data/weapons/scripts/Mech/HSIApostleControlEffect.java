package data.weapons.scripts.Mech;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;
import data.hullmods.HSITurbulanceShieldListenerV2;
import data.kit.HSII18nUtil;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.magiclib.util.MagicAnim;

import java.awt.*;

public class HSIApostleControlEffect implements EveryFrameWeaponEffectPlugin {

    private boolean init = false;
    private WeaponAPI LShoulder =null,LWeaponDefense=null,RShoulder=null, LWeapon=null, RWeapon=null, Body=null, Leg=null, Head=null, WS0=null, WS1=null;
    private ShipAPI ship =null;

    public void init(WeaponAPI weapon) {
        ship = weapon.getShip();
        if (ship != null) {
            ship.setCollisionClass(CollisionClass.FIGHTER);
            Global.getCombatEngine().addPlugin(new HSIApostleExtraControl(ship));
            Body = weapon;
            for (WeaponAPI w : ship.getAllWeapons()) {
                switch (w.getSlot().getId()) {
                    case "LS":
                        LShoulder = w;
                        LShoulderBase = w.getSprite().getCenterY();
                        FirepointBase = w.getSpec().getTurretFireOffsets().get(0).getX();
                        break;
                    case "LAD":
                        LWeaponDefense = w;
                        LWeaponDefenseBase = w.getSprite().getCenterY();
                    case "RS":
                        RShoulder = w;
                        break;
                    case "LA":
                        LWeapon = w;
                        LWeaponBase = w.getSprite().getCenterY();
                        break;
                    case "RA":
                        RWeapon = w;
                        break;
                    case "LEG":
                        Leg = w;
                        break;
                    case "HEAD":
                        Head = w;
                        break;
                    case "WS0001":
                        WS0 = w;
                        break;
                    case "WS0002":
                        WS1 = w;
                        break;
                }
            }
        }
    }


    private final IntervalUtil LegTimer = new IntervalUtil(0.04f, 0.04f);
    private int frame = 7;

    private final float HEAD_TURN_RATE = 200f;
    private boolean reachedStartPoint = false;
    private boolean isSwing = false;
    private boolean cooldown = false;
    private float swingLevel = 0f;
    private IntervalUtil animInterval = new IntervalUtil(0.012F, 0.012F);
    private float LWeaponBase = 0f;
    private float LShoulderBase = 0f,LWeaponDefenseBase = 0f,FirepointBase = 0f;

    private float defenseLevel = 0f;

    public static final Color JITTER_UNDER_COLOR = new Color(25, 171, 224, 55);

    @Override
    public void advance(float amount, CombatEngineAPI engine, WeaponAPI weapon) {
        if (!init) {
            init(weapon);
            init = true;
        }
        float shipFacing = ship.getFacing();
        //
        if (WS0 != null) {
            WS0.getSprite().setColor(new Color(0, 0, 0, 0));
            if (WS0.getBarrelSpriteAPI() != null) {
                WS0.getBarrelSpriteAPI().setColor(new Color(0, 0, 0, 0));
            }
        }

        if (WS1 != null) {
            WS1.getSprite().setColor(new Color(0, 0, 0, 0));
            if (WS1.getBarrelSpriteAPI() != null) {
                WS1.getBarrelSpriteAPI().setColor(new Color(0, 0, 0, 0));
            }
        }


        //body control
        Body.setCurrAngle(Misc.normalizeAngle(LWeapon.getCurrAngle() / 2f + RWeapon.getCurrAngle() / 2f));

        //leg control
        LegTimer.advance(amount);
        if (ship.getEngineController().isAcceleratingBackwards()) {
            if (LegTimer.intervalElapsed())
                if (frame != 1)
                    frame--;

            if (frame < 1)
                frame = 1;
        } else if (ship.getEngineController().isAccelerating()) {
            if (LegTimer.intervalElapsed())
                if (frame != 15)
                    frame++;

            if (frame > 15)
                frame = 15;
        } else {
            if (LegTimer.intervalElapsed()) {

                if (frame > 7)
                    frame--;

                else if (frame != 7)
                    frame++;
            }
        }
        Leg.getAnimation().setFrame(frame);

        //head control
        float expectedHeadAngle = ship.getFacing();
        if (ship.getSelectedGroupAPI() != null) {
            if (ship.getSelectedGroupAPI().getWeaponsCopy().contains(LWeapon)) {
                expectedHeadAngle = LWeapon.getCurrAngle();
            }
            if (ship.getSelectedGroupAPI().getWeaponsCopy().contains(RWeapon)) {
                expectedHeadAngle = RWeapon.getCurrAngle();
            }
        }
        if(ship.getAI()==null){
            expectedHeadAngle = VectorUtils.getAngle(ship.getLocation(),ship.getMouseTarget());
        }
        float angleChange = MathUtils.getShortestRotation(Head.getCurrAngle(), expectedHeadAngle);
        if (Math.abs(angleChange) > HEAD_TURN_RATE * amount) {
            angleChange = Math.signum(angleChange) * HEAD_TURN_RATE * amount;
        }
        Head.setCurrAngle(Head.getCurrAngle() + angleChange);

        //arm control
        float angleFaction = 0f;
        float diffY = 0f;
        float AngleDiff = 0f;
        boolean isBladeDefense = false;
        if(ship.getCustomData().containsKey(HSIApostleExtraControl.KEY_BLADE_DEFENSE)){
            isBladeDefense = (boolean)ship.getCustomData().get(HSIApostleExtraControl.KEY_BLADE_DEFENSE);
        }
        if (RWeapon != null) {
            float FacingDiff = MathUtils.getShortestRotation(shipFacing, RWeapon.getCurrAngle());
            if (!engine.isPaused()) {
                if (LWeapon != null) {

                    if(isBladeDefense) {
                        if (LWeapon.getChargeLevel() <= 0) {
                            float setAngle = LWeapon.getCurrAngle() - 90f * amount;
                            if (Math.abs(MathUtils.getShortestRotation(setAngle, shipFacing - 100f)) < 300f * amount) {
                                setAngle = shipFacing - 35f;
                                reachedStartPoint = true;
                            }
                            LWeapon.setCurrAngle(setAngle);
                            if(LWeaponDefense!=null){
                                LWeaponDefense.setForceFireOneFrame(true);
                            }
                            float defenseDiff = Math.abs(MathUtils.getShortestRotation(setAngle,shipFacing-35f));
                            if(defenseDiff<=1f){
                                defenseLevel = 1;
                            }else{
                                defenseLevel = MathUtils.clamp (1f-defenseDiff/90f,0f,1f);
                            }
                            angleFaction = MagicAnim.smoothNormalizeRange(defenseLevel, 0f, 0.9f);
                            diffY = MagicAnim.smoothNormalizeRange(defenseLevel, 0f, 1f);
                        }else{
                            ship.setCustomData(HSIApostleExtraControl.KEY_BLADE_DEFENSE,false);
                            defenseLevel = 0f;
                            isBladeDefense = false;
                        }
                        LWeapon.getSprite().setCenterY(LWeaponBase - 6f * diffY);
                        LWeapon.ensureClonedSpec();
                        //LWeapon.getSpec().getTurretFireOffsets().get(0).setX(FirepointBase - 6f * diffY);
                        if(LWeaponDefense!=null){
                            LWeaponDefense.ensureClonedSpec();
                            LWeaponDefense.getSprite().setCenterY(LWeaponBase - 6f * diffY);
                            //LWeaponDefense.getSpec().getTurretFireOffsets().get(0).setX(FirepointBase - 6f * diffY);
                        }
                    }else {
                        if (LWeapon.getChargeLevel() < 1f) {
                            angleFaction = MagicAnim.smoothNormalizeRange(LWeapon.getChargeLevel(), 0f, 0.9f);
                            diffY = MagicAnim.smoothNormalizeRange(LWeapon.getChargeLevel(), 0f, 1f);
                        } else {
                            angleFaction = 1f;
                            diffY = 1f;
                        }
                        LWeapon.getSprite().setCenterY(LWeaponBase - 12f * diffY);
                        LWeapon.ensureClonedSpec();
                        //LWeapon.getSpec().getTurretFireOffsets().get(0).setX(FirepointBase - 12f * diffY);
                        if(LWeaponDefense!=null){
                            LWeaponDefense.getSprite().setCenterY(LWeaponBase - 12f * diffY);
                            LWeaponDefense.ensureClonedSpec();
                            //LWeaponDefense.getSpec().getTurretFireOffsets().get(0).setX(FirepointBase - 12f * diffY);
                        }
                        if (LWeapon.getCooldownRemaining() <= 0f && !LWeapon.isFiring()) {
                            cooldown = false;
                        }
                        if (!reachedStartPoint && LWeapon.isFiring()) {
                            float setAngle = LWeapon.getCurrAngle() - 90f * amount;
                            if (Math.abs(MathUtils.getShortestRotation(setAngle, shipFacing - 100f)) < 180f * amount) {
                                setAngle = shipFacing - 35f;
                                reachedStartPoint = true;
                            }
                            LWeapon.setCurrAngle(setAngle);
                        }

                        if (reachedStartPoint && !isSwing && !cooldown && LWeapon.getChargeLevel() > 0f) {
                            LWeapon.setCurrAngle(LWeapon.getCurrAngle() + angleFaction * 120f * (float) Math.sin(Math.PI * LWeapon.getChargeLevel()) * LWeapon.getChargeLevel());
                        }

                        if (LWeapon.getChargeLevel() >= 1f) {
                            isSwing = true;
                        }

                        if (isSwing && LWeapon.getCurrAngle() != LWeapon.getShip().getFacing() + 60f) {
                            animInterval.advance(amount);
                            LWeapon.setCurrAngle(Math.min(LWeapon.getCurrAngle() + swingLevel, LWeapon.getCurrAngle() + LWeapon.getArc() / 2f));
                        }

                        if (isSwing && LWeapon.getChargeLevel() <= 0f) {
                            isSwing = false;
                            swingLevel = 0f;
                            cooldown = true;
                            reachedStartPoint = false;
                        }

                        if (animInterval.intervalElapsed()) {
                            swingLevel = Math.min((swingLevel + 0.2f + 0.5f * (float) Math.sin(Math.PI * LWeapon.getChargeLevel())), 9f);
                        }

                        if (!isSwing) {
                            swingLevel = 0f;
                        }
                    }
                    if(LWeaponDefense!=null){
                        LWeaponDefense.setCurrAngle(LWeapon.getCurrAngle());
                    }
                }

                if (LShoulder != null) {
                    LShoulder.setCurrAngle(weapon.getCurrAngle() - 5f * angleFaction - AngleDiff * 22.5f + MathUtils.getShortestRotation(weapon.getCurrAngle(), LWeapon.getCurrAngle()) * 0.7f);
                    LShoulder.getSprite().setCenterY(LShoulderBase - 6f * diffY);
                }

                if (RShoulder != null) {
                    RShoulder.setCurrAngle(shipFacing + angleFaction * 0.5f * (-45f + 45f * (swingLevel / 9f)) * 0.5f - AngleDiff * 45f + FacingDiff * 0.75f - 12.5f);
                }



                if(isBladeDefense){
                    ship.getMutableStats().getHullDamageTakenMult().modifyMult(HSIApostleExtraControl.KEY_BLADE_DEFENSE,(1f-0.8f*defenseLevel));
                    ship.getMutableStats().getArmorDamageTakenMult().modifyMult(HSIApostleExtraControl.KEY_BLADE_DEFENSE,(1f-0.8f*defenseLevel));
                    if(HSITurbulanceShieldListenerV2.hasShield(ship)){
                        HSITurbulanceShieldListenerV2 shield = HSITurbulanceShieldListenerV2.getInstance(ship);
                        shield.getStats().getShieldEffciency().modifyMult(HSIApostleExtraControl.KEY_BLADE_DEFENSE,(1f-0.8f*defenseLevel));
                    }
                    ship.getFluxTracker().increaseFlux(ship.getHullSpec().getFluxCapacity()*0.05f*amount,true);
                    addStatus(defenseLevel);
                    float jitterLevel = (float) (Math.pow(defenseLevel, 2));
                    ship.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel, 15, 0f, 4f + jitterLevel * 3f);
                }else{
                    ship.getMutableStats().getHullDamageTakenMult().unmodify(HSIApostleExtraControl.KEY_BLADE_DEFENSE);
                    ship.getMutableStats().getArmorDamageTakenMult().unmodify(HSIApostleExtraControl.KEY_BLADE_DEFENSE);
                    if(HSITurbulanceShieldListenerV2.hasShield(ship)){
                        HSITurbulanceShieldListenerV2 shield = HSITurbulanceShieldListenerV2.getInstance(ship);
                        shield.getStats().getShieldEffciency().unmodify(HSIApostleExtraControl.KEY_BLADE_DEFENSE);
                    }
                }
            }
        }
    }

    protected void addStatus(float level) {
        String content = HSII18nUtil.getHullModString("HSIApostleBladeDefense")+String.format("%.1f",level*100f*0.8)+"%";
        Global.getCombatEngine().maintainStatusForPlayerShip("HSIApostleBladeDefense",
                "graphics/icons/hullsys/HSI_BladeDefense.png", HSII18nUtil.getHullModString("HSIApostleBladeDefenseTitle"), content,
                false);
    }
}
