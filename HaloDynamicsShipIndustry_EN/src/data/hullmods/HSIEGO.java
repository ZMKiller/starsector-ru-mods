package data.hullmods;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.loading.WeaponSlotAPI;
import com.fs.starfarer.api.util.TimeoutTracker;
import data.kit.AjimusUtils;
import data.scripts.HSIRenderer.HSIButterflyRenderObject;
import data.scripts.HSIRenderer.HSIButterflyRenderObjectV2;
import data.scripts.HSIRenderer.HSICombatRendererV2;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BuffManagerAPI.Buff;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.CollisionClass;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.MutableStat;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.WeaponAPI.WeaponType;
import com.fs.starfarer.api.combat.listeners.AdvanceableListener;
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier;
import com.fs.starfarer.api.combat.listeners.HullDamageAboutToBeTakenListener;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.ui.Alignment;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.FaderUtil;
import com.fs.starfarer.api.util.IntervalUtil;
import com.fs.starfarer.api.util.Misc;

import data.kit.HSII18nUtil;

public class HSIEGO extends BaseHullMod {
    public static Map<HullSize, Integer> POSITIVE = new HashMap<HullSize, Integer>();

    static {
        POSITIVE.put(HullSize.FIGHTER, 500);
        POSITIVE.put(HullSize.FRIGATE, 1250);
        POSITIVE.put(HullSize.DESTROYER, 1750);
        POSITIVE.put(HullSize.CRUISER, 2250);
        POSITIVE.put(HullSize.CAPITAL_SHIP, 3500);
    }

    public static Map<HullSize, Integer> NEGATIVE = new HashMap<HullSize, Integer>();

    static {
        NEGATIVE.put(HullSize.FIGHTER, 200);
        NEGATIVE.put(HullSize.FRIGATE, 750);
        NEGATIVE.put(HullSize.DESTROYER, 1000);
        NEGATIVE.put(HullSize.CRUISER, 1250);
        NEGATIVE.put(HullSize.CAPITAL_SHIP, 1500);
    }

    public static Map<HullSize, Float> KILL_CR = new HashMap<>();

    static {
        KILL_CR.put(HullSize.FIGHTER, 2f);
        KILL_CR.put(HullSize.FRIGATE, 6f);
        KILL_CR.put(HullSize.DESTROYER, 9f);
        KILL_CR.put(HullSize.CRUISER, 18f);
        KILL_CR.put(HullSize.CAPITAL_SHIP, 25f);
    }

    public static Map<HullSize, Float> KILL_PEAK = new HashMap<>();

    static {
        KILL_PEAK.put(HullSize.FIGHTER, 3f);
        KILL_PEAK.put(HullSize.FRIGATE, 8f);
        KILL_PEAK.put(HullSize.DESTROYER, 12f);
        KILL_PEAK.put(HullSize.CRUISER, 20f);
        KILL_PEAK.put(HullSize.CAPITAL_SHIP, 30f);
    }

    public static final String HSI_EGO_BUTTERFLY = "HSI_T_01_68";

    protected static final float CR_REDUCE_BONUS = 0.66f;

    protected static final float EXTRA_CR_LIMIT = 0.3f;

    public static final int MAX_LEVEL = 6;

    public static class HSIEGOStats implements DamageDealtModifier, AdvanceableListener, HullDamageAboutToBeTakenListener {
        private ShipAPI ship;
        private HSIEGOBuff buff;
        private float DamageDealt = 0f;
        private float DamageDealtLegacy = 0f;
        private float DamageTaken = 0f;
        private HSIEGOCardsModifier cards = new HSIEGOCardsModifier();
        private IntervalUtil reviveDelay = new IntervalUtil(0.5f, 0.5f);

        private TimeoutTracker<ShipAPI> preys = new TimeoutTracker<>();
        // private float lastFrameHitpoints = 0f;

        public HSIEGOStats(ShipAPI ship, HSIEGOBuff buff) {
            this.ship = ship;
            this.buff = buff;
            int P = buff.getPositiveCount();
            int N = buff.getNegativeCount();
            // 初始化船插提供的生涯情绪点
            putEmotions(P, N);
        }

