package data.scripts.everyframe;

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI.ShipEngineAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.IntervalUtil;
import data.scripts.util.SWP_Util;
import java.awt.Color;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.dark.shaders.light.LightShader;
import org.dark.shaders.light.StandardLight;
import org.lwjgl.util.vector.Vector2f;

public class SWP_SystemLightInjector extends BaseEveryFrameCombatPlugin {

    private static final String DATA_KEY = "SWP_LightInjector";

    private static final Vector2f ZERO = new Vector2f();

    private CombatEngineAPI engine;
    private boolean activated = false;
    private final IntervalUtil inactiveInterval = new IntervalUtil(1f, 2f);

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine == null) {
            return;
        }

        if (engine.isPaused()) {
            return;
        }

        if (!activated) {
            inactiveInterval.advance(amount);
            if (!inactiveInterval.intervalElapsed()) {
                return;
            }
        }

        if (!engine.getCustomData().containsKey(DATA_KEY)) {
            engine.getCustomData().put(DATA_KEY, new LocalData());
        }

        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        final Map<ShipAPI, StandardLight> lights = localData.lights;

        List<ShipAPI> ships = engine.getShips();
        int shipsSize = ships.size();
        for (int i = 0; i < shipsSize; i++) {
            ShipAPI ship = ships.get(i);
            if (ship.isHulk()) {
                continue;
            }

            float shipRadius = SWP_Util.effectiveRadius(ship);

            ShipSystemAPI system = ship.getSystem();
            if (system != null) {
                String id = system.getId();
                switch (id) {
                    case "swp_punisherjets":
                        if (system.isActive()) {
                            Vector2f location = null;
                            if (ship.getEngineController() == null) {
                                break;
                            }
                            List<ShipEngineAPI> engines = ship.getEngineController().getShipEngines();
                            int num = 0;
                            int enginesSize = engines.size();
                            for (int j = 0; j < enginesSize; j++) {
                                ShipEngineAPI eng = engines.get(j);
                                if (eng.isActive() && !eng.isDisabled()) {
                                    num++;
                                    if (location == null) {
                                        location = new Vector2f(eng.getLocation());
                                    } else {
                                        Vector2f.add(location, eng.getLocation(), location);
                                    }
                                }
                            }
                            if (location == null) {
                                break;
                            }

                            location.scale(1f / num);

                            if (lights.containsKey(ship)) {
                                StandardLight light = lights.get(ship);

                                light.setLocation(location);

                                if ((system.isActive() && !system.isOn()) || system.isChargedown()) {
                                    if (!light.isFadingOut()) {
                                        light.fadeOut(0.4f);
                                    }
                                }
                            } else {
                                StandardLight light = new StandardLight(location, ZERO, ZERO, null);
                                float intensity = (float) Math.sqrt(shipRadius) / 20f;
                                float size = intensity * 400f;

                                light.setIntensity(intensity);
                                light.setSize(size);
                                Color color = null;
                                if (!ship.getEngineController().getShipEngines().isEmpty()) {
                                    color = ship.getEngineController().getShipEngines().get(0).getEngineColor();
                                }
                                if (color != null) {
                                    light.setColor(color);
                                }
                                light.fadeIn(0.4f);

                                lights.put(ship, light);
                                LightShader.addLight(light);
                            }
                        }
                        activated = true;
                        break;
                    case "swp_victoryjets":
                        if (system.isActive()) {
                            Vector2f location = null;
                            if (ship.getEngineController() == null) {
                                break;
                            }
                            List<ShipEngineAPI> engines = ship.getEngineController().getShipEngines();
                            int num = 0;
                            int enginesSize = engines.size();
                            for (int j = 0; j < enginesSize; j++) {
                                ShipEngineAPI eng = engines.get(j);
                                if (eng.isActive() && !eng.isDisabled()) {
                                    num++;
                                    if (location == null) {
                                        location = new Vector2f(eng.getLocation());
                                    } else {
                                        Vector2f.add(location, eng.getLocation(), location);
                                    }
                                }
                            }
                            if (location == null) {
                                break;
                            }

                            location.scale(1f / num);

                            if (lights.containsKey(ship)) {
                                StandardLight light = lights.get(ship);

                                light.setLocation(location);

                                if ((system.isActive() && !system.isOn()) || system.isChargedown()) {
                                    if (!light.isFadingOut()) {
                                        light.fadeOut(1f);
                                    }
                                }
                            } else {
                                StandardLight light = new StandardLight(location, ZERO, ZERO, null);
                                float intensity = (float) Math.sqrt(shipRadius) / 10f;
                                float size = intensity * 200f;

                                light.setIntensity(intensity);
                                light.setSize(size);
                                Color color = null;
                                if (!ship.getEngineController().getShipEngines().isEmpty()) {
                                    color = ship.getEngineController().getShipEngines().get(0).getEngineColor();
                                }
                                if (color != null) {
                                    light.setColor(color);
                                }
                                light.fadeIn(0.5f);

                                lights.put(ship, light);
                                LightShader.addLight(light);
                            }
                        }
                        activated = true;
                        break;
                    case "swp_boss_aresdrive":
                        if (system.isActive()) {
                            Vector2f location = null;
                            if (ship.getEngineController() == null) {
                                break;
                            }
                            List<ShipEngineAPI> engines = ship.getEngineController().getShipEngines();
                            int num = 0;
                            int enginesSize = engines.size();
                            for (int j = 0; j < enginesSize; j++) {
                                ShipEngineAPI eng = engines.get(j);
                                if (eng.isActive() && !eng.isDisabled()) {
                                    num++;
                                    if (location == null) {
                                        location = new Vector2f(eng.getLocation());
                                    } else {
                                        Vector2f.add(location, eng.getLocation(), location);
                                    }
                                }
                            }
                            if (location == null) {
                                break;
                            }

                            location.scale(1f / num);

                            if (lights.containsKey(ship)) {
                                StandardLight light = lights.get(ship);

                                light.setLocation(location);

                                if ((system.isActive() && !system.isOn()) || system.isChargedown()) {
                                    if (!light.isFadingOut()) {
                                        light.fadeOut(1f);
                                    }
                                }
                            } else {
                                StandardLight light = new StandardLight(location, ZERO, ZERO, null);
                                float intensity = (float) Math.sqrt(shipRadius) / 10f;
                                float size = intensity * 200f;

                                light.setIntensity(intensity);
                                light.setSize(size);
                                light.setColor(1f, 0.6f, 0.4f);
                                light.fadeIn(0.5f);

                                lights.put(ship, light);
                                LightShader.addLight(light);
                            }
                        }
                        activated = true;
                        break;
                    case "swp_boss_nikejets":
                        if (system.isActive()) {
                            Vector2f location = null;
                            if (ship.getEngineController() == null) {
                                break;
                            }
                            List<ShipEngineAPI> engines = ship.getEngineController().getShipEngines();
                            int num = 0;
                            int enginesSize = engines.size();
                            for (int j = 0; j < enginesSize; j++) {
                                ShipEngineAPI eng = engines.get(j);
                                if (eng.isActive() && !eng.isDisabled()) {
                                    num++;
                                    if (location == null) {
                                        location = new Vector2f(eng.getLocation());
                                    } else {
                                        Vector2f.add(location, eng.getLocation(), location);
                                    }
                                }
                            }
                            if (location == null) {
                                break;
                            }

                            location.scale(1f / num);

                            if (lights.containsKey(ship)) {
                                StandardLight light = lights.get(ship);

                                light.setLocation(location);

                                if ((system.isActive() && !system.isOn()) || system.isChargedown()) {
                                    if (!light.isFadingOut()) {
                                        light.fadeOut(1f);
                                    }
                                }
                            } else {
                                StandardLight light = new StandardLight(location, ZERO, ZERO, null);
                                float intensity = (float) Math.sqrt(shipRadius) / 15f;
                                float size = intensity * 200f;

                                light.setIntensity(intensity);
                                light.setSize(size);
                                light.setColor(0.4f, 0.6f, 1f);
                                light.fadeIn(1f);

                                lights.put(ship, light);
                                LightShader.addLight(light);
                            }
                        }
                        activated = true;
                        break;
                    case "swp_arcade_forceimpeder":
                        if (system.isActive()) {
                            Vector2f location = ship.getLocation();

                            if (lights.containsKey(ship)) {
                                StandardLight light = lights.get(ship);

                                light.setLocation(location);
                                light.setColor((float) Math.random() * 0.5f + 0.5f, (float) Math.random() * 0.5f + 0.5f,
                                        (float) Math.random() * 0.5f + 0.5f);

                                if ((system.isActive() && !system.isOn()) || system.isChargedown()) {
                                    if (!light.isFadingOut()) {
                                        light.fadeOut(1f);
                                    }
                                }
                            } else {
                                StandardLight light = new StandardLight(location, ZERO, ZERO, null);

                                light.setIntensity(0.5f);
                                light.setSize(4000f);
                                light.setColor((float) Math.random() * 0.5f + 0.5f, (float) Math.random() * 0.5f + 0.5f,
                                        (float) Math.random() * 0.5f + 0.5f);
                                light.fadeIn(2f);

                                lights.put(ship, light);
                                LightShader.addLight(light);
                            }
                        }
                        activated = true;
                        break;
                    default:
                        break;
                }
            }
        }

        Iterator<Map.Entry<ShipAPI, StandardLight>> iter = lights.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<ShipAPI, StandardLight> entry = iter.next();
            ShipAPI ship = entry.getKey();

            if ((ship.getSystem() != null && !ship.getSystem().isActive()) || !ship.isAlive()) {
                StandardLight light = entry.getValue();

                light.unattach();
                light.fadeOut(0);
                iter.remove();
            }
        }
    }

    @Override
    public void init(CombatEngineAPI engine) {
        this.engine = engine;
    }

    private static final class LocalData {

        final Map<ShipAPI, StandardLight> lights = new LinkedHashMap<>(100);
    }
}
