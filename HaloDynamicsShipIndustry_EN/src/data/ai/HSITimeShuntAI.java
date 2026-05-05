package data.ai;

import com.fs.starfarer.api.impl.campaign.ids.HullMods;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.FighterLaunchBayAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAIScript;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.api.util.IntervalUtil;

import data.shipsystems.scripts.HSITimeShunt.BOOSTERMODE;

public class HSITimeShuntAI implements ShipSystemAIScript {
    private ShipAPI ship;
    private CombatEngineAPI engine;
    private ShipwideAIFlags flags;
    private ShipSystemAPI system;
    private IntervalUtil think = new IntervalUtil(0.1f, 0.2f);

    public void init(ShipAPI ship, ShipSystemAPI system, ShipwideAIFlags flags, CombatEngineAPI engine) {
        this.ship = ship;
        this.flags = flags;
        this.engine = engine;
        this.system = system;
    }

    @SuppressWarnings("unchecked")
    public void advance(float amount, Vector2f missileDangerDir, Vector2f collisionDangerDir, ShipAPI target) {
        if (engine.isPaused())
            return;
        think.advance(amount);
        if (!think.intervalElapsed())
            return;
        if (system.isCoolingDown() || system.isOn())
            return;
        if (ship.getCustomData() != null && ship.getCustomData().containsKey("HSITimeBoosterMode")) {
            BOOSTERMODE currmode = (BOOSTERMODE) ship.getCustomData().get("HSITimeBoosterMode");
            BOOSTERMODE expectmode = BOOSTERMODE.HANGAR;
            float refitTime = 0;
            float lostBay = 0;
            int numBay = ship.getLaunchBaysCopy().size();
            if (ship.getLaunchBaysCopy() != null) {
                for (FighterLaunchBayAPI wing : ship.getLaunchBaysCopy()) {
                    if (wing.getWing() != null && wing.getWing().getSpec() != null
                            && wing.getWing().getWingMembers() != null) {
                        refitTime += (wing.getWing().getSpec().getNumFighters()
                                - wing.getWing().getWingMembers().size())
                                * wing.getWing().getSpec().getRefitTime();
                        if ((wing.getWing().getSpec().getNumFighters() - wing.getWing().getWingMembers().size()) > 0) {
                            lostBay++;
                        }
                    }
                }
            }
            if (refitTime >= 40f || lostBay / numBay >= 0.5f) {
                expectmode = BOOSTERMODE.HANGAR;
            } else {
                if (!ship.isPullBackFighters()) {
                    expectmode = BOOSTERMODE.FIGHTER;
                } else {
                    expectmode = BOOSTERMODE.WEAPON;
                }
            }

            if(ship.getVariant().hasHullMod(HullMods.SAFETYOVERRIDES)){
                if(target!=null&& MathUtils.getDistanceSquared(ship.getLocation(),target.getLocation())<(600+ship.getCollisionRadius()+target.getCollisionRadius())*(600+ship.getCollisionRadius()+target.getCollisionRadius())){
                    expectmode = BOOSTERMODE.WEAPON;
                }
            }

            if (currmode != expectmode) {
                ship.useSystem();
            }
        }
    }
}
