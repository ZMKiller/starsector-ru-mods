package data.hullmods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.listeners.*;
import data.kit.HSII18nUtil;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamageAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI.ShipEngineAPI;

public class HSIEGOCardsModifier {
    private int currCards = 0;
    private List<HSIEGOCard> picked = new ArrayList<HSIEGOCard>();
    protected static List<HSIEGOCard> PositiveCards = new ArrayList<HSIEGOCard>();
    static {
        PositiveCards.add(new HSIEGOCard("Peaceful", 0, false, null));
        PositiveCards.add(new HSIEGOCard("Pleasure", 4, false, null));
        PositiveCards.add(new HSIEGOCard("Steady", 0, false, null));
        PositiveCards.add(new HSIEGOCard("RIP", 0, true, HSIEGO.HSI_EGO_BUTTERFLY));
        PositiveCards.add(new HSIEGOCard("Coffin", 0, true, HSIEGO.HSI_EGO_BUTTERFLY));
    }

    protected static List<HSIEGOCard> NegativeCards = new ArrayList<HSIEGOCard>();
    static {
        NegativeCards.add(new HSIEGOCard("Desire", 0, false, null));
        NegativeCards.add(new HSIEGOCard("Depression", 4, false, null));
        NegativeCards.add(new HSIEGOCard("Anger", 0, false, null));
        NegativeCards.add(new HSIEGOCard("Mourning", 0, true, HSIEGO.HSI_EGO_BUTTERFLY));
        NegativeCards.add(new HSIEGOCard("Grief", 0, true, HSIEGO.HSI_EGO_BUTTERFLY));
    }

    public static Map<HullSize, Float> Desire = new HashMap<>();
    static {
        Desire.put(HullSize.FRIGATE, 13f);
        Desire.put(HullSize.DESTROYER, 10f);
        Desire.put(HullSize.CRUISER, 8f);
        Desire.put(HullSize.CAPITAL_SHIP, 5f);
    }

    private int FailSafeIndexP = 0;
    private int FailSafeIndexN = 0;

    public static class HSIEGOCardModifierStatsListener implements AdvanceableListener{
        protected String content = "";
        protected int level = 0;
        private ShipAPI ship;

        public HSIEGOCardModifierStatsListener(int level,String content,ShipAPI ship){
            this.level = level;
            this.content = content;
            this.ship= ship;
        }

        @Override
        public void advance(float amount) {
            if(!ship.isAlive()){
                ship.removeListener(this);
                return;
            }
            if(ship.equals(Global.getCombatEngine().getPlayerShip())){
                Global.getCombatEngine().maintainStatusForPlayerShip("HSIEGOCARD"+level,
                        "graphics/icons/hullsys/fortress_shield.png", HSII18nUtil.getHullModString("HSIEGOCardsEffect")+level, content,
                        false);
            }
        }
    }

