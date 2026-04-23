package data.scripts.util;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.OfficerDataAPI;
import com.fs.starfarer.api.combat.BattleObjectiveAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipHullSpecAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.mission.FleetSide;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.dark.shaders.util.ShaderLib;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import org.lwjgl.util.vector.Vector2f;

public class UW_Util {

    public static boolean OFFSCREEN = false;
    public static final float OFFSCREEN_GRACE_CONSTANT = 500f;
    public static final float OFFSCREEN_GRACE_FACTOR = 2f;

    /* LazyLib 2.4b revert */
    public static List<DamagingProjectileAPI> getProjectilesWithinRange(Vector2f location, float range) {
        List<DamagingProjectileAPI> projectiles = new ArrayList<>();

        for (DamagingProjectileAPI tmp : Global.getCombatEngine().getProjectiles()) {
            if ((tmp instanceof MissileAPI) || (tmp == null)) {
                continue;
            }

            if (MathUtils.isWithinRange(tmp.getLocation(), location, range)) {
                projectiles.add(tmp);
            }
        }

        return projectiles;
    }

    /* LazyLib 2.4b revert */
    public static List<MissileAPI> getMissilesWithinRange(Vector2f location, float range) {
        List<MissileAPI> missiles = new ArrayList<>();

        for (MissileAPI tmp : Global.getCombatEngine().getMissiles()) {
            if (MathUtils.isWithinRange(tmp.getLocation(), location, range)) {
                missiles.add(tmp);
            }
        }

        return missiles;
    }

    /* LazyLib 2.4b revert */
    public static List<ShipAPI> getShipsWithinRange(Vector2f location, float range) {
        List<ShipAPI> ships = new ArrayList<>();

        for (ShipAPI tmp : Global.getCombatEngine().getShips()) {
            if (tmp.isShuttlePod()) {
                continue;
            }

            if (MathUtils.isWithinRange(tmp, location, range)) {
                ships.add(tmp);
            }
        }

        return ships;
    }

    /* LazyLib 2.4b revert */
    public static List<CombatEntityAPI> getAsteroidsWithinRange(Vector2f location, float range) {
        List<CombatEntityAPI> asteroids = new ArrayList<>();

        for (CombatEntityAPI tmp : Global.getCombatEngine().getAsteroids()) {
            if (MathUtils.isWithinRange(tmp, location, range)) {
                asteroids.add(tmp);
            }
        }

        return asteroids;
    }

    /* LazyLib 2.4b revert */
    public static List<BattleObjectiveAPI> getObjectivesWithinRange(Vector2f location,
            float range) {
        List<BattleObjectiveAPI> objectives = new ArrayList<>();

        for (BattleObjectiveAPI tmp : Global.getCombatEngine().getObjectives()) {
            if (MathUtils.isWithinRange(tmp.getLocation(), location, range)) {
                objectives.add(tmp);
            }
        }

        return objectives;
    }

    /* LazyLib 2.4b revert */
    public static List<CombatEntityAPI> getEntitiesWithinRange(Vector2f location, float range) {
        List<CombatEntityAPI> entities = new ArrayList<>();

        for (CombatEntityAPI tmp : Global.getCombatEngine().getShips()) {
            if (MathUtils.isWithinRange(tmp, location, range)) {
                entities.add(tmp);
            }
        }

        // This also includes missiles
        for (CombatEntityAPI tmp : Global.getCombatEngine().getProjectiles()) {
            if (MathUtils.isWithinRange(tmp, location, range)) {
                entities.add(tmp);
            }
        }

        for (CombatEntityAPI tmp : Global.getCombatEngine().getAsteroids()) {
            if (MathUtils.isWithinRange(tmp, location, range)) {
                entities.add(tmp);
            }
        }

        return entities;
    }

    public static boolean isOnscreen(Vector2f point, float radius) {
        return OFFSCREEN || ShaderLib.isOnScreen(point, radius * OFFSCREEN_GRACE_FACTOR + OFFSCREEN_GRACE_CONSTANT);
    }

    public static void applyForce(CombatEntityAPI target, Vector2f dir, float force) {
        if (target instanceof ShipAPI shipAPI) {
            ShipAPI root = UW_Multi.getRoot(shipAPI);
            float forceRatio = root.getMass() / root.getMassWithModules();
            CombatUtils.applyForce(root, dir, force * forceRatio);
        } else {
            CombatUtils.applyForce(target, dir, force);
        }
    }

    public static String getNonDHullId(ShipHullSpecAPI spec) {
        if (spec == null) {
            return null;
        }
        if (spec.getDParentHullId() != null && !spec.getDParentHullId().isEmpty()) {
            return spec.getDParentHullId();
        } else {
            return spec.getHullId();
        }
    }

