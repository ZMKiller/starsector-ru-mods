package data.scripts.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import data.scripts.util.SWP_Util;
import java.awt.Color;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.entities.SimpleEntity;
import org.lwjgl.util.vector.Vector2f;

public class SWP_SonidoStats extends BaseShipSystemScript {

    private static final Color COLOR1 = new Color(255, 255, 255);
    private static final Color COLOR2 = new Color(150, 150, 150);
    private static final Color COLOR3 = new Color(100, 0, 0);
    private static final Color COLOR4 = new Color(255, 50, 50);
    private static final Color COLOR5 = new Color(50, 0, 0);
    private static final Color COLOR6 = new Color(150, 50, 200);

    private boolean done = false;

    @Override
    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (!(stats.getEntity() instanceof ShipAPI)) {
            return;
        }

        ShipAPI ship = (ShipAPI) stats.getEntity();

        if (!done) {
            done = true;
            Vector2f origin = ship.getLocation();
            if (!ship.getPhaseCloak().isActive()) {
                Global.getSoundPlayer().playSound("swp_arcade_sonido_activate", 1f, 1f, ship.getLocation(), ship.getVelocity());
            }
            int amountscalar;
            switch (ship.getHullSpec().getBaseHullId()) {
                case "swp_arcade_hyperzero":
                    amountscalar = 3;
                    break;
                case "swp_arcade_superzero":
                    amountscalar = 2;
                    break;
                default:
                    amountscalar = 1;
            }
            float shipRadius = SWP_Util.effectiveRadius(ship);
            for (int i = 0; i < 6 * amountscalar; i++) {
                Vector2f point1 = MathUtils.getRandomPointInCircle(origin, shipRadius * 1.25f);
                Vector2f point2 = MathUtils.getRandomPointInCircle(origin, shipRadius * 1.25f);
                if (ship.getPhaseCloak() != null && ship.getPhaseCloak().isActive()) {
                    Global.getCombatEngine().spawnEmpArc(ship, point1, new SimpleEntity(point1),
                                                         new SimpleEntity(point2), DamageType.ENERGY, 0f, 0f, 1000f,
                                                         null, 10f, COLOR1, COLOR1);
                } else {
                    switch (ship.getHullSpec().getBaseHullId()) {
                        case "swp_arcade_hyperzero":
                            Global.getCombatEngine().spawnEmpArc(ship, point1, new SimpleEntity(point1),
                                                                 new SimpleEntity(point2), DamageType.ENERGY, 0f, 0f,
                                                                 1000f, null, 10f, COLOR2, COLOR1);
                            break;
                        case "swp_arcade_superzero":
                            Global.getCombatEngine().spawnEmpArc(ship, point1, new SimpleEntity(point1),
                                                                 new SimpleEntity(point2), DamageType.ENERGY, 0f, 0f,
                                                                 1000f, null, 7.5f, COLOR3, COLOR4);
                            break;
                        default:
                            Global.getCombatEngine().spawnEmpArc(ship, point1, new SimpleEntity(point1),
                                                                 new SimpleEntity(point2), DamageType.ENERGY, 0f, 0f,
                                                                 1000f, null, 5f, COLOR5, COLOR6);
                    }
                }
            }
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        done = false;
    }
}
