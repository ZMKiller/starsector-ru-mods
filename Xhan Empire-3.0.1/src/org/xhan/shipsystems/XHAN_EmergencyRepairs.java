/*
code by Xaiier
*/

package org.xhan.shipsystems;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineLayers;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.graphics.SpriteAPI;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import org.lazywizard.lazylib.CollisionUtils;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;
import org.magiclib.util.MagicRender;

import java.awt.*;
import java.util.ArrayList;

public class XHAN_EmergencyRepairs extends BaseShipSystemScript {
    public static final float KLAXON_ARC = 45f;
    public static final float KLAXON_ROT_SPEED = 3f; //degrees/frame
    public static final float KLAXON_LIFETIME = 3f;
    public static final Color KLAXON_COLOR = Color.red;
    public static final float HULL_MULT = 0.3f; //fraction of hull repaired
    private static final float ARMOR_MULT = 0.15f; //fraction of total armor repaired (NOTE: this number will be lower than expected due to unhittable cells)
    private static final float MAX_REPAIR = 0.75f; //maximum % a single armor cell can be repaired to
    private static final float WEAPON_REPAIR_MODIFY_MULT = 0.1f; //10x
    private static final float ENGINE_REPAIR_MODIFY_MULT = 0.1f; //10x

    private static final Color[] ARMOR_COLORS = new Color[4]; //instantiated in constructor

    public static boolean GRAPHICSLIB_LOADED = false;
    ArrayList<XHAN_RepairKlaxon> klaxons = new ArrayList<>();
    ArrayList<Vector2f> klaxonPositions = new ArrayList<>(); //instantiated in constructor
    private boolean klaxonsNeedToBeSpawned = true;

