package data.shipsystems.scripts;

import java.util.EnumSet;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.FaderUtil;

public class HSIRepairDrone extends BaseShipSystemScript {
    private ShipAPI ship;
    // private boolean once = true;
    private CombatEngineAPI engine = Global.getCombatEngine();
    private ShipAPI source;

    public enum HSIRepairDroneMode {
        CR, AMMO, HP;
    }

    public static final String KEY = "HSIRepairDroneMode";

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        if (effectLevel >= 1 && ship.getAIFlags().hasFlag(AIFlags.TARGET_FOR_SHIP_SYSTEM)
                && ship.getAIFlags().getCustom(AIFlags.TARGET_FOR_SHIP_SYSTEM) instanceof ShipAPI) {
            ShipAPI target = (ShipAPI) ship.getAIFlags().getCustom(AIFlags.TARGET_FOR_SHIP_SYSTEM);
            if (target.getCustomData().containsKey(KEY)) {
                if (target.getCustomData().get(KEY) instanceof HSIRepairDroneMode) {
                    HSIRepairDroneMode modepick = (HSIRepairDroneMode) target.getCustomData().get(KEY);
                    switch (modepick) {
                        case CR:
                            engine.addLayeredRenderingPlugin(new HSIRepairDroneScriptCR(target));
                            break;
                        case AMMO:
                            
                    }
                }
            }
        }

    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
    }

    public class HSIRepairDroneScriptCR extends BaseCombatLayeredRenderingPlugin {
        private ShipAPI target;
        private FaderUtil scanfader = new FaderUtil(0f, 1.5f);
        private FaderUtil renderFader = new FaderUtil(0f, 1.5f);

        public HSIRepairDroneScriptCR(ShipAPI target) {
            this.target = target;
            scanfader.fadeIn();
            renderFader.fadeIn();
            this.layer = CombatEngineLayers.ABOVE_PARTICLES_LOWER;
        }

        public void advance(float amount) {
            if (!scanfader.isFadedIn()) {
                scanfader.advance(amount);
            } else {
                renderFader.advance(amount);
            }
        }

        @Override
        public float getRenderRadius() {
            return 10000000f;
        }

        @Override
        public void render(CombatEngineLayers layer, ViewportAPI viewport) {
            if (layer == this.layer) {
                if (!scanfader.isFadedIn()) {
                
            } else {
                
            }
            }
        }

        @Override
        public EnumSet<CombatEngineLayers> getActiveLayers() {
            return EnumSet.of(CombatEngineLayers.ABOVE_PARTICLES_LOWER);
        }
    }
}
