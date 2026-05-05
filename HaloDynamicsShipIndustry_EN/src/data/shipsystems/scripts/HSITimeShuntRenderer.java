package data.shipsystems.scripts;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.ViewportAPI;

import data.shipsystems.scripts.HSITimeShunt.HSITimeBoosterFx;

public class HSITimeShuntRenderer extends BaseCombatLayeredRenderingPlugin {
    protected boolean shouldExpire = false;
    protected List<HSITimeBoosterFx> fxs = new ArrayList<>();

    public HSITimeShuntRenderer() {
        this.layer = CombatEngineLayers.ABOVE_PARTICLES_LOWER;
        Global.getCombatEngine().addLayeredRenderingPlugin(this);
    }

    public void advance(float amount) {
        if (Global.getCombatEngine().isPaused())
            return;
        if (!fxs.isEmpty()) {
            List<HSITimeBoosterFx> toRemove = new ArrayList<>();
            for (HSITimeBoosterFx bfx : fxs) {
                if (bfx.isEnded()) {
                    toRemove.add(bfx);
                    continue;
                }
                bfx.advance(amount);
            }
            fxs.removeAll(toRemove);
        }
    }

    public void render(CombatEngineLayers layer, ViewportAPI viewport) {
        if (this.layer == layer) {
            for (HSITimeBoosterFx c : fxs) {
                c.render();
            }
        }
    }

    public float getRenderRadius() {
        return 10000f;
    }

    public boolean shouldExpire() {
        return shouldExpire;
    }

    public boolean isExpired() {
        return shouldExpire();
    }
}