        // 获取造成伤害

        public String modifyDamageDealt(Object param, CombatEntityAPI target,
                                        DamageAPI damage, Vector2f point, boolean shieldHit) {
            if (target instanceof ShipAPI) {
                ShipAPI t = (ShipAPI) target;
                if (!shieldHit) {
                    if (t.isHulk())
                        return null;
                    int[] cord = t.getArmorGrid().getCellAtLocation(point);
                    float aromr = t.getArmorGrid().getArmorRating() * 0.05f;
                    if (cord != null && cord.length == 2) {
                        aromr = t.getArmorGrid().getArmorValue(cord[0], cord[1]) * 15f;
                    }
                    float damageNum = damage.getDamage();
                    if (damage.isDps())
                        damageNum *= 0.1f;
                    float add = damageNum * damageNum / (damageNum + aromr);
                    addDamageDealt(add);
                    if (t.getHitpoints() < add) {
                        addDamageDealt(t.getMaxHitpoints() * 0.1f);
                        if (ship.getSystem() != null && ship.getSystem().getCooldownRemaining() > 0.1f) {
                            ship.getSystem().setCooldownRemaining(0.1f);
                        }
                    }
                }
                if (t.isHulk() || !t.isAlive() || t.getOwner() == ship.getOwner()) return null;
                if (!preys.contains(t)) {
                    preys.add(t, 5f);
                } else {
                    preys.set(t, 5f);
                }
            }
            return null;
        }

        /*
         * public void reportDamageApplied(Object source, CombatEntityAPI target,
         * ApplyDamageResultAPI result) {
         * addDamageDealt(result.getDamageToHull() + result.getTotalDamageToArmor());
         * if (result.getDamageToHull() > target.getHitpoints()) {
         * addDamageDealt(target.getMaxHitpoints() * 0.1f);
         * }
         * }
         */

        private boolean revived = false;
        private boolean isReviving = false;
        private FaderUtil reviveFader = new FaderUtil(1f, 8f, 6f);
        private boolean reviveDelayed = false;
        public static final String EGO_REVIVE = "HSIEGORevive";

        public boolean notifyAboutToTakeHullDamage(Object param, ShipAPI ship, Vector2f point, float damageAmount) {
            addDamageTaken(damageAmount);
            if (damageAmount >= ship.getHitpoints() && !revived) {
                isReviving = true;
                ship.setHitpoints(100);
                reviveFader.fadeOut();
                //reviveFader.setBounceUp(true);
                ship.getMutableStats().getHullDamageTakenMult().modifyMult(EGO_REVIVE, 0f);
                ship.setMaxHitpoints(ship.getMaxHitpoints() * 1.25f);
                ship.getMutableStats().getArmorBonus().modifyMult(EGO_REVIVE, 1.25f);
                return true;
            }
            return false;
        }

        // 控制复活流程
        public void advance(float amount) {
            /*
             * if (lastFrameHitpoints > ship.getHitpoints()) {
             * addDamageTaken(lastFrameHitpoints - ship.getHitpoints());
             * }
             * lastFrameHitpoints = ship.getHitpoints();
             */
            if (Global.getCombatEngine().isPaused())
                return;
            preys.advance(amount);
            if (!preys.getItems().isEmpty()) {
                List<ShipAPI> toRemove = new ArrayList<>();
                for (ShipAPI h : preys.getItems()) {
                    if (!h.isAlive()) {
                        toRemove.add(h);
                        preyGet(h);
                    }
                }
                for (ShipAPI killed : toRemove) {
                    preys.remove(killed);
                }
            }

            if (isReviving && !revived) {
                revive(amount);
            }
            if (revived && !reviveDelayed) {
                reviveDelay.advance(amount);
                if (reviveDelay.intervalElapsed()) {
                    ship.getMutableStats().getHullDamageTakenMult().unmodify(EGO_REVIVE);
                    reviveDelayed = true;
                }
            }
            /*MutableStat time = ship.getMutableStats().getTimeMult();
            if (time.getModifiedValue() > time.getBaseValue() && ship.getPeakTimeRemaining() > 0
                    && ship.areSignificantEnemiesInRange()) {
                float delta = (time.getModifiedValue() - time.getBaseValue()) / time.getModifiedValue();
                if (delta > 0) {
                    float curr = 0;
                    if (ship.getMutableStats().getPeakCRDuration().getFlatBonus("HSIEGO") != null) {
                        curr = ship.getMutableStats().getPeakCRDuration().getFlatBonus("HSIEGO").getValue();
                    }
                    //ship.getMutableStats().getPeakCRDuration().modifyFlat("HSIEGO", curr + delta * amount);
                }
            }*/
            if (ship == Global.getCombatEngine().getPlayerShip())
                updateStatusForEGO();
        }

