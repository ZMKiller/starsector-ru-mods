package data.scripts.HSIContrail;

import java.util.ArrayList;
import java.util.List;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ViewportAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.util.FaderUtil;

public class HSIContrailEntityPlugin extends BaseCombatLayeredRenderingPlugin {
    private ShipAPI ship;
    private List<HSIContrail> contrails = new ArrayList<>();
    public static final String Prefix = "HSI_ContrailDeco";
    private FaderUtil expireTimer = new FaderUtil(1f, 2f);
    public static final String RENDER_KEY = "HSIContrailPlugin";

    private float width = 12f;

    public HSIContrailEntityPlugin(ShipAPI ship) {
        this.ship = ship;
        for (WeaponAPI w : ship.getAllWeapons()) {
            if (w.isDecorative() && w.getId().startsWith(Prefix)) {
                contrails.add(new HSIContrail(ship, w,width));
            }
        }
    }

    public HSIContrailEntityPlugin(ShipAPI ship,float width) {
        this.ship = ship;
        this.width = width;
        for (WeaponAPI w : ship.getAllWeapons()) {
            if (w.isDecorative() && w.getId().startsWith(Prefix)) {
                contrails.add(new HSIContrail(ship, w,width));
            }
        }
    }

    public void advance(float amount) {
        if (shouldExpire() && !expireTimer.isFadingOut())
            expireTimer.fadeOut();
        if (shouldExpire())
            expireTimer.advance(amount);
        for (HSIContrail c : contrails) {
            c.advance(amount);
        }
    }


    public float getRenderRadius() {
        return 10000f;
    }

    public void render(CombatEngineLayers layer, ViewportAPI viewport) {
        if (this.layer == layer) {
            for (HSIContrail c : contrails) {
                c.render();
            }
        }
    }

    public List<HSIContrail> getContrails() {
        return contrails;
    }

    public ShipAPI getShip() {
        return ship;
    }

    public boolean isExpired() {
        return contrails.isEmpty()||(shouldExpire() && expireTimer.isFadedOut());
    }

    public boolean shouldExpire() {
        return !ship.isAlive();
    }

    public static HSIContrailEntityPlugin getInstance(ShipAPI source) {
        if (source.getCustomData().containsKey(RENDER_KEY)) {
            return (HSIContrailEntityPlugin) source.getCustomData().get(RENDER_KEY);
        } else {
            HSIContrailEntityPlugin renderer = new HSIContrailEntityPlugin(source);
            Global.getCombatEngine().addLayeredRenderingPlugin(renderer);
            source.setCustomData(RENDER_KEY, renderer);
            return renderer;
        }
    }

    public static HSIContrailEntityPlugin getInstance(ShipAPI source,float width) {
        if (source.getCustomData().containsKey(RENDER_KEY)) {
            return (HSIContrailEntityPlugin) source.getCustomData().get(RENDER_KEY);
        } else {
            HSIContrailEntityPlugin renderer = new HSIContrailEntityPlugin(source,width);
            Global.getCombatEngine().addLayeredRenderingPlugin(renderer);
            source.setCustomData(RENDER_KEY, renderer);
            return renderer;
        }
    }
}
