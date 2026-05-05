package data.shipsystems.scripts;

import java.awt.Color;

import data.scripts.HSIRenderer.HSIButterflyRenderObject;
import data.scripts.HSIRenderer.HSICombatRendererV2;
import org.lazywizard.lazylib.VectorUtils;
import org.lwjgl.util.vector.Vector2f;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ArmorGridAPI;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MissileAPI;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipSystemAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import com.fs.starfarer.api.util.Misc;

import data.hullmods.HSIEGO.HSIEGOStats;
import data.kit.HSII18nUtil;

public class HSIBurial extends BaseShipSystemScript {
    private ShipAPI ship = null;
    private CombatEngineAPI engine = Global.getCombatEngine();
    private float elapsed = 0f;
    public static final Color KINETIC = new Color(225, 225, 225, 255);
    public static final Color HIGH_EXPLOSIVE = new Color(255, 109, 51, 255);
    public static final Color FRAGMENTATION = new Color(249, 229, 26, 255);
    public static final Color OTHER = new Color(75, 75, 75, 255);
    private boolean start = true;
    private Vector2f from = null;
    private Vector2f to = null;
    public static float RANGE = 2000f;

    public void apply(MutableShipStatsAPI stats, String id, State state, float effectLevel) {
        if (stats.getEntity() instanceof ShipAPI) {
            ship = (ShipAPI) stats.getEntity();
        } else {
            return;
        }
        if (start) {
            from = new Vector2f(ship.getLocation());
            if (ship.getAIFlags().hasFlag(AIFlags.SYSTEM_TARGET_COORDS)) {
                to = new Vector2f((Vector2f) ship.getAIFlags().getCustom(AIFlags.SYSTEM_TARGET_COORDS));
            } else {
                to = new Vector2f(ship.getMouseTarget());
            }
            start = false;
        }
        int level = 0;
        if (ship.hasListenerOfClass(HSIEGOStats.class)) {
            level = ship.getListeners(HSIEGOStats.class).get(0).getLevel();
            //Global.getLogger(this.getClass()).info("EGO level"+level);
        }else{
            //Global.getLogger(this.getClass()).info("EGO not found");
        }
        if (effectLevel > 0) {
            move(state, effectLevel);
        }
        elapsed += engine.getElapsedInLastFrame();
        if (state == State.IN) {
            float seed = (float) Math.random();
            if (effectLevel > 0.5f && !ship.isPhased()) {
                ship.setPhased(true);
            }
            float gap = 0.05f;
            if (level >= 2 && level < 4) {
                gap = 0.033f;
            }
            if (level >= 6) {
                gap = 0.025f;
            }
            if (elapsed > gap) {
                elapsed -= gap;
                MissileAPI proj = (MissileAPI) engine.spawnProjectile(ship, null, "HSI_ButterFly",
                        Misc.getPointAtRadius(ship.getLocation(),
                                effectLevel * ship.getCollisionRadius() * 0.8f),
                        seed * 360f,
                        null);
                double t = Math.random();
                HSICombatRendererV2.getInstance().addFxObject(new HSIButterflyRenderObject(proj,7,15));
                proj.setSpriteAlphaOverride(0f);
                if (t <= 0.2f) {
                    proj.getDamage().setType(DamageType.KINETIC);
                    proj.getEngineController().getFlameColorShifter().setBase(KINETIC);
                } else if (t <= 0.4f) {
                    proj.getDamage().setType(DamageType.HIGH_EXPLOSIVE);
                    proj.getEngineController().getFlameColorShifter().setBase(HIGH_EXPLOSIVE);
                } else if (t <= 0.6f) {
                    proj.getDamage().setType(DamageType.FRAGMENTATION);
                    proj.getDamage().getModifier().modifyMult("HSIEGOButterFlyFRAG", 4f);
                    proj.getEngineController().getFlameColorShifter().setBase(FRAGMENTATION);
                } else if (t <= 0.65) {
                    proj.getDamage().setType(DamageType.OTHER);
                    proj.getEngineController().getFlameColorShifter().setBase(OTHER);
                }
                if (level >= 4) {
                    proj.getDamage().getModifier().modifyMult("HSIEGOButterFly", 1.5f);
                }
            }
            ship.setExtraAlphaMult(Math.max(0,1f - effectLevel) );
        }
        if (effectLevel >= 1) {
            if (level >= 2) {
                if (ship.hasListenerOfClass(HSIEGOStats.class)) {
                    HSIEGOStats ego = ship.getListeners(HSIEGOStats.class).get(0);
                    float canuse = ego.getDamageDealtLegacy() * 0.05f;
                    //Global.getLogger(this.getClass()).info(""+canuse);
                    ego.modifyDamageDealtLegacy(-canuse);
                    if (canuse > 0) {
                        float hploss = ship.getMaxHitpoints() - ship.getHitpoints();
                        if (hploss >= canuse) {
                            ship.setHitpoints(ship.getHitpoints() + canuse);
                            canuse = 0;
                        } else {
                            ship.setHitpoints(ship.getMaxHitpoints());
                            canuse -= hploss;
                        }
                    }
                    if (canuse > 0) {
                        ArmorGridAPI aromr = ship.getArmorGrid();
                        int y = 0;
                        for (float[] row : aromr.getGrid()) {
                            int x = 0;
                            for (float col : row) {
                                if (col < aromr.getMaxArmorInCell()) {
                                    float amloss = aromr.getMaxArmorInCell() - aromr.getArmorValue(x, y);
                                    if (amloss >= canuse) {
                                        aromr.setArmorValue(x, y, Math.max(aromr.getMaxArmorInCell(),
                                                aromr.getArmorValue(x, y) + canuse));
                                        canuse = 0;
                                    } else {
                                        canuse -= amloss;
                                        aromr.setArmorValue(x, y, aromr.getMaxArmorInCell());
                                    }
                                }
                                x++;
                            }
                            y++;
                        }
                    }
                    if (canuse > 0) {
                        ego.modifyDamageDealtLegacy(canuse);
                    }
                }
            }
        }
        if (state == State.OUT) {
            float seed = (float) Math.random();
            if (effectLevel > 0.5f && !ship.isPhased()) {
                ship.setPhased(true);
            }
            float gap = 0.05f;
            if (level >= 2 && level < 4) {
                gap = 0.033f;
            }
            if (level >= 6) {
                gap = 0.025f;
            }
            if (elapsed > gap) {
                elapsed -= gap;
                MissileAPI proj = (MissileAPI) engine.spawnProjectile(ship, null, "HSI_ButterFly",
                        Misc.getPointAtRadius(ship.getLocation(),
                                effectLevel * ship.getCollisionRadius() * 0.8f),
                        seed * 360f,
                        null);
                HSICombatRendererV2.getInstance().addFxObject(new HSIButterflyRenderObject(proj,7,15));
                proj.setSpriteAlphaOverride(0f);
                double t = Math.random();
                if (t <= 0.2f) {
                    proj.getDamage().setType(DamageType.KINETIC);
                    proj.getEngineController().getFlameColorShifter().setBase(KINETIC);
                } else if (t <= 0.4f) {
                    proj.getDamage().setType(DamageType.HIGH_EXPLOSIVE);
                    proj.getEngineController().getFlameColorShifter().setBase(HIGH_EXPLOSIVE);
                } else if (t <= 0.6f) {
                    proj.getDamage().setType(DamageType.FRAGMENTATION);
                    proj.getDamage().getModifier().modifyMult("HSIEGOButterFlyFRAG", 4f);
                    proj.getEngineController().getFlameColorShifter().setBase(FRAGMENTATION);
                } else if (t <= 0.65) {
                    proj.getDamage().setType(DamageType.OTHER);
                    proj.getEngineController().getFlameColorShifter().setBase(OTHER);
                }
                if (level >= 4) {
                    proj.getDamage().getModifier().modifyMult("HSIEGOButterFly", 1.5f);
                }
            }
            if (effectLevel < 0.5f && ship.isPhased()) {
                ship.setPhased(false);

            }
            ship.setExtraAlphaMult(Math.max(0,1f - effectLevel) );
        }
    }