        private void preyGet(ShipAPI prey) {
            float maxCR = ship.getCRAtDeployment();
            float reward = KILL_CR.get(prey.getHullSize()) / 100f;
            boolean hasPleasure = ship.getCustomData().containsKey("HSI_EGO_Pleasure");
            if (hasPleasure) reward *= 1.5f;
            if (ship.getCurrentCR() >= maxCR) {
                reward /= 2;
                ship.setCurrentCR(Math.min(maxCR + EXTRA_CR_LIMIT, ship.getCurrentCR() + reward));
            } else {
                ship.setCurrentCR(ship.getCurrentCR() + reward);
            }
        }

        private void updateStatusForEGO() {
            Global.getCombatEngine().maintainStatusForPlayerShip(EGO_REVIVE,
                    "graphics/icons/hullsys/temporal_shell.png",
                    HSII18nUtil.getHullModString("HSIEGOmaintain0"), "" + cards.getCurrCards(), isReviving);
            Global.getCombatEngine().maintainStatusForPlayerShip(EGO_REVIVE + 1,
                    "graphics/icons/hullsys/temporal_shell.png",
                    HSII18nUtil.getHullModString("HSIEGOmaintain0"), "" + DamageDealt + "/" + DamageTaken, isReviving);
            // Global.getLogger(this.getClass()).info("DMG dealt:
            // "+DamageDealt+"/"+DamageDealtLegacy+"DMG taken: "+DamageTaken);
        }

        private void revive(float amount) {
            reviveFader.advance(amount);
            ship.setCollisionClass(CollisionClass.NONE);
            ship.setAlphaMult(Math.max(0, reviveFader.getBrightness()));
            ship.setHitpoints(
                    Math.min(ship.getMaxHitpoints(), ship.getHitpoints() + ship.getMaxHitpoints() * 0.2f * amount));
            ship.blockCommandForOneFrame(ShipCommand.FIRE);
            ship.blockCommandForOneFrame(ShipCommand.ACCELERATE);
            ship.blockCommandForOneFrame(ShipCommand.ACCELERATE_BACKWARDS);
            ship.blockCommandForOneFrame(ShipCommand.DECELERATE);
            ship.blockCommandForOneFrame(ShipCommand.USE_SYSTEM);
            ship.blockCommandForOneFrame(ShipCommand.TURN_LEFT);
            ship.blockCommandForOneFrame(ShipCommand.TOGGLE_SHIELD_OR_PHASE_CLOAK);
            ship.blockCommandForOneFrame(ShipCommand.TURN_RIGHT);
            ArmorGridAPI aromr = ship.getArmorGrid();
            int y = 0;
            for (float[] row : aromr.getGrid()) {
                int x = 0;
                for (float col : row) {
                    if (col < aromr.getMaxArmorInCell()) {
                        aromr.setArmorValue(x, y, Math.max(aromr.getArmorRating() / 15f,
                                aromr.getArmorValue(x, y) + aromr.getArmorRating() / 15f * 0.2f * amount));
                    }
                    x++;
                }
                y++;
            }
            if (reviveFader.isFadedOut()) {
                ship.clearDamageDecals();
                reviveFader.fadeIn();
            }
            if (reviveFader.isFadedIn()) {
                ship.syncWithArmorGridState();
                ship.syncWeaponDecalsWithArmorDamage();
                isReviving = false;
                reviveDelayed = false;
                revived = true;
                ship.setAlphaMult(1);
                ship.setCollisionClass(CollisionClass.SHIP);
                ship.fadeToColor(EGO_REVIVE, new Color(200, 200, 200, 255), 9999f, 9999f, 0.3f);
                ship.getMutableStats().getFluxCapacity().modifyPercent(EGO_REVIVE, 20f);
                ship.getMutableStats().getFluxDissipation().modifyPercent(EGO_REVIVE, 20f);
                ship.getMutableStats().getSystemCooldownBonus().modifyPercent(EGO_REVIVE, 100f);
                ship.getFluxTracker().setCurrFlux(0);
                ship.getFluxTracker().setHardFlux(0);
                ship.setCurrentCR(1);
                putEmotions(0, 5);
                reloadMissile();
            }
        }