    public XHAN_EmergencyRepairs() {
        GRAPHICSLIB_LOADED = Global.getSettings().getModManager().isModEnabled("shaderLib");

        //slight variations in color
        ARMOR_COLORS[0] = new Color(255, 160, 0);
        ARMOR_COLORS[1] = new Color(255, 140, 0);
        ARMOR_COLORS[2] = new Color(255, 120, 0);
        ARMOR_COLORS[3] = new Color(255, 100, 0);

        //positions in ship-space
        klaxonPositions.add(new Vector2f(-161, 26f));
        klaxonPositions.add(new Vector2f(-161, -26f));
        klaxonPositions.add(new Vector2f(-6, 13));
        klaxonPositions.add(new Vector2f(125, 29));
        klaxonPositions.add(new Vector2f(125, -29));
        klaxonPositions.add(new Vector2f(267, -23));
    }

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        //not sure why we need to check for the ship this way, but this is how vanilla does it
        ShipAPI ship = null;
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }

        if (state == State.IN) {
            ship.setJitterUnder(this, ARMOR_COLORS[1], effectLevel, 25, 0f, 4f); //hull border effect

            if (klaxonsNeedToBeSpawned) {
                for (Vector2f klaxonPosition : klaxonPositions) {
                    if (GRAPHICSLIB_LOADED) {
                        XHAN_RepairKlaxon k = new XHAN_RepairKlaxon(ship, klaxonPosition);
                        klaxons.add(k);
                    } else { //fallback sprite
                        //TODO: make sprite that looks more like glib light
                        SpriteAPI sprote = Global.getSettings().getSprite("fx", "Xhan_klaxon");

                        //this will need to be updated based on whatever the sprite ends up being
                        MagicRender.objectspace(sprote, ship, klaxonPosition, new Vector2f(), new Vector2f(256f, 128f), new Vector2f(), MathUtils.getRandomNumberInRange(0f, 360f), KLAXON_ROT_SPEED * 60f, //wants degrees/s
                                true, KLAXON_COLOR, true, 0.5f, KLAXON_LIFETIME, 0.5f, true);
                    }
                }

                klaxonsNeedToBeSpawned = false;
            }
        }

        if (state == State.ACTIVE) {
            ship.setJitterUnder(this, ARMOR_COLORS[1], effectLevel, 25, 0f, 4f); //hull border effect

            //boost normal repairs
            stats.getCombatWeaponRepairTimeMult().modifyMult("XHAN_EmergencyRepairs", WEAPON_REPAIR_MODIFY_MULT);
            stats.getCombatEngineRepairTimeMult().modifyMult("XHAN_EmergencyRepairs", ENGINE_REPAIR_MODIFY_MULT);

            //heal hull
            addHull(ship, ship.getMaxHitpoints() * HULL_MULT / (ship.getPhaseCloak().getSpecAPI().getActive() * (1f / Global.getCombatEngine().getElapsedInLastFrame())));

            //heal armor
            int cellCount = (ship.getArmorGrid().getLeftOf() + ship.getArmorGrid().getRightOf()) * (ship.getArmorGrid().getAbove() + ship.getArmorGrid().getBelow()); //these return linear counts in each cardinal direction from the center
            addArmor(ship, ship.getArmorGrid().getMaxArmorInCell() * cellCount * ARMOR_MULT / (ship.getPhaseCloak().getSpecAPI().getActive() * (1f / Global.getCombatEngine().getElapsedInLastFrame())));

            /*
            //for debugging actual armor percentage (unhittable cells counted)
            float[][] armorGrid = ship.getArmorGrid().getGrid();
            float armor = 0f;
            for (int x = 0; x < armorGrid.length; x++) {
                for (int y = 0; y < armorGrid[x].length; y++) {
                    armor += ship.getArmorGrid().getArmorValue(x, y);
                }
            }
            Global.getLogger(this.getClass()).info("Armor is at " + (armor / (ship.getArmorGrid().getMaxArmorInCell() * cellCount)) * 100  + "%");
             */
        }
        if (state == State.OUT) {
            ship.setJitterUnder(this, ARMOR_COLORS[1], effectLevel, 25, 0f, 4f); //hull border effect
            klaxonsNeedToBeSpawned = true;
            klaxons.clear();
        }

        for (XHAN_RepairKlaxon k : klaxons) {
            k.Update();
        }
    }

    @Override
    public void unapply(MutableShipStatsAPI stats, String id) {
        //remove repair boosts
        stats.getCombatEngineRepairTimeMult().unmodifyMult("XHAN_EmergencyRepairs");
        stats.getCombatWeaponRepairTimeMult().unmodifyMult("XHAN_EmergencyRepairs");
    }

    public StatusData getStatusData(int index, State state, float effectLevel) {
        return null;
    }

    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
        return null;
    }

    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        return true;
    }

    private void addHull(ShipAPI ship, float amount) {
        float newHPLevel = ship.getHitpoints() + amount;
        ship.setHitpoints(Math.min(newHPLevel, ship.getMaxHitpoints()));
    }

    //evenly distributes total amount of armor between damaged cells
    private void addArmor(ShipAPI ship, float amount) {
        float[][] armorGrid = ship.getArmorGrid().getGrid();

        int damagedCellCount = 0;
        for (int x = 0; x < armorGrid.length; x++) {
            for (int y = 0; y < armorGrid[x].length; y++) {
                if (armorGrid[x][y] < ship.getArmorGrid().getMaxArmorInCell() * MAX_REPAIR) {
                    damagedCellCount++;

                    drawVisual(ship, x, y); //the magic
                }
            }
        }

        //Global.getLogger(this.getClass()).info("Applying " + amount + " healing between " + damagedCellCount + " cells");
        for (int x = 0; x < armorGrid.length; x++) {
            for (int y = 0; y < armorGrid[x].length; y++) {
                float newArmorLevel = ship.getArmorGrid().getArmorValue(x, y) + (amount / damagedCellCount); //split the amount between damaged, actually repairable cells
                newArmorLevel = Math.max(Math.min(newArmorLevel, ship.getArmorGrid().getMaxArmorInCell() * MAX_REPAIR), ship.getArmorGrid().getArmorValue(x, y)); //clips to MAX_REPAIR % and stops it from nuking healthy cells
                ship.getArmorGrid().setArmorValue(x, y, newArmorLevel);
            }
        }
        ship.syncWithArmorGridState();
        ship.syncWeaponDecalsWithArmorDamage();
    }

    //this method is full of magic numbers - probably best to leave them alone
    private void drawVisual(ShipAPI ship, int x, int y) {
        float[][] armorGrid = ship.getArmorGrid().getGrid();

        /*
        //full size cells - too jank to use
        Vector2f loc = ship.getArmorGrid().getLocation(x, y); //this is the "easy" way to get cell positions via getLocation
        Vector2f cellOffset = new Vector2f(ship.getArmorGrid().getCellSize() / 2f, -ship.getArmorGrid().getCellSize() / 2f); //cell locations are the corner, move to center
        cellOffset = VectorUtils.rotate(cellOffset, ship.getFacing()); //rotate to ship facing in world
        loc = Vector2f.add(loc, cellOffset, null); //loc already includes ship position
         */

        //quarter cells
        float half = ship.getArmorGrid().getCellSize() / 2f;
        float[] quartersOffsets = {0.5f * half, 1.5f * half};

        if (MathUtils.getRandomNumberInRange(0, 150) == 0) {
            Vector2f offset = new Vector2f(y, -x); //flip axes the right way around
            offset = (Vector2f) offset.scale(ship.getArmorGrid().getCellSize()); //scale to armor grid size
            offset = Vector2f.add(offset, new Vector2f(-armorGrid[x].length / 2f * ship.getArmorGrid().getCellSize(), armorGrid.length / 2f * ship.getArmorGrid().getCellSize()), null); //shift origin from grid corner to ship center
            //offset = Vector2f.add(offset, new Vector2f(ship.getArmorGrid().getCellSize() / 2f, -ship.getArmorGrid().getCellSize() / 2f), null); //cell locations are the corner, move to center
            offset = Vector2f.add(offset, new Vector2f(quartersOffsets[MathUtils.getRandomNumberInRange(0, 1)], -quartersOffsets[MathUtils.getRandomNumberInRange(0, 1)]), null); //offsets already include corner to center

            Vector2f test = new Vector2f(offset); //base ship-space offset
            test = VectorUtils.rotate(test, ship.getFacing()); //rotate to ship facing in world
            test = Vector2f.add(test, ship.getLocation(), null); //shift to ship position in world
            if (CollisionUtils.isPointWithinBounds(test, ship)) { //test that cell center is within bounds
                MagicRender.objectspace(Global.getSettings().getSprite("fx", "Xhan_gridCellLarge"), ship, offset, new Vector2f(), new Vector2f(ship.getArmorGrid().getCellSize(), ship.getArmorGrid().getCellSize()), //this results in half of actual cell size
                        new Vector2f(), MathUtils.getRandomNumberInRange(0, 3) * 90f, 0f, true, ARMOR_COLORS[MathUtils.getRandomNumberInRange(0, 3)], true, 0f, 0f, 0.5f, 1f, 0.1f, 0.1f, 2f, 0.1f, true, CombatEngineLayers.ABOVE_SHIPS_LAYER);
            }
        }

        //sixteenths cells
        float quarter = ship.getArmorGrid().getCellSize() / 4f;
        float[] sixteenthsOffsets = {0.5f * quarter, 1.5f * quarter, 2.5f * quarter, 3.5f * quarter};

        if (MathUtils.getRandomNumberInRange(0, 50) == 0) {
            Vector2f offset = new Vector2f(y, -x); //flip axes the right way around
            offset = (Vector2f) offset.scale(ship.getArmorGrid().getCellSize()); //scale to armor grid size
            offset = Vector2f.add(offset, new Vector2f(-armorGrid[x].length / 2f * ship.getArmorGrid().getCellSize(), armorGrid.length / 2f * ship.getArmorGrid().getCellSize()), null); //shift origin from grid corner to ship center
            //offset = Vector2f.add(offset, new Vector2f(ship.getArmorGrid().getCellSize() / 2f, -ship.getArmorGrid().getCellSize() / 2f), null); //cell locations are the corner, move to center
            offset = Vector2f.add(offset, new Vector2f(sixteenthsOffsets[MathUtils.getRandomNumberInRange(0, 3)], -sixteenthsOffsets[MathUtils.getRandomNumberInRange(0, 3)]), null); //offsets already include corner to center

            Vector2f test = new Vector2f(offset);
            test = VectorUtils.rotate(test, ship.getFacing());
            test = Vector2f.add(test, ship.getLocation(), null);
            if (CollisionUtils.isPointWithinBounds(test, ship)) {
                MagicRender.objectspace(Global.getSettings().getSprite("fx", "Xhan_gridCellSmall"), ship, offset, new Vector2f(), new Vector2f(ship.getArmorGrid().getCellSize() / 2f, ship.getArmorGrid().getCellSize() / 2f), //this results in a quarter of actual cell size
                        new Vector2f(), MathUtils.getRandomNumberInRange(0, 3) * 90f, 0f, true, ARMOR_COLORS[MathUtils.getRandomNumberInRange(0, 3)], true, 0f, 0f, 0.5f, 1f, 0.048f, 0.1f, 2f, 0.1f, true, CombatEngineLayers.ABOVE_SHIPS_LAYER);
            }
        }

        //
    }

}