    public void unapply(MutableShipStatsAPI stats, String id) {
        start = true;
        elapsed = 0;
        from = null;
        to = null;
    }

    public String getDisplayNameOverride(State state, float effectLevel) {
        if (ship != null && ship.hasListenerOfClass(HSIEGOStats.class)) {
            int level = ship.getListeners(HSIEGOStats.class).get(0).getLevel();
            if (level >= 2)
                return HSII18nUtil.getShipSystemString("HSIBurial0");
        }
        return null;
    }

    @Override
    public String getInfoText(ShipSystemAPI system, ShipAPI ship) {
        if (!isUsable(system, ship))
            return HSII18nUtil.getShipSystemString("HSIOutOfRange");
        return HSII18nUtil.getShipSystemString("HSIUsable");
    }

    @Override
    public boolean isUsable(ShipSystemAPI system, ShipAPI ship) {
        if(ship.getPhaseCloak()!=null&&ship.getPhaseCloak().isActive()) return false; 
        Vector2f checkfrom = ship.getLocation();
        Vector2f checkto = ship.getMouseTarget();
        if (ship.getAIFlags().hasFlag(AIFlags.SYSTEM_TARGET_COORDS)) {
            checkto = (Vector2f) ship.getAIFlags().getCustom(AIFlags.SYSTEM_TARGET_COORDS);
        }
        return Misc.getDistance(checkfrom, checkto) - ship.getCollisionRadius() < getRange(ship);
    }

    public static float getRange(ShipAPI ship) {
		if (ship == null)
			return RANGE;
		return ship.getMutableStats().getSystemRangeBonus().computeEffective(RANGE);
	}

    private void move(State state, float effectLevel) {
        if (from != null && to != null) {
            if (state == State.IN) {
                double t = (1-effectLevel) * (-Math.PI / 2);
                float frac = (float) ((Math.sin(t) + 1) / 2);
                ship.getLocation().set(from.x + frac * (to.x - from.x), from.y + frac * (to.y - from.y));
            } else if (state == State.OUT) {
                double t = (1 - effectLevel) * (Math.PI / 2);
                float frac = (float) ((Math.sin(t) + 1) / 2);
                ship.getLocation().set(from.x + frac * (to.x - from.x), from.y + frac * (to.y - from.y));
            }
        }
        if(ship.getShipTarget()!=null){
            ship.setFacing(VectorUtils.getAngle(ship.getLocation(), ship.getShipTarget().getLocation()));
        }else{
            ship.setFacing(VectorUtils.getAngle(ship.getLocation(), ship.getMouseTarget()));
        }
    }

}