        public void addDamageDealt(float amount) {
            DamageDealt += amount;
            DamageDealtLegacy += amount;
            if (Global.getCombatEngine() != null && !Global.getCombatEngine().isSimulation()
                    && !Global.getCombatEngine().isMission())
                buff.addPositive(amount);
            if (DamageDealt >= POSITIVE.get(ship.getHullSize())) {
                DamageDealt -= POSITIVE.get(ship.getHullSize());
                if (cards.getCurrCards() < 6)
                    putEmotions(1, 0);
            }
        }

        public void addDamageTaken(float amount) {
            DamageTaken += amount;
            if (Global.getCombatEngine() != null && !Global.getCombatEngine().isSimulation()
                    && !Global.getCombatEngine().isMission())
                buff.addNegative(amount);
            if (DamageTaken >= NEGATIVE.get(ship.getHullSize())) {
                DamageTaken -= NEGATIVE.get(ship.getHullSize());
                if (cards.getCurrCards() < 6)
                    putEmotions(0, 1);
            }
        }

        private void reloadMissile() {
            for (WeaponAPI weapon : ship.getAllWeapons()) {
                if (!weapon.isDecorative() && weapon.usesAmmo()) {
                    weapon.setAmmo(weapon.getMaxAmmo());
                }
            }
        }

        private int positive = 0;
        private int negative = 0;
        private int level = 0;

        private void putEmotions(int Positive, int Negative) {
            int P = Positive;
            int N = Negative;
            // Global.getLogger(this.getClass()).info("N:"+N+" P:"+P);
            if (getLevel() >= MAX_LEVEL) {
                return;
            }
            while (P > 0 || N > 0) {
                if (P > 0) {
                    P--;
                    positive++;
                    if (positive + negative >= 5 + level) {
                        level++;
                        checkEmotionCards();
                        int toMinus = 5 + level;
                        if (toMinus >= positive) {
                            positive = 0;
                            toMinus -= positive;
                        } else {
                            positive -= toMinus;
                            toMinus = 0;
                        }
                        if (toMinus > 0) {
                            negative -= toMinus;
                            toMinus = 0;
                        }


                    }
                }
                if (N > 0) {
                    N--;
                    negative++;
                    if (positive + negative >= 5 + level) {
                        level++;
                        checkEmotionCards();
                        int toMinus = 5 + level;
                        if (toMinus >= positive) {
                            positive = 0;
                            toMinus -= positive;
                        } else {
                            positive -= toMinus;
                            toMinus = 0;
                        }
                        if (toMinus > 0) {
                            negative -= toMinus;
                            toMinus = 0;
                        }
                    }
                }
            }
        }

        private void checkEmotionCards() {
            cards.pickCardAndApply(positive, negative, level,
                    ship.getHullSpec().getHullId(), ship);
        }

    public float getDamageDealtLegacy() {
        return DamageDealtLegacy;
    }

    public void setDamageDealtLegacy(float amount) {
        DamageDealtLegacy = amount;
    }

    public int getLevel() {
        return level;
    }

