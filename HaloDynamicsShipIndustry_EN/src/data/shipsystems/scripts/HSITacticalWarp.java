package data.shipsystems.scripts;

import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;

public class HSITacticalWarp extends BaseShipSystemScript {
    private ShipAPI ship;
    // private boolean once = true;
    private CombatEngineAPI engine = Global.getCombatEngine();
    private HSITacticalWarpScript warp = null;
    private ShipSystemAPI system;

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        if(ship!=null){
            if(ship.getSystem()!=null&&ship.getSystem().getSpecAPI().getId().equals("HSI_TacticalWarp")){
                system = ship.getSystem();
            }else if(ship.getPhaseCloak()!=null&&ship.getPhaseCloak().getSpecAPI().getId().equals("HSI_TacticalWarp")){
                system = ship.getPhaseCloak();
            }
        }
        if (effectLevel > 0 && warp == null&&system!=null) {
            warp = new HSITacticalWarpScript(ship, system);
            engine.addLayeredRenderingPlugin(warp);
        }
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        warp = null;
    }

    public class HSITacticalWarpScript extends BaseCombatLayeredRenderingPlugin {
        protected ShipAPI ship;
        protected ShipSystemAPI system;
        protected float effectlevel = 0f;

        public HSITacticalWarpScript(ShipAPI ship, ShipSystemAPI system) {
            this.ship = ship;
            this.system = system;
        }

        public void advance(float amount){
            if(system.getEffectLevel()>=1){
                Vector2f loc = ship.getMouseTarget();
                if(ship.getAIFlags().hasFlag(AIFlags.SYSTEM_TARGET_COORDS)&&ship.getAIFlags().getCustom(AIFlags.SYSTEM_TARGET_COORDS) instanceof Vector2f){
                    loc = (Vector2f)ship.getAIFlags().getCustom(AIFlags.SYSTEM_TARGET_COORDS);
                }
                ship.getLocation().set(loc);
            }
        }
    }
    

}