    public static int calculatePowerLevel(CampaignFleetAPI fleet) {
        int power = fleet.getFleetPoints();
        int offLvl = 0;
        int cdrLvl = 0;
        boolean commander = false;
        for (OfficerDataAPI officer : fleet.getFleetData().getOfficersCopy()) {
            if (officer.getPerson() == fleet.getCommander()) {
                commander = true;
                cdrLvl = officer.getPerson().getStats().getLevel();
            } else {
                offLvl += officer.getPerson().getStats().getLevel();
            }
        }
        if (!commander) {
            cdrLvl = fleet.getCommanderStats().getLevel();
        }
        power *= Math.sqrt(cdrLvl / 100f + 1f);
        int flatBonus = cdrLvl + offLvl + 10;
        if (power < flatBonus * 2) {
            flatBonus *= power / (float) (flatBonus * 2);
        }
        power += flatBonus;
        return power;
    }

    public static int clamp255(int x) {
        return Math.max(0, Math.min(255, x));
    }

    public static Color colorBlend(Color a, Color b, float amount) {
        float conjAmount = 1f - amount;
        return new Color(clamp255((int) (a.getRed() * conjAmount + b.getRed() * amount)),
                clamp255((int) (a.getGreen() * conjAmount + b.getGreen() * amount)),
                clamp255((int) (a.getBlue() * conjAmount + b.getBlue() * amount)),
                clamp255((int) (a.getAlpha() * conjAmount + b.getAlpha() * amount)));
    }

    public static Color colorJitter(Color color, float amount) {
        return new Color(clamp255((int) (color.getRed() + (int) (((float) Math.random() - 0.5f) * amount))),
                clamp255((int) (color.getGreen() + (int) (((float) Math.random() - 0.5f) * amount))),
                clamp255((int) (color.getBlue() + (int) (((float) Math.random() - 0.5f) * amount))),
                color.getAlpha());
    }

    public static float effectiveRadius(ShipAPI ship) {
        if (ship.getSpriteAPI() == null || ship.isPiece()) {
            return ship.getCollisionRadius();
        } else {
            float fudgeFactor = 1.5f;
            return ((ship.getSpriteAPI().getWidth() / 2f) + (ship.getSpriteAPI().getHeight() / 2f)) * 0.5f * fudgeFactor;
        }
    }

    public static float lerp(float x, float y, float alpha) {
        return (1f - alpha) * x + alpha * y;
    }

    public static void removeMarketInfluence(MarketAPI market, String faction) {
        @SuppressWarnings("unchecked")
        List<String> influences = (List<String>) market.getMemoryWithoutUpdate().get("$ds_market_influences");

        if (influences != null) {
            influences.remove(faction);
        }
    }

    /* Pyrolistical on StackExchange */
    public static double roundToSignificantFigures(double num, int n) {
        if (num == 0) {
            return 0;
        }

        final double d = Math.ceil(Math.log10(num < 0 ? -num : num));
        final int power = n - (int) d;

        final double magnitude = Math.pow(10, power);
        final long shifted = Math.round(num * magnitude);
        return shifted / magnitude;
    }

    public static long roundToSignificantFiguresLong(double num, int n) {
        return Math.round(roundToSignificantFigures(num, n));
    }

    public static void setMarketInfluence(MarketAPI market, String faction) {
        @SuppressWarnings("unchecked")
        List<String> influences = (List<String>) market.getMemoryWithoutUpdate().get("$ds_market_influences");

        if (influences == null) {
            influences = new ArrayList<>(3);
            market.getMemoryWithoutUpdate().set("$ds_market_influences", influences);
        }

        if (!influences.contains(faction)) {
            influences.add(faction);
        }
    }

    public static Vector2f getSafeSpawn(float collisionRadius, FleetSide side, float mapX, float mapY, boolean pursuit) {
        Vector2f spawnLocation = new Vector2f();
        float searchRadius = collisionRadius * 1.25f;
        while (searchRadius > 0) {
            searchRadius -= collisionRadius * 0.05f;
            spawnLocation.x = MathUtils.getRandomNumberInRange(-mapX / 2f, mapX / 2f);
            spawnLocation.y = mapY / 2f;
            if (side == FleetSide.PLAYER) {
                spawnLocation.y *= -1f;
            }
            if (pursuit) {
                spawnLocation.y *= -1f;
            }

            boolean collision = false;
            List<ShipAPI> ships = getShipsWithinRange(spawnLocation, searchRadius);
            for (ShipAPI ship : ships) {
                if (ship.isFighter() || ship.isDrone() || ship.isShuttlePod() || ship.isPiece()) {
                    continue;
                }
                collision = true;
            }

            if (!collision) {
                break;
            }
        }

        return spawnLocation;
    }

    public static float getMemberRadiusEstimate(FleetMemberAPI member) {
        float radius = 500f;
        if (member.getHullSpec().getShieldSpec() != null) {
            radius = member.getHullSpec().getShieldSpec().getRadius();
        } else if (!member.getHullSpec().getSpriteName().isEmpty()) {
            SpriteAPI sprite = Global.getSettings().getSprite(member.getHullSpec().getSpriteName());
            float fudgeFactor = 1.5f;
            radius = ((sprite.getWidth() / 2f) + (sprite.getHeight() / 2f)) * 0.5f * fudgeFactor;
        }
        return radius;
    }
}