    public void apply(String id, ShipAPI ship,int level) {
        MutableShipStatsAPI stats = ship.getMutableStats();
        switch (id) {
            case "Peaceful":
                float toAdd = 5000f;
                float lost = ship.getMaxHitpoints()-ship.getHitpoints();
                if(lost>toAdd){
                    ship.setHitpoints(ship.getHitpoints()+toAdd);
                    toAdd = 0;
                }else{
                    ship.setHitpoints(ship.getHitpoints()+lost);
                    toAdd-=lost;
                }
                if(toAdd>0){
                    toAdd/=2;
                    ship.setMaxHitpoints(ship.getMaxHitpoints()+toAdd);
                    ship.setHitpoints(ship.getMaxHitpoints());
                }
                break;
            case "Pleasure":
                ship.setCustomData("HSI_EGO_Pleasure",true);
                ship.addListener(new HSIEGOCardModifierStatsListener(level,HSII18nUtil.getHullModString("HSIEGOCardsEffectPleasure"),ship));
                break;
            case "Steady":
                stats.getBallisticWeaponRangeBonus().modifyMult("HSIEGOSteady", 1.15f);
                stats.getEnergyWeaponRangeBonus().modifyMult("HSIEGOSteady", 1.15f);
                stats.getMissileWeaponRangeBonus().modifyMult("HSIEGOSteady", 1.15f);
                ship.addListener(new HSIEGOCardModifierStatsListener(level,HSII18nUtil.getHullModString("HSIEGOCardsEffectSteady"),ship));
                break;
            case "RIP":
                ship.addListener(new DamageDealtModifier() {
                    public String modifyDamageDealt(Object param, CombatEntityAPI target,
                            DamageAPI damage, Vector2f point, boolean shieldHit) {
                        if (target.getHitpoints() < target.getMaxHitpoints() * 0.33f) {
                            damage.getModifier().modifyMult("HSIEGORIP", 1.15f);
                        }
                        return "HSIEGORIP";
                    }
                });
                ship.addListener(new HSIEGOCardModifierStatsListener(level,HSII18nUtil.getHullModString("HSIEGOCardsEffectRIP"),ship));
                break;
            case "Coffin":
                ship.addListener(new DamageDealtModifier() {
                    public String modifyDamageDealt(Object param, CombatEntityAPI target,
                            DamageAPI damage, Vector2f point, boolean shieldHit) {
                        float frac = Math.max(damage.getBaseDamage() * 0.005f,0.85f);
                        if (Math.random() < frac) {
                            if (target instanceof ShipAPI) {
                                ShipAPI t = (ShipAPI) target;
                                List<ShipEngineAPI> engines = t.getEngineController().getShipEngines();
                                engines.get((int) (Math.min(engines.size() - 1, Math.random() * engines.size())))
                                        .disable();
                            }
                        }
                        return "HSIEGOCoffin";
                    }
                });
                ship.addListener(new HSIEGOCardModifierStatsListener(level,HSII18nUtil.getHullModString("HSIEGOCardsEffectCoffin"),ship));
                break;
            case "Desire":
                stats.getHullDamageTakenMult().modifyPercent("HSIEGODesire", 10f);
                stats.getMaxSpeed().modifyPercent("HSIEGODesire", Desire.get(ship.getHullSize()));
                stats.getMaxTurnRate().modifyPercent("HSIEGODesire", 10f);
                stats.getAcceleration().modifyPercent("HSIEGODesire", 10f);
                stats.getDeceleration().modifyPercent("HSIEGODesire", 10f);
                stats.getTurnAcceleration().modifyPercent("HSIEGODesire", 10f);
                ship.addListener(new HSIEGOCardModifierStatsListener(level,HSII18nUtil.getHullModString("HSIEGOCardsEffectDesire"),ship));
                break;
            case "Depression":
                stats.getBallisticWeaponRangeBonus().modifyMult("HSIEGODepression", 0.9f);
                stats.getEnergyWeaponRangeBonus().modifyMult("HSIEGODepression", 0.9f);
                stats.getMissileWeaponRangeBonus().modifyMult("HSIEGODepression", 0.9f);
                stats.getBallisticWeaponDamageMult().modifyPercent("HSIEGODepression", 10f);
                stats.getEnergyWeaponDamageMult().modifyPercent("HSIEGODepression", 10f);
                stats.getMissileWeaponDamageMult().modifyPercent("HSIEGODepression", 10f);
                ship.addListener(new HSIEGOCardModifierStatsListener(level,HSII18nUtil.getHullModString("HSIEGOCardsEffectDepression"),ship));
                break;
            case "Anger":
                stats.getBallisticRoFMult().modifyPercent("HSIEGOAnger", 10f);
                stats.getEnergyRoFMult().modifyPercent("HSIEGOAnger", 10f);
                stats.getMissileRoFMult().modifyPercent("HSIEGOAnger", 10f);
                stats.getHullDamageTakenMult().modifyPercent("HSIEGOAnger", 10f);
                ship.addListener(new HSIEGOCardModifierStatsListener(level,HSII18nUtil.getHullModString("HSIEGOCardsEffectAnger"),ship));
                break;
            case "Mourning":
                ship.addListener(new DamageDealtModifier() {
                    public String modifyDamageDealt(Object param, CombatEntityAPI target,
                            DamageAPI damage, Vector2f point, boolean shieldHit) {
                        if (target instanceof ShipAPI) {
                            ShipAPI t = (ShipAPI) target;
                            if (t.getFluxLevel() > 0.8f)
                                damage.getModifier().modifyPercent("HSIEGOMourning0", 10f);
                            if (t.getFluxTracker().isOverloaded())
                                damage.getModifier().modifyPercent("HSIEGOMourning1", 10f);
                            if (t.getFluxTracker().isVenting())
                                damage.getModifier().modifyPercent("HSIEGOMourning2", 10f);
                            if (t.getEngineController().isFlamedOut())
                                damage.getModifier().modifyPercent("HSIEGOMourning3", 10f);
                            if (t.getCurrentCR() < t.getCRAtDeployment())
                                damage.getModifier().modifyPercent("HSIEGOMourning4", 10f);
                        }
                        return "HSIEGOMourning";
                    }
                });
                //stats.getFluxDissipation().modifyPercent("HSIEGOMourning", -15f);
                ship.addListener(new HSIEGOCardModifierStatsListener(level,HSII18nUtil.getHullModString("HSIEGOCardsEffectMourning"),ship));
                break;
            case "Grief":
                ship.addListener(new DamageDealtModifier() {
                    public String modifyDamageDealt(Object param, CombatEntityAPI target,
                            DamageAPI damage, Vector2f point, boolean shieldHit) {
                        if (target instanceof ShipAPI) {
                            ShipAPI t = (ShipAPI) target;
                            if (t.getFluxTracker().isOverloadedOrVenting())
                                damage.getModifier().modifyPercent("HSIEGOGrief", 20f);
                        }
                        return "HSIEGOGrief";
                    }
                });
                ship.addListener(new DamageTakenModifier() {
                    public String modifyDamageTaken(Object param, CombatEntityAPI target,
                            DamageAPI damage, Vector2f point, boolean shieldHit) {
                        if (target instanceof ShipAPI) {
                            ShipAPI t = (ShipAPI) target;
                            if (t.getFluxTracker().isOverloadedOrVenting())
                                damage.getModifier().modifyMult("HSIEGOGrief", 0.8f);
                        }
                        return "HSIEGOGrief";
                    }
                });
                ship.addListener(new HSIEGOCardModifierStatsListener(level,HSII18nUtil.getHullModString("HSIEGOCardsEffectGrief"),ship));
                break;
        }
    }

