package org.dark.graphics.plugins;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.LocationAPI;
import com.fs.starfarer.api.campaign.PlanetAPI;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI.ShipEngineAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.util.Misc;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.dark.shaders.light.LightShader;
import org.dark.shaders.light.StandardLight;
import org.dark.shaders.util.GraphicsLibSettings;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

@SuppressWarnings("UseSpecificCatch")
public class LightInjector extends BaseEveryFrameCombatPlugin {

    private static final String DATA_KEY = "GLib_LightInjector";

    private static final Vector2f ZERO = new Vector2f();

    private static float effectiveRadius(ShipAPI ship) {
        if (ship.getSpriteAPI() == null || ship.isPiece()) {
            return ship.getCollisionRadius();
        } else {
            float fudgeFactor = 1.5f;
            return ((ship.getSpriteAPI().getWidth() / 2f) + (ship.getSpriteAPI().getHeight() / 2f)) * 0.5f * fudgeFactor;
        }
    }

    private static List<NearbyPlanetData> getNearbyStars(CampaignFleetAPI playerFleet) {
        LocationAPI loc = playerFleet.getContainingLocation();
        List<NearbyPlanetData> result = new ArrayList<>(2);
        if (loc instanceof StarSystemAPI system) {
            for (PlanetAPI planet : system.getPlanets()) {
                if (!planet.isStar()) {
                    continue;
                }
                Vector2f vector = Vector2f.sub(playerFleet.getLocation(), planet.getLocation(), new Vector2f());
                result.add(new NearbyPlanetData(vector, planet));
            }
        }
        return result;
    }

    private CombatEngineAPI engine;