    public void modifyDamageDealtLegacy(float delta) {
        DamageDealtLegacy += delta;
        if (DamageDealtLegacy < 0) {
            DamageDealtLegacy = 0;
        }
    }

}

public static class HSIEGOBuff implements Buff {
    public static final String ID = "HSI_EGO_Campaign_Buff";
    private int PositiveCount = 0;
    private int PositiveDealt = 0;
    private float currPositive = 0;
    private int NegativeCount = 0;
    private int NegativeDealt = 0;
    private float currNegative = 0;
    private static final int POSITIVE_BASE = 100000;
    private static final int NEGATIVE_BASE = 60000;

    public void apply(FleetMemberAPI member) {
    }

    public String getId() {
        return ID;
    }

    public void advance(float days) {

    }

    public boolean isExpired() {
        return false;
    }

    public int getPositiveCount() {
        return PositiveCount;
    }

    public int getNegativeCount() {
        return NegativeCount;
    }

    public int getPositiveDealt() {
        return PositiveDealt;
    }

    public int getNegativeDealt() {
        return NegativeDealt;
    }

    public void addPositive(float amount) {
        currPositive += amount;
        PositiveDealt = (int) (PositiveDealt + amount);
        if (currPositive >= POSITIVE_BASE * (1 + 0.25f * PositiveCount)) {
            currPositive -= POSITIVE_BASE * (1 + 0.25f * PositiveCount);
            PositiveCount++;
        }
    }

    public void addNegative(float amount) {
        currNegative += amount;
        NegativeDealt = (int) (NegativeDealt + amount);
        if (currNegative >= NEGATIVE_BASE * (1 + 0.25f * NegativeCount)) {
            currNegative -= NEGATIVE_BASE * (1 + 0.25f * NegativeCount);
            NegativeCount++;
        }
    }

}

    public void applyEffectsBeforeShipCreation(MutableShipStatsAPI stats, String id) {
        HSIEGOBuff buff = new HSIEGOBuff();
        if (stats.getFleetMember() != null) {
            if (stats.getFleetMember().getBuffManager().getBuff(HSIEGOBuff.ID) != null) {
                buff = (HSIEGOBuff) (stats.getFleetMember().getBuffManager().getBuff(HSIEGOBuff.ID));
            } else {
                stats.getFleetMember().getBuffManager().addBuff(buff);
            }
        }
        stats.getCRLossPerSecondPercent().modifyMult(id, CR_REDUCE_BONUS);
    }

    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {
        HSIEGOBuff buff = new HSIEGOBuff();
        FleetMemberAPI member = ship.getMutableStats().getFleetMember();
        if (member != null) {
            if (member.getBuffManager().getBuff(HSIEGOBuff.ID) != null) {
                buff = (HSIEGOBuff) (member.getBuffManager().getBuff(HSIEGOBuff.ID));
            } else {
                member.getBuffManager().addBuff(buff);
            }
        }
        HSICombatRendererV2.getInstance().addFxObject(new HSIButterflyRenderObjectV2(ship, ship.getHullSpec().getWeaponSlot("DECOB"), 16, 15));
        ship.addListener(new HSIEGOStats(ship, buff));
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if (!ship.isAlive()) return;
        if (ship.getCurrentCR() > ship.getCRAtDeployment()) {
            ship.setCurrentCR(ship.getCurrentCR() - 0.0025f * amount);
        }
    }

    @Override
    public void applyEffectsToFighterSpawnedByShip(ShipAPI fighter, ShipAPI ship, String id) {
        if (!fighter.getCustomData().containsKey("HSI_FX_DONE") && fighter.getHullSpec().getBaseHullId().equals("HSI_ButterFly")) {
            HSICombatRendererV2.getInstance().addFxObject(new HSIButterflyRenderObject(fighter, 16, 15));
            fighter.setExtraAlphaMult(0f);
            fighter.setApplyExtraAlphaToEngines(true);
            fighter.setCustomData("HSI_FX_DONE", true);
        }
    }