    public static final int DRAW_CARD_NUM = 3;

    public void pickCardAndApply(int positive,int negative, int lv, String HullId, ShipAPI ship) {
        List<HSIEGOCard> deck = new ArrayList<HSIEGOCard>();
        int P = positive;
        int N = negative;
        float PFrac = (float) (P / (P + N));
        List<HSIEGOCard> tempP = new ArrayList<HSIEGOCard>(PositiveCards);
        List<HSIEGOCard> tempN = new ArrayList<HSIEGOCard>(NegativeCards);
        for (HSIEGOCard pd : picked) {
            if (tempP.contains(pd)) {
                tempP.remove(pd);
            }
            if (tempN.contains(pd)) {
                tempN.remove(pd);
            }
        }
        List<HSIEGOCard> remove = new ArrayList<HSIEGOCard>();
        for (HSIEGOCard p : tempP) {
            if (p.isSpecific) {
                if (!p.id.equals(HullId)) {
                    remove.add(p);
                }
            } else {
                if (p.lv > lv) {
                    remove.add(p);
                }
            }
        }
        tempP.removeAll(remove);
        remove.clear();
        for (HSIEGOCard n : tempN) {
            if (n.isSpecific) {
                if (!n.id.equals(HullId)) {
                    remove.add(n);
                }
            } else {
                if (n.lv > lv) {
                    remove.add(n);
                }
            }
        }
        tempN.removeAll(remove);
        remove.clear();
        for (int i = 0; i < DRAW_CARD_NUM; i++) {
            if (Math.random() <= PFrac) {
                int index = (int) (Math.random() * tempP.size());
                if (index >= tempP.size())
                    index = tempP.size() - 1;
                if (index < 0)
                    index = 0;
                if (tempP.size() > index) {
                    deck.add(tempP.get(index));
                }
            } else {
                int index = (int) (Math.random() * tempN.size());
                if (index >= tempN.size())
                    index = tempN.size() - 1;
                if (index < 0)
                    index = 0;
                if (tempN.size() > index) {
                    deck.add(tempN.get(index));
                }
            }
        }
        int index = (int) (Math.random() * deck.size());
        if (index >= deck.size())
            index = deck.size() - 1;
        if (index < 0)
            index = 0;
        if (!deck.isEmpty()) {
            apply(deck.get(index).id, ship,lv);
            currCards++;
            picked.add(deck.get(index));
        } else {
            if (FailSafeIndexN >= FailSafeIndexP) {
                if(FailSafeIndexP>2) FailSafeIndexP = 2;
                apply(PositiveCards.get(FailSafeIndexP).id, ship,lv);
                currCards++;
                FailSafeIndexP++;
            }else{
                if(FailSafeIndexN>2) FailSafeIndexN = 2;
                apply(NegativeCards.get(FailSafeIndexN).id, ship,lv);
                currCards++;
                FailSafeIndexN++;
            }
        }
    }

    public int getCurrCards() {
        return currCards;
    }
}