    @Override
    public void advance(float amount, List<InputEventAPI> events) {
        if (engine == null) {
            return;
        }

        if (engine.isPaused()) {
            return;
        }

        if (!Global.getCombatEngine().getCustomData().containsKey(DATA_KEY)) {
            Global.getCombatEngine().getCustomData().put(DATA_KEY, new LocalData());
        }

        final LocalData localData = (LocalData) engine.getCustomData().get(DATA_KEY);
        final Map<ShipAPI, StandardLight> lights = localData.lights;
        final Map<ShipAPI, StandardLight> travelLights = localData.travelLights;

        for (ShipAPI ship : engine.getShips()) {
            if (ship.isHulk()) {
                continue;
            }

            float shipRadius = effectiveRadius(ship);

            if (!ship.isFighter() && !ship.isDrone() && (ship.getTravelDrive() != null) && ship.getTravelDrive().isActive()) {
                Vector2f location = null;
                if (ship.getEngineController() == null) {
                    break;
                }
                int num = 0;
                for (ShipEngineAPI eng : ship.getEngineController().getShipEngines()) {
                    if (eng.isActive() && !eng.isDisabled() && eng.getContribution() > 0f) {
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

                if (travelLights.containsKey(ship)) {
                    StandardLight light = travelLights.get(ship);

                    light.setLocation(location);

                    if ((ship.getTravelDrive().isActive() && !ship.getTravelDrive().isOn())
                            || ship.getTravelDrive().isChargedown()) {
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
                    if (ship.getTravelDrive().isChargeup()) {
                        light.fadeIn(2f);
                    }

                    travelLights.put(ship, light);
                    LightShader.addLight(light);
                }

                continue;
            }

            ShipSystemAPI system = ship.getSystem();
            if (system != null) {
                String id = system.getId();
                switch (id) {
                    case "burndrive" -> {
                        if (system.isActive()) {
                            Vector2f location = null;
                            if (ship.getEngineController() == null) {
                                break;
                            }
                            int num = 0;
                            for (ShipEngineAPI eng : ship.getEngineController().getShipEngines()) {
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
                                light.fadeIn(2f);

                                lights.put(ship, light);
                                LightShader.addLight(light);
                            }
                        }
                    }
                    case "maneuveringjets" -> {
                        if (system.isActive()) {
                            Vector2f location = null;
                            if (ship.getEngineController() == null) {
                                break;
                            }
                            int num = 0;
                            for (ShipEngineAPI eng : ship.getEngineController().getShipEngines()) {
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
                                float intensity = (float) Math.sqrt(shipRadius) / 40f;
                                float size = intensity * 600f;

                                light.setIntensity(intensity);
                                light.setSize(size);
                                Color color = null;
                                if (!ship.getEngineController().getShipEngines().isEmpty()) {
                                    color = ship.getEngineController().getShipEngines().get(0).getEngineColor();
                                }
                                if (color != null) {
                                    light.setColor(color);
                                }
                                light.fadeIn(1f);

                                lights.put(ship, light);
                                LightShader.addLight(light);
                            }
                        }
                    }
                    case "plasmajets" -> {
                        if (system.isActive()) {
                            Vector2f location = null;
                            if (ship.getEngineController() == null) {
                                break;
                            }
                            int num = 0;
                            for (ShipEngineAPI eng : ship.getEngineController().getShipEngines()) {
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
                                        light.fadeOut(2f);
                                    }
                                }
                            } else {
                                StandardLight light = new StandardLight(location, ZERO, ZERO, null);
                                float intensity = (float) Math.sqrt(shipRadius) / 25f;
                                float size = intensity * 400f;

                                light.setIntensity(intensity);
                                light.setSize(size);
                                Color color = new Color(100, 255, 100, 255);
                                light.setColor(color);
                                light.fadeIn(1f);

                                lights.put(ship, light);
                                LightShader.addLight(light);
                            }
                        }
                    }
                    case "microburn" -> {
                        if (system.isActive()) {
                            Vector2f location = null;
                            if (ship.getEngineController() == null) {
                                break;
                            }
                            int num = 0;
                            for (ShipEngineAPI eng : ship.getEngineController().getShipEngines()) {
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
                                        light.fadeOut(0.5f);
                                    }
                                }
                            } else {
                                StandardLight light = new StandardLight(location, ZERO, ZERO, null);
                                float intensity = (float) Math.sqrt(shipRadius) / 10f;
                                float size = intensity * 200f;

                                light.setIntensity(intensity);
                                light.setSize(size);
                                Color color = new Color(100, 255, 100, 255);
                                light.setColor(color);
                                light.fadeIn(0.5f);

                                lights.put(ship, light);
                                LightShader.addLight(light);
                            }
                        }
                    }
                    case "microburn_omega" -> {
                        if (system.isActive()) {
                            Vector2f location = null;
                            if (ship.getEngineController() == null) {
                                break;
                            }
                            int num = 0;
                            for (ShipEngineAPI eng : ship.getEngineController().getShipEngines()) {
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
                                        light.fadeOut(0.5f);
                                    }
                                }
                            } else {
                                StandardLight light = new StandardLight(location, ZERO, ZERO, null);
                                float intensity = (float) Math.sqrt(shipRadius) / 10f;
                                float size = intensity * 200f;

                                light.setIntensity(intensity);
                                light.setSize(size);
                                Color color = new Color(100, 255, 100, 255);
                                light.setColor(color);
                                light.fadeIn(0.5f);

                                lights.put(ship, light);
                                LightShader.addLight(light);
                            }
                        }
                    }
                    case "combat_burn" -> {
                        if (system.isActive()) {
                            Vector2f location = null;
                            if (ship.getEngineController() == null) {
                                break;
                            }
                            int num = 0;
                            for (ShipEngineAPI eng : ship.getEngineController().getShipEngines()) {
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
                                float intensity = (float) Math.sqrt(shipRadius) / 20f;
                                float size = intensity * 300f;

                                light.setIntensity(intensity);
                                light.setSize(size);
                                Color color = null;
                                if (!ship.getEngineController().getShipEngines().isEmpty()) {
                                    color = ship.getEngineController().getShipEngines().get(0).getEngineColor();
                                }
                                if (color != null) {
                                    light.setColor(color);
                                }
                                light.fadeIn(1f);

                                lights.put(ship, light);
                                LightShader.addLight(light);
                            }
                        }
                    }
                    case "emp" -> {
                        if (system.isActive()) {
                            Vector2f location = ship.getLocation();

                            if (lights.containsKey(ship)) {
                                StandardLight light = lights.get(ship);

                                light.setLocation(location);

                                if ((system.isActive() && !system.isOn()) || system.isChargedown()) {
                                    if (!light.isFadingOut()) {
                                        light.fadeOut(0.5f);
                                    }
                                }
                            } else {
                                StandardLight light = new StandardLight(location, ZERO, ZERO, null);

                                light.setIntensity(0.3f);
                                light.setSize(550f);
                                light.setColor(1.0f, 0.65f, 1.0f);
                                light.fadeIn(0.5f);

                                lights.put(ship, light);
                                LightShader.addLight(light);
                            }
                        }
                    }
                    default -> {
                    }
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

        iter = travelLights.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<ShipAPI, StandardLight> entry = iter.next();
            ShipAPI ship = entry.getKey();

            if ((ship.getTravelDrive() != null && !ship.getTravelDrive().isActive()) || !ship.isAlive()) {
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
        if (engine != null) {
            engine.getCustomData().put(DATA_KEY, new LocalData());
            if (engine.getCustomData().containsKey("noSunPlugin")) {
                return;
            }
            if (GraphicsLibSettings.enableHyperLight()) {
                if (engine.isInCampaign()) {
                    if (Global.getSector().getHyperspace() == Global.getSector().getPlayerFleet().getContainingLocation()) {
                        if (Misc.getAbyssalDepthOfPlayer() < 1f) {
                            engine.addPlugin(new HyperPlugin());
                        }
                    }
                }
            }
            if (GraphicsLibSettings.enableSunLight()) {
                if (engine.isInCampaign()) {
                    List<NearbyPlanetData> stars = getNearbyStars(Global.getSector().getPlayerFleet());
                    for (NearbyPlanetData data : stars) {
                        engine.addPlugin(new SunPlugin(data.offset.length(), data.offset.normalise(data.offset), data.planet, 5f / (float) Math.sqrt(stars.size())));
                    }
                } else {
                    engine.addPlugin(new SunPlugin());
                }
            }
        }
    }

    public static final class HyperPlugin extends BaseEveryFrameCombatPlugin {

        private final Vector3f actualDirection = new Vector3f();
        private final Vector3f baseDirection;
        private final StandardLight hyper;
        private final float sineMScale;
        private float sineTimeM;
        private final float sineTimeMScale;
        private float sineTimeX;
        private final float sineTimeXScale;
        private float sineTimeY;
        private final float sineTimeYScale;
        private float sineTimeZ;
        private final float sineTimeZScale;
        private final float sineXScale;
        private final float sineYScale;
        private final float sineZScale;
        private boolean started = false;

        public HyperPlugin() {
            float magnitude = Global.getSettings().getFloat("sunLightBrightnessScale") / 1.5f;
            magnitude = Math.min(Math.max(magnitude, 0f), Global.getSettings().getFloat("sunLightBrightnessMax"));
            Vector2f horizontal = new Vector2f(((float) Math.random() + 0.0001f) * 2f - 1f, ((float) Math.random()
                    + 0.0001f) * 2f - 1f);
            Misc.normalise(horizontal);
            baseDirection = new Vector3f(horizontal.x, horizontal.y, -0.4f);
            baseDirection.normalise();
            hyper = new StandardLight(baseDirection);
            hyper.setColor(new Color(0, 0, 255));
            hyper.setIntensity(magnitude);
            hyper.setSpecularIntensity(magnitude);
            hyper.makePermanent();
            sineTimeXScale = (float) Math.random() * 2f + 1f;
            sineTimeYScale = (float) Math.random() * 2f + 1f;
            sineTimeZScale = (float) Math.random() * 2f + 1f;
            sineTimeMScale = (float) Math.random() * 2f + 1f;
            sineTimeX = (float) (Math.random() * Math.PI * 2.0);
            sineTimeY = (float) (Math.random() * Math.PI * 2.0);
            sineTimeZ = (float) (Math.random() * Math.PI * 2.0);
            sineTimeM = (float) (Math.random() * Math.PI * 2.0);
            float r = (float) Math.random();
            if (r < 0.33f) {
                sineXScale = (float) Math.random() * 0.4f + 0.4f;
                sineYScale = 0f;
            } else if (r < 0.67f) {
                sineXScale = 0f;
                sineYScale = (float) Math.random() * 0.4f + 0.4f;
            } else {
                sineXScale = (float) Math.random() * 0.4f + 0.4f;
                sineYScale = (float) Math.random() * 0.4f + 0.4f;
            }
            sineZScale = (float) Math.random() * 0.15f + 0.15f;
            sineMScale = (float) Math.random() * 0.1f + 0.1f;
        }

        @Override
        public void advance(float amount, List<InputEventAPI> events) {
            if (!started && (Global.getCombatEngine() != null) && !Global.getCombatEngine().getCustomData().containsKey("noSunPlugin")) {
                started = true;
                LightShader.addLight(hyper);
            }

            if (!Global.getCombatEngine().isPaused()) {
                sineTimeX += amount * sineTimeXScale;
                sineTimeY += amount * sineTimeYScale;
                sineTimeZ += amount * sineTimeZScale;
                sineTimeM += amount * sineTimeMScale;
                actualDirection.set(baseDirection);
                actualDirection.x += Math.sin(sineTimeX) * sineXScale;
                actualDirection.y += Math.sin(sineTimeY) * sineYScale;
                actualDirection.z += Math.sin(sineTimeZ) * sineZScale;
                hyper.setDirection(actualDirection);
                float magnitude = Global.getSettings().getFloat("sunLightBrightnessScale") * (1f + (float) Math.sin(
                        sineTimeM) * sineMScale)
                        / 1.5f;
                magnitude = Math.min(Math.max(magnitude, 0f), Global.getSettings().getFloat("sunLightBrightnessMax"));
                hyper.setIntensity(magnitude);
                hyper.setSpecularIntensity(magnitude * Global.getSettings().getFloat("sunLightSpecularFactor"));
            }
        }

        @Override
        public void init(CombatEngineAPI engine) {
        }
    }

    public static final class SunPlugin extends BaseEveryFrameCombatPlugin {

        private boolean started = false;
        private final StandardLight sun;

        public SunPlugin() {
            float magnitude = 0.35f * Global.getSettings().getFloat("sunLightBrightnessScale");
            magnitude = Math.min(Math.max(magnitude, 0f), Global.getSettings().getFloat("sunLightBrightnessMax"));
            Vector3f direction = new Vector3f(-1f, -1f, -MathUtils.getRandomNumberInRange(
                    Global.getSettings().getFloat("sunLightZComponentMin"),
                    Global.getSettings().getFloat("sunLightZComponentMax")));
            sun = new StandardLight();
            sun.setType(3);
            sun.setDirection(direction);
            sun.setIntensity(magnitude);
            sun.setSpecularIntensity(magnitude * Global.getSettings().getFloat("sunLightSpecularFactor"));
            sun.setColor(1f, 1f, 1f);
            sun.makePermanent();
            LightShader.addLight(sun);
        }

        public SunPlugin(float distance, Vector2f offset, PlanetAPI star, float scale) {
            this(distance, offset, star.getSpec().getCoronaColor(), star.getRadius(), scale);
        }

        public SunPlugin(float distance, Vector2f offset, Color color, float radius, float scale) {
            float magnitude = (float) Math.sqrt(radius / 500f) * (radius + 350f) / (distance + (radius + 350f))
                    * Global.getSettings().getFloat("sunLightBrightnessScale");
            magnitude = Math.min(Math.max(magnitude * scale, 0f), Global.getSettings().getFloat("sunLightBrightnessMax"));
            Vector3f direction = new Vector3f(offset.x, offset.y, -MathUtils.getRandomNumberInRange(
                    Global.getSettings().getFloat("sunLightZComponentMin"),
                    Global.getSettings().getFloat("sunLightZComponentMax")));
            direction.normalise();
            sun = new StandardLight(direction);
            sun.setColor(color);
            sun.setIntensity(magnitude);
            sun.setSpecularIntensity(magnitude * Global.getSettings().getFloat("sunLightSpecularFactor"));
            sun.makePermanent();
        }

        @Override
        public void advance(float amount, List<InputEventAPI> events) {
            if (!started && (Global.getCombatEngine() != null) && !Global.getCombatEngine().getCustomData().containsKey("noSunPlugin")) {
                started = true;
                LightShader.addLight(sun);
            }
        }

        @Override
        public void init(CombatEngineAPI engine) {
        }
    }

    private static final class LocalData {

        final Map<ShipAPI, StandardLight> lights = new LinkedHashMap<>(100);
        final Map<ShipAPI, StandardLight> travelLights = new LinkedHashMap<>(100);

    }

    private static class NearbyPlanetData {

        final Vector2f offset;
        final PlanetAPI planet;

        NearbyPlanetData(Vector2f offset, PlanetAPI planet) {
            this.offset = offset;
            this.planet = planet;
        }
    }
}