    @Override
    public void addPostDescriptionSection(TooltipMakerAPI tooltip, HullSize hullSize, ShipAPI ship, float width,
                                          boolean isForModSpec) {
        float pad = 3f;
        float opad = 10f;
        Color h = Misc.getHighlightColor();
        Color bad = Misc.getNegativeHighlightColor();

        LabelAPI label = tooltip.addPara(HSII18nUtil.getHullModString("HSIEGO0"), pad, h);

        tooltip.addSectionHeading(HSII18nUtil.getHullModString("HSIEGO1"), Alignment.MID, opad);
        label = tooltip.addPara(HSII18nUtil.getHullModString("HSIEGO2"), opad, h,
                "" + NEGATIVE.get(hullSize), "" + POSITIVE.get(hullSize), "" + 1, "" + 5, "" + 1, "" + 6);
        label.setHighlight("" + NEGATIVE.get(hullSize), "" + POSITIVE.get(hullSize), "" + 1, "" + 5, "" + 1, "" + 6);
        label.setHighlightColors(h, h, h, h, h, h);

        label = tooltip.addPara(HSII18nUtil.getHullModString("HSIEGO3"), opad, h,
                "" + 4 + "%");
        label.setHighlight("" + 4 + "%");
        label.setHighlightColors(h);

        label = tooltip.addPara(HSII18nUtil.getHullModString("HSIEGO4"), opad, h,
                "50%", "25%", "5");
        label.setHighlight("50%", "25%", "5");
        label.setHighlightColors(h, h, bad);

        label = tooltip.addPara(HSII18nUtil.getHullModString("HSIEGO_HUNT"), opad, h,
                AjimusUtils.FormatMapOutPutWithPercentage(KILL_CR), (int) (CR_REDUCE_BONUS * 100f) + "%", (int) (EXTRA_CR_LIMIT * 100f) + "%");
        label.setHighlight(AjimusUtils.FormatMapOutPutWithPercentage(KILL_CR), (int) (CR_REDUCE_BONUS * 100f) + "%", (int) (EXTRA_CR_LIMIT * 100f) + "%");
        label.setHighlightColors(h, h, bad);

        tooltip.addSectionHeading(HSII18nUtil.getHullModString("HSIEGO5"), Alignment.MID, opad);
        label = tooltip.addPara(HSII18nUtil.getHullModString("HSIEGO6"), opad, h);

        tooltip.addSectionHeading(HSII18nUtil.getHullModString("HSIEGO7"), Alignment.MID, opad);
        int P = 0;
        int N = 0;
        int PD = 0;
        int ND = 0;
        if (ship.getFleetMember() != null) {
            if (ship.getFleetMember().getBuffManager().getBuff(HSIEGOBuff.ID) != null) {
                HSIEGOBuff buff = (HSIEGOBuff) ship.getFleetMember().getBuffManager().getBuff(HSIEGOBuff.ID);
                N = buff.getNegativeCount();
                P = buff.getPositiveCount();
                PD = buff.getPositiveDealt();
                ND = buff.getNegativeDealt();
            }
        }
        label = tooltip.addPara(HSII18nUtil.getHullModString("HSIEGO8"), opad, h, "" + PD + "(" + P + ")",
                "" + ND + "(" + N + ")");
        label.setHighlight("" + PD + "(" + P + ")", "" + ND + "(" + N + ")");
        label.setHighlightColors(h, bad);

        if (ship.getHullSpec().getBaseHullId().equals(HSI_EGO_BUTTERFLY)) {
            tooltip.addSectionHeading(HSII18nUtil.getHullModString("HSIEGOButterFly0"), Alignment.MID, opad);
            label = tooltip.addPara(HSII18nUtil.getHullModString("HSIEGOButterFly1"), opad, h, "50%", "5%", "75%");
            label.setHighlight("50%", "5%", "75%");
            label.setHighlightColors(h, h, h);

            label = tooltip.addPara(HSII18nUtil.getHullModString("HSIEGOButterFly2"), opad, h, "50%");
            label.setHighlight("50%");
            label.setHighlightColors(h);

            label = tooltip.addPara(HSII18nUtil.getHullModString("HSIEGOButterFly3"), opad, h, "50%");
            label.setHighlight("50%");
            label.setHighlightColors(h);
        }
    }

}
